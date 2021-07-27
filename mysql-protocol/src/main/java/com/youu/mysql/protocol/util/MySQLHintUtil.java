package com.youu.mysql.protocol.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/21
 */
public class MySQLHintUtil {
    public static Optional<Integer> getIndex(String sql) {
        int start = sql.indexOf("/*+");
        int end = sql.indexOf("*/");
        if ((start | end) > 0) {
            String hint = sql.substring(start + 3, end);
            String r = "(.*)(USE_STORE\\()(\\d)(\\))(.*)";
            Pattern p = Pattern.compile(r, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(hint);

            if (m.find() && m.groupCount() > 3) {
                return Optional.of(Integer.valueOf(m.group(3)));
            }
        }
        return Optional.empty();
    }
}
