package io.spbx.webby.netty.marshal;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Message;
import com.google.protobuf.compiler.PluginProtos;
import io.spbx.webby.testing.Testing;
import okio.Buffer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
public class ProtobufMarshallerIntegrationTest {
    private final ProtobufMarshaller marshaller = Testing.testStartup().getInstance(ProtobufMarshaller.class);

    private static final Map<Class<? extends Message>, Message> TEST_MESSAGES = Stream.of(
        PluginProtos.CodeGeneratorRequest.newBuilder()
            .setCompilerVersion(PluginProtos.Version.newBuilder().setMajor(1).setMinor(2))
            .addProtoFile(
                DescriptorProtos.FileDescriptorProto.newBuilder()
                    .addEnumType(DescriptorProtos.EnumDescriptorProto.newBuilder().addReservedName("name"))
                    .setOptions(
                        DescriptorProtos.FileOptions.newBuilder()
                            .setCcGenericServices(true)
                            .setFeatures(
                                DescriptorProtos.FeatureSet.newBuilder()
                                    .setEnumType(DescriptorProtos.FeatureSet.EnumType.OPEN)
                            ).build()
                    ).build()
            ).build(),
        PluginProtos.CodeGeneratorResponse.newBuilder()
            .setError("Error")
            .build()
    ).collect(Collectors.toMap(Message::getClass, message -> message));

    @ParameterizedTest
    @ValueSource(classes = {PluginProtos.CodeGeneratorRequest.class, PluginProtos.CodeGeneratorResponse.class})
    public void proto_roundtrip(Class<Message> klass) throws Exception {
        Message message = TEST_MESSAGES.get(klass);
        assertNotNull(message, "Add the class %s to the test map".formatted(klass));
        assertThat(message.getClass()).isEqualTo(klass);

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
