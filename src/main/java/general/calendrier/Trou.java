package general.calendrier;

import general.InformateurObjet;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Trou extends Evenement {

    /**
     * Le trou par défaut dure toute la journée
     */
    public Trou(String nomType,
                String nomID,
                String nomDescription,
                String nomCouleurTexte,
                String nomIDProprietaire,
                String nomDateDebut,
                String nomDateFin,
                String nomCouleurCadre,
                String nomCouleurFond,
                String nomLieu, InformateurObjet informateur) {
        super(nomType, nomID, nomDescription, nomCouleurTexte, nomIDProprietaire,
                nomDateDebut, nomDateFin, nomCouleurCadre, nomCouleurFond, nomLieu,
                informateur);

        setDebut_Duree(LocalDate.now().atStartOfDay(), 1439);
        setColor();
        setPriorite(0);
    }

    /**
     * Le trou par défaut dure toute la journée
     */
    public Trou(ArrayList<String> arrayList, InformateurObjet informateur) {
        super(arrayList, informateur);

        setDebut_Duree(LocalDate.now().atStartOfDay(), 1439);
        setColor();
        setPriorite(0);
    }

    private void setColor() {
        setCouleurCadre(Color.WHITE);
        setCouleurFond(Color.WHITE);
        setCouleurTexte(Color.BLUE);
    }

    /**
     * Copie toutes les données de l'hashMap dans un nouveau trou
     * ID compris !
     *
     * @return la copie de l'objet actuel
     */
    @Override
    public Evenement getCopyOf() {
        Trou nouveau = new Trou(getType(),
                nomID,
                nomNOM,
                COULEUR_TEXTE,
                nomIDPROPRIETAIRE,
                nomDEBUT,
                nomFIN,
                COULEUR_CADRE,
                COULEUR_FOND,
                NOM_LIEU, getInformateurHashObjet());
        nouveau.setData(getData(), true);
        nouveau.setPriorite(getPriorite());
        return nouveau;
    }

    @Override
    public String getDescription() {
        return toString() + "\n" + super.getDescription();
    }

    @Override
    public String toString() {
        try {
            return getDebut().format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " -> " + getFin().format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (NullPointerException e) {
            return " - Vide - ";
        }
    }

    @Override
    public String getCalendarString() {
        return getStringHeureDebut() + " -> " + getStringHeureFin();
    }
}

