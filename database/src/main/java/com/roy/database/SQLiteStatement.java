package com.roy.database;

/**
 * Created by Roy on 2021/9/25.
 * sqlite3_stmt* jni 封装
 */
public class SQLiteStatement {

  /**
   * 文件描述符（等于 native 层 sqlite3_stmt* 指针）
   * sqlite3_prepare_v2(sqlite3*, const char*,int,sqlite3_stmt**,const char**)调用成功时分配的内存地址
   */
  private long handle;

  /**
   * 构造函数
   *
   * @param handle sqlite3* 指针
   * @param sql    SQL语句
   * @throws SQLiteException 编译SQL失败
   */
  /*package private*/ SQLiteStatement(final long handle, final String sql) throws SQLiteException {
    this.handle = nPrepare(handle, sql);
  }

  /**
   * 绑定空参数
   *
   * @param index sql语句中的第几个占位符号
   * @throws SQLiteException 绑定出错时抛出
   */
  public void bindNull(final int index) throws SQLiteException {
    nBindNull(handle, index);
  }

  /**
   * 绑定 int
   *
   * @param index sql语句中的第几个占位符号
   * @param value 32位整数
   * @throws SQLiteException 绑定出错时抛出
   */
  public void bindInt32(final int index, final int value) throws SQLiteException {
    nBindInt32(handle, index, value);
  }

  /**
   * 绑定 long
   *
   * @param index sql语句中的第几个占位符号
   * @param value 64位整数
   * @throws SQLiteException 绑定出错时抛出
   */
  public void bindInt64(final int index, final long value) throws SQLiteException {
    nBindInt64(handle, index, value);
  }

  /**
   * 绑定 double
   *
   * @param index sql语句中的第几个占位符号
   * @param value 小数
   * @throws SQLiteException 绑定出错时抛出
   */
  public void bindDouble(final int index, final double value) throws SQLiteException {
    nBindDouble(handle, index, value);
  }

  /**
   * 绑定 字符串
   *
   * @param index sql语句中的第几个占位符号
   * @param value 字符串
   * @throws SQLiteException 绑定出错时抛出
   */
  public void bindString(final int index, final String value) throws SQLiteException {
    nBindString(handle, index, value);
  }

  /**
   * 绑定 byte[]
   *
   * @param index sql语句中的第几个占位符号
   * @param value byte[]
   * @throws SQLiteException 绑定出错时抛出
   */
  public void bindBlob(final int index, final byte[] value) throws SQLiteException {
    if (null == value)
      throw new SQLiteException("value can't be null.");
    nBindBlob(handle, index, value, value.length);
  }

  /**
   * 绑定参数完成之后可通过调用这个函数来执行
   *
   * @return 查询 : 0 -> 还有数据; 执行SQL : 1 -> 执行成功; -1 -> 失败（无论什么操作）
   * @throws SQLiteException 调用sqlite3_step(sqlite3_stmt *)的返回值不是以上三个的时候抛出
   */
  public int step() throws SQLiteException {
    return nStep(handle);
  }

  /**
   * {@link #step()}
   *
   * @return 返回本例方便链式调用
   * @throws SQLiteException 执行sql语句出错时抛出
   */
  public SQLiteStatement stepThis() throws SQLiteException {
    final int code = nStep(handle);
    if (-1 == code)
      throw new SQLiteException("call step failed with return code [" + code + "]");
    return this;
  }

  /**
   * 重置已绑定的参数、以用于再次绑定
   *
   * @throws SQLiteException 重置过程出错时抛出
   */
  public void reset() throws SQLiteException {
    nReset(handle);
  }

  /**
   * 绑定数据
   *
   * @param args 与sql语句占位符匹配的参数
   * @return 返回本例方便链式调用
   * @throws SQLiteException 绑定过程出错的时候抛出
   */
  public SQLiteStatement bind(final Object[] args) throws SQLiteException {
    if (args == null)
      throw new IllegalArgumentException();

    checkFinalized();
    nReset(handle);
    int index = 1;
    for (Object obj : args) {
      if (null == obj) {
        nBindNull(handle, index);
      } else if (obj instanceof Integer) {
        nBindInt32(handle, index, (Integer) obj);
      } else if (obj instanceof Double || obj instanceof Float) {
        nBindDouble(handle, index, ((Number) obj).doubleValue());
      } else if (obj instanceof String) {
        nBindString(handle, index, (String) obj);
      } else if (obj instanceof Long) {
        nBindInt64(handle, index, (Long) obj);
      } else if (obj instanceof byte[]) {
        nBindBlob(handle, index, (byte[]) obj, ((byte[]) obj).length);
      } else {
        throw new IllegalArgumentException();
      }
      ++index;
    }
    return this;
  }

  /**
   * 查询数据
   *
   * @param args 与sql语句占位符匹配的参数
   * @return {@link SQLiteCursor}
   * @throws SQLiteException 绑定过程出错的时候抛出
   */
  public SQLiteCursor query(final Object[] args) throws SQLiteException {
    return new SQLiteCursor(bind(args));
  }

  /**
   * @return sqlite3_stmt*
   */
  /*package private*/ long getHandle() {
    return handle;
  }

  /**
   * 销毁
   */
  public void dispose() {
    if (0 != handle) {
      try {
        nFinalize(handle);
      } catch (SQLiteException ignored) {
      } finally {
        handle = 0;
      }
    }
  }

  /**
   * 检测当前的SQLiteStatement是否已被销毁
   *
   * @throws SQLiteException 已被销毁时抛出
   */
  private void checkFinalized() throws SQLiteException {
    if (0 == handle)
      throw new SQLiteException("Prepared query finalized");
  }


  private static native long nPrepare(long handle, String sql) throws SQLiteException;
  private static native void nReset(long handle) throws SQLiteException;
  private static native void nBindNull(long handle, int index) throws SQLiteException;
  private static native void nBindInt32(long handle, int index, int value) throws SQLiteException;
  private static native void nBindInt64(long handle, int index, long value) throws SQLiteException;
  private static native void nBindDouble(long handle, int index, double value) throws SQLiteException;
  private static native void nBindString(long handle, int index, String value) throws SQLiteException;
  private static native void nBindBlob(long handle, int index, byte[] bytes, int len) throws SQLiteException;
  private static native void nFinalize(long handle) throws SQLiteException;
  private static native int nStep(long handle) throws SQLiteException;

}
