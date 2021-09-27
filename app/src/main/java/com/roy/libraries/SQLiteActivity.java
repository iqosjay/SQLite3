package com.roy.libraries;

import static com.roy.libraries.utils.AndroidUtil.dp;
import static com.roy.libraries.utils.AndroidUtil.makeShape;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.roy.database.SQLiteCursor;
import com.roy.database.SQLiteDatabase;
import com.roy.database.SQLiteException;
import com.roy.database.SQLiteStatement;
import com.roy.libraries.adapter.SQLiteListAdapter;
import com.roy.libraries.data.Student;
import com.roy.libraries.utils.AndroidUtil;
import com.roy.libraries.utils.ToastUtil;
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
  private LoadingView loadingView = null;
  private String dbname = null;
  private byte[] avatar = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.dbname = App.getApp().getFilesDir().getAbsolutePath() + "/database/roy.db";
    this.setContentView(new ContentView());
    AndroidUtil.runOnUIThread(this::executeQuery, 200);
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

  private void executeQuery() {
    loadingView.setVisibility(View.VISIBLE);
    loadingView.setLoadingText("正在查询...");
    cachedThreadPool.execute(() -> {
      final List<Student> students = selectStudent();
      AndroidUtil.runOnUIThread(() -> {
        loadingView.setVisibility(View.GONE);
        ToastUtil.showToast("刷新成功，共有 [" + students.size() + "] 条数据.");
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

  private void deleteStudent(final Student student) {
    SQLiteDatabase db = null;
    try {
      db = SQLiteDatabase.open(dbname);
      if (db.isTableExists("Student")) {
        db.compile("DELETE FROM Student WHERE id=?;").bind(new Object[]{student.getId()}).stepThis().dispose();
        AndroidUtil.runOnUIThread(() -> {
          ToastUtil.showToast("删除成功！");
          executeQuery();
        });
      }
    } catch (SQLiteException e) {
      AndroidUtil.runOnUIThread(() -> ToastUtil.showToast(e.getMessage()));
      e.printStackTrace();
    } finally {
      closeSafely(db);
    }
  }

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
        ToastUtil.showToast("插入数据成功！");
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
      AndroidUtil.runOnUIThread(() -> loadingView.setVisibility(View.GONE));
      if (null != cursor) cursor.dispose();
      closeSafely(db);
    }
    return students;
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

      final int buttonLayoutId = 1;

      final LinearLayout buttonLayout = new LinearLayout(context);
      final LayoutParams buttonParams = new LayoutParams(-1, -2);
      buttonParams.addRule(ALIGN_PARENT_BOTTOM);
      buttonParams.setMargins(0, dp(8), 0, dp(8));
      buttonLayout.setId(buttonLayoutId);
      buttonLayout.setLayoutParams(buttonParams);
      buttonLayout.setPadding(dp(16), 0, dp(16), 0);
      buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

      final Button buttonInsert = new Button(context);
      final LinearLayout.LayoutParams insertButtonLayoutParams = new LinearLayout.LayoutParams(0, -2, 1f);
      insertButtonLayoutParams.setMargins(0, dp(4), dp(4), dp(4));
      buttonInsert.setLayoutParams(insertButtonLayoutParams);
      buttonInsert.setTextColor(Color.WHITE);
      buttonInsert.setBackground(makeShape(AndroidUtil.getColor(R.color.teal_200), 4, 0, 0));
      buttonInsert.setText("插入数据");

      final Button buttonSelect = new Button(context);
      final LinearLayout.LayoutParams selectButtonLayoutParams = new LinearLayout.LayoutParams(0, -2, 1f);
      selectButtonLayoutParams.setMargins(dp(4), dp(4), 0, dp(4));
      buttonSelect.setLayoutParams(selectButtonLayoutParams);
      buttonSelect.setTextColor(Color.WHITE);
      buttonSelect.setBackground(makeShape(AndroidUtil.getColor(R.color.teal_200), 4, 0, 0));
      buttonSelect.setText("查询数据");

      final ListView listView = new ListView(context);
      final LayoutParams listViewLayoutParams = new LayoutParams(-1, -1);
      listViewLayoutParams.addRule(ABOVE, buttonLayoutId);
      listView.setLayoutParams(listViewLayoutParams);
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

      buttonLayout.addView(buttonInsert);
      buttonLayout.addView(buttonSelect);

      addView(buttonLayout);
      addView(listView);
      addView(loadingView);

      buttonInsert.setOnClickListener(v -> executeInsert());
      buttonSelect.setOnClickListener(v -> executeQuery());
    }

  }

}