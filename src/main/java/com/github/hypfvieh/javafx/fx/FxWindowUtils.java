package com.github.hypfvieh.javafx.fx;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.javafx.ui.BaseWindowController;
import com.github.hypfvieh.javafx.windows.interfaces.IBlockClose;
import com.github.hypfvieh.javafx.windows.interfaces.ICssStyle;
import com.github.hypfvieh.javafx.windows.interfaces.ICustomInitialize;
import com.github.hypfvieh.javafx.windows.interfaces.IKeyboardShortcut;
import com.github.hypfvieh.javafx.windows.interfaces.IObjectConsumer;
import com.github.hypfvieh.javafx.windows.interfaces.IResultProvider;
import com.github.hypfvieh.javafx.windows.interfaces.ISaveOnClose;
import com.github.hypfvieh.javafx.windowsaver.WindowPositionSaver;

public class FxWindowUtils {
    private static final Logger   LOGGER = LoggerFactory.getLogger(FxWindowUtils.class);

    private static final List<String> CSS_THEMES = new ArrayList<>();
    private static String default_window_icon = null;

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
     * @param _fileName
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
     */
    public static <B extends Initializable> void showWindow(B _parent, String _fXmlFile, boolean _wait, Modality _modal,
            boolean _resizeable, String _title) {
        showWindowWithValue(_parent, _fXmlFile, _wait, _modal, _resizeable, _title, null);
    }

    /**
     * Show a window or dialog with certain features like setting and getting values.
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
     */
    @SuppressWarnings("unchecked")
    public static <B extends Initializable, T, C> C showWindowWithValueAndReturn(B _parentWindow, String _fXmlFile, boolean _wait, Modality _modal,
            WindowOptions _sizeSettings, String _title, Class<C> _resultClass, T _obj) {

        Stage rootStage = null;
        Class<?> rootClass = FxWindowUtils.class;
        if (_parentWindow != null && _parentWindow instanceof BaseWindowController) {
            rootStage = ((BaseWindowController) _parentWindow).getControllerStage();
            rootClass = rootStage != null ? rootStage.getClass() : FxWindowUtils.class;
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

            Stage stage = new Stage();
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

            stage.setResizable(_sizeSettings.isResizeable());
            stage.setMaximized(_sizeSettings.isMaximize());

            if (_sizeSettings.isAlwaysOnTop()) {
                stage.setAlwaysOnTop(true);
            } else if (_sizeSettings.isCloseOnFocusLost()) {
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

            if (_sizeSettings.getWidth() > 0) {
                stage.setWidth(_sizeSettings.getWidth());
            }
            if (_sizeSettings.getHeight() > 0) {
                stage.setHeight(_sizeSettings.getHeight());
            }

            InputStream imgStream = FxWindowUtils.class.getClassLoader().getResourceAsStream("images/" + controller.getClass().getSimpleName() + ".png");
            if (imgStream == null && default_window_icon != null) {
                imgStream = FxWindowUtils.class.getClassLoader().getResourceAsStream(default_window_icon);
            }
            if (imgStream != null) {
                stage.getIcons().add(new Image(imgStream));
            }

            Scene scene = new Scene(root);

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
            if (rootStage != null) {
                // show stage and hide it again, required to set the height/width of the new stage for calculation
                stage.show();
                stage.hide();
                stage.setX((rootStage.getX() + rootStage.getWidth() / 2 - stage.getWidth() / 2) + 10);
                stage.setY((rootStage.getY() + rootStage.getHeight() / 2 - stage.getHeight() / 2) + 10);
            }

            AtomicBoolean systemClosedButtonUsed = new AtomicBoolean(false);

            if (controller instanceof ISaveOnClose) {
                stage.setOnCloseRequest(ev -> {
                    try {
                        if (!((ISaveOnClose) controller).saveAndClose()) {
                            ev.consume(); // consume event, do not close window
                        }

                        if (controller instanceof IBlockClose && !((IBlockClose) controller).allowClose()) {
                            ev.consume();
                            Runnable action = ((IBlockClose) controller).getBlockAction();
                            if (action != null) {
                                action.run();
                            }
                        }
                        if (controller instanceof BaseWindowController) {
                            systemClosedButtonUsed.set(((BaseWindowController) stage.getUserData()).isClosedByWindowManager());
                        }

                    } catch (Exception _ex) {
                        new RuntimeException("Error executing closing action.", _ex);
                    }
                    WindowPositionSaver.saveWindowPosition(((Initializable) controller), stage);
                });
            } else if (controller instanceof IBlockClose) {
                stage.setOnCloseRequest(ev -> {
                    if (!((IBlockClose) controller).allowClose()) {
                        ev.consume();
                        Runnable action = ((IBlockClose) controller).getBlockAction();
                        if (action != null) {
                            action.run();
                        }
                    }
                    WindowPositionSaver.saveWindowPosition(((Initializable) controller), stage);
                });
            } else {
                stage.setOnCloseRequest(ev -> {
                    WindowPositionSaver.saveWindowPosition(((Initializable) controller), stage);
                });
            }

            // do custom initialize as late as possible so we have stage and scene ready to use in controller when
            // custom initialize is called
            stage.setOnShown(ev -> {
                if (controller instanceof ICustomInitialize) {
                    ((ICustomInitialize) controller).customInitialize();
                }
            });

            WindowPositionSaver.restoreWindowPosition(((Initializable) controller), stage, root);

            if (_wait) {
                stage.showAndWait();
            } else {
                stage.show();
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
            LOGGER.error("Error while showing window:", _ex);
        }
        return null;
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

            if (_stage.getUserData() instanceof ISaveOnClose) {
                ISaveOnClose x = ((ISaveOnClose) _stage.getUserData());
                if (x.saveAndClose()) {
                    _stage.close();
                }
            } else {
                _stage.close();
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
        private boolean maximize;
        /** Allow window resizing (use WindowManager icons to switch to fullscreen or use mouse to change size) */
        private boolean resizeable;
        /** Window is always visible */
        private boolean alwaysOnTop;
        /** Window will be closed when it is no longer the active window. Will be ignored if alwaysOnTop is enabled. */
        private boolean closeOnFocusLost;

        private WindowOptions() {}

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

        public boolean isMaximize() {
            return maximize;
        }

        public WindowOptions withMaximize(boolean _maximize) {
            maximize = _maximize;
            return this;
        }

        public boolean isResizeable() {
            return resizeable;
        }

        public WindowOptions withResizeable(boolean _resizeable) {
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
    }

}
