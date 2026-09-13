package eu.oberon.oss.tools.logging.gitid;

import eu.oberon.oss.tools.converters.string.Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static eu.oberon.oss.tools.logging.gitid.GitPropertyNames.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitPropertyNamesTest {

    private static Stream<Arguments> expectedPropertyNames() {
        return Stream.of(
                Arguments.of(GIT_BRANCH, "git.branch"),
                Arguments.of(GIT_BUILD_HOST, "git.build.host"),
                Arguments.of(GIT_BUILD_TIME, "git.build.time"),
                Arguments.of(GIT_BUILD_USER_EMAIL, "git.build.user.email"),
                Arguments.of(GIT_BUILD_USER_NAME, "git.build.user.name"),
                Arguments.of(GIT_BUILD_VERSION, "git.build.version"),
                Arguments.of(GIT_CLOSEST_TAG_COMMIT_COUNT, "git.closest.tag.commit.count"),
                Arguments.of(GIT_CLOSEST_TAG_NAME, "git.closest.tag.name"),
                Arguments.of(GIT_COMMIT_AUTHOR_TIME, "git.commit.author.time"),
                Arguments.of(GIT_COMMIT_COMMITTER_TIME, "git.commit.committer.time"),
                Arguments.of(GIT_COMMIT_ID_ABBREV, "git.commit.id.abbrev"),
                Arguments.of(GIT_COMMIT_ID_DESCRIBE, "git.commit.id.describe"),
                Arguments.of(GIT_COMMIT_ID_DESCRIBE_SHORT, "git.commit.id.describe.short"),
                Arguments.of(GIT_COMMIT_ID_FULL, "git.commit.id.full"),
                Arguments.of(GIT_COMMIT_MESSAGE_FULL, "git.commit.message.full"),
                Arguments.of(GIT_COMMIT_MESSAGE_SHORT, "git.commit.message.short"),
                Arguments.of(GIT_COMMIT_TIME, "git.commit.time"),
                Arguments.of(GIT_COMMIT_USER_EMAIL, "git.commit.user.email"),
                Arguments.of(GIT_COMMIT_USER_NAME, "git.commit.user.name"),
                Arguments.of(GIT_DIRTY, "git.dirty"),
                Arguments.of(GIT_LOCAL_BRANCH_AHEAD, "git.local.branch.ahead"),
                Arguments.of(GIT_LOCAL_BRANCH_BEHIND, "git.local.branch.behind"),
                Arguments.of(GIT_REMOTE_ORIGIN_URL, "git.remote.origin.url"),
                Arguments.of(GIT_TAG, "git.tag"),
                Arguments.of(GIT_TAGS, "git.tags"),
                Arguments.of(GIT_TOTAL_COMMIT_COUNT, "git.total.commit.count")
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("expectedPropertyNames")
    @DisplayName("getPropertyName() returns expected dotted property name for each enum constant")
    void testGetPropertyNameMatchesExpected(GitPropertyNames property, String expectedPropertyName) {
        assertThat(property.getPropertyName()).isEqualTo(expectedPropertyName);
    }

    @ParameterizedTest
    @EnumSource(GitPropertyNames.class)
    @DisplayName("getPropertyName() satisfies format invariants across all constants")
    void testGetPropertyNameInvariants(GitPropertyNames property) {
        String name = property.getPropertyName();

        assertThat(name)
                .isNotNull()
                .isNotEmpty()
                .startsWith("git.")
                .doesNotContain("_")
                .isEqualTo(name.toLowerCase(Locale.ROOT))
                .matches("^[a-z]+(\\.[a-z]+)+$");
    }

    @Test
    @DisplayName("getPropertyName() produces unique names for every enum value")
    void testGetPropertyNameUniqueness() {
        GitPropertyNames[] values = GitPropertyNames.values();
        Set<String> propertyNames = new HashSet<>();

        for (GitPropertyNames property : values) {
            propertyNames.add(property.getPropertyName());
        }

        assertThat(propertyNames).hasSameSizeAs(values);
    }

    @ParameterizedTest
    @EnumSource(GitPropertyNames.class)
    @DisplayName("Round-trip: getProperty(property.getPropertyName()) returns the original enum constant")
    void testGetPropertyRoundTrip(GitPropertyNames property) {
        assertThat(GitPropertyNames.getProperty(property.getPropertyName())).isSameAs(property);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "git.branch",
            "git.commit.id.describe-short",
            "git-commit-id-describe-short",
            "GIT.BRANCH",
            "Git.Branch",
            "GIT_BRANCH",
            "git_commit_id_abbrev"
    })
    @DisplayName("getProperty() resolves property names in different case and delimiter formats")
    void testGetPropertyWithVariousFormats(String input) {
        GitPropertyNames resolved = GitPropertyNames.getProperty(input);
        assertThat(resolved).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("getProperty() throws on null or blank/invalid property names")
    void testGetPropertyInvalidInputs(String invalidInput) {
        if (invalidInput == null) {
            //noinspection DataFlowIssue - Deliberate for testing purposes
            assertThatThrownBy(() -> GitPropertyNames.getProperty(null)).isInstanceOf(NullPointerException.class);
        } else {
            assertThatThrownBy(() -> GitPropertyNames.getProperty(invalidInput)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"non.existent.property", "git.unknown", "unknown", "12345"})
    @DisplayName("getProperty() throws IllegalArgumentException for non-existent properties")
    void testGetPropertyNonExistent(String unknownProperty) {
        assertThatThrownBy(() -> GitPropertyNames.getProperty(unknownProperty)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(GitPropertyNames.class)
    @DisplayName("getConverter() returns non-null converter and valid type class for all constants")
    void testGetConverterNonNull(GitPropertyNames property) {
        assertThat(property.getConverter()).isNotNull();
        assertThat(property.getConverter().getTypeClass()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(GitPropertyNames.class)
    @DisplayName("convertToString() returns identity function for default string enum constants")
    void testConvertToStringForDefaultProperties(GitPropertyNames property) {
        if (property.getConverter().getTypeClass().equals(String.class)) {
            Converter<String> converter = property.getConverter();
            Function<String, String> convertToString = converter.convertToString();

            assertThat(convertToString).isNotNull();
            assertThat(convertToString.apply("test-value")).isEqualTo("test-value");
            assertThat(convertToString.apply("")).isEmpty();
            assertThat(convertToString.apply(null)).isNull();
        }
    }

    @Test
    @DisplayName("Default string converter convertToString() behaves as Function.identity()")
    void testConvertToStringIdentity() {
        Converter<String> converter = GIT_BRANCH.getConverter();
        Function<String, String> convertToString = converter.convertToString();

        String input = "anyStringValue123";
        assertThat(convertToString.apply(input)).isSameAs(input);
    }
}
