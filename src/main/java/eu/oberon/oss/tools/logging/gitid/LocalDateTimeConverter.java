package eu.oberon.oss.tools.logging.gitid;

import eu.oberon.oss.tools.converters.string.AbstractStringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

class LocalDateTimeConverter extends AbstractStringConverter<LocalDateTime> {

    private static final Function<LocalDateTime, String> LOCAL_DATE_TIME_TO_STRING = LocalDateTime::toString;
    private static final Function<String, LocalDateTime> STRING_TO_LOCAL_DATE_TIME = s -> LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);

    protected LocalDateTimeConverter() {
        super(LocalDateTime.class, LOCAL_DATE_TIME_TO_STRING, STRING_TO_LOCAL_DATE_TIME);
    }

}
