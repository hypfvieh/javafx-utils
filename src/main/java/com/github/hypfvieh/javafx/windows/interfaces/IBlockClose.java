package com.github.hypfvieh.javafx.windows.interfaces;

import javafx.application.Platform;

/**
 * Interface which allows implementing windows to block the window closed operation.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface IBlockClose {
    /**
     * Returns true to allow windows closing, false to block window closing.
     * @return boolean
     */
    boolean allowClose();

    /**
     * A runnable which will be executed when the window is not allowed to be closed.
     * This can e.g. be used to show a message dialog etc. to the user (using {@link Platform#runLater(Runnable)}).
     *
     * @return runnable or null (default)
     */
    default Runnable getBlockAction() {
        return null;
    }
}
