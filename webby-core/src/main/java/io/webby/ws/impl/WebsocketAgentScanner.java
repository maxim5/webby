package io.webby.ws.impl;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.app.AppClasspathScanner;
import io.webby.url.annotate.Serve;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WebsocketAgentScanner {
    @Inject private Settings settings;
    @Inject private AppClasspathScanner scanner;

    public @NotNull Set<Class<?>> getAgentClassesFromClasspath() {
        return scanner.timed("@Serve").getAnnotatedClasses(settings.handlerFilter(), Serve.class);
    }
}
