package com.github.hypfvieh.javafx.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.github.hypfvieh.javafx.db.DbManager.DbCred;

class DbManagerTest {

    @AfterEach
    void afterEach() {
        File dbFile = new File(System.getProperty("java.io.tmpdir"), "test.mv.db");
        File dbTraceFile = new File(System.getProperty("java.io.tmpdir"), "test.trace.db");
        dbFile.delete();
        dbTraceFile.delete();
    }

    @Test
    void testEncryptedCredentials() {
        DbManager.setHibernateXml("hibernate_pw_encrypted.cfg.xml");
        DbManager.useEncryption((c,s) -> {
            if (c == DbCred.PASSWORD) {
                String rem = s.substring(4, s.length() -1);
                return new StringBuilder(rem).reverse().toString();
            } else if (c == DbCred.DRIVER ) {
                String[] driverParts = s.split("\\.");
                List<String> driverName = new ArrayList<>();
                for (int i = driverParts.length -1; i >= 0; i--) {
                    driverName.add(driverParts[i]);
                }
                return String.join(".", driverName);
            }
            return s;
        });

        Map<DbCred, String> dbCredentials = DbManager.getDbCredentials();

        assertEquals("sa", dbCredentials.get(DbCred.USERNAME));
        assertEquals("TODO", dbCredentials.get(DbCred.PASSWORD));
        assertEquals("org.h2.Driver", dbCredentials.get(DbCred.DRIVER));

        DbManager.closeInstance();
    }

    @Test
    void testNoPwCredentials() {
        DbManager.setHibernateXml("hibernate_nopw.cfg.xml");

        Map<DbCred, String> dbCredentials = DbManager.getDbCredentials();

        assertEquals("sa", dbCredentials.get(DbCred.USERNAME));
        assertEquals("", dbCredentials.get(DbCred.PASSWORD));

        DbManager.closeInstance();
    }

}
