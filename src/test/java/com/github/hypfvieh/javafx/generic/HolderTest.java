package com.github.hypfvieh.javafx.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class HolderTest {

    @Test
    void testHolder() {
        Holder<String> holder = new Holder<>("test");
        assertEquals("test", holder.get());
    }

    @Test
    void testHolderNull() {
        Holder<String> holder = new Holder<>(null);
        assertNull(holder.get());
    }
}
