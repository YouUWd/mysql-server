# MySQL-Protocol
--------
**MySQL通信协议，从0到1，创建一个 Java版 MySQL Sever**

## 启动MySQL Server

### 方案1
```shell
# start mysql server
run com.youu.mysql.protocol.net.MySQLServer
```
### 方案2
```shell
# start mysql server
mvn clean package -Dmaven.test.skip
java -jar distribution/mysql-server.jar
```
### 自定义启动端口
```
# asign port 6060 or others as you wish
java -Dport=6060 -jar distribution/mysql-server.jar
```

## 使用MySQL客户端连接MySQL-SERVER
```
# open mysql client
mysql -h127.0.0.1 -P3306
```



## Road Map
- Parse引擎
- 内存数据库
- 行列并存(OLTP OLAP HTAP)

## MySQL Storage Engine Architecture
[MySQL Architecture with Pluggable Storage Engines](https://dev.mysql.com/doc/refman/8.0/en/pluggable-storage-overview.html)

![Architecture](https://dev.mysql.com/doc/refman/8.0/en/images/mysql-architecture.png)