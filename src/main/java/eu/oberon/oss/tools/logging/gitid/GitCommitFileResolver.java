package eu.oberon.oss.tools.logging.gitid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Generic, logging-framework agnostic resolver that reads git commit hash
 * from build-generated metadata, CI platform environment variables, or JVM system properties.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>JVM system properties ({@code git.commit.id.abbrev}, {@code gitCommit}, {@code GIT_COMMIT})</li>
 *   <li>CI platform environment variables (automatically detected, no config needed)</li>
 *   <li>Build-generated {@code git.properties} in classpath (via git-commit-id-maven-plugin)</li>
 *   <li>Fallback: {@code "unknown"}</li>
 * </ol>
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class GitCommitFileResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCommitFileResolver.class);

    private GitCommitFileResolver() {

    }

    public static final String GIT_PROPERTIES_FILE = "git.properties";
    public static final String GIT_COMMIT_PROPERTY = "git.commit.id.abbrev";
    public static final String GIT_COMMIT_FULL_PROPERTY = "git.commit.id";
    public static final String GIT_COMMIT_SYSTEM_PROPERTY = "git.commit.id.abbrev";
    public static final String GIT_COMMIT_ALIAS_SYSTEM_PROPERTY = "gitCommit";
    public static final String GIT_COMMIT_ENV_STYLE_SYSTEM_PROPERTY = "GIT_COMMIT";
    public static final String UNKNOWN = "unknown";

    /**
     * Environment variable names used by common CI/CD platforms to expose the current commit SHA.
     * Checked in order; the first non-blank value wins.
     */
    private static final String[] CI_COMMIT_ENV_VARS = {
            GIT_COMMIT_ENV_STYLE_SYSTEM_PROPERTY,           // Jenkins, generic
            "GITHUB_SHA",                                   // GitHub Actions
            "CI_COMMIT_SHA",                                // GitLab CI
            "BUILD_SOURCEVERSION",                          // Azure DevOps
            "BITBUCKET_COMMIT",                             // Bitbucket Pipelines
            "CIRCLE_SHA1",                                  // CircleCI
            "TRAVIS_COMMIT",                                // Travis CI
            "DRONE_COMMIT_SHA",                             // Drone CI
            "CF_SHORT_REVISION",                            // Codefresh
            "SOURCE_VERSION",                               // Heroku CI
    };

    /**
     * Abbreviate a long SHA to 8 characters; return as-is if already short or null.
     */
    public static String abbreviate(String sha) {
        if (sha == null) {
            return null;
        }
        return sha.length() > 8 ? sha.substring(0, 8) : sha;
    }

    /**
     * Resolve the git commit hash following the defined resolution order.
     *
     * @return the resolved 8-character (abbreviated) git commit hash, or {@code "unknown"} if not found
     */
    public static String resolve() {
        String result;
        // 1. JVM property override
        result = scanJVMPropertyOverrides();
        if (result != null) {
            return result;
        }

        // 2. CI platform environment variables (checked automatically, no config needed)
        result = scanEnvVariables();
        if (result != null) {
            return result;
        }

        // 3. Build-generated git.properties (works when .git is present during the Maven build)
        result = readPropertyFile();
        if (result != null) {
            return result;
        }

        return UNKNOWN;
    }

    private static String readPropertyFile() {
        try (InputStream inputStream = getResourceAsStream()) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                String commit = properties.getProperty(GIT_COMMIT_PROPERTY);
                if (commit == null || commit.isBlank()) {
                    commit = properties.getProperty(GIT_COMMIT_FULL_PROPERTY);
                }
                if (commit != null && !commit.isBlank()) {
                    return abbreviate(commit.trim());
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to read git.properties file {}", GitCommitFileResolver.GIT_PROPERTIES_FILE, e);
        }
        return null;
    }

    private static String scanEnvVariables() {
        for (String envVar : CI_COMMIT_ENV_VARS) {
            String value = System.getenv(envVar);
            if (value != null && !value.isBlank()) {
                return abbreviate(value.trim());
            }
        }
        return null;
    }


    private static String scanJVMPropertyOverrides() {
        // 1. JVM property override
        for (String systemValueName : new String[]{GIT_COMMIT_SYSTEM_PROPERTY, GIT_COMMIT_ALIAS_SYSTEM_PROPERTY, GIT_COMMIT_ENV_STYLE_SYSTEM_PROPERTY}) {
            String systemValue = System.getProperty(systemValueName);
            if (systemValue != null && !systemValue.isBlank()) {
                return abbreviate(systemValue.trim());
            }
        }
        return null;
    }

    /**
     * Resolves the git commit hash and populates standard JVM system properties
     * ({@code git.commit.id.abbrev}, {@code gitCommit}, and {@code GIT_COMMIT}) if not already set.
     * This makes the commit ID universally accessible in log patterns across all logging frameworks
     * (Logback, Log4j2, JUL, Spring Boot, etc.).
     *
     * @return the resolved git commit hash
     */
    public static String setSystemProperties() {
        String commit = resolve();
        System.setProperty(GIT_COMMIT_SYSTEM_PROPERTY, commit);
        System.setProperty(GIT_COMMIT_ALIAS_SYSTEM_PROPERTY, commit);
        System.setProperty(GIT_COMMIT_ENV_STYLE_SYSTEM_PROPERTY, commit);
        return commit;
    }

    private static InputStream getResourceAsStream() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            InputStream is = contextClassLoader.getResourceAsStream(GitCommitFileResolver.GIT_PROPERTIES_FILE);
            if (is != null) {
                return is;
            }
        }
        ClassLoader classLoader = GitCommitFileResolver.class.getClassLoader();
        if (classLoader != null) {
            InputStream is = classLoader.getResourceAsStream(GitCommitFileResolver.GIT_PROPERTIES_FILE);
            if (is != null) {
                return is;
            }
        }
        return ClassLoader.getSystemResourceAsStream(GitCommitFileResolver.GIT_PROPERTIES_FILE);
    }
}

