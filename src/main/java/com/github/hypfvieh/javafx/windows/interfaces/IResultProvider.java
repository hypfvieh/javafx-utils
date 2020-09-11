package com.github.hypfvieh.javafx.windows.interfaces;

/**
 * Interface which allows the controller to return a value on window close.
 *
 * @param <T> type of return value
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface IResultProvider<T> {
    /**
     * Value to provide when window closes.
     * @return value, maybe null
     */
    T getValue();

    /**
     * Return a value if window is getting closed by 'X' icon of the window manager.
     * @return true to return value when getValue() is called, false otherwise
     */
    default boolean returnValueOnSystemClose()  {
        return false;
    };

}
