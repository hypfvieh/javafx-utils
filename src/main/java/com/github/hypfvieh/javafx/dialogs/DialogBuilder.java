package com.github.hypfvieh.javafx.dialogs;

import javafx.scene.control.Alert;

/**
 * Builder to create and show various dialogs based on JavaFX {@link Alert}.
 *
 * @author hypfvieh
 * @since v11.0.2 - 2022-05-19
 */
public class DialogBuilder extends BaseDialogBuilder<DialogBuilder> {

    public static DialogBuilder get() {
        return new DialogBuilder();
    }

}
