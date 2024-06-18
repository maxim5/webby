package io.spbx.webby.ws.meta;

import io.netty.buffer.ByteBuf;
import io.spbx.webby.netty.marshal.MarshallerFactory.SupportedJsonLibrary;
import io.spbx.webby.netty.ws.errors.BadFrameException;
import io.spbx.webby.testing.Testing;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.webby.testing.AssertJson.withJsonLibrary;
import static io.spbx.util.testing.TestingBytes.asByteBuf;
import static io.spbx.util.testing.TestingBytes.assertBytes;
import static io.spbx.webby.testing.ws.meta.AssertMeta.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("slow")
public class JsonMetadataTest {
    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void parse_strict_json(SupportedJsonLibrary library) {
        Testing.testStartup(withJsonLibrary(library));
        JsonMetadata metadata = new JsonMetadata(Testing.Internals.json(), Testing.Internals.charset());

        ByteBuf input1 = asByteBuf("""
            {"on": "foo", "id": 123, "data": "{'x':'y'}"}
        """);
        metadata.parse(input1, (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("foo");
            assertThat(requestId).isEqualTo(123);
            assertBytes(content).isEqualTo("{'x':'y'}");
        });

        ByteBuf input2 = asByteBuf("""
            {"on": "bar", "id": 123.0, "data": "{'x':'y'}"}
        """);
        metadata.parse(input2, (acceptorId, requestId, content) -> {
            assertBytes(acceptorId).isEqualTo("bar");
            assertThat(requestId).isEqualTo(123);
            assertBytes(content).isEqualTo("{'x':'y'}");
        });
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void parse_valid_json_wrong_format(SupportedJsonLibrary library) {
        Testing.testStartup(withJsonLibrary(library));
        JsonMetadata metadata = new JsonMetadata(Testing.Internals.json(), Testing.Internals.charset());

        assertNotParsed("{}", metadata);
        assertNotParsed("{\"on\": \"bar\"}", metadata);
        assertNotParsed("{\"on\": \"\", \"id\": 123, \"data\": \"\"}", metadata);
        assertNotParsed("{\"on_\": \"foo\", \"id_\": 123, \"data_\": \"\"}", metadata);
        assertNotParsed("{\"on\": \"foo\", \"id\": \"\", \"data\": \"\"}", metadata);
        assertNotParsed("{\"on\": \"foo\", \"id\": 123, \"data\": 123}", metadata);

        assertNotParsedOrThrows(BadFrameException.class, "[]", metadata);
        assertNotParsedOrThrows(BadFrameException.class, "123", metadata);
    }

    @ParameterizedTest
    @EnumSource(SupportedJsonLibrary.class)
    public void parse_invalid_json(SupportedJsonLibrary library) {
        Testing.testStartup(withJsonLibrary(library));
        JsonMetadata metadata = new JsonMetadata(Testing.Internals.json(), Testing.Internals.charset());

        assertThrows(BadFrameException.class, () -> metadata.parse(asByteBuf(""), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> metadata.parse(asByteBuf("foo"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> metadata.parse(asByteBuf("<"), EMPTY_CONSUMER));
        assertThrows(BadFrameException.class, () -> metadata.parse(asByteBuf("!@)*$?<>&(#&(%_#?`~|:"), EMPTY_CONSUMER));

        assertNotParsedOrThrows(BadFrameException.class, "{", metadata);
        assertNotParsedOrThrows(BadFrameException.class, "}", metadata);
    }
}
