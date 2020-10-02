package com.github.hypfvieh.javafx.controls.tables;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * Utility to enable copy and paste features on table views.<br>
 * Uses two Maps with formatters to map String to underlying object or map Object to String.<br>
 * <br>
 * Every formatter in the map will be used for the column index it is specified for.<br>
 * Column indices starts with 0.<br>
 * <br>
 * <b>For converting an object to String for copying:</b><br>
 * The provided lambda function will receive the value of of the observerable field from the table row/column if the value
 * is not null.<br>
 * It is expected to return a string representation of the given value, if null is returned, null will be converted to String, so use empty String if cell should be empty.
 * <br>
 * If no formatter for the given cell can be found in the {@link #copyColumnFormatters} Map, object.toString() is used.
 * <br><br>
 * <b>Converting from String to Object:</b><br>
 * The provided lambda function will receive the String value from the clipboard for the column, it may be incompatible
 * with the expected format, so please check carefully if you are able to convert the value. If not return null.<br>
 * It is possible that not every formatter is called if the received String does not contain as many columns as expected by the table.
 * <br>
 * Pasting will only fill as much rows as rows are present from the starting from current selected row.<br>
 * Additional rows only be added if a itemFactory is provided during construction (used to create new empty lines).
 * <br><br>
 * The copy/paste format is tab-separated (\t) columns, lines are expected to end with line-break (\n).
 *
 * @param <S> type of the given {@link TableView}
 *
 * @author hypfvieh
 * @version 1.0.6 - 2019-10-01
 **/
public class TableViewCopyAndPaste<S> {

    private final Map<Integer, Function<Object, String>> copyColumnFormatters;
    private final Map<Integer, Function<String, Object>> pasteColumnFormatters;

    private Supplier<S> itemFactory;

    private boolean enablePaste;
    private boolean allowPasteAtTheEnd;

    /**
     * Create a new {@link TableViewCopyAndPaste} object.
     */
    private TableViewCopyAndPaste() {
        copyColumnFormatters = new HashMap<>();
        pasteColumnFormatters = new HashMap<>();
    }

    /**
     * Create a new utility instance.
     * @param _tblViewGenericClass class used in the {@link TableView}
     * @param <S> type of table view content
     * @return this
     */
    public static <S> TableViewCopyAndPaste<S> create(Class<S> _tblViewGenericClass) {
        return new TableViewCopyAndPaste<>();
    }

    /**
     * Configure ItemFactory for creating new lines. Use null to disable.
     * @param _itemFactory itemfactory to use
     * @return this for chaining
     */
    public TableViewCopyAndPaste<S> setItemFactory(Supplier<S> _itemFactory) {
        itemFactory = _itemFactory;
        return this;
    }

    /**
     * Enable/disable paste support on table.
     * @param _enablePaste true to enable paste feature, false to disable
     * @return this for chaining
     */
    public TableViewCopyAndPaste<S> setEnablePaste(boolean _enablePaste) {
        enablePaste = _enablePaste;
        return this;
    }

    /**
     * Enable/disable pasting results at the end of the table if no selection has been applied.
     *
     * @param _allowPasteAtTheEnd true to add pasted content to table ending if no selection. Only used if paste is enabled.
     * @return this for chaining
     */
    public TableViewCopyAndPaste<S> setAllowPasteAtTheEnd(boolean _allowPasteAtTheEnd) {
        allowPasteAtTheEnd = _allowPasteAtTheEnd;
        return this;
    }

    /**
     * Add a formatter for copying cells to clipboard.
     * @param _colNo column index (0-based)
     * @param _formatFunc formatting lambda
     * @return this for chaining
     */
    public TableViewCopyAndPaste<S> addCopyColumnFormatter(int _colNo, Function<Object, String> _formatFunc) {
        Objects.requireNonNull(_formatFunc);
        copyColumnFormatters.put(_colNo, _formatFunc);
        return this;
    }

    /**
     * Add a formatter for pasting clipboard values to table.
     * @param _colNo column index (0-based)
     * @param _formatFunc formatting lambda
     * @return this for chaining
     */
    public TableViewCopyAndPaste<S> addPasteColumnFormatter(int _colNo, Function<String, Object> _formatFunc) {
        Objects.requireNonNull(_formatFunc);
        pasteColumnFormatters.put(_colNo, _formatFunc);
        return this;
    }

    /**
     * Install the created copy and paste listener on the given table.
     * @param _tableView {@link TableView}, never null
     */
    public void install(TableView<S> _tableView) {
        Objects.requireNonNull(_tableView);

        _tableView.setOnKeyPressed(new TableKeyEventHandler(_tableView));
    }

    /**
     * Get the selected cells/rows and copy them to clipboard.
     *
     * @param _tableView to install utility to, never null!
     */
    @SuppressWarnings("rawtypes")
    void copySelectionToClipboard(TableView<S> _tableView) {

        Objects.requireNonNull(_tableView);
        Set<Integer> rows = new TreeSet<>();

        for (TablePosition tblPos : _tableView.getSelectionModel().getSelectedCells()) {
            rows.add(tblPos.getRow());
        }

        final StringBuilder strb = new StringBuilder();
        boolean firstRow = true;
        for (final Integer row : rows) {
            if (!firstRow) {
                strb.append('\n');
            }
            firstRow = false;
            boolean firstCol = true;
            for (int i = 0; i < _tableView.getColumns().size(); i++) {
                TableColumn<?, ?> column = _tableView.getColumns().get(i);

                if (!firstCol) {
                    strb.append('\t');
                }
                firstCol = false;

                ObservableValue<?> observableValue = column.getCellObservableValue(row);

                // provide empty string for nulls
                if (observableValue == null || observableValue.getValue() == null) {
                    strb.append("");
                } else {
                    if (copyColumnFormatters.containsKey(i)) { // use formatter if we have any
                        strb.append(copyColumnFormatters.get(i).apply(observableValue.getValue()));
                    } else { // use toString() if no formatter available
                        strb.append(observableValue.getValue().toString());
                    }
                }
            }
        }

        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(strb.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    /**
     * Paste the clipboard content to the given table view.
     *
     * @param _tableView {@link TableView} to install utility to, never null!
     */
    @SuppressWarnings("unchecked")
    void pasteFromClipboard(TableView<S> _tableView) {
        Objects.requireNonNull(_tableView);

        int pasteCellRow = 0;
        int pasteCellCol = 0;

        // do nothing if pasting at the end is not allowed and there is no selection
        if (!allowPasteAtTheEnd && _tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            return;
        } else if (allowPasteAtTheEnd && _tableView.getSelectionModel().getSelectedCells().isEmpty()) { // paste allowed, but no rows left
            pasteCellRow = _tableView.getItems().size() - 1;
            pasteCellCol = 0;

        } else { // pasting allowed and we have a selected row
            @SuppressWarnings("rawtypes")
            TablePosition pasteCellPosition = _tableView.getSelectionModel().getSelectedCells().get(0);
            pasteCellRow = pasteCellPosition.getRow();
            pasteCellCol = pasteCellPosition.getColumn();
        }

        String pasteString = Clipboard.getSystemClipboard().getString();
        pasteString = pasteString.replace("\r\n", "\n"); // change from windows line ending to unix line ending
        int rowClipboard = -1;

        StringTokenizer rowTokenizer = new StringTokenizer(pasteString, "\n");
        while (rowTokenizer.hasMoreTokens()) {

            rowClipboard++;

            String rowString = rowTokenizer.nextToken();

            StringTokenizer columnTokenizer = new StringTokenizer(rowString, "\t");

            int colClipboard = -1;

            while (columnTokenizer.hasMoreTokens()) {

                colClipboard++;

                // get next cell from clipboard
                String clipboardCellContent = columnTokenizer.nextToken();

                // calculate position in table cell
                int rowTable = pasteCellRow + rowClipboard;
                int colTable = pasteCellCol + colClipboard;

                if (rowTable >= _tableView.getItems().size()) {
                    if (itemFactory != null) { // if we have an itemFactory use it to create new lines
                        _tableView.getItems().add(itemFactory.get());
                    } else { // otherwise skip
                        continue;
                    }
                }

                // skip columns which are larger than the count of colums in table
                if (colTable >= _tableView.getColumns().size()) {
                    continue;
                }

                // get cell data
                TableColumn<S, ?> tableColumn = _tableView.getColumns().get(colTable);
                ObservableValue<?> observableValue = tableColumn.getCellObservableValue(rowTable);

                // if value is writeable, write it back to the table
                if (observableValue instanceof WritableValue) {
                    if (pasteColumnFormatters.containsKey(colTable)) {
                        ((WritableValue<Object>) observableValue).setValue(pasteColumnFormatters.get(colTable).apply(clipboardCellContent));
                    }
                }
            }
        }
    }

    /**
     * EventHandler which will be installed on the table cell to catch copy and paste shortcuts.
     */
    class TableKeyEventHandler implements EventHandler<KeyEvent> {

        KeyCodeCombination copyKeyCodeCompination  = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        KeyCodeCombination pasteKeyCodeCompination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_ANY);
        private TableView<S> tableView;

        public TableKeyEventHandler(TableView<S> _tableView) {
            tableView = _tableView;
        }

        @Override
        public void handle(final KeyEvent keyEvent) {
            if (copyKeyCodeCompination.match(keyEvent)) {
                if (keyEvent.getSource() instanceof TableView) {
                    // copy selected table content to clipboard
                    copySelectionToClipboard(tableView);
                    // consume event (has been handled)
                    keyEvent.consume();
                }
            } else if (pasteKeyCodeCompination.match(keyEvent)) {
                if (keyEvent.getSource() instanceof TableView) {
                    if (!enablePaste) {
                        return;
                    }
                    // paste from clipboard to table
                    pasteFromClipboard(tableView);
                    // consume event (has been handled)
                    keyEvent.consume();
                }
            }
        }
    }
}
