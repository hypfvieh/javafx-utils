package com.github.hypfvieh.javafx.fx;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.hypfvieh.javafx.ui.BaseWindowController;
import com.github.hypfvieh.javafx.windows.interfaces.IBlockClose;
import com.github.hypfvieh.javafx.windows.interfaces.ICssStyle;
import com.github.hypfvieh.javafx.windows.interfaces.ICustomInitialize;
import com.github.hypfvieh.javafx.windows.interfaces.IKeyboardShortcut;
import com.github.hypfvieh.javafx.windows.interfaces.IObjectConsumer;
import com.github.hypfvieh.javafx.windows.interfaces.IResultProvider;
import com.github.hypfvieh.javafx.windows.interfaces.ISaveOnClose;
import com.github.hypfvieh.javafx.windows.interfaces.IStageControllerAware;
import com.github.hypfvieh.javafx.windowsaver.WindowPositionSaver;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Utilities to show, close or modify JavaFx windows.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-10-05
 */
public class FxWindowUtils {
    private static final Logger   LOGGER = System.getLogger(FxWindowUtils.class.getName());

    private static final List<String> CSS_THEMES = new ArrayList<>();
    private static String default_window_icon = null;

    /** It is required to set this to false when using TestFx, otherwise TestFx will get stuck. */
    private static boolean exitAfterLastWindow = true;

    /** Contains all windows flagged with 'onlyonce' opened by FxWindowUtils. */ 
    private static final Map<String, WeakReference<Stage>> OPENED_WINDOWS = new ConcurrentHashMap<>();
    
    /**
     * Enable/Disable application termination if last JavaFx window gets closed.
     * <p>
     * <strong>Note:</strong> When using TestFx, set this to false!
     * @param _exit true to enable, false to disable
     */
    public static void setExitAfterLastWindow(boolean _exit) {
        exitAfterLastWindow = _exit;
    }

    /**
     * Add a new CSS Style sheet file to the list of CSS Style sheets which will be added to every new created window.
     * @param _fileName file to add
     */
    public static void addCssThemeFile(String _fileName) {
        if (_fileName == null || _fileName.isBlank()) {
            return;
        }

        CSS_THEMES.add(_fileName);
    }

    /**
     * Returns the list of currently configured CSS stylesheets.
     *
     * @return list, maybe empty
     */
    public static List<String> getCssThemes() {
        return Collections.unmodifiableList(CSS_THEMES);
    }


    /**
     * Set the default icon to use when opening a new window.
     * Use null to disable default icon.
     *
     * @param _fileName icon file name
     */
    public static void setDefaultWindowIcon(String _fileName) {
        default_window_icon = _fileName;
    }
    /**
     * Opens a JavaFX window using an FXML file.
     *
     * @param _parent parent class (cannot be null)
     * @param _fXmlFile fxml file to load
     * @param _wait use showAndWait instead of show
     * @param _modal set window modality
     * @param _resizeable set window resizable
     * @param _title set window title
     *
     * @param <B> parent controller type
     */
    public static <B extends Initializable> void showWindow(B _parent, String _fXmlFile, boolean _wait, Modality _modal,
            boolean _resizeable, String _title) {
        showWindowWithValue(_parent, _fXmlFile, _wait, _modal, _resizeable, _title, null);
    }

