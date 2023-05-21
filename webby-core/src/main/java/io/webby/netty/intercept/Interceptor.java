package io.webby.netty.intercept;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.errors.ServeException;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an interceptor of an HTTP request before it hits the handler.
 * Allows to block the request by throwing a {@code ServeException}, amend the incoming request
 * (e.g. by setting an attribute), amend or replace the outgoing response (e.g. adding a header).
 * <p>
 * Interceptors are arranged in a stack and are always called in this order.
 * E.g., if the stack contains <code>[A, B, C]</code> interceptors, the {@link #enter(MutableHttpRequestEx)} method
 * is called in order <code>[A, B, C]</code> and {@link #exit(MutableHttpRequestEx, HttpResponse)} and
 * {@link #cleanup()} methods are called in reverse order <code>[C, B, A]</code>.
 * <p>
 * By default, the interceptors are enabled unconditionally, but the implementation may turn it off depending on
 * the server configuration.
 *
 * @see io.webby.netty.intercept.attr.Attributes
 */
public interface Interceptor {
    /**
     * Returns whether the interceptor is enabled. Currently, the value is requested just once at the server startup.
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Processes the incoming {@code request} before it hit the handler.
     * This method is called if no other interceptor before this in the stack blocked the request.
     *
     * @throws ServeException to block the request and force error response
     */
    default void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        // Do nothing
    }

    /**
     * Processes the outgoing {@code response} after the handler.
     * This method is not expected to throw, but can amend the {@code response} if necessary.
     * This method is called only on successful path, i.e. if no interceptor blocked the incoming request. Otherwise,
     * the {@link #cleanup()} is called instead.
     *
     * @return the new response if necessary (by default, returns the same {@code response} instance)
     */
    default @NotNull HttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull HttpResponse response) {
        return response;
    }

    /**
     * Cleans up the state in case the request was intercepted or an error occurred, in other words
     * if {@link #exit(MutableHttpRequestEx, HttpResponse)} was not called.
     * <p>
     * Note that this method may be called when a request didn't {@link #enter(MutableHttpRequestEx)} this interceptor.
     * This method is not expected to throw.
     */
    default void cleanup() {
        // Do nothing
    }
}
