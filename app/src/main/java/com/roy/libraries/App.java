package com.roy.libraries;

import android.app.Application;

/**
 * Created by Roy on 2021/9/26.
 */
public class App extends Application {
  private static App sApp = null;

  @Override
  public void onCreate() {
    super.onCreate();
    sApp = this;
  }

  public static App getApp() {
    return sApp;
  }
}
