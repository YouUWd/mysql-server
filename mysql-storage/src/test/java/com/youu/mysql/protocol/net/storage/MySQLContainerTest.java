package com.youu.mysql.protocol.net.storage;

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

    @Test
    public void test1() throws SQLException {
        // Add MYSQL_ROOT_HOST environment so that we can root login from anywhere for testing purposes
        try (MySQLContainer<?> mysql = new MySQLContainer<>(MYSQL_80_IMAGE)
            .withDatabaseName("db1")
            .withUsername("root")
            .withPassword("pass")) {

            mysql.start();

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
            int columnCount = data.getColumnCount();
            if (data instanceof com.mysql.cj.jdbc.result.ResultSetMetaData) {
                List<Integer> collect = Stream.of(((com.mysql.cj.jdbc.result.ResultSetMetaData)data).getFields()).map(
                    field -> field
                        .getMysqlTypeId()).collect(Collectors.toList());
                System.out.println(collect);
            }
            //getTableName->t1
            //getScale->0
            //getColumnDisplaySize->8
            //getColumnName->s0
            //getSchemaName->
            //getCatalogName->db1
            //getColumnTypeName->DECIMAL
            //getColumnClassName->java.math.BigDecimal
            //getColumnLabel->s0
            //getColumnType->3
            //getPrecision->8
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(data.getColumnName(i));
                int columnType = data.getColumnType(i);
                System.out.println(columnType);
                System.out.println(data.getColumnTypeName(i));
                System.out.println("===============");
            }
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
