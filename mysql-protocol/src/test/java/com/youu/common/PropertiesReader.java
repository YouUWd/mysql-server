package com.youu.common;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/15
 */
public class PropertiesReader {
    @Test
    public void test() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        System.out.println(props);
    }
}
