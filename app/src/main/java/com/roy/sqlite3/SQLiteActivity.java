package com.roy.sqlite3;

import static com.roy.sqlite3.utils.AndroidUtil.dp;
import static com.roy.sqlite3.utils.AndroidUtil.makeShape;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.roy.database.SQLiteCursor;
import com.roy.database.SQLiteDatabase;
import com.roy.database.SQLiteException;
import com.roy.database.SQLiteStatement;
import com.roy.sqlite3.adapter.SQLiteListAdapter;
import com.roy.sqlite3.data.Student;
import com.roy.sqlite3.utils.AndroidUtil;
import com.roy.sqlite3.utils.ToastUtil;
import com.roy.sqlite3.widget.EditStudentView;
import com.roy.sqlite3.widget.EmptyView;
import com.roy.sqlite3.widget.LoadingView;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Roy on 2021/9/26.
 * SQLite3 简单使用示例
 */
public class SQLiteActivity extends Activity {
  private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
  private final Context context = this;
  private final SQLiteListAdapter adapter = new SQLiteListAdapter();
  private EmptyView emptyView = null;
  private LoadingView loadingView = null;
  private String dbname = null;
  private byte[] avatar = null;

  /**
   * 插入数据（这里为了方便 生成的数据都是写死的）
   */
  private void insertStudent() {
    SQLiteDatabase db = null;
    SQLiteStatement stmt = null;
    try {
      db = SQLiteDatabase.open(dbname);
      if (!db.isTableExists("Student")) {
        db.compile("CREATE TABLE Student (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT," +
            "age INTEGER," +
            "height DECIMAL," +
            "avatar BLOB" +
            ");")
            .stepThis()
            .dispose();
      }
      db.beginTransaction();
      stmt = db.compile("INSERT INTO Student(name,age,height,avatar) VALUES(?,?,?,?);")
          .bind(new Object[]{"Roy", 27, 178.5, defaultAvatar()});
      int code = stmt.step();
      if (1 != code)
        throw new SQLiteException("call step failed.");

      db.commitTransaction();
      final List<Student> students = selectStudents();
      AndroidUtil.runOnUIThread(() -> {
        adapter.setStudents(students);
        adapter.notifyDataSetChanged();
      });
    } catch (final SQLiteException e) {
      AndroidUtil.runOnUIThread(() -> ToastUtil.showToast(e.getMessage()));
      e.printStackTrace();
    } finally {
      if (null != stmt) stmt.dispose();
      closeSafely(db);
    }
  }

  /**
   * 删除数据
   *
   * @param student 要删除的数据（为空删除所有！）
   */
  private void deleteStudent(final Student student) {
    SQLiteDatabase db = null;
    try {
      db = SQLiteDatabase.open(dbname);
      if (db.isTableExists("Student")) {
        if (null == student) {  //删除所有
          db.compile("DELETE FROM Student;").stepThis().dispose();
        } else {                //删除单条
          db.compile("DELETE FROM Student WHERE id=?;").bind(new Object[]{student.getId()}).stepThis().dispose();
        }
        final List<Student> students = selectStudents();
        AndroidUtil.runOnUIThread(() -> {
          adapter.setStudents(students);
          adapter.notifyDataSetChanged();
        });
      }
    } catch (SQLiteException e) {
      AndroidUtil.runOnUIThread(() -> ToastUtil.showToast(e.getMessage()));
      e.printStackTrace();
    } finally {
      closeSafely(db);
    }
  }

  /**
   * 修改数据
   */
  private void updateStudent(final Student student) {
    if (null == student) {
      return;
    }
    SQLiteDatabase db = null;
    SQLiteStatement stmt = null;
    try {
      db = SQLiteDatabase.open(dbname);
      stmt = db.compile("UPDATE Student SET name=?, age=?, height=? WHERE id=?;")
          .bind(new Object[]{student.getName(), student.getAge(), student.getHeight(), student.getId()});
      final int code = stmt.step();
      if (1 != code)
        throw new SQLiteException("更新数据失败：code = [" + code + "]");

      final List<Student> students = selectStudents();
      AndroidUtil.runOnUIThread(() -> {
        adapter.setStudents(students);
        adapter.notifyDataSetChanged();
      });
    } catch (SQLiteException e) {
      AndroidUtil.runOnUIThread(() -> ToastUtil.showToast(e.getMessage()));
      e.printStackTrace();
    } finally {
      if (null != stmt) stmt.dispose();
      closeSafely(db);
    }
  }

