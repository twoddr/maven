package general;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

public class ExtracteurPrimitif {
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
        try {
            return Float.parseFloat(String.valueOf(suspect));
        } catch (NumberFormatException e) {
            return 0;
        }
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

        try {
            return Integer.parseInt(String.valueOf(suspect));
        } catch (NumberFormatException e) {
            return 0;
        }

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
        try {
            return Long.parseLong(String.valueOf(suspect));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String extraire_string(Object suspect) {
        return String.valueOf(suspect);
    }

    public static boolean extraire_booleen(Object suspect) {
        if (suspect instanceof Boolean) {
            return (Boolean) suspect;
        }
        return Boolean.parseBoolean(String.valueOf(suspect));
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
        try {
            return LocalDate.parse(String.valueOf(suspect));
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate extraire_date(Object suspect, String format) {
        LocalDate localDate = extraire_date(suspect);
        if (localDate != null) return localDate;
        try {
            return LocalDate.parse(String.valueOf(suspect), DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            return null;
        }
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
        try {
            return LocalDateTime.parse(String.valueOf(suspect));
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime extraire_dateHeure(Object suspect, String pattern) {
        LocalDateTime localDateTime = extraire_dateHeure(suspect);
        if (localDateTime != null) return localDateTime;
        try {
            return LocalDateTime.parse(String.valueOf(suspect), DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return null;
        }
    }


}
