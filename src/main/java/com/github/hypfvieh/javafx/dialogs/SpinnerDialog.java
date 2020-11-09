package com.github.hypfvieh.javafx.dialogs;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Dialog to display a spinner.
 *
 * @author hypfvieh
 * @since v1.0.0 - 2019-04-01
 */
public class SpinnerDialog<T extends Number> extends Dialog<T> {

    private final GridPane grid;
    private final Label label;
    private final Spinner<T> spinner;
    private final T initialValue;

    /**
     * Creates a new SpinnerDialog instance.
     *
     * @param _startValue The initial value
     */
    public SpinnerDialog(String _title, String _headerText, String _contentLabelText, SpinnerValueFactory<T> _valueFactory, T _startValue) {
        final DialogPane dialogPane = getDialogPane();

        // -- grid
        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        initialValue = _startValue;

        // -- label
        label = createContentLabel(_contentLabelText);
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(_title);
        dialogPane.setHeaderText(_headerText);
        dialogPane.getStyleClass().add("choice-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // setup IDs so we can click the buttons with TestFX
        dialogPane.lookupButton(ButtonType.OK).setId("btnOk");
        dialogPane.lookupButton(ButtonType.CANCEL).setId("btnCancel");

        final double MIN_WIDTH = 150;

        spinner = new Spinner<>();
        spinner.setId("spinner");

        spinner.setMinWidth(MIN_WIDTH);

        if (_valueFactory != null) {
            spinner.setValueFactory(_valueFactory);
        }

        spinner.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(spinner, Priority.ALWAYS);
        GridPane.setFillWidth(spinner, true);

        if (initialValue != null) {
            spinner.getValueFactory().setValue(initialValue);
        }

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? getSelectedItem() : null;
        });
    }

    /**
     * Returns the current value
     * @return the current value.
     */
    public final T getSelectedItem() {
        return spinner.getValue();
    }

    /**
     * Represents the current value of the SpinnerValueFactory, or null if no value has been set.
     * @return the current value property
     */
    public final ReadOnlyObjectProperty<T> valueProperty() {
        return spinner.getValueFactory().valueProperty();
    }

    /**
     * Sets the current value
     * @param item value to set
     */
    public final void setValue(T item) {
        spinner.getValueFactory().setValue(item);
    }

    /**
     * Returns the default choice that was specified in the constructor.
     * @return the default choice
     */
    public final T getInitialValue() {
        return initialValue;
    }

    public final Spinner<T> getSpinner() {
        return spinner;
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

    /**************************************************************************
     *
     * Private Implementation
     *
     **************************************************************************/

    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(label, 0, 0);
        grid.add(spinner, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> spinner.requestFocus());
    }
}
