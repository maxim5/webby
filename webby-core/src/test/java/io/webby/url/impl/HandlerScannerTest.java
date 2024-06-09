package io.webby.url.impl;

import io.webby.url.annotate.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.url.impl.HandlerScanner.*;

public class HandlerScannerTest {
    @Test
    public void matchesAny_class_one_annotation() {
        @Json
        interface Foo {}

        assertThat(matchesAny(Foo.class, Set.of(Json.class))).isTrue();
        assertThat(matchesAny(Foo.class, Set.of(Serve.class))).isFalse();
        assertThat(matchesAny(Foo.class, Set.of(Serve.class, Json.class))).isTrue();
        assertThat(matchesAny(Foo.class, Set.of(Protobuf.class, Json.class))).isTrue();
        assertThat(matchesAny(Foo.class, Set.of(Protobuf.class, Serve.class))).isFalse();
        assertThat(matchesAny(Foo.class, Set.of(Protobuf.class))).isFalse();
    }

    @Test
    public void matchesAny_class_two_annotations() {
        @Serve @Json interface Foo {}

        assertThat(matchesAny(Foo.class, Set.of(Json.class))).isTrue();
        assertThat(matchesAny(Foo.class, Set.of(Serve.class))).isTrue();
        assertThat(matchesAny(Foo.class, Set.of(Serve.class, Json.class))).isTrue();
        assertThat(matchesAny(Foo.class, Set.of(Protobuf.class, Serve.class))).isTrue();
        assertThat(matchesAny(Foo.class, Set.of(Protobuf.class))).isFalse();
    }

    @Test
    public void isHandlerClass_serve_class_matches() {
        @Serve interface Foo {}

        assertThat(isHandlerClassDefault(Foo.class)).isTrue();
    }

    @Test
    public void isHandlerClass_serve_class_get_method_matches() {
        @Serve interface Foo {
            @GET void foo();
        }

        assertThat(isHandlerClassDefault(Foo.class)).isTrue();
    }

    @Test
    public void isHandlerClass_only_get_method_matches() {
        interface Foo {
            @GET void foo();
        }

        assertThat(isHandlerClassDefault(Foo.class)).isTrue();
    }

    @Test
    public void isHandlerClass_only_post_method_matches() {
        interface Foo {
            @POST void foo();
        }

        assertThat(isHandlerClassDefault(Foo.class)).isTrue();
    }

    @Test
    public void isHandlerClass_only_call_method_matches() {
        interface Foo {
            @Call void foo();
        }

        assertThat(isHandlerClassDefault(Foo.class)).isTrue();
    }

    @Test
    public void isHandlerClass_simple_class_no_match() {
        assertThat(isHandlerClassDefault(HandlerScanner.class)).isFalse();
        assertThat(isHandlerClassDefault(HandlerScannerTest.class)).isFalse();
    }

    @Test
    public void isHandlerClass_test_method_match() {
        assertThat(isHandlerClass(HandlerScannerTest.class, Serve.class, Set.of(), Set.of(Test.class))).isTrue();
    }

    private static boolean isHandlerClassDefault(@NotNull Class<?> klass) {
        return isHandlerClass(klass, MAIN_HANDLER_ANNOTATION, HANDLER_CLASS_ANNOTATIONS, HANDLER_METHOD_ANNOTATIONS);
    }
}
