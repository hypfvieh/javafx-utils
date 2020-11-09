package com.github.hypfvieh.javafx.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public final Logger      logger               = LoggerFactory.getLogger(AppMainBaseWithSplash.class);
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
     *
     * @param _stage the primary stage
     *
     * @return runnable or null to do nothing
     */
    public abstract Runnable onMainWindowCloseAction(Stage _stage);

    /**
     * Runnable which will be executed when application window is shown.
     *
     * @param _stage the primary stage
     *
     * @return runnable or null to do nothing
     */
    public abstract Runnable onMainWindowShownAction(Stage _stage);

    /**
     * Called when application is started with {@link AppLock} support and there was already an instance running.
     * <p>
     * Default will show a error message to the user and exit the application. <br>
     * Overwrite this method to do something else.
     *
     * @return Consumer, or null to do nothing
     */
    public Consumer<AppAlreadyRunningException> handleAppAlreadyRunning() {
        return _ex -> {
            logger.error("Could not start application, software already running", _ex);

            Platform.runLater(() -> {
                FxDialogUtils.showDialog(AlertType.ERROR,
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
     * @return Consumer, or null to do nothing
     */
    public Consumer<Exception> handleOtherStartupExceptions() {
        return _ex -> {
            logger.error("Exception while starting application:", _ex);

            Platform.setImplicitExit(true);

            Platform.runLater(() -> {
                FxDialogUtils.showDialog(AlertType.ERROR,
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

        if (task.getOnFailed() == null) {
            task.setOnFailed(evt -> {
                logger.error("Error while running application:", task.getException());

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
     * Display the main stage after splash is done.
     *
     * @param _stage primary stage
     * @throws IOException on error
     */
    void showMainStage(Stage _stage) throws Exception {

        Platform.setImplicitExit(false);

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
                    LoggerFactory.getLogger(getClass()).error("Error while closing window", _ex);
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
                    LoggerFactory.getLogger(getClass()).error("Error while showing window", _ex);
                }
            });

        FxWindowUtils.showWindowWithValueAndReturn(_stage, getClass(), true, config.getMainWindowFxml(), false, Modality.NONE,
                windowOptions, config.getMainWindowTitle(), null, null);
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
            Consumer<Exception> handleOtherStartupExceptions = handleOtherStartupExceptions();
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
            Consumer<AppAlreadyRunningException> handleAppAlreadyRunning = handleAppAlreadyRunning();
            if (handleAppAlreadyRunning != null) {
                handleAppAlreadyRunning.accept(_ex);
            }
            return;
        } catch (Exception _ex) {
            Consumer<Exception> handleOtherStartupExceptions = handleOtherStartupExceptions();
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
                    Consumer<Exception> handleOtherStartupExceptions = handleOtherStartupExceptions();
                    if (handleOtherStartupExceptions != null) {
                        handleOtherStartupExceptions.accept(_ex);
                    }
                }
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
                logger.error("Could not load splash screen image {} from classpath", config.getSplashImage());
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

        splashLayout.getChildren().add(progressBar);

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

    }
}
