package io.webby.netty.marshal;

import com.google.api.LabelDescriptor;
import com.google.api.Usage;
import com.google.api.UsageRule;
import com.google.protobuf.Message;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.webby.testing.Testing;
import okio.Buffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ProtobufMarshallerIntegrationTest {
    private final ProtobufMarshaller marshaller = Testing.testStartupNoHandlers().getInstance(ProtobufMarshaller.class);

    private static final Map<Class<? extends Message>, Message> TEST_MESSAGES = Stream.of(
        Status.newBuilder()
                .setCode(Code.OK.getNumber())
                .addAllDetails(List.of())
                .build(),
        LabelDescriptor.newBuilder()
                .setKey("key")
                .setValueType(LabelDescriptor.ValueType.STRING)
                .setDescription("Description")
                .build(),
        Usage.newBuilder()
                .addAllRequirements(List.of("foo/bar"))
                .addAllRules(List.of(UsageRule.newBuilder().setSelector("*").setSkipServiceControl(true).build()))
                .build()
    ).collect(Collectors.toMap(Message::getClass, message -> message));

    @ParameterizedTest
    @ValueSource(classes = {Status.class, LabelDescriptor.class, Usage.class})
    public void proto_roundtrip(Class<Message> klass) throws Exception {
        Message message = TEST_MESSAGES.get(klass);
        assertNotNull(message, "Add the class %s to the test map".formatted(klass));
        assertEquals(klass, message.getClass());

        assertProtoStreamRoundTrip(message, klass);
        assertProtoBytesRoundTrip(message, klass);
        assertProtoCharsRoundTrip(message, klass);
        assertProtoStringRoundTrip(message, klass);
    }

    private void assertProtoStreamRoundTrip(Message message, Class<? extends Message> klass) throws IOException {
        Buffer buffer = new Buffer();
        marshaller.writeBytes(buffer.outputStream(), message);
        Message result = marshaller.readBytes(buffer.inputStream(), klass);
        assertThat(result).isEqualTo(message);
    }

    private void assertProtoBytesRoundTrip(Message message, Class<? extends Message> klass) {
        byte[] bytes = marshaller.writeBytes(message);
        Message result = marshaller.readBytes(bytes, klass);
        assertThat(result).isEqualTo(message);
    }

    private void assertProtoCharsRoundTrip(Message message, Class<? extends Message> klass) throws IOException {
        String text;
        try (StringWriter writer = new StringWriter()) {
            marshaller.writeChars(writer, message);
            text = writer.toString();
        }
        try (StringReader reader = new StringReader(text)) {
            Message result = marshaller.readChars(reader, klass);
            assertThat(result).isEqualTo(message);
        }
    }

    private void assertProtoStringRoundTrip(Message message, Class<? extends Message> klass) {
        String text = marshaller.writeString(message);
        Message result = marshaller.readString(text, klass);
        assertThat(result).isEqualTo(message);
    }
}
