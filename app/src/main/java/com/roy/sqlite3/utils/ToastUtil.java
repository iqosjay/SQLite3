package com.roy.sqlite3.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.roy.sqlite3.App;

/**
 * Created by Roy on 2021/9/26.
 */
public class ToastUtil {
  private static Toast toast = null;

  public static void showToast(final String text) {
    final Context context = App.getApp().getApplicationContext();
    if (null == context || TextUtils.isEmpty(text)) {
      return;
    }
    if (null == toast)
      toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
    else
      toast.setText(text);
    toast.show();
  }
}
