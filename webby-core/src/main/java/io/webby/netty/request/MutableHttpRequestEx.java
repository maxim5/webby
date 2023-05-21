package io.webby.netty.request;

import io.webby.auth.session.SessionModel;
import io.webby.auth.user.UserModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MutableHttpRequestEx extends HttpRequestEx {
    /**
     * Binds the current {@code user} with the request and current session.
     * Throws if the request is already authenticated.
     */
    void authenticate(@NotNull UserModel user);

    /**
     * Sets the {@code session} attribute to the request.
     * Throws if the request already has a session or a user attribute.
     *
     * @see #setAttr(int, Object)
     */
    void setSession(@NotNull SessionModel session);

    /**
     * Sets the {@code user} attribute to the request.
     * Throws if the request already has a user attribute.
     * Throws if the {@code user} is not consistent with the current session.
     *
     * @see #setAttr(int, Object)
     */
    void setUser(@NotNull UserModel user);

    /**
     * Sets the non-null attribute {@code attr} at a given {@code position}.
     * Must be called no more than once per position. Throws if the value is already set.
     *
     * @see #attr(int)
     */
    void setAttr(int position, @NotNull Object attr);

    /**
     * Sets the nullable attribute {@code attr} at a given {@code position}.
     * Must be called no more than once per position. Throws if the value is already set.
     *
     * @see #attr(int)
     */
    void setNullableAttr(int position, @Nullable Object attr);
}
