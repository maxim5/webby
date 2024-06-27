package io.spbx.webby.netty.dispatch;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.spbx.util.props.PropertyMap;
import org.jetbrains.annotations.NotNull;

class NettyConst {
    public final int masterThreads;
    public final int workerThreads;
    public final int maxInitLineLength;
    public final int maxHeaderLength;
    public final int maxChunkLength;
    public final int maxContentLength;

    @Inject
    public NettyConst(@NotNull PropertyMap properties) {
        masterThreads = properties.getInt("netty.master.group.threads", 0);
        workerThreads = properties.getInt("netty.worker.group.threads", 0);
        maxInitLineLength = properties.getInt("netty.max.init.line.length", HttpObjectDecoder.DEFAULT_MAX_INITIAL_LINE_LENGTH);
        maxHeaderLength = properties.getInt("netty.max.header.length", HttpObjectDecoder.DEFAULT_MAX_HEADER_SIZE);
        maxChunkLength = properties.getInt("netty.max.chunk.length", HttpObjectDecoder.DEFAULT_MAX_CHUNK_SIZE);
        maxContentLength = properties.getInt("netty.content.max.length.bytes", 10 << 20);
    }
}
