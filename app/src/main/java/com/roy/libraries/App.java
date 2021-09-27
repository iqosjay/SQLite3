package com.roy.libraries;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by Roy on 2021/9/26.
 */
public class App extends Application {
  private static App sApp = null;
  private Handler handler = null;

  @Override
  public void onCreate() {
    super.onCreate();
    handler = new Handler(Looper.getMainLooper());
    sApp = this;
  }

  public static App getApp() {
    return sApp;
  }

  public static Handler getHandler() {
    return sApp.handler;
  }
}
