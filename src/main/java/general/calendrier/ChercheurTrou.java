package general.calendrier;


import general.InformateurObjet;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

public class ChercheurTrou {

    private final String NOM_TYPE, NOM_ID, NOM_DESCRIPTION, NOM_COULEUR, NOM_IDINTERMEDIAIRE,
            NOM_DATEDU, NOM_DATEAU, NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU;
    private InformateurObjet informateurObjet;

    public ChercheurTrou(String nomType,
                         String nomID,
                         String nomDescription,
                         String nomCouleurTexte,
                         String nomIDProprietaire,
                         String nomDateDebut,
                         String nomDateFin,
                         String nomCouleurCadre,
                         String nomCouleurFond,
                         String nomLieu, InformateurObjet informateur) {
        NOM_TYPE = nomType;
        NOM_ID = nomID;
        NOM_DESCRIPTION = nomDescription;
        NOM_COULEUR = nomCouleurTexte;
        NOM_IDINTERMEDIAIRE = nomIDProprietaire;
        NOM_DATEDU = nomDateDebut;
        NOM_DATEAU = nomDateFin;
        NOM_COULEUR_CADRE = nomCouleurCadre;
        NOM_COULEUR_FOND = nomCouleurFond;
        NOM_LIEU = nomLieu;
        informateurObjet = informateur;
    }

    public ChercheurTrou(ArrayList<String> listeNoms, InformateurObjet informateur) {
        NOM_TYPE = listeNoms.isEmpty() ? "type" : listeNoms.get(0);
        int taille = listeNoms.size();
        NOM_ID = taille > 1 ? listeNoms.get(1) : "id";
        NOM_DESCRIPTION = taille > 2 ? listeNoms.get(2) : "description";
        ;
        NOM_COULEUR = taille > 3 ? listeNoms.get(3) : "couleurTexte";
        NOM_IDINTERMEDIAIRE = taille > 4 ? listeNoms.get(4) : "idProprietaire";
        NOM_DATEDU = taille > 5 ? listeNoms.get(5) : "dateDu";
        NOM_DATEAU = taille > 6 ? listeNoms.get(6) : "dateAu";
        NOM_COULEUR_CADRE = taille > 7 ? listeNoms.get(7) : "couleurCadre";
        NOM_COULEUR_FOND = taille > 8 ? listeNoms.get(8) : "couleurFond";
        NOM_LIEU = taille > 9 ? listeNoms.get(9) : "lieu";
        informateurObjet = informateur;
    }

    /**
     * Permet de trouver le trou le plus proche pour un événement, dans une
     * liste d'événements
     *
     * @param appointment l'événement
     * @param liste       la liste d'événements
     * @return Le trou le plus pertinent
     */
    public Trou donne_moi_trou(ArrayList<? extends Evenement> liste, Evenement appointment) {
        Trou trou;

        // établir la liste des trous disponibles
        LocalDateTime debutVide = appointment.getDebut().toLocalDate().atStartOfDay();
        LocalDateTime finVide = debutVide.plusMinutes(2);
        TreeSet<Trou> listeDeTrous = new TreeSet<>();
        if (liste.isEmpty()) {
            Trou trou1 = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                    NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                    NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
            trou1.setDebut_Duree(appointment.getDebut().toLocalDate().atStartOfDay(),
                    1440);
            listeDeTrous.add(trou1);
        } else {
            for (int i = 0; i < liste.size(); i++) {
                if (liste.get(i).getDebut().isAfter(finVide)) {
                    Trou x = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                            NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                            NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
                    x.setDebut_Duree(debutVide,
                            (int) ChronoUnit.MINUTES.between(debutVide, liste.get(i).getDebut()));
                    listeDeTrous.add(x);
                }
                debutVide = liste.get(i).getFin();
                if (i < liste.size() - 1) {
                    finVide = debutVide.plusMinutes(2);
                } else {
                    finVide = debutVide.toLocalDate().atTime(23, 59);
                    Trou x = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                            NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                            NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
                    x.setDebut_Duree(debutVide, (int) ChronoUnit.MINUTES.between(debutVide, finVide));
                    listeDeTrous.add(x);
                }
            }
        }

        // chercher et renvoyer le meilleur trou
        trou = chercher_meilleur_trou(appointment, listeDeTrous);

        return trou;
    }

