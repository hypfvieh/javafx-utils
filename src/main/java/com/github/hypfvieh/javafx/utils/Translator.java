package com.github.hypfvieh.javafx.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper for working with resource bundle for translations base on class names.
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class Translator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Set<ResourceBundle> bundles = new LinkedHashSet<>();


    /**
     * Create a new translator using the given bundle name and the given locale.
     *
     * @param _bundleName bundle name
     * @param _locale locale
     */
    public Translator(String _bundleName, Locale _locale) {
       addResourceBundle(_bundleName, _locale);
    }

    /**
     * Create a new translator using the given bundle name and the current default locale.
     *
     * @param _bundleName bundle name
     */
    public Translator(String _bundleName) {
        this(_bundleName, Locale.getDefault());
    }

    /**
     * Creates a new instance which will hold all resource bundles belonging to the given class and parent classes.
     * @param _clz class to use (never null)
     * @param _subdir subdirectory where to look for resource bundles
     * @param _useSimpleName true to look for file names with simple class name instead of FQCN
     * @param _locale locale to use
     */
    public Translator(Class<?> _clz, String _subdir, boolean _useSimpleName, Locale _locale) {
        addResourceBundle(_clz, _subdir, _useSimpleName, _locale);
    }

    /**
     * Creates a new instance which will hold all resource bundles belonging to the given class and parent classes.
     * Will use the current default locale.
     *
     * @param _clz class to use (never null)
     * @param _subdir subdirectory where to look for resource bundles
     * @param _useSimpleName true to look for file names with simple class name instead of FQCN
     */
    public Translator(Class<?> _clz, String _subdir, boolean _useSimpleName) {
        this(_clz, _subdir, _useSimpleName, Locale.getDefault());
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
        String defaultResult = _default;
        defaultResult = defaultResult.replace("%n", System.lineSeparator());

        if (bundles.isEmpty()) {
            return _default;
        }
        if (_key == null) {
            return _default;
        }

        for (ResourceBundle rb : bundles) {
            try {
                 String string = rb.getString(_key);
                 if (string != null) {
                     return string.replace("%n", System.lineSeparator());
                 }
            } catch (MissingResourceException _ex) {
            }
        }

        return defaultResult;

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
                logger.debug("Placeholder count differs between found and provided placeholders in value of {}, placeholders provided: {}", _key, _placeholder.length);
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

    /**
     * Will add another resource bundle to the list of internal bundles.
     * The additional bundle will be appended to the list, which means
     * it will only be tried if all entries before have failed.
     *
     * @param _bundleName bundle name
     * @param _locale locale
     */
    public void addResourceBundle(String _bundleName, Locale _locale) {
        if (StringHelper.isBlank(_bundleName)) {
            logger.warn("Cannot load empty/blank bundle name");
            return;
        }

        try {
            bundles.add(ResourceBundle.getBundle(_bundleName, _locale == null ? Locale.getDefault() : _locale));
        } catch (java.util.MissingResourceException _ex) {
            logger.warn("Requested resource bundle {} not found", _bundleName);
        }
    }

    /**
     * Will add resource bundles based on the given class to the list of internal bundles.
     * <p>
     * It will also add all bundles belonging to parent classes of the given class in order (child -&gt; parent)
     * The additional bundle will be appended to the list, which means
     * <p>
     * Additional resources will only be tried if all entries before have failed.
     *
     * @param _clz class to start with
     * @param _subdir subdirectory where to look for resource bundles
     * @param _useSimpleName use simple class name instead of FQCN
     * @param _locale locale
     */
    public void addResourceBundle(Class<?> _clz, String _subdir, boolean _useSimpleName, Locale _locale) {
        Class<?> clz = Objects.requireNonNull(_clz, "Class cannot be null");
        Locale locale = _locale == null ? Locale.getDefault() : _locale;

        Set<ResourceBundle> addBundles = new LinkedHashSet<>();

        while (clz != null) {
            String bundleName = _useSimpleName ? clz.getSimpleName(): clz.getName();
            if (_subdir != null) {
                bundleName = _subdir + "/" + bundleName;
            }
            try {
                addBundles.add(ResourceBundle.getBundle(bundleName, locale));
            } catch (java.util.MissingResourceException _ex) {
            }
            clz = clz.getSuperclass();
        }

        if (addBundles.isEmpty()) {
            logger.warn("No resource bundle found for {}", _clz.getName());
        } else {
            bundles.addAll(addBundles);
        }

    }
}
