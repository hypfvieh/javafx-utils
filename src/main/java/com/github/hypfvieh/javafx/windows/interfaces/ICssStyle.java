package com.github.hypfvieh.javafx.windows.interfaces;

import java.util.List;

/**
 * Interface which allows a implementing class to specify additional CSS style sheet files.
 * Optionally allows to disable the include of the style sheets configured by default.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface ICssStyle {
    /**
     * Returns a list of stylesheets which should be used in
     * addition/as replacement of the default style sheets.
     *
     * @return List, empty List or null
     */
    List<String> getCssStyleFiles();

    default boolean replaceDefaultStyles() {
        return false;
    }
}
