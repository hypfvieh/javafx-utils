package com.github.hypfvieh.javafx.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class TranslatorTest {

    @Test
    void testTranslateNoArgs() {
        Translator translator = new Translator("testbundle", Locale.GERMAN);
        String msg = translator.t("test_msg", "Default Message");
        assertEquals("Standard Nachricht", msg);
    }

    @Test
    void testTranslateWithArgs() {
        Translator translator = new Translator("testbundle", Locale.GERMAN);
        String msg = translator.t("test_msg_args", "Default Message: %s", "Hello");
        assertEquals("Standard Nachricht: Hello", msg);
    }

    @Test
    void testTranslateDefaultInvalidKey() {
        Translator translator = new Translator("testbundle", Locale.GERMAN);
        String msg = translator.t("blubb__", "Blubb");
        assertEquals("Blubb", msg);
    }

    @Test
    void testTranslateDefaultNoBundle() {
        Translator translator = new Translator("noBundle");
        String msg = translator.t("no_bundle_here", "We have no bundle");
        assertEquals("We have no bundle", msg);
    }

}
