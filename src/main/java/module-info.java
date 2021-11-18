module com.github.hypfvieh.javafx.utils {
    opens com.github.hypfvieh.javafx.beans;
    opens com.github.hypfvieh.javafx.factories;
    opens com.github.hypfvieh.javafx.app;
    opens com.github.hypfvieh.javafx.interfaces;
    opens com.github.hypfvieh.javafx.controls.listview;
    opens com.github.hypfvieh.javafx.windowsaver;
    opens com.github.hypfvieh.javafx.db;
    opens com.github.hypfvieh.javafx.controls;
    opens com.github.hypfvieh.javafx.fx;
    opens com.github.hypfvieh.javafx.other;
    opens com.github.hypfvieh.javafx.ui;
    opens com.github.hypfvieh.javafx.controls.table;
    opens com.github.hypfvieh.javafx.formatter;
    opens com.github.hypfvieh.javafx.functional;
    opens com.github.hypfvieh.javafx.utils;
    opens com.github.hypfvieh.javafx.windows.interfaces;
    opens com.github.hypfvieh.javafx.converter;
    opens com.github.hypfvieh.javafx.generic;
    opens com.github.hypfvieh.javafx.dialogs;
    opens com.github.hypfvieh.javafx.fx.fonts;

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
