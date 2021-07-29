package io.webby.util;

public interface ThrowRunnable<E extends Throwable> {
    void run() throws E;
}
