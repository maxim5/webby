package io.webby.examples.hello;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.CollectionAnalyzer;
import com.dslplatform.json.runtime.Settings;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonCustom {
    public static void customize(@NotNull Binder binder) {
        DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());
        dslJson.registerReader(Object.class, dslJson.tryFindReader(Map.class));  /* ObjectConverter.MapReader */
        dslJson.registerReader(List.class, CollectionAnalyzer.Runtime.JSON_READER);
        dslJson.registerReader(ArrayList.class, CollectionAnalyzer.Runtime.JSON_READER);
        binder.bind(new TypeLiteral<DslJson<Object>>() {}).toInstance(dslJson);
    }

    // See also
    // https://github.com/ngs-doo/dsl-json/blob/master/examples/AutoValue/src/main/java/com/dslplatform/autovalue/Example.java
    public static void main(String[] args) throws IOException {
        DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());

        SampleBean sample = new SampleBean(1, "foo", List.of(1, 2, 3));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        dslJson.serialize(sample, os);
        System.out.println(os);

        byte[] bytes = "{\"x\": 1, \"s\": \"foo\", \"list\": [1, 2, 3]}".getBytes();
        SampleBean deserialize = dslJson.deserialize(SampleBean.class, bytes, bytes.length);
        System.out.println(deserialize);
    }
}
