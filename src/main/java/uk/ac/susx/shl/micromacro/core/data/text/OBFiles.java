package uk.ac.susx.shl.micromacro.core.data.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

public class OBFiles {

    private final Path start;

    public OBFiles(Path start) {
        this.start = start;
    }

    private final DateTimeFormatter file2Date = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();

    public Set<Path> getFiles(LocalDate from, LocalDate to) throws IOException {

        LocalDate searchTo = to.minus(2, ChronoUnit.MONTHS);

        Set<Path> paths = new HashSet<>(java.nio.file.Files.walk(start).filter(path -> {
            if(!path.toString().endsWith(".xml")) {
                return false;
            }

            String normalised = path.getFileName().toString().replaceAll("^(\\d{4}\\d{2}\\d{2})\\w?\\.xml", "$1");
            try {

                LocalDate fileDate = LocalDate.parse(normalised, file2Date);
                if(fileDate.isAfter(from) && fileDate.isBefore(searchTo) || fileDate.equals(from) || fileDate.equals(to)) {
                    return true;
                } else {
                    return false;
                }
            } catch(DateTimeParseException e) {
                return false;
            }


        }).collect(Collectors.toList()));


        return paths;
    }
}
