module hypfvieh.javafx.utils {
    exports com.github.hypfvieh.javafx.beans;
    exports com.github.hypfvieh.javafx.factories;
    exports com.github.hypfvieh.javafx.app;
    exports com.github.hypfvieh.javafx.interfaces;
    exports com.github.hypfvieh.javafx.controls.listview;
    exports com.github.hypfvieh.javafx.windowsaver;
    exports com.github.hypfvieh.javafx.db;
    exports com.github.hypfvieh.javafx.controls;
    exports com.github.hypfvieh.javafx.fx;
    exports com.github.hypfvieh.javafx.other;
    exports com.github.hypfvieh.javafx.ui;
    exports com.github.hypfvieh.javafx.controls.table;
    exports com.github.hypfvieh.javafx.formatter;
    exports com.github.hypfvieh.javafx.functional;
    exports com.github.hypfvieh.javafx.utils;
    exports com.github.hypfvieh.javafx.windows.interfaces;
    exports com.github.hypfvieh.javafx.converter;
    exports com.github.hypfvieh.javafx.generic;
    exports com.github.hypfvieh.javafx.dialogs;
    exports com.github.hypfvieh.javafx.fx.fonts;

    requires java.desktop;
    requires java.naming;
    requires java.persistence;
    requires java.prefs;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive org.hibernate.orm.core;
}
