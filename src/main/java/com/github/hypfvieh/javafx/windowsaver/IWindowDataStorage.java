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

    /**
     * Read values from given file and return the results as Map.
     *
     * @param _file file to read
     *
     * @return Map, maybe null
     *
     * @throws IOException when file reading fails
     */
    Map<String, WindowPosInfo> read(File _file) throws IOException;

    /**
     * Write the content of the given Map to the given file.
     *
     * @param _file file to write to
     * @param _data data to write
     *
     * @throws IOException if writing fails
     */
    void write(File _file, Map<String, WindowPosInfo> _data) throws IOException;

    /**
     * File extension (without leading dot) used as extension for storage file.
     * @return String, never null
     */
    String getFileExtension();
}
