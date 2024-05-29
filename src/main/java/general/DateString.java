package general;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateString {
    private LocalDateTime localDateTime = null;

    public DateString(Object date) {
        localDateTime = extraire_dateTime(date);
    }

    public static LocalDate getLocalDate(Object hypoDate) {
        LocalDateTime dateTime = extraire_dateTime(hypoDate);
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    public static LocalDateTime getLocalDateTime(Object hypoDate) {
        return extraire_dateTime(hypoDate);
    }

    private static LocalDateTime extraire_dateTime(Object date) {
        LocalDateTime terug = null;
        if (date instanceof LocalDateTime) {
            return (LocalDateTime) date;
        }
        if (date instanceof LocalDate) {
            return ((LocalDate) date).atStartOfDay();
        }
        if (date instanceof String) {
            terug = test_if_iso(date);
            if (terug != null) {
                return terug;
            }
            terug = test_if_iso_date(date);
            if (terug != null) {
                return terug;
            }
            terug = test_if_standard(date);
            if (terug != null) {
                return terug;
            }
            terug = test_if_standard2(date);
            if (terug != null) {
                return terug;
            }
            terug = test_if_standard_date(date);
            if (terug != null) {
                return terug;
            }
            terug = test_if_frStandard(date);
            if (terug != null) {
                return terug;
            }
            terug = test_if_frStandard_date(date);
            if (terug != null) {
                return terug;
            }
        }
        System.err.println("DateString : échec de conversion vers une date de <" + date + "> : Format non reconnu !");
        return null;
    }

    private static LocalDateTime test_if_iso(Object date) {
        String dateString = String.valueOf(date).split("\\.")[0];
        try {
            return java.time.LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            /*System.err.println("DateString/extraire_dateTime : " + date + " n'est pas au format ISO !!!" +
                    "J'essaye autre chose...");*/
            return null;
        }
    }

    private static LocalDateTime test_if_standard(Object date) {
        String dateString = String.valueOf(date).split("\\.")[0];
        try {
            return java.time.LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        } catch (Exception e) {
            //System.err.println("DateString/extraire_dateTime : " + date + " n'est pas au format yyyy/MM/dd HH:mm:ss !!!");
            return null;
        }
    }

    private static LocalDateTime test_if_standard2(Object date) {
        String dateString = String.valueOf(date).split("\\.")[0];
        if (dateString.length() > 19) {
            dateString = dateString.substring(0, 19);
        }
        try {
            return java.time.LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            //System.err.println("DateString/extraire_dateTime : " + date + " n'est pas au format yyyy-MM-dd HH:mm:ss !!!");
            return null;
        }
    }


    private static LocalDateTime test_if_frStandard(Object date) {
        try {
            return java.time.LocalDateTime.parse(String.valueOf(date), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        } catch (Exception e) {
            //System.err.println("DateString/extraire_dateTime : " + date + " n'est pas au format dd/MM/yyyy HH:mm:ss !!!");
            return null;
        }
    }

    private static LocalDateTime test_if_iso_date(Object date) {
        try {
            return LocalDate.parse(String.valueOf(date), DateTimeFormatter.ISO_DATE).atStartOfDay();
        } catch (Exception e) {
           /* System.err.println("DateString/extraire_date : " + date + " n'est pas au format ISO !!!" +
                    "J'essaye autre chose...");*/
            return null;
        }
    }

    private static LocalDateTime test_if_standard_date(Object date) {
        try {
            return LocalDate.parse(String.valueOf(date), DateTimeFormatter.ofPattern("yyyy/MM/dd")).atStartOfDay();
        } catch (Exception e) {
            //System.err.println("DateString/extraire_date : " + date + " n'est pas au format yyyy/MM/dd !!!");
            return null;
        }
    }

    private static LocalDateTime test_if_frStandard_date(Object date) {
        try {
            return LocalDateTime.parse(String.valueOf(date), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            //System.err.println("DateString/extraire_date : " + date + " n'est pas au format dd/MM/yyyy !!!");
            return null;
        }
    }

    public LocalDate getLocalDate() {
        if (localDateTime == null) return null;
        return localDateTime.toLocalDate();
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public String getLocalDateTimeISO() {
        if (localDateTime == null) return null;
        return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public String getLocalDateISO() {
        if (localDateTime == null) return null;
        return localDateTime.toLocalDate().format(DateTimeFormatter.ISO_DATE);
    }
}
