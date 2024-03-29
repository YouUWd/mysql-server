package com.youu.mysql.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

@Slf4j
public class NettyTcpProxyTest {

    private static final int PORT = 3306;
    private static final String PROXY_2_HOST = "127.0.0.1";
    private static final int PROXY_2_PORT = 33050;

    @Ignore
    @Test
    public void testNettyTcpProxyServer() throws InterruptedException {

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new LoggingHandler(LogLevel.INFO));
                        p.addLast(new NettyProxyServerHandler());
                    }
                });

            // Start the server.
            ChannelFuture f = b.bind(PORT).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class NettyProxyServerHandler extends ChannelInboundHandlerAdapter {

        private Channel clientChannel;

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // 创建netty client,连接到远程地址
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new LoggingHandler(LogLevel.INFO));
                        p.addLast(new NettyProxyClientHandler(ctx.channel()));
                    }
                });
            ChannelFuture future = bootstrap.connect(PROXY_2_HOST, PROXY_2_PORT);

            future.addListener(ls -> {
                if (ls.isSuccess()) {
                    log.info("suc");
                } else {
                    log.error("fail");
                    ctx.channel().close();
                }
            });
            clientChannel = future.channel();

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            log.info("NettyProxyServerHandler.channelRead {}", msg);
            clientChannel.writeAndFlush(msg);
        }
    }

    static class NettyProxyClientHandler extends ChannelInboundHandlerAdapter {
        private Channel proxyChannel;

        public NettyProxyClientHandler(Channel channel) {
            this.proxyChannel = channel;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            log.info("NettyProxyClientHandler.channelRead {}", msg);
            proxyChannel.writeAndFlush(msg);
        }
    }

}