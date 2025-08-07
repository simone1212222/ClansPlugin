package com.develop.clansPlugin.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginLogger {

    private final Logger logger;

    public PluginLogger(Logger baseLogger) {
        this.logger = baseLogger;
    }

    public void info(String message) {
        logger.log(Level.INFO, message);
    }

    public void warn(String message) {
        logger.log(Level.WARNING, message);
    }

    public void error(String message) {
        logger.log(Level.SEVERE, message);
    }

    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

}
