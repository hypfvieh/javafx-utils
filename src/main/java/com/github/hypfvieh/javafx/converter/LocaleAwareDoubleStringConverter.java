package com.github.hypfvieh.javafx.converter;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import javafx.util.StringConverter;

/**
 * Converter for String and double which while honor the given/set locale.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class LocaleAwareDoubleStringConverter extends StringConverter<Double> {

    private final NumberFormat numberInstance;
    private final Pattern numberPattern;

    private String decimalDelim;
    private String groupingDelim;

    public LocaleAwareDoubleStringConverter() {
        this(Locale.getDefault());
    }


    public LocaleAwareDoubleStringConverter(Locale _locale) {

        numberInstance = NumberFormat.getNumberInstance();

        decimalDelim = DecimalFormatSymbols.getInstance(_locale).getDecimalSeparator() + "";
        groupingDelim = DecimalFormatSymbols.getInstance(_locale).getGroupingSeparator() + "";
        numberPattern = Pattern.compile("-?(?:[0-9]|" + Pattern.quote(groupingDelim) + ")+" + Pattern.quote(decimalDelim) + "{0,1}[0-9]*");
    }


    @Override
    public String toString(Double _value) {
        if (_value == null) {
            return "0";
        }

        return numberInstance.format(_value);
    }

    @Override
    public Double fromString(String _value) {
        if (_value == null) {
            return 0.0;
        }

        _value = _value.trim();

        if (_value.length() < 1) {
            return 0.0;
        }

        if (!numberPattern.matcher(_value).matches()) {
            return 0.0;
        }

        _value = _value.replace(groupingDelim, "");
        _value = _value.replace(decimalDelim, ".");

        return Double.valueOf(_value);
    }

}
