package com.youu.mysql.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/29
 */
public class H2MySQLConverter {

    /**
     * @param sql h2 sql语法
     * @return
     */
    public static String convertMySQL(String sql) {
        int start = sql.indexOf("/*+");
        int end = sql.indexOf("*/");
        String tmp = sql;
        if ((start | end) >= 0) {
            tmp = sql.substring(0, start) + sql.substring(end + 2);
        }
        String[] tokens = StringUtils.split(tmp, null, 0);

        //create|drop database -> create|drop schema
        if ((StringUtils.equalsIgnoreCase("create", tokens[0]) || StringUtils.equalsIgnoreCase("drop", tokens[0]))
            && StringUtils.equalsIgnoreCase("database", tokens[1])) {
            return StringUtils.replaceIgnoreCase(sql, "database",
                StringUtils.isAllUpperCase(tokens[1]) ? "SCHEMA" : "schema",
                1);
        }
        return sql;
    }

}
