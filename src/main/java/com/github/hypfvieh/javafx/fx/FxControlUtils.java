package com.github.hypfvieh.javafx.fx;

import java.util.Objects;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Utility to manipulate certain JavaFX controls.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class FxControlUtils {

    /**
     * Replace column header with a label including a tooltip.
     *
     * @param _colText column header text
     * @param _colTooltip tooltip text
     * @param _column column to modify header
     */
    public static void setColumnHeader(String _colText, String _colTooltip, TableColumn<?, ?> _column) {
        _column.setText("");
        Label colLabel = new Label(_colText);
        colLabel.setTooltip(new Tooltip(_colTooltip));
        _column.setGraphic(colLabel);
    }

    /**
     * Scrolls the table until the given index is visible.
     * Optionally positions the index to the middle of the table if possible.
     * Possible means, if item was on top or bottom and there are no more items
     * before or after this item table will not be scrolled.
     *
     * @param _tableView table view to scroll
     * @param _index to be shown
     * @param _center center index instead of showing it on top or bottom (depending on direction)
     */
    public static void scrollTableViewToIndex(TableView<?> _tableView, int _index, boolean _center){
        Objects.requireNonNull(_tableView);

        TableViewSkin<?> tableSkin = (TableViewSkin<?>) _tableView.getSkin();
        VirtualFlow<?> virtualFlow = tableSkin.getChildren() == null || tableSkin.getChildren().size() < 2 ? null : (VirtualFlow<?>) tableSkin.getChildren().get(1);

        if (virtualFlow == null) {
            _tableView.scrollTo(_index);
        }

        int first = virtualFlow.getFirstVisibleCell().getIndex();
        int last = virtualFlow.getLastVisibleCell().getIndex();

        // get the item from top or bottom
        if (_index <= first){
            while (_index <= first && virtualFlow.scrollPixels(-5) < 0){
                first = virtualFlow.getFirstVisibleCell().getIndex();
            }

            if (_center) {
                int firstVis = virtualFlow.getFirstVisibleCell().getIndex();
                int lastVis = virtualFlow.getLastVisibleCell().getIndex();

                int calc = firstVis - ((lastVis - firstVis) / 2);

                if (calc < 0) { // already on top or near top
                    return;
                }

                double sum = 0;
                for (int i = calc; i < first; i++) {
                    sum += virtualFlow.getCell(i).getHeight();
                }

                virtualFlow.scrollPixels(sum*-1);
            }
        } else {
            while (_index >= last && virtualFlow.scrollPixels(5) > 0){
                last = virtualFlow.getLastVisibleCell().getIndex();
            }

            if (_center) {
                int firstVis = virtualFlow.getFirstVisibleCell().getIndex();
                int lastVis = virtualFlow.getLastVisibleCell().getIndex();

                int calc = lastVis - ((lastVis - firstVis) / 2);

                if (calc < 0) {
                    return;
                }

                double sum = 0;
                for (int i = calc; i < last; i++) {
                    sum += virtualFlow.getCell(i).getHeight();
                }

                virtualFlow.scrollPixels(sum);
            }
        }
    }

    /**
     * Adds a listener to {@link DatePicker#focusedProperty()} to commit changes in date picker fields when it loosed focus.
     * <p>
     * This is required if you want to commit changes made in the date picker by manually entering the date
     * and not using the button or pressing enter after edit.
     *
     * @param _picker date picker to add listener to
     */
    public static void setDatePickerAutoCommit(DatePicker _picker) {
        if (_picker == null) {
            return;
        }
        _picker.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
            if (!newValue){
                _picker.setValue(_picker.getConverter().fromString(_picker.getEditor().getText()));
            }
        });
    }
}
