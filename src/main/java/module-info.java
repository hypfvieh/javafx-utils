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
    requires java.prefs;
    requires transitive org.slf4j;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires static transitive org.hibernate.orm.core;
    requires static jakarta.persistence;

}
