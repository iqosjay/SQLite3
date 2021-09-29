
### Android SQLite3 NDK 封装

[Demo下载](https://github.com/iqosjay/SQLite3/releases/download/1.0.0/sqlite3_demo_1.0.0.apk)

#### 写在前面 

[sqlite3](https://sqlite.org/index.html) 开源、集成简单（现在的版本只有2个文件 sqlite3.h sqlite3.c）

这个库抽离自 [Telegram](https://github.com/DrKLO/Telegram) 的开源代码、作者：[DrKLO](https://github.com/DrKLO)

我个人感觉 Telegram 的源码每一行都值得学习（如果你追求极致性能、更应该研究）

#### 如何使用？

[SQLiteActivity.java](https://github.com/iqosjay/SQLite3/blob/main/app/src/main/java/com/roy/sqlite3/SQLiteActivity.java) 前 4 个方法分别演示了增、删、改、查 和 不同的数据类型

仓库国庆过了再发，假如你立刻要用

1、Android Studio 下载 NDK

2、下载源码把 『database』模块添加依赖就行了

