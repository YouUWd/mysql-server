package com.youu.mysql.protocol.base;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/26
 */
public class MySQLContainerBaseTest {
    protected static boolean localTest = false;
    protected static final DockerImageName MYSQL_80_IMAGE = DockerImageName.parse("mysql:8.0.25");

    protected static final String USER_NAME = "root";
    protected static final String PASS_WORD = "pass";

    protected static final MySQLContainer MYSQL = new MySQLContainer<>(MYSQL_80_IMAGE)
        .withDatabaseName("test")
        .withUsername(USER_NAME)
        .withPassword(PASS_WORD);

    static {
        if (!localTest) {
            MYSQL.start();
        }
    }

}
