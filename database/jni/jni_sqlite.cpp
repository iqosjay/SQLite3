//
// Created by roy on 2021/9/25.
//

#include <jni.h>
#include <cassert>

#include <sqlite/sqlite3.h>

#include "definitions.h"
#include "logger.h"

//SQLiteDatabase.java
#define FXN_SQLITE3_DATABASE(RETURN_TYPE, FXN_NAME, ...)    \
        extern "C" JNIEXPORT RETURN_TYPE JNICALL            \
        Java_com_roy_database_SQLiteDatabase_##FXN_NAME     \
        (JNIEnv* env, jclass clazz, ##__VA_ARGS__)

//SQLiteStatement.java
#define FXN_SQLITE3_STATEMENT(RETURN_TYPE, FXN_NAME, ...)   \
        extern "C" JNIEXPORT RETURN_TYPE JNICALL            \
        Java_com_roy_database_SQLiteStatement_##FXN_NAME    \
        (JNIEnv* env, jclass clazz, ##__VA_ARGS__)

//SQLiteCursor.java
#define FXN_SQLITE3_CURSOR(RETURN_TYPE, FXN_NAME, ...)      \
        extern "C" JNIEXPORT RETURN_TYPE JNICALL            \
        Java_com_roy_database_SQLiteCursor_##FXN_NAME       \
        (JNIEnv* env, jclass clazz, ##__VA_ARGS__)


#define GET_JAVA_STR(J_STR)                 env->GetStringUTFChars((J_STR), JNI_FALSE)
#define DEL_JAVA_STR(J_STR, C_STR)          env->ReleaseStringUTFChars((J_STR), (C_STR))
#define NEW_JAVA_STR(C_STR)                 env->NewStringUTF((C_STR))

namespace {

constexpr const char kQueryTable[] = "SELECT rowid FROM sqlite_master WHERE type='table' AND name=?;";

constexpr const char kDatabaseClosed[] = "database might be closed.";
constexpr const char kStatementFreed[] = "sqlite3_stmt has been finalized.";

constexpr const int32_t kRetRow = 0;
constexpr const int32_t kRetDone = 1;
constexpr const int32_t kRetFailed = -1;

}

FORCEINLINE void throw_sqlite3_exception(JNIEnv* env, const char* err_msg) {
  jclass clazz = env->FindClass("com/roy/database/SQLiteException");
  env->ThrowNew(clazz, err_msg);
}

FORCEINLINE void throw_sqlite3_exception(JNIEnv* env, sqlite3* handle) {
  if (nullptr != handle) {
    throw_sqlite3_exception(env, sqlite3_errmsg(handle));
  } else {
    throw_sqlite3_exception(env, "unknown error~");
  }
}

FORCEINLINE bool statement_is_freed(JNIEnv* env, sqlite3_stmt* stmt) {
  if (nullptr == stmt) {
    throw_sqlite3_exception(env, kStatementFreed);
    return true;
  }
  return false;
}

//打开数据库
FXN_SQLITE3_DATABASE(jlong, nOpenDatabase, jstring dbname) {
  const char* filename = GET_JAVA_STR(dbname);
  sqlite3* db = nullptr;
  int32_t err = sqlite3_open(filename, &db);
  if (SQLITE_OK != err) {
    throw_sqlite3_exception(env, db);
    db = nullptr;
  }
  if (dbname && filename) {
    DEL_JAVA_STR(dbname, filename);
  }
  return nullptr == db ? 0 : reinterpret_cast<jlong>(db);
}

//开启事务
FXN_SQLITE3_DATABASE(void, nBeginTransaction, jlong handle) {
  auto db = reinterpret_cast<sqlite3*> (handle);
  if (nullptr == db) {
    throw_sqlite3_exception(env, kDatabaseClosed);
  } else {
    sqlite3_exec(db, "BEGIN", nullptr, nullptr, nullptr);
  }
}

//提交事务
FXN_SQLITE3_DATABASE(void, nCommitTransaction, jlong handle) {
  auto db = reinterpret_cast<sqlite3*> (handle);
  if (nullptr == db) {
    throw_sqlite3_exception(env, kDatabaseClosed);
  } else {
    sqlite3_exec(db, "COMMIT", nullptr, nullptr, nullptr);
  }
}

//判断某张表是否存在
FXN_SQLITE3_DATABASE(jboolean, nIsTblExists, jlong handle, jstring table_name) {
  sqlite3* db;
  sqlite3_stmt* stmt = nullptr;
  int error_code;
  bool tbl_is_exists = false;
  const char* tbl_name = nullptr;

  if (nullptr == table_name) {
    throw_sqlite3_exception(env, "table-name can't be null.");
    goto end;
  }

  db = reinterpret_cast<sqlite3*> (handle);
  if (nullptr == db) {
    throw_sqlite3_exception(env, kDatabaseClosed);
    goto end;
  }

  error_code = sqlite3_prepare_v2(db, kQueryTable, -1, &stmt, nullptr);
  if (SQLITE_OK != error_code) {
    throw_sqlite3_exception(env, db);
    goto end;
  }

  tbl_name = GET_JAVA_STR(table_name);
  error_code = sqlite3_bind_text(stmt, 1, tbl_name, -1, SQLITE_TRANSIENT);

  if (SQLITE_OK != error_code) {
    throw_sqlite3_exception(env, db);
    goto end;
  }
  error_code = sqlite3_step(stmt);
  if (SQLITE_ROW == error_code) {
    tbl_is_exists = (SQLITE_INTEGER == sqlite3_column_type(stmt, 0));
  }

end:
  if (stmt) {
    sqlite3_finalize(stmt);
  }
  if (table_name && tbl_name) {
    DEL_JAVA_STR(table_name, tbl_name);
  }
  return static_cast<jboolean>(tbl_is_exists);
}

//关闭数据库
FXN_SQLITE3_DATABASE(void, nCloseDatabase, jlong handle) {
  auto db = reinterpret_cast<sqlite3*> (handle);
  if (nullptr == db) {
    throw_sqlite3_exception(env, kDatabaseClosed);
  } else {
    int32_t err = sqlite3_close(db);
    if (SQLITE_OK != err) {
      throw_sqlite3_exception(env, db);
    }
  }
}

//获取总列数
FXN_SQLITE3_CURSOR(jint, nGetCount, jlong handle) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return 0;
  }
  return sqlite3_column_count(statement);
}

//获取某列对应的数据类型
FXN_SQLITE3_CURSOR(jint, nGetType, jlong handle, jint index) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return -1;
  }
  return sqlite3_column_type(statement, index);
}

