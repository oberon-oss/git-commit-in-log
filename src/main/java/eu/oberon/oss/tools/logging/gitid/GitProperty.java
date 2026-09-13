package eu.oberon.oss.tools.logging.gitid;


import org.jetbrains.annotations.Nullable;

/**
 * Represents an interface for Git properties.
 *
 * @param <T> The type of the value associated with the Git property.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface GitProperty<T> {

    /**
     * Default placeholder string representation for missing or blank Git property values.
     *
     * @since 1.0.0
     */
    String NOT_FOUND = "** NOT FOUND **";

    /**
     * Retrieves the name of the Git property.
     *
     * @return The name of the Git property.
     *
     * @since 1.0.0
     */
    GitPropertyNames getPropertyName();

    /**
     * Retrieves the value of the Git property.
     *
     * @return The value of the Git property.
     *
     * @since 1.0.0
     */
    @Nullable T getValue();

    /**
     * Sets the value of the Git property.
     *
     * @param value The value to set for the Git property.
     *
     * @since 1.0.0
     */
    void setValue(@Nullable T value);
}
