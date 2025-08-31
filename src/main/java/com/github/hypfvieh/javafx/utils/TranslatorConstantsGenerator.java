package com.github.hypfvieh.javafx.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Helper to generate constant classes from a properties file to use with {@link Translator}.
 */
public final class TranslatorConstantsGenerator {

    private TranslatorConstantsGenerator() {

    }

    public static void main(String[] _args) {
        if (_args.length < 3) {
            throw new IllegalArgumentException("Required: [packageName] [input-properties-file] [output-filename]");
        }

        String packageName = _args[0];

        File inputFile = new File(_args[1]);
        if (!inputFile.exists() || !inputFile.canRead()) {
            throw new IllegalArgumentException("Input file does not exist or cannot be read");
        }

        File outputFile = _args[2].endsWith(".java") ? new File(_args[2]) : new File(_args[2] + ".java");

        if (outputFile.exists() && !outputFile.canWrite() || outputFile.isDirectory()) {
            throw new IllegalArgumentException("Output file either exists and cannot be overwritten or is a directory");
        }

        if (!outputFile.getParentFile().exists()) {
            if (!outputFile.getParentFile().mkdirs()) {
                throw new IllegalArgumentException("Unable to create output directory");
            }
        }

        Properties props = new Properties();

        try {
            props.load(new FileInputStream(inputFile));
        } catch (IOException _ex) {
            throw new UncheckedIOException(_ex);
        }

        List<Entry<String, String>> readInput = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            String constName = key.replace('.', '_').replace(',', '_').toUpperCase();
            readInput.add(Map.entry(constName, key));
        }

        String clzName = outputFile.getName().replaceFirst("\\.java$", "");

        List<String> classContent = new ArrayList<>();
        classContent.add(String.format("package %s;", packageName));
        classContent.add("");
        classContent.add(String.format("public final class %s {", clzName));
        classContent.add("");

        int block = 0;
        for (Entry<String, String> keyVal : readInput) {
            classContent.add(String.format("    public static final String %s = \"%s\";", keyVal.getKey(), keyVal.getValue()));
            block++;
            if (block == 10) {
                classContent.add("");
                block = 0;
            }
        }
        classContent.add("");
        classContent.add(String.format("    private %s() {}", clzName));
        classContent.add("");

        classContent.add("}");

        try {
            Files.write(outputFile.toPath(), classContent, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Created constant class: " + outputFile);
        } catch (IOException _ex) {
            throw new UncheckedIOException(_ex);
        }
    }

}