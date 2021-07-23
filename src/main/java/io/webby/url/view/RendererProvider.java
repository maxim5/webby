package io.webby.url.view;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.jetbrains.annotations.NotNull;

public class RendererProvider {
    @Inject private Injector injector;

    @Provides
    @NotNull
    public Renderer getRenderer() {
        // return injector.getInstance(FreeMarkerRenderer.class);
        return injector.getInstance(JteRenderer.class);
    }
}
