package com.github.hypfvieh.javafx.converter;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import com.github.hypfvieh.javafx.utils.StringHelper;

import javafx.util.StringConverter;

/**
 * Converter for TextFields/Spinner to convert {@link Double} to and from {@link String}.
 *
 * @author hypfvieh
 * @since v11.0.2 - 2022-08-12
 */
public class BigDecimalStringConverterExt extends StringConverter<BigDecimal> {

    private final NumberFormat instance;

    private String decimalDelim;
    private String groupingDelim;

    /**
     * Create new instance using default (current) {@link Locale} and allow negative values.
     */
    public BigDecimalStringConverterExt() {
        this(true, true);
    }

    /**
     * Create new instance using default (current) {@link Locale} and optionally allow negative values.
     * @param _allowNegative true to allow negative values, false otherwise
     * @param _grouping enable/disable decimal grouping (e.g. for german locale: 12345 -&gt; 12.345)
     */
    public BigDecimalStringConverterExt(boolean _allowNegative, boolean _grouping) {
        this(Locale.getDefault(), _allowNegative, _grouping);
    }

    /**
     * Create a new instance with the given {@link Locale} and optionally allow negative values.
     * @param _locale {@link Locale} to use
     * @param _allowNegative true to allow negative values, false otherwise
     * @param _grouping enable/disable decimal grouping (e.g. for german locale: 12345 -&gt; 12.345)
     */
    public BigDecimalStringConverterExt(Locale _locale, boolean _allowNegative, boolean _grouping) {

        instance = NumberFormat.getInstance(_locale);

        decimalDelim = DecimalFormatSymbols.getInstance(_locale).getDecimalSeparator() + "";
        groupingDelim = DecimalFormatSymbols.getInstance(_locale).getGroupingSeparator() + "";
        instance.setGroupingUsed(_grouping);
    }


    /**
     * Convert double to currency String based on the configured {@link Locale}.
     * @param _value double to convert
     *
     * @return Currency string
     */
    @Override
    public String toString(BigDecimal _value) {
        if (_value == null) {
            return "0";
        }

        return instance.format(_value);
    }

    /**
     * Convert given String to {@link Double}.
     * If null or empty String is given, 0.0 will be returned.
     *
     * @param _value value to convert
     * @return Double value
     */
    @Override
    public BigDecimal fromString(String _value) {
        if (_value == null) {
            return BigDecimal.ZERO;
        }

        _value = _value.trim();

        if (_value.length() < 1) {
            return BigDecimal.ZERO;
        }

        _value = _value.replace(groupingDelim, "");
        _value = _value.replace(decimalDelim, ".");

        _value = _value.replaceAll("[\\t|\\s|\\xA0|\\xC2\\xA0]", ""); // replace tabs, spaces and non-breaking spaces
        if (StringHelper.isBlank(_value)) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(_value);
    }

}
