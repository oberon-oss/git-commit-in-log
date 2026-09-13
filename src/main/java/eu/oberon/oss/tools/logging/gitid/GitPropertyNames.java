package eu.oberon.oss.tools.logging.gitid;

import eu.oberon.oss.tools.converters.string.Converter;
import eu.oberon.oss.tools.converters.string.std.BooleanConverter;
import eu.oberon.oss.tools.converters.string.std.IntegerConverter;

import java.util.function.Function;

/**
 * Enum representing the names of Git properties.
 * <p>
 * The specified names are the properties that can be written at compile time and/or during CI/CD pipeline processing, when using the
 * <a href="https://github.com/git-commit-id/git-commit-id-maven-plugin">Git Commit ID Maven Plugin</a>.
 *
 * @since 1.0.0
 */
public enum GitPropertyNames {
    GIT_BRANCH,
    GIT_BUILD_HOST,
    GIT_BUILD_TIME(new LocalDateTimeConverter()),
    GIT_BUILD_USER_EMAIL,
    GIT_BUILD_USER_NAME,
    GIT_BUILD_VERSION,
    GIT_CLOSEST_TAG_COMMIT_COUNT(new IntegerConverter()),
    GIT_CLOSEST_TAG_NAME,
    GIT_COMMIT_AUTHOR_TIME(new LocalDateTimeConverter()),
    GIT_COMMIT_COMMITTER_TIME(new LocalDateTimeConverter()),
    GIT_COMMIT_ID_ABBREV,
    GIT_COMMIT_ID_DESCRIBE,
    GIT_COMMIT_ID_DESCRIBE_SHORT,
    GIT_COMMIT_ID_FULL,
    GIT_COMMIT_MESSAGE_FULL,
    GIT_COMMIT_MESSAGE_SHORT,
    GIT_COMMIT_TIME(new LocalDateTimeConverter()),
    GIT_COMMIT_USER_EMAIL,
    GIT_COMMIT_USER_NAME,
    GIT_DIRTY(new BooleanConverter()),
    GIT_LOCAL_BRANCH_AHEAD(new IntegerConverter()),
    GIT_LOCAL_BRANCH_BEHIND(new IntegerConverter()),
    GIT_REMOTE_ORIGIN_URL,
    GIT_TAG,
    GIT_TAGS,
    GIT_TOTAL_COMMIT_COUNT(new IntegerConverter());

    private final Converter<?> datatype;

    GitPropertyNames() {
        datatype = new Converter<String>() {
            @Override
            public Class<String> getTypeClass() {
                return String.class;
            }

            @Override
            public Function<String, String> convertToString() {
                return Function.identity();
            }

            @Override
            public Function<String, String> convertFromString() {
                return Function.identity();
            }
        };
    }

    GitPropertyNames(Converter<?> converter) {
        this.datatype = converter;
    }

    /**
     * Retrieves the converter associated with the enum constant.
     *
     * @param <T> The type of the converter's target value.
     *
     * @return A {@link Converter} instance that handles the conversion of the associated type.
     *
     * @since 1.0.0
     */
    public <T> Converter<T> getConverter() {
        //noinspection unchecked
        return (Converter<T>) datatype;
    }

    /**
     * Retrieves the property name of the current enum constant.
     *
     * @return the property name as a String
     *
     * @since 1.0.0
     */
    public String getPropertyName() {
        return name().toLowerCase().replace("_", ".");
    }

    /**
     * Converts a property name to the corresponding {@link GitPropertyNames} enum value.
     *
     * @param propertyName The property name to convert.
     *
     * @return The corresponding {@link GitPropertyNames} enum value, or null if the property name is not found.
     *
     * @since 1.0.0
     */
    public static GitPropertyNames getProperty(String propertyName) {
        String value = propertyName.replace(".", "_").replace("-", "_").toUpperCase();
        return GitPropertyNames.valueOf(value);
    }
}
