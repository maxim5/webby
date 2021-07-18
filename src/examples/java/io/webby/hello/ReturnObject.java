package io.webby.hello;

import io.webby.url.GET;
import io.webby.url.Json;
import io.webby.url.Serve;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Serve
public class ReturnObject {
    @GET(url="/json/{*var}")
    @Json
    public Object json_map(String var) {
        Map<String, Serializable> map = new LinkedHashMap<>();
        map.put("foo", 1);
        map.put("var", var.split("/"));
        return map;
    }
}
