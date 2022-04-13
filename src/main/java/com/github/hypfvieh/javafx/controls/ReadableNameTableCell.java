package com.github.hypfvieh.javafx.controls;

import com.github.hypfvieh.javafx.interfaces.IReadableName;

import javafx.scene.control.TableCell;

/**
 * Universal {@link TableCell} to be used with classes implementing {@link IReadableName} interface.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class ReadableNameTableCell<S, T extends IReadableName> extends TableCell<S, T> {

    @Override
    protected void updateItem(T _item, boolean _empty) {
        super.updateItem(_item, _empty);
        if (_item == null || _empty) {
            setText(null);
        } else {
            setText(_item.getReadable());
        }
    }

}
