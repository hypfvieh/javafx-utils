package com.github.hypfvieh.javafx.fx;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;

import com.github.hypfvieh.javafx.utils.StringHelper;

/**
 * Convenience interface for showing dialogs.
 * Will store the window reference and will display all dialogs based on that window.
 *
 * @author hypfvieh
 * @since v11.0.1 - 2021-06-22
 */
public interface IFxDialogAccessor {
    Stage getControllerStage();

    /**
     * @see #showConfirmYesNo(AlertType, String, String, String, String, String, String)
     *
     * @param _msg dialog message
     * @param _title dialog title
     * @param _subTitle dialog header text
     * @param _btnYesCaption yes button caption
     * @param _btnNoCaption no button caption
     * @param _btnCancel cancel button caption (use null to hide this button)
     *
     * @return {@link ButtonData} representing clicked button ({@link ButtonData#YES}, {@link ButtonData#NO}, {@link ButtonData#CANCEL_CLOSE})
     */
    default ButtonData showConfirmYesNo(String _msg, String _title, String _subTitle, String _btnYesCaption,
            String _btnNoCaption, String _btnCancel) {
        return FxDialogUtils.showConfirmYesNo(getControllerStage(), _msg, _title, _subTitle, _btnYesCaption, _btnNoCaption, _btnCancel);
    }

    /**
     * Shows an confirmation dialog with custom buttons.
     *
     * <p>
     * For dialog styling see {@link #styleDialog(Alert, AlertType)}.
     *
     * @param _type alert type (e.g. information, warning, error)
     * @param _msg message to show
     * @param _title window title
     * @param _subTitle sub title displayed left of the icon in bigger font
     * @param _btnYesCaption caption for 'yes' button
     * @param _btnNoCaption caption for 'no' button
     * @param _btnCancel Cancel button caption, if null, no button will be shown
     * @return pressed Button (ButtonData.CANCEL_CLOSE in doubt)
     */
    default ButtonData showConfirmYesNo(AlertType _type, String _msg, String _title, String _subTitle, String _btnYesCaption,
            String _btnNoCaption, String _btnCancel) {
        return FxDialogUtils.showConfirmYesNo(getControllerStage(), _type, _msg, _title, _subTitle, _btnYesCaption, _btnNoCaption, _btnCancel);
    }

    /**
     * Shows any kind of dialog (useful for warning/information/error dialogs).
     * <p>
     * For dialog styling see {@link #styleDialog(Alert, AlertType)}.
     *
     * @param _type alert type
     * @param _title title of dialog
     * @param _subTitle subtitle in dialog
     * @param _msg message to display
     */
    default void showDialog(AlertType _type, String _title, String _subTitle, String _msg) {
        FxDialogUtils.showDialog(getControllerStage(), _type, _title, _subTitle, _msg);
    }

    /**
     * Create a new dialog of the given type with given title, subtitle and message.
     *
     * @param _type alert type
     * @param _title title of dialog
     * @param _subTitle subtitle in dialog
     * @param _msg message to display
     *
     * @return Alert dialog instance
     */
    default Alert createDialog(AlertType _type, String _title, String _subTitle, String _msg) {
        return FxDialogUtils.createDialog(getControllerStage(), _type, _title, _subTitle, _msg);
    }

    /**
     * Shows a custom dialog with an expandable textbox with exception details.
     *
     * @param _type alert type
     * @param _title title of dialog
     * @param _subTitle subtitle in dialog
     * @param _msg message to display
     * @param _detailsBtnText text on the button to show more details (e.g. the stack trace)
     * @param _ex throwable to show in textbox
     */
    default void showExceptionDialog(AlertType _type, String _title, String _subTitle, String _msg, String _detailsBtnText, Throwable _ex) {
        Alert createDialog = createDialog(_type, _title, _subTitle, _msg);
        FxDialogUtils.setExpandableContent(_detailsBtnText, StringHelper.getStackTrace(_ex), createDialog);
    }

    /**
     * Opens the given file in the associated program.
     * Will show error messages if file does not exist or is not readable.
     * If given file is null, nothing will happen.
     *
     * @param _file file to open
     */
    default void openFile(File _file) {
        FxDialogUtils.openFile(getControllerStage(), _file);
    }
}