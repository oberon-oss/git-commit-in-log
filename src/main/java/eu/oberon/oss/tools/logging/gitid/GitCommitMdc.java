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

    public static void loadProperties(Map<GitPropertyNames, Object> properties) {
        for (Map.Entry<GitPropertyNames, Object> entry : properties.entrySet()) {
            Object value = entry.getValue();
            MDC.put(entry.getKey().toString(), value == null || value.toString().isBlank() ? GitProperty.NOT_FOUND : value.toString());
        }
    }

    public static void loadProperties(InputStream inputStream) throws IOException {
        loadProperties(AbstractGitProperty.loadGitProperties(inputStream));
    }
}
