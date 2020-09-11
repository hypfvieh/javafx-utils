package com.github.hypfvieh.javafx.dialogs;

import java.time.LocalDate;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import com.github.hypfvieh.javafx.beans.DateRange;

/**
 * Dialog which shows a datepicker.
 *
 * @author hypfvieh
 * @since v1.0.2 - 2020-09-02
 */
public class DateRangeDialog extends Dialog<DateRange> {

    private final GridPane grid;
    private final Label labelBegin;
    private final Label labelEnd;
    private final Label labelDescription;

    private final DatePicker pickerBegin;
    private final DatePicker pickerEnd;
    private final TextField txtDescription;

    private final LocalDate defaultBegin;
    private final LocalDate defaultEnd;
    private final String defaultDescription;

    public DateRangeDialog(String _title, String _headerText, String _beginLabelText, String _endLabelText,String _descriptionLabelText, LocalDate _defaultBegin, LocalDate _defaultEnd, String _defaultDescription) {
        final DialogPane dialogPane = getDialogPane();

        // -- grid
        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        this.defaultBegin = _defaultBegin == null ? LocalDate.now() : _defaultBegin;
        this.defaultEnd = _defaultEnd == null ? LocalDate.now().plusDays(1) : _defaultEnd;
        this.defaultDescription = _defaultDescription;

        // -- label
        labelBegin = createContentLabel(_beginLabelText);
        labelBegin.setPrefWidth(Region.USE_COMPUTED_SIZE);
        labelBegin.textProperty().bind(dialogPane.contentTextProperty());

        labelEnd = createContentLabel(_endLabelText);
        labelEnd.setPrefWidth(Region.USE_COMPUTED_SIZE);

        labelDescription = createContentLabel(_descriptionLabelText == null ? "" : _descriptionLabelText);
        labelDescription.setPrefWidth(Region.USE_COMPUTED_SIZE);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(_title);
        dialogPane.setHeaderText(_headerText);
        dialogPane.getStyleClass().add("choice-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final double MIN_WIDTH = 150;

        pickerBegin = new DatePicker();
        pickerBegin.setMinWidth(MIN_WIDTH);
        pickerBegin.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(pickerBegin, Priority.ALWAYS);
        GridPane.setFillWidth(pickerBegin, true);
        pickerBegin.setValue(defaultBegin);

        pickerEnd = new DatePicker();
        pickerEnd.setMinWidth(MIN_WIDTH);
        pickerEnd.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(pickerEnd, Priority.ALWAYS);
        GridPane.setFillWidth(pickerEnd, true);
        pickerEnd.setValue(defaultEnd);

        txtDescription = new TextField();
        txtDescription.setMinWidth(MIN_WIDTH);
        txtDescription.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(txtDescription, Priority.ALWAYS);
        GridPane.setFillWidth(txtDescription, true);
        txtDescription.setText(defaultDescription);

        if (_descriptionLabelText == null) {
            labelDescription.setVisible(false);
            txtDescription.setVisible(false);
        }

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? getValue() : null;
        });
    }


    public final DateRange getValue() {
        DateRange x = new DateRange();
        x.setFrom(pickerBegin.getValue());
        x.setUntil(pickerEnd.getValue());
        //x.setDescription(_description);
        return x;
    }

    public final ReadOnlyObjectProperty<LocalDate> beginValueProperty() {
        return pickerBegin.valueProperty();
    }

    public final ReadOnlyObjectProperty<LocalDate> endValueProperty() {
        return pickerEnd.valueProperty();
    }

    public final StringProperty descriptionProperty() {
        return txtDescription.textProperty();
    }

    public final void setBeginDate(LocalDate _item) {
        pickerBegin.setValue(_item);
    }

    public final void setEndDate(LocalDate _item) {
        pickerEnd.setValue(_item);
    }

    public final void setDescription(String _item) {
        txtDescription.setText(_item);
    }

    public final LocalDate getDefaultBegin() {
        return defaultBegin;
    }

    public final LocalDate getDefaultEnd() {
        return defaultEnd;
    }

    public final DatePicker getBeginDatePicker() {
        return pickerBegin;
    }

    public final DatePicker getEndDatePicker() {
        return pickerEnd;
    }

    public final TextField getDescriptionText() {
        return txtDescription;
    }

    static Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(labelBegin, 0, 0);
        grid.add(pickerBegin, 1, 0);

        grid.add(labelEnd, 2, 0);
        grid.add(pickerEnd, 3, 0);

        grid.add(labelDescription, 0, 1);
        grid.add(txtDescription, 1, 1);

        getDialogPane().setContent(grid);

        Platform.runLater(() -> pickerBegin.requestFocus());
    }
}
