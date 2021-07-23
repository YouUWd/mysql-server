package com.youu.mysql.storage.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class MySQLStorageProviderTest {

    private static final DockerImageName MYSQL_80_IMAGE = DockerImageName.parse("mysql:8.0.25");
    private static MySQLStorageProvider provider;

    private static final String DB = "db1", USERNAME = "root", PASSWORD = "pass";

    @BeforeClass
    public static void init() {
        MySQLContainer mysql = new MySQLContainer<>(MYSQL_80_IMAGE)
            .withDatabaseName(DB)
            .withUsername(USERNAME)
            .withPassword(PASSWORD);
        mysql.start();
        provider = new MySQLStorageProvider(mysql.getJdbcUrl(), USERNAME, PASSWORD);
    }

    @Before
    public void initDB() throws SQLException {
        provider.init(DB);
    }

    @Test
    public void execute() throws SQLException {
        provider.execute("create database d2");
    }

    @Test
    public void executeQuery() throws SQLException {
        ResultSet resultSet = provider.executeQuery("select 1");
        while (resultSet.next()) {
            int r = resultSet.getInt("1");
            Assert.assertEquals(1, r);
        }
    }

    @Test
    public void release() throws SQLException {
        provider.release();
    }
}