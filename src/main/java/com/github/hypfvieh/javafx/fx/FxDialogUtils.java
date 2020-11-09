package com.github.hypfvieh.javafx.fx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import com.github.hypfvieh.javafx.utils.StringHelper;
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
    public static ButtonData showConfirmYesNo(String _msg, String _title, String _subTitle, String _btnYesCaption,
            String _btnNoCaption, String _btnCancel) {
       return showConfirmYesNo(AlertType.CONFIRMATION, _msg, _title, _subTitle, _btnYesCaption, _btnNoCaption, _btnCancel);

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
    public static ButtonData showConfirmYesNo(AlertType _type, String _msg, String _title, String _subTitle, String _btnYesCaption,
            String _btnNoCaption, String _btnCancel) {

        Alert alert = createDialog(_type, _title, _subTitle, _msg);

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
     * <p>
     * For dialog styling see {@link #styleDialog(Alert, AlertType)}.
     *
     * @param _type alert type
     * @param _title title of dialog
     * @param _subTitle subtitle in dialog
     * @param _msg message to display
     */
    public static void showDialog(AlertType _type, String _title, String _subTitle, String _msg) {
        Alert alert = createDialog(_type, _title, _subTitle, _msg);
        alert.showAndWait();
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
    public static Alert createDialog(AlertType _type, String _title, String _subTitle, String _msg) {
        Alert alert = new Alert(_type);
        alert.setTitle(_title);
        alert.setHeaderText(_subTitle);
        alert.setContentText(_msg);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(500);
        styleDialog(alert, _type);
        return alert;
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
    public static void showExceptionDialog(AlertType _type, String _title, String _subTitle, String _msg, String _detailsBtnText, Throwable _ex) {
        Alert createDialog = createDialog(_type, _title, _subTitle, _msg);
        setExpandableContent(_detailsBtnText, StringHelper.getStackTrace(_ex), createDialog);
    }

    /**
     * Style a dialog using custom CSS style sheet.
     * Looks for a CSS file with the name 'dialog-style-level.css', where level is the type
     * of dialog (e.g. AlertType#ERROR) in lower-case.
     *
     * @param _alertBox to style
     * @param _type alert type
     */
    private static void styleDialog(Alert _alertBox, AlertType _type) {
        if (_alertBox == null || _type == null) {
            return;
        }

        String cssName = "dialog-style-" + _type.name().toLowerCase() + ".css";

        URL resource = FxDialogUtils.class.getClassLoader().getResource(cssName);
        if (resource != null) {
            _alertBox.getDialogPane().getScene().getStylesheets().add(cssName);
        }
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

    /**
     * Manipulate content of a dialog to add an expandable textbox.
     *
     * @param _detailtBtnText caption of the button for expansion
     * @param _text text to display in textarea
     * @param _dialog dialog to modify
     */
    public static void setExpandableContent(String _detailtBtnText, String _text, Dialog<?> _dialog) {
        if (_dialog == null) {
            return;
        }

        Label label = new Label(_detailtBtnText);

        TextArea textArea = new TextArea(_text);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expandableContent = new GridPane();
        expandableContent.setMaxWidth(Double.MAX_VALUE);
        expandableContent.add(label, 0, 0);
        expandableContent.add(textArea, 0, 1);
        _dialog.getDialogPane().setExpandableContent(expandableContent);

        _dialog.getDialogPane().expandedProperty().addListener((observable) -> {
            Platform.runLater(() -> {
                _dialog.getDialogPane().requestLayout();
                Stage stage = (Stage)_dialog.getDialogPane().getScene().getWindow();
                stage.sizeToScene();
            });
        });
    }
}
