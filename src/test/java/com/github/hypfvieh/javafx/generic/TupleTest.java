package com.github.hypfvieh.javafx.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TupleTest {

    @Test
    void testTuple() {
        Tuple<String, Integer> tuple = new Tuple<>("String", 1);
        assertEquals("String", tuple.getKey());
        assertEquals(1, tuple.getValue());
    }

}
