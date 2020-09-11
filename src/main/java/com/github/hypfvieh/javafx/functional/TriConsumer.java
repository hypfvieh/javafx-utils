package com.github.hypfvieh.javafx.functional;

/**
 * Consumer which accepts 3 input arguments.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         * @param v the third input argument
         */
        void accept(T t, U u, V v);
}
