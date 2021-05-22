package com.github.hypfvieh.javafx.ui;

import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import com.github.hypfvieh.javafx.fx.FxWindowUtils;
import com.github.hypfvieh.javafx.windows.interfaces.IKeyboardShortcut;
import com.github.hypfvieh.javafx.windows.interfaces.ISaveWindowPreferences;

/**
 * Base class for all FXML controller classes.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public abstract class BaseWindowController implements ISaveWindowPreferences, IKeyboardShortcut, Initializable {

    private boolean closedByWindowManager = true;


    //CHECKSTYLE:OFF
    protected final Logger logger = System.getLogger(getClass().getName());

    //CHECKSTYLE:ON

    private Stage controllerStage;

    public void setClosedByWindowManager(boolean _b) {
        closedByWindowManager = _b;
    }

    public boolean isClosedByWindowManager() {
        return closedByWindowManager;
    }

    @Override
    public WindowData saveWindowPreferences() {
        return WindowData.BOTH;
    }

    @Override
    public Map<KeyCombination, Runnable> getGlobalShortcuts() {
        Map<KeyCombination, Runnable> keys = new HashMap<>();

        keys.put(new KeyCodeCombination(KeyCode.W,
                KeyCombination.CONTROL_DOWN), () -> FxWindowUtils.closeWindow(getControllerStage()));

        return keys;
    }

    public final Stage getControllerStage() {
        return controllerStage;
    }

    public final void setControllerStage(Stage _controllerStage) {
        controllerStage = _controllerStage;
    }

}
