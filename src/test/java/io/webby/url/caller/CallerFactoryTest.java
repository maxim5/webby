package io.webby.url.caller;

import com.google.common.collect.Streams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.routekit.util.CharBuffer;
import io.routekit.util.MutableCharBuffer;
import io.webby.url.UrlModule;
import io.webby.url.impl.Binding;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class CallerFactoryTest {
    private static final String URL = "/";

    private CallerFactory factory;

    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector(new UrlModule());
        factory = injector.getInstance(CallerFactory.class);
    }

    @Test
    public void types() {
        Assertions.assertTrue(CallerFactory.isInt(int.class));
        Assertions.assertFalse(CallerFactory.isInt(short.class));
        Assertions.assertFalse(CallerFactory.isInt(long.class));
        Assertions.assertFalse(CallerFactory.isInt(byte.class));
        Assertions.assertFalse(CallerFactory.isInt(char.class));
        Assertions.assertFalse(CallerFactory.isInt(Integer.class));

        Assertions.assertTrue(CallerFactory.isStringLike(String.class));
        Assertions.assertTrue(CallerFactory.isStringLike(CharSequence.class));
        Assertions.assertTrue(CallerFactory.isStringLike(CharBuffer.class));
        Assertions.assertTrue(CallerFactory.isStringLike(MutableCharBuffer.class));
        Assertions.assertTrue(CallerFactory.isStringLike(AsciiString.class));
        Assertions.assertTrue(CallerFactory.isStringLike(StringBuilder.class));
        Assertions.assertFalse(CallerFactory.isStringLike(Object.class));

        Assertions.assertTrue(CallerFactory.canPassHttpRequest(HttpRequest.class));
        Assertions.assertTrue(CallerFactory.canPassHttpRequest(FullHttpRequest.class));
        Assertions.assertTrue(CallerFactory.canPassHttpRequest(DefaultHttpRequest.class));
        Assertions.assertTrue(CallerFactory.canPassHttpRequest(DefaultFullHttpRequest.class));
        Assertions.assertFalse(CallerFactory.canPassHttpRequest(Object.class));

        Assertions.assertTrue(CallerFactory.canPassBuffer(CharSequence.class));
        Assertions.assertTrue(CallerFactory.canPassBuffer(CharBuffer.class));
        Assertions.assertFalse(CallerFactory.canPassBuffer(MutableCharBuffer.class));  // routekit contains immutable
        Assertions.assertFalse(CallerFactory.canPassBuffer(String.class));
        Assertions.assertFalse(CallerFactory.canPassBuffer(StringBuilder.class));
        Assertions.assertFalse(CallerFactory.canPassBuffer(Object.class));
    }

    @Test
    public void zero_vars() throws Exception {
        IntSupplier instance = () -> 42;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of());

        Assertions.assertEquals(42, caller.call(get(), vars()));
        Assertions.assertEquals(42, caller.call(post(), vars()));
    }

    @Test
    public void zero_vars_with_request() throws Exception {
        interface RequestFunction {
            String apply(HttpRequest request);
        }
        RequestFunction instance = (request) -> request.method().toString();
        Caller caller = factory.create(instance, binding(instance, "POST"), asMap(), List.of());

        Assertions.assertEquals("POST", caller.call(post(), vars()));
        Assertions.assertEquals("GET", caller.call(get(), vars()));  // TODO: must fail (type not checked!)
    }

    @Test
    public void one_var_primitive_int() throws Exception {
        IntFunction<String> instance = String::valueOf;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("i"));

        Assertions.assertEquals("10", caller.call(get(), vars("i", 10)));
        Assertions.assertEquals("10", caller.call(post(), vars("i", 10)));
        Assertions.assertThrows(ValidationError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void one_var_primitive_int_with_request() throws Exception {
        interface IntRequestFunction {
            String apply(HttpRequest request, int i);
        }
        IntRequestFunction instance = (request, i) -> "%s:%s".formatted(request.method(), i);
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("i"));

        Assertions.assertEquals("GET:10", caller.call(get(), vars("i", 10)));
        Assertions.assertEquals("GET:0", caller.call(get(), vars("i", 0)));
        Assertions.assertEquals("POST:10", caller.call(post(), vars("i", 10)));
        Assertions.assertThrows(ValidationError.class, () -> caller.call(get(), vars("i", "foo")));
        Assertions.assertThrows(ValidationError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void one_var_primitive_string() throws Exception {
        StringFunction<String> instance = String::toUpperCase;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("str"));

        Assertions.assertEquals("FOO", caller.call(get(), vars("str", "foo")));
        Assertions.assertEquals("BAR", caller.call(post(), vars("str", "bar")));
    }

    @NotNull
    private static Binding binding(Object instance) {
        return binding(instance, null);
    }

    @NotNull
    private static Binding binding(Object instance, String type) {
        return new Binding(URL, getDeclaredMethod(instance), type, new EndpointOptions(null, null, null));
    }

    @NotNull
    private static Method getDeclaredMethod(Object instance) {
        Method[] methods = instance.getClass().getDeclaredMethods();
        Assertions.assertEquals(1, methods.length);
        return methods[0];
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Map<String, CharBuffer> vars(Object ... items) {
        List<CharSequence> list = Streams.mapWithIndex(
                Arrays.stream(items),
                (item, i) -> (i % 2 == 0) ? item.toString() : new CharBuffer(item.toString())
        ).collect(Collectors.toList());
        return asMap(list);
    }

    private static <K, V> Map<K, V> asMap(Object ... items) {
        return asMap(List.of(items));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> asMap(List<?> items) {
        Assertions.assertEquals(0, items.size() % 2);
        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i += 2) {
            result.put((K) items.get(i), (V) items.get(i + 1));
        }
        return result;
    }

    @NotNull
    private static DefaultFullHttpRequest get() {
        return request(HttpMethod.GET);
    }

    @NotNull
    private static DefaultFullHttpRequest post() {
        return request(HttpMethod.POST);
    }

    @NotNull
    private static DefaultFullHttpRequest request(HttpMethod method) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, URL);
    }

    private interface StringFunction<T> {
        T apply(String s);
    }
}
