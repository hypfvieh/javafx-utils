package com.github.hypfvieh.javafx.windowsaver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JacksonWithReflectionStorageTest {

    @Test
    void testWriteAndReadByReflection() throws IOException {
        JacksonWithReflectionStorage store = new JacksonWithReflectionStorage();
        File tmp = File.createTempFile("jackson_reflection_test", ".json");

        WindowPosInfo window = new WindowPosInfo();
        window.setHeight(1000);
        window.setWidth(543);

        Map<String, WindowPosInfo> data = Map.of("Test", window);

        store.write(tmp, data);

        Map<String, WindowPosInfo> read = store.read(tmp);

        assertTrue(read.containsKey("Test"));
        assertEquals(read.get("Test"), window);
    }

}
