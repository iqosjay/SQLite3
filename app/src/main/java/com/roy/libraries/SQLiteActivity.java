package com.roy.libraries;

import static com.roy.libraries.utils.AndroidUtil.dp;
import static com.roy.libraries.utils.AndroidUtil.getActionBarHeight;
import static com.roy.libraries.utils.AndroidUtil.makeShape;
import static com.roy.libraries.utils.AndroidUtil.screenWidth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import com.roy.database.SQLiteCursor;
import com.roy.database.SQLiteDatabase;
import com.roy.database.SQLiteException;
import com.roy.database.SQLiteStatement;
import com.roy.libraries.adapter.SQLiteListAdapter;
import com.roy.libraries.data.Student;
import com.roy.libraries.utils.AndroidUtil;
import com.roy.libraries.utils.ToastUtil;
import com.roy.libraries.widget.EmptyView;
import com.roy.libraries.widget.LoadingView;

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
      final List<Student> students = selectStudent();
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
        AndroidUtil.runOnUIThread(this::executeSelect);
      }
    } catch (SQLiteException e) {
      AndroidUtil.runOnUIThread(() -> ToastUtil.showToast(e.getMessage()));
      e.printStackTrace();
    } finally {
      closeSafely(db);
    }
  }

  /**
   * 查询数据
   *
   * @return Student 表中的所有数据
   */
  private List<Student> selectStudent() {
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
      final List<Student> students = selectStudent();
      AndroidUtil.runOnUIThread(() -> {
        loadingView.setVisibility(View.GONE);
        adapter.setStudents(students);
        adapter.notifyDataSetChanged();
      });
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

  private class ContentView extends ViewGroup {
    private final int dp16 = dp(16);
    private final int dp8 = dp16 >> 1;
    private final Button buttonInsert;
    private final Button buttonSelect;
    private final ListView listView;

    public ContentView() {
      super(context);
      buttonInsert = new Button(context);
      buttonSelect = new Button(context);
      listView = new ListView(context);
      emptyView = new EmptyView(context);

      final int halfWidth = screenWidth >> 1;
      final int dp24 = dp16 + dp8;

      buttonInsert.setLayoutParams(new LayoutParams(halfWidth - dp24, -2));
      buttonInsert.setTextColor(Color.WHITE);
      buttonInsert.setBackground(makeShape(AndroidUtil.getColor(R.color.teal_200), 4, 0, 0));
      buttonInsert.setText("插入数据");

      buttonSelect.setLayoutParams(new LayoutParams(halfWidth - dp24, -2));
      buttonSelect.setTextColor(Color.WHITE);
      buttonSelect.setBackground(makeShape(AndroidUtil.getColor(R.color.teal_200), 4, 0, 0));
      buttonSelect.setText("查询数据");

      listView.setLayoutParams(new LayoutParams(-1, -1));
      listView.setEmptyView(emptyView);
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

      addView(buttonInsert);
      addView(buttonSelect);
      addView(listView);
      addView(loadingView);

      buttonInsert.setOnClickListener(v -> executeInsert());
      buttonSelect.setOnClickListener(v -> executeSelect());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
      final int heightMode = MeasureSpec.getMode(widthMeasureSpec);
      final int widthSpec;
      final int heightSpec;
      if (MeasureSpec.AT_MOST == widthMode || MeasureSpec.UNSPECIFIED == widthMode) {
        widthSpec = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY);
      } else {
        widthSpec = widthMeasureSpec;
      }
      if (MeasureSpec.AT_MOST == heightMode || MeasureSpec.UNSPECIFIED == heightMode) {
        heightSpec = MeasureSpec.makeMeasureSpec(getActionBarHeight(), MeasureSpec.EXACTLY);
      } else {
        heightSpec = heightMeasureSpec;
      }
      super.onMeasure(widthSpec, heightSpec);
      measureChildren(widthSpec, heightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      buttonInsert.layout(l + dp16,
        b - dp8 - buttonInsert.getMeasuredHeight(),
        l + (screenWidth >> 1) - dp8,
        b - dp8
      );
      buttonSelect.layout(l + (screenWidth >> 1) + dp8,
        b - dp8 - buttonSelect.getMeasuredHeight(),
        r - dp16,
        b - dp8
      );
      listView.layout(l, t, r, b - buttonInsert.getMeasuredHeight() - dp16);
      loadingView.layout(l, t, r, b);
    }

  }

}