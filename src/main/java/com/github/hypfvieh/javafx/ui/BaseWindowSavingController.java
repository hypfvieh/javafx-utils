package com.github.hypfvieh.javafx.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javafx.event.EventHandler;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import com.github.hypfvieh.javafx.fx.FxDialogUtils;
import com.github.hypfvieh.javafx.windows.interfaces.ISaveOnClose;

/**
 * Base window controller class which handles saving on window close.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public abstract class BaseWindowSavingController extends BaseWindowController implements ISaveOnClose {

    private boolean changed;


    /**
     * Method to create a listener which will set the {@link #changed} property of this
     * window if there are changes.
     *
     * @return {@link EventHandler}
     */
    protected <S, T> EventHandler<CellEditEvent<S, T>> createHasChangedListener() {
        return ev -> {
            if (!Objects.equals(ev.getNewValue(), ev.getOldValue())) {
                changed = true;
            }
        };
    }

    /**
     * Represents the current changed status.
     * @return true if changes were made, false otherwise
     */
    public final boolean isChanged() {
        return changed;
    }

    /**
     * Change the changed state of this window controller.
     * Use false to suppress the warning dialog on window closing
     * (e.g to just ignore any changes and do not ask the user).
     *
     * Use true to force the dialog to be shown.
     *
     * @param _changed
     */
    public final void setChanged(boolean _changed) {
        changed = _changed;
    }

    /**
     * Method which should be overridden by subclasses to support
     * the save before close operation.
     */
    protected abstract void saveChanges();


    /**
     * Called when a Window is closed to maybe save changed values.
     * If there are changes, a dialog is shown to ask for saving.
     * If this dialog is closed with yes or no, the changes should be saved or dropped.
     * The dialog also has a cancel button which will cancel window closing when used.
     *
     * @return true to continue closing the window, false to interrupt window closing
     */
    @Override
    public final boolean saveAndClose() {
        if (changed) {
            ButtonData result = FxDialogUtils.showConfirmYesNo("Es gibt ungespeicherte Änderungen." + System.lineSeparator() + "Sollen diese nun gespeichert werden?", "Änderungen", "Ungespeicherte Änderungen", "Ja", "Nein", "Abbrechen");
            if (result == ButtonData.CANCEL_CLOSE) {
                return false;
            } else if (result == ButtonData.YES) {
                saveChanges();
            }
        }
        close(); // do other close actions (e.g. close db connection)
        return true;
    }

    @Override
    public void close() {
        // default does nothing
    }

    /**
     * Registers CTRL+S as default global shortcut for saving data.
     */
    @Override
    public Map<KeyCombination, Runnable> getGlobalShortcuts() {
        Map<KeyCombination, Runnable> globalShortcuts = super.getGlobalShortcuts();
        if (globalShortcuts == null) {
            globalShortcuts = new HashMap<>();
        }

        globalShortcuts.put(new KeyCodeCombination(KeyCode.S,
                KeyCombination.CONTROL_DOWN), () -> saveChanges());

        return globalShortcuts;
    }
}
