package eu.oberon.oss.tools.logging.gitid;

import org.slf4j.MDC;

/**
 * Utility class to populate and clear the Git commit ID in SLF4J's {@link MDC}.
 *
 * <p>This allows log patterns across any SLF4J-compatible logging framework (Logback, Log4j2, etc.)
 * to access the git commit hash via {@code %X{gitCommit}} or a custom key.
 */
public final class GitCommitMdc {

    public static final String DEFAULT_MDC_KEY = "gitCommit";

    private GitCommitMdc() {
        // utility class
    }

    /**
     * Put the resolved git commit ID into MDC under the default key ({@code "gitCommit"}).
     *
     * @return the resolved git commit ID that was placed in MDC
     */
    public static String put() {
        return put(DEFAULT_MDC_KEY);
    }

    /**
     * Put the resolved git commit ID into MDC under the specified key.
     *
     * @param key the MDC key to use
     * @return the resolved git commit ID that was placed in MDC
     */
    public static String put(String key) {
        String commit = GitCommitFileResolver.resolve();
        MDC.put(key, commit);
        return commit;
    }

    /**
     * Remove the git commit ID from MDC under the default key ({@code "gitCommit"}).
     */
    public static void remove() {
        remove(DEFAULT_MDC_KEY);
    }

    /**
     * Remove the git commit ID from MDC under the specified key.
     *
     * @param key the MDC key to remove
     */
    public static void remove(String key) {
        MDC.remove(key);
    }
}
