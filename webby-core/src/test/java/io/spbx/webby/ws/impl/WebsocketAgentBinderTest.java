package io.spbx.webby.ws.impl;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class WebsocketAgentBinderTest {
    // private final WebsocketAgentBinder binder = Testing.testStartupNoHandlers().getInstance(WebsocketAgentBinder.class);

    @Test
    public void idFromName_simple() {
        assertThat(WebsocketAgentBinder.idFromName("on")).isEqualTo("on");
        assertThat(WebsocketAgentBinder.idFromName("onFoo")).isEqualTo("foo");
        assertThat(WebsocketAgentBinder.idFromName("onFooBar")).isEqualTo("foobar");
        assertThat(WebsocketAgentBinder.idFromName("otherName")).isEqualTo("otherName");
        assertThat(WebsocketAgentBinder.idFromName("123")).isEqualTo("123");
    }

    @Test
    public void idFromName_too_long() {
        String longName = Strings.repeat("0123456789", 10);
        assertThat(WebsocketAgentBinder.idFromName(longName))
            .isEqualTo("0123456789012345678901234567890123456789012345678901234567890123");
    }
}
