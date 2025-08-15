package com.github.hypfvieh.javafx.converter;

import javafx.util.StringConverter;

public class StringStringConverter extends StringConverter<String> {

    @Override
    public String toString(String _object) {
        return _object;
    }

    @Override
    public String fromString(String _string) {
        return _string;
    }

}
