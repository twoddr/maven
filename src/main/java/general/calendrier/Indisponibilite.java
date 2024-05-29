package general.calendrier;

import general.InformateurObjet;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Indisponibilite extends Evenement {

    /**
     * Le trou par défaut dure toute la journée
     */
    public Indisponibilite(String nomType,
                           String nomID,
                           String nomDescription,
                           String nomCouleurTexte,
                           String nomIDProprietaire,
                           String nomDateDebut,
                           String nomDateFin,
                           String nomCouleurCadre,
                           String nomCouleurFond,
                           String nomLieu,
                           InformateurObjet informateur) {
        super(nomType, nomID, nomDescription, nomCouleurTexte, nomIDProprietaire,
                nomDateDebut, nomDateFin, nomCouleurCadre, nomCouleurFond, nomLieu,
                informateur);

        setDebut_Duree(LocalDate.now().atStartOfDay(), 1439);
        setColor();
        setPriorite(2);
    }

    /**
     * L'indisponibilité par défaut dure toute la journée
     */
    public Indisponibilite(ArrayList<String> arrayList, InformateurObjet informateurObjet) {
        super(arrayList, informateurObjet);

        setDebut_Duree(LocalDate.now().atStartOfDay(), 1439);
        setColor();
        setPriorite(2);
    }

    public Indisponibilite(Evenement evenementIHM, InformateurObjet informateur) {
        super(evenementIHM.getNomVariables(), informateur);

        setDebut_Fin(evenementIHM.getDebut(), evenementIHM.getFin());
        setColor();
        setPriorite(2);
    }

    private void setColor() {
        setCouleurCadre(Color.GRAY);
        setCouleurFond(Color.GRAY);
        setCouleurTexte(new Color(0.827451f * 255,
                0.827451f * 255,
                0.827451f * 255));
    }

    /**
     * Copie toutes les données de l'hashMap dans un nouveau trou
     * ID compris !
     *
     * @return la copie de l'objet actuel
     */
    @Override
    public Evenement getCopyOf() {
        Indisponibilite nouveau = new Indisponibilite(getType(),
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
        return nouveau;
    }

    @Override
    public String getDescription() {
        return this + "\n" + super.getDescription();
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

