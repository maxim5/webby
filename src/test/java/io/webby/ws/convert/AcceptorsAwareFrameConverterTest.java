package io.webby.ws.convert;

import io.webby.netty.ws.errors.ClientDeniedException;
import io.webby.url.annotate.FrameType;
import io.webby.ws.context.ClientFrameType;
import io.webby.ws.convert.AcceptorsAwareFrameConverter.ConcreteFrameType;
import org.junit.jupiter.api.Test;

import static io.webby.ws.convert.AcceptorsAwareFrameConverter.resolveFrameType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AcceptorsAwareFrameConverterTest {
    @Test
    public void resolveFrameType_cases() {
        assertEquals(ConcreteFrameType.TEXT, resolveFrameType(ClientFrameType.TEXT, FrameType.TEXT_ONLY));
        assertEquals(ConcreteFrameType.TEXT, resolveFrameType(ClientFrameType.ANY, FrameType.TEXT_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BINARY, FrameType.TEXT_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BOTH, FrameType.TEXT_ONLY));

        assertEquals(ConcreteFrameType.BINARY, resolveFrameType(ClientFrameType.BINARY, FrameType.BINARY_ONLY));
        assertEquals(ConcreteFrameType.BINARY, resolveFrameType(ClientFrameType.ANY, FrameType.BINARY_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.TEXT, FrameType.BINARY_ONLY));
        assertThrows(ClientDeniedException.class, () -> resolveFrameType(ClientFrameType.BOTH, FrameType.BINARY_ONLY));

        assertEquals(ConcreteFrameType.TEXT, resolveFrameType(ClientFrameType.TEXT, FrameType.FROM_CLIENT));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.ANY, FrameType.FROM_CLIENT));  // temp
        assertEquals(ConcreteFrameType.BINARY, resolveFrameType(ClientFrameType.BINARY, FrameType.FROM_CLIENT));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.BOTH, FrameType.FROM_CLIENT));

        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.TEXT, FrameType.ALLOW_BOTH));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.BINARY, FrameType.ALLOW_BOTH));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.ANY, FrameType.ALLOW_BOTH));
        assertEquals(ConcreteFrameType.BOTH, resolveFrameType(ClientFrameType.BOTH, FrameType.ALLOW_BOTH));
    }
}
