package io.spbx.webby.netty.response;

import io.netty.buffer.ByteBufHolder;

public interface ContentHolder extends ByteBufHolder {
    @Override
    default ByteBufHolder copy() {
        return replace(content().copy());
    }

    @Override
    default ByteBufHolder duplicate() {
        return replace(content().duplicate());
    }

    @Override
    default ByteBufHolder retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    default int refCnt() {
        return content().refCnt();
    }

    @Override
    default ByteBufHolder retain() {
        content().retain();
        return this;
    }

    @Override
    default ByteBufHolder retain(int increment) {
        content().retain(increment);
        return this;
    }

    @Override
    default ByteBufHolder touch() {
        content().touch();
        return this;
    }

    @Override
    default ByteBufHolder touch(Object hint) {
        content().touch(hint);
        return this;
    }

    @Override
    default boolean release() {
        return content().release();
    }

    @Override
    default boolean release(int decrement) {
        return content().release(decrement);
    }
}
