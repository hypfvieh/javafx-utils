package com.github.hypfvieh.javafx.interfaces;

/**
 * Interface for providing a human readable String for an object for displaying on the UI.
 * 
 * @author hypfvieh
 * @since v1.0.1 - 2020-02-09
 */
public interface IReadableName {
    /**
     * The human readable representation of the object implementing this interface
     * @return String, maybe null
     */
    String getReadable();
}
