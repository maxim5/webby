package io.spbx.webby.demo.hello;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.squareup.moshi.Moshi;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonCustom {
    public static void customize(@NotNull Binder binder) {
        // nothing to do
    }

    public static void main(String[] args) throws IOException {
        checkDslJson();
        checkMoshi();
    }

    // See also
    // https://github.com/ngs-doo/dsl-json/blob/master/examples/AutoValue/src/main/java/com/dslplatform/autovalue/Example.java
    private static void checkDslJson() throws IOException {
        DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());

        SampleBean sample = new SampleBean(1, "foo", List.of(1, 2, 3));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        dslJson.serialize(sample, os);
        System.out.println(os);

        byte[] bytes = "{\"x\": 1, \"s\": \"foo\", \"list\": [1, 2, 3]}".getBytes();
        SampleBean deserialize = dslJson.deserialize(SampleBean.class, bytes, bytes.length);
        System.out.println(deserialize);
    }

    private static void checkMoshi() throws IOException {
        Moshi moshi = new Moshi.Builder().add((type, annotations, moshi1) -> {
            if (type == ArrayList.class) {
                return moshi1.adapter(List.class);
            }
            return null;
        }).build();
        ArrayList<?> arrayList = moshi.adapter(ArrayList.class).fromJson("[1, 2]");
        System.out.println(arrayList);
        System.out.println(moshi.adapter(ArrayList.class).toJson(Lists.newArrayList(1, 2, 3)));
        System.out.println(moshi.adapter(Map.class).toJson(Map.of()));
    }
}
