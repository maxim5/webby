package io.webby.examples.model;

public record ComplexIdModel(Key id, int a) {

    record Key(int x, long y, String z) {}
}
