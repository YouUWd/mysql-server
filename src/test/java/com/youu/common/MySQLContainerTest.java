package com.youu.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    @Test
    public void test() throws SQLException {
        // Add MYSQL_ROOT_HOST environment so that we can root login from anywhere for testing purposes
        try (MySQLContainer<?> mysql = new MySQLContainer<>(MYSQL_80_IMAGE)
            .withDatabaseName("db1")
            .withUsername("root")
            .withPassword("pass")) {

            mysql.start();

            ResultSet resultSet = performQuery(mysql, "SELECT 1");

            int resultSetInt = resultSet.getInt(1);
            assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
        }

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
