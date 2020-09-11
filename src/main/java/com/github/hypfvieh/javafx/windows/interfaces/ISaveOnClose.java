package com.github.hypfvieh.javafx.windows.interfaces;

/**
 * Interface which will execute a method when a window is closed for e.g. saving data.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface ISaveOnClose extends AutoCloseable {
    /**
     * Action to execute when window is closed.
     * @return true to continue window closing, false to interrupt window closing
     */
    boolean saveAndClose();

}
