package io.webby.websockets;

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
}
