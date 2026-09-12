package eu.oberon.oss.tools.logging.gitid;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitCommitFileResolverTest {

    @BeforeEach
    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(GitCommitFileResolver.GIT_COMMIT_SYSTEM_PROPERTY);
        System.clearProperty(GitCommitFileResolver.GIT_COMMIT_ALIAS_SYSTEM_PROPERTY);
        System.clearProperty(GitCommitFileResolver.GIT_COMMIT_ENV_STYLE_SYSTEM_PROPERTY);
    }

    @Test
    void testAbbreviate() {
        assertThat(GitCommitFileResolver.abbreviate(null)).isNull();
        assertThat(GitCommitFileResolver.abbreviate("abc")).isEqualTo("abc");
        assertThat(GitCommitFileResolver.abbreviate("12345678")).isEqualTo("12345678");
        assertThat(GitCommitFileResolver.abbreviate("1234567890abcdef")).isEqualTo("12345678");
    }

    @Test
    void testResolveFromSystemProperty() {
        System.setProperty(GitCommitFileResolver.GIT_COMMIT_SYSTEM_PROPERTY, "abcdef1234567890");
        assertThat(GitCommitFileResolver.resolve()).isEqualTo("abcdef12");
    }

    @Test
    void testResolveFromSystemPropertyAlias() {
        System.setProperty(GitCommitFileResolver.GIT_COMMIT_ALIAS_SYSTEM_PROPERTY, "fedcba9876543210");
        assertThat(GitCommitFileResolver.resolve()).isEqualTo("fedcba98");
    }

    @Test
    void testResolveFromSystemPropertyEnvStyle() {
        System.setProperty(GitCommitFileResolver.GIT_COMMIT_ENV_STYLE_SYSTEM_PROPERTY, "1122334455667788");
        assertThat(GitCommitFileResolver.resolve()).isEqualTo("11223344");
    }

    @Test
    void testResolveFallback() {
        // In the test environment without system properties, CI env vars or git.properties,
        // it should resolve either to a CI env var (if running in CI) or "unknown".
        String resolved = GitCommitFileResolver.resolve();
        assertThat(resolved).isNotNull().isNotEmpty();
    }

    @Test
    void testSetSystemProperties() {
        System.setProperty(GitCommitFileResolver.GIT_COMMIT_SYSTEM_PROPERTY, "aabbccddeeff");
        String commit = GitCommitFileResolver.setSystemProperties();
        assertThat(commit).isEqualTo("aabbccdd");
        assertThat(System.getProperty(GitCommitFileResolver.GIT_COMMIT_SYSTEM_PROPERTY)).isEqualTo("aabbccdd");
        assertThat(System.getProperty(GitCommitFileResolver.GIT_COMMIT_ALIAS_SYSTEM_PROPERTY)).isEqualTo("aabbccdd");
        assertThat(System.getProperty(GitCommitFileResolver.GIT_COMMIT_ENV_STYLE_SYSTEM_PROPERTY)).isEqualTo("aabbccdd");
    }

    @Test
    void testResolveFromCustomClassLoaderWithAbbrevProperty() {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            java.util.Map<String, byte[]> resourceMap = java.util.Map.of(
                    GitCommitFileResolver.GIT_PROPERTIES_FILE,
                    "git.commit.id.abbrev=99887766\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
            ClassLoader mockClassLoader = new ClassLoader(null) {
                @Override
                public java.io.InputStream getResourceAsStream(String name) {
                    byte[] data = resourceMap.get(name);
                    return data != null ? new java.io.ByteArrayInputStream(data) : null;
                }
            };
            Thread.currentThread().setContextClassLoader(mockClassLoader);

            String result = GitCommitFileResolver.resolve();
            assertThat(result).isEqualTo("99887766");
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Test
    void testResolveFromCustomClassLoaderWithFullProperty() {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            java.util.Map<String, byte[]> resourceMap = java.util.Map.of(
                    GitCommitFileResolver.GIT_PROPERTIES_FILE,
                    "git.commit.id=12345678901234567890\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
            ClassLoader mockClassLoader = new ClassLoader(null) {
                @Override
                public java.io.InputStream getResourceAsStream(String name) {
                    byte[] data = resourceMap.get(name);
                    return data != null ? new java.io.ByteArrayInputStream(data) : null;
                }
            };
            Thread.currentThread().setContextClassLoader(mockClassLoader);

            String result = GitCommitFileResolver.resolve();
            assertThat(result).isEqualTo("12345678");
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }
}
