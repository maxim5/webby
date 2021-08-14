package io.webby.url.ws;

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
    }
}
