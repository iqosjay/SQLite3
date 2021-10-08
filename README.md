
## Android SQLite3 NDK 封装

[Demo下载](https://github.com/iqosjay/SQLite3/releases/download/1.0.0/sqlite3_demo_1.0.0.apk)
（操作：按钮新增 按钮查询 点按编辑 长按删除）

### 写在前面 

[sqlite3](https://sqlite.org/index.html) 开源、集成简单（现在的版本只有2个文件 sqlite3.h sqlite3.c）

这个库抽离自 [Telegram](https://github.com/DrKLO/Telegram) 的开源代码、作者：[DrKLO](https://github.com/DrKLO)

我个人感觉 Telegram 的源码每一行都值得学习（如果你追求极致性能、更应该研究）

### 优点

1、与 Android 原生 sqlite3 相比性能更高效（测试数据在最后）

2、API更简洁

3、线程安全、多个线程操作同一个数据库文件绝对不会 Crash (重试次数6 每次等待500毫秒 共3秒 仍然无法执行则返回失败)

4、所有API都抛受检异常而非运行时异常（这可以降低App的崩溃率）

### 缺点

1、使用方面不太简单，仅仅只是比原生的API好用

2、SQLiteDatabase 与 SQLiteStatement（SQLiteCursor） 必须手动释放内存、如果忘记那么将会一直占据内存

3、没有数据库版本、如果需要做数据库升级则要自己维护一个数据库版本


### 如何使用？

导入：
1、首先在工程级的 build.gradle 中添加 mavenCentral()

```
buildscript {
  repositories {
    mavenCentral() // 最新版的Android Studio应该是默认就有 mavenCentral() 配置
  }
}
```

2、再在你需要使用模块级的 build.gradle 中添加依赖 'io.github.iqosjay:sqlite3:1.0.0'

```
dependencies {
  ...
  implementation 'io.github.iqosjay:sqlite3:1.0.0' //就是我了~~
  ...
}
```

基本套路：

1、SQLiteDatabase.open("数据库文件的绝对路径") 获得一个 database 对象

2、SQLiteDatabase#compile("SQL语句")编译SQL语句

3、SQLiteStatement#bind(Object[] args)绑定占位符的数据

4、SQLiteStatement#step() SQLiteStatement#stepThis() 执行绑定了数据的SQL

5、SQLiteDatabase#close()  释放 sqlite3*

6、SQLiteStatement#close() 释放 sqlite3_stmt*

例：

SQLiteDatabase db = SQLiteDatabase.open("数据库文件的绝对路径");

db.compile("SQL语句").compile(new Object[]{...}).stepThis().dispose(); 一气呵成

db.close();

实际代码：

插入数据：
```
private void insert() {
  SQLiteDatabase db = null;
  SQLiteStatement stmt = null;
  try {
    db = SQLiteDatabase.open(dbname);   //创建或打开一个数据库
    if (!db.isTableExists("Student")) { //判断表是否存在？
      db.compile("CREATE TABLE Student (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,age INTEGER,height DECIMAL,avatar BLOB);").stepThis().dispose();
    }
    db.beginTransaction();  //开启事务
    stmt = db.compile("INSERT INTO Student(name,age,height,avatar) VALUES(?,?,?,?);").bind(new Object[]{"Roy", 27, 178.5, defaultAvatar()}); //编译SQL语句并绑定占位符的数据
    int code = stmt.step(); //执行SQL
    if (1 != code)
      throw new SQLiteException("call step failed.");

    db.commitTransaction(); //提交事务
  } catch (final SQLiteException e) {
    e.printStackTrace();
  } finally {
    if (null != stmt) stmt.dispose(); //释放 sqlite3_stmt*
    if (null != db) db.close();       //释放 sqlite3*
  }
}
```

删除数据：
```
private void delete(final Student student) {
  SQLiteDatabase db = null;
  try {
    db = SQLiteDatabase.open(dbname);
    if (db.isTableExists("Student")) { //只有表存在的情况下才往下执行
      if (null == student) {  //删除所有
        db.compile("DELETE FROM Student;").stepThis().dispose();
      } else {                //删除单条
        db.compile("DELETE FROM Student WHERE id=?;").bind(new Object[]{student.getId()}).stepThis().dispose();
      }
    }
  } catch (SQLiteException e) {
    e.printStackTrace();
  } finally {
    if (null != db) db.close(); //释放 sqlite3*
  }
}
```

修改数据：
```
private void update(final Student student) {
  if (null == student) { //空校验
    return;
  }
  SQLiteDatabase db = null;
  SQLiteStatement stmt = null;
  try {
    db = SQLiteDatabase.open(dbname); //创建或打开一个数据库
    stmt = db.compile("UPDATE Student SET name=?, age=?, height=? WHERE id=?;").bind(new Object[]{student.getName(), student.getAge(), student.getHeight(), student.getId()}); //编译SQL语句并绑定占位符的数据
    final int code = stmt.step();     //执行SQL
    if (1 != code)
      throw new SQLiteException("更新数据失败：code = [" + code + "]");
  } catch (SQLiteException e) {
    e.printStackTrace();
  } finally {
    if (null != stmt) stmt.dispose(); //释放 sqlite3_stmt*
    if (null != db) db.close();       //释放 sqlite3*
  }
}
```

查询数据：
```
private List<Student> select() {
  final List<Student> students = new ArrayList<>();
  SQLiteDatabase db = null;
  SQLiteCursor cursor = null;
  try {
    db = SQLiteDatabase.open(dbname);   //创建或打开一个数据库
    if (db.isTableExists("Student")) {  //只有表存在的情况下才能查询
      cursor = db.compile("SELECT * FROM Student WHERE height>?;").query(new Object[]{0.0d});//编译SQL语句并绑定占位符的数据
      while (cursor.next()) {           //还有下一行数据
        final long id = cursor.getInt64(0);
        final String name = cursor.getString(1);
        final int age = cursor.getInt32(2);
        final double height = cursor.getDouble(3);
        final byte[] avatar = cursor.getBlob(4);
        final Student student = new Student();
        student.setId(id);
        student.setAge(age);
        student.setName(name);
        student.setHeight(height);
        student.setAvatar(avatar);
        students.add(student);
      }
    }
  } catch (final SQLiteException e) {
    e.printStackTrace();
  } finally {
    if (null != cursor) cursor.dispose(); //释放 sqlite3_stmt* (Cursor 通过SQLiteStatement实现的，Cursor的dispose 本质上调用的是SQLiteStatement的dispose)
    if (null != db) db.close();           //释放 sqlite3*
  }
  return students;
}
```

应用：

[SQLiteActivity.java](https://github.com/iqosjay/SQLite3/blob/main/app/src/main/java/com/roy/sqlite3/SQLiteActivity.java) 前 4 个方法分别演示了增、删、改、查 和 不同的数据类型

或者你也可以 clone 本工程下来并依赖 database 模块

### 测试数据：

##### 单表插入

------

测试手机：华为MED-AL00（办宽带送的、用来当开发测试机再合适不过了）
耗时单位：毫秒
每条数据三个字段（id INTEGER AUTOINCREMENT, name TEXT, age INTEGER）

|1条      | 第 1 次 | 第 2 次 | 第 3 次 | 第 4 次 | 第 5 次 | 第 6 次 | 第 7 次 | 第 8 次 | 第 9 次 | 第 10 次 |
|:--------:---------:---------:---------:---------:---------:---------:---------:---------:---------:----------|
|Android: | 13      | 4       | 6       | 4       | 5       | 5       | 6       | 5       | 6       | 5        |
|SQLite3: | 18      | 6       | 7       | 7       | 6       | 7       | 7       | 5       | 6       | 6        |


|10条     | 第 1 次 | 第 2 次 | 第 3 次 | 第 4 次 | 第 5 次 | 第 6 次 | 第 7 次 | 第 8 次 | 第 9 次 | 第 10 次 |
|:--------:---------:---------:---------:---------:---------:---------:---------:---------:---------:----------|
|Android: | 31      | 7       | 9       | 7       | 9       | 7       | 9       | 8       | 8       | 7        |
|SQLite3: | 30      | 8       | 8       | 6       | 7       | 7       | 6       | 6       | 7       | 7        |

|100条    | 第 1 次 | 第 2 次 | 第 3 次 | 第 4 次 | 第 5 次 | 第 6 次 | 第 7 次 | 第 8 次 | 第 9 次 | 第 10 次 |
|:--------:---------:---------:---------:---------:---------:---------:---------:---------:---------:----------|
|Android: | 29      | 19      | 18      | 16      | 15      | 13      | 13      | 13      | 12      | 11       |
|SQLite3: | 21      | 9       | 8       | 7       | 10      | 18      | 8       | 8       | 9       | 8        |

|1000条   | 第 1 次 | 第 2 次 | 第 3 次 | 第 4 次 | 第 5 次 | 第 6 次 | 第 7 次 | 第 8 次 | 第 9 次 | 第 10 次 |
|:--------:---------:---------:---------:---------:---------:---------:---------:---------:---------:----------|
|Android: | 101     | 59      | 39      | 30      | 30      | 29      | 24      | 28      | 23      | 24       |
|SQLite3: | 53      | 24      | 22      | 20      | 20      | 18      | 17      | 18      | 16      | 17       |

|10000条  | 第 1 次 | 第 2 次 | 第 3 次 | 第 4 次 | 第 5 次 | 第 6 次 | 第 7 次 | 第 8 次 | 第 9 次 | 第 10 次 |
|:--------:---------:---------:---------:---------:---------:---------:---------:---------:---------:----------|
|Android: | 341     | 200     | 224     | 205     | 202     | 206     | 196     | 200     | 208     | 217      |
|SQLite3: | 143     | 91      | 91      | 93      | 96      | 94      | 95      | 95      | 91      | 95       |

|100000条 | 第 1 次 | 第 2 次 | 第 3 次 | 第 4 次 | 第 5 次 | 第 6 次 | 第 7 次 | 第 8 次 | 第 9 次 | 第 10 次 |
|:--------:---------:---------:---------:---------:---------:---------:---------:---------:---------:----------|
|Android: | 2111    | 1901    | 1988    | 1943    | 1921    | 1883    | 1881    | 1876    | 1922    | 1904     |
|SQLite3: | 857     | 795     | 792     | 797     | 795     | 791     | 795     | 796     | 794     | 798      |

------

如有问题，请发送邮件至： iqosjay@gmail.com