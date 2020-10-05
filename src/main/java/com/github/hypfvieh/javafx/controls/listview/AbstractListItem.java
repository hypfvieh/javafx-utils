package com.github.hypfvieh.javafx.controls.listview;

import javafx.beans.property.SimpleObjectProperty;

/**
 * Base item for listviews.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-10-02
 */
public class AbstractListItem<T> {
    private final SimpleObjectProperty<T> object = new SimpleObjectProperty<>();

    public AbstractListItem(T _obj) {
        object.set(_obj);
    }

    public SimpleObjectProperty<T> objectProperty() {
        return object;
    }

    public T getObject() {
        return object.get();
    }

    public void setObject(T _obj) {
        object.set(_obj);
    }
}
