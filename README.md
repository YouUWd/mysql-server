# MySQL Sever Framework for Java
--------
[![Java CI with Maven](https://github.com/YouUWd/mysql-server/actions/workflows/maven.yml/badge.svg)](https://github.com/YouUWd/mysql-server/actions/workflows/maven.yml)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

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

## 想要体验分布式版本，请参考注释配置数据源
> mysql-server/mysql-storage/src/main/resources/config.properties 
```
schema=127.0.0.1:3306
username=root
password=pass
# 新增用户需要在这里配置，英文逗号分隔，后续需要自动注册
user_pass=root:pass
storages=127.0.0.1:33050,127.0.0.1:33060
```
### 启动集群版Server
> 先启动后端的MySQL集群，然后启动代理 server

```
java -Dcluster=true -jar distribution/mysql-server.jar
```
```
# 使用HINT指定具体的store查询
mysql> /*+ USE_STORE(1)  */select 1;
+---+
| 1 |
+---+
| 1 |
+---+
```

## Road Map
- Parse引擎
- 内存数据库
- 行列并存(OLTP OLAP HTAP)

## MySQL Storage Engine Architecture
[MySQL Architecture with Pluggable Storage Engines](https://dev.mysql.com/doc/refman/8.0/en/pluggable-storage-overview.html)

![Architecture](https://dev.mysql.com/doc/refman/8.0/en/images/mysql-architecture.png)