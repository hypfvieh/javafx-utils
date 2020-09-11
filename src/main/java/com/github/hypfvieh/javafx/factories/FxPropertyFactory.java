package com.github.hypfvieh.javafx.factories;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanBooleanPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanDoublePropertyBuilder;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanLongPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;

/**
 * Factory to create JavaFx Property objects.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public final class FxPropertyFactory {

    private static JavaBeanStringPropertyBuilder  stringPropBuilder = JavaBeanStringPropertyBuilder.create();
    private static JavaBeanIntegerPropertyBuilder intPropBuilder    = JavaBeanIntegerPropertyBuilder.create();
    private static JavaBeanLongPropertyBuilder    longPropBuilder   = JavaBeanLongPropertyBuilder.create();
    private static JavaBeanDoublePropertyBuilder  doublePropBuilder = JavaBeanDoublePropertyBuilder.create();
    private static JavaBeanBooleanPropertyBuilder boolPropBuilder   = JavaBeanBooleanPropertyBuilder.create();

    private FxPropertyFactory() {
    }

    /**
     * Create a String binding.
     * @param _bean object to bind
     * @param _fieldName fieldname to bind
     * @return {@link StringProperty}
     */
    public static StringProperty createStringProperty(Object _bean, String _fieldName) {
        try {
            return stringPropBuilder.bean(_bean).name(_fieldName).build();
        } catch (NoSuchMethodException _ex) {
            throw new RuntimeException(_ex);
        }
    }

    /**
     * Create a Integer binding.
     * @param _bean object to bind
     * @param _fieldName fieldname to bind
     * @return {@link IntegerProperty}
     */
    public static IntegerProperty createIntegerProperty(Object _bean, String _fieldName) {
        try {
            return intPropBuilder.bean(_bean).name(_fieldName).build();
        } catch (NoSuchMethodException _ex) {
            throw new RuntimeException(_ex);
        }
    }

    /**
     * Create a Long binding.
     * @param _bean object to bind
     * @param _fieldName fieldname to bind
     * @return {@link LongProperty}
     */
    public static LongProperty createLongProperty(Object _bean, String _fieldName) {
        try {
            return longPropBuilder.bean(_bean).name(_fieldName).build();
        } catch (NoSuchMethodException _ex) {
            throw new RuntimeException(_ex);
        }
    }

    /**
     * Create a Double binding.
     * @param _bean object to bind
     * @param _fieldName fieldname to bind
     * @return {@link DoubleProperty}
     */
    public static DoubleProperty createDoubleProperty(Object _bean, String _fieldName) {
        try {
            return doublePropBuilder.bean(_bean).name(_fieldName).build();
        } catch (NoSuchMethodException _ex) {
            throw new RuntimeException(_ex);
        }
    }

    /**
     * Create a Boolean binding.
     * @param _bean object to bind
     * @param _fieldName fieldname to bind
     * @return {@link BooleanProperty}
     */
    public static BooleanProperty createBooleanProperty(Object _bean, String _fieldName) {
        try {
            return boolPropBuilder.bean(_bean).name(_fieldName).build();
        } catch (NoSuchMethodException _ex) {
            throw new RuntimeException(_ex);
        }
    }

    /**
     * Create a object binding.
     * @param _bean object to bind
     * @param _fieldName fieldname to bind
     * @return {@link ObjectProperty}
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectProperty<T> createObjectProperty(Object _bean, String _fieldName) {
        JavaBeanObjectPropertyBuilder<T> objPropBuilder = JavaBeanObjectPropertyBuilder.create();

        try {
            return objPropBuilder.bean(_bean).name(_fieldName).build();
        } catch (NoSuchMethodException _ex) {
            throw new RuntimeException(_ex);
        }
    }

}
