package io.webby.examples.model;

public record InnerDataModel(long id, Simple simple) {

    static record Simple(int id, long a, String b) {}
}