    /**
     * Show a window or dialog with certain features like setting and getting values.
     * <p>
     * To provide a custom icon for the dialog either set the {@link WindowOptions#icon} property or add an image as resource in a subfolder called "images".<br>
     * The image should have the same name as the controller class and end with '.png'.
     * <p>
     * Please note: Getting a value only works if the window is blocking (wait = true).
     *
     * @param _rootStage stage to use
     * @param _rootClass class to use to find fxml files
     * @param _useRootStage use the given rootStage to show window instead of creating a new stage, this is mainly imported if method is used to show the primary stage when FX application starts
     * @param _fXmlFile FXML UI file to load
     * @param _wait if true, block until window is closed, false to continue (false will not allow you to get values)
     * @param _modal modality mode
     * @param windowOptions additional window options
     * @param _title title for the window
     * @param _resultClass result class of object retrieved from window controller (only possible if controller implements {@link IResultProvider})
     *                      Use null here to disable retrieval of values
     * @param _obj object to pass to the window controller (only possible if controller implements {@link IObjectConsumer})
     *              Use null to not pass any value to the controller
     * @return retrieved value of resultClass type or null
     *
     * @param <T> input object class
     * @param <C> output object class
     * 
     * @throws WindowAlreadyOpenedException if window should only be shown once at a time but is opened a second time
     * @throws NullPointerException when fxml file could not be found by classloader
     * @throws IllegalArgumentException when controller does not implement required interfaces for some actions (e.g. receiving or returning values)
     */
    @SuppressWarnings("unchecked")
    public static <T, C> C showWindowWithValueAndReturn(Stage _rootStage, Class<?> _rootClass, boolean _useRootStage, String _fXmlFile, boolean _wait, Modality _modal,
            WindowOptions _windowOptions, String _title, Class<C> _resultClass, T _obj) {

        WindowOptions windowOptions = _windowOptions == null ? new WindowOptions() : _windowOptions;
        
        if (windowOptions.isOnlyOnce() && OPENED_WINDOWS.containsKey(_fXmlFile)) {
            throw new WindowAlreadyOpenedException(OPENED_WINDOWS.get(_fXmlFile).get(), "Window " + _fXmlFile + " already opened");
        }
        
        Class<?> rootClass = _rootClass;
        if (_rootClass == null) {
            rootClass = FxWindowUtils.class;
        }
        try {
            URL url = rootClass.getClassLoader().getResource(_fXmlFile);
            if (url == null) {
                throw new NullPointerException("FXML resource " + _fXmlFile + " could not be found using classloader of " + rootClass.getName());
            }

            FXMLLoader fxmlloader = new FXMLLoader(url);
            fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
            fxmlloader.load();

            Object controller = fxmlloader.getController();

            Stage stage ;
            if (_useRootStage) {
                stage = _rootStage;
            } else {
                stage = new Stage();
                stage.initOwner(_rootStage);
            }
            
            stage.setUserData(controller);

            if (controller instanceof BaseWindowController) {
                ((BaseWindowController) controller).setControllerStage(stage);
            }

            if (_obj != null && !(controller instanceof IObjectConsumer)) {
                throw new IllegalArgumentException("Controller configured in FXML " + _fXmlFile + " does not implement required interface " + IObjectConsumer.class.getName());
            }

            if (_resultClass != null && !(controller instanceof IResultProvider)) {
                throw new IllegalArgumentException("Controller configured in FXML " + _fXmlFile + " does not implement required interface " + IResultProvider.class.getName());
            }

            if (controller instanceof IObjectConsumer) {
                ((IObjectConsumer<T>) controller).setValue(_obj);
            }

            Parent root = fxmlloader.getRoot();


            stage.initModality(_modal);
            stage.setTitle(_title);

            if (windowOptions.isResizeable() != null) {
                stage.setResizable(windowOptions.isResizeable());
            }
            
            if (windowOptions.isMaximize() != null) {
                stage.setMaximized(windowOptions.isMaximize());
            }

            if (windowOptions.isAlwaysOnTop()) {
                stage.setAlwaysOnTop(true);
            } else if (windowOptions.isCloseOnFocusLost()) {
                stage.focusedProperty().addListener(new ChangeListener<Boolean>() {

                    @Override
                    public void changed(ObservableValue<? extends Boolean> _observable, Boolean _oldValue,
                            Boolean _newValue) {
                        if (_newValue != null && _newValue == false) {
                            stage.close();
                        }
                    }
                });
            }

            if (windowOptions.getWidth() > 0) {
                stage.setWidth(windowOptions.getWidth());
            }
            if (windowOptions.getHeight() > 0) {
                stage.setHeight(windowOptions.getHeight());
            }

            List<String> possibleIcons = new ArrayList<>();
            possibleIcons.add(windowOptions.getIcon());
            possibleIcons.add("images/" + controller.getClass().getSimpleName() + ".png");
            possibleIcons.add(default_window_icon);

            // find a proper icon and set it, if none is found, no icon will be set
            for (String iconFile : possibleIcons) {
                if (iconFile == null || iconFile.isBlank()) {
                    continue;
                }
                if (FxWindowUtils.class.getClassLoader().getResource(iconFile) == null) {
                    continue;
                }
                try (InputStream imgStream = FxWindowUtils.class.getClassLoader().getResourceAsStream(iconFile)) {
                    if (imgStream != null) {
                        stage.getIcons().add(new Image(imgStream));
                        break; // we have an icon
                    }
                }
            }

            Scene scene = new Scene(root);

            if (!windowOptions.getCssStyleSheets().isEmpty()) {
                scene.getStylesheets().addAll(windowOptions.getCssStyleSheets());
            }
            
            if (controller instanceof ICssStyle) {
                ICssStyle cssStyle = (ICssStyle) controller;
                List<String> cssStyleFiles = cssStyle.getCssStyleFiles();
                if (cssStyleFiles != null && !cssStyleFiles.isEmpty()) {
                    scene.getStylesheets().addAll(cssStyle.getCssStyleFiles());
                    if (!cssStyle.replaceDefaultStyles()) {
                        scene.getStylesheets().addAll(CSS_THEMES);
                    }
                }
            } else {
                scene.getStylesheets().addAll(CSS_THEMES);
            }
            if (controller instanceof IKeyboardShortcut && ((IKeyboardShortcut) controller).getGlobalShortcuts() != null) {
                for (Entry<KeyCombination, Runnable> e : ((IKeyboardShortcut) controller).getGlobalShortcuts().entrySet()) {

                    scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                        final KeyCombination keyComb = e.getKey();

                        @Override
                        public void handle(KeyEvent _event) {
                            if (keyComb.match(_event)) {
                                e.getValue().run();
                                _event.consume(); // consume event
                            }
                        }
                    });
                }
            }