//某一列的数据是否为空
FXN_SQLITE3_CURSOR(jboolean, nColumnIsNull, jlong handle, jint index) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return false;
  }
  int value_type = sqlite3_column_type(statement, index);
  return static_cast<jboolean>(SQLITE_NULL == value_type);
}

//获取某一列的32位整数
FXN_SQLITE3_CURSOR(jint, nGetInt32, jlong handle, jint index) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return 0;
  }
  int value_type = sqlite3_column_type(statement, index);
  if (SQLITE_NULL == value_type) {
    return 0;
  }
  return sqlite3_column_int(statement, index);
}

//获取某一列的64位整数
FXN_SQLITE3_CURSOR(jlong, nGetInt64, jlong handle, jint index) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return 0;
  }
  int value_type = sqlite3_column_type(statement, index);
  if (SQLITE_NULL == value_type) {
    return 0;
  }
  return sqlite3_column_int64(statement, index);
}

//获取某一列的小数
FXN_SQLITE3_CURSOR(jdouble, nGetDouble, jlong handle, jint index) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return 0;
  }
  int value_type = sqlite3_column_type(statement, index);
  if (SQLITE_NULL == value_type) {
    return 0;
  }
  return sqlite3_column_double(statement, index);
}

//获取某一列的字符串
FXN_SQLITE3_CURSOR(jstring, nGetString, jlong handle, jint index) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return nullptr;
  }
  int value_type = sqlite3_column_type(statement, index);
  if (SQLITE_NULL == value_type) {
    return nullptr;
  }
  auto str = reinterpret_cast<const char*>(sqlite3_column_text(statement, index));
  return NEW_JAVA_STR(str);
}

//获取某一列的原始byte数组
FXN_SQLITE3_CURSOR(jbyteArray, nGetBlob, jlong handle, jint index) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return nullptr;
  }
  int value_type = sqlite3_column_type(statement, index);
  if (SQLITE_BLOB != value_type) {
    return nullptr;
  }
  auto buf = reinterpret_cast<const jbyte*>(sqlite3_column_blob(statement, index));
  int length = sqlite3_column_bytes(statement, index);
  if (nullptr != buf && length > 0) {
    jbyteArray bytes = env->NewByteArray(length);
    env->SetByteArrayRegion(bytes, 0, length, buf);
    return bytes;
  }
  return nullptr;
}


