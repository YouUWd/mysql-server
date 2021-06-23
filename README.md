# MySQL-Protocol

MySQL通信协议，从0到1，创建一个MySQL Sever。

## 启动MySQL Server

```shell
# start mysql server
mvn clean compile exec:java -Dexec.mainClass="com.youu.mysql.protocol.net.MySQLServer"
# open mysql client
mysql -h127.0.0.1 -P3306
```



## Road Map

- Parse引擎
- 内存数据库
- 行列并存(OLTP OLAP HTAP)

