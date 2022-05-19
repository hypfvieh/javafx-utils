package com.github.hypfvieh.javafx.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.hypfvieh.javafx.fx.FxDialogUtils;
import com.github.hypfvieh.javafx.utils.StringHelper;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Builder to create and show various dialogs based on JavaFX {@link Alert}.
 *
 * @author hypfvieh
 * @since v11.0.2 - 2022-05-19
 */
public class DialogBuilder {

    private AlertType                     type;
    private final Map<String, ButtonData> buttons = new LinkedHashMap<>();

    private String                        subTitle;
    private String                        title;
    private final List<String>            message = new ArrayList<>();

    private Double                        width;
    private Double                        height;

    private Throwable                     throwable;
    private String                        detailButtonCaption;
    private Supplier<Boolean>             doShowIf;

    // allow subclassing so builder is easier testable
    protected DialogBuilder() {

    }

    public static DialogBuilder get() {
        return new DialogBuilder();
    }

    /**
     * Set Dialog type.
     * @param _type type
     *
     * @return this
     */
    public DialogBuilder withType(AlertType _type) {
        type = _type;
        return this;
    }

    /**
     * Set dialog window title.
     * @param _title title
     *
     * @return this
     */
    public DialogBuilder withTitle(String _title) {
        title = _title;
        return this;
    }

    /**
     * Set dialog subtitle.
     * @param _subTitle title
     *
     * @return this
     */
    public DialogBuilder withSubTitle(String _subTitle) {
        subTitle = _subTitle;
        return this;
    }

    /**
     * Set dialog message.
     * Each item in the given var-args array will be concatinated with
     * {@link System#lineSeparator()} when message is shown in dialog.
     *
     * @param _msg message
     *
     * @return this
     */
    public DialogBuilder withMessage(String... _msg) {
        if (_msg == null || _msg.length == 1 && _msg[0] == null) {
            message.clear();
        } else {
            message.addAll(Arrays.asList(_msg));
        }
        return this;
    }

    /**
     * Add the given button with the given caption.
     *
     * @param _button button type
     * @param _caption caption
     * @return this
     */
    public DialogBuilder withButton(ButtonData _button, String _caption) {
        buttons.put(_caption, _button);
        return this;
    }

    /**
     * Add a 'YES' button with the given caption.
     *
     * @param _caption caption
     * @return this
     */
    public DialogBuilder withYesButton(String _caption) {
        return withButton(ButtonData.YES, _caption);
    }

    /**
     * Add a 'NO' button with the given caption.
     *
     * @param _caption caption
     * @return this
     */
    public DialogBuilder withNoButton(String _caption) {
        return withButton(ButtonData.NO, _caption);
    }

    /**
     * Add a 'CANCEL' button with the given caption.
     *
     * @param _caption caption
     * @return this
     */
    public DialogBuilder withCancelButton(String _caption) {
        return withButton(ButtonData.CANCEL_CLOSE, _caption);
    }

    /**
     * Set the height of the created dialog.
     * If null is given, default size is used.
     *
     * @param _height height
     *
     * @return this
     */
    public DialogBuilder withHeight(Double _height) {
        height = _height;
        return this;
    }

    /**
     * Set the width of the created dialog.
     * If null is given, default size is used.
     *
     * @param _width width
     *
     * @return this
     */
    public DialogBuilder withWidth(Double _width) {
        width = _width;
        return this;
    }

    /**
     * Set a throwable (exception) to show a dialog with
     * extended information (the stack trace).
     * <p>
     * If no value (null or blank/empty String) is given for the button
     * caption 'Details' will be used.
     *
     * @param _throw throwable
     * @param _detailButtonCaption caption for the button to show details
     *
     * @return this
     */
    public DialogBuilder withThrowable(Throwable _throw, String _detailButtonCaption) {
        throwable = _throw;
        detailButtonCaption = _detailButtonCaption == null || _detailButtonCaption.isBlank() ? "Details" : _detailButtonCaption;
        return this;
    }

