package io.webby.url.caller;

import io.netty.handler.codec.http.FullHttpRequest;
import io.routekit.util.CharArray;

import java.util.Map;
import java.util.function.BiFunction;

interface CallArgumentFunction extends BiFunction<FullHttpRequest, Map<String, CharArray>, Object> {
}
