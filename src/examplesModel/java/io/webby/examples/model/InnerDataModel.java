package io.webby.examples.model;

public record InnerDataModel(long id, Simple simple, Level1 level1) {

    static record Simple(int id, long a, String b) {}
    static record Level1(int id, Simple simple) {}
}
