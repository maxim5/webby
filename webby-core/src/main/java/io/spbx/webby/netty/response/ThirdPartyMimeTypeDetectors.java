package io.spbx.webby.netty.response;


import com.google.common.collect.ImmutableMap;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import io.spbx.util.classpath.EasyClasspath;
import io.spbx.util.func.ThrowFunction;
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

    // https://github.com/j256/simplemagic
    private static @Nullable String runSimpleMagic(@NotNull File file) throws IOException {
        ContentInfoUtil util = new ContentInfoUtil();
        ContentInfo info = file.exists() ? util.findMatch(file) : null;
        if (info == null) {
            info = ContentInfoUtil.findExtensionMatch(file.getName());
        }
        return info != null ? info.getMimeType() : null;
    }

    private static @Nullable String runMimeUtil2(@NotNull File file) {
        MimeUtil2 mimeUtil = new MimeUtil2();

        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        MimeType mimeType = MimeUtil2.getMostSpecificMimeType(mimeUtil.getMimeTypes(file));
        if (mimeType != MimeUtil2.UNKNOWN_MIME_TYPE) {
            return mimeType.toString();
        }

        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        mimeType = MimeUtil2.getMostSpecificMimeType(mimeUtil.getMimeTypes(file.getName()));
        if (mimeType != MimeUtil2.UNKNOWN_MIME_TYPE) {
            return mimeType.toString();
        }

        return null;
    }
}
