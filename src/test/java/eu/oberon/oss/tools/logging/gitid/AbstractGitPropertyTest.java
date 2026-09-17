package eu.oberon.oss.tools.logging.gitid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static eu.oberon.oss.tools.logging.gitid.AbstractGitProperty.NOT_FOUND;
import static eu.oberon.oss.tools.logging.gitid.GitPropertyNames.*;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class AbstractGitPropertyTest {

    public static Stream<Arguments> testLoadGitPropertiesUsingResourceFile() {

        return Stream.of(
                Arguments.of(GIT_BRANCH, String.class, "main"),
                Arguments.of(GIT_BUILD_HOST, String.class, "Tiger-Lilly-4970K"),
                Arguments.of(GIT_BUILD_TIME, LocalDateTime.class, LocalDateTime.parse("2026-09-13T12:56:44+02:00", ISO_OFFSET_DATE_TIME)),
                Arguments.of(GIT_BUILD_USER_EMAIL, String.class, "info@oberon-oss.eu"),
                Arguments.of(GIT_BUILD_USER_NAME, String.class, "TigerLilly"),
                Arguments.of(GIT_BUILD_VERSION, String.class, "1.0-SNAPSHOT"),
                Arguments.of(GIT_CLOSEST_TAG_COMMIT_COUNT, Integer.class, NOT_FOUND),
                Arguments.of(GIT_CLOSEST_TAG_NAME, String.class, NOT_FOUND),
                Arguments.of(GIT_COMMIT_AUTHOR_TIME, LocalDateTime.class, LocalDateTime.parse("2026-09-12T15:51:41+02:00", ISO_OFFSET_DATE_TIME)),
                Arguments.of(GIT_COMMIT_COMMITTER_TIME, LocalDateTime.class, LocalDateTime.parse("2026-09-12T15:51:41+02:00", ISO_OFFSET_DATE_TIME)),
                Arguments.of(GIT_COMMIT_ID_ABBREV, String.class, "f452bda"),
                Arguments.of(GIT_COMMIT_ID_DESCRIBE, String.class, "f452bda-dirty"),
                Arguments.of(GIT_COMMIT_ID_DESCRIBE_SHORT, String.class, "f452bda-dirty"),
                Arguments.of(GIT_COMMIT_ID_FULL, String.class, "f452bda7c2f262fd0b3670fd109f93675988560f"),
                Arguments.of(GIT_COMMIT_MESSAGE_SHORT, String.class, "Add git commit ID resolution and SLF4J MDC utilities"),
                Arguments.of(GIT_COMMIT_MESSAGE_FULL, String.class, "Add git commit ID resolution and SLF4J MDC utilities"),
                Arguments.of(GIT_COMMIT_TIME, LocalDateTime.class, LocalDateTime.parse("2026-09-12T15:51:41+02:00", ISO_OFFSET_DATE_TIME)),
                Arguments.of(GIT_COMMIT_USER_EMAIL, String.class, "info@oberon-oss.eu"),
                Arguments.of(GIT_COMMIT_USER_NAME, String.class, "TigerLilly"),
                Arguments.of(GIT_DIRTY, Boolean.class, true),
                Arguments.of(GIT_LOCAL_BRANCH_AHEAD, Integer.class, 0),
                Arguments.of(GIT_LOCAL_BRANCH_BEHIND, Integer.class, 0),
                Arguments.of(GIT_REMOTE_ORIGIN_URL, String.class, "git@github.com:oberon-oss/git-commit-in-log.git"),
                Arguments.of(GIT_TAG, String.class, NOT_FOUND),
                Arguments.of(GIT_TAGS, String.class, NOT_FOUND),
                Arguments.of(GIT_TOTAL_COMMIT_COUNT, Integer.class, 1)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLoadGitPropertiesUsingResourceFile(GitPropertyNames gitPropertyNames, Class<?> type, Object expectedValue) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/git.properties")) {
            assertThat(inputStream).isNotNull();

            Map<GitPropertyNames, Object> properties = AbstractGitProperty.loadGitProperties(inputStream);

            assertThat(properties)
                    .isNotNull()
                    .hasSize(26)
                    .containsOnlyKeys(values());

            assertThat(gitPropertyNames.getConverter().getTypeClass()).isEqualTo(type);

            if (gitPropertyNames == GIT_COMMIT_MESSAGE_FULL || gitPropertyNames == GIT_COMMIT_ID_FULL) {
                assertTrue(properties.get(gitPropertyNames).toString().startsWith(expectedValue.toString()));
            } else if (NOT_FOUND.equals(expectedValue)) {
                assertEquals(NOT_FOUND, properties.get(gitPropertyNames));
            } else {
                Object convertedValue = gitPropertyNames.getConverter().convertFromString().apply((String) properties.get(gitPropertyNames));
                assertThat(convertedValue).isEqualTo(expectedValue);
            }
        }
    }

    @Test
    void testLoadGitPropertiesReturnsUnmodifiableMap() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/git.properties")) {
            assertThat(inputStream).isNotNull();

            Map<GitPropertyNames, Object> properties = AbstractGitProperty.loadGitProperties(inputStream);

            //noinspection DataFlowIssue
            assertThatThrownBy(() -> properties.put(GIT_BRANCH, "new-branch")).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Test
    void testLoadGitPropertiesEmptyStream() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
            Map<GitPropertyNames, Object> properties = AbstractGitProperty.loadGitProperties(inputStream);
            assertThat(properties)
                    .isNotNull()
                    .hasSize(26)
                    .containsOnlyKeys(values());

            for (GitPropertyNames propertyName : values()) {
                assertEquals(NOT_FOUND, properties.get(propertyName));
            }
        }
    }

    @Test
    void testLoadGitPropertiesThrowsOnIOException() {
        //noinspection resource
        InputStream faultyStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Stream read failed");
            }
        };

        assertThatThrownBy(() -> AbstractGitProperty.loadGitProperties(faultyStream))
                .isInstanceOf(IOException.class)
                .hasMessage("Stream read failed");
    }

    @Test
    void testAbstractGitPropertyGetterAndSetter() {
        AbstractGitProperty<String> property = new AbstractGitProperty<>(GIT_BRANCH) {
        };

        assertThat(property.getPropertyName()).isEqualTo(GIT_BRANCH);
        assertThat(property.getValue()).isNull();

        property.setValue("main");
        assertThat(property.getValue()).isEqualTo("main");
    }

    @Test
    void testAbstractGitPropertyNullPropertyName() {
        AbstractGitProperty<String> property = new AbstractGitProperty<>(null) {
        };

        assertThat(property.getPropertyName()).isNull();
        assertThat(property.getValue()).isNull();
    }

    @Test
    void testLoadGitPropertiesWithPartialAndBlankProperties() throws IOException {
        String propertiesContent = """
                git.branch=feature/test
                git.commit.id.abbrev=  \s
                git.commit.user.name=
                """;
        try (InputStream inputStream = new ByteArrayInputStream(propertiesContent.getBytes())) {
            Map<GitPropertyNames, Object> properties = AbstractGitProperty.loadGitProperties(inputStream);

            assertThat(properties).hasSize(26);
            assertEquals("feature/test", properties.get(GIT_BRANCH));
            assertEquals(NOT_FOUND, properties.get(GIT_COMMIT_ID_ABBREV));
            assertEquals(NOT_FOUND, properties.get(GIT_COMMIT_USER_NAME));
            assertEquals(NOT_FOUND, properties.get(GIT_COMMIT_ID_FULL));
            assertEquals(NOT_FOUND, properties.get(GIT_BUILD_VERSION));
        }
    }

    @Test
    void testLoadGitPropertiesWithNullPropertyValue() throws IOException {
        try (var _ = mockConstruction(Properties.class, (mock, _) -> {
            Map<Object, Object> entryMap = Collections.singletonMap("git.branch", null);
            when(mock.entrySet()).thenReturn(entryMap.entrySet());
        })) {
            try (InputStream inputStream = new ByteArrayInputStream(new byte[0])) {
                Map<GitPropertyNames, Object> properties = AbstractGitProperty.loadGitProperties(inputStream);

                assertThat(properties)
                        .isNotNull()
                        .hasSize(26);
                assertEquals(NOT_FOUND, properties.get(GIT_BRANCH));
            }
        }
    }
}
