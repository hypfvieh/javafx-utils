package com.github.hypfvieh.javafx.fx;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.hypfvieh.javafx.fx.FxWindowUtils.WindowAlreadyOpenedException;
import com.github.hypfvieh.javafx.fx.FxWindowUtils.WindowOptions;
import com.github.hypfvieh.javafx.utils.StringHelper;

import javafx.fxml.Initializable;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Builder to show new windows from FXML files easily.
 *
 * @author hypfvieh
 * @since v11.0.1 - 2022-02-26
 */
public class FxWindowPresenter {
    private final WindowOptions        windowOptions               = new WindowOptions();

    private String                     windowTitle                 = null;
    private String                     fxmlFile                    = null;
    private Stage                      ownerStage                  = null;
    private boolean                    wait                        = false;
    private Modality                   modality                    = Modality.NONE;
    private boolean                    useOwnerStage               = false;
    private Class<?>                   rootClass                   = getClass();
    private Consumer<Stage>            windowAlreadyShowingHandler = null;
    private Object                     controllerInstance          = null;
    private Callback<Class<?>, Object> controllerFactory           = null;

    /**
     * Set width of the created window.
     * If not set, size defined in FXML is used.
     * @param _width must be > 0
     *
     * @return this
     */
    public FxWindowPresenter withWidth(double _width) {
        windowOptions.withWidth(_width);
        return this;
    }


    /**
     * Set height of the created window.
     * If not set, size defined in FXML is used.
     * @param _height must be > 0
     *
     * @return this
     */
    public FxWindowPresenter withHeight(double _height) {
        windowOptions.withHeight(_height);
        return this;
    }

    /**
     * Show the window maximized.
     * If null is given, JavaFx default will be used.
     *
     * @param _maximize true to allow, false to disallow, null to use JavaFx default
     *
     * @return this
     */
    public FxWindowPresenter withMaximized(Boolean _maximize) {
        windowOptions.withMaximize(_maximize);
        return this;
    }

    /**
     * Enables/Disables the option to resize the window.
     * If null is given, JavaFx default will be used.
     *
     * @param _resizeable true to allow, false to disallow, null to use JavaFx default
     *
     * @return this
     */
    public FxWindowPresenter withResizeable(Boolean _resizeable) {
        windowOptions.withResizeable(_resizeable);
        return this;
    }

    public FxWindowPresenter withForceMaximized(boolean _fullScreen) {
        windowOptions.withForceFullScreen(_fullScreen);
        return this;
    }

    /**
     * Toggles if window should be always shown on top of all other windows.
     * Default is false.
     *
     * @param _alwaysOnTop true to show on top, false to allow sending to background
     *
     * @return this
     */
    public FxWindowPresenter withAlwaysOnTop(boolean _alwaysOnTop) {
        windowOptions.withAlwaysOnTop(_alwaysOnTop);
        return this;
    }

    /**
     * Toggles if window should be closed if focus is removed from the window.
     * Default is false.
     *
     * @param _closeOnFocusLost true to close window when focus lost
     *
     * @return this
     */
    public FxWindowPresenter withCloseOnFocusLost(boolean _closeOnFocusLost) {
        windowOptions.withCloseOnFocusLost(_closeOnFocusLost);
        return this;
    }

    /**
     * Set the icon to disable in the taskbar/upper corner of the window (if enabled by window manager).
     * If null is given or icon could not be found no (custom) icon will be used.
     *
     * @param _icon icon to show
     *
     * @return this
     */
    public FxWindowPresenter withIcon(String _icon) {
        windowOptions.withIcon(_icon);
        return this;
    }

    /**
     * Setup multiple window icons used in taskbar/window decoration.
     * The icon should use the same name and end with the size and a supported file extension.
     * E.g.: my_name_16x16.png, my_name_32x32.png
     *
     * @param _icons icon names
     * @return this
     */
    public FxWindowPresenter withIcons(List<String> _icon) {
        windowOptions.withIcons(_icon);
        return this;
    }

    /**
     * Run this runnable when window is getting closed.
     *
     * @param _runOnClose runnable
     *
     * @return this
     */
    public FxWindowPresenter withRunOnClose(Runnable _runOnClose) {
        windowOptions.withRunOnClose((c, s) -> _runOnClose.run());
        return this;
    }

