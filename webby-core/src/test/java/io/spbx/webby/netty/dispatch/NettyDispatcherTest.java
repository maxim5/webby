package io.spbx.webby.netty.dispatch;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NettyDispatcherTest {
    @Test
    public void parseProtocol_simple() {
        assertThat(NettyDispatcher.parseProtocol("")).isEmpty();
        assertThat(NettyDispatcher.parseProtocol(",")).isEmpty();
        assertThat(NettyDispatcher.parseProtocol("foo,bar")).isEmpty();
        assertThat(NettyDispatcher.parseProtocol("foo, bar ,,,")).isEmpty();
        assertThat(NettyDispatcher.parseProtocol("foo, a|b")).containsExactly("a", "b");
        assertThat(NettyDispatcher.parseProtocol("foo, a|b, x|, |y")).containsExactly("a", "b", "x", "", "", "y");
        assertThat(NettyDispatcher.parseProtocol("foo, a|b|c, |||")).containsExactly("a", "b|c", "", "||");
        assertThat(NettyDispatcher.parseProtocol("foo, |")).containsExactly("", "");
        assertThrows(IllegalStateException.class, () -> NettyDispatcher.parseProtocol("a|b, a|c"));
    }
}
