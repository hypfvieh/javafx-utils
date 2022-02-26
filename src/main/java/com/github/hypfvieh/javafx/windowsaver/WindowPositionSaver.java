package com.github.hypfvieh.javafx.windowsaver;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import com.github.hypfvieh.javafx.windows.interfaces.ISaveWindowPreferences;
import com.github.hypfvieh.javafx.windows.interfaces.ISaveWindowPreferences.WindowData;

/**
 * Helper to save/restore JavaFX window position and status.
 * <p>
 * The window properties will be saved in JSON format using jackson if possible.
 * If jackson is not available, nothing will be saved.
 * <p>
 * If you don't want to use jackson, provide a custom implementation of {@link IWindowDataStorage}.
 * <p>
 * The {@link WindowPositionSaver} is disabled by default, to use it call
 * {@link WindowPositionSaver#setEnabled(boolean)} and set it to true as soon as possible.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class WindowPositionSaver {


    private static final Logger LOGGER = System.getLogger(WindowPositionSaver.class.getName());

    private static IWindowDataStorage storageProvider = new JacksonWithReflectionStorage();

    private static File storageFolder = new File(System.getProperty("user.home"), ".javafx");
    private static String storageFile = "windowPrefs";

    private static boolean enabled = false;

    /**
     * Set another {@link IWindowDataStorage} to store/load data.
     * This has to be done before calling {@link #getStoredData()}.
     *
     * @param _provider provider to set, null is ignored
     */
    public static void setDataStorageProvider(IWindowDataStorage _provider ) {
        if (_provider != null) {
            storageProvider = _provider;
        }
    }

    /**
     * Enable or disable the {@link WindowPositionSaver}.
     *
     * @param _enabled true to enable, false to disable
     */
    public static void setEnabled(boolean _enabled) {
        enabled = _enabled;
    }

    /**
     * Get current status.
     *
     * @return boolean
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Folder where to store window preferences.
     * Defaults to "user.home/.javafx".
     *
     * @param _folder target folder, null is ignored
     */
    public static void setStorageFolder(File _folder) {
        if (_folder == null) {
            return;
        }
        storageFolder = _folder;
    }

    /**
     * Name of the file to store window preferences in.
     * File extension will be appended by the chosen storage backend.
     *
     * Defaults to "windowPrefs".
     *
     * @param _folder target folder, null is ignored
     */
    public static void setStorageFile(String _storeFile) {
        if (_storeFile == null || _storeFile.isBlank()) {
            return;
        }

        storageFile = _storeFile;
    }

    /**
     * Read the data stored in the data file.
     * If file is empty or does not exists, an empty map is returned.
     * @return Map, maybe empty - never null
     */
    public static Map<String, WindowPosInfo> getStoredData() {
        if (!enabled) {
            return null;
        }

    	File prefFile = getDataStoreFile();
    	if (!prefFile.exists()) {
    		return new HashMap<>();
    	}

    	try {

			Map<String, WindowPosInfo> readValue = storageProvider.read(prefFile);
			return readValue;
		} catch (IOException _ex) {
			LOGGER.log(Level.ERROR, "Could not read stored window position data from {}.", prefFile, _ex);
			return new HashMap<>();
		}

    }

    /**
     * Write the given data map back to the data file overwriting existing content.
     * @param _data date to save
     */
    private static void updateStoredData(Map<String, WindowPosInfo> _data) {
        if (!enabled) {
            return;
        }

        if (_data == null) {
            return;
        }
        File prefFile = getDataStoreFile();

        try {
            storageProvider.write(prefFile, _data);
        } catch (IOException _ex) {
            LOGGER.log(Level.ERROR, "Could not save window position to file {}." , prefFile, _ex);
        }
    }

    /**
     * Returns the file object where the saved window data could be found.
     * @return File
     */
    private static File getDataStoreFile() {
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        String fileName = storageFile + "." + storageProvider.getFileExtension();

        File prefFile = new File(storageFolder, fileName);
        return prefFile;
    }

    /**
     * Save given stage status/size with the given name.
     * @param _controller class to use
     * @param _stage stage to save
     */
    public static void saveWindowPosition(Initializable _controller, Stage _stage) {
        if (!enabled) {
            return;
        }

        Objects.requireNonNull(_controller, "Window class cannot be null");
        Objects.requireNonNull(_stage, "Stage cannot be null");

        WindowData windowPrefsSaveLoad = _controller instanceof ISaveWindowPreferences ? ((ISaveWindowPreferences) _controller).saveWindowPreferences() : WindowData.NONE;
        windowPrefsSaveLoad = windowPrefsSaveLoad == null ? WindowData.NONE : windowPrefsSaveLoad;

        Map<String, WindowPosInfo> storedData = getStoredData();

        if (windowPrefsSaveLoad == WindowData.NONE) {
            clearSavedWindowPreferences(_controller.getClass());

        } else {

            WindowPosInfo posInfo = storedData.getOrDefault(_controller.getClass().getName(), new WindowPosInfo());

            if (windowPrefsSaveLoad == WindowData.POSITION || windowPrefsSaveLoad == WindowData.BOTH) {
                posInfo.setX(_stage.getX());
                posInfo.setY(_stage.getY());
            }

            if (windowPrefsSaveLoad == WindowData.SIZE || windowPrefsSaveLoad == WindowData.BOTH) {
                posInfo.setHeight(_stage.getHeight());
                posInfo.setWidth(_stage.getWidth());
                posInfo.setMaximized(_stage.isMaximized());

                if (_stage.getScene() != null && _stage.getScene().getRoot() != null) {
                    posInfo.setMinWidth(_stage.getScene().getRoot().minWidth(-1));
                    posInfo.setMinHeight(_stage.getScene().getRoot().minHeight(-1));
                }
            }
            posInfo.setTitle(_stage.getTitle());
            storedData.put(_controller.getClass().getName(), posInfo);

            LOGGER.log(Level.DEBUG, "Saving window properties: window={}, width={}, heigth={}, x={}, y={}, maximized={}", _controller.getClass().getName(), _stage.getWidth(), _stage.getHeight(), _stage.getX(), _stage.getY(), _stage.isMaximized());
        }

        updateStoredData(storedData);
    }

    /**
     * Clear all saved window preferences for the given controller class.
     *
     * @param _windowClass window controller class
     */
    public static void clearSavedWindowPreferences(Class<? extends Initializable> _windowClass) {
        if (!enabled) {
            return;
        }

        if (_windowClass == null) {
            return;
        }
        clearSavedWindowPreferences(_windowClass.getName());
    }

    /**
     * Remove all saved window preferences found under given class name.
     * @param _windowClassName name of the window class to clear preferences for
     */
    public static void clearSavedWindowPreferences(String _windowClassName) {
        if (!enabled) {
            return;
        }

        if (_windowClassName == null || _windowClassName.isBlank()) {
            return;
        }

        Map<String, WindowPosInfo> storedData = getStoredData();
        if (storedData.containsKey(_windowClassName)) {
            storedData.remove(_windowClassName);
            LOGGER.log(Level.DEBUG, "Removing stored window positions for window: {}", _windowClassName);
            updateStoredData(storedData);
        }
    }
    /**
     * Clear all saved window preferences.
     */
    public static void clearAllSavedWindowPreferences() {
        if (!enabled) {
            return;
        }

        File store = getDataStoreFile();
        if (store == null || !store.exists()) {
            return;
        }

        if (store.delete()) {
            LOGGER.log(Level.INFO, "Removed all stored window positions ({} deleted)", store);
        } else {
            LOGGER.log(Level.ERROR, "Could not remove window position data store file {}", store);
        }
    }

    /**
     * Restores the store default sizes of the given window (if any).
     * 
     * @param _controllerClass controller class
     * @param _stage stage to resize
     */
    public static void restoreDefaultWindowSize(Class<?> _controllerClass, Stage _stage) {
        if (!enabled) {
            return;
        }
        Objects.requireNonNull(_controllerClass, "Controller class cannot be null");
        Objects.requireNonNull(_stage, "Stage cannot be null");

        WindowPosInfo storedData = getStoredData().get(_controllerClass.getName());
        if (storedData == null) {
            return; // no saved defaults
        }

        _stage.setMaxHeight(storedData.getMaxHeight());
        _stage.setMinHeight(storedData.getMinHeight());

        _stage.setMinWidth(storedData.getMinWidth());
        _stage.setMaxWidth(storedData.getMaxWidth());
        
        _stage.sizeToScene();

    }
    
    /**
     * Restores the store default sizes of the given window (if any).
     * 
     * @param _controller controller
     * @param _stage stage to resize
     */
    public static void restoreDefaultWindowSize(Initializable _controller, Stage _stage) {
        Objects.requireNonNull(_controller, "Controller cannot be null");
        restoreDefaultWindowSize(_controller.getClass(), _stage);
    }
    
    /**
     * Restore given stage status/size to values found with the given name.
     * @param _controller controller/window to restore
     * @param _stage stage to restore
     * @param _root parent
     */
    public static void restoreWindowPosition(Initializable _controller, Stage _stage, Parent _root) {
        if (!enabled) {
            return;
        }

        Objects.requireNonNull(_controller, "WindowController cannot be null");
        Objects.requireNonNull(_stage, "Stage cannot be null");

        // force layout before applying window saver position/size because stage is uninitialized before
        _stage.getScene().getRoot().layout();
        _stage.sizeToScene();

        WindowData windowPrefsSaveLoad = _controller instanceof ISaveWindowPreferences ? ((ISaveWindowPreferences) _controller).saveWindowPreferences() : WindowData.NONE;
        windowPrefsSaveLoad = windowPrefsSaveLoad == null ? WindowData.NONE : windowPrefsSaveLoad;

        if (windowPrefsSaveLoad == WindowData.NONE) {
            return;
        }

        Map<String, WindowPosInfo> storedData = getStoredData();

        if (!storedData.containsKey(_controller.getClass().getName())) {
            return; // no saved data
        }

        WindowPosInfo posInfo = storedData.get(_controller.getClass().getName());

        // if default has changed, remove saved settings and do NOT load changed window dimensions/positions
        if (maybeUpdateDefaults(posInfo, _controller, _stage, _root)) {
            LOGGER.log(Level.INFO, "Will not restore window position/dimension due to changed default sizes (removing old invalid)");
            clearSavedWindowPreferences(_controller.getClass());
            return;
        }

        double[] dimensions = computeAllScreenBounds();

        // X position is off screen
        if (posInfo.getX() > dimensions[2] -10 || posInfo.getX() < dimensions[0]) {
            return;
        }

        // Y position is off screen
        if (posInfo.getY() > dimensions[3] || posInfo.getY() < dimensions[1]) {
            return;
        }

        // width is smaller/bigger than screen resolution
        if (posInfo.getWidth() < dimensions[0] || posInfo.getWidth() > dimensions[2]) {
            return;
        }

        // height is smaller/bigger than screen resolution
        if (posInfo.getHeight() < dimensions[1] || posInfo.getHeight() > dimensions[3]) {
            return;
        }

        LOGGER.log(Level.DEBUG, "Restoring window properties: window={}, saveLoadOption={}, {}", _controller.getClass().getName(), windowPrefsSaveLoad, posInfo);

        if (windowPrefsSaveLoad == WindowData.BOTH || windowPrefsSaveLoad == WindowData.SIZE) {
            _stage.setHeight(posInfo.getHeight());
            _stage.setWidth(posInfo.getWidth());
            _stage.setMaximized(posInfo.isMaximized());
        }

        _stage.setX(posInfo.getX());
        _stage.setY(posInfo.getY());
    }

    /**
     * Calculates the maximum screen size for multi monitor screens.
     *
     * @return double array, 0: minX, 1: minY, 2: maxX, 3: maxY
     */
    private static double[] computeAllScreenBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Screen screen : Screen.getScreens()) {
            Rectangle2D screenBounds = screen.getBounds();
            if (screenBounds.getMinX() < minX) {
                minX = screenBounds.getMinX();
            }
            if (screenBounds.getMinY() < minY) {
                minY = screenBounds.getMinY() ;
            }
            if (screenBounds.getMaxX() > maxX) {
                maxX = screenBounds.getMaxX();
            }
            if (screenBounds.getMaxY() > maxY) {
                maxY = screenBounds.getMaxY() ;
            }
        }
        return new double[] {minX, minY, maxX, maxY};
    }

    /**
     * Updates the saved default values if values have been changed.
     *
     * @param _posInfo window position info
     * @param _windowClass window controller class
     * @param _stage stage to check
     * @return true if changes were applied, false otherwise
     */
    private static boolean maybeUpdateDefaults(WindowPosInfo _posInfo, Initializable _windowClass, Stage _stage, Parent _root) {
        boolean hasChanged = false;

        for (DefaultWindowPrefs dwp : DefaultWindowPrefs.values()) {
            double oldDefaultMin = dwp.getMinGetter().apply(_posInfo);
            double oldDefaultMax = dwp.getMaxGetter().apply(_posInfo);

            Double min = dwp.getStageMinGetter().apply(_stage);
            // minimum value was changed and needs to be bigger than the old value
            if (min > oldDefaultMin) {
                LOGGER.log(Level.DEBUG, "{} default window min{} changed from {} to {}", _windowClass.getClass().getSimpleName(), dwp.getLogStr(), oldDefaultMin == -1 ? "NOT SET" : oldDefaultMin, min);
                dwp.getMinSetter().accept(_posInfo, min);
                hasChanged = true;
            }

            Double max = dwp.getStageMaxGetter().apply(_stage);
            // max value was changed and has to be smaller than the old value
            if (max < oldDefaultMax) {
                LOGGER.log(Level.DEBUG, "{} default window min{} changed from {} to {}", _windowClass.getClass().getSimpleName(), dwp.getLogStr(), oldDefaultMax == -1 ? "NOT SET" : oldDefaultMax, max);
                dwp.getMaxSetter().accept(_posInfo, min);
                hasChanged = true;
            }

        }

        return hasChanged;
    }

    /**
     * Helper enum to read/write default window properties like min/max/pref values.
     *
     * @author hypfvieh
     * @since v11.0.0 - 2020-09-11
     */
    enum DefaultWindowPrefs {
        HEIGHT("Height",
                wi -> wi.getMinHeight(), // min window height getter
                wi -> wi.getMaxHeight(), // max window height getter
                (wi, d) -> wi.setMinHeight(d), // min window height setter
                (wi, d) -> wi.setMaxHeight(d), // max window height setter
                st -> st.getMinHeight(), // get min height from stage
                st -> st.getMaxHeight() // get max height from stage
        ),
        WIDTH("Width",
                wi -> wi.getMinWidth(), // min window width getter
                wi -> wi.getMaxWidth(), // max window width getter
                (wi, d) -> wi.setMinWidth(d), // min window width Setter
                (wi, d) -> wi.setMaxWidth(d), // max window width Setter
                st -> st.getMinWidth(), // get min width from stage
                st -> st.getMaxWidth() // get max width from stage
        );

        private final String logStr;

        /** stored minimum width/height getter */
        private final Function<WindowPosInfo, Double> minGetter;
        /** stored minimum width/height setter */
        private final BiConsumer<WindowPosInfo, Double> minSetter;
        /** stored maximum width/height setter */
        private final BiConsumer<WindowPosInfo, Double> maxSetter;
        /** stored maximum width/height getter */
        private final Function<WindowPosInfo, Double> maxGetter;

        /** stage minimum width/height getter */
        private final Function<Stage, Double> stageMinGetter;
        /** stage maximum width/height getter */
        private final Function<Stage, Double> stageMaxGetter;


        DefaultWindowPrefs(String _logStr,
                Function<WindowPosInfo, Double> _minGetter,
                Function<WindowPosInfo, Double> _maxGetter,
                BiConsumer<WindowPosInfo, Double> _minSetter,
                BiConsumer<WindowPosInfo, Double> _maxSetter,
                Function<Stage, Double> _stageMinGetter,
                Function<Stage, Double> _stageMaxGetter
                ) {

            minGetter = _minGetter;
            logStr = _logStr;
            maxGetter = _maxGetter;
            minSetter = _minSetter;
            maxSetter = _maxSetter;
            stageMinGetter = _stageMinGetter;
            stageMaxGetter = _stageMaxGetter;
        }

        public String getLogStr() {
            return logStr;
        }

        public Function<WindowPosInfo, Double> getMinGetter() {
            return minGetter;
        }

        public BiConsumer<WindowPosInfo, Double> getMinSetter() {
            return minSetter;
        }

        public Function<Stage, Double> getStageMinGetter() {
            return stageMinGetter;
        }

        public Function<Stage, Double> getStageMaxGetter() {
            return stageMaxGetter;
        }

        public BiConsumer<WindowPosInfo, Double> getMaxSetter() {
            return maxSetter;
        }

        public Function<WindowPosInfo, Double> getMaxGetter() {
            return maxGetter;
        }

    }
}
