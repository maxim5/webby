package io.webby.netty.response;

import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

public class IgnoringHttpHeaders extends EmptyHttpHeaders {
    public static final IgnoringHttpHeaders INSTANCE = new IgnoringHttpHeaders();

    @Override
    public HttpHeaders add(String name, Object value) {
        return this;
    }

    @Override
    public HttpHeaders add(String name, Iterable<?> values) {
        return this;
    }

    @Override
    public HttpHeaders addInt(CharSequence name, int value) {
        return this;
    }

    @Override
    public HttpHeaders addShort(CharSequence name, short value) {
        return this;
    }

    @Override
    public HttpHeaders set(String name, Object value) {
        return this;
    }

    @Override
    public HttpHeaders set(String name, Iterable<?> values) {
        return this;
    }

    @Override
    public HttpHeaders setInt(CharSequence name, int value) {
        return this;
    }

    @Override
    public HttpHeaders setShort(CharSequence name, short value) {
        return this;
    }

    @Override
    public HttpHeaders remove(String name) {
        return this;
    }

    @Override
    public HttpHeaders clear() {
        return this;
    }
}
