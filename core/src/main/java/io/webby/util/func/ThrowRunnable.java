package io.webby.util.func;

public interface ThrowRunnable<E extends Throwable> {
    void run() throws E;
}