    /**
     * Run this BiConsumer when window is getting closed.
     * BiConsumer will receive controller and stage of the window which is getting closed.
     *
     * @param _runOnClose BiConsumer
     *
     * @return this
     */
    public FxWindowPresenter withRunOnClose(BiConsumer<Initializable, Stage> _runOnClose) {
        windowOptions.withRunOnClose(_runOnClose);
        return this;
    }

    /**
     * Run this runnable when window is about to be shown.
     *
     * @param _runOnShow runnable
     *
     * @return this
     */
    public FxWindowPresenter withRunOnShow(Runnable _runOnShow) {
        windowOptions.withRunOnShow((c, s) -> _runOnShow.run());
        return this;
    }

    /**
     * Run this BiConsumer when window is about to be shown.
     * BiConsumer will receive controller and stage of the window which is getting shown.
     *
     * @param _runOnShow BiConsumer
     *
     * @return this
     */
    public FxWindowPresenter withRunOnShow(BiConsumer<Initializable, Stage> _runOnShow) {
        windowOptions.withRunOnShow(_runOnShow);
        return this;
    }

    /**
     * Set this to true to allow showing this window only once at the same time.<br>
     * You can use {@link #withWindowAlreadyShowingHandler(Runnable)} to execute
     * additional actions in case the window is already opened.
     *
     * @param _onlyOnce true to allow window only once at the same time
     *
     * @return this
     */
    public FxWindowPresenter withOnlyOnce(boolean _onlyOnce) {
        windowOptions.withOnlyOnce(_onlyOnce);
        return this;
    }

    /**
     * Adds the given Css files to the supported/used style sheets.<br>
     * These style sheets will be used (or pseudoclasses will be available)
     * in the created/shown window.
     *
     * @param _cssFiles list of css files to add
     *
     * @return this
     */
    public FxWindowPresenter withCssStyleSheets(List<String> _cssFiles) {
        if (_cssFiles != null) {
            windowOptions.withCssStyleSheets(_cssFiles);
        }
        return this;
    }

    /**
     * Adds the given Css files to the supported/used style sheets.<br>
     * These style sheets will be used (or pseudoclasses will be available)
     * in the created/shown window.
     *
     * @param _cssFiles list of css files to add
     *
     * @return this
     */
    public FxWindowPresenter withCssStyleSheets(String... _cssFiles) {
        if (_cssFiles != null) {
            List<String> collect = Arrays.stream(_cssFiles).filter(Objects::nonNull).collect(Collectors.toList());
            windowOptions.withCssStyleSheets(collect);
        }
        return this;
    }


    /**
     * Title of the window which will be created.
     *
     * @param _windowTitle title
     *
     * @return this
     */
    public FxWindowPresenter withWindowTitle(String _windowTitle) {
        windowTitle = _windowTitle;
        return this;
    }

    /**
     * Use this controller instance instead of the controller referenced in FXML file.
     * This should also be used when no controller is referenced in FXML.
     *
     * @param _controllerInstance
     * @return this
     */
    public FxWindowPresenter withControllerInstance(Object _controllerInstance) {
        controllerInstance = _controllerInstance;
        return this;
    }

    /**
     * Use custom controller factory to create new controller instances for this window.<br>
     * Will override {@link #withControllerInstance(Object)} when both options are set.
     *
     * @param _controllerInstance
     * @return this
     */
    public FxWindowPresenter withControllerFactory(Callback<Class<?>, Object> _controllerFactory) {
        controllerFactory = _controllerFactory;
        return this;
    }

    /**
     * FXML file containing the window definition (e.g. created with SceneBuilder).
     *
     * @param _fxmlFile fxml file
     *
     * @return this
     */
    public FxWindowPresenter withFxmlFile(String _fxmlFile) {
        fxmlFile = _fxmlFile;
        return this;
    }

