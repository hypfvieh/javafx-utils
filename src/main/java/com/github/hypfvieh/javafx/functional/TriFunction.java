package com.github.hypfvieh.javafx.functional;

/**
 * FunctionalInterface which support 3 input and 1 return value.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param a the first function argument
     * @param b the second function argument
     * @param c the third function argument
     * @return the function result
     */
    R apply(A a, B b, C c);


}
