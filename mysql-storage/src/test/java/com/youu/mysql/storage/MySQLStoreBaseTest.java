package com.youu.mysql.storage;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/27
 */
public class MySQLStoreBaseTest {
    private static final DockerImageName MYSQL_80_IMAGE = DockerImageName.parse("mysql:8.0.25");

    protected static final String USER_NAME = "root";
    protected static final String PASS_WORD = "pass";
    protected static final String DATABASE = "test";

    protected static final MySQLContainer MYSQL = new MySQLContainer<>(MYSQL_80_IMAGE)
        .withDatabaseName(DATABASE)
        .withUsername(USER_NAME)
        .withPassword(PASS_WORD);

    static {
        MYSQL.start();
    }
}