//编译SQL语句
FXN_SQLITE3_STATEMENT(jlong, nPrepare, jlong handle, jstring sql) {
  char const* zsql = nullptr;
  sqlite3_stmt* statement = nullptr;
  int32_t error_code;
  auto db = reinterpret_cast<sqlite3*> (handle);
  if (nullptr == db) {
    throw_sqlite3_exception(env, kDatabaseClosed);
    goto end;
  }
  if (nullptr == sql) {
    throw_sqlite3_exception(env, "sql can't be null.");
    goto end;
  }
  zsql = GET_JAVA_STR(sql);
  error_code = sqlite3_prepare_v2(db, zsql, -1, &statement, nullptr);
  if (SQLITE_OK != error_code) {
    throw_sqlite3_exception(env, db);
    goto end;
  }

end:
  if (nullptr != sql && nullptr != zsql) {
    DEL_JAVA_STR(sql, zsql);
  }
  return reinterpret_cast<jlong>(statement);
}

//重置已绑定的数据
FXN_SQLITE3_STATEMENT(void, nReset, jlong handle) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return;
  }
  int32_t err = sqlite3_reset(statement);
  if (SQLITE_OK != err) {
    throw_sqlite3_exception(env, sqlite3_db_handle(statement));
  }
}

//绑定空数据
FXN_SQLITE3_STATEMENT(void, nBindNull, jlong handle, jint index) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return;
  }
  int32_t err = sqlite3_bind_null(statement, index);
  if (SQLITE_OK != err) {
    throw_sqlite3_exception(env, sqlite3_db_handle(statement));
  }
}

//绑定32位的整数
FXN_SQLITE3_STATEMENT(void, nBindInt32, jlong handle, jint index, jint val) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return;
  }
  int32_t error_code = sqlite3_bind_int(statement, index, val);
  if (SQLITE_OK != error_code) {
    throw_sqlite3_exception(env, sqlite3_db_handle(statement));
  }
}

//绑定64位整数
FXN_SQLITE3_STATEMENT(void, nBindInt64, jlong handle, jint index, jlong val) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return;
  }
  int32_t error_code = sqlite3_bind_int64(statement, index, val);
  if (SQLITE_OK != error_code) {
    throw_sqlite3_exception(env, sqlite3_db_handle(statement));
  }
}

//绑定浮点小数
FXN_SQLITE3_STATEMENT(void, nBindDouble, jlong handle, jint index, jdouble val) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return;
  }
  int32_t error_code = sqlite3_bind_double(statement, index, val);
  if (SQLITE_OK != error_code) {
    throw_sqlite3_exception(env, sqlite3_db_handle(statement));
  }
}

//绑定字符串
FXN_SQLITE3_STATEMENT(void, nBindString, jlong handle, jint index, jstring val) {
  if (nullptr == val) {
    throw_sqlite3_exception(env, "String can't be null");
    return;
  }
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return;
  }
  const char* str = GET_JAVA_STR(val);
  int32_t err = sqlite3_bind_text(statement, index, str, -1, SQLITE_TRANSIENT);
  if (SQLITE_OK != err) {
    throw_sqlite3_exception(env, sqlite3_db_handle(statement));
  }
  if (val && str) {
    DEL_JAVA_STR(val, str);
  }
}

//绑定原始byte数组
FXN_SQLITE3_STATEMENT(void, nBindBlob, jlong handle, jint index, jbyteArray bytes, jint len) {
  if (nullptr == bytes) {
    throw_sqlite3_exception(env, "bytes == null.");
    return;
  }
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return;
  }
  jbyte* buf = env->GetByteArrayElements(bytes, JNI_FALSE);
  int32_t rc = sqlite3_bind_blob(statement, index, buf, len, SQLITE_STATIC);
  env->ReleaseByteArrayElements(bytes, buf, JNI_COMMIT);
  if (SQLITE_OK != rc) {
    throw_sqlite3_exception(env, sqlite3_db_handle(statement));
  }
}

//sqlite3_stmt 销毁
FXN_SQLITE3_STATEMENT(void, nFinalize, jlong handle) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (nullptr == statement) {
    throw_sqlite3_exception(env, kStatementFreed);
  } else {
    sqlite3_finalize(statement);
  }
}

//执行已绑定数据的SQL
FXN_SQLITE3_STATEMENT(jint, nStep, jlong handle) {
  auto statement = reinterpret_cast<sqlite3_stmt*>(handle);
  if (statement_is_freed(env, statement)) {
    return kRetFailed;
  }
  int error_code = sqlite3_step(statement);
  if (SQLITE_ROW == error_code) {
    return kRetRow;
  } else if (SQLITE_DONE == error_code) {
    return kRetDone;
  } else if (SQLITE_BUSY == error_code) {
    return kRetFailed;
  } else {
    throw_sqlite3_exception(env, sqlite3_db_handle(statement));
    return kRetFailed;
  }
}