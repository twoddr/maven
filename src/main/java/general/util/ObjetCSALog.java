package general.util;

/**
 * Simple logging façade pour ObjetCSA.
 * Délègue à System.err/System.out si SLF4J non disponible (pas de dépendance).
 */
public final class ObjetCSALog {
    private ObjetCSALog() {}

    public static void info(String msg) {
        System.out.println(msg);
    }

    public static void warn(String msg) {
        System.err.println(msg);
    }

    public static void error(String msg) {
        System.err.println(msg);
    }

    public static void debug(String msg) {
        // par défaut, debug -> stdout
        System.out.println(msg);
    }
}