            stage.setScene(scene);

            // center window to parent stage
            if (!_useRootStage && _rootStage != null) {
                // show stage and hide it again, required to set the height/width of the new stage for calculation
                stage.show();
                stage.hide();
                stage.setX((_rootStage.getX() + _rootStage.getWidth() / 2 - stage.getWidth() / 2) + 10);
                stage.setY((_rootStage.getY() + _rootStage.getHeight() / 2 - stage.getHeight() / 2) + 10);
            }

            AtomicBoolean systemClosedButtonUsed = new AtomicBoolean(false);

            stage.setOnCloseRequest(ev -> {
                blockClose(windowOptions, controller, stage, ev);
                saveOnClose(windowOptions, controller, stage, systemClosedButtonUsed, ev);
                
                OPENED_WINDOWS.remove(_fXmlFile);
                
                WindowPositionSaver.saveWindowPosition(((Initializable) controller), stage);
                if (windowOptions.getRunOnClose() != null) {
                    windowOptions.getRunOnClose().run();
                }
            });

            // do custom initialize as late as possible so we have stage and scene ready to use in controller when
            // custom initialize is called
            stage.setOnShown(ev -> {
                if (controller instanceof ICustomInitialize) {
                    ((ICustomInitialize) controller).customInitialize();
                }
                if (windowOptions.getRunOnShow() != null) {
                    windowOptions.getRunOnShow().run();
                }
                if (WindowPositionSaver.isEnabled()) {
                    // restore window settings after stage has been initialized
                    WindowPositionSaver.restoreWindowPosition(((Initializable) controller), stage, root);
                }
            });

            if (windowOptions.isOnlyOnce()) {
                OPENED_WINDOWS.put(_fXmlFile, new WeakReference<>(stage));
            }

            if (_wait) {
                stage.showAndWait();
            } else {
                stage.show();
            }

            if (controller instanceof Closeable) {
                ((Closeable) controller).close();
            }

