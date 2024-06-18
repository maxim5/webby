package io.webby.netty.response;

import com.google.inject.Inject;
import io.webby.common.InjectorHelper;
import io.webby.netty.HttpConst;
import io.spbx.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;

import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.spbx.util.base.EasyNulls.*;

public class ContentTypeDetector {
    private ContentTypeProvider contentTypeProvider;

    @Inject
    private void init(@NotNull InjectorHelper helper) {
        contentTypeProvider = helper.getOrDefault(ContentTypeProvider.class, () -> path -> null);
    }

    public @NotNull CharSequence guessContentType(@NotNull Path path) {
        return firstNonNull(
            List.of(
                () -> contentTypeProvider.getContentType(path),
                () -> URLConnection.guessContentTypeFromName(path.toString()),
                Unchecked.Suppliers.rethrow(() -> Files.probeContentType(path)),
                Unchecked.Suppliers.rethrow(() -> ThirdPartyMimeTypeDetectors.detect(path.toFile()))
            ),
            HttpConst.APPLICATION_OCTET_STREAM
        );
    }
}
