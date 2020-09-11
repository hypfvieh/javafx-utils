package com.github.hypfvieh.javafx.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StringHelperTest {

    @Test
    void testIsBlank() {
        assertTrue(StringHelper.isBlank(""));
        assertTrue(StringHelper.isBlank("  "));

        assertFalse(StringHelper.isBlank(" hi"));
        assertFalse(StringHelper.isBlank("hi "));
        assertFalse(StringHelper.isBlank("hi"));
    }

    @Test
    void testCountSubStr() {
        String str = "This bla is a test bla, for counting bla substrings in a longer bla string";
        assertEquals(4, StringHelper.countSubString(str, "bla"));
    }

}
