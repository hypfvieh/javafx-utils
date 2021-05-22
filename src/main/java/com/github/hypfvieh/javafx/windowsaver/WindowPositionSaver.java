package com.github.hypfvieh.javafx.windowsaver;

import java.awt.Dimension;
import java.awt.Toolkit;
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
import javafx.scene.Parent;
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
        String userHome = System.getProperty("user.home");

        File targetDir = new File(userHome, ".sgbm-scorecard");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File prefFile = new File(targetDir, "windowPrefs.json");
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
                posInfo.setMaxHeight(_stage.getMaxHeight());
                posInfo.setMaxWidth(_stage.getMaxWidth());
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


        // window is bigger than the screen, do nothing
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (screenSize != null
        		&& posInfo.getWidth() > screenSize.getWidth()
        		|| posInfo.getX() > screenSize.getWidth() -10
        		|| posInfo.getHeight() > screenSize.getHeight()
        		|| posInfo.getY() > screenSize.getHeight() -10) {
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

            Double max = dwp.getParentMaxGetter().apply(_root);
            if (!max.equals(oldDefaultMax)) {
                LOGGER.log(Level.DEBUG, "{} default window max{} changed from {} to {}", _windowClass.getClass().getSimpleName(), dwp.getLogStr(), oldDefaultMax == -1 ? "NOT SET" : oldDefaultMax, max);
                dwp.getMaxSetter().accept(_posInfo, max);
                hasChanged = true;
            }

            Double min = dwp.getParentMinGetter().apply(_root);
            if (!min.equals(oldDefaultMin)) {
                LOGGER.log(Level.DEBUG, "{} default window min{} changed from {} to {}", _windowClass.getClass().getSimpleName(), dwp.getLogStr(), oldDefaultMin == -1 ? "NOT SET" : oldDefaultMin, min);
                dwp.getMinSetter().accept(_posInfo, min);
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
                wi -> wi.getMinHeight(),
                wi -> wi.getMaxHeight(),
                s -> s.minHeight(-1),
                s -> s.maxHeight(-1),
                (wi, d) -> wi.setMaxHeight(d),
                (wi, d) -> wi.setMinHeight(d)
        ),
        WIDTH("Width",
                wi -> wi.getMinWidth(),
                wi -> wi.getMaxWidth(),
                s -> s.minWidth(-1),
                s -> s.maxWidth(-1),
                (wi, d) -> wi.setMaxWidth(d),
                (wi, d) -> wi.setMinWidth(d)
        );

        private final String logStr;

        private final Function<WindowPosInfo, Double> maxGetter;
        private final Function<WindowPosInfo, Double> minGetter;
        private final BiConsumer<WindowPosInfo, Double> maxSetter;
        private final BiConsumer<WindowPosInfo, Double> minSetter;
        private final Function<Parent, Double> parentMinGetter;
        private final Function<Parent, Double> parentMaxGetter;

        DefaultWindowPrefs(String _logStr,
                Function<WindowPosInfo, Double> _minGetter, Function<WindowPosInfo, Double> _maxGetter,
                Function<Parent, Double> _parentMinGetter, Function<Parent, Double> _parentMaxGetter,
                BiConsumer<WindowPosInfo, Double> _maxSetter, BiConsumer<WindowPosInfo, Double> _minSetter) {
            minGetter = _minGetter;
            maxGetter = _maxGetter;
            parentMinGetter = _parentMinGetter;
            parentMaxGetter = _parentMaxGetter;
            logStr = _logStr;
            maxSetter = _maxSetter;
            minSetter = _minSetter;
        }

        public String getLogStr() {
            return logStr;
        }

        public Function<Parent, Double> getParentMinGetter() {
            return parentMinGetter;
        }

        public Function<Parent, Double> getParentMaxGetter() {
            return parentMaxGetter;
        }

        public Function<WindowPosInfo, Double> getMaxGetter() {
            return maxGetter;
        }

        public Function<WindowPosInfo, Double> getMinGetter() {
            return minGetter;
        }

        public BiConsumer<WindowPosInfo, Double> getMaxSetter() {
            return maxSetter;
        }

        public BiConsumer<WindowPosInfo, Double> getMinSetter() {
            return minSetter;
        }

    }
}
