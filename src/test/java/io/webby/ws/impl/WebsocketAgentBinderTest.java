package io.webby.ws.impl;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebsocketAgentBinderTest {
    // private final WebsocketAgentBinder binder = Testing.testStartupNoHandlers().getInstance(WebsocketAgentBinder.class);

    @Test
    public void idFromName_simple() {
        assertEquals("on", WebsocketAgentBinder.idFromName("on"));
        assertEquals("foo", WebsocketAgentBinder.idFromName("onFoo"));
        assertEquals("foobar", WebsocketAgentBinder.idFromName("onFooBar"));
        assertEquals("otherName", WebsocketAgentBinder.idFromName("otherName"));
        assertEquals("123", WebsocketAgentBinder.idFromName("123"));
    }

    @Test
    public void idFromName_too_long() {
        String longName = Strings.repeat("0123456789", 10);
        assertEquals("0123456789012345678901234567890123456789012345678901234567890123",
                     WebsocketAgentBinder.idFromName(longName));
    }
}
