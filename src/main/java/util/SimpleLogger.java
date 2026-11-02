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

    public static SimpleLogger getLogger(Class<?> cls) {
        return getLogger(cls == null ? "null" : cls.getName());
    }

    public static SimpleLogger getLogger(String name) {
        Objects.requireNonNull(name);
        return LOGGERS.computeIfAbsent(name, SimpleLogger::new);
    }

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
    public static Level getLevel() {
        return globalLevel;
    }

    public static void setLevel(Level lvl) {
        if (lvl != null) globalLevel = lvl;
    }

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

    public void info(String msg) {
        log(Level.INFO, msg, null);
    }

    public void debug(String msg) {
        log(Level.DEBUG, msg, null);
    }

    public void warn(String msg) {
        log(Level.WARN, msg, null);
    }

    public void error(String msg) {
        log(Level.ERROR, msg, null);
    }

    public void error(String msg, Throwable t) {
        log(Level.ERROR, msg, t);
    }

    public enum Level {ERROR, WARN, INFO, DEBUG}
}
