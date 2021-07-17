package io.webby;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.webby.app.AppConfigException;
import io.webby.app.AppModule;
import io.webby.app.AppSettings;
import io.webby.netty.NettyModule;
import io.webby.url.UrlModule;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Webby {
    @NotNull
    public static Injector startup(@NotNull AppSettings settings) throws AppConfigException {
        validateSettings(settings);
        return Guice.createInjector(new AppModule(settings), new NettyModule(), new UrlModule());
    }

    private static void validateSettings(@NotNull AppSettings settings) {
        String webPath = settings.webPath();
        validateWebPath(webPath);
    }

    private static void validateWebPath(String webPath) {
        if (webPath == null) {
            throw new AppConfigException("Invalid app settings: static web path is not set");
        }
        if (!new File(webPath).exists()) {
            throw new AppConfigException("Invalid app settings: static web path does not exist: %s".formatted(webPath));
        }
    }
}
