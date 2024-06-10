package io.webby.url.caller;

import com.google.common.collect.Streams;
import com.google.inject.Injector;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;
import io.webby.util.base.CharArray;
import io.webby.util.base.MutableCharArray;
import io.webby.netty.request.HttpRequestEx;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Testing;
import io.webby.url.HandlerConfigError;
import io.webby.url.convert.Constraint;
import io.webby.url.convert.ConversionError;
import io.webby.url.convert.StringConstraint;
import io.webby.url.handle.Handler;
import io.webby.url.handle.IntHandler;
import io.webby.url.handle.StringHandler;
import io.webby.url.impl.Binding;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongFunction;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.util.collect.EasyMaps.asMap;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unused")
public class CallerFactoryTest {
    private static final String URL = "/dummy/CallerFactoryTest";
    private final CallerFactory factory = Testing.testStartup().getInstance(CallerFactory.class);

    @Test
    public void types() {
        assertThat(CallerFactory.isInt(int.class)).isTrue();
        assertThat(CallerFactory.isInt(short.class)).isFalse();
        assertThat(CallerFactory.isInt(long.class)).isFalse();
        assertThat(CallerFactory.isInt(byte.class)).isFalse();
        assertThat(CallerFactory.isInt(char.class)).isFalse();
        assertThat(CallerFactory.isInt(Integer.class)).isFalse();
        assertThat(CallerFactory.isInt(Number.class)).isFalse();

        assertThat(CallerFactory.isStringLike(String.class)).isTrue();
        assertThat(CallerFactory.isStringLike(CharSequence.class)).isTrue();
        assertThat(CallerFactory.isStringLike(CharArray.class)).isTrue();
        assertThat(CallerFactory.isStringLike(MutableCharArray.class)).isTrue();
        assertThat(CallerFactory.isStringLike(AsciiString.class)).isTrue();
        assertThat(CallerFactory.isStringLike(StringBuilder.class)).isTrue();
        assertThat(CallerFactory.isStringLike(Object.class)).isFalse();

        assertThat(CallerFactory.canPassHttpRequest(HttpRequest.class)).isTrue();
        assertThat(CallerFactory.canPassHttpRequest(FullHttpRequest.class)).isTrue();
        assertThat(CallerFactory.canPassHttpRequest(DefaultHttpRequest.class)).isTrue();
        assertThat(CallerFactory.canPassHttpRequest(DefaultFullHttpRequest.class)).isTrue();
        assertThat(CallerFactory.canPassHttpRequest(HttpRequestEx.class)).isTrue();
        assertThat(CallerFactory.canPassHttpRequest(Object.class)).isFalse();

        assertThat(CallerFactory.canPassContent(Object.class)).isTrue();
        assertThat(CallerFactory.canPassContent(Map.class)).isTrue();
        assertThat(CallerFactory.canPassContent(int.class)).isFalse();
        assertThat(CallerFactory.canPassContent(String.class)).isFalse();
        assertThat(CallerFactory.canPassContent(CharSequence.class)).isFalse();
    }

    @Test
    public void native_handler() throws Exception {
        Handler<String> instance = (request, variables) -> "%s:%s".formatted(request.method(), variables.keySet());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("foo", "bar"));

