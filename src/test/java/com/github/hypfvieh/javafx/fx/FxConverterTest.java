package com.github.hypfvieh.javafx.fx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FxConverterTest {

    @Test
    void testPhoneNumber() {
        assertTrue(FxConverter.PHONE_PATTERN.matcher("01234-23455").matches());
        assertTrue(FxConverter.PHONE_PATTERN.matcher("+47234 23455 2345").matches());
        assertTrue(FxConverter.PHONE_PATTERN.matcher("01234/23455").matches());
        assertTrue(FxConverter.PHONE_PATTERN.matcher("01234 2345335").matches());
    }

    @Test
    void testIntNotNegative() {
        assertTrue(FxConverter.INT_NON_NEGATIVE.matcher("123").matches());
        assertTrue(FxConverter.INT_NON_NEGATIVE.matcher("1").matches());
        assertFalse(FxConverter.INT_NON_NEGATIVE.matcher("-1").matches());
    }

    @Test
    void testIntOptNegative() {
        assertTrue(FxConverter.INT_OPT_NEGATIVE.matcher("123").matches());
        assertTrue(FxConverter.INT_OPT_NEGATIVE.matcher("1").matches());
        assertTrue(FxConverter.INT_OPT_NEGATIVE.matcher("-1").matches());
    }

}
