package com.github.hypfvieh.javafx.controls;

import javafx.scene.control.ListCell;

import com.github.hypfvieh.javafx.interfaces.IReadableName;

/**
 * Universal {@link ListCell} to be used with classes implementing {@link IReadableName} interface.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class ReadableNameListCell<T extends IReadableName> extends ListCell<T> {

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
