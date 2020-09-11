package com.github.hypfvieh.javafx.converter;

import javafx.util.StringConverter;

/**
 * IntegerStringConverter which ignores all sorts of delimiters (like . or ,).
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class DecimalDelimiterIgnoringIntegerStringConverter  extends StringConverter<Integer> {

    @Override
    public String toString(Integer _object) {
        return _object == null ? null : String.valueOf(_object).replace(".", "").replace(",", "");
    }

    @Override
    public Integer fromString(String _string) {
        return _string != null ? Integer.valueOf(_string.replace(".", "").replace(",", "")) : null;
    }

}
