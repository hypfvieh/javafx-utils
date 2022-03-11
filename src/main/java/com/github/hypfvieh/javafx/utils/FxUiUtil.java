package com.github.hypfvieh.javafx.utils;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Helper class imported from java-utils-extended.
 *
 * @author hypfvieh
 */
public class FxUiUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FxUiUtil.class);

    /**
     * Opens a JavaFX window using an FXML file.
     *
     * @param _parent parent class (cannot be null)
     * @param _fXmlFile fxml file to load
     * @param _wait use showAndWait instead of show
     * @param _modal set window modality
     * @param _resizeable set window resizable
     * @param _title  set window title
     */
    public static void showWindow(Class<?> _parent, String _fXmlFile, boolean _wait, Modality _modal, boolean _resizeable, String _title) {
        if (_parent == null) {
            return;
        }
        try {
            Parent root = FXMLLoader.load(_parent.getResource(_fXmlFile));

            Stage stage = new Stage();
            stage.initModality(_modal);
            stage.setTitle(_title);
            stage.setResizable(_resizeable);
            stage.setScene(new Scene(root));
            if (_wait) {
                stage.showAndWait();
            } else {
                stage.show();
            }
        } catch (IOException _ex) {
            LOGGER.error("Error while showing window:",_ex);
        }

    }

    /**
     * Opens a JavaFX window using an FXML file and sets resizable to true.
     * @see #showWindow(Class, String, boolean, Modality, boolean, String)
     *
     * @param _parent parent class (cannot be null)
     * @param _fXmlFile fxml file to load
     * @param _wait use showAndWait instead of show
     * @param _modal set window modality
     * @param _title window title
     */
    public static void showWindow(Class<?> _parent, String _fXmlFile, boolean _wait, Modality _modal,  String _title) {
        showWindow(_parent, _fXmlFile, _wait, _modal, true, _title);
    }

    /**
     * Opens a JavaFX window using an FXML file and sets resizable to true and modality to none.
     * @see #showWindow(Class, String, boolean, Modality, boolean, String)
     *
     * @param _parent parent class (cannot be null)
     * @param _fXmlFile fxml file to load
     * @param _wait use showAndWait instead of show
     * @param _title window title
     */
    public static void showWindow(Class<?> _parent, String _fXmlFile, boolean _wait, String _title) {
        showWindow(_parent, _fXmlFile, _wait, Modality.NONE, true, _title);
    }
    /**
     * Opens a JavaFX window using an FXML file and sets modality to none.
     * @see #showWindow(Class, String, boolean, Modality, boolean, String)
     *
     * @param _parent parent class (cannot be null)
     * @param _fXmlFile fxml file to load
     * @param _wait use showAndWait instead of show
     * @param _resizeable true to allow window resizing, false otherwise
     * @param _title window title
     */
    public static void showWindow(Class<?> _parent, String _fXmlFile, boolean _wait, boolean _resizeable,  String _title) {
        showWindow(_parent, _fXmlFile, _wait, Modality.NONE, _resizeable, _title);
    }

    /**
     * Closes a JavaFX stage (close window).
     * @param _javaFxComponentOrNode component to get stage from
     */
    public static void closeWindow(Node _javaFxComponentOrNode) {
        if (_javaFxComponentOrNode.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) _javaFxComponentOrNode.getScene().getWindow();
            if (stage != null) {
                 stage.close();
            }
        }
    }

    /**
     * Converts JavaFX color to HTML Style Colorcode.
     * @param _color javaFX color object
     *
     * @return HTML color code
     */
    public static String fxColorToHtmlRgbCode(Color _color) {
        return String.format( "#%02X%02X%02X",
            (int)( _color.getRed() * 255 ),
            (int)( _color.getGreen() * 255 ),
            (int)( _color.getBlue() * 255 ) );
    }

    /**
     * Shows an confirmation dialog with custom buttons.
     * @param _msg message to show
     * @param _title message box title
     * @param _subTitle subtitle to use
     * @param _btnYesCaption caption of 'Yes' button
     * @param _btnNoCaption caption of 'No' button
     *
     * @return true if yes was clicked, false otherwise
     */
    public static boolean showConfirmYesNo(String _msg, String _title, String _subTitle, String _btnYesCaption, String _btnNoCaption) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(_title);
            alert.setHeaderText(_subTitle);
            alert.setContentText(_msg);

            ButtonType btnYes = new ButtonType(_btnYesCaption, ButtonData.YES);
            ButtonType btnNo = new ButtonType(_btnNoCaption, ButtonData.NO);

            alert.getButtonTypes().setAll(btnYes, btnNo);

            Optional<ButtonType> result = alert.showAndWait();

            return result.get() == btnYes;

    }

    /**
     * Shows any kind of dialog (useful for warning/information/error dialogs).
     * @param _type dialog type
     * @param _title title of dialog box
     * @param _subTitle subtitle of dialog
     * @param _msg message of dialog
     */
    public static void showDialog(AlertType _type, String _title, String _subTitle, String _msg) {
            Alert alert = new Alert(_type);
            alert.setTitle(_title);
            alert.setHeaderText(_subTitle);
            alert.setContentText(_msg);

            alert.showAndWait();
    }
}
