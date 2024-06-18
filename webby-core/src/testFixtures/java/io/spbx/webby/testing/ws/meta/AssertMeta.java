package io.spbx.webby.testing.ws.meta;

import io.spbx.webby.netty.ws.FrameConst;
import io.spbx.webby.ws.meta.FrameMetadata;
import org.jetbrains.annotations.NotNull;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.testing.TestingBytes.asByteBuf;
import static io.spbx.util.testing.TestingBytes.assertBytes;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssertMeta {
    public static final FrameMetadata.MetadataConsumer EMPTY_CONSUMER = (acceptorId, requestId, content) -> {};

    public static void assertNotParsed(@NotNull String input, @NotNull FrameMetadata metadata) {
        metadata.parse(asByteBuf(input), (acceptorId, requestId, content) -> {
            assertThat((Object) acceptorId).isNull();
            assertThat(requestId).isEqualTo(FrameConst.RequestIds.NO_ID);
            assertBytes(content).isEqualTo(input);
        });
    }

    public static <T extends Throwable> void assertNotParsedOrThrows(@NotNull Class<T> expectedType,
                                                                     @NotNull String input,
                                                                     @NotNull FrameMetadata metadata) {
        try {
            assertNotParsed(input, metadata);
        } catch (Exception e) {
            assertThrows(expectedType, () -> {throw e;});
        }
    }
}
