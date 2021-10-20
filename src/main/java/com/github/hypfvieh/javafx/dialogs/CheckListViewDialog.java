package com.github.hypfvieh.javafx.dialogs;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import com.github.hypfvieh.javafx.controls.listview.CheckListItemFx;

/**
 * Dialog which contains a listview to allow displaying a list of options.
 * All given items will be wrapped in an {@link CheckListItemFx}.
 * It is possible to limit the maximum allowed items which can be selected.
 * Use {@link #setValueConverter(Function)} to provide a function
 * used to create the visible item text in the listview.
 * If no function is given, toString() of the item will be called.
 *
 * @author hypfvieh
 * @since v11.0.1 - 2021-10-20
 */
public class CheckListViewDialog<T> extends Dialog<List<T>> {

    /**************************************************************************
     *
     * Fields
     *
     **************************************************************************/

    private final GridPane grid;
    private final Label label;
    private final ListView<CheckListItemFx<T>> listView;
    private Function<CheckListItemFx<T>, String> valConverter;
    private long maxSelection;

    private final Set<CheckListItemFx<T>> selectedItems = new LinkedHashSet<>();

    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Creates a new dialog without selection limit.
     *
     * @param _title dialog title
     * @param _headerText dialog header text
     * @param _contentLabelText description text
     * @param _choices possible choices
     * @param _defaultChoices default selected choice
     */
    public CheckListViewDialog(String _title, String _headerText, String _contentLabelText, Collection<T> _choices, List<T> _defaultChoices) {
        this(_title, _headerText, _contentLabelText, -1, _choices, _defaultChoices);
    }
    /**
     * Creates a new dialog.
     *
     * @param _title dialog title
     * @param _headerText dialog header text
     * @param _contentLabelText description text
     * @param _maxSelection maximum allow items to be selected
     * @param _choices possible choices
     * @param _defaultChoices default selected choice
     */
    public CheckListViewDialog(String _title, String _headerText, String _contentLabelText, long _maxSelection, Collection<T> _choices, List<T> _defaultChoices) {
        maxSelection = _maxSelection;
        final DialogPane dialogPane = getDialogPane();

        setValueConverter(null);

        // -- grid
        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        // -- label
        label = createContentLabel(_contentLabelText);
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        contentTextProperty().set(_contentLabelText);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(_title);
        dialogPane.setHeaderText(_headerText);
        dialogPane.getStyleClass().add("choice-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final double MIN_WIDTH = 150;

        listView = new ListView<>();
        listView.setCellFactory(p -> {
            CheckBoxListCell<CheckListItemFx<T>> cell = new CheckBoxListCell<>(
                    _item -> {
                        return _item.checkedProperty();
                    }) {

                @Override
                public void updateItem(CheckListItemFx<T> _item, boolean _empty) {
                    super.updateItem(_item, _empty);
                    if (_item != null && !_empty) {
                        setDisable(false);
                        setText(valConverter != null ? valConverter.apply(_item) : String.valueOf(_item));
                    } else {
                        setText("");
                    }
                }

            };

            return cell;
        });

        listView.setMinWidth(MIN_WIDTH);
        if (_choices != null) {
            listView.getItems().addAll(_choices.stream().map(c -> new CheckListItemFx<>(c, false)).collect(Collectors.toList()));

            if (_defaultChoices != null) {
                for (T defChoice : _defaultChoices) {
                    for (CheckListItemFx<T> lvEntry : listView.getItems()) {
                        if (defChoice.equals(lvEntry.getObject())) {
                            lvEntry.setChecked(true);
                        }
                    }
                }
            }
        }

        listView.getItems().stream().forEach(e -> {
            e.checkedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> _observable, Boolean _oldValue, Boolean _newValue) {
                    if (_newValue) {
                        selectedItems.add(e);
                        if (maxSelection > 0 && selectedItems.size() > maxSelection) {
                            e.setChecked(false);
                            listView.refresh();
                        }
                    } else {
                        selectedItems.remove(e);
                    }
                }
            });
        });

        listView.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(listView, Priority.ALWAYS);
        GridPane.setFillWidth(listView, true);

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? getSelectedItems() : null;
        });
    }
    public CheckListViewDialog<T> setValueConverter(Function<CheckListItemFx<T>, String> _converter) {
        if (_converter == null) {
            valConverter = i -> i != null ? i.toString() : null;
        } else {
            valConverter = _converter;
        }
        return this;
    }

    /**
     * Returns the checked items from the dialog
     * @return checked items, maybe empty
     */
    public final List<T> getSelectedItems() {
        return selectedItems.stream().map(e -> e.getObject()).collect(Collectors.toList()); //listView.getItems().stream().filter(s -> s.isChecked()).map(s -> s.getObject()).collect(Collectors.toList());
    }

    public CheckListViewDialog<T> setInitOwner(Window _owner) {
        initOwner(_owner);
        return this;
    }

    static Label createContentLabel(String _text) {
        Label label = new Label(_text);
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
        grid.add(new Label(""), 0, 1);
        grid.add(listView, 0, 2);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> listView.requestFocus());
    }


}
