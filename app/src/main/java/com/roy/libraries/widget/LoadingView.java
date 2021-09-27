package com.roy.libraries.widget;

import static com.roy.libraries.utils.AndroidUtil.dp;
import static com.roy.libraries.utils.AndroidUtil.getColor;
import static com.roy.libraries.utils.AndroidUtil.makeShape;
import static com.roy.libraries.utils.AndroidUtil.screenWidth;
import static com.roy.libraries.utils.AndroidUtil.sp;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roy.libraries.R;

/**
 * Created by Roy on 2021/9/27.
 * 加载中...
 */
public class LoadingView extends RelativeLayout {
  private final TextView textView;

  public LoadingView(Context context) {
    super(context);

    final View cover = new View(context);
    cover.setLayoutParams(new LayoutParams(-1, -1));
    cover.setBackgroundColor(0x33000000);
    cover.setOnClickListener(v -> {});

    final LinearLayout loadingLayout = new LinearLayout(context);
    final LayoutParams loadingLayoutLayoutParams = new LayoutParams(screenWidth / 3, -2);
    loadingLayoutLayoutParams.addRule(CENTER_IN_PARENT);
    loadingLayout.setOrientation(LinearLayout.VERTICAL);
    loadingLayout.setGravity(Gravity.CENTER_HORIZONTAL);
    loadingLayout.setBackground(makeShape(Color.WHITE, 4, 0, 0));
    loadingLayout.setElevation(dp(4));
    loadingLayout.setLayoutParams(loadingLayoutLayoutParams);

    final ProgressBar progressBar = new ProgressBar(context);
    final LinearLayout.LayoutParams progressLayoutParams = new LinearLayout.LayoutParams(-2, -2);
    progressLayoutParams.setMargins(0, dp(16), 0, 0);
    progressBar.setLayoutParams(progressLayoutParams);

    textView = new TextView(context);
    final LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(-2, -2);
    textViewLayoutParams.setMargins(0, dp(16), 0, dp(16));
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sp(18));
    textView.setTextColor(getColor(R.color.teal_200));
    textView.setLayoutParams(textViewLayoutParams);

    loadingLayout.addView(progressBar);
    loadingLayout.addView(textView);
    addView(cover);
    addView(loadingLayout);
  }

  public void setLoadingText(final CharSequence text) {
    textView.setText(text);
  }
}
