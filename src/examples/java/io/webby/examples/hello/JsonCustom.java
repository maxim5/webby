package io.webby.examples.hello;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.CollectionAnalyzer;
import com.dslplatform.json.runtime.Settings;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.squareup.moshi.Moshi;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonCustom {
    public static void customize(@NotNull Binder binder) {
        DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());
        dslJson.registerReader(Object.class, dslJson.tryFindReader(Map.class));  /* ObjectConverter.MapReader */
        dslJson.registerReader(ConcurrentHashMap.class, dslJson.tryFindReader(Map.class));  /* ObjectConverter.MapReader */
        dslJson.registerReader(Set.class, CollectionAnalyzer.Runtime.JSON_READER);
        dslJson.registerReader(HashSet.class, CollectionAnalyzer.Runtime.JSON_READER);
        dslJson.registerReader(List.class, CollectionAnalyzer.Runtime.JSON_READER);
        dslJson.registerReader(ArrayList.class, CollectionAnalyzer.Runtime.JSON_READER);
        dslJson.registerReader(LinkedList.class, CollectionAnalyzer.Runtime.JSON_READER);
        dslJson.registerReader(ArrayDeque.class, CollectionAnalyzer.Runtime.JSON_READER);
        binder.bind(new TypeLiteral<DslJson<Object>>() {}).toInstance(dslJson);

        binder.bind(Moshi.class).toInstance(new Moshi.Builder().add((type, annotations, moshi) -> {
            if (type instanceof Class<?> klass) {
                if (List.class.isAssignableFrom(klass) && klass != List.class) {
                    return moshi.adapter(List.class);
                }
                if (Collection.class.isAssignableFrom(klass) && klass != Collection.class) {
                    return moshi.adapter(Collection.class);
                }
                if (Map.class.isAssignableFrom(klass) && klass != Map.class) {
                    return moshi.adapter(Map.class);
                }
            }
            return null;
        }).build());
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
