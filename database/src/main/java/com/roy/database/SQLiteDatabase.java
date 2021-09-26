package com.roy.database;

import java.io.Closeable;
import java.io.File;

/**
 * Created by Roy on 2021/9/25.
 */
public class SQLiteDatabase implements Closeable {
  /**
   * 文件描述符（等于 native 层 sqlite3* 指针）
   * sqlite3_open(const char*, sqlite3**)调用成功时分配的内存地址
   */
  private long handle;

  /**
   * 是否处于事务开启状态的标识
   */
  private boolean isInTransaction = false;

  /**
   * 构造函数初始化数据库文件描述符
   *
   * @param dbname 数据库的文件（全路径） 如果数据库存在则打开、否则创建一个打开
   * @throws SQLiteException 路径无法写入、创建或打开数据库失败的时候抛出
   */
  private SQLiteDatabase(final String dbname) throws SQLiteException {
    this.handle = nOpenDatabase(dbname);
  }

  /**
   * 新创建或打开一个已有的数据库
   *
   * @param dbname 数据库的文件（全路径） 如果数据库存在则打开、否则创建一个打开
   * @return 可对数据库进行读写操作的 {@link SQLiteDatabase}
   * @throws SQLiteException 路径无法写入、创建或打开数据库失败的时候抛出
   */
  public static SQLiteDatabase open(final String dbname) throws SQLiteException {
    if (null == dbname)
      throw new SQLiteException("the filename of database can't be null");

    final File file = new File(dbname);
    final File dir = file.getParentFile();
    if (null == dir || !(dir.exists() || dir.mkdirs()))
      throw new SQLiteException("can't create dir.");

    return new SQLiteDatabase(dbname);
  }

  /**
   * 检测某张表是否在数据库中存在
   *
   * @param tableName 要检测的表的名字
   * @return 表存在返回 ture
   * @throws SQLiteException 数据库已关闭时抛出
   */
  public boolean isTableExists(final String tableName) throws SQLiteException {
    return nIsTblExists(handle, tableName);
  }

  /**
   * 编译SQL语句
   *
   * @param sql 要进行编译的SQL语句
   * @return {@link SQLiteStatement}
   * @throws SQLiteException 当传入的SQL语句有错无法编译的时候抛出
   */
  public SQLiteStatement compile(final String sql) throws SQLiteException {
    return new SQLiteStatement(handle, sql);
  }

  /**
   * 开启事务 （当进行批量操作的时候需要调用 否则很慢）
   *
   * @throws SQLiteException 上一次开启的事务未提交的时候抛出
   */
  public void beginTransaction() throws SQLiteException {
    if (isInTransaction)
      throw new SQLiteException("database has already in transaction.");

    try {
      nBeginTransaction(handle);
    } catch (SQLiteException e) {
      e.printStackTrace();
    } finally {
      isInTransaction = true;
    }
  }

  /**
   * 提交事务
   */
  public void commitTransaction() throws SQLiteException {
    if (isInTransaction) {
      try {
        nCommitTransaction(handle);
      } finally {
        isInTransaction = false;
      }
    }
  }

  /**
   * 关闭数据库 释放native层 sqlite3_open(const char*, sqlite3**) 所分配的内存
   * 该方法必须调用！否则将在程序运行期间永远不释放所分配的内存
   */
  @Override
  public void close() {
    if (0 != handle) {
      try {
        nCloseDatabase(handle);
      } catch (SQLiteException e) {
        e.printStackTrace();
      } finally {
        handle = 0;
      }
    }
  }

  private static native long nOpenDatabase(final String dbname) throws SQLiteException;
  private static native boolean nIsTblExists(final long handle, final String tableName) throws SQLiteException;
  private static native void nBeginTransaction(final long handle) throws SQLiteException;
  private static native void nCommitTransaction(final long handle) throws SQLiteException;
  private static native void nCloseDatabase(final long handle) throws SQLiteException;

  static {
    System.loadLibrary("sqlite3");
  }
}
