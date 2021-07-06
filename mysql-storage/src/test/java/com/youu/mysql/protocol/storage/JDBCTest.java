package com.youu.mysql.protocol.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.h2.value.DataType;
import org.junit.Test;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/24
 */
@Slf4j
public class JDBCTest {
    @Test
    public void test() throws SQLException, InterruptedException {
        Server server = Server.createTcpServer("-tcpPort", "9101", "-baseDir", "~/h2/tcp", "-ifNotExists");
        server.start();
        org.h2.Driver.load();
        try (Connection conn = DriverManager.getConnection(
            "jdbc:h2:tcp://localhost:9101/~/h2/tcp/d1;database_to_upper=false", "sa", "sa")) {
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("show tables");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
            System.out.println("=============");
            stat.execute("drop table if exists t1");
            stat.execute("drop table if exists T1");
            stat.execute("create table if not exists t1(id int,name varchar(16))");
            rs = stat.executeQuery("select database()");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
            rs = stat.executeQuery("show tables");
            System.out.println("=============");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        server.shutdown();
    }

    @Test
    public void test1() {
        org.h2.Driver.load();
        try (Connection conn = DriverManager.getConnection("jdbc:h2:~/h2/test;database_to_upper=false", "sa", "sa")) {
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("show tables");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
            System.out.println("=============");
            stat.execute("create database d2");
            stat.execute("drop table if exists t1");
            stat.execute("drop table if exists T1");
            stat.execute("create table if not exists t1(id int,name varchar(16))");
            rs = stat.executeQuery("select database()");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
            rs = stat.executeQuery("show tables");
            System.out.println("=============");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    @Test
    public void test2() {
        org.h2.Driver.load();
        try (Connection conn = DriverManager.getConnection(
            "jdbc:h2:mem:d1;MODE=MYSQL;database_to_upper=false;INIT=CREATE SCHEMA IF NOT EXISTS d1", "sa", "sa")) {
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("show tables");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
            System.out.println("=============");
            stat.execute("drop table if exists t1");
            stat.execute("drop table if exists T1");
            stat.execute("create table if not exists t1(id int,name varchar(16))");
            rs = stat.executeQuery("show databases");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
            rs = stat.executeQuery("show tables");
            System.out.println("=============");
            while (rs.next()) {
                System.out.println((rs.getString(1)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    @Test
    public void test3() {
        org.h2.Driver.load();
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:d1;MODE=MYSQL;database_to_upper=false", "sa",
            "sa")) {
            Statement stat = conn.createStatement();
            stat.execute("CREATE TABLE `t1` (\n"
                + "  `id` bigint unsigned NOT NULL AUTO_INCREMENT,\n"
                + "  `s0` decimal(8,0) DEFAULT NULL,\n"
                + "  `s1` double DEFAULT NULL,\n"
                + "  `s2` text,\n"
                + "  `s3` blob,\n"
                + "  PRIMARY KEY (`id`)\n"
                + ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8");
            stat.execute("insert into t1 values(1,null,null,null,null)");

            ResultSet resultSet = stat.executeQuery("select * from t1");

            System.out.println(resultSet);

            ResultSetMetaData data = resultSet.getMetaData();
            int columnCount = data.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(data.getColumnName(i));
                int columnType = data.getColumnType(i);
                System.out.println(columnType);
                System.out.println(data.getColumnTypeName(i));
                System.out.println("===============");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    @Test
    public void test4() {
        org.h2.Driver.load();
        ArrayList<DataType> types = DataType.getTypes();
        System.out.println(types.size());
        for (DataType type : types) {
            log.info("{} {} {}", type.name, type.sqlType, type);
        }

    }
}
