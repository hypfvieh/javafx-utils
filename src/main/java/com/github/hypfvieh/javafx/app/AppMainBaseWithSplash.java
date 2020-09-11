package com.github.hypfvieh.javafx.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
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
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.javafx.fx.FxDialogUtils;
import com.github.hypfvieh.javafx.fx.FxWindowUtils;
import com.github.hypfvieh.javafx.other.AppLock;
import com.github.hypfvieh.javafx.other.AppLock.AppAlreadyRunningException;
import com.github.hypfvieh.javafx.ui.BaseWindowController;
import com.github.hypfvieh.javafx.utils.Translator;
import com.github.hypfvieh.javafx.windows.interfaces.ICustomInitialize;
import com.github.hypfvieh.javafx.windowsaver.WindowPositionSaver;

/**
 * A base application class to show splash screen and handle startup/teardown actions as
 * well as exception handling on startup.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public abstract class AppMainBaseWithSplash extends Application {

    public final Logger logger = LoggerFactory.getLogger(AppMainBaseWithSplash.class);
    private final Translator translator = new Translator("AppMainBaseWithSplash");

    private final String mainWindowTitle;
    private final String mainWindowFxml;
    private final String splashImage;
    private final String appIcon;

    private Paint progressLabelFgColor = Color.ORANGERED;
    private String progressBarStyle = "-fx-accent: orange;";

    public AppMainBaseWithSplash(String _mainWindowTitle, String _mainWindowFxml, String _appIcon, String _splashImage) {
        mainWindowTitle = _mainWindowTitle;
        mainWindowFxml = _mainWindowFxml;
        appIcon = _appIcon;
        splashImage = _splashImage;
    }

    /**
     * Task which is called after the splash screen is getting visible.
     * <p>
     * Use this to do some long running tasks before the main application is visible.
     * You can use {@link Task}.updateMessage and {@link Task}.updateProgress to use a label and progress bar.
     *
     * @return task or null to do nothing
     */
    public abstract Task<Void> startupTaskAction();

    /**
     * Task which will be executed when application is closed.
     *
     * @return runnable or null to do nothing
     */
    public abstract Runnable shutdownTaskAction();

    /**
     * Called when application is started with {@link AppLock} support and there was already an instance running.
     * <p>
     * Default will show a error message to the user and exit the application.
     * <br>
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
                        translator.t("app_running_msg", "The application is already running.%nPlease close the running instance and try again.")
                );
            });
        };
    }

    /**
     * Called when application throws any exception (except {@link AppAlreadyRunningException}) at startup.
     * <p>
     * Default will show a error message to the user and exit the application with exit code = 1.
     * <br>
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
                        translator.t("app_could_not_be_started_msg", "The application could not be started because of an unknown failure.%nPlease check application log if available."));
                System.exit(1);
            });
        };
    }

    /*
     * Internally used to either get the task of the user
     * or create an empty task.
     */
    private Task<Void> getStartupTaskInternal() {
        Task<Void> startTask = startupTaskAction();
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
        Task<Void> task = getStartupTaskInternal();

        if (task.getOnFailed() == null) {
            task.setOnFailed(evt -> {
                logger.error("Error while running application:", task.getException());

                String msg = translator.t("error_starting_app", "Error while starting application. Please contact the support.");

                if (task.getException() instanceof RuntimeException) {
                    msg = translator.t("error_on_startup", "Error while starting application: %s", task.getException().getLocalizedMessage());
                }

                // initialization of java fx failed somehow, use swing error dialog
                JOptionPane.showMessageDialog(null,
                        msg,
                        translator.t("error_dlg_title", "Error"),
                        JOptionPane.ERROR_MESSAGE);

                System.exit(1);
            });
        }

        showSplash(_stage, task);
        new Thread(task).start();

    }

    /**
     * Display the main stage after splash is done.
     *
     * @param _stage primary stage
     * @throws IOException on error
     */
    void showMainStage(Stage _stage) throws IOException {

        Platform.setImplicitExit(false);

        URL url = getClass().getResource(mainWindowFxml);

        FXMLLoader fxmlloader = new FXMLLoader();
        fxmlloader.setLocation(url);
        fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());

        fxmlloader.load(url.openStream());

        final BaseWindowController mainController = (BaseWindowController) fxmlloader.getController();
        mainController.setControllerStage(_stage);
        Parent root = fxmlloader.getRoot();

        Scene scene = new Scene(root);

        scene.getStylesheets().addAll(FxWindowUtils.getCssThemes());

        _stage.setTitle(mainWindowTitle);
        _stage.setUserData(mainController);

        if (appIcon != null) {
            try (InputStream stream = AppMainBaseWithSplash.class.getClassLoader().getResourceAsStream(appIcon)) {
                if (stream != null) {
                    _stage.getIcons().add(new Image(stream));
                }
            }
        }

        _stage.initModality(Modality.NONE);

        // do something when window is closed (application quits)
        _stage.setOnCloseRequest((WindowEvent we) -> {
            try {
                Runnable shutdownTaskAction = shutdownTaskAction();
                if (shutdownTaskAction != null) {
                    shutdownTaskAction.run();
                }
            } catch (Exception _ex) {
                LoggerFactory.getLogger(getClass()).error("Error while closing controller", _ex);
            }
        });

        _stage.setScene(scene);

        _stage.setOnShown(ev -> {
            if (mainController instanceof ICustomInitialize) {
                ((ICustomInitialize) mainController).customInitialize();
            }
        });

        _stage.show();
        WindowPositionSaver.restoreWindowPosition(mainController, _stage, root);

    }

    /**
     * Call this in your application to actual run the application.
     * <p>
     * This will not use any locking, so you can run multiple instances of your app at
     * the same time.
     *
     * @param _args args from static main
     */
    public void runApp(String[] _args) {
        try {

            launch(_args);

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

            launch(_args);

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
                } catch (IOException _ex) {
                }
           }
        });

        Pane splashLayout = new VBox();
        splashLayout.setBackground(Background.EMPTY);

        ProgressBar progressBar = new ProgressBar();

        if (splashImage != null) {
            try (InputStream stream = AppMainBaseWithSplash.class.getClassLoader().getResourceAsStream(splashImage)) {
                if (stream != null) {
                    Image image = new Image(splashImage);
                    ImageView splash = new ImageView(image);
                    progressBar.setPrefWidth(image.getWidth() - 20);
                    splashLayout.getChildren().add(splash);

                    Rectangle2D bounds = Screen.getPrimary().getBounds();

                    _initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - image.getWidth() / 2);
                    _initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - image.getHeight() / 2);
                }
            } catch (IOException _ex) {
                logger.error("Could not load splash screen image {} from classpath", splashImage);
            }
        }

        Label progressLabel = new Label();
        VBox.setMargin(progressLabel, new Insets(0, 0, 0, 5));
        progressLabel.setTextFill(progressLabelFgColor);
        progressLabel.setBackground(Background.EMPTY);
        splashLayout.getChildren().add(progressLabel);

        progressBar.setStyle(progressBarStyle);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        progressBar.progressProperty().bind(_task.progressProperty());
        progressLabel.textProperty().bind(_task.messageProperty());

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);


        splashLayout.getChildren().add(progressBar);

        _initStage.setScene(splashScene);
        _initStage.initStyle(StageStyle.TRANSPARENT);
        _initStage.setAlwaysOnTop(true);
        _initStage.setTitle(mainWindowTitle + " - Start");
        _initStage.show();
    }
}
