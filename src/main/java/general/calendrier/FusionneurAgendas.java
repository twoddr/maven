/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package general.calendrier;


import general.InformateurObjet;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * @author didie
 */
public class FusionneurAgendas {


    private final InformateurObjet informateurObjet;
    protected TreeSet<Evenement> sauves = new TreeSet<>(),
            bannis = new TreeSet<>(),
            tronques = new TreeSet<>(),
            crees = new TreeSet<>();
    private String NOM_TYPE, NOM_ID, NOM_DESCRIPTION, NOM_COULEUR, NOM_IDINTERMEDIAIRE,
            NOM_DATEDU, NOM_DATEAU, NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU;

    /**
     * Objet permettant le fusionnage d'éléments dans un agenda
     *
     * @param nomType           le nom de la variable type
     * @param nomID             le nom de l'id
     * @param nomDescription    la description d'un événement
     * @param nomCouleurTexte   le nom de la variable couleur
     * @param nomIDProprietaire le nom de la variable idProprio
     * @param nomDateDebut      le nom de la date de début d'un événement
     * @param nomDateFin        le nom de la date de fin
     * @param nomCouleurCadre   le nom de la couleur du cadre d'agenda
     * @param nomCouleurFond    le nom de la couleur de fond de l'agenda
     * @param nomLieu           le nom de la variable de lieu
     */
    public FusionneurAgendas(String nomType,
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

    /**
     * Constructeur avec la liste des noms des champs dans l'ordre suivant :
     * nomType, nomID, nomDescription, nomCouleurTexte,
     * nomIDProprietaire, nomDateDebut, nomDateFin, nomCouleurCadre,
     * nomCouleurFond, nomLieu
     *
     * @param listeNoms
     */
    public FusionneurAgendas(ArrayList<String> listeNoms,
                             InformateurObjet informateur) {
        if (!listeNoms.isEmpty()) {
            NOM_TYPE = listeNoms.get(0);
        }
        if (listeNoms.size() > 1) {
            NOM_ID = listeNoms.get(1);
        }
        if (listeNoms.size() > 2) {
            NOM_DESCRIPTION = listeNoms.get(2);
        }
        if (listeNoms.size() > 3) {
            NOM_COULEUR = listeNoms.get(3);
        }
        if (listeNoms.size() > 4) {
            NOM_IDINTERMEDIAIRE = listeNoms.get(4);
        }
        if (listeNoms.size() > 5) {
            NOM_DATEDU = listeNoms.get(5);
        }
        if (listeNoms.size() > 6) {
            NOM_DATEAU = listeNoms.get(6);
        }
        if (listeNoms.size() > 7) {
            NOM_COULEUR_CADRE = listeNoms.get(7);
        }
        if (listeNoms.size() > 8) {
            NOM_COULEUR_FOND = listeNoms.get(8);
        }
        if (listeNoms.size() > 9) {
            NOM_LIEU = listeNoms.get(9);
        }
        informateurObjet = informateur;
    }

    /**
     * Permet de fusionner plusieurs événements en évitant les chevauchements
     *
     * @param etrangers  les nouveaux événements
     * @param evenements les événements initiaux
     * @param priorite   la priorité de fusion
     * @return le résultat de la fusion
     */
    public TreeSet<Evenement> fusionner(
            TreeSet<Evenement> etrangers,
            TreeSet<Evenement> evenements,
            String priorite) {
        vider_listes();
        TreeSet<Evenement> listeDynamique = new TreeSet<>(evenements);
        for (Evenement elt : etrangers) {
            switch (priorite) {
                case "nonPrio":
                    inserer_non_prioritaire(elt, listeDynamique);
                    break;
                case "prio":
                    inserer_prioritaire(elt, listeDynamique);
                    break;
                case "semiPrio":
                    inserer_malin(elt, listeDynamique);
                default:
                    System.err.println("FusionneurAgendas/fusionner : Pas moyen de trouver la priorité" +
                            " d'insertion d'un événement (" + elt + ") , donc j'applique l'insertion comme non-prioritaire !");
                    inserer_non_prioritaire(elt, listeDynamique);
                    break;
            }
            listeDynamique = new TreeSet<>();
            listeDynamique.addAll(getCrees());
            listeDynamique.addAll(getTronques());
            listeDynamique.addAll(getSauves());
        }

        return listeDynamique;
    }

    /**
     * Permet de fusionner plusieurs événements en évitant les chevauchements
     *
     * @param etranger   le nouvel événement
     * @param evenements les événements initiaux
     * @param priorite   la priorité de fusion
     * @return le résultat de la fusion
     */
    public TreeSet<Evenement> fusionner(
            Evenement etranger,
            TreeSet<Evenement> evenements,
            String priorite) {
        vider_listes();
        TreeSet<Evenement> listeResultat = new TreeSet<>(evenements);
        switch (priorite) {
            case "nonPrio":
                inserer_non_prioritaire(etranger, listeResultat);
                break;
            case "prio":
                inserer_prioritaire(etranger, listeResultat);
                break;
            case "semiPrio":
                inserer_malin(etranger, listeResultat);
            default:
                System.err.println("FusionneurAgendas/fusionner : Pas moyen de trouver la priorité" +
                        " d'insertion d'un événement (" + etranger + ") , donc j'applique l'insertion comme non-prioritaire !");
                inserer_non_prioritaire(etranger, listeResultat);
                break;
        }
        listeResultat = new TreeSet<>();
        listeResultat.addAll(getCrees());
        listeResultat.addAll(getTronques());
        listeResultat.addAll(getSauves());

        return listeResultat;
    }

    /**
     * Permet d'insérer un événement qui n'a pas la priorité sur les autres, et
     * ce sans autoriser les chevauchements
     *
     * @param evenement  c'est l'événement intrus
     * @param evenements c'est la liste des événements initiaux
     */
    public void inserer_non_prioritaire(Evenement evenement, TreeSet<Evenement> evenements) {
        vider_listes();

        while (evenement.getDuree() >= 1) {
            if (evenements.isEmpty()) {
                //System.out.println("* Insertion non-prioritaire : 1er elt ajouté : " + evenement.toExport());
                crees.add(evenement);
                return;
            }
            Evenement premier = evenements.first();
            //System.out.println("* Télescopage de " + evenement.toExport() + " avec " + premier.toExport() + " : ");
            FusionneurEvenements telescopeur = new FusionneurEvenements();
            // premier est prioritaire sur l'événement
            TreeSet<Evenement> resultatTelescopage = telescopeur.telescoper(premier, evenement);

            switch (resultatTelescopage.size()) {
                case 0: // pas de collision !
                    //System.out.println("=> esquive !");
                    sauves.add(premier);
                    evenements.remove(premier);
                    break;
                case 1:
                    //System.out.println("=> événement avalé !");
                    sauves.addAll(evenements);
                    bannis.add(evenement);
                    return;
                case 2:
                    if (telescopeur.nonPrioritaireDroite == null) {
                        evenement.setDebut_Fin(evenement.getDebut(), premier.getDebut());
                        //System.out.println("=> événement coupé par le bas => " + evenement.toExport());
                        if (evenement.getDuree() >= 1) {
                            crees.add(evenement);
                        }
                        sauves.addAll(evenements);
                        return;
                    }
                    evenement.setDebut_Fin(premier.getFin(), evenement.getFin());
                    //System.out.println("=> événement coupé par le haut => " + evenement.toExport());
                    sauves.add(premier);
                    evenements.remove(premier);
                    break;
                case 3:
                    //System.out.println(" => événement se réduit à 2 moceaux : ");
                    Evenement clone = evenement.getCopyOf();
                    clone.setId(0);
                    clone.setDebut_Fin(clone.getDebut(), premier.getDebut());
                    //System.out.println(" => supérieur => " + clone.toExport());
                    if (clone.getDuree() >= 1) {
                        crees.add(clone);
                    }
                    evenement.setDebut_Fin(premier.getFin(), evenement.getFin());
                    //System.out.println(" => Inférieur => " + evenement.toExport());
                    sauves.add(premier);
                    evenements.remove(premier);
                    break;
                default:
                    System.err.println("Oups ! Le fusionneur non prioritaire d'événements produit une liste d'événements "
                            + "de taille égale à " + resultatTelescopage.size() + " ! Ceci est un problème !");
                    return;
            }

        }
        sauves.addAll(evenements);
    }

    /**
     * Permet d'insérer un événement qui a la priorité sur les autres, et ce
     * sans autoriser les chevauchements
     *
     * @param evenement  c'est l'événement intrus qui peut écraser les autres ;il
     *                   ne sera pas modifié !
     * @param evenements c'est la liste des événements initiaux
     */
    public void inserer_prioritaire(Evenement evenement, TreeSet<Evenement> evenements) {
        if (evenement.getDuree() > 1) {
            vider_listes();
            while (!evenements.isEmpty()) {
                FusionneurEvenements telescopeur = new FusionneurEvenements();
                Evenement premier = evenements.first();
                TreeSet<Evenement> resultatTelescopage = telescopeur.telescoper(evenement, premier);

                boolean collision = !resultatTelescopage.isEmpty();
                boolean unSeulSurvivant = resultatTelescopage.size() == 1;
                boolean deuxElements = resultatTelescopage.size() == 2;
                boolean troisElements = resultatTelescopage.size() == 3;

                if (!collision) {
                    sauves.add(premier);
                } else if (unSeulSurvivant) {
                    bannis.add(premier);
                } else if (deuxElements) {
                    Evenement nonprio = telescopeur.getNonPrioritaireDroite() == null
                            ? telescopeur.getNonPrioritaireGauche()
                            : telescopeur.getNonPrioritaireDroite();
                    nonprio.setModifie(true);
                    if (nonprio.getDuree() >= 5) {
                        tronques.add(nonprio);
                    } else {
                        bannis.add(nonprio);
                    }
                } else if (troisElements) {
                    Evenement nonPrioGauche = telescopeur.getNonPrioritaireGauche();
                    if (nonPrioGauche.getDuree() >= 5) {
                        crees.add(nonPrioGauche);
                    }
                    premier.setDebut_Fin(telescopeur.getNonPrioritaireDroite().getDebut(),
                            premier.getFin());
                    premier.setModifie(true);
                    if (premier.getDuree() >= 5) {
                        tronques.add(premier);
                    } else {
                        bannis.add(premier);
                    }
                } else {
                    System.err.println("Oups ! Le fusionneur prioritaire d'événements produit une liste d'événements "
                            + "de taille égale à " + resultatTelescopage.size() + " ! Ceci est un problème !");
                    return;
                }
                evenements.remove(premier);
            }
            crees.add(evenement);
        } else {
            sauves.addAll(evenements);
        }
    }

    /**
     * Permet d'insérer un événement selon la priorité, et ce
     * sans autoriser les chevauchements
     *
     * @param evenement  c'est l'événement intrus qui veut s'insérer avec les autres ;il
     *                   peut être modifié !
     * @param evenements c'est la liste des événements initiaux
     */
    public void inserer(Evenement evenement, TreeSet<Evenement> evenements) {
        if (evenement.getDuree() > 1) {
            vider_listes();
            while (!evenements.isEmpty()) {
                FusionneurEvenements telescopeur = new FusionneurEvenements();
                Evenement premier = evenements.first();
                TreeSet<Evenement> resultatTelescopage = evenement.getPriorite() >= premier.getPriorite() ?
                        telescopeur.telescoper(evenement, premier) :
                        telescopeur.telescoper(premier, evenement);

                boolean collision = !resultatTelescopage.isEmpty();
                boolean unSeulSurvivant = resultatTelescopage.size() == 1;
                boolean deuxElements = resultatTelescopage.size() == 2;
                boolean troisElements = resultatTelescopage.size() == 3;

                if (!collision) {
                    sauves.add(premier);
                } else if (unSeulSurvivant) {
                    bannis.add(premier);
                } else if (deuxElements) {
                    Evenement nonprio = telescopeur.getNonPrioritaireDroite() == null
                            ? telescopeur.getNonPrioritaireGauche()
                            : telescopeur.getNonPrioritaireDroite();
                    nonprio.setModifie(true);
                    if (nonprio.getDuree() >= 5) {
                        tronques.add(nonprio);
                    } else {
                        bannis.add(nonprio);
                    }
                } else if (troisElements) {
                    Evenement nonPrioGauche = telescopeur.getNonPrioritaireGauche();
                    if (nonPrioGauche.getDuree() >= 5) {
                        crees.add(nonPrioGauche);
                    }
                    premier.setDebut_Fin(telescopeur.getNonPrioritaireDroite().getDebut(),
                            premier.getFin());
                    premier.setModifie(true);
                    if (premier.getDuree() >= 5) {
                        tronques.add(premier);
                    } else {
                        bannis.add(premier);
                    }
                } else {
                    System.err.println("Oups ! Le fusionneur prioritaire d'événements produit une liste d'événements "
                            + "de taille égale à " + resultatTelescopage.size() + " ! Ceci est un problème !");
                    return;
                }
                evenements.remove(premier);
            }
            crees.add(evenement);
        } else {
            sauves.addAll(evenements);
        }
    }

    private void vider_listes() {
        sauves = new TreeSet<>();
        crees = new TreeSet<>();
        bannis = new TreeSet<>();
        tronques = new TreeSet<>();
    }

    public TreeSet<Evenement> getSauves() {
        return sauves;
    }

    public TreeSet<Evenement> getBannis() {
        return bannis;
    }

    public TreeSet<Evenement> getTronques() {
        return tronques;
    }

    public TreeSet<Evenement> getCrees() {
        return crees;
    }

    public boolean isTelescopage() {
        return !tronques.isEmpty() || !bannis.isEmpty();
    }

    public void inserer_malin(Evenement evenement, TreeSet<Evenement> evenementTreeSet) {
        vider_listes();
        if (evenementTreeSet.isEmpty()) {
            crees.add(evenement);
            return;
        }
        ChercheurTrou chercheurTrou = new ChercheurTrou(NOM_TYPE, NOM_ID, NOM_DESCRIPTION,
                NOM_COULEUR, NOM_IDINTERMEDIAIRE, NOM_DATEDU, NOM_DATEAU,
                NOM_COULEUR_CADRE, NOM_COULEUR_FOND, NOM_LIEU, informateurObjet);

        try {
            TreeSet<Trou> listeTrous = chercheurTrou.chercher_liste_trous(evenementTreeSet);
            Trou meilleurTrou = chercheurTrou.chercher_meilleur_trou(evenement, listeTrous);
            evenement.setDebut_Fin(meilleurTrou.getDebut(), meilleurTrou.getFin());
            if (evenement.getDuree() > 1) {
                crees.add(evenement);
            } else {
                bannis.add(evenement);
            }
            sauves.addAll(evenementTreeSet);
        } catch (Exception e) {
            System.err.println("*** Ceci ne devrait pas se produire ! FusionneurAgendas/insererMalin tente d'insérer" +
                    " " + evenement + " dans une liste non vide mais pour laquelle la liste des trous est NULL ! => " +
                    evenementTreeSet);
        }
    }
}
