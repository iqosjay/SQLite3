package com.roy.sqlite3.widget;

import static com.roy.sqlite3.utils.AndroidUtil.dp;
import static com.roy.sqlite3.utils.AndroidUtil.makeShape;
import static com.roy.sqlite3.utils.AndroidUtil.sp;

import android.content.Context;
import android.graphics.Color;
import android.media.session.MediaController;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.roy.sqlite3.data.Student;

import java.util.Locale;

/**
 * Created by Roy on 2021/9/29.
 * 修改学生的信息弹窗布局
 */
public class EditStudentView extends LinearLayout {

  private final EditText editName;
  private final EditText editAge;
  private final EditText editHeight;

  private Student student = null;

  public EditStudentView(final Context context) {
    super(context);

    setOrientation(VERTICAL);


    editName = createEditText(context);
    editName.setHint("姓名");

    editAge = createEditText(context);
    editAge.setHint("年龄");
    editAge.setInputType(InputType.TYPE_CLASS_NUMBER);
    editAge.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

    editHeight = createEditText(context);
    editHeight.setHint("身高");
    editHeight.setImeOptions(EditorInfo.IME_ACTION_DONE);
    editHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    editHeight.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

    final LinearLayout nameLayout = createItem(context, "姓名：");
    final LinearLayout ageLayout =createItem(context, "年龄：");
    final LinearLayout heightLayout =createItem(context, "身高：");
    nameLayout.addView(editName);
    ageLayout.addView(editAge);
    heightLayout.addView(editHeight);

    addView(nameLayout);
    addView(ageLayout);
    addView(heightLayout);

  }


  public void setStudent(Student student) {
    this.student = student;
    editAge.setText(String.valueOf(student.getAge()));
    editName.setText(student.getName());
    editHeight.setText(String.format(Locale.CHINA, "%1$2f", student.getHeight()));
  }

  public Student getStudent() {
    if (null == student) {
      return null;
    }
    final Student copy = student.clone();
    final String name = editName.getText().toString();
    if (!TextUtils.isEmpty(name)) {
      copy.setName(name);
    }
    try {
      copy.setAge(Integer.parseInt(editAge.getText().toString()));
    } catch (NumberFormatException ignored) {
    }
    try {
      copy.setHeight(Double.parseDouble(editHeight.getText().toString()));
    } catch (NumberFormatException ignored) {
    }
    return copy;
  }

  private static LinearLayout createItem(final Context context, final String text) {
    final LinearLayout linearLayout = new LinearLayout(context);
    final LayoutParams layoutParams = new LayoutParams(-1, -1);
    layoutParams.setMargins(dp(24), dp(8), dp(24), 0);
    linearLayout.setOrientation(HORIZONTAL);
    linearLayout.addView(createTextView(context, text));
    linearLayout.setLayoutParams(layoutParams);
    return linearLayout;
  }

  private static TextView createTextView(final Context context, final String text) {
    final TextView textView = new TextView(context);
    textView.setLayoutParams(new LayoutParams(-2, -2));
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(18));
    textView.setTextColor(Color.BLACK);
    textView.setSingleLine(true);
    textView.setText(text);
    return textView;
  }

  private static EditText createEditText(final Context context) {
    final EditText editText = new EditText(context);
    final LayoutParams layoutParams = new LayoutParams(-1, -2);
    editText.setBackground(makeShape(0, 4, 0xffefeff4, 1));
    editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(18));
    editText.setPadding(dp(8), dp(8), dp(8), dp(8));
    editText.setSingleLine(true);
    editText.setTextColor(Color.BLACK);
    editText.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) {
        editText.setSelection(editText.getText().length());
      }
    });
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    editText.setInputType(InputType.TYPE_CLASS_TEXT);
    editText.setLayoutParams(layoutParams);
    return editText;
  }

}
