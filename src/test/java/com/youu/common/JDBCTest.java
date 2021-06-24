package com.youu.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.h2.tools.Server;
import org.junit.Test;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/24
 */
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
    public void test3() throws InvocationTargetException, IllegalAccessException {
        org.h2.Driver.load();
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:d1;MODE=MYSQL;database_to_upper=false", "sa",
            "sa")) {
            Statement stat = conn.createStatement();
            stat.execute("create table t1 (id int, name varchar(16))");
            stat.execute("insert into t1 values(1,'a')");

            ResultSet resultSet = stat.executeQuery("show databases");

            System.out.println(resultSet);

            ResultSetMetaData data = resultSet.getMetaData();
            int columnCount = data.getColumnCount();
            Class<ResultSetMetaData> metaDataClass = ResultSetMetaData.class;
            Method[] methods = metaDataClass.getDeclaredMethods();
            System.out.println(Arrays.toString(methods));
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("==============");
                for (Method method : methods) {
                    if (method.getName().startsWith("get") && method.getParameterCount() == 1) {
                        method.setAccessible(true);
                        Object res = method.invoke(data, i);
                        System.out.println(method.getName() + "->" + res);
                    }
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }
}
