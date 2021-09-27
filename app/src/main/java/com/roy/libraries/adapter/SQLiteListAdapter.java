package com.roy.libraries.adapter;

import static com.roy.libraries.utils.AndroidUtil.dp;
import static com.roy.libraries.utils.AndroidUtil.getColor;
import static com.roy.libraries.utils.AndroidUtil.sp;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.roy.libraries.R;
import com.roy.libraries.data.Student;

import java.util.List;
import java.util.Locale;

/**
 * Created by Roy on 2021/9/26.
 */
public class SQLiteListAdapter extends BaseAdapter {
  private List<Student> students = null;

  public void setStudents(List<Student> students) {
    this.students = students;
  }

  @Override
  public int getCount() {
    return null == students ? 0 : students.size();
  }

  @Override
  public Student getItem(int position) {
    return null == students ? null : students.get(position);
  }

  @Override
  public long getItemId(int position) {
    return null == students ? -1 : position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final ViewHolder holder;
    if (null == convertView) {
      holder = new ViewHolder(new ItemView(parent.getContext()));
      convertView = holder.itemView;
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    final Student student = getItem(position);
    if (null != student) {
      holder.itemView.tvId.setText(String.format(Locale.CHINA, "Id:%1$04d", student.getId()));
      holder.itemView.tvNumber.setText(String.format("编号：%1$s", student.getNumber()));
      final int sex = student.getSex();
      final int age = student.getAge();
      final String name = student.getName();
      final double height = student.getHeight();
      final double weight = student.getWeight();
      final String tel = student.getTel();
      final String info = String.format(Locale.CHINA,
        "姓名: %1$s \n性别: %2$s \n年龄: %3$s \n手机: %4$s \n身高 %5$.2fcm \n体重 %6$.2fkg",
        name, 1 == sex ? "男" : "女", age, tel, height, weight);
      holder.itemView.tvInfo.setText(info);
    }
    return convertView;
  }

  private static class ItemView extends LinearLayout {
    private final TextView tvId;
    private final TextView tvNumber;
    private final TextView tvInfo;

    public ItemView(Context context) {
      super(context);
      setOrientation(VERTICAL);

      tvId = new TextView(context);
      tvNumber = new TextView(context);
      tvInfo = new TextView(context);

      final LinearLayout topLayout = new LinearLayout(context);
      topLayout.setPadding(dp(16), 0, dp(16), 0);
      topLayout.setOrientation(HORIZONTAL);
      topLayout.setGravity(Gravity.BOTTOM);

      tvId.setLayoutParams(new LayoutParams(0, -2, 1f));
      tvId.setTextColor(getColor(R.color.teal_200));
      tvId.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(18));
      tvId.setGravity(Gravity.START);
      tvId.setPadding(0, dp(8), 0, dp(8));

      tvNumber.setLayoutParams(new LayoutParams(0, -2, 3f));
      tvNumber.setTextColor(Color.BLACK);
      tvNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(16));
      tvNumber.setGravity(Gravity.START);
      tvNumber.setPadding(0, dp(8), 0, dp(8));

      tvInfo.setTextColor(Color.DKGRAY);
      tvInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(14));
      tvInfo.setGravity(Gravity.START);
      tvInfo.setPadding(dp(16), 0, dp(16), dp(8));

      topLayout.addView(tvId);
      topLayout.addView(tvNumber);
      addView(topLayout);
      addView(tvInfo);
    }
  }

  private static class ViewHolder {
    private final ItemView itemView;

    private ViewHolder(final ItemView itemView) {
      this.itemView = itemView;
    }
  }

}
