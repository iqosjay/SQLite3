package com.roy.sqlite3.widget;

import static com.roy.sqlite3.utils.AndroidUtil.dp;
import static com.roy.sqlite3.utils.AndroidUtil.makeShape;
import static com.roy.sqlite3.utils.AndroidUtil.sp;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.roy.sqlite3.data.Student;

import java.util.Locale;

/**
 * Created by Roy on 2021/9/29.
 * 修改学生的信息弹窗布局
 */
public class EditStudentView extends ViewGroup {

  private final EditText editName;
  private final EditText editAge;
  private final EditText editHeight;

  private Student student = null;

  public EditStudentView(final Context context) {
    super(context);

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

    addView(editName);
    addView(editAge);
    addView(editHeight);

  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    measureChildren(widthMeasureSpec, heightMeasureSpec);
    final int parentWidth = getMeasuredWidth();
    final int editTextHeight = editName.getMeasuredHeight();
    editName.getLayoutParams().width = parentWidth - dp(24);
    editAge.getLayoutParams().width = parentWidth - dp(24);
    editHeight.getLayoutParams().width = parentWidth - dp(24);
    setMeasuredDimension(parentWidth, editTextHeight * 3 + dp(32));
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int childCount = getChildCount();
    final int x = dp(24);
    int y = t + dp(16);
    for (int i = 0; i != childCount; ++i) {
      final View child = getChildAt(i);
      child.layout(x, y, x + child.getMeasuredWidth(), y += child.getMeasuredHeight());
      y += dp(8);
    }
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
    final String name = copy.getName();
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

  private static EditText createEditText(final Context context) {
    final EditText editText = new EditText(context);
    final LayoutParams layoutParams = new LayoutParams(-1, -2);
    editText.setBackground(makeShape(0, 4, 0xffefeff4, 1));
    editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(16));
    editText.setPadding(dp(8), dp(8), dp(8), dp(8));
    editText.setSingleLine(true);
    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    editText.setInputType(InputType.TYPE_CLASS_TEXT);
    editText.setLayoutParams(layoutParams);
    return editText;
  }

}
