package general;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;

public class ExtracteurHashMap {
    public static float extraire_float(Object suspect) {
        if (suspect instanceof Float) {
            return (Float) suspect;
        }
        if (suspect instanceof Integer) {
            return (Integer) suspect;
        }
        if (suspect instanceof Long) {
            return ((Long) suspect).intValue();
        }
        if (suspect instanceof String) {
            try {
                return Float.parseFloat((String) suspect);
            } catch (NumberFormatException e) {
                return 0f;
            }
        }
        return 0f;
    }

    public static int extraire_int(Object suspect) {
        if (suspect instanceof Integer) {
            return (Integer) suspect;
        }
        if (suspect instanceof Long) {
            return ((Long) suspect).intValue();
        }
        if (suspect instanceof Float) {
            return ((Float) suspect).intValue();
        }
        if (suspect instanceof String) {
            try {
                return Integer.parseInt((String) suspect);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static long extraire_long(Object suspect) {
        if (suspect instanceof Long) {
            return (Long) suspect;
        }
        if (suspect instanceof Integer) {
            return ((Integer) suspect).longValue();
        }
        if (suspect instanceof Float) {
            return ((Float) suspect).longValue();
        }
        if (suspect instanceof String) {
            try {
                return Long.parseLong((String) suspect);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0;
    }

    public static String extraire_string(Object suspect) {
        return suspect + "";
    }

    public static boolean extraire_booleen(Object suspect) {
        if (suspect instanceof Boolean) {
            return (Boolean) suspect;
        }
        return false;
    }

    public static Collection extraire_liste(Object suspect) {
        if (suspect instanceof Collection) {
            return (Collection) suspect;
        }
        return new ArrayList();
    }

    public static LocalDate extraire_date(Object suspect) {
        if (suspect instanceof LocalDateTime) {
            return ((LocalDateTime) suspect).toLocalDate();
        }
        if (suspect instanceof LocalDate) {
            return (LocalDate) suspect;
        }
        if (suspect instanceof Long) {
            return Instant.ofEpochMilli((long) suspect).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    public static LocalDateTime extraire_dateHeure(Object suspect) {
        if (suspect instanceof LocalDate) {
            return ((LocalDate) suspect).atStartOfDay();
        }
        if (suspect instanceof LocalDateTime) {
            return (LocalDateTime) suspect;
        }
        if (suspect instanceof Long) {
            return Instant.ofEpochMilli((long) suspect).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }


}
