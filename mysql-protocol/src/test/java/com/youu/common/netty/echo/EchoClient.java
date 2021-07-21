package com.youu.common.netty.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/14
 */
@Slf4j
public class EchoClient {
    private static final String HOST = System.getProperty("host", "127.0.0.1");
    private static final int PORT = Integer.parseInt(System.getProperty("port", "8322"));

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                             @Override
                             protected void initChannel(SocketChannel ch) {
                                 ChannelPipeline pipeline = ch.pipeline();

                                 // and then business logic.
                                 pipeline.addLast(new EchoClientHandler());
                             }
                         }
                );

            // Make a new connection.
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // Get the handler instance to retrieve the answer.
            EchoClientHandler handler =
                (EchoClientHandler)f.channel().pipeline().last();

            String result = handler.sayHello("Timmy");
            log.info("server return: {}", result);
            log.info("server return: {}", handler.sayHello("Jack"));
        } finally {
            group.shutdownGracefully();
        }
    }

}
