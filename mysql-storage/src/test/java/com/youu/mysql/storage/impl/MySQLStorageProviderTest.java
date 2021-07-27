package com.youu.mysql.storage.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.youu.mysql.storage.MySQLStoreBaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MySQLStorageProviderTest extends MySQLStoreBaseTest {

    private static MySQLStorageProvider provider;

    @BeforeClass
    public static void init() {

        provider = new MySQLStorageProvider(MYSQL.getJdbcUrl(), USER_NAME, PASS_WORD);
    }

    @Before
    public void initDB() throws SQLException {
        provider.init(DATABASE);
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