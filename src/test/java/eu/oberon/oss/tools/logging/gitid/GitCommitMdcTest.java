package eu.oberon.oss.tools.logging.gitid;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.Map;

import static eu.oberon.oss.tools.logging.gitid.GitPropertyNames.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitCommitMdcTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    @DisplayName("loadProperties() correctly puts all map entries into MDC")
    void testLoadProperties() {
        Map<GitPropertyNames, Object> properties = Map.of(
                GIT_BRANCH, "main",
                GIT_COMMIT_ID_ABBREV, "f452bda",
                GIT_DIRTY, true,
                GIT_TOTAL_COMMIT_COUNT, 42
        );

        GitCommitMdc.loadProperties(properties);

        assertThat(MDC.get(GIT_BRANCH.toString())).isEqualTo("main");
        assertThat(MDC.get(GIT_COMMIT_ID_ABBREV.toString())).isEqualTo("f452bda");
        assertThat(MDC.get(GIT_DIRTY.toString())).isEqualTo("true");
        assertThat(MDC.get(GIT_TOTAL_COMMIT_COUNT.toString())).isEqualTo("42");
    }

    @Test
    @DisplayName("loadProperties() populates MDC with all properties loaded from git.properties")
    void testLoadPropertiesFromGitPropertiesResource() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/git.properties")) {
            assertThat(inputStream).isNotNull();

            Map<GitPropertyNames, Object> properties = AbstractGitProperty.loadGitProperties(inputStream);
            GitCommitMdc.loadProperties(properties);

            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            assertThat(contextMap).isNotNull().hasSize(26);

            for (Map.Entry<GitPropertyNames, Object> entry : properties.entrySet()) {
                assertThat(MDC.get(entry.getKey().toString())).isEqualTo(entry.getValue().toString());
            }
        }
    }

    @Test
    @DisplayName("loadProperties() with empty map does not modify MDC")
    void testLoadPropertiesEmptyMap() {
        GitCommitMdc.loadProperties(Map.of());

        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        assertThat(contextMap).isNullOrEmpty();
    }

    @Test
    @DisplayName("loadProperties() throws NullPointerException when properties map is null")
    void testLoadPropertiesNullMap() {
        //noinspection DataFlowIssue - Deliberate null value for testing purposes
        assertThatThrownBy(() -> GitCommitMdc.loadProperties((Map<GitPropertyNames, Object>) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("loadProperties(InputStream) populates MDC with all properties loaded from git.properties")
    void testLoadPropertiesDirectlyFromInputStream() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/git.properties")) {
            assertThat(inputStream).isNotNull();

            GitCommitMdc.loadProperties(inputStream);

            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            assertThat(contextMap).isNotNull().hasSize(26);
            assertThat(MDC.get(GIT_BRANCH.toString())).isEqualTo("main");
            assertThat(MDC.get(GIT_COMMIT_ID_ABBREV.toString())).isEqualTo("f452bda");
            assertThat(MDC.get(GIT_TAG.toString())).isEqualTo(AbstractGitProperty.NOT_FOUND);
            assertThat(MDC.get(GIT_CLOSEST_TAG_COMMIT_COUNT.toString())).isEqualTo(AbstractGitProperty.NOT_FOUND);
        }
    }

    @Test
    @DisplayName("loadProperties() puts NOT_FOUND into MDC when map values are blank or null")
    void testLoadPropertiesWithBlankAndNullValues() {
        Map<GitPropertyNames, Object> properties = new EnumMap<>(GitPropertyNames.class);
        properties.put(GIT_BRANCH, "  ");
        properties.put(GIT_COMMIT_ID_ABBREV, null);
        properties.put(GIT_DIRTY, "true");

        GitCommitMdc.loadProperties(properties);

        assertThat(MDC.get(GIT_BRANCH.toString())).isEqualTo(AbstractGitProperty.NOT_FOUND);
        assertThat(MDC.get(GIT_COMMIT_ID_ABBREV.toString())).isEqualTo(AbstractGitProperty.NOT_FOUND);
        assertThat(MDC.get(GIT_DIRTY.toString())).isEqualTo("true");
    }

    @Test
    @DisplayName("Logback logs contain MDC properties loaded by GitCommitMdc")
    void testLoggingWithMdcProperties() {
        Logger logger = (Logger) LoggerFactory.getLogger(GitCommitMdcTest.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            Map<GitPropertyNames, Object> properties = Map.of(
                    GIT_COMMIT_ID_ABBREV, "f452bda",
                    GIT_BRANCH, "main"
            );
            GitCommitMdc.loadProperties(properties);

            logger.info("Application event with Git commit MDC context");

            assertThat(listAppender.list).hasSize(1);
            ILoggingEvent event = listAppender.list.getFirst();
            assertThat(event.getMDCPropertyMap())
                    .containsEntry(GIT_COMMIT_ID_ABBREV.toString(), "f452bda")
                    .containsEntry(GIT_BRANCH.toString(), "main");
        } finally {
            logger.detachAppender(listAppender);
        }
    }

    @Test
    @DisplayName("Private constructor cannot be called directly or is accessible via reflection")
    void testPrivateConstructor() throws Exception {
        Constructor<GitCommitMdc> constructor = GitCommitMdc.class.getDeclaredConstructor();
        assertThat(constructor.canAccess(null)).isFalse();

        constructor.setAccessible(true);
        GitCommitMdc instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }
}
