package com.github.hypfvieh.javafx.fx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.github.hypfvieh.javafx.utils.StringHelper;
import com.github.hypfvieh.javafx.utils.Translator;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Helper to display various types of message dialogs.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class FxDialogUtils {

    /**
     * @see #showConfirmYesNo(Window, AlertType, String, String, String, String, String, String)
     *
     * @param _owner owner window, null if none
     * @param _msg dialog message
     * @param _title dialog title
     * @param _subTitle dialog header text
     * @param _btnYesCaption yes button caption
     * @param _btnNoCaption no button caption
     * @param _btnCancel cancel button caption (use null to hide this button)
     *
     * @return {@link ButtonData} representing clicked button ({@link ButtonData#YES}, {@link ButtonData#NO}, {@link ButtonData#CANCEL_CLOSE})
     */
    public static ButtonData showConfirmYesNo(Window _owner, String _msg, String _title, String _subTitle, String _btnYesCaption,
            String _btnNoCaption, String _btnCancel) {
       return showConfirmYesNo(_owner, AlertType.CONFIRMATION, _msg, _title, _subTitle, _btnYesCaption, _btnNoCaption, _btnCancel);

    }

    /**
     * Shows an confirmation dialog with custom buttons.
     *
     * <p>
     * For dialog styling see {@link #styleDialog(Alert, AlertType)}.
     *
     * @param _owner owner window, null if none
     * @param _type alert type (e.g. information, warning, error)
     * @param _msg message to show
     * @param _title window title
     * @param _subTitle sub title displayed left of the icon in bigger font
     * @param _btnYesCaption caption for 'yes' button
     * @param _btnNoCaption caption for 'no' button
     * @param _btnCancel Cancel button caption, if null, no button will be shown
     * @return pressed Button (ButtonData.CANCEL_CLOSE in doubt)
     */
    public static ButtonData showConfirmYesNo(Window _owner, AlertType _type, String _msg, String _title, String _subTitle, String _btnYesCaption,
            String _btnNoCaption, String _btnCancel) {

        Alert alert = createDialog(_owner, _type, _title, _subTitle, _msg);

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
     * @param _owner owner window, null if none
     * @param _type alert type
     * @param _title title of dialog
     * @param _subTitle subtitle in dialog
     * @param _msg message to display
     */
    public static void showDialog(Window _owner, AlertType _type, String _title, String _subTitle, String _msg) {
        Alert alert = createDialog(_owner, _type, _title, _subTitle, _msg);
        alert.showAndWait();
    }

    /**
     * Create a new dialog of the given type with given title, subtitle and message.
     *
     * @param _owner owner window, null if none
     * @param _type alert type
     * @param _title title of dialog
     * @param _subTitle subtitle in dialog
     * @param _msg message to display
     *
     * @return Alert dialog instance
     */
    public static Alert createDialog(Window _owner, AlertType _type, String _title, String _subTitle, String _msg) {
        Alert alert = new Alert(_type);
        alert.setTitle(_title);
        alert.setHeaderText(_subTitle);
        alert.setContentText(_msg);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(_owner);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(500);
        styleDialog(alert, _type);
        return alert;
    }

    /**
     * Shows a custom dialog with an expandable textbox with exception details.
     *
     * @param _owner owner window, null if none
     * @param _type alert type
     * @param _title title of dialog
     * @param _subTitle subtitle in dialog
     * @param _msg message to display
     * @param _detailsBtnText text on the button to show more details (e.g. the stack trace)
     * @param _ex throwable to show in textbox
     */
    public static void showExceptionDialog(Window _owner, AlertType _type, String _title, String _subTitle, String _msg, String _detailsBtnText, Throwable _ex) {
        Alert createDialog = createDialog(_owner, _type, _title, _subTitle, _msg);
        setExpandableContent(_detailsBtnText, StringHelper.getStackTrace(_ex), createDialog);
        createDialog.showAndWait();
    }

    /**
     * Style a dialog using custom CSS style sheet.
     * Looks for a CSS file with the name 'dialog-style-level.css', where level is the type
     * of dialog (e.g. AlertType#ERROR) in lower-case.
     *
     * @param _alertBox to style
     * @param _type alert type
     */
    static void styleDialog(Alert _alertBox, AlertType _type) {
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
     * Opens the given file or directory in the associated program.
     * Will show error messages if file/directory does not exist or is not readable.
     * If given file is null, nothing will happen.
     *
     * @param _owner owner window, null if none
     * @param _file file or directory to open
     */
    public static void openFile(Window _owner, File _file) {
        if (_file == null) {
            return;
        }
        Translator translator = new Translator("DialogUtils");
        if (!_file.exists()) {
            showDialog(_owner, AlertType.ERROR,
                translator.t("dlg_openfile_fnf_title", "File or directory not found"),
                translator.t("dlg_openfile_fnf_title", "File or directory not found"),
                translator.t("dlg_openfile_fnf_msg_file_not_exists", "File/Directory %s could not be opened because it does not exist.", _file)
            );
            return;
        } else if (!_file.canRead()) {
            if (_file.isDirectory()) {
                showDialog(_owner, AlertType.ERROR,
                        translator.t("dlg_opendir_dnr_title", "Directory not readable"),
                        translator.t("dlg_opendir_dnr_title", "Directory not readable"),
                        translator.t("dlg_opendir_dnr_msg", "The directory %s cannot be opened because it is not readable.", _file)
                        );
            } else {
                showDialog(_owner, AlertType.ERROR,
                    translator.t("dlg_openfile_fnr_title", "File not readable"),
                    translator.t("dlg_openfile_fnr_title", "File not readable"),
                    translator.t("dlg_openfile_fnr_msg", "The file %s cannot be opened because it is not readable.", _file)
                    );
            }
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                Desktop.getDesktop().open(_file);
            } catch (IOException _ex) {
                Platform.runLater(() -> {
                    if (_file.isDirectory()) {
                        showDialog(_owner, AlertType.ERROR,
                                translator.t("dlg_opendir_error", "Error"),
                                translator.t("dlg_opendir_error_subtitle", "Directory could not be opened"),
                                translator.t("dlg_opendir_error_msg", "Directory could not be opened.%nMaybe there is no default application configured for opening directories.")
                            );
                    } else {
                        showDialog(_owner, AlertType.ERROR,
                            translator.t("dlg_openfile_error", "Error"),
                            translator.t("dlg_openfile_error_subtitle", "File could not be opened"),
                            translator.t("dlg_openfile_error_msg", "File could not be opened.%nMaybe there is no default application configured for this file type.")
                        );
                    }

                });
            }
        }, "open-file:" + _file.getName());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Manipulate content of a dialog to add an expandable textbox.
     *
     * @param _detailBtnText caption of the button for expansion
     * @param _text text to display in textarea
     * @param _dialog dialog to modify
     */
    public static void setExpandableContent(String _detailBtnText, String _text, Dialog<?> _dialog) {
        if (_dialog == null) {
            return;
        }

        TextArea textArea = new TextArea(_text);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expandableContent = new GridPane();
        expandableContent.setMaxWidth(Double.MAX_VALUE);
        expandableContent.add(textArea, 0, 0);

        _dialog.getDialogPane().setExpandableContent(expandableContent);

        if (_detailBtnText != null) {
            setDetailButtonText(_dialog, _detailBtnText);
        }

        _dialog.getDialogPane().expandedProperty().addListener((observable) -> {
            setDetailButtonText(_dialog, _detailBtnText);
            Platform.runLater(() -> {
                _dialog.getDialogPane().requestLayout();
                Scene scene = _dialog.getDialogPane().getScene();
                if (scene != null) {
                    Stage stage = (Stage) scene.getWindow();
                    stage.sizeToScene();
                }
            });
        });

    }

    private static void setDetailButtonText(Dialog<?> _dialog, String _detailBtnText) {
        _dialog.getDialogPane().getChildren().stream()
        .filter(ButtonBar.class::isInstance)
            .map(ButtonBar.class::cast)
            .findFirst()
            .flatMap(b -> b.getButtons().stream()
                .filter(Hyperlink.class::isInstance)
                .map(Hyperlink.class::cast)
                .findFirst())
            .ifPresent(c -> {
                c.setText(_detailBtnText);
            });
    }
}
