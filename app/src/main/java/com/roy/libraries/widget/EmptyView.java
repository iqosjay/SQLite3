package com.roy.libraries.widget;

import static com.roy.libraries.utils.AndroidUtil.dp;
import static com.roy.libraries.utils.AndroidUtil.sp;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Roy on 2021/9/28.
 */
public class EmptyView extends LinearLayout {

  private static final String NO_DATA_TEXT = "没有数据";

  public EmptyView(Context context) {
    super(context);
    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);
    final TextView textView = new TextView(context);
    textView.getPaint().setFakeBoldText(true);
    textView.setLayoutParams(new LayoutParams(-2, -2));
    textView.setPadding(dp(24), dp(8), dp(24), dp(8));
    textView.setTextColor(Color.BLACK);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(24));
    textView.setText(NO_DATA_TEXT);
    addView(textView);
  }

}
