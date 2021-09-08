package io.webby.ws.impl;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.ClasspathScanner;
import io.webby.url.annotate.Serve;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WebsocketAgentScanner {
    @Inject private Settings settings;
    @Inject private ClasspathScanner scanner;

    public @NotNull Set<? extends Class<?>> getAgentClassesFromClasspath() {
        return scanner.getAnnotatedClasses(settings.handlerFilter(), Serve.class);
    }
}