  /**
   * 查询数据
   *
   * @return Student 表中的所有数据
   */
  private List<Student> selectStudents() {
    final List<Student> students = new ArrayList<>();
    SQLiteDatabase db = null;
    SQLiteCursor cursor = null;
    try {
      db = SQLiteDatabase.open(dbname);
      if (db.isTableExists("Student")) {
        cursor = db.compile("SELECT * FROM Student WHERE height>?;").query(new Object[]{0.0d});
        while (cursor.next()) {
          final long id = cursor.getInt64(0);
          final String name = cursor.getString(1);
          final int age = cursor.getInt32(2);
          final double height = cursor.getDouble(3);
          final byte[] avatar = cursor.getBlob(4);
          final Student student = new Student();
          student.setId(id);
          student.setAge(age);
          student.setName(name);
          student.setHeight(height);
          student.setAvatar(avatar);
          students.add(student);
        }
      }
    } catch (final SQLiteException e) {
      AndroidUtil.runOnUIThread(() -> ToastUtil.showToast(e.getMessage()));
      e.printStackTrace();
    } finally {
      if (null != cursor) cursor.dispose();
      closeSafely(db);
    }
    return students;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(new ContentView());
    this.dbname = App.getApp().getFilesDir().getAbsolutePath() + "/database/roy.db";
    final Window window = getWindow();
    final View parent;
    if (null != window && null != (parent = window.getDecorView()) && parent instanceof ViewGroup) {
      ((ViewGroup) parent).addView(emptyView);
    }
    final ActionBar actionBar = getActionBar();
    if (null != actionBar) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeAsUpIndicator(R.drawable.ic_ab_close);
    }
    AndroidUtil.runOnUIThread(this::executeSelect, 100);
  }

  @Override
  public void onBackPressed() {
    if (null != loadingView && View.VISIBLE == loadingView.getVisibility()) {
      loadingView.setVisibility(View.GONE);
    } else {
      super.onBackPressed();
    }
  }

  private void executeInsert() {
    loadingView.setVisibility(View.VISIBLE);
    loadingView.setLoadingText("正在插入...");
    cachedThreadPool.execute(() -> {
      insertStudent();
      AndroidUtil.runOnUIThread(() -> loadingView.setVisibility(View.GONE));
    });
  }

  private void executeSelect() {
    loadingView.setVisibility(View.VISIBLE);
    loadingView.setLoadingText("正在查询...");
    cachedThreadPool.execute(() -> {
      final List<Student> students = selectStudents();
      AndroidUtil.runOnUIThread(() -> {
        loadingView.setVisibility(View.GONE);
        adapter.setStudents(students);
        adapter.notifyDataSetChanged();
      });
    });
  }

  private void executeUpdate(final Student student) {
    loadingView.setVisibility(View.VISIBLE);
    loadingView.setLoadingText("正在修改...");
    cachedThreadPool.execute(() -> {
      updateStudent(student);
      AndroidUtil.runOnUIThread(() -> loadingView.setVisibility(View.GONE));
    });
  }

  private void executeDelete(final Student student) {
    loadingView.setVisibility(View.VISIBLE);
    loadingView.setLoadingText("正在删除...");
    cachedThreadPool.execute(() -> {
      deleteStudent(student);
      AndroidUtil.runOnUIThread(() -> loadingView.setVisibility(View.GONE));
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_sqlite, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (android.R.id.home == item.getItemId()) {
      onBackPressed();
      return true;
    } else if (R.id.delete == item.getItemId()) {
      final AlertDialog dialog = new AlertDialog.Builder(context)
          .setTitle("警告")
          .setIcon(R.drawable.ic_warning)
          .setMessage("确定要删除所有数据吗？删除之后不可恢复！")
          .setPositiveButton("删除", (dlg, which) -> {
            executeDelete(null);
            dlg.dismiss();
          })
          .setNegativeButton("不", (dlg, which) -> dlg.dismiss())
          .setCancelable(true)
          .create();
      dialog.setCanceledOnTouchOutside(false);
      dialog.show();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  private byte[] defaultAvatar() {
    if (null == avatar) {
      final Bitmap bitmap = BitmapFactory.decodeResource(App.getApp().getResources(), R.drawable.avatar);
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
      avatar = baos.toByteArray();
      closeSafely(baos);
    }
    return avatar;
  }

  private static void closeSafely(final Closeable closeable) {
    if (null != closeable) {
      try {
        closeable.close();
      } catch (IOException ignored) {
      }
    }
  }

  private class ContentView extends RelativeLayout {

    public ContentView() {
      super(context);
      final Drawable buttonShape = makeShape(AndroidUtil.getColor(R.color.teal_200), 4, 0, 0);
      final int buttonLayoutId = 1;

      emptyView = new EmptyView(context);

      final Button buttonInsert = new Button(context);
      final LinearLayout.LayoutParams insertLayoutParams = new LinearLayout.LayoutParams(0, -2, 1f);
      insertLayoutParams.setMargins(dp(16), dp(4), dp(4), dp(8));
      buttonInsert.setLayoutParams(insertLayoutParams);
      buttonInsert.setTextColor(Color.WHITE);
      buttonInsert.setBackground(buttonShape);
      buttonInsert.setText("插入数据");

      final Button buttonSelect = new Button(context);
      final LinearLayout.LayoutParams selectLayoutParams = new LinearLayout.LayoutParams(0, -2, 1f);
      selectLayoutParams.setMargins(dp(4), dp(4), dp(16), dp(8));
      buttonSelect.setLayoutParams(selectLayoutParams);
      buttonSelect.setTextColor(Color.WHITE);
      buttonSelect.setBackground(buttonShape);
      buttonSelect.setText("查询数据");

      final LinearLayout buttonLayout = new LinearLayout(context);
      final LayoutParams buttonLayoutParams = new LayoutParams(-1, -2);
      buttonLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
      buttonLayout.setLayoutParams(buttonLayoutParams);
      buttonLayout.setId(buttonLayoutId);
      buttonLayout.addView(buttonInsert);
      buttonLayout.addView(buttonSelect);

      final ListView listView = new ListView(context);
      final LayoutParams listViewLayoutParams = new LayoutParams(-1, -1);
      listViewLayoutParams.addRule(ABOVE, buttonLayoutId);
      listView.setLayoutParams(listViewLayoutParams);
      listView.setEmptyView(emptyView);
      listView.setOnItemClickListener((parent, view, position, id) -> {
        final EditStudentView editView = new EditStudentView(context);
        editView.setStudent(adapter.getItem(position));
        final AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("编辑")
            .setView(editView)
            .setPositiveButton("修改", (dlg, which) -> {
              executeUpdate(editView.getStudent());
              dlg.dismiss();
            })
            .setNegativeButton("放弃", (dlg, which) -> dlg.dismiss())
            .setCancelable(true)
            .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
      });
      listView.setOnItemLongClickListener((parent, view, position, id) -> {
        final AlertDialog dialog = new AlertDialog.Builder(context)
            .setTitle("警告")
            .setMessage("确定要删除这条数据吗？删除之后不可恢复！")
            .setPositiveButton("删除", (dlg, which) -> {
              executeDelete(adapter.getItem(position));
              dlg.dismiss();
            })
            .setNegativeButton("不", (dlg, which) -> dlg.dismiss())
            .setCancelable(true)
            .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        return true;
      });
      listView.setAdapter(adapter);

      loadingView = new LoadingView(context);
      loadingView.setLayoutParams(new LayoutParams(-1, -1));
      loadingView.setLoadingText("加载中...");
      loadingView.setVisibility(View.GONE);

      addView(listView);
      addView(buttonLayout);
      addView(loadingView);

      buttonInsert.setOnClickListener(v -> executeInsert());
      buttonSelect.setOnClickListener(v -> executeSelect());
    }

  }

}