package com.github.hypfvieh.javafx.controls.listview;

import javafx.beans.property.SimpleBooleanProperty;

/**
 * ListItem which contains a value and an additonal boolean to e.g. display a Checkbox.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-10-02
 */
public class CheckListItemFx<T> extends AbstractListItem<T> {
    private final SimpleBooleanProperty checked = new SimpleBooleanProperty();


    public CheckListItemFx(T _obj, boolean _checked) {
        super(_obj);
        checked.set(_checked);
    }

    public SimpleBooleanProperty checkedProperty() {
        return checked;
    }

    public boolean isChecked() {
        return checked.get();
    }

    public void setChecked(boolean _check) {
        checked.set(_check);
    }

}
