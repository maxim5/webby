package io.webby.ws.meta;

import io.webby.netty.ws.Constants;
import org.jetbrains.annotations.NotNull;

import static io.webby.testing.TestingBytes.asByteBuf;
import static io.webby.testing.TestingBytes.assertByteBuf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AssertMeta {
    public static final FrameMetadata.MetadataConsumer EMPTY_CONSUMER = (acceptorId, requestId, content) -> {};

    public static void assertNotParsed(@NotNull String input, @NotNull FrameMetadata metadata) {
        metadata.parse(asByteBuf(input), (acceptorId, requestId, content) -> {
            assertNull(acceptorId);
            assertEquals(Constants.RequestIds.NO_ID, requestId);
            assertByteBuf(content, input);
        });
    }
}
