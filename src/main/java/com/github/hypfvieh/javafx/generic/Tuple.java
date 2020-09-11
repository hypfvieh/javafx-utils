package com.github.hypfvieh.javafx.generic;

import java.io.Serializable;

/**
 * Class which holds two values.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class Tuple<K, V> implements Serializable {
    private static final long serialVersionUID = 8044352769627978560L;

    private final K key;
    private final V value;

    public Tuple(K _key, V _value) {
        key = _key;
        value = _value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

}
