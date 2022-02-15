package com.github.hypfvieh.javafx.formatter;

import java.lang.System.Logger.Level;
import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Objects;

import com.github.hypfvieh.javafx.utils.StringHelper;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;


/**
 * StringConverter to convert between Date and String.
 * Uses custom date format, and will use 1900 as base year when two digit year is typed in.
 *
 * @author hypfvieh
 * @since v1.0.0 - 2019-05-20
 */
public class BirthdayDateFormatter extends StringConverter<LocalDate> {

    private final DateTimeFormatter formatter;
    private final String datePattern;
    private final String prePattern;
    private final String postPattern;
    private final int baseYear;
    private final Chronology chrono;

    public BirthdayDateFormatter(DatePicker _picker, String _datePattern) {
        datePattern = StringHelper.requireNonBlank(_datePattern, "Date pattern required");
        
        chrono = Objects.requireNonNull(_picker, "DatePicker cannot be null").getChronology();
        formatter = DateTimeFormatter.ofPattern(_datePattern);
        
        prePattern = datePattern.substring(0, datePattern.indexOf("y"));
        postPattern = datePattern.substring(datePattern.lastIndexOf("y")+1);
        baseYear = LocalDate.now().getYear() - 99;
    }

    @Override
    public String toString(LocalDate _object) {
        if (_object == null) {
            return null;
        }
        return formatter.format(_object);
    }

    @Override
    public LocalDate fromString(String _string) {
        try {
            if (_string != null && !_string.isEmpty()) {
                DateTimeFormatter df = new DateTimeFormatterBuilder()
                            .parseLenient()
                            .appendPattern(prePattern)
                            .appendValueReduced(ChronoField.YEAR, 2, 2, baseYear)
                            .appendPattern(postPattern)
                            .toFormatter();
                return LocalDate.from(chrono.date(df.parse(_string)));
            } else {
                return null;
            }
        } catch (DateTimeParseException _ex) {
            System.getLogger(getClass().getName()).log(Level.DEBUG, "Error converting date", _ex);
            return null;
        }
    }

}
