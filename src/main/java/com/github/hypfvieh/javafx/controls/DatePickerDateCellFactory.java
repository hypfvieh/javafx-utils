package com.github.hypfvieh.javafx.controls;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.Objects;

public class DatePickerDateCellFactory implements Callback<DatePicker, DateCell> {

    private final LocalDate minDate;
    private final LocalDate maxDate;

    public DatePickerDateCellFactory(LocalDate _minDate, LocalDate _maxDate) {
        minDate = Objects.requireNonNull(_minDate, "Min date required");
        maxDate = Objects.requireNonNull(_maxDate, "Max date required");
    }

    @Override
    public DateCell call(DatePicker _param) {
        return new DateCell() {

            @Override
            public void updateItem(LocalDate _item, boolean _empty) {
                super.updateItem(_item, _empty);
                if (_item != null && (_item.isBefore(minDate) || _item.isAfter(maxDate))) {
                    setDisable(true);
                }
            }

        };
    }
}
