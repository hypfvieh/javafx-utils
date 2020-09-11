package com.github.hypfvieh.javafx.beans;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.hypfvieh.javafx.utils.StringHelper;

/**
 * Defines a single date (from and until has the same date) or date range with a description.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class DateRange implements Comparable<DateRange> {
    private DateTimeFormatter dtFormat;

    private LocalDate from;
    private LocalDate until;

    private String description;

    public DateRange() {
        this(null, null, null, null);
    }

    public DateRange(LocalDate _from, LocalDate _until) {
        this(_from, _until, null, null);
    }

    public DateRange(LocalDate _from, LocalDate _until, String _description) {
        this(_from, _until, _description, null);
    }

    public DateRange(LocalDate _from, LocalDate _until, String _description, DateTimeFormatter _dtFormat) {
        dtFormat = _dtFormat == null ? DateTimeFormatter.ISO_DATE : _dtFormat;
        from = _from;
        until = _until;
        description = _description;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate _from) {
        from = _from;
    }

    public LocalDate getUntil() {
        return until;
    }

    public void setUntil(LocalDate _until) {
        until = _until;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String _description) {
        description = _description;
    }

    public DateTimeFormatter getDtFormat() {
        return dtFormat;
    }

    public void setDtFormat(DateTimeFormatter _dtFormat) {
        dtFormat = _dtFormat;
    }

    @Override
    public DateRange clone() {
        DateRange dateRange = new DateRange();
        dateRange.setFrom(LocalDate.from(from));
        dateRange.setUntil(LocalDate.from(until));
        dateRange.setDescription(description);

        return dateRange;
    }

    /**
     * Get all dates between start date and end date.
     *
     * @return list of dates
     */
    public List<LocalDate> getDates() {
        long between = ChronoUnit.DAYS.between(from, until);

        return IntStream.iterate(0, i -> i + 1)
            .limit(between)
            .mapToObj(i -> from.plusDays(i))
            .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, from, until);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DateRange other = (DateRange) obj;
        return Objects.equals(description, other.description) && Objects.equals(from, other.from)
                && Objects.equals(until, other.until);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (from != null && from.equals(until)) {
            sb.append(dtFormat.format(from));
        } else {
            sb.append(dtFormat.format(from));
            sb.append(" - ");
            sb.append(dtFormat.format(until));
        }

        if (!StringHelper.isBlank(description)) {
            sb.append(" (").append(description).append(")");
        }

        return sb.toString();
    }

    @Override
    public int compareTo(DateRange _o) {
        if (from == null || _o == null) {
            return 0;
        }

        if (from != null && _o.getFrom() != null) {
            int compareToFrom = from.compareTo(_o.getFrom());
            if (compareToFrom != 0) {
                return compareToFrom;
            }
        }

        if (until != null && _o.getUntil() != null) {
            int compareToUntil = until.compareTo(_o.getUntil());
            if (compareToUntil != 0) {
                return compareToUntil;
            }
        }

        return 0;
    }

}