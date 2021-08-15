package io.webby.netty;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NettyDispatcherTest {
    @Test
    public void parseProtocol_simple() {
        Truth.assertThat(NettyDispatcher.parseProtocol("")).isEmpty();
        Truth.assertThat(NettyDispatcher.parseProtocol(",")).isEmpty();
        Truth.assertThat(NettyDispatcher.parseProtocol("foo,bar")).isEmpty();
        Truth.assertThat(NettyDispatcher.parseProtocol("foo, bar ,,,")).isEmpty();
        Truth.assertThat(NettyDispatcher.parseProtocol("foo, a|b")).containsExactly("a", "b");
        Truth.assertThat(NettyDispatcher.parseProtocol("foo, a|b, x|, |y")).containsExactly("a", "b", "x", "", "", "y");
        Truth.assertThat(NettyDispatcher.parseProtocol("foo, a|b|c, |||")).containsExactly("a", "b|c", "", "||");
        Truth.assertThat(NettyDispatcher.parseProtocol("foo, |")).containsExactly("", "");
        Assertions.assertThrows(IllegalStateException.class, () -> NettyDispatcher.parseProtocol("a|b, a|c"));
    }
}
