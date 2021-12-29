package io.webby.examples.hello;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.webby.url.annotate.GET;
import io.webby.url.annotate.Json;

import java.util.List;

public class ReturnJson {
    @GET(url = "/r/json/map/{*var}")
    public @Json Object json_map(String var) {
        return ImmutableMap.of(
            "foo", 1,
            "var", var.split("/")
        );
    }

    @GET(url = "/r/json/list/{*var}")
    public @Json List<?> json_list(String var) {
        return List.of("foo", 1, var.split("/"));
    }

    @GET(url = "/r/json/sample_bean/{*var}")
    public @Json SampleBean json_sample_bean(String var) {
        return new SampleBean(0, var, List.of(1, 2, 3));
    }

    @GET(url = "/r/json/gson/element/{var}")
    public @Json JsonElement json_gson_element(String var) {
        List<Character> characters = var.chars().boxed().map(i -> (char) i.intValue()).toList();
        return new Gson().toJsonTree(characters);
    }
}
