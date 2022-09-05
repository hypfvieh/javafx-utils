package com.github.hypfvieh.javafx.controls.table;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.github.hypfvieh.javafx.controls.DatePickerDateCellFactory;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;

public class DatePickerTableCell<S> extends TableCell<S, LocalDate> {

    private DatePicker        datePicker;
    private LocalDate         minDate;
    private LocalDate         maxDate;
    private DateTimeFormatter dtFormatter;

    public DatePickerTableCell(LocalDate _minDate, LocalDate _maxDate, DateTimeFormatter _dtFormatter) {
        super();

        minDate = _minDate == null ? LocalDate.MIN : _minDate;
        maxDate = _maxDate == null ? LocalDate.MAX : _maxDate;
        dtFormatter = _dtFormatter == null ? DateTimeFormatter.ISO_DATE : _dtFormatter;
    }

    @Override
    public void updateItem(LocalDate item, boolean empty) {

        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (datePicker != null && getItem() != null) {
                    datePicker.setValue(getItem());
                }
                setText(null);
                setGraphic(datePicker);
            } else {
                setText(getItem() != null ? dtFormatter.format(getItem()) : null);
                setGraphic(null);
            }
        }
    }

    private void createDatePicker() {
        datePicker = new DatePicker();
        if (getItem() != null) {
            datePicker.setValue(getItem());
        }
        datePicker.setEditable(true);
        datePicker.setDayCellFactory(new DatePickerDateCellFactory(minDate, maxDate));
        datePicker.setOnAction((e) -> {
            commitEdit(datePicker.getValue());
        });
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getItem() != null ? dtFormatter.format(getItem()) : null);
        setGraphic(null);
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return;
        }

        if (datePicker == null) {
            createDatePicker();
        }

        datePicker.setValue(getItem());

        super.startEdit();
        setText(null);
        setGraphic(datePicker);

    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

}
