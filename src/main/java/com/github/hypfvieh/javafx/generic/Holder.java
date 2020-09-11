package com.github.hypfvieh.javafx.generic;

/**
 * Holds a value of any type. The value may be null.
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public final class Holder<T> {

    private T value;

    public Holder() {
    }

    public Holder(T _value) {
        set(_value);
    }

    public T get() {
        return value;
    }

    public void set(T _value) {
        value = _value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value + "]";
    }

}
