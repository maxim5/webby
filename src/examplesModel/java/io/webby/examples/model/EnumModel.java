package io.webby.examples.model;

public record EnumModel(Foo id, Foo foo, Nested nested) {

    enum Foo {
        ZERO,
        FIRST,
        SECOND
    }
    record Nested(Foo foo, String s) {}
}
