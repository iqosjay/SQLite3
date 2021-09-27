package com.roy.libraries;

import static com.roy.libraries.utils.AndroidUtil.dp;
import static com.roy.libraries.utils.AndroidUtil.makeShape;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.roy.database.SQLiteCursor;
import com.roy.database.SQLiteDatabase;
import com.roy.database.SQLiteException;
import com.roy.database.SQLiteStatement;
import com.roy.libraries.adapter.SQLiteListAdapter;
import com.roy.libraries.data.Student;
import com.roy.libraries.utils.AndroidUtil;
import com.roy.libraries.utils.ToastUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteActivity extends Activity {
  private final Context context = this;
  private final SQLiteListAdapter adapter = new SQLiteListAdapter();
  private String dbname = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.dbname = App.getApp().getFilesDir().getAbsolutePath() + "/database/roy.db";
    this.setContentView(new ContentView());
  }

  private void insertStudent() {
    SQLiteDatabase db = null;
    SQLiteStatement stmt = null;
    try {
      db = SQLiteDatabase.open(dbname);
      if (!db.isTableExists("Student")) {
        db.compile("CREATE TABLE Student (" +
          "id INTEGER PRIMARY KEY AUTOINCREMENT," +
          "sex INTEGER," +
          "age INTEGER," +
          "name TEXT," +
          "number TEXT," +
          "height DECIMAL," +
          "weight DECIMAL," +
          "tel TEXT" +
          ");")
          .stepThis()
          .dispose();
      }
      final String uuid = UUID.randomUUID().toString();
      db.beginTransaction();
      stmt = db.compile("INSERT INTO Student(sex,age,name,number,height,weight,tel) " +
        "VALUES(?,?,?,?,?,?,?);")
        .bind(new Object[]{1, 27, "Roy", String.valueOf(uuid.hashCode()), 178.5, 74.2, "13666666666"});
      stmt.step();
      db.commitTransaction();
    } catch (SQLiteException e) {
      ToastUtil.showToast(e.getMessage());
      e.printStackTrace();
    } finally {
      if (null != stmt) stmt.dispose();
      closeSafely(db);
    }
  }

  private void selectStudent() {
    final List<Student> students = new ArrayList<>();
    SQLiteDatabase db = null;
    SQLiteCursor cursor = null;
    try {
      db = SQLiteDatabase.open(dbname);
      if (db.isTableExists("Student")) {
        cursor = db.compile("SELECT * FROM Student WHERE sex=?;").query(new Object[]{1});
        while (cursor.next()) {
          final long id = cursor.getInt64(0);
          final int sex = cursor.getInt32(1);
          final int age = cursor.getInt32(2);
          final String name = cursor.getString(3);
          final String number = cursor.getString(4);
          final double height = cursor.getDouble(5);
          final double weight = cursor.getDouble(6);
          final String tel = cursor.getString(7);
          final Student student = new Student();
          student.setId(id);
          student.setSex(sex);
          student.setAge(age);
          student.setName(name);
          student.setNumber(number);
          student.setHeight(height);
          student.setWeight(weight);
          student.setTel(tel);
          students.add(student);
        }
      }
    } catch (SQLiteException e) {
      ToastUtil.showToast(e.getMessage());
      e.printStackTrace();
    } finally {
      if (null != cursor) cursor.dispose();
      closeSafely(db);
    }
    ToastUtil.showToast("total amount [" + students.size() + "]");
    adapter.setStudents(students);
    adapter.notifyDataSetChanged();
  }

  private static void closeSafely(final Closeable closeable) {
    if (null != closeable) {
      try {
        closeable.close();
      } catch (IOException ignored) {
      }
    }
  }

  private class ContentView extends LinearLayout {

    public ContentView() {
      super(context);
      setOrientation(VERTICAL);
      final ListView listView = new ListView(context);
      listView.setLayoutParams(new LayoutParams(-1, 0, 1f));
      listView.setAdapter(adapter);

      final LinearLayout buttonLayout = new LinearLayout(context);
      final LayoutParams layoutParams = new LayoutParams(-1, -2);
      layoutParams.setMargins(0, dp(8), 0, dp(8));
      buttonLayout.setLayoutParams(layoutParams);
      buttonLayout.setPadding(dp(16), 0, dp(16), 0);
      buttonLayout.setOrientation(HORIZONTAL);

      final Button buttonInsert = new Button(context);
      final LayoutParams insertButtonLayoutParams = new LayoutParams(0, -2, 1f);
      insertButtonLayoutParams.setMargins(0, dp(4), dp(4), dp(4));
      buttonInsert.setLayoutParams(insertButtonLayoutParams);
      buttonInsert.setTextColor(Color.WHITE);
      buttonInsert.setBackground(makeShape(AndroidUtil.getColor(R.color.teal_200), 4, 0, 0));
      buttonInsert.setText("插入数据");

      final Button buttonSelect = new Button(context);
      final LayoutParams selectButtonLayoutParams = new LayoutParams(0, -2, 1f);
      selectButtonLayoutParams.setMargins(dp(4), dp(4), 0, dp(4));
      buttonSelect.setLayoutParams(selectButtonLayoutParams);
      buttonSelect.setTextColor(Color.WHITE);
      buttonSelect.setBackground(makeShape(AndroidUtil.getColor(R.color.teal_200), 4, 0, 0));
      buttonSelect.setText("查询数据");

      buttonLayout.addView(buttonInsert);
      buttonLayout.addView(buttonSelect);
      addView(listView);
      addView(buttonLayout);


      buttonInsert.setOnClickListener(v -> insertStudent());
      buttonSelect.setOnClickListener(v -> selectStudent());
    }

  }

}