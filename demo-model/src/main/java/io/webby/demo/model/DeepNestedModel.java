package io.webby.demo.model;

public record DeepNestedModel(int id, D d, A a) {

    record A(int id) {}
    record B(int id, A a) {}
    record C(int id, A a, B b, A aa) {}
    record D(int id, C c, B b) {}
}
