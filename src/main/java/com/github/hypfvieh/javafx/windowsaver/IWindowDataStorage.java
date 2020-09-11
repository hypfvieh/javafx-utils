package com.github.hypfvieh.javafx.windowsaver;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Interface implemented by all storage provider classes for {@link WindowPositionSaver} utility.
 * Implement this interface if you want to use a custom provider.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public interface IWindowDataStorage {
    Map<String, WindowPosInfo> read(File _file) throws IOException;
    void write(File _file, Map<String, WindowPosInfo> _data) throws IOException;
}
