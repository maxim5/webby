package io.webby.ws.meta;

import io.webby.netty.ws.FrameConst;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingBytes.asByteBuf;
import static io.webby.testing.TestingBytes.assertByteBuf;
import static org.junit.jupiter.api.Assertions.*;

public class AssertMeta {
    public static final FrameMetadata.MetadataConsumer EMPTY_CONSUMER = (acceptorId, requestId, content) -> {};

    public static void assertNotParsed(@NotNull String input, @NotNull FrameMetadata metadata) {
        metadata.parse(asByteBuf(input), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(FrameConst.RequestIds.NO_ID, requestId);
            assertByteBuf(content, input);
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
