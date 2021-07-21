package com.youu.mysql.protocol;

import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class NettyMySQLProxyTest {

    private static int PORT;
    private static String PROXY_2_HOST;
    private static int PROXY_2_PORT;

    public static final DockerImageName MYSQL_80_IMAGE = DockerImageName.parse("mysql:8.0.25");
    private static MySQLContainer<?> mysql;

    private static final String USER_NAME = "root";
    private static final String PASS_WORD = "pass";

    @BeforeClass
    public static void init() {
        mysql = new MySQLContainer<>(MYSQL_80_IMAGE)
            .withDatabaseName("test")
            .withUsername(USER_NAME)
            .withPassword(PASS_WORD);
        mysql.start();
        String jdbcUrl = mysql.getJdbcUrl();
        URI uri = URI.create(jdbcUrl.substring(5));
        PROXY_2_HOST = uri.getHost();
        PROXY_2_PORT = uri.getPort();
        log.info("{} Proxy to {} {}", jdbcUrl, PROXY_2_HOST, PROXY_2_PORT);
    }

    @Test
    public void test() throws ClassNotFoundException, SQLException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                startNettyTcpProxyServer(countDownLatch);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        countDownLatch.await();
        Class.forName("com.mysql.cj.jdbc.Driver");

        Statement statement = DriverManager.getConnection("jdbc:mysql://127.0.0.1:" + PORT + "/test?useSSL=true",
            USER_NAME,
            PASS_WORD)
            .createStatement();

        ResultSet resultSet = performQuery(statement, "SELECT 1");
        while (resultSet.next()) {
            log.info("ResultSet {}", resultSet.getInt(1));
            Assert.assertEquals(resultSet.getInt(1), 1);
        }
        statement.close();
    }

    protected void execute(Statement statement, String sql) throws SQLException {
        statement.execute(sql);
    }

    protected ResultSet performQuery(Statement statement, String sql) throws SQLException {
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();
        return resultSet;
    }

    public void startNettyTcpProxyServer(CountDownLatch countDownLatch) throws InterruptedException {

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
                        p.addLast(new NettyProxyServerHandler());
                    }
                });

            // Start the server.
            ChannelFuture f = b.bind(0).sync();
            PORT = ((InetSocketAddress)f.channel().localAddress()).getPort();
            countDownLatch.countDown();
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
            log.info("xxxxxxxxxxxxxxxxxxxxxxxxxxxx");
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