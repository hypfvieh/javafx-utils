package com.github.hypfvieh.javafx.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Helper for string handling.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class StringHelper {
    /**
     * Count the number of instances of substring within a string.
     *
     * @param string String to look for substring in.
     * @param substring Sub-string to look for.
     * @return Count of substrings in string.
     */
    public static int countSubString(String string, String substring) {
        int count = 0;
        int idx = 0;

        while ((idx = string.indexOf(substring, idx)) != -1) {
            idx++;
            count++;
        }

        return count;
    }

    /**
     * Check if string is blank.
     *
     * @param _str string to check
     * @return true if string is null or blank
     */
    public static boolean isBlank(String _str) {
        if (_str == null) {
            return true;
        }

        return _str.trim().isEmpty();
    }

    /**
     * Checks if given String is null or blank.
     * Throws {@link IllegalArgumentException} when String is blank/null.
     * 
     * @param _str string to check
     * @param _message message to show in exception
     * @return input string if not blank or null
     */
    public static String requireNonBlank(String _str, String _message) {
        if (_str == null || _str.isBlank()) {
            throw new IllegalArgumentException(_message);
        }
        return _str;
    }
    
    /**
     * Return the stack trace of the given throwable as String.
     *
     * @param _throwable throwable/exception
     * @return stack trace as String or null if input was null
     */
    public static String getStackTrace(Throwable _throwable) {
        if (_throwable == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        _throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
