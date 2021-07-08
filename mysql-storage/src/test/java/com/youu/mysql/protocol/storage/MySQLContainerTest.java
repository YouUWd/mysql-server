package com.youu.mysql.protocol.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.Assert.assertEquals;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/28
 */
public class MySQLContainerTest {
    public static final DockerImageName MYSQL_80_IMAGE = DockerImageName.parse("mysql:8.0.25");
    private static MySQLContainer<?> mysql;

    @BeforeClass
    public static void init() {
        mysql = new MySQLContainer<>(MYSQL_80_IMAGE)
            //.withDatabaseName("db1")
            .withUsername("root")
            .withPassword("pass");
        mysql.start();
    }

    @Test
    public void test() throws SQLException {
        System.out.println(mysql.getDatabaseName());
        //mysql.withDatabaseName("d2");
        execute(mysql, "create database d1");
        execute(mysql, "use d1");
        System.out.println(mysql.getTestQueryString());
        mysql.withDatabaseName("d1");
        System.out.println(mysql.getDatabaseName());

        ResultSet resultSet = performQuery(mysql, "SELECT database()");
        String resultSetInt = resultSet.getString(1);
        assertEquals("A basic SELECT query succeeds", "d1", resultSetInt);

    }

    /**
     * getMysqlTypeId
     *
     * @throws SQLException
     */
    @Test
    public void test1() throws SQLException {

        execute(mysql, "CREATE TABLE `t1` (\n"
            + "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,\n"
            + "  `s0` decimal(8,0) DEFAULT NULL,\n"
            + "  `s1` double DEFAULT NULL,\n"
            + "  `s2` text,\n"
            + "  `s3` blob,\n"
            + "  PRIMARY KEY (`id`)\n"
            + ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8");

        ResultSet resultSet = performQuery(mysql, "SELECT * from t1");

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
