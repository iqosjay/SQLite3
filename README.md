
## Android SQLite3 NDK 封装

[Demo下载](https://github.com/iqosjay/SQLite3/releases/download/1.0.0/sqlite3_demo_1.0.0.apk)
（操作：按钮新增 按钮查询 点按编辑 长按删除）

### 写在前面 

[sqlite3](https://sqlite.org/index.html) 开源、集成简单（现在的版本只有2个文件 sqlite3.h sqlite3.c）

这个库抽离自 [Telegram](https://github.com/DrKLO/Telegram) 的开源代码、作者：[DrKLO](https://github.com/DrKLO)

我个人感觉 Telegram 的源码每一行都值得学习（如果你追求极致性能、更应该研究）

### 有什么用？或者我为什么要用你这个？

1、与 Android 原生 sqlite3 相比性能更高效（我测试了但是没统计、有兴趣的朋友自己试试看吧、性能大概是原生 Android 的5倍左右）

2、API更简洁

3、调用更安全、所有API都抛受检异常而非运行时异常（这可以降低App的崩溃率）


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

2、再在你需要使用的 模块级的 build.gradle 中添加依赖 'io.github.iqosjay:sqlite3:1.0.0'

```
dependencies {
  ...
  implementation 'io.github.iqosjay:sqlite3:1.0.0' //就是我了~~
  ...
}
```

具体调用：

[SQLiteActivity.java](https://github.com/iqosjay/SQLite3/blob/main/app/src/main/java/com/roy/sqlite3/SQLiteActivity.java) 前 4 个方法分别演示了增、删、改、查 和 不同的数据类型

