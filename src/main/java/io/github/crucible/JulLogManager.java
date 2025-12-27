package io.github.crucible;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
/**
 * Backport and simplify log4j2-jul
 */
public class JulLogManager extends LogManager {

    @Override
    public boolean addLogger(Logger logger) {
        return false;
    }

    @Override
    public Logger getLogger(String name) {
        try {
            return new JulToLog4jLogger(name);
        } catch (Throwable e) {
            return new JulNoopLogger(name);
        }
    }

    public static class JulNoopLogger extends Logger {

        protected JulNoopLogger(String name) {
            super(name, null);
        }
    }

    public static class JulToLog4jLogger extends Logger {
        private final org.apache.logging.log4j.Logger logger;

        public JulToLog4jLogger(String name) {
            super(name, null);
            this.logger = org.apache.logging.log4j.LogManager.getLogger(name);
        }

        @Override
        public void log(LogRecord record) {
            final org.apache.logging.log4j.Level level = convertLevel(record.getLevel());
            final Object[] parameters = record.getParameters();
            final MessageFactory messageFactory = logger.getMessageFactory();
            final Message message = parameters == null
                    ? messageFactory.newMessage(record.getMessage())
                    : messageFactory.newMessage(record.getMessage(), parameters);
            final Throwable thrown = record.getThrown();
            logger.log(level, null, message, thrown);
        }

        @Override
        public boolean isLoggable(Level level) {
            return logger.isEnabled(convertLevel(level));
        }

        @Override
        public String getName() {
            return super.getName();
        }

        @Override
        public Level getLevel() {
            return super.getLevel();
        }

        @Override
        public void setLevel(Level newLevel) throws SecurityException {
            super.setLevel(newLevel);
        }

        public org.apache.logging.log4j.Level convertLevel(Level level) {
            if(level == Level.ALL) {
                return org.apache.logging.log4j.Level.ALL;
            }
            if(level == Level.FINEST || level == Level.FINER) {
                return org.apache.logging.log4j.Level.TRACE;
            }
            if(level == Level.FINE) {
                return org.apache.logging.log4j.Level.DEBUG;
            }
            if(level == Level.INFO) {
                return org.apache.logging.log4j.Level.INFO;
            }
            if(level == Level.WARNING) {
                return org.apache.logging.log4j.Level.WARN;
            }
            if(level == Level.SEVERE) {
                return org.apache.logging.log4j.Level.ERROR;
            }
            return org.apache.logging.log4j.Level.TRACE;
        }

        @Override
        public void setParent(final Logger parent) {
            logger.warn("Logger.setParent() not supported by jul-to-log4j LogManager. Logs may be broken");
        }

        @Override
        public Logger getParent() {
            final org.apache.logging.log4j.core.Logger parent = ((org.apache.logging.log4j.core.Logger)logger).getParent();
            return parent == null ? null : Logger.getLogger(parent.getName());
        }

        @Override
        public void log(final Level level, final String msg) {
            logger.log(convertLevel(level), msg);
        }

        @Override
        public void log(final Level level, final String msg, final Object param1) {
            logger.log(convertLevel(level), msg, param1);
        }

        @Override
        public void log(final Level level, final String msg, final Object[] params) {
            logger.log(convertLevel(level), msg, params);
        }

        @Override
        public void log(final Level level, final String msg, final Throwable thrown) {
            logger.log(convertLevel(level), msg, thrown);
        }

        @Override
        public void logp(final Level level, final String sourceClass, final String sourceMethod, final String msg) {
            log(level, msg);
        }

        @Override
        public void logp(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String msg,
                final Object param1) {
            log(level, msg, param1);
        }

        @Override
        public void logp(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String msg,
                final Object[] params) {
            log(level, msg, params);
        }

        @Override
        public void logp(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String msg,
                final Throwable thrown) {
            log(level, msg, thrown);
        }

        @Override
        public void logrb(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String bundleName,
                final String msg) {
            log(level, msg);
        }

        @Override
        public void logrb(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String bundleName,
                final String msg,
                final Object param1) {
            log(level, msg, param1);
        }

        @Override
        public void logrb(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String bundleName,
                final String msg,
                final Object[] params) {
            log(level, msg, params);
        }

        @Override
        public void logrb(
                final Level level,
                final String sourceClass,
                final String sourceMethod,
                final String bundleName,
                final String msg,
                final Throwable thrown) {
            log(level, msg, thrown);
        }

        @Override
        public void throwing(final String sourceClass, final String sourceMethod, final Throwable thrown) {
            logger.throwing(thrown);
        }

        @Override
        public void severe(final String msg) {
            logger.log(org.apache.logging.log4j.Level.ERROR, null, msg);
        }

        @Override
        public void warning(final String msg) {
            logger.log(org.apache.logging.log4j.Level.WARN, null, msg);
        }

        @Override
        public void info(final String msg) {
            logger.log(org.apache.logging.log4j.Level.INFO, null, msg);
        }

        @Override
        public void config(final String msg) {
            logger.log(org.apache.logging.log4j.Level.INFO, null, msg);
        }

        @Override
        public void fine(final String msg) {
            logger.log(org.apache.logging.log4j.Level.DEBUG, null, msg);
        }

        @Override
        public void finer(final String msg) {
            logger.log(org.apache.logging.log4j.Level.TRACE, null, msg);
        }

        @Override
        public void finest(final String msg) {
            logger.log(org.apache.logging.log4j.Level.TRACE, null, msg);
        }
    }
}
