package io.webby.url.ws;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.ClasspathScanner;
import io.webby.url.annotate.ServeWebsocket;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WebsocketAgentScanner {
    @Inject private Settings settings;
    @Inject private ClasspathScanner scanner;

    @NotNull
    public Set<? extends Class<?>> getAgentClassesFromClasspath() {
        return scanner.getMatchingClasses(
                settings.handlerFilter(),
                klass -> klass.isAnnotationPresent(ServeWebsocket.class),
                "websocket-agent"
        );
    }
}
