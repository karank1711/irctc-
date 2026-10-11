package com.irctc.listener;

import com.irctc.service.AuthService;
import com.irctc.util.SchemaInitializer;
import com.irctc.util.StartupStatus;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AppStartupListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(AppStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            new SchemaInitializer().initialize();
            new AuthService().ensureDefaultAdmin();
            StartupStatus.clear();
            LOGGER.info("IRCTC Admin Dashboard is ready.");
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Startup database setup failed", ex);
            StartupStatus.fail("The database could not be initialized. Check the database settings in the .env file, then restart.");
        }
    }
}
