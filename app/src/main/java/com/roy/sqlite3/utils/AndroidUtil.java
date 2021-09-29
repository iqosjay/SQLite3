package com.roy.sqlite3.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.roy.sqlite3.App;

/**
 * Created by Roy on 2021/9/26.
 */
public class AndroidUtil {
  private static final DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();

  public static final int screenWidth = dm.widthPixels;
  public static final int screenHeight = dm.heightPixels;

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

  public static int getStatusBarHeight() {
    final Resources resources = App.getApp().getResources();
    int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
    return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
  }

  public static int getActionBarHeight() {
    final Context context = App.getApp().getApplicationContext();
    final TypedValue typedValue = new TypedValue();
    if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
      return TypedValue.complexToDimensionPixelSize(typedValue.data, dm);
    }
    return 0;
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

  public static Bitmap bytes2bitmap(final byte[] bytes) {
    final int len;
    if (null != bytes && 0 != (len = bytes.length)) {
      return BitmapFactory.decodeByteArray(bytes, 0, len);
    }
    return null;
  }

  public static boolean isInMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  public static void runOnUIThread(final Runnable runnable) {
    runOnUIThread(runnable, 0);
  }

  public static void runOnUIThread(final Runnable runnable, final long delay) {
    final Handler handler = App.getHandler();
    if (0 >= delay) {
      if (isInMainThread()) {
        runnable.run();
      } else {
        handler.post(runnable);
      }
    } else {
      handler.postDelayed(runnable, delay);
    }
  }

}
