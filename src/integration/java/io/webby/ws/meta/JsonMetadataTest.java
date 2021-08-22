package io.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import io.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import io.webby.netty.ws.errors.BadFrameException;
import io.webby.testing.Testing;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static io.webby.testing.TestingBytes.asByteBuf;
import static io.webby.testing.TestingBytes.assertByteBuf;
import static io.webby.ws.meta.AssertMeta.EMPTY_CONSUMER;
import static io.webby.ws.meta.AssertMeta.assertNotParsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonMetadataTest {
    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void parse_strict_json(SupportedJsonLibrary library) {
        Testing.testStartup(settings -> settings.setProperty("json.library", library.slug));
        JsonMetadata metadata = new JsonMetadata(Testing.Internals.json(), Testing.Internals.charset());

        ByteBuf input = asByteBuf("""
            {"on": "foo", "id": 123, "data": "{'x':'y'}"}
        """);

        metadata.parse(input, (acceptorId, requestId, content) -> {
            assertByteBuf(acceptorId, "foo");
            assertEquals(123, requestId);
            assertByteBuf(content, "{'x':'y'}");
        });
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void parse_valid_json_wrong_format(SupportedJsonLibrary library) {
        Testing.testStartup(settings -> settings.setProperty("json.library", library.slug));
        JsonMetadata metadata = new JsonMetadata(Testing.Internals.json(), Testing.Internals.charset());

        assertNotParsed("{}", metadata);
        assertNotParsed("{\"on\": \"bar\"}", metadata);
        assertNotParsed("{\"on_\": \"foo\", \"id_\": 123, \"data_\": \"\"}", metadata);
        assertNotParsed("{\"on\": \"foo\", \"id\": \"\", \"data\": \"\"}", metadata);
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void parse_invalid_json(SupportedJsonLibrary library) {
        Testing.testStartup(settings -> settings.setProperty("json.library", library.slug));
        JsonMetadata metadata = new JsonMetadata(Testing.Internals.json(), Testing.Internals.charset());
        assertThrows(BadFrameException.class, () -> metadata.parse(asByteBuf(""), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> metadata.parse(asByteBuf("!@)*$?<>&(#&(%_#?`~|:"), EMPTY_CONSUMER));
    }
}
