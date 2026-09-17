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
    /**
     * Current Git branch name.
     *
     * @since 1.0.0
     */
    GIT_BRANCH,
    /**
     * Name of the host on which the build was executed.
     *
     * @since 1.0.0
     */
    GIT_BUILD_HOST,
    /**
     * Timestamp of the build.
     *
     * @since 1.0.0
     */
    GIT_BUILD_TIME(new LocalDateTimeConverter()),
    /**
     * Email of the user who executed the build.
     *
     * @since 1.0.0
     */
    GIT_BUILD_USER_EMAIL,
    /**
     * Name of the user who executed the build.
     *
     * @since 1.0.0
     */
    GIT_BUILD_USER_NAME,
    /**
     * Version of the build.
     *
     * @since 1.0.0
     */
    GIT_BUILD_VERSION,
    /**
     * Number of commits since the closest tag.
     *
     * @since 1.0.0
     */
    GIT_CLOSEST_TAG_COMMIT_COUNT(new IntegerConverter()),
    /**
     * Name of the closest tag.
     *
     * @since 1.0.0
     */
    GIT_CLOSEST_TAG_NAME,
    /**
     * Timestamp of the commit authoring.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_AUTHOR_TIME(new LocalDateTimeConverter()),
    /**
     * Timestamp when the commit was committed.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_COMMITTER_TIME(new LocalDateTimeConverter()),
    /**
     * Abbreviated commit ID.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_ID_ABBREV,
    /**
     * Output of {@code git describe} for the commit.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_ID_DESCRIBE,
    /**
     * Short output of {@code git describe} for the commit.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_ID_DESCRIBE_SHORT,
    /**
     * Full commit ID.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_ID_FULL,
    /**
     * Full commit message.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_MESSAGE_FULL,
    /**
     * Short commit message.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_MESSAGE_SHORT,
    /**
     * Timestamp of the commit.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_TIME(new LocalDateTimeConverter()),
    /**
     * Email of the commit author.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_USER_EMAIL,
    /**
     * Name of the commit author.
     *
     * @since 1.0.0
     */
    GIT_COMMIT_USER_NAME,
    /**
     * Whether the Git working tree has uncommitted changes.
     *
     * @since 1.0.0
     */
    GIT_DIRTY(new BooleanConverter()),
    /**
     * Number of commits the local branch is ahead of the remote.
     *
     * @since 1.0.0
     */
    GIT_LOCAL_BRANCH_AHEAD(new IntegerConverter()),
    /**
     * Number of commits the local branch is behind the remote.
     *
     * @since 1.0.0
     */
    GIT_LOCAL_BRANCH_BEHIND(new IntegerConverter()),
    /**
     * URL of the remote origin repository.
     *
     * @since 1.0.0
     */
    GIT_REMOTE_ORIGIN_URL,
    /**
     * Current Git tag name.
     *
     * @since 1.0.0
     */
    GIT_TAG,
    /**
     * Tags pointing to the current commit.
     *
     * @since 1.0.0
     */
    GIT_TAGS,
    /**
     * Total number of commits.
     *
     * @since 1.0.0
     */
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
