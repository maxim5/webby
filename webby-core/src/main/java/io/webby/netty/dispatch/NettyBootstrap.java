package io.webby.netty.dispatch;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.webby.app.AppLifetime;
import io.webby.app.AppMaintenance;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.util.log.EasyLogs;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class NettyBootstrap {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private AppMaintenance maintenance;
    @Inject private Provider<NettyDispatcher> nettyDispatcher;
    @Inject private NettyConst nc;

    private final Lifetime.Definition lifetime;

    @Inject
    public NettyBootstrap(@NotNull AppLifetime appLifetime) {
        lifetime = appLifetime.getLifetime();
    }

    public void runLocally(int port) throws InterruptedException {
        attachShutdownHook();

        EventLoopGroup masterGroup = new NioEventLoopGroup(nc.masterThreads);
        lifetime.onTerminate(masterGroup::shutdownGracefully);

        EventLoopGroup workerGroup = new NioEventLoopGroup(nc.workerThreads);
        lifetime.onTerminate(workerGroup::shutdownGracefully);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(masterGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(@NotNull Channel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new HttpServerCodec(nc.maxInitLineLength, nc.maxHeaderLength, nc.maxChunkLength));
                        pipeline.addLast(nettyDispatcher.get());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture httpChannel = bootstrap.bind(port).sync();
            log.at(Level.INFO).log("Server running at %s://%s:%d/", "http", "localhost", port);
            httpChannel.channel().closeFuture().sync();
        } finally {
            lifetime.terminateIfAlive();    // normally a shutdown hook is called, so it shouldn't do anything
        }
    }

    private void attachShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.at(Level.WARNING).log("Shutdown hook received: gracefully terminating the application");

            maintenance.trySetMaintenanceMode();

            log.at(Level.WARNING).log("Terminating application lifetime...");
            lifetime.terminate();
            log.at(Level.WARNING).log("Lifetime terminated");

            log.at(Level.FINE).log("Shutting down the logger...");
            EasyLogs.shutdownAny();
        }));
    }
}