            // return a proper result if we have a return class and the controller is an instance of IResultProvider
            if (_resultClass != null && controller instanceof IResultProvider) {
                // controller should not return a value when system close is used
                if (!((IResultProvider<?>) controller).returnValueOnSystemClose() && systemClosedButtonUsed.get()) {
                    return null;
                }

                return ((IResultProvider<C>) controller).getValue();
            }
        } catch (RuntimeException _ex) {
            throw _ex;
        } catch (Exception _ex) {
            LOGGER.log(Level.ERROR, "Error while showing window:", _ex);
        }
        return null;
    }

    private static void blockClose(WindowOptions _sizeSettings, Object controller, Stage stage, WindowEvent ev) {
        if (controller instanceof IBlockClose && !((IBlockClose) controller).allowClose()) {
            ev.consume();
            Runnable action = ((IBlockClose) controller).getBlockAction();
            if (action != null) {
                action.run();
            }
            if (_sizeSettings.getRunOnClose() != null) {
                _sizeSettings.getRunOnClose().run();
            }
        }
    }

    private static void saveOnClose(WindowOptions _sizeSettings, Object _controller, Stage _stage,
            AtomicBoolean _systemClosedButtonUsed, WindowEvent _ev) {
        if (_controller instanceof ISaveOnClose) {
            try {
                if (!((ISaveOnClose) _controller).saveAndClose()) {
                    _ev.consume(); // consume event, do not close window
                }

                if (_controller instanceof IBlockClose && !((IBlockClose) _controller).allowClose()) {
                    _ev.consume();
                    Runnable action = ((IBlockClose) _controller).getBlockAction();
                    if (action != null) {
                        action.run();
                    }
                }
                if (_controller instanceof BaseWindowController) {
                    _systemClosedButtonUsed.set(((BaseWindowController) _stage.getUserData()).isClosedByWindowManager());
                }
                if (_sizeSettings.getRunOnClose() != null) {
                    _sizeSettings.getRunOnClose().run();
                }
            } catch (Exception _ex) {
                new RuntimeException("Error executing closing action.", _ex);
            }
        }

    }

    /**
     * Show a window or dialog with certain features like setting and getting values.
     * <p>
     * To provide a custom icon for the dialog either set the {@link WindowOptions#icon} property or add an image as resource in a subfolder called "images".<br>
     * The image should have the same name as the controller class and end with '.png'.
     * <p>
     * Please note: Getting a value only works if the window is blocking (wait = true).
     *
     * @param _parentWindow parent window
     * @param _fXmlFile FXML UI file to load
     * @param _wait if true, block until window is closed, false to continue (false will not allow you to get values)
     * @param _modal modality mode
     * @param _sizeSettings window sizing settings
     * @param _title title for the window
     * @param _resultClass result class of object retrieved from window controller (only possible if controller implements {@link IResultProvider})
     *                      Use null here to disable retrieval of values
     * @param _obj object to pass to the window controller (only possible if controller implements {@link IObjectConsumer})
     *              Use null to not pass any value to the controller
     * @return retrieved value of resultClass type or null
     * @param <T> input object class
     * @param <C> output object class
     * @param <B> window controller implementing {@link Initializable}
     */
    public static <B extends Initializable, T, C> C showWindowWithValueAndReturn(B _parentWindow, String _fXmlFile, boolean _wait, Modality _modal,
            WindowOptions _sizeSettings, String _title, Class<C> _resultClass, T _obj) {

        Stage rootStage = null;
        Class<?> rootClass = FxWindowUtils.class;

        if (_parentWindow != null && _parentWindow instanceof IStageControllerAware) {
            rootStage = ((IStageControllerAware) _parentWindow).getControllerStage();
            rootClass = rootStage != null ? rootStage.getClass() : FxWindowUtils.class;
        }

        return showWindowWithValueAndReturn(rootStage, rootClass, false, _fXmlFile, _wait, _modal,
                _sizeSettings, _title,  _resultClass, _obj);
    }

    /**
     * Opens a new window, provides the given value to it and returns a value.
     *
     * @param <B> parent type
     * @param <T> input object type
     * @param <C> resulting object type
     *
     * @param _parent parent window
     * @param _fXmlFile fxml file to load
     * @param _modal modality mode
     * @param _resizeable true to allow window to be resized
     * @param _maximized true to open window maximized
     * @param _title window title
     * @param _resultClass class of the resulting value
     * @param _obj object to provide
     *
     * @return an instance of T or null
     */
    public static <B extends Initializable, T, C> C showWindowWithValueAndReturn(B _parent, String _fXmlFile, Modality _modal,
            boolean _resizeable, boolean _maximized, String _title, Class<C> _resultClass, T _obj) {

        WindowOptions sizeSettings = WindowOptions.build().withMaximize(_maximized).withResizeable(_resizeable);

        return showWindowWithValueAndReturn(_parent, _fXmlFile, true, _modal, sizeSettings, _title, _resultClass, _obj);
    }

    /**
     * Opens a new window, provides the given value to it and returns a value.
     *
     * @param <B> parent type
     * @param <T> input object type
     * @param <C> resulting object type
     *
     * @param _parent parent window
     * @param _fXmlFile fxml file to load
     * @param _modal modality mode
     * @param _resizeable true to allow window to be resized
     * @param _title window title
     * @param _resultClass class of the resulting value
     * @param _obj object to provide

     * @return an instance of T or null

     */
    public static <B extends Initializable, T, C> C showWindowWithValueAndReturn(B _parent, String _fXmlFile,
            Modality _modal, boolean _resizeable, String _title, Class<C> _resultClass, T _obj) {
        return showWindowWithValueAndReturn(_parent, _fXmlFile, _modal, _resizeable, false, _title, _resultClass, _obj);
    }

    /**
     * Opens a new window and provides the given value to it.
     *
     * @param <B> parent type
     * @param <T> value type
     * @param _parent parent window
     * @param _fXmlFile fxml file to load
     * @param _wait true to block until window is closed
     * @param _modal modality mode
     * @param _resizeable true to allow window to be resized
     * @param _title window title
     * @param _obj object to provide
     */
    public static <B extends Initializable, T> void showWindowWithValue(B _parent, String _fXmlFile, boolean _wait, Modality _modal,
            boolean _resizeable, String _title, T _obj) {

        WindowOptions options = WindowOptions.build().withResizeable(_resizeable);
        showWindowWithValueAndReturn(_parent, _fXmlFile, _wait, _modal, options, _title, null, _obj);
    }

    /**
     * Opens a window and waits for return value.
     *
     * @param <B> parent type
     * @param <T> value type
     * @param _parent parent window
     * @param _resizeable true to allow window to be resized
     * @param _title window title
     * @param _fxml fxml file to load
     * @param _resultClass class of the resulting value
     *
     * @return instance of result class type or null
     */
    public static <B extends Initializable, T> T showWindowWithReturn(B _parent, boolean _resizeable, String _title, String _fxml, Class<T> _resultClass) {
        return showWindowWithValueAndReturn(_parent, _fxml, Modality.APPLICATION_MODAL, _resizeable, _title, _resultClass, null);
    }

    /**
     * Closes a JavaFX stage (close window).
     *
     * @param _javaFxComponentOrNode component to get stage from
     */
    public static void closeWindow(Node _javaFxComponentOrNode) {
        if (_javaFxComponentOrNode.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) _javaFxComponentOrNode.getScene().getWindow();
            closeWindow(stage);
        }
    }

    /**
     * Close a window provided by the given stage.
     * @param _stage stage
     */
    public static void closeWindow(Stage _stage) {
        if (_stage != null) {
            if (_stage.getUserData() instanceof BaseWindowController) {
                ((BaseWindowController) _stage.getUserData()).setClosedByWindowManager(false);
            }
            
            // remove stage from opened window map
            String removeKey = null;
            for (Entry<String, WeakReference<Stage>> entry : OPENED_WINDOWS.entrySet()) {
                if (entry.getValue().get() == _stage) {
                    removeKey = entry.getKey();
                    break;
                }
            }
            if (removeKey != null) {
                OPENED_WINDOWS.remove(removeKey);
            }
            
            if (_stage.getUserData() instanceof ISaveOnClose) {
                ISaveOnClose x = ((ISaveOnClose) _stage.getUserData());
                if (x.saveAndClose()) {
                    _stage.close();
                }
            } else {
                _stage.close();
            }

            if (exitAfterLastWindow && Stage.getWindows().isEmpty()) { // no more windows, close application
                // fire close event to call close handlers
                _stage.getOnCloseRequest().handle(new WindowEvent(_stage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
                Platform.setImplicitExit(true);
            }
        }
    }

    /**
     * Maximizes the window of the given javafx component.
     *
     * @param _javaFxComponentOrNode component (e.g. button)
     */
    public static void maximizeWindow(Node _javaFxComponentOrNode) {
        if (_javaFxComponentOrNode.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) _javaFxComponentOrNode.getScene().getWindow();
            if (stage != null) {
                stage.setMaximized(true);
            }
        }
    }

    /**
     * Resizes the stage window of the given javafx component.
     *
     * @param _javaFxComponentOrNode component (e.g. button)
     * @param _width width to set
     * @param _height height to set
     */
    public static void resizeWindow(Node _javaFxComponentOrNode, double _width, double _height) {
        if (_javaFxComponentOrNode != null && _javaFxComponentOrNode.getScene() != null && _javaFxComponentOrNode.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) _javaFxComponentOrNode.getScene().getWindow();
            if (stage != null) {
                stage.setWidth(_width);
                stage.setHeight(_height);
            }
        }
    }




    /**
     * Window size settings used when creating a new window.
     *
     * @author hypfvieh
     * @since v11.0.0 - 2020-09-11
     */
    public static class WindowOptions {
        /** Window width */
        private double width;
        /** Window height */
        private double height;
        /** Maximize window */
        private Boolean maximize;
        /** Allow window resizing (use WindowManager icons to switch to fullscreen or use mouse to change size) */
        private Boolean resizeable;
        /** Window is always visible */
        private boolean alwaysOnTop;
        /** Window will be closed when it is no longer the active window. Will be ignored if alwaysOnTop is enabled. */
        private boolean closeOnFocusLost;
        /** Window icon. */
        private String icon;
        /** Called when window gets closed (after IBlockClose, ISaveOnClose). */
        private Runnable runOnClose;
        /** Called when window gets shown (after ICustomInitialize). */
        private Runnable runOnShow;
        /** Determine if this window should only be opened once at the same time. */
        private boolean onlyOnce;
        
        private List<String> cssStyleSheets = new ArrayList<>();
        
        public WindowOptions() {}

        public static WindowOptions build() {
            return new WindowOptions();
        }

        public double getWidth() {
            return width;
        }

        public WindowOptions withWidth(double _width) {
            width = _width;
            return this;
        }

        public double getHeight() {
            return height;
        }

        public WindowOptions withHeight(double _height) {
            height = _height;
            return this;
        }

        public Boolean isMaximize() {
            return maximize;
        }

        public WindowOptions withMaximize(Boolean _maximize) {
            maximize = _maximize;
            return this;
        }

        public Boolean isResizeable() {
            return resizeable;
        }

        public WindowOptions withResizeable(Boolean _resizeable) {
            resizeable = _resizeable;
            return this;
        }

        public boolean isAlwaysOnTop() {
            return alwaysOnTop;
        }

        public WindowOptions withAlwaysOnTop(boolean _alwaysOnTop) {
            alwaysOnTop = _alwaysOnTop;
            return this;
        }

        public boolean isCloseOnFocusLost() {
            return closeOnFocusLost;
        }

        public WindowOptions withCloseOnFocusLost(boolean _closeOnFocusLost) {
            closeOnFocusLost = _closeOnFocusLost;
            return this;
        }

        public Runnable getRunOnClose() {
            return runOnClose;
        }

        public WindowOptions withRunOnClose(Runnable _runOnClose) {
            runOnClose = _runOnClose;
            return this;
        }

        public Runnable getRunOnShow() {
            return runOnShow;
        }

        public WindowOptions withRunOnShow(Runnable _runOnShow) {
            runOnShow = _runOnShow;
            return this;
        }

        public String getIcon() {
            return icon;
        }

        public WindowOptions withIcon(String _icon) {
            icon = _icon;
            return this;
        }

        public WindowOptions withOnlyOnce(boolean _val) {
            onlyOnce = _val;
            return this;
        }

        public boolean isOnlyOnce() {
            return onlyOnce;
        }
        
        public WindowOptions withCssStyleSheets(List<String> _list) {
            if (_list != null) {
                cssStyleSheets.addAll(_list);
            }
            return this;
        }

        public List<String> getCssStyleSheets() {
            return cssStyleSheets;
        }
        
    }

    /**
     * Thrown if a window flagged as 'onlyonce' is tried to be opened a second time.
     */
    public static class WindowAlreadyOpenedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final Stage openedStage;
        
        public WindowAlreadyOpenedException(Stage _openedStage, String _message) {
            super(_message);
            openedStage = _openedStage;
        }

        public Stage getOpenedStage() {
            return openedStage;
        }
        
    }
    
}
