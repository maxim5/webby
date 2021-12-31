package io.webby.util.io;

import io.webby.util.base.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;

public class UncheckedAppendable implements Appendable {
    private final Appendable delegate;

    public UncheckedAppendable(@NotNull Appendable delegate) {
        this.delegate = delegate;
    }

    @Override
    public Appendable append(CharSequence csq) throws UncheckedIOException {
        try {
            return delegate.append(csq);
        } catch (IOException e) {
            return Rethrow.rethrow(e);
        }
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws UncheckedIOException {
        try {
            return delegate.append(csq, start, end);
        } catch (IOException e) {
            return Rethrow.rethrow(e);
        }
    }

    @Override
    public Appendable append(char c) throws UncheckedIOException {
        try {
            return delegate.append(c);
        } catch (IOException e) {
            return Rethrow.rethrow(e);
        }
    }
}
