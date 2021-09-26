package com.roy.libraries.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;

import com.roy.libraries.App;

/**
 * Created by Roy on 2021/9/26.
 */
public class AndroidUtil {
  private static final DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();

  public static int dp(final float dp) {
    return (int) (dm.density * dp + 0.5f);
  }

  public static int sp(final float sp) {
    return (int) (dm.scaledDensity * sp + 0.5f);
  }

  public static int getColor(final int id) {
    final Resources res = App.getApp().getResources();
    return res.getColor(id);
  }

  public static Drawable makeShape(final int solidColor,
                                   final float cornerRadius,
                                   final int strokeColor,
                                   final float strokeWidth) {
    final GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(solidColor);
    drawable.setCornerRadius(dp(cornerRadius));
    drawable.setStroke(dp(strokeWidth), strokeColor);
    return drawable;
  }

}
