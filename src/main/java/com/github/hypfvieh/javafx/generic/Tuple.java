package com.github.hypfvieh.javafx.generic;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tuple<?,?> other = (Tuple<?,?>) obj;
        return Objects.equals(key, other.key) && Objects.equals(value, other.value);
    }


}
