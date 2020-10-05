package com.github.hypfvieh.javafx.converter;

import javafx.util.StringConverter;

/**
 * Integer/String converter which handles null values without throwing exceptions.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-10-02
 */
public class IntegerStringConverterExt extends StringConverter<Integer> {

    @Override
    public Integer fromString(String value) {
        // If the specified value is null or zero-length, return null
        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.length() < 1) {
            return null;
        }

        // is it a numeric value (optionally negative)
        if (!value.matches("^-?[0-9]+$")) {
            return 0;
        }

        return Integer.valueOf(value);
    }

    @Override
    public String toString(Integer value) {
        // If the specified value is null, return a zero-length String
        if (value == null) {
            return "";
        }

        return (Integer.toString(value.intValue()));
    }
}