        assertThat(caller instanceof NativeCaller).isTrue();
        assertThat(caller.call(get(), vars("foo", 1))).isEqualTo("GET:[foo]");
        assertThat(caller.call(post(), vars("bar", "x"))).isEqualTo("POST:[bar]");
        assertThat(caller.call(get(), vars("foo", 1, "bar", 2))).isEqualTo("GET:[foo, bar]");
    }

    @Test
    public void native_int_handler() throws Exception {
        IntHandler<String> instance = (request, val) -> "%s:%d".formatted(request.method(), val);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("foo"));

        assertThat(caller instanceof NativeIntCaller).isTrue();
        assertThat(caller.call(get(), vars("foo", 1))).isEqualTo("GET:1");
        assertThat(caller.call(post(), vars("foo", -1))).isEqualTo("POST:-1");
        assertThrows(ConversionError.class, () -> caller.call(post(), vars("bar", 1)));
        assertThrows(ConversionError.class, () -> caller.call(post(), vars("foo", "x")));
    }

    @Test
    public void native_string_handler() throws Exception {
        StringHandler<String> instance = (request, val) -> "%s:%s".formatted(request.method(), val);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("foo"));

        assertThat(caller instanceof NativeStringCaller).isTrue();
        assertThat(caller.call(get(), vars("foo", "1"))).isEqualTo("GET:1");
        assertThat(caller.call(post(), vars("foo", "bar"))).isEqualTo("POST:bar");
        assertThrows(ConversionError.class, () -> caller.call(post(), vars("bar", "foo")));
    }

    @Test
    public void optimized_zero_vars() throws Exception {
        IntSupplier instance = () -> 42;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of());

        assertThat(caller.call(get(), vars())).isEqualTo(42);
        assertThat(caller.call(post(), vars())).isEqualTo(42);
    }

    @Test
    public void optimized_zero_vars_with_request() throws Exception {
        interface RequestFunction {
            String apply(HttpRequest request);
        }
        RequestFunction instance = (request) -> request.method().toString();
        Caller caller = factory.create(instance, binding(instance, "POST"), Map.of(), List.of());

        assertThat(caller.call(post(), vars())).isEqualTo("POST");
        // Note: does not fail: RouteEndpoint is responsible for cutting off wrong requests.
        assertThat(caller.call(get(), vars())).isEqualTo("GET");
    }

    @Test
    public void optimized_one_var_int() throws Exception {
        IntFunction<String> instance = String::valueOf;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertThat(caller.call(get(), vars("i", 10))).isEqualTo("10");
        assertThat(caller.call(post(), vars("i", 10))).isEqualTo("10");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void optimized_one_var_int_with_request() throws Exception {
        interface IntRequestFunction {
            String apply(HttpRequest request, int i);
        }
        IntRequestFunction instance = (request, i) -> "%s:%s".formatted(request.method(), i);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertThat(caller.call(get(), vars("i", 10))).isEqualTo("GET:10");
        assertThat(caller.call(get(), vars("i", 0))).isEqualTo("GET:0");
        assertThat(caller.call(post(), vars("i", 10))).isEqualTo("POST:10");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void optimized_one_var_string() throws Exception {
        StringFunction instance = String::toUpperCase;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("str"));

        assertThat(caller.call(get(), vars("str", "foo"))).isEqualTo("FOO");
        assertThat(caller.call(post(), vars("str", "bar"))).isEqualTo("BAR");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
    }

    @Test
    public void optimized_one_var_char_array() throws Exception {
        interface CharArrayFunction {
            int apply(CharArray s);
        }
        CharArrayFunction instance = CharArray::length;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("str"));

        assertThat(caller.call(get(), vars("str", "foo"))).isEqualTo(3);
        assertThat(caller.call(post(), vars("str", "bar"))).isEqualTo(3);
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
    }

    @Test
    public void optimized_two_vars_int_int() throws Exception {
        interface BiIntFunction {
            String apply(int x, int y);
        }
        BiIntFunction instance = "%d:%d"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i", "j"));

        assertThat(caller.call(get(), vars("i", 1, "j", 0))).isEqualTo("1:0");
        assertThat(caller.call(post(), vars("i", -1, "j", -1))).isEqualTo("-1:-1");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("j", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", 10, "j", "foo")));
    }

    @Test
    public void optimized_two_vars_string_string() throws Exception {
        interface BiStrFunction {
            String apply(String a, String b);
        }
        BiStrFunction instance = "%s:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("a", "b"));

        assertThat(caller.call(get(), vars("a", "foo", "b", "bar"))).isEqualTo("foo:bar");
        assertThat(caller.call(get(), vars("a", "foo", "b", ""))).isEqualTo("foo:");
        assertThat(caller.call(post(), vars("a", "", "b", "bar"))).isEqualTo(":bar");
        assertThat(caller.call(post(), vars("a", "", "b", ""))).isEqualTo(":");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("a", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("b", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
    }

    @Test
    public void optimized_two_vars_int_string() throws Exception {
        interface IntStrFunction {
            String apply(int a, String b);
        }
        IntStrFunction instance = "%d:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("a", "b"));

        assertThat(caller.call(get(), vars("a", 1, "b", "bar"))).isEqualTo("1:bar");
        assertThat(caller.call(get(), vars("a", 0, "b", ""))).isEqualTo("0:");
        assertThat(caller.call(post(), vars("a", 0, "b", "bar"))).isEqualTo("0:bar");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("a", "foo", "b", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("a", 10)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("b", "bar")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
    }

    @Test
    public void optimized_unrecognized_one_var_object() {
        Function<Object, String> instance = Object::toString;
        assertThrows(HandlerConfigError.class, () -> factory.create(instance, binding(instance), Map.of(), List.of("x")));
    }

    @Test
    public void generic_zero_vars_injected_deps() throws Exception {
        interface InjectedFunction {
            String apply(Injector injector);
        }
        InjectedFunction instance = (injector) -> "%s".formatted(injector.getClass().getName());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of());

        assertThat(caller.call(get(), vars())).isEqualTo("com.google.inject.internal.InjectorImpl");
    }

    @Test
    public void generic_zero_vars_injected_deps_with_request() throws Exception {
        interface InjectedFunction {
            String apply(Injector injector, HttpRequest request);
        }
        InjectedFunction instance = (injector, request) -> "%s:%s".formatted(injector.getClass().getName(), request.method());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of());

        assertThat(caller.call(get(), vars())).isEqualTo("com.google.inject.internal.InjectorImpl:GET");
    }

    @Test
    public void generic_one_var_long() throws Exception {
        LongFunction<String> instance = String::valueOf;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertThat(caller.call(get(), vars("i", Long.MAX_VALUE))).isEqualTo("9223372036854775807");
        assertThat(caller.call(post(), vars("i", Long.MIN_VALUE))).isEqualTo("-9223372036854775808");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars()));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("i", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 10)));
    }

    @Test
    public void generic_one_var_injected_deps() throws Exception {
        interface IntInjectedFunction {
            String apply(Injector injector, int i);
        }
        IntInjectedFunction instance = (injector, i) -> "%s:%d".formatted(injector.getClass().getName(), i);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertThat(caller.call(get(), vars("i", 10))).isEqualTo("com.google.inject.internal.InjectorImpl:10");
    }

    @Test
    public void generic_one_var_injected_deps_swapped() throws Exception {
        interface IntInjectedFunction {
            String apply(int i, Injector injector);
        }
        IntInjectedFunction instance = (i, injector) -> "%d:%s".formatted(i, injector.getClass().getName());
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("i"));

        assertThat(caller.call(get(), vars("i", 10))).isEqualTo("10:com.google.inject.internal.InjectorImpl");
    }

    @Test
    public void generic_many_vars_primitive_with_request() throws Exception {
        interface GenericFunction {
            String apply(HttpRequest request, int x, long y, byte z, String s);
        }
        GenericFunction instance = (request, x, y, z, s) -> "%s:%d:%d:%d:%s".formatted(request.method(), x, y, z, s);
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z", "s"));

        assertThat(caller.call(get(), vars("x", 1, "y", 2, "z", 3, "s", "foobar"))).isEqualTo("GET:1:2:3:foobar");
        assertThat(caller.call(get(), vars("x", 0, "y", Long.MAX_VALUE, "z", 127, "s", "foo")))
            .isEqualTo("GET:0:9223372036854775807:127:foo");
        assertThat(caller.call(get(), vars("x", -10, "y", Long.MIN_VALUE, "z", -128, "s", "bar")))
            .isEqualTo("GET:-10:-9223372036854775808:-128:bar");

        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1, "y", 2, "z", 256, "s", "foo")));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1, "y", 2, "z", 3, "str", "foo")));
    }

    @Test
    public void generic_many_vars_boxed() throws Exception {
        interface GenericFunction {
            String apply(Integer x, Long y, Byte z);
        }
        GenericFunction instance = "%d:%d:%d"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z"));

        assertThat(caller.call(get(), vars("x", 1, "y", 2, "z", 3))).isEqualTo("1:2:3");
        assertThat(caller.call(get(), vars("x", 0, "y", Long.MAX_VALUE, "z", 127)))
            .isEqualTo("0:9223372036854775807:127");
        assertThat(caller.call(get(), vars("x", -10, "y", Long.MIN_VALUE, "z", -128)))
            .isEqualTo("-10:-9223372036854775808:-128");

        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1, "y", 2, "z", 256)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1, "y", 2, "w", 3)));
    }

    @Test
    public void generic_many_vars_primitive_and_boxed() throws Exception {
        interface GenericFunction {
            String apply(short x, float y, Double z, boolean b);
        }
        GenericFunction instance = "%d:%.2f:%.2f:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y", "z", "b"));

        assertThat(caller.call(get(), vars("x", 1, "y", 2.0, "z", 3.0, "b", false))).isEqualTo("1:2.00:3.00:false");
        assertThat(caller.call(get(), vars("x", 1, "y", 2.0, "z", 3.0, "b", true))).isEqualTo("1:2.00:3.00:true");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", 1_000_000, "y", 0, "z", 0, "b", true)));
    }

    @Test
    public void generic_two_vars_primitive_and_boxed_character() throws Exception {
        interface GenericFunction {
            String apply(char ch1, Character ch2);
        }
        GenericFunction instance = "%s:%s"::formatted;
        Caller caller = factory.create(instance, binding(instance), Map.of(), List.of("x", "y"));

        assertThat(caller.call(get(), vars("x", 65, "y", 66))).isEqualTo("A:B");
        assertThat(caller.call(get(), vars("x", 0x90AB, "y", 0))).isEqualTo("\u90AB:\u0000");
        // assertThrows(ValidationError.class, () -> caller.call(get(), vars("x", -1, "y", -2)));
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("x", -1, "y", Long.MAX_VALUE)));
    }

    @Test
    public void optimized_one_var_string_converter() throws Exception {
        StringConstraint constraint = new StringConstraint(5);
        StringFunction instance = String::toUpperCase;
        Caller caller = factory.create(instance, binding(instance), Map.of("s", constraint), List.of("s"));

        assertThat(caller.call(get(), vars("s", "foo"))).isEqualTo("FOO");
        assertThat(caller.call(post(), vars("s", "bar"))).isEqualTo("BAR");
        assertThrows(ConversionError.class, () -> caller.call(get(), vars("s", "foobar")));
    }

    @Test
    public void optimized_one_var_char_sequence_converter() throws Exception {
        Constraint<String> constraint = s -> Objects.toString(s).toUpperCase();
        StringFunction instance = s -> s;
        Caller caller = factory.create(instance, binding(instance), Map.of("s", constraint), List.of("s"));

        assertThat(caller.call(get(), vars("s", "foo"))).isEqualTo("FOO");
        assertThat(caller.call(post(), vars("s", "bar"))).isEqualTo("BAR");
        assertThat(caller.call(post(), vars("s", "FooBar"))).isEqualTo("FOOBAR");
    }

    private static @NotNull Binding binding(Object instance) {
        return binding(instance, null);
    }

    private static @NotNull Binding binding(Object instance, String type) {
        return new Binding(URL, getSingleDeclaredMethod(instance), type, EndpointOptions.DEFAULT);
    }

    private static @NotNull Method getSingleDeclaredMethod(Object instance) {
        Method[] methods = instance.getClass().getDeclaredMethods();
        assertThat(methods.length).isEqualTo(1);
        return methods[0];
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Map<String, CharArray> vars(Object @NotNull ... items) {
        List<CharSequence> list = Streams.mapWithIndex(
            Arrays.stream(items),
            (item, i) -> (i % 2 == 0) ? item.toString() : new CharArray(item.toString())
        ).toList();
        return asMap(list);
    }

    public static @NotNull HttpRequestEx get() {
        // Do not care about QueryParams for now
        return HttpRequestBuilder.get(URL).ex();
    }

    public static @NotNull HttpRequestEx post() {
        // Do not care about QueryParams for now
        return HttpRequestBuilder.post(URL).ex();
    }

    private interface StringFunction {
        String apply(String s);
    }
}
