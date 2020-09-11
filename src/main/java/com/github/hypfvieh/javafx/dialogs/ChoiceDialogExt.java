package com.github.hypfvieh.javafx.dialogs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Extended copied version of {@link ChoiceDialog}.
 * Allows modifying the provided Combobox to e.g. disable certain entries.
 *
 * @author hypfvieh
 * @since v1.0.0 - 2019-04-01
 */
public class ChoiceDialogExt<T> extends Dialog<T> {

    /**************************************************************************
     *
     * Fields
     *
     **************************************************************************/

    private final GridPane grid;
    private final Label label;
    private final ComboBox<T> comboBox;
    private final T defaultChoice;



    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Creates a new ChoiceDialog instance with the first argument specifying the
     * default choice that should be shown to the user, and all following arguments
     * considered a varargs array of all available choices for the user. It is
     * expected that the defaultChoice be one of the elements in the choices varargs
     * array. If this is not true, then defaultChoice will be set to null and the
     * dialog will show with the initial choice set to the first item in the list
     * of choices.
     *
     * @param defaultChoice The item to display as the pre-selected choice in the dialog.
     *        This item must be contained within the choices varargs array.
     * @param choices All possible choices to present to the user.
     */
    public ChoiceDialogExt(String _title, String _headerText, String _contentLabelText, T defaultChoice,  @SuppressWarnings("unchecked") T... choices) {
        this(_title, _headerText, _contentLabelText, defaultChoice,
             choices == null ? Collections.emptyList() : Arrays.asList(choices));
    }

    /**
     * Creates a new ChoiceDialog instance with the first argument specifying the
     * default choice that should be shown to the user, and the second argument
     * specifying a collection of all available choices for the user. It is
     * expected that the defaultChoice be one of the elements in the choices
     * collection. If this is not true, then defaultChoice will be set to null and the
     * dialog will show with the initial choice set to the first item in the list
     * of choices.
     *
     * @param defaultChoice The item to display as the pre-selected choice in the dialog.
     *        This item must be contained within the choices varargs array.
     * @param choices All possible choices to present to the user.
     */
    public ChoiceDialogExt(String _title, String _headerText, String _contentLabelText, T defaultChoice, Collection<T> choices) {
        final DialogPane dialogPane = getDialogPane();

        // -- grid
        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

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

        comboBox = new ComboBox<T>();
        comboBox.setMinWidth(MIN_WIDTH);
        if (choices != null) {
            comboBox.getItems().addAll(choices);
        }
        comboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(comboBox, Priority.ALWAYS);
        GridPane.setFillWidth(comboBox, true);

        this.defaultChoice = comboBox.getItems().contains(defaultChoice) ? defaultChoice : null;

        if (defaultChoice == null) {
            comboBox.getSelectionModel().selectFirst();
        } else {
            comboBox.getSelectionModel().select(defaultChoice);
        }

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? getSelectedItem() : null;
        });
    }



    /**************************************************************************
     *
     * Public API
     *
     **************************************************************************/

    /**
     * Returns the currently selected item in the dialog.
     * @return the currently selected item
     */
    public final T getSelectedItem() {
        return comboBox.getSelectionModel().getSelectedItem();
    }

    /**
     * Returns the property representing the currently selected item in the dialog.
     * @return the currently selected item property
     */
    public final ReadOnlyObjectProperty<T> selectedItemProperty() {
        return comboBox.getSelectionModel().selectedItemProperty();
    }

    /**
     * Sets the currently selected item in the dialog.
     * @param item The item to select in the dialog.
     */
    public final void setSelectedItem(T item) {
        comboBox.getSelectionModel().select(item);
    }

    /**
     * Returns the list of all items that will be displayed to users. This list
     * can be modified by the developer to add, remove, or reorder the items
     * to present to the user.
     * @return the list of all items that will be displayed to users
     */
    public final ObservableList<T> getItems() {
        return comboBox.getItems();
    }

    /**
     * Returns the default choice that was specified in the constructor.
     * @return the default choice
     */
    public final T getDefaultChoice() {
        return defaultChoice;
    }

    public final ComboBox<T> getComboBox() {
        return comboBox;
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
        grid.add(comboBox, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> comboBox.requestFocus());
    }
}
