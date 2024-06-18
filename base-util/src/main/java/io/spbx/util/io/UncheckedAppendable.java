package io.spbx.util.io;

import io.spbx.util.base.Unchecked;
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
            return Unchecked.rethrow(e);
        }
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws UncheckedIOException {
        try {
            return delegate.append(csq, start, end);
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        }
    }

    @Override
    public Appendable append(char c) throws UncheckedIOException {
        try {
            return delegate.append(c);
        } catch (IOException e) {
            return Unchecked.rethrow(e);
        }
    }
}
