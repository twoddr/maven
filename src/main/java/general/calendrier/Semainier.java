package general.calendrier;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class Semainier {

    private LocalDate localDate;

    public Semainier(LocalDate date) {
        localDate = date;
    }

    /**
     * Donne le numéro de semaine
     *
     * @return le jour de l'année divisé par 7
     */
    public int getNumeroSemaine() {
        return localDate.getDayOfYear() / 7;
    }

    public LocalDate getLundi() {
        DayOfWeek jourSemaine = localDate.getDayOfWeek();
        LocalDate lundi = localDate.minusDays(jourSemaine.getValue() - 1);
        return lundi;
    }

    public LocalDate getMardi() {
        DayOfWeek jourSemaine = localDate.getDayOfWeek();
        LocalDate mardi = localDate.minusDays(jourSemaine.getValue() - 2);
        return mardi;
    }

    public LocalDate getMercredi() {
        DayOfWeek jourSemaine = localDate.getDayOfWeek();
        LocalDate mercredi = localDate.minusDays(jourSemaine.getValue() - 3);
        return mercredi;
    }

    public LocalDate getJeudi() {
        DayOfWeek jourSemaine = localDate.getDayOfWeek();
        LocalDate jeudi = localDate.minusDays(jourSemaine.getValue() - 4);
        return jeudi;
    }

    public LocalDate getVendredi() {
        DayOfWeek jourSemaine = localDate.getDayOfWeek();
        LocalDate vendredi = localDate.minusDays(jourSemaine.getValue() - 5);
        return vendredi;
    }

    public LocalDate getSamedi() {
        DayOfWeek jourSemaine = localDate.getDayOfWeek();
        LocalDate samedi = localDate.minusDays(jourSemaine.getValue() - 6);
        return samedi;
    }

    public LocalDate getDimanche() {
        DayOfWeek jourSemaine = localDate.getDayOfWeek();
        LocalDate dimanche = localDate.minusDays(jourSemaine.getValue() - 7);
        return dimanche;
    }
}
