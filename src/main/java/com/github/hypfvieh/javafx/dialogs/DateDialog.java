package com.github.hypfvieh.javafx.dialogs;

import java.time.LocalDate;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Dialog which shows a datepicker.
 *
 * @author hypfvieh
 * @since v1.0.2 - 2020-09-02
 */
public class DateDialog extends Dialog<LocalDate> {

    private final GridPane grid;
    private final Label label;
    private final DatePicker picker;
    private final LocalDate defaultChoice;

    public DateDialog(String _title, String _headerText, String _contentLabelText, LocalDate _defaultChoice) {
        final DialogPane dialogPane = getDialogPane();

        // -- grid
        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        this.defaultChoice = _defaultChoice;
        // -- label
        label = createContentLabel(_contentLabelText);
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(_title);
        dialogPane.setHeaderText(_headerText);
        dialogPane.getStyleClass().add("choice-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final double MIN_WIDTH = 150;

        picker = new DatePicker();
        picker.setMinWidth(MIN_WIDTH);
        picker.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(picker, Priority.ALWAYS);
        GridPane.setFillWidth(picker, true);

        if (_defaultChoice == null) {
            picker.setValue(LocalDate.now());
        } else {
            picker.setValue(_defaultChoice);
        }

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? getSelectedItem() : null;
        });
    }


    public final LocalDate getSelectedItem() {
        return picker.getValue();
    }

    public final ReadOnlyObjectProperty<LocalDate> valueProperty() {
        return picker.valueProperty();
    }

    public final void setSelectedItem(LocalDate item) {
        picker.setValue(item);
    }

    public final LocalDate getDefaultChoice() {
        return defaultChoice;
    }

    public final DatePicker getDatePicker() {
        return picker;
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

        grid.add(label, 0, 0);
        grid.add(picker, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> picker.requestFocus());
    }
}
