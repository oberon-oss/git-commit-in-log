package eu.oberon.oss.tools.logging.gitid;

import eu.oberon.oss.tools.converters.string.AbstractStringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * A converter for transforming {@link LocalDateTime} objects to and from their {@link String} representations.
 * <p>
 * This converter utilizes the {@link DateTimeFormatter#ISO_DATE_TIME} format for parsing and formatting operations.
 * <p>
 * It extends the {@code AbstractStringConverter} to provide type-safe and reusable conversions for {@link LocalDateTime} values in applications.
 *
 * @author TigerLilly64
 * @since 1.0.0
 *
 */
class LocalDateTimeConverter extends AbstractStringConverter<LocalDateTime> {

    private static final Function<LocalDateTime, String> LOCAL_DATE_TIME_TO_STRING = LocalDateTime::toString;
    private static final Function<String, LocalDateTime> STRING_TO_LOCAL_DATE_TIME = s -> LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);

    /**
     * Constructs a protected instance of {@code LocalDateTimeConverter}.
     * <p>
     * The converter facilitates the transformation of {@link LocalDateTime} objects to their {@link String} representations and vice versa.
     *
     * @since 1.0.0
     */
    protected LocalDateTimeConverter() {
        super(LocalDateTime.class, LOCAL_DATE_TIME_TO_STRING, STRING_TO_LOCAL_DATE_TIME);
    }

}
