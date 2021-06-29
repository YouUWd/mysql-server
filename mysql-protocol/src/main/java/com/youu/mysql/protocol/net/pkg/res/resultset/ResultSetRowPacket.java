package com.youu.mysql.protocol.net.pkg.res.resultset;

import java.util.List;

import com.google.common.collect.Lists;
import com.youu.mysql.protocol.net.pkg.MySQLPacket;
import com.youu.mysql.protocol.util.MySQLBufUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/22
 */
@Data
@ToString(callSuper = true, exclude = "countPacket")
public class ResultSetRowPacket extends MySQLPacket {
    private ColumnCountPacket countPacket;
    private List<String> values = Lists.newArrayList();

    public ResultSetRowPacket(ColumnCountPacket countPacket) {
        this.countPacket = countPacket;
    }

    @Override
    protected void readPayload(ByteBuf buffer) {
        for (int i = 0; i < countPacket.getColumnCount(); i++) {
            values.add(MySQLBufUtil.readLenEncString(buffer));
        }
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        for (String value : values) {
            MySQLBufUtil.writeLenEncString(buffer, value);
        }
    }
}
