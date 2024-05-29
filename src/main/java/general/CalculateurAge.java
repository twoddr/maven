package general;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by didiertowe on 21/06/17.
 */
public class CalculateurAge {

    private static LocalDateTime date;

    public CalculateurAge(LocalDateTime d) {
        date = d;
    }

    /**
     * Détermine l'age de l'individu actuel: en années si plus d'un an; en mois
     * si né après le mois en cours; en jours si né avant aujourd'hui; Dans tous
     * les autres cas, l'age vaut zéro
     *
     * @return l'age de l'individu en cours
     */
    public String getAgeString() {
        LocalDateTime now = LocalDateTime.now();
        long deltaJours = ChronoUnit.DAYS.between(date, now);
        boolean aMoinsDUnAn = deltaJours < 365;
        long nbreMois = ChronoUnit.MONTHS.between(date, now);
        int jourDuMois = date.getDayOfMonth();
        LocalDateTime jourAnniv = LocalDate.of(now.getYear(), now.getMonth(), jourDuMois).atStartOfDay();
        if (aMoinsDUnAn) {
            if (deltaJours < 31) {
                return deltaJours + (deltaJours > 1 ? " jours" : " jour");
            }

            long nbreJours = ChronoUnit.DAYS.between(jourAnniv, now);
            if (nbreJours < 0) {
                nbreMois--;
                nbreJours = ChronoUnit.DAYS.between(jourAnniv.minusMonths(1), now);
                if (nbreJours == 0) {
                    return nbreMois + " mois";
                }
            }

            return nbreMois + " mois, " + nbreJours + (nbreJours > 1 ? " jours" : " jour");
        }
        long ageInt = ChronoUnit.YEARS.between(date, now);
        if (ageInt < 6) {
            long deltaMois = ChronoUnit.MONTHS.between(jourAnniv, now);
            if (deltaMois < 0) {
                ageInt--;
                deltaMois = ChronoUnit.MONTHS.between(jourAnniv.minusYears(1), now);
                if (deltaMois == 0) {
                    return ageInt + (ageInt > 1 ? " ans" : " an");
                }
            }
            String out = ageInt + (ageInt > 1 ? " ans" : " an");
            return out + ", " + nbreMois + " mois";
        }
        return ageInt + " ans";
    }

    public long getAge() {
        return ChronoUnit.YEARS.between(date, LocalDateTime.now());
    }

}
