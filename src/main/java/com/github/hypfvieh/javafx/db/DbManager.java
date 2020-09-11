package com.github.hypfvieh.javafx.db;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Singleton manager class to access database easily.
 * <p>
 * This class holds a instance of {@link QueryUtil} which can be gathered by calling {@link #getQueryUtil()}.
 * That will guarantee that you will always get the same database session.
 * Please note that this may not work well with multi-threading.
 * <p>
 * <strong>Requires hibernate, please add it to your project, otherwise using this class will fail
 * </strong>
 *
 * @author hypfvieh
 * @since v11.0.0 - 2020-09-11
 */
public class DbManager implements AutoCloseable {
    private static final SessionFactory sessionFactory = DbManager.initDb();

    private QueryUtil queryUtil = new QueryUtil(sessionFactory);
    private final List<QueryUtil> otherSessions = new ArrayList<>();


    private static DbManager INSTANCE = new DbManager();

    /**
     * Initialize Hibernate {@link SessionFactory}.
     * @return SessionFactory
     * @throws RuntimeException when {@link SessionFactory} creation failed
     */
    public static SessionFactory initDb() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            return new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception _ex) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
            throw new RuntimeException("Could not initialize database", _ex);
        }

    }

    public static void newInstance() {
        INSTANCE = new DbManager();
    }

    private DbManager() {

    }

    /**
     * Returns a new {@link QueryUtil}.
     * All returned instances will be tracked and will be close if MainWindow is closed.
     * @return {@link SessionFactory}
     */
    public static QueryUtil getNewQueryUtil() {
        QueryUtil qutil = new QueryUtil(sessionFactory);
        INSTANCE.otherSessions.add(qutil);
        return qutil;
    }


    /**
     * Get the {@link QueryUtil} with the currently used session.
     * @return {@link QueryUtil}
     */
    public static QueryUtil getQueryUtil() {
        return INSTANCE.queryUtil;
    }

    @Override
    public void close() {
        INSTANCE.queryUtil.close();
        INSTANCE.otherSessions.forEach(s -> s.closeSession());
    }

    public static void closeInstance() {
        INSTANCE.close();
    }

}
