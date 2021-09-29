package com.roy.sqlite3.widget;

import static com.roy.sqlite3.utils.AndroidUtil.dp;
import static com.roy.sqlite3.utils.AndroidUtil.getColor;
import static com.roy.sqlite3.utils.AndroidUtil.makeShape;
import static com.roy.sqlite3.utils.AndroidUtil.screenHeight;
import static com.roy.sqlite3.utils.AndroidUtil.screenWidth;
import static com.roy.sqlite3.utils.AndroidUtil.sp;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.roy.sqlite3.R;

/**
 * Created by Roy on 2021/9/27.
 * 加载中...
 */
public class LoadingView extends ViewGroup {
  private final View coverView;
  private final LinearLayout loadingLayout;
  private final TextView textView;

  public LoadingView(Context context) {
    super(context);

    coverView = new View(context);
    coverView.setLayoutParams(new LayoutParams(-1, -1));
    coverView.setBackgroundColor(0x33000000);
    coverView.setOnClickListener(v -> {});

    loadingLayout = new LinearLayout(context);
    final LayoutParams loadingLayoutLayoutParams = new LayoutParams(screenWidth / 3, -2);
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

    addView(coverView);
    addView(loadingLayout);

    setElevation(1024);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    final int heightMode = MeasureSpec.getMode(widthMeasureSpec);
    final int widthSpec;
    final int heightSpec;
    if (MeasureSpec.UNSPECIFIED == widthMode) {
      widthSpec = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.AT_MOST);
    } else {
      widthSpec = widthMeasureSpec;
    }
    if (MeasureSpec.UNSPECIFIED == heightMode) {
      heightSpec = MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.AT_MOST);
    } else {
      heightSpec = heightMeasureSpec;
    }
    super.onMeasure(widthSpec, heightSpec);
    measureChildren(widthSpec, heightSpec);
  }
  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int centerX = (r - l) >> 1;
    final int centerY = (b - t) >> 1;
    coverView.layout(l, t, r, b);
    loadingLayout.layout(centerX - (loadingLayout.getMeasuredWidth() >> 1),
      centerY - (loadingLayout.getMeasuredHeight() >> 1),
      centerX + (loadingLayout.getMeasuredWidth() >> 1),
      centerY + (loadingLayout.getMeasuredHeight() >> 1)
    );
  }

  public void setLoadingText(final CharSequence text) {
    textView.setText(text);
  }
}
