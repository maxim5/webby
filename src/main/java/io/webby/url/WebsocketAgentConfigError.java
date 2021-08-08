package io.webby.url;

import io.webby.app.AppConfigException;

public class WebsocketAgentConfigError extends AppConfigException {
    public WebsocketAgentConfigError(String message) {
        super(message);
    }

    public WebsocketAgentConfigError(String message, Throwable cause) {
        super(message, cause);
    }

    public WebsocketAgentConfigError(Throwable cause) {
        super(cause);
    }

    public static void failIf(boolean cond, String message) {
        if (cond) {
            throw new WebsocketAgentConfigError(message);
        }
    }
}
