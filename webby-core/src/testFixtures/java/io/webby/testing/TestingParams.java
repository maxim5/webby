package io.webby.testing;

import com.google.common.primitives.Bytes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static io.spbx.util.testing.TestingBytes.CHARSET;
import static java.util.Objects.requireNonNull;

public class TestingParams {
    public static byte @Nullable [] paramToBytes(@NotNull String encoded) {
        return encoded.equals("null") ? null :
                encoded.isEmpty() || encoded.equals("[]") ? new byte[0] :
                Bytes.toArray(Arrays.stream(encoded.split(",")).map(String::trim).map(Byte::parseByte).toList());
    }

    public static @Nullable String paramToString(@NotNull String encoded) {
        return encoded.equals("null") ? null :
                encoded.matches("(\\d+,?\\s*)+") ?
                        new String(requireNonNull(paramToBytes(encoded)), CHARSET) : encoded;
    }
}
