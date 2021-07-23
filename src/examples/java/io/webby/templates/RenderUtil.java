package io.webby.templates;

import io.webby.util.ThrowConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

public class RenderUtil {
    @NotNull
    public static <E extends Throwable> String writeToString(ThrowConsumer<Writer, E> consumer) throws E {
        Writer writer = new StringWriter();
        consumer.accept(writer);
        return writer.toString();
    }

    public static <E extends Throwable> byte[] writeToBytes(ThrowConsumer<Writer, E> consumer) throws E {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(output);
        consumer.accept(writer);
        return output.toByteArray();
    }
}
