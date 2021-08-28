package io.webby.examples.hello;

import io.webby.url.annotate.Json;
import io.webby.url.annotate.POST;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @POST(url="/json/sample/{id}")
    public String json_object(int id, @Json Sample content) {
        incoming.set(content);
        return "ok";
    }

    public @Nullable Object getIncoming() {
        return incoming.getAndSet(null);
    }

    public static class Sample {
        private int x;
        private String s;
        private List<Integer> list;

        public Sample() {
        }

        public Sample(int x, String s, List<Integer> list) {
            this.x = x;
            this.s = s;
            this.list = list;
        }

        public int getX() {
            return x;
        }

        public String getS() {
            return s;
        }

        public List<Integer> getList() {
            return list;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setS(String s) {
            this.s = s;
        }

        public void setList(List<Integer> list) {
            this.list = list;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Sample sample && x == sample.x && Objects.equals(s, sample.s) && Objects.equals(list, sample.list);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, s, list);
        }

        @Override
        public String toString() {
            return "Sample{x=%d, s='%s', list=%s}".formatted(x, s, list);
        }
    }
}
