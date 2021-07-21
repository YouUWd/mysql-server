package com.youu.mysql.protocol.handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.youu.mysql.protocol.pkg.MySQLPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/15
 */
@Slf4j
public class MySQLClientHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext ctx;

    final BlockingQueue<ByteBuf> answer = new LinkedBlockingQueue<>();

    public ByteBuf handshake() {
        //HandShakeResponse without request
        ByteBuf result;
        boolean interrupted = false;
        for (; ; ) {
            try {
                result = answer.take();
                break;
            } catch (InterruptedException ignore) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    public ByteBuf execute(MySQLPacket packet) {
        //HandShakeResponse without packet
        if (packet != null) {
            ctx.writeAndFlush(packet);
        }

        ByteBuf result;
        boolean interrupted = false;
        for (; ; ) {
            try {
                result = answer.take();
                break;
            } catch (InterruptedException ignore) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        answer.add(((ByteBuf)msg));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channelInactive");
        answer.add(Unpooled.EMPTY_BUFFER);
    }

}