    /**
     * Use the given stage as parent owner stage (e.g. required for modality).
     * If null is given, no parent will be set, therefore {@link Modality#WINDOW_MODAL}
     * will not block the parent stage. This will also cause the application to keep running
     * if main stage was closed but sub window was not.
     *
     * @param _ownerStage stage
     *
     * @return this
     */
    public FxWindowPresenter withOwnerStage(Stage _ownerStage) {
        ownerStage = _ownerStage;
        return this;
    }

    /**
     * Class which will be used as starting point to find the configured FXML file in class path.
     * If not set, {@link FxWindowPresenter} is used as starting point.
     *
     * @param _rootClass root class
     *
     * @return this
     */
    public FxWindowPresenter withRootClass(Class<?> _rootClass) {
        rootClass = _rootClass;
        return this;
    }

    /**
     * Toggles if the window is opened using showAndWait() or show().
     * When wait is true, the showing window will block until it is closed.
     * <p>
     * This will implicitly be set to true if any result of a window is expected to be returned.
     * Otherwise the calling code will continue before there is any result provided.
     *
     * @param _wait true to wait for window closing
     *
     * @return this
     */
    public FxWindowPresenter withWait(boolean _wait) {
        wait = _wait;
        return this;
    }

    /**
     * Modality to use.
     * Defaults to {@link Modality#NONE}.
     *
     * @param _modality modality
     *
     * @return this
     */
    public FxWindowPresenter withModality(Modality _modality) {
        modality = _modality == null ? Modality.NONE : _modality;
        return this;
    }

    /**
     * (Re)-use the given owner stage to show the FXML instead of creating a new stage.
     * This should be true if you use {@link FxWindowPresenter} to show your primary stage
     * on startup.
     *
     * @param _useOwnerStage true to reuse owner, false otherwise
     *
     * @return this
     */
    public FxWindowPresenter withUseOwnerStage(boolean _useOwnerStage) {
        useOwnerStage = _useOwnerStage;
        return this;
    }

    /**
     * Sets a function which will be called when a window was configured to be only
     * opened once at a time but tried to get opened a second time.
     * The function will receive the already existing stage for the FXML.
     *
     * @param _handler
     *
     * @return this
     */
    public FxWindowPresenter withWindowAlreadyShowingHandler(Consumer<Stage> _handler) {
        windowAlreadyShowingHandler = _handler;
        return this;
    }

    /**
     * Create a new instance.
     *
     * @return new instance
     */
    public static FxWindowPresenter create() {
        return new FxWindowPresenter();
    }

    /**
     * Shows the window based on the configuration.
     * Will pass in a value and return a value.
     *
     * @param <T> input object type
     * @param <C> type of return value
     * @param _resultClass class expected to be returned
     * @param _inputObject input object
     *
     * @return whatever the window returns or null on failure
     */
    public <T, C> C showWindow(Class<C> _resultClass, T _inputObject) {
        if (StringHelper.isBlank(fxmlFile)) {
            throw new IllegalArgumentException("No FXML file set");
        }

        // if a result is expected, we always have to wait
        if (_resultClass != null) {
            wait = true;
        }

        try {
            return FxWindowUtils.showWindowWithValueAndReturn(ownerStage, rootClass, useOwnerStage, fxmlFile, wait,
                    modality, windowOptions, windowTitle, controllerInstance, controllerFactory, _resultClass, _inputObject);
        } catch (WindowAlreadyOpenedException _ex) {
            if (windowAlreadyShowingHandler != null) {
                windowAlreadyShowingHandler.accept(_ex.getOpenedStage());
            }
            return null;
        }
    }

    /**
     * Shows the window based on configuration without
     * any values passed to the window or retrived from the window.
     */
    public void showWindow() {
        showWindow(null, null);
    }

    /**
     * Shows the window based on configuration.
     * Passes the given value to the created window.
     *
     * @param <T> type to pass in
     * @param _inputObject object to pass to window
     */
    public <T> void showWindow(T _inputObject) {
        showWindow(null, _inputObject);
    }

    /**
     * Shows the window based on configuration and returns
     * whatever the window returns.
     *
     * @param <C> type of window return
     * @param _resultClass class of expected window return value
     *
     * @return whatever window returns, maybe null
     */
    public <C> C showWindow(Class<C> _resultClass) {
        return showWindow(_resultClass, null);
    }
}
