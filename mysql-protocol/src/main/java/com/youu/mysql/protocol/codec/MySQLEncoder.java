package com.youu.mysql.protocol.codec;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/16
 */
public class MySQLEncoder extends MessageToByteEncoder<MySQLPacket> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MySQLPacket packet, ByteBuf buffer) {
        packet.write(buffer);
    }
}
