package com.roy.database;

import java.io.IOException;

/**
 * Created by Roy on 2021/9/25.
 */
public class SQLiteException extends IOException {
  public SQLiteException(String message) {
    super(message);
  }

  public SQLiteException(Throwable cause) {
    super(cause);
  }
}
