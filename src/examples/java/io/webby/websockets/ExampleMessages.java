package io.webby.websockets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

class ExampleMessages {
    public interface SimpleMessage {}

    public static class PrimitiveMessage implements SimpleMessage {
        int i;
        long l;
        byte b;
        short s;
        char ch;
        float f;
        double d;
        boolean bool;

        public @NotNull PrimitiveMessage withInt(int i) { this.i = i; return this; }
        public @NotNull PrimitiveMessage withLong(long l) { this.l = l; return this; }
        public @NotNull PrimitiveMessage withByte(byte b) { this.b = b; return this; }
        public @NotNull PrimitiveMessage withShort(short s) { this.s = s; return this; }
        public @NotNull PrimitiveMessage withChar(char ch) { this.ch = ch; return this; }
        public @NotNull PrimitiveMessage withFloat(float f) { this.f = f; return this; }
        public @NotNull PrimitiveMessage withDouble(double d) { this.d = d; return this; }
        public @NotNull PrimitiveMessage withBool(boolean bool) { this.bool = bool; return this; }

        public static @NotNull PrimitiveMessage primitiveInt(int i) {
            return new PrimitiveMessage().withInt(i);
        }

        public static @NotNull PrimitiveMessage primitiveLong(long l) {
            return new PrimitiveMessage().withLong(l);
        }

        public static @NotNull PrimitiveMessage primitiveChar(char ch) {
            return new PrimitiveMessage().withChar(ch);
        }

        public static @NotNull PrimitiveMessage primitiveBool(boolean bool) {
            return new PrimitiveMessage().withBool(bool);
        }

        @Override
        public String toString() {
            return "PrimitiveMessage{i=%d, l=%d, b=%s, s=%s, ch=%s, f=%s, d=%s, bool=%s}".formatted(i, l, b, s, ch, f, d, bool);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PrimitiveMessage that && i == that.i && l == that.l && b == that.b && s == that.s &&
                    ch == that.ch && Float.compare(that.f, f) == 0 && Double.compare(that.d, d) == 0 && bool == that.bool;
        }

        @Override
        public int hashCode() {
            return Objects.hash(i, l, b, s, ch, f, d, bool);
        }
    }

    public static class StringMessage implements SimpleMessage {
        String s;

        public StringMessage(@Nullable String s) {
            this.s = s;
        }

        public @NotNull StringMessage with(@Nullable String s) {
            this.s = s;
            return this;
        }

        @Override
        public String toString() {
            return "StringMessage{s='%s'}".formatted(s);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof StringMessage that && Objects.equals(s, that.s);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s);
        }
    }
}
