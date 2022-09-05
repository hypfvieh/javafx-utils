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
     * Creates a UnaryOperator usable as filter for e.g. Spinner which only allows valid network ports.
     * @param _allowWellKnown allow well-known ports (1-1024)
     *
     * @return {@link UnaryOperator}
     */
    public static UnaryOperator<Change> createPortFilter(boolean _allowWellKnown) {
        return c -> {
            if (c.isContentChange()) {
                if (!c.getControlNewText().matches("[0-9]+")) {
                    if (c.getControlNewText() == null || c.getControlNewText().isEmpty()) {
                        return c;
                    }
                    return null;
                } else {
                    int port = Integer.parseInt(c.getControlNewText());
                    if (port < 0 || port > 65535) {
                        return c;
                    }
                    if (!_allowWellKnown && port <= 1024) {
                        return null;
                    }
                }
            }
            return c;
        };
    }

    /**
     * Creates a UnaryOperator usable as filter for filtering all characters invalid for phone numbers.
     * @param _allowNull true to allow <code>null</code> values.
     *
     * @return {@link UnaryOperator}
     */
    public static UnaryOperator<Change> createPhoneNumberFilter(boolean _allowNull) {
        return c -> {
            if (c.isContentChange()) {
                if (!c.getControlNewText().matches("[0-9\\-+/ ]+")) {
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
        return createDecimalFilter(Locale.getDefault(), _allowNull, true);
    }

    /**
     * A filter to validate decimal patterns.
     *
     * @param _locale locale used to determine decimal separator
     * @param _allowNull allow null or empty values
     * @param _allowNegative true allow negative values
     *
     * @return {@link UnaryOperator}
     */
    public static UnaryOperator<Change> createDecimalFilter(Locale _locale, boolean _allowNull, boolean _allowNegative) {
        char decimalSeparator = DecimalFormatSymbols.getInstance(_locale).getDecimalSeparator();
        Pattern pattern = Pattern.compile(_allowNegative ? "-?" : "" + "[0-9]+\\" + decimalSeparator + "?[0-9]*");
        return c -> {
            if (c.isContentChange()) {
                if (c.getText() == null || c.getControlNewText() == null) {
                    return null;
                } else if (!pattern.matcher(c.getControlNewText()).matches()) {
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
    public static StringConverter<Integer> createNullawareIntegerComboBoxConverter(String _anyTitle) {
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
