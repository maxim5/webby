package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents foreign entity referenced from another entity. In SQL, it's represented by a foreign key, i.e. just an id.
 * In case Table API retrieves a shallow entity, the {@link Foreign} instance contains only id of type {@link I}.
 * In case Table API retrieves a full entity (following foreign references), the {@link Foreign} instance in addition
 * contains the referenced entity of type {@link E} (which too can be shallow or full).
 * <p>
 * Instances of {@link Foreign} are essentially immutable, but can be changed from shallow to full, i.e. updated with
 * an entity reference.
 *
 * @param <I> foreign key type
 * @param <E> foreign entity type
 *
 * @see ReadFollow
 */
public interface Foreign<I, E> {
    /**
     * Returns the foreign key (id). Is always set.
     */
    @NotNull I getFk();

    /**
     * Returns the nullable foreign entity, if it is set.
     */
    @Nullable E getEntity();

    /**
     * Returns whether the foreign entity is present.
     */
    default boolean hasEntity() {
        return getEntity() != null;
    }

    /**
     * Sets the {@code entity} reference if it's not present in this instance.
     * @return true if updated
     */
    boolean setEntityIfMissing(@NotNull E entity);

    /**
     * Sets the {@code entity} regardless of the current present value.
     */
    void setEntityUnconditionally(@NotNull E entity);
}
