package io.spbx.webby.ws.impl;

import com.google.inject.Inject;
import io.spbx.util.classpath.ClasspathScanner;
import io.spbx.webby.app.Settings;
import io.spbx.webby.url.annotate.Serve;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WebsocketAgentScanner {
    @Inject private Settings settings;
    @Inject private ClasspathScanner scanner;

    public @NotNull Set<Class<?>> getAgentClassesFromClasspath() {
        return scanner.timed("@Serve").getAnnotatedClasses(settings.handlerFilter(), Serve.class);
    }
}
