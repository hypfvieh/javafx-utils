package com.github.hypfvieh.javafx.windows.interfaces;

import javafx.fxml.Initializable;

/**
 * Interface which allows passing objects to a window controller on creation.
 *
 * @param <T> type of object
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface IObjectConsumer<T> {
    /**
     * Set the value in the controller.
     * Use this for setting a non-final member to the passed value.
     * The value will then be present in all methods except {@link Initializable#initialize(java.net.URL, java.util.ResourceBundle)}.
     *
     * @param _value value to set (maybe null)
     */
    void setValue(T _value);
}
