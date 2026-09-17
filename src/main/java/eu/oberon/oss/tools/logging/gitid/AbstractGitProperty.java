package eu.oberon.oss.tools.logging.gitid;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import static eu.oberon.oss.tools.logging.gitid.GitPropertyNames.getProperty;

/**
 * Represents an abstract implementation of the {@link GitProperty} interface.
 *
 * @param <T> The type of the value associated with the Git property.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class AbstractGitProperty<T> implements GitProperty<T> {

    private T value;
    private final GitPropertyNames gitPropertyName;

    /**
     * Constructs an {@code AbstractGitProperty} with the specified Git property name and converter.
     *
     * @param gitPropertyName The name of the Git property.
     *
     * @since 1.0.0
     */
    protected AbstractGitProperty(GitPropertyNames gitPropertyName) {
        this.gitPropertyName = gitPropertyName;
    }

    @Override
    public GitPropertyNames getPropertyName() {
        return gitPropertyName;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Loads Git properties from the given input stream and maps them to their corresponding {@link GitPropertyNames} keys. Missing or blank property entries
     * are populated with {@link #NOT_FOUND}.
     *
     * @param inputStream The input stream from which the Git properties are to be loaded. Cannot be null.
     *
     * @return An unmodifiable map where the keys are {@link GitPropertyNames} representing the property names and the values are the associated property values
     *         (or {@link #NOT_FOUND} for missing/blank entries).
     *
     * @throws IOException If an I/O error occurs while reading from the input stream.
     * @since 1.0.0
     */
    public static Map<GitPropertyNames, Object> loadGitProperties(final InputStream inputStream) throws IOException {
        Properties gitProperties = new Properties();
        gitProperties.load(inputStream);
        Map<GitPropertyNames, Object> gitPropertyMap = new EnumMap<>(GitPropertyNames.class);
        for (Map.Entry<Object, Object> entry : gitProperties.entrySet()) {
            GitPropertyNames name = getProperty(entry.getKey().toString());
            Object value = entry.getValue();
            if (value == null || value.toString().isBlank()) {
                gitPropertyMap.put(name, NOT_FOUND);
            } else {
                gitPropertyMap.put(name, value);
            }
        }
        for (GitPropertyNames name : GitPropertyNames.values()) {
            gitPropertyMap.putIfAbsent(name, NOT_FOUND);
        }
        return Map.copyOf(gitPropertyMap);
    }
}
