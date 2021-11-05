package io.webby.examples.model;

public record EnumModel(Foo id, Foo foo, Nested nested) {

    public enum Foo {
        FIRST,
        SECOND
    }
    public record Nested(Foo foo, String s) {}
}
