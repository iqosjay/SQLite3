package com.roy.sqlite3.adapter;

import static com.roy.sqlite3.utils.AndroidUtil.dp;
import static com.roy.sqlite3.utils.AndroidUtil.sp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roy.sqlite3.data.Student;
import com.roy.sqlite3.utils.AndroidUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    holder.setStudentData(getItem(position));
    return convertView;
  }

  private static class ItemView extends RelativeLayout {
    private final ImageView ivAvatar;
    private final TextView tvName;
    private final TextView tvInfo;

    public ItemView(Context context) {
      super(context);

      final int avatarId = 1;

      ivAvatar = new ImageView(context);
      tvName = new TextView(context);
      tvInfo = new TextView(context);

      final LayoutParams avatarLayoutParams = new LayoutParams(dp(48), dp(56));
      avatarLayoutParams.setMargins(dp(16), dp(8), dp(8), dp(8));
      ivAvatar.setId(avatarId);
      ivAvatar.setLayoutParams(avatarLayoutParams);
      ivAvatar.setScaleType(ImageView.ScaleType.FIT_XY);

      final LayoutParams nameLayoutParams = new LayoutParams(-1, -2);
      nameLayoutParams.setMargins(dp(8), 0, dp(16), 0);
      nameLayoutParams.addRule(END_OF, avatarId);
      nameLayoutParams.addRule(ALIGN_TOP, avatarId);
      tvName.setLayoutParams(nameLayoutParams);
      tvName.setTextColor(Color.BLACK);
      tvName.getPaint().setFakeBoldText(true);
      tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(18));
      tvName.setGravity(Gravity.START);

      final LayoutParams infoLayoutParams = new LayoutParams(-1, -2);
      infoLayoutParams.setMargins(dp(8), 0, dp(16), 0);
      infoLayoutParams.addRule(END_OF, avatarId);
      infoLayoutParams.addRule(ALIGN_BOTTOM, avatarId);
      tvInfo.setLayoutParams(infoLayoutParams);
      tvInfo.setSingleLine(true);
      tvInfo.setEllipsize(TextUtils.TruncateAt.START);
      tvInfo.setTextColor(Color.DKGRAY);
      tvInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(14));
      tvInfo.setGravity(Gravity.START);

      addView(ivAvatar);
      addView(tvName);
      addView(tvInfo);
    }
  }

  private static class ViewHolder {
    private static final String INFO_FORMAT = "主键Id: [ %1$5d ] 年龄: %2$d 身高: %3$.2f cm";
    private final ItemView itemView;
    private Map<byte[], Bitmap> avatarMap = null;


    private ViewHolder(final ItemView itemView) {
      this.itemView = itemView;
    }

    private void setStudentData(final Student student) {
      if (null != student) {
        final long id = student.getId();
        final String name = student.getName();
        final byte[] avatar = student.getAvatar();
        final double height = student.getHeight();
        final int age = student.getAge();
        final String info = String.format(Locale.CHINA, INFO_FORMAT, id, age, height);
        itemView.tvName.setText(name);
        itemView.tvInfo.setText(info);
        itemView.ivAvatar.setImageBitmap(getAvatarBmp(avatar));
      }
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
  }

}
