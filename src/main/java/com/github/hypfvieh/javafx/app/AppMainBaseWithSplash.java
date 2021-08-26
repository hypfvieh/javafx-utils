package com.github.hypfvieh.javafx.app;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.github.hypfvieh.javafx.fx.FxDialogUtils;
import com.github.hypfvieh.javafx.fx.FxWindowUtils;
import com.github.hypfvieh.javafx.fx.FxWindowUtils.WindowOptions;
import com.github.hypfvieh.javafx.other.AppLock;
import com.github.hypfvieh.javafx.other.AppLock.AppAlreadyRunningException;
import com.github.hypfvieh.javafx.utils.StringHelper;
import com.github.hypfvieh.javafx.utils.Translator;

/**
 * A base application class to show splash screen and handle startup/teardown actions as well as exception handling on
 * startup.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public abstract class AppMainBaseWithSplash extends Application {

    public final Logger      logger               = System.getLogger(AppMainBaseWithSplash.class.getName());
    private final Translator translator           = new Translator("AppMainBaseWithSplash");

    private SplashAppConfig config;

    public AppMainBaseWithSplash() {
        super();
        initImpl();
    }

    /**
     * Initialize application base information.
     * @return {@link SplashAppConfig}
     */
    protected abstract SplashAppConfig initialize();

    /**
     * Called internally when constructor is called by {@link Application}.
     */
    private void initImpl() {
        config = Objects.requireNonNull(initialize(), "Application config required");
        if (StringHelper.isBlank(config.getMainWindowFxml())) {
            throw new IllegalArgumentException("Window FXML cannot be empty or null!");
        }
        // setup the app icon as default icon
        FxWindowUtils.setDefaultWindowIcon(config.getAppIcon());
    }

    /**
     * Task which is called after the splash screen is getting visible.
     * <p>
     * Use this to do some long running tasks before the main application is visible. You can use
     * {@link Task}.updateMessage and {@link Task}.updateProgress to use a label and progress bar.
     *
     * @param _stage the primary stage
     *
     * @return task or null to do nothing
     */
    public abstract Task<Void> startupTaskAction(Stage _stage);

    /**
     * Runnable which will be executed when application is closed.
     * Override this method if you need to execute something when window should be closed.
     *
     * @param _stage the primary stage
     *
     * @return runnable or null to do nothing
     */
    public Runnable onMainWindowCloseAction(Stage _stage) {
        return null;
    }

    /**
     * Runnable which will be executed when application window is shown.
     * Override this method if you need to execute something after the main window's OnShown method is called.
     *
     * @param _stage the primary stage
     *
     * @return runnable or null to do nothing
     */
    public Runnable onMainWindowShownAction(Stage _stage) {
        return null;
    }

    /**
     * Called when application is started with {@link AppLock} support and there was already an instance running.
     * <p>
     * Default will show a error message to the user and exit the application. <br>
     * Overwrite this method to do something else.
     *
     * @param _stage initial stage
     *
     * @return Consumer, or null to do nothing
     */
    public Consumer<AppAlreadyRunningException> handleAppAlreadyRunning(Stage _stage) {
        return _ex -> {
            logger.log(Level.ERROR, "Could not start application, software already running", _ex);

            Platform.runLater(() -> {
                FxDialogUtils.showDialog(_stage, AlertType.ERROR,
                        translator.t("app_running", "Application already running"),
                        translator.t("app_already_running", "Application already running"),
                        translator.t("app_running_msg",
                                "The application is already running.%nPlease close the running instance and try again."));
            });
        };
    }

    /**
     * Called when application throws any exception (except {@link AppAlreadyRunningException}) at startup.
     * <p>
     * Default will show a error message to the user and exit the application with exit code = 1. <br>
     * Overwrite this method to do something else.
     *
     * @param _stage initial stage
     * @return Consumer, or null to do nothing
     */
    public Consumer<Exception> handleOtherStartupExceptions(Stage _stage) {
        return _ex -> {
            logger.log(Level.ERROR, "Exception while starting application:", _ex);

            Platform.setImplicitExit(true);

            Platform.runLater(() -> {
                FxDialogUtils.showDialog(_stage, AlertType.ERROR,
                        translator.t("app_could_not_be_started", "Application could not be started"),
                        translator.t("app_could_not_be_started", "Application could not be started"),
                        translator.t("app_could_not_be_started_msg",
                                "The application could not be started because of an unknown failure.%nPlease check application log if available."));
                System.exit(1);
            });
        };
    }

    /**
     * Internally used to either get the task of the user or create an empty task.
     *
     * @param _stage primary stage
     * @return task
     */
    private Task<Void> getStartupTaskInternal(Stage _stage) {
        Task<Void> startTask = startupTaskAction(_stage);
        Task<Void> task = startTask == null ? task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                return null;
            }
        } : startTask;

        return task;
    }

    @Override
    public void start(Stage _stage) throws IOException {
        Task<Void> task = getStartupTaskInternal(_stage);

        Thread.setDefaultUncaughtExceptionHandler(getUncaughtExceptionHandler(_stage));

        if (task.getOnFailed() == null) {
            task.setOnFailed(evt -> {
                logger.log(Level.ERROR, "Error while running application:", task.getException());

                String msg = translator.t("error_starting_app",
                        "Error while starting application. Please contact the support.");

                if (task.getException() instanceof RuntimeException) {
                    msg = translator.t("error_on_startup", "Error while starting application: %s",
                            task.getException().getLocalizedMessage());
                }

                _stage.hide();
                // initialization of java fx failed somehow, use swing error dialog
                JOptionPane.showMessageDialog(null,
                        msg,
                        translator.t("error_dlg_title", "Error"),
                        JOptionPane.ERROR_MESSAGE);

                System.exit(1);
            });
        }

        showSplash(_stage, task);
        new Thread(task, "Application Startup Task").start();

    }

    /**
     * Returns the exception handler which will be used for unchecked exceptions in JavaFX Application Thread.
     * Should check if the current thread is FX Application thread when trying to show any dialog.
     *
     * @param _stage initial stage
     * @return {@link UncaughtExceptionHandler}
     */
    protected UncaughtExceptionHandler getUncaughtExceptionHandler(Stage _stage) {
        return (_thread, _ex) -> {
            logger.log(Level.ERROR, "Uncaught exception in thread '{}'", _thread, _ex);
            if (Platform.isFxApplicationThread()) {
                FxDialogUtils.showExceptionDialog(_stage, AlertType.ERROR,
                        translator.t("error_fxmain_thread_title", "Error"),
                        translator.t("error_fxmain_thread_subtitle", "Fatal Error"),
                        translator.t("error_fxmain_thread_msg", "Unexpected error: %s", _ex.toString()),
                        translator.t("error_fxmain_thread_detailbtn", "Details"),
                        _ex);
            }
        };
    }

    /**
     * Display the main stage after splash is done.
     *
     * @param _stage primary stage
     * @throws IOException on error
     */
    void showMainStage(Stage _stage) throws Exception {

        Platform.setImplicitExit(false);

        BiConsumer<Stage, WindowOptions> showWindowAction = getShowMainWindowAction(config);
        if (showWindowAction == null) {
            throw new NullPointerException("Action to show main window cannot be null");
        }

        WindowOptions windowOptions = new WindowOptions();
        windowOptions
            .withResizeable(true)
            .withIcon(config.getAppIcon())
            .withRunOnClose(() -> {
                try {
                    Runnable shutdownTaskAction = onMainWindowCloseAction(_stage);
                    if (shutdownTaskAction != null) {
                        shutdownTaskAction.run();
                    }
                } catch (Exception _ex) {
                    System.getLogger(getClass().getName()).log(Level.ERROR, "Error while closing window", _ex);
                }
                Platform.setImplicitExit(true);

            })
            .withRunOnShow(() -> {
                try {
                    Runnable showAction = onMainWindowShownAction(_stage);
                    if (showAction != null) {
                        showAction.run();
                    }
                } catch (Exception _ex) {
                    System.getLogger(getClass().getName()).log(Level.ERROR, "Error while showing window", _ex);
                }
            });
        showWindowAction.accept(_stage, windowOptions);
    }

    /**
     * Shows the main window.
     * Override this if you want to do custom things before showing the main window.
     * @param _splashConfig splash configuration
     *
     * @return {@link BiConsumer}, the consumer will receive the primary stage (created by {@link Application})
     *         and preconfigured window options
     */
    protected BiConsumer<Stage, WindowOptions> getShowMainWindowAction(SplashAppConfig _splashConfig) {
        return (s, w) -> {
            FxWindowUtils.showWindowWithValueAndReturn(s, getClass(), true, _splashConfig.getMainWindowFxml(), false, Modality.NONE,
                    w, _splashConfig.getMainWindowTitle(), null, null);
        };
    }

    /**
     * Call this in your application to actual run the application.
     * <p>
     * This will not use any locking, so you can run multiple instances of your app at the same time.
     *
     * @param _args args from static main
     */
    public void runApp(String[] _args) {
        try {

            Application.launch(getClass(), _args);

        } catch (Exception _ex) {
            Consumer<Exception> handleOtherStartupExceptions = handleOtherStartupExceptions(null);
            if (handleOtherStartupExceptions != null) {
                handleOtherStartupExceptions.accept(_ex);
            }
        }
    }

    /**
     * Call this in your application to actual run the application.
     * <p>
     * This will use {@link AppLock} to prevent your application to run multiple times.
     *
     * @param _args args from static main
     */
    public void runAppWithAppLock(String[] _args) {
        try (AppLock appLock = new AppLock(getClass())) {

            Application.launch(getClass(), _args);

        } catch (AppAlreadyRunningException _ex) {
            Consumer<AppAlreadyRunningException> handleAppAlreadyRunning = handleAppAlreadyRunning(null);
            if (handleAppAlreadyRunning != null) {
                handleAppAlreadyRunning.accept(_ex);
            }
            return;
        } catch (Exception _ex) {
            Consumer<Exception> handleOtherStartupExceptions = handleOtherStartupExceptions(null);
            if (handleOtherStartupExceptions != null) {
                handleOtherStartupExceptions.accept(_ex);
            }
        }
    }

    /**
     * Show the splash screen.
     *
     * @param _initStage primary stage
     * @param _task task to execute
     */
    private void showSplash(Stage _initStage, Task<?> _task) {
        _task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                _initStage.hide();
                try {
                    showMainStage(new Stage(StageStyle.DECORATED));
                } catch (Exception _ex) {
                    Consumer<Exception> handleOtherStartupExceptions = handleOtherStartupExceptions(_initStage);
                    if (handleOtherStartupExceptions != null) {
                        handleOtherStartupExceptions.accept(_ex);
                    }
                }
            } else if (newState == Worker.State.FAILED) {
                _initStage.setAlwaysOnTop(false);
                _initStage.hide();
            }
        });

        Pane splashLayout = new VBox();
        splashLayout.setBackground(Background.EMPTY);

        ProgressBar progressBar = new ProgressBar();

        if (config.getSplashImage() != null) {
            try (InputStream stream = AppMainBaseWithSplash.class.getClassLoader().getResourceAsStream(config.getSplashImage())) {
                if (stream != null) {
                    Image image = new Image(stream);
                    ImageView splash = new ImageView(image);
                    progressBar.setPrefWidth(image.getWidth() - 20);
                    splashLayout.getChildren().add(splash);

                    Rectangle2D bounds = Screen.getPrimary().getBounds();

                    _initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - image.getWidth() / 2);
                    _initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - image.getHeight() / 2);
                }
            } catch (IOException _ex) {
                logger.log(Level.ERROR, "Could not load splash screen image {} from classpath", config.getSplashImage());
            }
        }

        Label progressLabel = new Label();
        VBox.setMargin(progressLabel, new Insets(0, 0, 0, 5));
        progressLabel.setTextFill(config.getProgressLabelTextColor());
        progressLabel.setBackground(config.getProgressLabelBackground());
        splashLayout.getChildren().add(progressLabel);

        progressBar.setStyle(config.getProgressBarCssStyle());
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.progressProperty().bind(_task.progressProperty());

        progressLabel.textProperty().bind(_task.messageProperty());

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);

        if (config.isUseProgressBar()) {
            splashLayout.getChildren().add(progressBar);
        }

        _initStage.setScene(splashScene);
        _initStage.initStyle(StageStyle.TRANSPARENT);
        _initStage.setAlwaysOnTop(true);
        _initStage.setTitle(config.getMainWindowTitle() + " - Start");
        _initStage.show();
    }

    /**
     * Configuration for a splash powered application.
     *
     * @author hypfvieh
     * @since v11.0.0 - 2020-09-12
     */
    protected static class SplashAppConfig {
        private final String mainWindowFxml;
        private final String splashImage;

        private String mainWindowTitle;
        private String appIcon;
        private String splashWindowTitle;

        private Color progressLabelTextColor = Color.ORANGERED;
        private Background progressLabelBackground = Background.EMPTY;
        private String progressBarCssStyle = "-fx-accent: orange;";

        private boolean useProgressBar = true;

        public SplashAppConfig(String _mainWindowFxml, String _splashImage) {
            mainWindowFxml = _mainWindowFxml;
            splashImage = _splashImage;
        }

        /**
         * Title to set on main window.
         *
         * @param _title title
         * @return this
         */
        public SplashAppConfig withMainWindowTitle(String _title) {
            mainWindowTitle = _title;
            return this;
        }

        /**
         * Title to set to splash window (will only be visible in taskbar).
         *
         * @param _title title
         * @return this
         */
        public SplashAppConfig withSplashWindowTitle(String _title) {
            splashWindowTitle = _title;
            return this;
        }

        /**
         * Icon to set to main application/splash window scene.
         *
         * @param _iconPath icon with path found in classpath
         * @return this
         */
        public SplashAppConfig withAppIcon(String _iconPath) {
            appIcon = _iconPath;
            return this;
        }

        /**
         * Enable / disable progress bar visible on splash.
         * @param _enable true to enable, false to disable
         * @return this
         */
        public SplashAppConfig withUseProgressBar(boolean _enable) {
            useProgressBar = _enable;
            return this;
        }


        /**
         * Color of the progress label text (default: {@link Color#ORANGERED})
         *
         * @param _color color
         * @return this
         */
        public SplashAppConfig withProgressLabelTextColor(Color _color) {
            progressLabelTextColor = _color;
            return this;
        }

        /**
         * Background style of progress label (default: {@link Background#EMPTY}).
         *
         * @param _bg background
         * @return this
         */
        public SplashAppConfig withProgressLabelBackground(Background _bg) {
            progressLabelBackground = _bg;
            return this;
        }

        /**
         * Progressbar CSS Style (default: -fx-accent: orange;).
         *
         * @param _cssStyle css style
         * @return this
         */
        public SplashAppConfig withProgressBarCssStyle(String _cssStyle) {
            progressBarCssStyle = _cssStyle;
            return this;
        }

        public String getMainWindowTitle() {
            return mainWindowTitle;
        }

        public String getMainWindowFxml() {
            return mainWindowFxml;
        }

        public String getSplashImage() {
            return splashImage;
        }

        public String getAppIcon() {
            return appIcon;
        }

        public String getSplashWindowTitle() {
            return splashWindowTitle;
        }

        public Color getProgressLabelTextColor() {
            return progressLabelTextColor;
        }

        public Background getProgressLabelBackground() {
            return progressLabelBackground;
        }

        public String getProgressBarCssStyle() {
            return progressBarCssStyle;
        }

        public boolean isUseProgressBar() {
            return useProgressBar;
        }

    }
}
