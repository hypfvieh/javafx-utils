package com.github.hypfvieh.javafx.dialogs;

import com.github.hypfvieh.javafx.fx.FxDialogUtils;
import com.github.hypfvieh.javafx.fx.FxWindowUtils;
import com.github.hypfvieh.javafx.utils.StringHelper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Builder to create and show various dialogs based on JavaFX {@link Alert}.
 *
 * @author hypfvieh
 * @since v11.0.2 - 2022-05-19
 */
public class DialogBuilder {

    private AlertType                     type;
    private final Map<ButtonData, String> buttons = new LinkedHashMap<>();

    private String                        subTitle;
    private String                        title;
    private final List<String>            message = new ArrayList<>();

    private Double                        width;
    private Double                        height;

    private Throwable                     throwable;
    private String                        detailButtonCaption;
    private Supplier<Boolean>             doShowIf;
    private Collection<String>            lines;

    private final List<String>            windowIcons = new ArrayList<>();
    private boolean expanded;

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
     *
     * @throws IllegalArgumentException when button was already defined
     */
    public DialogBuilder withButton(ButtonData _button, String _caption) {
        if (buttons.containsKey(_button)) {
            throw new IllegalArgumentException("Button of type " + _button + " already defined");
        }
        buttons.put(_button, _caption);
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
     * Expand the detail pane when showing the dialog.
     *
     * @param _expanded true to expand
     * @return this
     *
     * @since 11.0.3 - 2024-11-21
     */
    public DialogBuilder withDetailsExpanded(boolean _expanded) {
        expanded = _expanded;
        return this;
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
     * Set the icon(s) used in tray bar and dialog decoration.
     * <p>
     * The icon will be chosen by JavaFX by selecting the icon
     * which resolution fits best for the current decoration icon size.
     * If icon is smaller or bigger, it will be resized.
     * </p>
     *
     * @param _windowIcon list of URLs with icons to set, null to remove all icons
     *
     * @return this
     */
    public DialogBuilder withWindowIcon(List<String> _windowIcon) {
        if (_windowIcon != null) {
            windowIcons.addAll(_windowIcon);
        } else {
            windowIcons.clear();
        }
        return this;
    }

    /**
     * Set a throwable (exception) to show a dialog with
     * extended information (the stack trace).
     * <p>
     * If no value (null or blank/empty String) Alert-Dialog default is used.
     *
     * @param _throw throwable
     * @param _detailButtonCaption caption for the button to show details
     *
     * @return this
     */
    public DialogBuilder withThrowable(Throwable _throw, String _detailButtonCaption) {
        throwable = _throw;
        setDetailButtonCaption(_detailButtonCaption);
        return this;
    }

    private void setDetailButtonCaption(String _text) {
        detailButtonCaption = _text != null && !_text.isBlank() ? _text : null;
    }

    /**
     * Add extra text in a text area which is hidden behind a button by default.<br>
     * This uses the same feature as {@link #withThrowable(Throwable, String)}.
     * <p>
     * <b>Caution:</b>
     * When {@link #withThrowable(Throwable, String)} was used, the content of the exception
     * takes precedence over the content set by this method.
     *
     * @param _throw
     * @param _detailButtonCaption
     *
     * @return this
     *
     * @since 11.0.3 - 2022-12-05
     */
    public DialogBuilder withExtendedContent(Collection<String> _lines, String _detailButtonCaption) {
        lines = _lines;
        setDetailButtonCaption(_detailButtonCaption);
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

            for (Entry<ButtonData, String> e : buttons.entrySet()) {
                if (e.getValue() == null || e.getKey() == null) {
                    continue;
                }
                dialog.getButtonTypes().add(new ButtonType(e.getValue(), e.getKey()));
            }
        }

        if (!windowIcons.isEmpty()) {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            try {
                FxWindowUtils.loadStageIcons(windowIcons, stage);
            } catch (IOException _ex) {
                LoggerFactory.getLogger(getClass()).error("Error loading stage icons", _ex);
            }
        }

        if (throwable != null) {
            FxDialogUtils.setExpandableContent(detailButtonCaption, StringHelper.getStackTrace(throwable), dialog);
        } else if (lines != null) {
            FxDialogUtils.setExpandableContent(detailButtonCaption, String.join(System.lineSeparator(), lines), dialog);
        }

        dialog.getDialogPane().setExpanded(expanded);

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
        windowIcons.clear();
        lines = null;
        expanded = false;

        return this;
    }

}
