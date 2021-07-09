package io.webby;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.routekit.Match;
import io.routekit.Router;
import io.routekit.RouterSetup;
import io.webby.netty.NettyChannelHandler;
import io.webby.url.UrlRouter;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Main {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(new FileInputStream("build/resources/main/logging.properties"));

        // checkRouter();
        runNetty(8888);
    }

    private static void runNetty(int port) throws InterruptedException {
        Injector injector = Guice.createInjector();

        EventLoopGroup masterGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(masterGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                            pipeline.addLast(new NettyChannelHandler(injector.getInstance(UrlRouter.class)));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture httpChannel = bootstrap.bind(port).sync();
            log.at(Level.INFO).log("Server running at %d", port);
            httpChannel.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            masterGroup.shutdownGracefully();
        }
    }

    private static void checkRouter() {
        Router<String> router = new RouterSetup<String>()
                .add("/", "home")
                .add("/index", "index")
                .add("/user", "all_users")
                .add("/user/{id}", "user")
                .add("/post", "all_posts")
                .add("/post/{id}", "post")
                .add("/post/{id}/{slug}", "post")
                .build();

        String url = "/post/12345/java-microbenchmark-harness";
        Match<String> match = router.routeOrNull(url);
        log.at(Level.INFO).log("%s -> %s", url, match);
    }
}
