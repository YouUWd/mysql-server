package com.youu.mysql.protocol.net.pkg.req;

import java.util.HashMap;
import java.util.Map;

import com.youu.mysql.protocol.net.pkg.MySQLPacket;
import com.youu.mysql.protocol.net.util.MySQLBufUtil;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/17
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest extends MySQLPacket {
    private long capabilityFlags;
    private long maxPacketSize;
    private short characterSet;
    private String username;
    private String database;
    private String authPluginName;
    private Map<String, String> attributes = new HashMap<>();

    @Override
    protected void readPayload(ByteBuf buffer) {
        //TODO 密码校验等
        this.capabilityFlags = buffer.readUnsignedIntLE();
        this.maxPacketSize = buffer.readUnsignedIntLE();
        this.characterSet = buffer.readUnsignedByte();
        buffer.skipBytes(23);//reserved (all [0])
        this.username = MySQLBufUtil.readNullTerminatedString(buffer);
        buffer.readerIndex(getPayloadLength() + 4);
    }

    @Override
    protected void writePayload(ByteBuf buffer) {

    }

}
