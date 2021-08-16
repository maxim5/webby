package io.webby.ws.impl;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebsocketAgentBinderTest {
    // private final WebsocketAgentBinder binder = Testing.testStartupNoHandlers().getInstance(WebsocketAgentBinder.class);

    @Test
    public void idFromName_simple() {
        Assertions.assertEquals("on", WebsocketAgentBinder.idFromName("on"));
        Assertions.assertEquals("foo", WebsocketAgentBinder.idFromName("onFoo"));
        Assertions.assertEquals("foobar", WebsocketAgentBinder.idFromName("onFooBar"));
        Assertions.assertEquals("otherName", WebsocketAgentBinder.idFromName("otherName"));
        Assertions.assertEquals("123", WebsocketAgentBinder.idFromName("123"));
    }

    @Test
    public void idFromName_too_long() {
        String longName = Strings.repeat("0123456789", 10);
        Assertions.assertEquals("0123456789012345678901234567890123456789012345678901234567890123",
                                WebsocketAgentBinder.idFromName(longName));
    }
}
