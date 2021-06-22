package com.youu.mysql.protocol.net.pkg.req;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.youu.mysql.protocol.net.pkg.CapabilityFlags;
import com.youu.mysql.protocol.net.pkg.MySQLCharacterSet;
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
    private Set<CapabilityFlags> capabilityFlags = EnumSet.noneOf(CapabilityFlags.class);
    private int maxPacketSize;
    private MySQLCharacterSet characterSet;
    private String username;
    private String database;
    private String authPluginName;
    private Map<String, String> attributes = new HashMap<>();

    @Override
    protected void readPayload(ByteBuf buffer) {
        //TODO 密码校验等
        long capabilityFlags = buffer.readUnsignedIntLE();
        long maxPacketSize = buffer.readUnsignedIntLE();
        byte charset = buffer.readByte();
        buffer.skipBytes(23);//reserved (all [0])
        String username = MySQLBufUtil.readNullTerminatedString(buffer);
        buffer.readerIndex(getPayloadLength() + 4);
    }

    @Override
    protected void writePayload(ByteBuf buffer) {

    }

}