    /**
     * Shows the dialog and blocks until a button is pressed.<br>
     * When the pressed button is equals to the given button, the given runnable will be executed.
     *
     * @param _stage parent stage
     * @param _button button to react on
     * @param _r runnable to execute
     */
    public void showAndExecute(Window _stage, ButtonData _button, Runnable _r) {
        Objects.requireNonNull(_button, "Button required");
        Objects.requireNonNull(_r, "Runnable required");

        if (show(_stage) == _button) {
            _r.run();
        }
    }

    /**
     * Can be used to check if a condition is true before showing the dialog.<br>
     * If the condition is false, the dialog will not be shown.
     * <p>
     * This method will <b>not</b> show the dialog, it will only store a reference to a {@link Supplier}<br>
     * which will be validated when any show-method is called.
     * </p>
     * <p>
     * If <code>null</code> is used, the condition will be ignored and the dialog will be<br>
     * shown when any show-method is called.
     * </p>
     *
     * @param _condition condition which should return true when dialog should be shown
     *
     * @return this
     */
    public DialogBuilder showIf(Supplier<Boolean> _condition) {
        doShowIf = _condition;
        return this;
    }

    /**
     * Shows the dialog and blocks until a button is pressed.
     * When the button pressed consumer will be called receiving the pressed button.
     *
     * @param _stage parent stage
     * @param _button button to react on
     * @param _r consumer to execute
     */
    public void showAndExecute(Window _stage, Consumer<ButtonData> _r) {
        Objects.requireNonNull(_r, "Consumer required");
        ButtonData show = show(_stage);
        _r.accept(show);
    }

    /**
     * Shows the dialog and blocks until a button is pressed.
     * When the button pressed function will be called receiving the pressed button.
     *
     * @param _stage parent stage
     * @param _button button to react on
     * @param _r function to execute
     */
    public <T> T showAndExecute(Window _stage, Function<ButtonData, T> _r) {
        Objects.requireNonNull(_r, "Consumer required");
        ButtonData show = show(_stage);
        return _r.apply(show);
    }

    /**
     * Show the dialog and blocks until a button is pressed.
     *
     * @param _stage parent stage
     * @return ButtonData of pressed button or default (CANCEL)
     */
    public ButtonData show(Window _stage) {
        if (doShowIf != null && !doShowIf.get()) {
            return ButtonData.CANCEL_CLOSE;
        }

        Alert dialog = FxDialogUtils.createDialog(_stage, type, title, subTitle, String.join(System.lineSeparator(), message));

        if (height != null) {
            dialog.setHeight(height);
        }
        if (width != null) {
            dialog.setWidth(width);
        }

        if (!buttons.isEmpty()) {
            dialog.getButtonTypes().clear();

            for (Entry<String, ButtonData> e : buttons.entrySet()) {
                if (e.getKey() == null) {
                    continue;
                }
                dialog.getButtonTypes().add(new ButtonType(e.getKey(), e.getValue()));
            }
        }

        if (throwable != null) {
            FxDialogUtils.setExpandableContent(detailButtonCaption, StringHelper.getStackTrace(throwable), dialog);
        }

        ButtonData result = dialog.showAndWait().orElse(ButtonType.CANCEL).getButtonData();

        // reset builder before returning:
        reset();

        return result;
    }

    /**
     * Reset the state of the internal fields to the default.
     * <br>
     * This will automatically be called when a dialog was shown,
     * but maybe called at any time to reset the builder and to create a new
     * dialog.
     *
     * @return this
     */
    public DialogBuilder reset() {
        type = null;
        buttons.clear();
        subTitle = null;
        title = null;
        message.clear();
        width = null;
        height = null;
        throwable = null;
        detailButtonCaption = null;
        doShowIf = null;

        return this;
    }

}
