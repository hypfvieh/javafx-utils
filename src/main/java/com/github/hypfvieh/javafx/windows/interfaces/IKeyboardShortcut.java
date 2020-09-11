package com.github.hypfvieh.javafx.windows.interfaces;

import java.util.Map;

import javafx.scene.input.KeyCombination;

/**
 * Interface to add support for window-global key shortcuts.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface IKeyboardShortcut {
    /**
     * Returns a Map of KeyCombinations to press and Runnable with the action to perform on keypress.
     * @return Map or null
     */
    Map<KeyCombination, Runnable> getGlobalShortcuts();
}
