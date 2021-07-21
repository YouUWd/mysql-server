package com.youu.mysql.protocol.net;

import lombok.Builder;
import lombok.Data;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/8
 */
@Data
@Builder
public class ConnectionAttr {
    private int connectionId;
    private String schema;
    private int clientCharset;
}
