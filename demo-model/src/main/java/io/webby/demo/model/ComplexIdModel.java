package io.webby.demo.model;

public record ComplexIdModel(Key id, int a) {

    record Key(int x, long y, String z) {}
}
