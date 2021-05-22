package com.github.hypfvieh.javafx.utils;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Helper for working with resource bundle for translations.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class Translator {

    private final Logger logger = System.getLogger(getClass().getName());
    private ResourceBundle rb;

    public Translator(String _bundleName, Locale _locale) {
        try {
            rb = ResourceBundle.getBundle(_bundleName, _locale == null ? Locale.getDefault() : _locale);
        } catch (java.util.MissingResourceException _ex) {
            logger.log(Level.WARNING, "Requested resource bundle {} not found", _bundleName);
        }
    }

    public Translator(String _bundleName) {
        this(_bundleName, Locale.getDefault());
    }

    /**
     * Helper for translating strings.
     *
     * @param _key key to lookup
     * @param _default default to use if no translation found
     *
     * @return value from bundle or default
     */
    public String t(String _key, String _default) {
        String result = _default;
        result = result.replace("%n", System.lineSeparator());

        if (rb == null) {
            return _default;
        }
        if (_key == null) {
            return _default;
        }

        try {
            result = rb.getString(_key);
        } catch (MissingResourceException _ex) {
            return _default;
        }

        return result == null ? _default : result.replace("%n", System.lineSeparator());
    }

    /**
     * Get translation of the given key, adding provided placeholders.
     *
     * @param _key key to lookup
     * @param _default default to use if lookup fails
     * @param _placeholder placeholders to put in string
     *
     * @return string or default
     */
    public String t(String _key, String _default, Object... _placeholder) {
        String str = t(_key, _default);
        if (str.contains("%s")) {
            int countSubString = StringHelper.countSubString(str, "%s");
            if (countSubString != _placeholder.length) {
                logger.log(Level.DEBUG, "Placeholder count differs between found and provided placeholders in value of {}, placeholders provided: {}", _key, _placeholder.length);
            }
            List<Object> placeholders = new ArrayList<>(Arrays.asList(_placeholder));
            if (countSubString > _placeholder.length) {
                for (int i = _placeholder.length; i < countSubString; i++) {
                    placeholders.add("missing_placeholder_" + i);
                }
            } else if (countSubString < _placeholder.length) {
                placeholders = placeholders.subList(0, countSubString);
            }
            return String.format(str, placeholders.toArray());
        }
        return str;
    }
}
