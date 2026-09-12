package eu.oberon.oss.tools.logging.gitid;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class GitCommitMdcTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        MDC.clear();
        System.clearProperty(GitCommitFileResolver.GIT_COMMIT_SYSTEM_PROPERTY);
    }

    @Test
    void testPutDefaultKey() {
        System.setProperty(GitCommitFileResolver.GIT_COMMIT_SYSTEM_PROPERTY, "123456789abc");
        String result = GitCommitMdc.put();

        assertThat(result).isEqualTo("12345678");
        assertThat(MDC.get(GitCommitMdc.DEFAULT_MDC_KEY)).isEqualTo("12345678");
    }

    @Test
    void testPutCustomKey() {
        System.setProperty(GitCommitFileResolver.GIT_COMMIT_SYSTEM_PROPERTY, "123456789abc");
        String result = GitCommitMdc.put("customGitKey");

        assertThat(result).isEqualTo("12345678");
        assertThat(MDC.get("customGitKey")).isEqualTo("12345678");
    }

    @Test
    void testRemoveDefaultKey() {
        MDC.put(GitCommitMdc.DEFAULT_MDC_KEY, "test-commit");
        assertThat(MDC.get(GitCommitMdc.DEFAULT_MDC_KEY)).isEqualTo("test-commit");

        GitCommitMdc.remove();
        assertThat(MDC.get(GitCommitMdc.DEFAULT_MDC_KEY)).isNull();
    }

    @Test
    void testRemoveCustomKey() {
        MDC.put("customGitKey", "test-commit");
        assertThat(MDC.get("customGitKey")).isEqualTo("test-commit");

        GitCommitMdc.remove("customGitKey");
        assertThat(MDC.get("customGitKey")).isNull();
    }
}
