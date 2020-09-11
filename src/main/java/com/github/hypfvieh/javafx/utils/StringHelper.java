package com.github.hypfvieh.javafx.utils;

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

}
