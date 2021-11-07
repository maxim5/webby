package io.webby.netty.response;


import com.google.common.collect.ImmutableMap;
import io.webby.util.reflect.EasyClasspath;
import io.webby.util.func.ThrowFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ThirdPartyMimeTypeDetectors {
    private static final Map<String, ThrowFunction<File, String, Exception>> SUPPORTED_DETECTORS = ImmutableMap.of(
        "com.j256.simplemagic.ContentInfo", ThirdPartyMimeTypeDetectors::runSimpleMagic,
        "eu.medsea.mimeutil.MimeUtil2", ThirdPartyMimeTypeDetectors::runMimeUtil2
    );

    public static @Nullable String detect(@NotNull File file) throws Exception {
        for (Map.Entry<String, ThrowFunction<File, String, Exception>> entry : SUPPORTED_DETECTORS.entrySet()) {
            String className = entry.getKey();
            ThrowFunction<File, String, Exception> function = entry.getValue();
            if (EasyClasspath.isInClassPath(className)) {
                String mimeType = function.apply(file);
                if (mimeType != null) {
                    return mimeType;
                }
            }
        }
        return null;
    }

    private static @Nullable String runSimpleMagic(@NotNull File file) throws IOException {
        com.j256.simplemagic.ContentInfoUtil util = new com.j256.simplemagic.ContentInfoUtil();
        com.j256.simplemagic.ContentInfo info = file.exists() ? util.findMatch(file) : null;
        if (info == null) {
            info = com.j256.simplemagic.ContentInfoUtil.findExtensionMatch(file.getName());
        }
        return info != null ? info.getMimeType() : null;
    }

    private static @NotNull String runMimeUtil2(@NotNull File file) {
        eu.medsea.mimeutil.MimeUtil2 mimeUtil = new eu.medsea.mimeutil.MimeUtil2();
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        return eu.medsea.mimeutil.MimeUtil2.getMostSpecificMimeType(mimeUtil.getMimeTypes(file)).toString();
    }
}
