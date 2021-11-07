package io.webby.examples.model;

public record NestedModel(long id, Simple simple, Level1 level1) {

    record Simple(int id, long a, String b) {}
    record Level1(int id, Simple simple) {}
}
