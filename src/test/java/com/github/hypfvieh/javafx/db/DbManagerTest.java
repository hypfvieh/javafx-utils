package com.github.hypfvieh.javafx.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
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
        DbManager.useEncryption(s -> {
            String rem = s.substring(4, s.length() -1);
            return new StringBuilder(rem).reverse().toString();
        });

        Map<DbCred, String> dbCredentials = DbManager.getDbCredentials();

        assertEquals("sa", dbCredentials.get(DbCred.USERNAME));
        assertEquals("TODO", dbCredentials.get(DbCred.PASSWORD));

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
