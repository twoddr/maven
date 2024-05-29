package general;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;

public class ConvertisseurDeCalendrier {
    public static Calendar localDateTime2Calendar(LocalDateTime dateTime) {
        Calendar retour = Calendar.getInstance();
        retour.set(dateTime.getYear(),
                convertir_mois(dateTime.getMonth()),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute());
        return retour;
    }

    public static LocalDateTime calendar2LocalDateTime(Calendar calendrier) {
        LocalDateTime retour = LocalDateTime.of(calendrier.get(Calendar.YEAR),
                calendrier.get(Calendar.MONTH) + 1,
                calendrier.get(Calendar.DAY_OF_MONTH),
                calendrier.get(Calendar.HOUR),
                calendrier.get(Calendar.MINUTE));
        return retour;
    }

    private static int convertir_mois(Month month) {
        if (month.equals(Month.JANUARY)) {
            return Calendar.JANUARY;
        } else if (month.equals(Month.FEBRUARY)) {
            return Calendar.FEBRUARY;
        } else if (month.equals(Month.MARCH)) {
            return Calendar.MARCH;
        } else if (month.equals(Month.APRIL)) {
            return Calendar.APRIL;
        } else if (month.equals(Month.MAY)) {
            return Calendar.MAY;
        } else if (month.equals(Month.JUNE)) {
            return Calendar.JUNE;
        } else if (month.equals(Month.JULY)) {
            return Calendar.JULY;
        } else if (month.equals(Month.AUGUST)) {
            return Calendar.AUGUST;
        } else if (month.equals(Month.SEPTEMBER)) {
            return Calendar.SEPTEMBER;
        } else if (month.equals(Month.OCTOBER)) {
            return Calendar.OCTOBER;
        } else if (month.equals(Month.NOVEMBER)) {
            return Calendar.NOVEMBER;
        } else if (month.equals(Month.DECEMBER)) {
            return Calendar.DECEMBER;
        }
        System.err.println("Didier : problème dans Convertisseur calendrier : " +
                "Impossible de trouver le mois " + month);
        return 0;
    }
}
