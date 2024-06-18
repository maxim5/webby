package io.webby.netty.marshal;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.runtime.CollectionAnalyzer;
import com.dslplatform.json.runtime.Settings;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import io.webby.common.InjectorHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.spbx.util.base.Unchecked.rethrow;
import static java.util.Objects.requireNonNull;

public record DslJsonMarshaller(@NotNull DslJson<Object> json, @NotNull Charset charset) implements Json, Marshaller {
    @Inject
    public DslJsonMarshaller(@NotNull InjectorHelper helper, @NotNull Charset charset) {
        this(helper.getOrDefault(new TypeLiteral<DslJson<Object>>() {}, DslJsonMarshaller::defaultDslJson), charset);
    }

    @Override
    public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
        return charset == this.charset ? this : new DslJsonMarshaller(json, charset);
    }

    @Override
    public void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException {
        json.serialize(instance, output);
    }

    @Override
    public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
        return requireNonNull(json.deserialize(klass, input));
    }

    @Override
    public <T> @NotNull T readBytes(byte @NotNull [] bytes, @NotNull Class<T> klass) {
        try {
            return requireNonNull(json.deserialize(klass, bytes, bytes.length));
        } catch (IOException e) {
            return rethrow(e);
        }
    }

    private static final Class<?>[] MAPS = {
            Object.class, ConcurrentHashMap.class,
    };
    private static final Class<?>[] COLLECTIONS = {
            Set.class, HashSet.class, List.class, ArrayList.class, LinkedList.class, ArrayDeque.class,
    };

    @SuppressWarnings("rawtypes")
    private static @NotNull DslJson<Object> defaultDslJson() {
        DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());
        JsonReader.ReadObject<Map> mapReadObject = dslJson.tryFindReader(Map.class);  /* ObjectConverter.MapReader */
        for (Class<?> klass : MAPS) {
            dslJson.registerReader(klass, mapReadObject);
        }
        for (Class<?> klass : COLLECTIONS) {
            dslJson.registerReader(klass, CollectionAnalyzer.Runtime.JSON_READER);
        }
        return dslJson;
    }
}
