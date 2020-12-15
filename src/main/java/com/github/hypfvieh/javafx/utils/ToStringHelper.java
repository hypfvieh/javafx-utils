package com.github.hypfvieh.javafx.utils;

import java.time.format.DateTimeFormatter;

import com.github.hypfvieh.javafx.beans.DateRange;

/**
 * Utility class containing toString() variants for various classes.
 *
 * @author hypfvieh
 * @since v11.0.1 - 2020-12-10
 */
public final class ToStringHelper {

    private ToStringHelper() {}

    /**
     * Formats a {@link DateRange} object with a given date formatter and optional description.
     * If no date formatter is given, {@link DateTimeFormatter#ISO_DATE} is used.
     *
     * @param _dt date range object
     * @param _formatter formatter to use for date format
     *
     * @return String or null if input was null
     */
    public static String formatDateRange(DateRange _dt, DateTimeFormatter _formatter) {
        if (_dt == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        DateTimeFormatter formatter = _formatter == null ? DateTimeFormatter.ISO_DATE : _formatter;

        if (_dt.getFrom() != null && _dt.getFrom().equals(_dt.getUntil())) {
            sb.append(formatter.format(_dt.getFrom()));
        } else {
            sb.append(formatter.format(_dt.getFrom()));
            sb.append(" - ");
            sb.append(formatter.format(_dt.getUntil()));
        }

        if (!StringHelper.isBlank(_dt.getDescription())) {
            sb.append(" (").append(_dt.getDescription()).append(")");
        }

        return sb.toString();
    }
}
