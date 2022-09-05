package com.github.hypfvieh.javafx.windows.interfaces;

/**
 * Interface which marks the window as dimension and position being saved/restored on close/creation.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface ISaveWindowPreferences {
    /**
     * Specifies which window information will be saved/restored.
     * @return {@link WindowData} or null
     */
    WindowData saveWindowPreferences();
}