    /**
     * cherche une liste de trous dans une liste d'événements. On commence à
     * minuit le premier jour et on termine à 23h59 le dernier jour
     * Et s'il y a des jours entre les événements, des trous de 0h00 à 23h59
     * seront créés les jours absents
     * Donc attention si les événements sont dans des années différentes ! ça peut
     * générer beaucoup de trous !
     *
     * @param evenements c'est la liste de départ
     * @return une liste triée de trous; null si la liste de départ est vide
     */
    public TreeSet<Trou> chercher_liste_trous(Collection<? extends Evenement> evenements) throws Exception {
        if (evenements.isEmpty()) {
            throw new UnsupportedOperationException("*** Impossible de fournir une liste de trous à partir" +
                    " d'une liste d'événements vide !");
        }
        TreeSet<Evenement> listeTriee = new TreeSet<>(evenements);
        TreeSet<Trou> trous = new TreeSet<>();

        // parcours de la liste à la recherche de trous
        LocalDateTime debut = null;
        for (Evenement evenementActuel : listeTriee) {
            boolean memeJour = debut != null &&
                    evenementActuel.getDebut().toLocalDate().equals(debut.toLocalDate());
            if (memeJour) {
                // la première fois, debut==null
                if (debut.isBefore(evenementActuel.getDebut().minusMinutes(1))) {
                    Trou trouX = getNewTrou(debut, evenementActuel.getDebut());
                    trous.add(trouX);
                }
            } else {
                // on a changé de jour
                if (debut == null) {
                    if (evenementActuel.getDebut().toLocalTime().isAfter(LocalTime.of(0, 1))) {
                        Trou trouX = getNewTrou(evenementActuel.getDebut().toLocalDate().atStartOfDay(),
                                evenementActuel.getDebut());
                        trous.add(trouX);
                    }
                } else {
                    // si on n'est pas le même jour, on est forcément un jour ultérieur car TreeSet !
                    Trou trouX = getNewTrou(debut, debut.toLocalDate().atTime(23, 59, 59));
                    trous.add(trouX);
                    // boucher avec des trous sur une journée complète
                    debut = debut.plusDays(1).toLocalDate().atStartOfDay();
                    TreeSet<Trou> trousAdditionnels = remplir_jours_vides(debut, evenementActuel);
                    trous.addAll(trousAdditionnels);
                    debut = debut.plusDays(trousAdditionnels.size());

                    boolean condition = !debut.toLocalDate().equals(evenementActuel.getDebut().toLocalDate());
                    afficher_si_erreur(condition,
                            "*** ChercheurTrou-chercher_liste_trous : le nombre de trous avant " +
                                    evenementActuel.getDebut().toLocalDate()
                                    + " ne sera pas bon. Didier : il faut checker !" +
                                    "\n-> debut = " + debut.toLocalDate() +
                                    "\n-> jour actuel supposé = " + evenementActuel.getDebut().toLocalDate());
                    if (evenementActuel.getDebut().isAfter(debut.plusMinutes(1))) {
                        Trou trouY = getNewTrou(debut, evenementActuel.getDebut());
                        trous.add(trouY);
                    }
                }
            }
            debut = evenementActuel.getFin();
        }
        // Dernier trou ?
        if (listeTriee.last().getFin().toLocalTime().isBefore(LocalTime.of(23, 59))) {
            Trou trouX = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                    NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                    NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
            trouX.setDebut_Fin(listeTriee.last().getFin(),
                    listeTriee.last().getFin().toLocalDate().atTime(23, 59, 59));
            trous.add(trouX);
        }

        return trous;
    }

    private TreeSet<Trou> remplir_jours_vides(LocalDateTime debut, Evenement evenementActuel) {
        TreeSet<Trou> trous = new TreeSet<>();
        while (!debut.toLocalDate().equals(evenementActuel.getDebut().toLocalDate())) {
            afficher_si_erreur(debut.toLocalDate().isAfter(evenementActuel.getDebut().toLocalDate()),
                    "le début :" + debut + " doit être avant l'événement actuel : " + evenementActuel);
            Trou trouI = getNewTrou(debut.toLocalDate().atStartOfDay(), 1439);
            trous.add(trouI);
            debut = debut.plusDays(1);
        }
        return trous;
    }

    private void afficher_si_erreur(boolean condition, String s) {
        if (condition) {
            throw new RuntimeException(s);
        }
    }

    private Trou getNewTrou(LocalDateTime debut, LocalDateTime fin) {
        Trou trouX = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
        trouX.setDebut_Fin(debut, fin);
        return trouX;
    }

    private Trou getNewTrou(LocalDateTime debut, int duree) {
        Trou trouX = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
        trouX.setDebut_Duree(debut, duree);
        return trouX;
    }

    public Trou chercher_meilleur_trou(Evenement evenementMobile, TreeSet<Trou> listeDeTrous) {
        long duree = evenementMobile.getDuree();
        Trou trouPrecedent = null;
        for (Trou t : listeDeTrous) {
            if (t.getDuree() >= 15) {
                // si l'événement est plus haut que le trou, insérer
                // si l'événement est décalé vers le haut, insérer
                // si l'événement est compris dans le trou, insérer
                if (evenementMobile.getFin().isBefore(t.getFin())) {
                    return inserer_elt_dans_trou(evenementMobile, t);
                }

                // si l'événement est décalé vers le bas, on le glisse vers le haut
                if (evenementMobile.getDebut().isAfter(t.getDebut())
                        && evenementMobile.getDebut().isBefore(t.getFin())) {
                    Trou trouX = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                            NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                            NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
                    trouX.setDebut_Fin(evenementMobile.getDebut(), t.getFin());
                    return trouX;
                }

                // si l'événement est plus bas que le trou, passer au suivant
            }
            trouPrecedent = t;
        }

        // si on arrive ici, alors on n'a pas su insérer
        // on retourne le trou précédent
        if (trouPrecedent != null) {
            if (trouPrecedent.getDuree() > duree) {
                Trou trouX = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                        NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                        NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
                trouX.setDebut_Fin(trouPrecedent.getDebut(), trouPrecedent.getDebut().plusMinutes(duree));
                return trouX;
            }
            return trouPrecedent;
        }

        return null;
    }

    private Trou inserer_elt_dans_trou(Evenement appointment, Trou trou) {
        int duree = (int) ChronoUnit.MINUTES.between(appointment.getDebut(),
                appointment.getFin());

        // par défaut, on prend les limites du trou
        Trou retour = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
        retour.setDebut_Fin(trou.getDebut(), trou.getFin());

        // si début événement avant trou, coller vers le haut
        if (appointment.getDebut().isBefore(trou.getDebut())) {
            if (duree < trou.getDuree()) {
                retour.setDebut_Duree(retour.getDebut(), duree);
            }
        } else if (appointment.getFin().isBefore(trou.getFin())) {
            retour = new Trou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                    NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                    NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);
            retour.setDebut_Fin(appointment.getDebut(), appointment.getFin());
        } else {
            if (duree < trou.getDuree()) {
                retour.setDebut_Duree(trou.getFin().minusMinutes(duree), duree);
            }
        }

        return retour;
    }
}
