package com.github.hypfvieh.javafx.controls;

import java.util.function.Function;

import javafx.scene.control.TableCell;

/**
 * Universal {@link TableCell} to be used with classes enums.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class EnumTableCell<S, T extends Enum<?>> extends TableCell<S, T> {

    private Function<T, String> converter;

    public EnumTableCell(Function<T, String> _converter) {
        converter = _converter;
    }

    @Override
    protected void updateItem(T _item, boolean _empty) {
        super.updateItem(_item, _empty);
        if (_item == null || _empty) {
            setText(null);
        } else {
            setText(converter != null ? converter.apply(_item) : _item.name());
        }
    }

}
