package com.github.hypfvieh.javafx.fx;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.scene.control.TextFormatter.Change;
import javafx.util.StringConverter;

public class FxConverter {
    private static final Pattern CURRENCY_PATTERN = createCurrencyPattern(true, Locale.getDefault());

    /**
     * Creates a regex matching currency strings.
     *
     * @param _allowNegative allow negative values
     * @param _locale the locale to use for currency and delimiters
     * @return Pattern
     */
    public static Pattern createCurrencyPattern(boolean _allowNegative, Locale _locale) {
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(_locale);

        String decimalDelim = DecimalFormatSymbols.getInstance(_locale).getDecimalSeparator() + "";
        String groupingDelim = DecimalFormatSymbols.getInstance(_locale).getGroupingSeparator() + "";
        String currSym = currencyInstance.getCurrency().getSymbol();
        //[\\s|\\xA0|\\xC2\\xA0] ==> Matching space and the evil invisible nonbreaking-space
        return Pattern.compile(String.format("^%s(?:[0-9|%s])*%s?[0-9]*[\\s|\\t|\\xA0|\\xC2\\xA0]?%s?$", _allowNegative ? "-?" : "", Pattern.quote(groupingDelim), Pattern.quote(decimalDelim), Pattern.quote(currSym)));
    }

    /**
     * Creates a UnaryOperator usable as filter for e.g. Spinner.
     * @param _allowNull true to allow <code>null</code> values.
     *
     * @return {@link UnaryOperator}
     */
    public static UnaryOperator<Change> createIntegerFilter(boolean _allowNull) {
        return c -> {
            if (c.isContentChange()) {
                if (!c.getControlNewText().matches("-?[0-9]+")) {
                    if (_allowNull && c.getControlNewText() == null || c.getControlNewText().isEmpty()) {
                        return c;
                    }
                    return null;
                }
            }
            return c;
        };
    }

    /**
     * A filter which only allows double patterns.
     * @param _allowNull allow null or empty values
     *
     * @return {@link UnaryOperator}
     */
    public static UnaryOperator<Change> createDoubleFilter(boolean _allowNull) {
        char decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
        return c -> {
            if (c.isContentChange()) {
                if (c.getText() == null || c.getControlNewText() == null) {
                    return null;
                } else if (!c.getControlNewText().matches("-?[0-9]+\\" + decimalSeparator + "?[0-9]*")) {
                    if (_allowNull && c.getControlNewText() == null || c.getControlNewText().isEmpty()) {
                        return c;
                    }
                    return null;
                }
            }
            return c;
        };
    }

    /**
     * A filter which only allows currency patterns.
     * @param _allowNull allow null or empty values
     *
     * @return {@link UnaryOperator}
     */
    public static UnaryOperator<Change> createCurrencyFilter(boolean _allowNull) {
        return c -> {
            if (c.isContentChange()) {
                if (_allowNull && c.getControlNewText() == null || c.getControlNewText().isEmpty()) {
                    return c;
                }
                if (!CURRENCY_PATTERN.matcher(c.getControlNewText()).matches()) {
                    return null;
                }
            }
            return c;
        };
    }

    /**
     * Creates a StringConverter which will show the given placeholder when value is negative.
     *
     * @param _negativePlaceholder placeholder to show
     * @return StringConverter for Integer values
     */
    public static StringConverter<Integer> createPlaceholderIntegerStringConverter(String _negativePlaceholder) {
        return new StringConverter<>() {

            @Override
            public String toString(Integer _year) {
                return (_year == null || _year < 0) ? _negativePlaceholder : String.valueOf(_year);
            }

            @Override
            public Integer fromString(String _string) {
                if (_string != null && _string.matches("^-?[0-9]+$")) {
                    return Integer.valueOf(_string);
                }
                return -1;
            }
        };
    }


    /**
     * Converts the given {@link LocalDate} to a legacy {@link Date} object.
     *
     * @param _dateToConvert Localdate to convert
     * @return Date
     */
    public static Date convertToDateViaInstant(LocalDate _dateToConvert) {
        return java.util.Date.from(_dateToConvert.atStartOfDay()
          .atZone(ZoneId.systemDefault())
          .toInstant());
    }

    /**
     * Create a Converter for years which will handle null and negative values.
     *
     * @param _anyTitle String to show when value is smaller than 0 or null (cannot be null!)
     * @return Converter
     */
    public static StringConverter<Integer> createNullawareComboBoxConverter(String _anyTitle) {
        Objects.requireNonNull(_anyTitle, "Null placeholder text cannot be null");
        return new StringConverter<>() {

            @Override
            public String toString(Integer _year) {
                return (_year == null || _year <= -1) ? _anyTitle : String.valueOf(_year);
            }

            @Override
            public Integer fromString(String _string) {
                if (_string != null && _string.matches("^-?[0-9]+$")) {
                    return Integer.valueOf(_string);
                }
                return -1;
            }
        };
    }

}
