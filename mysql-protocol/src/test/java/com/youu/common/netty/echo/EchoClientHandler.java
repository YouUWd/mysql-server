package com.youu.common.netty.echo;

import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/14
 */
@Slf4j
public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext ctx;

    final BlockingQueue<String> answer = new LinkedBlockingQueue<>();

    public String sayHello(String name) {
        log.info("sayHello {}", name);
        ctx.writeAndFlush(Unpooled.copiedBuffer(name, Charset.defaultCharset()));
        String result;
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
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        TimeUnit.SECONDS.sleep(3);
        log.info("channelRegistered");
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("Read 1: {}", msg);
        String as = ((ByteBuf)msg).toString(Charset.defaultCharset());
        log.info("Read 2: {}", as);
        answer.add(as);
    }
}
