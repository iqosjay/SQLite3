package com.roy.libraries.adapter;

import static com.roy.libraries.utils.AndroidUtil.dp;
import static com.roy.libraries.utils.AndroidUtil.sp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.roy.libraries.data.Student;
import com.roy.libraries.utils.AndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Roy on 2021/9/26.
 */
public class SQLiteListAdapter extends BaseAdapter {
  private Map<byte[], Bitmap> avatarMap = null;
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
      final String name = student.getName();
      final byte[] avatar = student.getAvatar();
      final double height = student.getHeight();
      final int age = student.getAge();
      final String info = String.format(Locale.CHINA, "年龄: %1$d    身高: %2$.2f cm", age, height);
      holder.itemView.tvName.setText(name);
      holder.itemView.tvInfo.setText(info);
      holder.itemView.ivAvatar.setImageBitmap(getAvatarBmp(avatar));
    }
    return convertView;
  }

  private Bitmap getAvatarBmp(final byte[] bytes) {
    if (null == bytes) {
      return null;
    }
    if (null == avatarMap) {
      avatarMap = new HashMap<>();
    }
    final Bitmap bitmap = avatarMap.get(bytes);
    if (null != bitmap) {
      return bitmap;
    }
    final Bitmap bmp = AndroidUtil.bytes2bitmap(bytes);
    avatarMap.put(bytes, bmp);
    return bmp;
  }

  private static class ItemView extends LinearLayout {
    private final ImageView ivAvatar;
    private final TextView tvName;
    private final TextView tvInfo;

    public ItemView(Context context) {
      super(context);
      setOrientation(HORIZONTAL);

      ivAvatar = new ImageView(context);
      tvName = new TextView(context);
      tvInfo = new TextView(context);

      final LayoutParams avatarLayoutParams = new LayoutParams(dp(48), dp(64));
      avatarLayoutParams.setMargins(dp(16), dp(8), dp(16), dp(8));
      avatarLayoutParams.gravity = Gravity.CENTER_VERTICAL;
      ivAvatar.setLayoutParams(avatarLayoutParams);
      ivAvatar.setScaleType(ImageView.ScaleType.FIT_XY);

      final LinearLayout infoLayoutFrame = new LinearLayout(context);
      final LayoutParams infoLayoutParam = new LayoutParams(-1, -2);
      infoLayoutFrame.setLayoutParams(infoLayoutParam);
      infoLayoutParam.setMargins(0, dp(8), dp(16), dp(8));
      infoLayoutParam.gravity = Gravity.CENTER_VERTICAL;
      infoLayoutFrame.setOrientation(VERTICAL);

      final LayoutParams nameLayoutParams = new LayoutParams(-1, -2);
      nameLayoutParams.setMargins(0, dp(8), dp(16), dp(8));
      tvName.setLayoutParams(nameLayoutParams);
      tvName.setTextColor(Color.BLACK);
      tvName.getPaint().setFakeBoldText(true);
      tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(18));
      tvName.setGravity(Gravity.START);

      final LayoutParams infoLayoutParams = new LayoutParams(-1, -2);
      infoLayoutParams.setMargins(0, dp(8), dp(16), dp(8));
      tvInfo.setLayoutParams(infoLayoutParams);
      tvInfo.setTextColor(Color.DKGRAY);
      tvInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(14));
      tvInfo.setGravity(Gravity.START);

      infoLayoutFrame.addView(tvName);
      infoLayoutFrame.addView(tvInfo);

      addView(ivAvatar);
      addView(infoLayoutFrame);
    }
  }

  private static class ViewHolder {
    private final ItemView itemView;

    private ViewHolder(final ItemView itemView) {
      this.itemView = itemView;
    }
  }

}
