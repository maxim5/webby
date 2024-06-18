package io.spbx.webby.demo.model;

public record PrimitiveModel(int id, int i, long l, byte b, short s, char ch, float f, double d, boolean bool) {

    // Pojos for custom adapters
    public record PrimitiveDuo(int i, long l) {}
    public record PrimitiveTrio(byte b, short s, char ch) {}
}
