package com.github.hypfvieh.javafx.ui;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.javafx.fx.FxWindowUtils;
import com.github.hypfvieh.javafx.fx.IFxDialogAccessor;
import com.github.hypfvieh.javafx.windows.interfaces.IKeyboardShortcut;
import com.github.hypfvieh.javafx.windows.interfaces.ISaveWindowPreferences;
import com.github.hypfvieh.javafx.windows.interfaces.IStageControllerAware;
import com.github.hypfvieh.javafx.windows.interfaces.WindowData;

import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Base class for all FXML controller classes.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public abstract class BaseWindowController implements ISaveWindowPreferences, IKeyboardShortcut, Initializable, IStageControllerAware, IFxDialogAccessor {

    private boolean closedByWindowManager = true;


    //CHECKSTYLE:OFF
    protected final Logger logger = LoggerFactory.getLogger(getClass());

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

    @Override
    public final Stage getControllerStage() {
        return controllerStage;
    }

    @Override
    public final void setControllerStage(Stage _controllerStage) {
        controllerStage = _controllerStage;
    }

}
