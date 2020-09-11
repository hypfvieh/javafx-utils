package com.github.hypfvieh.javafx.converter;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import javafx.util.StringConverter;

import com.github.hypfvieh.javafx.fx.FxConverter;
import com.github.hypfvieh.javafx.utils.StringHelper;

/**
 * Converter for TextFields/Spinner to convert currency formatted {@link BigDecimal} to and from {@link String}.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class BigDecimalCurrencyStringConverter extends StringConverter<BigDecimal> {

    private final NumberFormat currencyInstance;
    private final Pattern currencyPattern;

    private String decimalDelim;
    private String groupingDelim;
    private String currSym;

    /**
     * Create new instance using default (current) {@link Locale} and allow negative values.
     */
    public BigDecimalCurrencyStringConverter() {
        this(true);
    }

    /**
     * Create new instance using default (current) {@link Locale} and optionally allow negative values.
     * @param _allowNegative true to allow negative values, false otherwise
     */
    public BigDecimalCurrencyStringConverter(boolean _allowNegative) {
        this(Locale.getDefault(), _allowNegative);
    }

    /**
     * Create a new instance with the given {@link Locale} and optionally allow negative values.
     * @param _locale {@link Locale} to use
     * @param _allowNegative true to allow negative values, false otherwise
     */
    public BigDecimalCurrencyStringConverter(Locale _locale, boolean _allowNegative) {

        currencyInstance = NumberFormat.getCurrencyInstance(_locale);

        decimalDelim = DecimalFormatSymbols.getInstance(_locale).getDecimalSeparator() + "";
        groupingDelim = DecimalFormatSymbols.getInstance(_locale).getGroupingSeparator() + "";
        currSym = currencyInstance.getCurrency().getSymbol();
        //[\\s|\\xA0|\\xC2\\xA0] ==> Matching space and the evil invisible nonbreaking-space
        currencyPattern = FxConverter.createCurrencyPattern(_allowNegative, _locale);
    }


    /**
     * Convert BigDecimal to currency String based on the configured {@link Locale}.
     * @param _value BigDecimal to convert
     *
     * @return Currency string
     */
    @Override
    public String toString(BigDecimal _value) {
        if (_value == null) {
            return "0";
        }

        return currencyInstance.format(_value);
    }

    /**
     * Convert given String to {@link Double}.
     * If null or empty String is given, 0 will be returned.
     *
     * @param _value value to convert
     * @return BigDecimal value
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

        if (!currencyPattern.matcher(_value).matches()) {
            return BigDecimal.ZERO;
        }

        _value = _value.replace(groupingDelim, "");
        _value = _value.replace(currSym, "");
        _value = _value.replace(decimalDelim, ".");

        _value = _value.replaceAll("[\\t|\\s|\\xA0|\\xC2\\xA0]", ""); // replace tabs, spaces and non-breaking spaces
        if (StringHelper.isBlank(_value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(_value);
    }

}
