package io.spbx.webby.demo.model;

public record NestedModel(long id, Simple simple, Level1 level1) {

    public record Simple(int id, long a, String b) {}
    public record Level1(int id, Simple simple) {}
}
