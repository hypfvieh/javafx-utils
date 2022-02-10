package com.github.hypfvieh.javafx.formatter;

import java.lang.System.Logger.Level;
import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Objects;

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

    private final DatePicker picker;
    private final DateTimeFormatter formatter;

    public BirthdayDateFormatter(DatePicker _picker, DateTimeFormatter _dateFormatter) {
        picker = Objects.requireNonNull(_picker, "DatePicker cannot be null");
        formatter = Objects.requireNonNull(_dateFormatter, "Dateformatter cannot be null");
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
                Locale locale = Locale.getDefault(Locale.Category.FORMAT);
                Chronology chrono = picker.getChronology();
                String pattern =
                    DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT,
                                                                         null, chrono, locale);
                String prePattern = pattern.substring(0, pattern.indexOf("y"));
                String postPattern = pattern.substring(pattern.lastIndexOf("y")+1);
                int baseYear = LocalDate.now().getYear() - 99;

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
