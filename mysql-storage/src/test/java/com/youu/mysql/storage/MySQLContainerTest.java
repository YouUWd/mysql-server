package com.youu.mysql.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.junit.Assert.assertEquals;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/28
 */
public class MySQLContainerTest extends MySQLStoreBaseTest {

    @Test
    public void test1() throws SQLException {
        System.out.println(MYSQL.getDatabaseName());
        //mysql.withDatabaseName("d2");
        execute(MYSQL, "create database d1");
        execute(MYSQL, "use d1");
        System.out.println(MYSQL.getTestQueryString());
        MYSQL.withDatabaseName("d1");
        System.out.println(MYSQL.getDatabaseName());

        ResultSet resultSet = performQuery(MYSQL, "SELECT database()");
        String resultSetInt = resultSet.getString(1);
        assertEquals("A basic SELECT query succeeds", "d1", resultSetInt);

    }

    /**
     * getMysqlTypeId
     *
     * @throws SQLException
     */
    @Test
    public void test2() throws SQLException {

        execute(MYSQL, "CREATE TABLE `t1` (\n"
            + "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n"
            + "  `s0` decimal(8,0) DEFAULT NULL,\n"
            + "  `s1` double DEFAULT NULL,\n"
            + "  `s2` text,\n"
            + "  `s3` blob,\n"
            + "  PRIMARY KEY (`id`)\n"
            + ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8");

        ResultSet resultSet = performQuery(MYSQL, "SELECT * from t1");

        ResultSetMetaData data = resultSet.getMetaData();
        if (data instanceof com.mysql.cj.jdbc.result.ResultSetMetaData) {
            List<Integer> collect = Stream.of(((com.mysql.cj.jdbc.result.ResultSetMetaData)data).getFields()).map(
                field -> field
                    .getMysqlTypeId()).collect(Collectors.toList());
            System.out.println(collect);
        }

    }

    protected void execute(JdbcDatabaseContainer<?> container, String sql) throws SQLException {
        Connection connection = DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(),
            container.getPassword());
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    protected ResultSet performQuery(JdbcDatabaseContainer<?> container, String sql) throws SQLException {
        Connection connection = DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(),
            container.getPassword());
        Statement statement = connection.createStatement();
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        return resultSet;
    }

}
