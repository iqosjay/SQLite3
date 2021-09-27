package com.roy.database;

/**
 * Created by Roy on 2021/9/25.
 * sqlite3_stmt* jni 封装
 * 查询也是通过 sqlite3_stmt* 来实现的 -> 在Java中表现为一个 Cursor
 */
public class SQLiteCursor {
  /**
   * 字段类型 -> 为整型
   */
  public static final int TYPE_INT = 1;

  /**
   * 字段类型 -> 浮点型
   */
  public static final int TYPE_FLOAT = 2;

  /**
   * 字段类型 -> 字符串
   */
  public static final int TYPE_STRING = 3;

  /**
   * 字段类型 -> byte数组
   */
  public static final int TYPE_BYTE_ARRAY = 4;

  /**
   * 字段类型 -> 空
   */
  public static final int TYPE_NULL = 5;

  /**
   * 是否可以执行查询的标识
   */
  private boolean inRow = false;

  /**
   * {@link SQLiteStatement}
   */
  private SQLiteStatement stmt;

  /**
   * @param stmt {@link SQLiteStatement}
   */
  /*package private*/ SQLiteCursor(final SQLiteStatement stmt) {
    this.stmt = stmt;
  }

  /**
   * 把游标下移一行
   * 实质上是调用 sqlite3_step(sqlite3_stmt*)
   * 当它的返回值是 SQLITE_ROW 的时候表示还有下一行数据
   * 这个时候视为 “下移成功”
   *
   * @return true 下移成功
   * @throws SQLiteException 执行过程中出错时抛出
   */
  public boolean next() throws SQLiteException {
    int res = stmt.step();
    if (-1 == res) {
      int count = 7;
      while (0 != --count) {
        try {
          Thread.sleep(500);
          res = stmt.step();
          if (0 == res) break;
        } catch (Exception ignore) {
        }
      }
      if (-1 == res)
        throw new SQLiteException("sqlite busy");
    }
    return inRow = (0 == res);
  }

  /**
   * 获取一共有多少列
   *
   * @return 执行过程中出错时抛出
   */
  public int getCount() throws SQLiteException {
    return nGetCount(stmt.getHandle());
  }

  /**
   * 获取某一列对应的数据类型
   *
   * @param index 列下标索引
   * @return 下标索引所对应的数据类型
   * @throws SQLiteException 获取出错时抛出
   * @see #TYPE_INT          整数
   * @see #TYPE_FLOAT        浮点数
   * @see #TYPE_STRING       字符串
   * @see #TYPE_BYTE_ARRAY   byte数组
   * @see #TYPE_NULL         空
   */
  public int getType(final int index) throws SQLiteException {
    checkRow(this);
    return nGetType(stmt.getHandle(), index);
  }

  /**
   * 检测某一列是否为空
   *
   * @param index 列下标索引
   * @return 为空返回 true
   * @throws SQLiteException 检测出错时抛出
   */
  public boolean columnIsNull(final int index) throws SQLiteException {
    checkRow(this);
    return nColumnIsNull(stmt.getHandle(), index);
  }

  /**
   * 获取某个下标对应的整型数据
   *
   * @param index 下标索引
   * @return 整型数
   * @throws SQLiteException 获取出错的时候抛出
   */
  public int getInt32(final int index) throws SQLiteException {
    checkRow(this);
    return nGetInt32(stmt.getHandle(), index);
  }

  /**
   * 获取某个下标对应的长整型数据
   *
   * @param index 下标索引
   * @return 长整型
   * @throws SQLiteException 获取出错的时候抛出
   */
  public long getInt64(final int index) throws SQLiteException {
    checkRow(this);
    return nGetInt64(stmt.getHandle(), index);
  }

  /**
   * 获取某个下标对应的浮点型数据
   *
   * @param index 下标索引
   * @return 浮点型数据
   * @throws SQLiteException 获取出错的时候抛出
   */
  public double getDouble(final int index) throws SQLiteException {
    checkRow(this);
    return nGetDouble(stmt.getHandle(), index);
  }

  /**
   * 获取某个下标对应的字符串数据
   *
   * @param index 下标索引
   * @return 字符串
   * @throws SQLiteException 获取出错的时候抛出
   */
  public String getString(final int index) throws SQLiteException {
    checkRow(this);
    return nGetString(stmt.getHandle(), index);
  }

  /**
   * 获取某个下标对应的原始byte数组
   *
   * @param index 下标索引
   * @return 原始byte数组
   * @throws SQLiteException 获取出错的时候抛出
   */
  public byte[] getBlob(final int index) throws SQLiteException {
    checkRow(this);
    return nGetBlob(stmt.getHandle(), index);
  }

  /**
   * 检测是否可以执行获取数据
   *
   * @throws SQLiteException 不可以获取数据的时候抛出
   */
  private static void checkRow(final SQLiteCursor cursor) throws SQLiteException {
    if (!cursor.inRow)
      throw new SQLiteException("You must call next before.");
  }

  /**
   * 释放资源
   */
  public void dispose() {
    if (null != stmt) {
      stmt.dispose();
      stmt = null;
    }
  }


  private static native int nGetCount(long handle) throws SQLiteException;
  private static native int nGetType(long handle, int index) throws SQLiteException;
  private static native int nGetInt32(long handle, int index) throws SQLiteException;
  private static native double nGetDouble(long handle, int index) throws SQLiteException;
  private static native long nGetInt64(long handle, int index) throws SQLiteException;
  private static native String nGetString(long handle, int index) throws SQLiteException;
  private static native byte[] nGetBlob(long handle, int index) throws SQLiteException;
  private static native boolean nColumnIsNull(long handle, int index) throws SQLiteException;
}
