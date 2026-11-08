package util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple, lightweight logger utilitaire.
 * KISS: pas de dépendances externes, API minimale.
 */
public class SimpleLogger {

    private static final ConcurrentMap<String, SimpleLogger> LOGGERS = new ConcurrentHashMap<>();
    private static volatile Level globalLevel = Level.INFO;
    private static volatile BufferedWriter fileWriter = null;
    private static final Thread shutdownHook = new Thread(() -> {
        try {
            close();
        } catch (Throwable ignored) {
        }
    });
    private static volatile boolean consoleOut = true;
    private final String name;

    private SimpleLogger(String name) {
        this.name = name;
    }

    /**
     * Retrieves a logger instance for the specified class.
     * Uses the class name as the logger name.
     *
     * @param cls the class to create a logger for, may be null
     * @return a SimpleLogger instance for the class
     */
    public static SimpleLogger getLogger(Class<?> cls) {
        return getLogger(cls == null ? "null" : cls.getName());
    }

    /**
     * Retrieves a logger instance with the specified name.
     * Uses a concurrent map to ensure thread-safe singleton behavior per name.
     *
     * @param name the name of the logger, must not be null
     * @return a SimpleLogger instance with the given name
     * @throws NullPointerException if name is null
     */
    public static SimpleLogger getLogger(String name) {
        Objects.requireNonNull(name);
        return LOGGERS.computeIfAbsent(name, SimpleLogger::new);
    }

    /**
     * Initializes the global SimpleLogger configuration.
     * Sets the minimum log level, output file, and console output preferences.
     * Registers a shutdown hook to cleanly close file resources.
     *
     * @param level the minimum log level to output, null keeps current level
     * @param file the log file path, null for file-only logging
     * @param console whether to output to console, true enables console output
     */
    public static synchronized void init(Level level, Path file, boolean console) {
        if (level != null) globalLevel = level;
        consoleOut = console;
        // try to open file
        if (file != null) {
            try {
                Path parent = file.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                OpenOption[] opts = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};
                fileWriter = Files.newBufferedWriter(file, StandardCharsets.UTF_8, opts);
            } catch (IOException e) {
                fileWriter = null;
                System.err.println("SimpleLogger: impossible d'ouvrir le fichier de log: " + e.getMessage());
            }
        }
        // Register a JVM shutdown hook to close the writer cleanly
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (Throwable ignored) {
        }
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (Throwable ignored) {
        }

        // ensure a startup message
        getLogger(SimpleLogger.class).info("SimpleLogger initialisé (level=" + globalLevel + ", file=" +
                (file == null ? "<none>" : file.toString()) +
                ", console=" + consoleOut + ")");
    }

    /**
     * Initializes SimpleLogger from a Properties object.
     * Expected properties:
     * - cm.logging.level: log level (ERROR, WARN, INFO, DEBUG)
     * - cm.logging.file: log file path (optional)
     * - cm.logging.console: console output flag (optional, defaults to true)
     *
     * @param props the properties configuration, null uses defaults
     */
    public static void initFromProperties(Properties props) {
        if (props == null) {
            init(null, null, true);
            return;
        }
        String levelStr = props.getProperty("cm.logging.level");
        String fileStr = props.getProperty("cm.logging.file");
        String consoleStr = props.getProperty("cm.logging.console");
        Level level = null;
        if (levelStr != null) {
            try {
                level = Level.valueOf(levelStr.trim().toUpperCase());
            } catch (Exception ignored) {
            }
        }
        Path file = null;
        if (fileStr != null && !fileStr.trim().isEmpty()) {
            file = Paths.get(fileStr.trim());
        }
        boolean console = true;
        if (consoleStr != null) {
            console = Boolean.parseBoolean(consoleStr.trim());
        }
        init(level, file, console);
    }

    // Public helpers to query/change global log level and to close resources
    /**
     * Gets the current global minimum log level.
     *
     * @return the current global log level
     */
    public static Level getLevel() {
        return globalLevel;
    }

    /**
     * Sets the global minimum log level.
     * Only messages at this level or higher will be logged.
     *
     * @param lvl the new log level, null keeps current level
     */
    public static void setLevel(Level lvl) {
        if (lvl != null) globalLevel = lvl;
    }

    /**
     * Closes the file writer and cleans up resources.
     * This method is thread-safe and can be called multiple times.
     * Automatically called during JVM shutdown via registered hook.
     */
    public static synchronized void close() {
        try {
            if (fileWriter != null) {
                try {
                    fileWriter.flush();
                } catch (IOException ignored) {
                }
                try {
                    fileWriter.close();
                } catch (IOException ignored) {
                }
                fileWriter = null;
            }
        } finally {
            // best effort: no-op
        }
    }

    private boolean isEnabled(Level lvl) {
        return lvl.ordinal() <= globalLevel.ordinal();
    }

    private String shortName() {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1) : name;
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    private void writeLine(String line) {
        synchronized (SimpleLogger.class) {
            try {
                if (consoleOut) {
                    System.out.println(line);
                }
                if (fileWriter != null) {
                    // write line and flush; support multi-line content by writing each line separately
                    String[] parts = line.split("\\r?\\n");
                    for (String p : parts) {
                        fileWriter.write(p);
                        fileWriter.newLine();
                    }
                    fileWriter.flush();
                }
            } catch (IOException e) {
                System.err.println("SimpleLogger write failed: " + e.getMessage());
            }
        }
    }

    private void log(Level lvl, String msg, Throwable t) {
        if (!isEnabled(lvl)) return;
        String line = String.format("%s [%s] %s %s - %s", timestamp(), lvl, Thread.currentThread().getName(), shortName(), msg == null ? "" : msg);
        writeLine(line);
        if (t != null) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            // write stack trace line by line to preserve readability and avoid giant single writes
            String[] lines = stack.split("\\r?\\n");
            for (String l : lines) {
                writeLine(l);
            }
        }
    }

    /**
     * Logs an informational message.
     * Only logged if INFO level is enabled globally.
     *
     * @param msg the message to log, may be null
     */
    public void info(String msg) {
        log(Level.INFO, msg, null);
    }

    /**
     * Logs a debug message.
     * Only logged if DEBUG level is enabled globally.
     *
     * @param msg the message to log, may be null
     */
    public void debug(String msg) {
        log(Level.DEBUG, msg, null);
    }

    /**
     * Logs a warning message.
     * Only logged if WARN level is enabled globally.
     *
     * @param msg the message to log, may be null
     */
    public void warn(String msg) {
        log(Level.WARN, msg, null);
    }

    /**
     * Logs an error message.
     * Only logged if ERROR level is enabled globally.
     *
     * @param msg the message to log, may be null
     */
    public void error(String msg) {
        log(Level.ERROR, msg, null);
    }

    /**
     * Logs an error message with an associated throwable.
     * The throwable's stack trace will be included in the log output.
     * Only logged if ERROR level is enabled globally.
     *
     * @param msg the message to log, may be null
     * @param t the throwable to log, may be null
     */
    public void error(String msg, Throwable t) {
        log(Level.ERROR, msg, t);
    }

    public enum Level {ERROR, WARN, INFO, DEBUG}
}
