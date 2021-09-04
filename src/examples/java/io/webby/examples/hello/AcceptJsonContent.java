package io.webby.examples.hello;

import io.webby.url.annotate.Json;
import io.webby.url.annotate.POST;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AcceptJsonContent {
    private final AtomicReference<Object> incoming = new AtomicReference<>(null);

    @POST(url="/json/map/{id}")
    public String json_map(int id, @Json Map<?, ?> content) {
        incoming.set(content);
        return "ok";
    }

    @POST(url="/json/list/{id}")
    public String json_list(int id, @Json List<?> content) {
        incoming.set(content);
        return "ok";
    }

    @POST(url="/json/obj/{id}")
    public String json_object(int id, @Json Object content) {
        incoming.set(content);
        return "ok";
    }

    @POST(url="/json/sample_bean/{id}")
    public String json_sample_bean(int id, @Json SampleBean content) {
        incoming.set(content);
        return "ok";
    }

    public @Nullable Object getIncoming() {
        return incoming.getAndSet(null);
    }
}
