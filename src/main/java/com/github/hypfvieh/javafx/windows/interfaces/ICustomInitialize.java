package com.github.hypfvieh.javafx.windows.interfaces;

import javafx.fxml.Initializable;

/**
 * Interface which allows to do additional initialization on window showing.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface ICustomInitialize {
    /**
     * Method which will be called to initialize.
     * <p>
     * This method will be called after {@link Initializable#initialize(java.net.URL, java.util.ResourceBundle)} and can
     * work with controls as they already have been initialized.
     */
    void customInitialize();
}
