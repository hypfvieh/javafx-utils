package com.github.hypfvieh.javafx.fx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import com.github.hypfvieh.javafx.utils.Translator;

/**
 * Helper to display various types of message dialogs.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class FxDialogUtils {

    /**
     * @see #showConfirmYesNo(AlertType, String, String, String, String, String, String)
     */
    public static ButtonData showConfirmYesNo(String _msg, String _title, String _subTitle, String _btnYesCaption,
            String _btnNoCaption, String _btnCancel) {
       return showConfirmYesNo(AlertType.CONFIRMATION, _msg, _title, _subTitle, _btnYesCaption, _btnNoCaption, _btnCancel);

    }

    /**
     * Shows an confirmation dialog with custom buttons.
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
    public static ButtonData showConfirmYesNo(AlertType _type, String _msg, String _title, String _subTitle, String _btnYesCaption,
            String _btnNoCaption, String _btnCancel) {
        Alert alert = new Alert(_type);
        alert.setTitle(_title);
        alert.setHeaderText(_subTitle);
        alert.setContentText(_msg);

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        ButtonType btnYes = new ButtonType(_btnYesCaption, ButtonData.YES);
        ButtonType btnNo = new ButtonType(_btnNoCaption, ButtonData.NO);
        alert.getButtonTypes().setAll(btnYes, btnNo);

        if (_btnCancel != null) {
            ButtonType btnCancel = new ButtonType(_btnCancel, ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().add(btnCancel);
        }

        return alert.showAndWait().orElse(ButtonType.CANCEL).getButtonData();

    }

    /**
     * Shows any kind of dialog (useful for warning/information/error dialogs).
     *
     * @param _type alert type
     * @param _title title of dialog
     * @param _subTitle subtitle in dialog
     * @param _msg message to display
     */
    public static void showDialog(AlertType _type, String _title, String _subTitle, String _msg) {
        Alert alert = new Alert(_type);
        alert.setTitle(_title);
        alert.setHeaderText(_subTitle);
        alert.setContentText(_msg);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(500);
        if (_type == AlertType.ERROR) {
            alert.getDialogPane().getScene().getStylesheets().add("dialog-style-error.css");
        }
        alert.showAndWait();
    }

    /**
     * Opens the given file in the associated program.
     * Will show error messages if file does not exist or is not readable.
     * If given file is null, nothing will happen.
     *
     * @param _file file to open
     */
    public static void openFile(File _file) {
        if (_file == null) {
            return;
        }
        Translator translator = new Translator("DialogUtils");
        if (!_file.exists()) {
            showDialog(AlertType.ERROR,
                translator.t("dlg_openfile_fnf_title", "File not found"),
                translator.t("dlg_openfile_fnf_title", "File not found"),
                translator.t("dlg_openfile_fnf_msg_file_not_exists", "File %s could not be opened because it does not exist.", _file)
            );
            return;
        } else if (!_file.canRead()) {
            showDialog(AlertType.ERROR,
                    translator.t("dlg_openfile_fnr_title", "File not readable"),
                    translator.t("dlg_openfile_fnr_title", "File not readable"),
                    translator.t("dlg_openfile_fnr_msg", "The file %s cannot be opened because it is not readable.", _file)
                    );
            return;
        }

        new Thread(() -> {
            try {
                Desktop.getDesktop().open(_file);
            } catch (IOException _ex) {
                Platform.runLater(() -> {
                    showDialog(AlertType.ERROR,
                        translator.t("dlg_openfile_error", "Error"),
                        translator.t("dlg_openfile_error_subtitle", "File could not be opened"),
                        translator.t("dlg_openfile_error_msg", "File could not be opened.%nMaybe there is no default application configured for this file type.")
                    );

                });
            }
        }).start();

    }
}
