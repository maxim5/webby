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
import io.webby.url.validate.ValidationError;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class CallerFactoryTest {
    private static final String URL = "/dummy/CallerFactoryTest";

    private CallerFactory factory;

    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector(new UrlModule());
        factory = injector.getInstance(CallerFactory.class);

        Locale.setDefault(Locale.US);  // any way to remove this?
    }

    @Test
    public void types() {
        Assertions.assertTrue(CallerFactory.isInt(int.class));
        Assertions.assertFalse(CallerFactory.isInt(short.class));
        Assertions.assertFalse(CallerFactory.isInt(long.class));
        Assertions.assertFalse(CallerFactory.isInt(byte.class));
        Assertions.assertFalse(CallerFactory.isInt(char.class));
        Assertions.assertFalse(CallerFactory.isInt(Integer.class));
        Assertions.assertFalse(CallerFactory.isInt(Number.class));

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

        Assertions.assertTrue(CallerFactory.canPassContent(Object.class));
        Assertions.assertTrue(CallerFactory.canPassContent(Map.class));
        Assertions.assertFalse(CallerFactory.canPassContent(int.class));
        Assertions.assertFalse(CallerFactory.canPassContent(String.class));
        Assertions.assertFalse(CallerFactory.canPassContent(CharSequence.class));

        Assertions.assertTrue(CallerFactory.canPassBuffer(CharSequence.class));
        Assertions.assertTrue(CallerFactory.canPassBuffer(CharBuffer.class));
        Assertions.assertFalse(CallerFactory.canPassBuffer(MutableCharBuffer.class));  // routekit contains immutable
        Assertions.assertFalse(CallerFactory.canPassBuffer(String.class));
        Assertions.assertFalse(CallerFactory.canPassBuffer(StringBuilder.class));
        Assertions.assertFalse(CallerFactory.canPassBuffer(Object.class));
    }

    @Test
    public void zero_vars_native() throws Exception {
        IntSupplier instance = () -> 42;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of());

        Assertions.assertEquals(42, caller.call(get(), vars()));
        Assertions.assertEquals(42, caller.call(post(), vars()));
    }

    @Test
    public void zero_vars_native_with_request() throws Exception {
        interface RequestFunction {
            String apply(HttpRequest request);
        }
        RequestFunction instance = (request) -> request.method().toString();
        Caller caller = factory.create(instance, binding(instance, "POST"), asMap(), List.of());

        Assertions.assertEquals("POST", caller.call(post(), vars()));
        Assertions.assertEquals("GET", caller.call(get(), vars()));  // TODO: must fail (type not checked!)
    }

    @Test
    public void one_var_native_int() throws Exception {
        IntFunction<String> instance = String::valueOf;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("i"));

        Assertions.assertEquals("10", caller.call(get(), vars("i", 10)));
        Assertions.assertEquals("10", caller.call(post(), vars("i", 10)));
        Assertions.assertThrows(ValidationError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void one_var_native_int_with_request() throws Exception {
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
    public void one_var_native_string() throws Exception {
        StringFunction<String> instance = String::toUpperCase;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("str"));

        Assertions.assertEquals("FOO", caller.call(get(), vars("str", "foo")));
        Assertions.assertEquals("BAR", caller.call(post(), vars("str", "bar")));
    }

    @Test
    public void zero_vars_generic_injected_deps() throws Exception {
        interface InjectedFunction {
            String apply(Injector injector);
        }
        InjectedFunction instance = (injector) -> "%s".formatted(injector.getClass().getName());
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of());

        Assertions.assertEquals("com.google.inject.internal.InjectorImpl", caller.call(get(), vars()));
    }

    @Test
    public void one_var_generic_injected_deps() throws Exception {
        interface IntInjectedFunction {
            String apply(Injector injector, int i);
        }
        IntInjectedFunction instance = (injector, i) -> "%s:%d".formatted(injector.getClass().getName(), i);
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("i"));

        Assertions.assertEquals("com.google.inject.internal.InjectorImpl:10", caller.call(get(), vars("i", 10)));
    }

    @Test
    public void many_vars_generic_primitive_with_request() throws Exception {
        interface GenericFunction {
            String apply(HttpRequest request, int x, long y, byte z, String s);
        }
        GenericFunction instance = (request, x, y, z, s) -> "%s:%d:%d:%d:%s".formatted(request.method(), x, y, z, s);
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("x", "y", "z", "s"));

        Assertions.assertEquals(
                "GET:1:2:3:foobar",
                caller.call(get(), vars("x", 1, "y", 2, "z", 3, "s", "foobar"))
        );
        Assertions.assertEquals(
                "GET:0:9223372036854775807:127:foo",
                caller.call(get(), vars("x", 0, "y", Long.MAX_VALUE, "z", 127, "s", "foo"))
        );
        Assertions.assertEquals(
                "GET:-10:-9223372036854775808:-128:bar",
                caller.call(get(), vars("x", -10, "y", Long.MIN_VALUE, "z", -128, "s", "bar"))
        );

        Assertions.assertThrows(ValidationError.class, () ->
                caller.call(get(), vars("x", 1, "y", 2, "z", 256, "s", "foo")));
        Assertions.assertThrows(ValidationError.class, () ->
                caller.call(get(), vars("x", 1, "y", 2, "z", 3, "str", "foo")));
    }

    @Test
    public void many_vars_generic_boxed() throws Exception {
        interface GenericFunction {
            String apply(Integer x, Long y, Byte z);
        }
        GenericFunction instance = "%d:%d:%d"::formatted;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("x", "y", "z"));

        Assertions.assertEquals("1:2:3",caller.call(get(), vars("x", 1, "y", 2, "z", 3)));
        Assertions.assertEquals("0:9223372036854775807:127", caller.call(get(), vars("x", 0, "y", Long.MAX_VALUE, "z", 127)));
        Assertions.assertEquals("-10:-9223372036854775808:-128", caller.call(get(), vars("x", -10, "y", Long.MIN_VALUE, "z", -128)));

        Assertions.assertThrows(ValidationError.class, () ->
                caller.call(get(), vars("x", 1, "y", 2, "z", 256)));
        Assertions.assertThrows(ValidationError.class, () ->
                caller.call(get(), vars("x", 1, "y", 2, "w", 3)));
    }

    @Test
    public void many_vars_generic_primitive_and_boxed() throws Exception {
        interface GenericFunction {
            String apply(short x, float y, Double z, boolean b);
        }
        GenericFunction instance = "%d:%.2f:%.2f:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("x", "y", "z", "b"));

        Assertions.assertEquals("1:2.00:3.00:false", caller.call(get(), vars("x", 1, "y", 2.0, "z", 3.0, "b", false)));
        Assertions.assertThrows(ValidationError.class, () -> caller.call(get(), vars("x", 1_000_000, "y", 0, "z", 0, "b", true)));
    }

    @Test
    public void two_vars_generic_primitive_and_boxed_character() throws Exception {
        interface GenericFunction {
            String apply(char ch1, Character ch2);
        }
        GenericFunction instance = "%s:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), asMap(), List.of("x", "y"));

        Assertions.assertEquals("A:B", caller.call(get(), vars("x", 65, "y", 66)));
        Assertions.assertEquals("\u90AB:\u0000", caller.call(get(), vars("x", 0x90AB, "y", 0)));
        // Assertions.assertThrows(ValidationError.class, () -> caller.call(get(), vars("x", -1, "y", -2)));
        Assertions.assertThrows(ValidationError.class, () -> caller.call(get(), vars("x", -1, "y", Long.MAX_VALUE)));
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
