package eu.oberon.oss.tools.logging.gitid;

import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Utility class to populate and clear the Git commit ID in SLF4J's {@link MDC}.
 *
 * <p>This allows log patterns across any SLF4J-compatible logging framework (Logback, Log4j2, etc.)
 * to access the git commit hash via {@code %X{gitCommit}} or a custom key.
 */
public final class GitCommitMdc {

    private GitCommitMdc() {
        // utility class
    }

    /**
     * Loads a set of Git properties into SLF4J's MDC context.
     * <p>
     * Each Git property name and its corresponding value is added to the MDC. If the property value is null or blank, a default placeholder string is used
     * instead.
     *
     * @param properties A map containing {@link GitPropertyNames} keys and their associated property values.
     *
     * @since 1.0.0
     */
    public static void loadProperties(Map<GitPropertyNames, Object> properties) {
        for (Map.Entry<GitPropertyNames, Object> entry : properties.entrySet()) {
            Object value = entry.getValue();
            MDC.put(entry.getKey().toString(), value == null || value.toString().isBlank() ? GitProperty.NOT_FOUND : value.toString());
        }
    }

    /**
     * Loads Git properties from the provided input stream and populates these properties into the SLF4J MDC context.
     * <p>
     * Each property name and its corresponding value is added to the MDC, with a default placeholder string used for entries that are null or blank.
     *
     * @param inputStream The input stream from which the Git properties are to be loaded. Must not be null.
     *
     * @throws IOException If an I/O error occurs while reading from the input stream.
     * @since 1.0.0
     */
    public static void loadProperties(InputStream inputStream) throws IOException {
        loadProperties(AbstractGitProperty.loadGitProperties(inputStream));
    }
}
