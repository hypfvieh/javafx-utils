package com.github.hypfvieh.javafx.windowsaver;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@link IWindowDataStorage} implementation which uses jackson by reflection to store window information.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class JacksonWithReflectionStorage implements IWindowDataStorage {

    private final Logger logger = System.getLogger(getClass().getName());

    @Override
    public Map<String, WindowPosInfo> read(File _file) throws IOException {
        return readWithJackson(_file);
    }

    @Override
    public void write(File _file, Map<String, WindowPosInfo> _data) throws IOException {
        writeWithJackson(_file, _data);
    }

    @Override
    public String getFileExtension() {
        return "json";
    }

    /**
     * Uses reflection to read values from file using the jackson library.
     *
     * @param _file file to read
     *
     * @return result set or null when no data found or read failed
     * @throws IOException when jackson's readvalue throws
     */
    @SuppressWarnings("unchecked")
    private Map<String, WindowPosInfo> readWithJackson(File _file) throws IOException {
        try {
            Class<?> objectMapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            Object newInstance = objectMapperClass.getDeclaredConstructor().newInstance();

            setupJacksonObjectMapper(objectMapperClass, newInstance);

            Class<?> typeFactoryClass = Class.forName("com.fasterxml.jackson.databind.type.TypeFactory");
            Object typeFactory = typeFactoryClass.getDeclaredMethod("defaultInstance").invoke(null);
            Method constructMapMethod = typeFactoryClass.getDeclaredMethod("constructMapType", Class.class, Class.class, Class.class);
            constructMapMethod.setAccessible(true);

            Object reference = constructMapMethod.invoke(typeFactory, HashMap.class, String.class, WindowPosInfo.class);

            Class<?> javaTypeClass = Class.forName("com.fasterxml.jackson.databind.JavaType");

            Method readValueMethod = objectMapperClass.getDeclaredMethod("readValue", File.class, javaTypeClass);
            readValueMethod.setAccessible(true);
            Object result = readValueMethod.invoke(newInstance, _file, reference);

            if (result == null) {
                return null;

            } else if (result instanceof Map) {
                return Map.class.cast(result);
            }

            throw new IllegalArgumentException("Retrieved result object is of unsupported type " + result.getClass().getName());
        } catch (InvocationTargetException _ex) {
            if (_ex.getCause() instanceof IOException) {
                throw (IOException) _ex.getCause();
            }
            logger.log(Level.ERROR, "Error invoking jackson 'readValue' method", _ex);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException _ex) {
            logger.log(Level.ERROR, "Error using reflection to call jackson's 'writeValue' method", _ex);
        }

        return null;
    }

    /**
     * Uses reflection to write values to file using the jackson library.
     *
     * @param _file file to write
     * @param _data data to write
     * @throws IOException when jackson's writeValue throws
     */
    private void writeWithJackson(File _file, Map<String, WindowPosInfo> _data) throws IOException {
        try {
            Class<?> objectMapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            Object newInstance = objectMapperClass.getDeclaredConstructor().newInstance();

            setupJacksonObjectMapper(objectMapperClass, newInstance);

            Class<?> objectWriterClass = Class.forName("com.fasterxml.jackson.databind.ObjectWriter");

            Object objectWriter = objectMapperClass.getDeclaredMethod("writerWithDefaultPrettyPrinter")
                .invoke(newInstance);

            Method declaredMethod = objectWriterClass.getDeclaredMethod("writeValue", File.class, Object.class);
            declaredMethod.invoke(objectWriter, _file, _data);
        } catch (InvocationTargetException _ex) {
            if (_ex.getCause() instanceof IOException) {
                throw (IOException) _ex.getCause();
            }
            logger.log(Level.ERROR, "Error invoking jackson 'writeValue' method", _ex);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException _ex) {
            logger.log(Level.ERROR, "Error using reflection to call jackson's 'writeValue' method", _ex);
        }
    }

    /**
     * Setup the properties of the jackson object mapper.
     *
     * @param _objectMapperClass object mapper class
     * @param _objectMapperInstance object mapper instance
     *
     * @throws ClassNotFoundException any class could not be found
     * @throws IllegalAccessException class is not accessable
     * @throws InvocationTargetException operation failed
     * @throws NoSuchMethodException method not found
     */
    private void setupJacksonObjectMapper(Class<?> _objectMapperClass, Object _objectMapperInstance)
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<?> deSerFeatureClass = Class.forName("com.fasterxml.jackson.databind.DeserializationFeature");

        Optional<?> findFirst = Arrays.stream(deSerFeatureClass.getEnumConstants())
            .map(e -> (Enum<?>) e)
            .filter(e -> !"FAIL_ON_UNKNOWN_PROPERTIES".equals(e.name()))
            .findFirst();

        if (findFirst.isEmpty()) {
            throw new IllegalArgumentException("Could not find ignore 'FAIL_ON_UNKNOWN_PROPERTIES' option for object mapper");
        }

        // configure object mapper to ignore missing/unknown properties in objects
        Method configureMethod = _objectMapperClass.getDeclaredMethod("configure", deSerFeatureClass, boolean.class);
        configureMethod.setAccessible(true);

        configureMethod
            .invoke(_objectMapperInstance, findFirst.get(), false);
    }

}
