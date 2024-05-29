package general.calendrier;

import general.ExtracteurHashMap;
import general.InformateurObjet;
import general.ObjetDi;
import general.RGBConverter;

import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class Evenement extends ObjetDi {

    protected String COULEUR_CADRE, COULEUR_FOND, COULEUR_TEXTE, NOM_LIEU, EXTERNAL_DUREE,
            nomDEBUT, nomFIN, nomIDPROPRIETAIRE;
    protected boolean verrouille = false,
            modifie = false,
            isDebutSet = false,
            isFinSet = false;
    protected int evenementLie = super.hashCode(), priorite = 1;
    private Integer dureeTmp = null;
    private InformateurObjet informateurObjet;

    /**
     * Permet de créer un événement d'agenda
     *
     * @param nomTable          nom de la variable qui contient le type : Evenement, rdv, etc
     * @param nomID             nom de la variable qui contient l'id
     * @param nomDescription    nom de la variable qui contient la description
     * @param nomCouleurTexte   nom de la variable qui contient la couleur du texte
     * @param nomIDProprietaire nom de la variable qui contient l'id du propriétaire
     * @param nomDateDebut      nom de la variable qui contient la date du début
     * @param nomDateFin        nom de la variable qui la date de fin
     * @param nomLieu           nom de la variable qui contient le lieu de l'evt
     */
    public Evenement(String nomTable,
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
        super(nomTable.isEmpty() ? "Evenement" : nomTable, nomID, nomDescription);
        informateurObjet = informateur;
        COULEUR_TEXTE = (nomCouleurTexte.isEmpty() || COULEUR_TEXTE == null) ?
                "couleurTexte" :
                nomCouleurTexte;
        COULEUR_CADRE = nomCouleurCadre.isEmpty() ? "couleurCadre" : nomCouleurCadre;
        COULEUR_FOND = nomCouleurFond.isEmpty() ? "couleurFond" : nomCouleurFond;
        NOM_LIEU = nomLieu.isEmpty() ? "lieu" : nomLieu;
        nomDEBUT = nomDateDebut;
        nomFIN = nomDateFin;

        reinitialiser();

    }

    /**
     * Permet de créer un événement d'agenda
     * Les informations nécessaires permettent d'identifier, dans l'hashmap de données,
     * * quelles sont celles qu'on observe généralement dans une interface graphique
     * * L'id et l'id intermédiaire sont utiles lors des sélections et échanges de liste...
     * *
     *
     * @param arrayList c'est la liste des noms de variables
     *                  d'après l'ordre ci-dessous :
     *                  * nomType le nom de la classe de l'objet représenté
     *                  * nomID              le nom de la variable qui stocke l'id
     *                  * nomObjet           le nom du nom de la variable nom
     *                  *  nomCouleur le nom de la variable qui contient la couleur
     *                  * nomIDIntermediaire le nom de l'id intermédiaire
     *                  * nomDateDu          le nom de la variable dateDu
     *                  *  nomDateAu          le nom de la variable dateAu
     *                  * couleur cadre
     *                  * couleur fond
     *                  * lieu
     */
    public Evenement(ArrayList<String> arrayList, InformateurObjet informateur) {
        super(arrayList);
        informateurObjet = informateur;
        int taille = arrayList.size();

        COULEUR_TEXTE = taille < 4 ? "couleurTexte" : arrayList.get(3);
        nomDEBUT = taille < 6 ? "dateDu" : arrayList.get(5);
        nomFIN = taille < 7 ? "dateAu" : arrayList.get(6);
        COULEUR_CADRE = taille < 8 ? "couleurCadre" : arrayList.get(7);
        COULEUR_FOND = taille < 9 ? "couleurFond" : arrayList.get(8);
        NOM_LIEU = taille < 10 ? "lieu" : arrayList.get(9);

        setPublicName("Événements");
        setType("Evenement");

    }

  /*  @Override
    public void setIntermediaireData(HashMap<String, Object> data) {
        System.err.println("** Evenement : Appel de la méthode setIntermédaire ! Normalement, c'est à " +
                "un des objets étendus qui doit l'implémenter => " + getType());
    }*/

    public InformateurObjet getInformateurHashObjet() {
        return informateurObjet;
    }

    public void setInformateurHashObjet(InformateurObjet informateurObjet) {
        this.informateurObjet = informateurObjet;
    }

    public void reinitialiser() {
        initialiser(COULEUR_TEXTE, RGBConverter.convert_color_2_int(Color.BLUE));
        initialiser(COULEUR_CADRE, RGBConverter.convert_color_2_int(Color.GREEN));
        initialiser(COULEUR_FOND, RGBConverter.convert_color_2_int(Color.YELLOW));

        initialiser(NOM_LIEU, "Liège");

        setPublicName("Événements");
        setType("Evenement");

    }

    public int getPriorite() {
        return priorite;
    }

    public void setPriorite(int priorite) {
        this.priorite = priorite;
    }

    public ArrayList<String> getNomVariables() {
        ArrayList<String> retour = new ArrayList<>();
        retour.add(nomID);
        retour.add(nomNOM);
        retour.add(COULEUR_TEXTE);
        retour.add(nomDEBUT);
        retour.add(nomFIN);
        retour.add(COULEUR_CADRE);
        retour.add(COULEUR_FOND);
        retour.add(NOM_LIEU);

        return retour;
    }

    /**
     * Permet de rentrer le nom de la variable qui devra piloter la durée
     *
     * @param external_duree
     */
    public void setEXTERNAL_DUREE(String external_duree) {
        EXTERNAL_DUREE = external_duree;
    }


    public int getEvenementLie() {
        return evenementLie;
    }

    public void setEvenementLie(int evenementLie) {
        this.evenementLie = evenementLie;
    }

    public boolean isVerrouille() {
        return verrouille;
    }

    public void setVerrouille(boolean verrouille) {
        this.verrouille = verrouille;
    }


    public String getDescription() {
        return getNom();
    }

    public void setDescription(String description) {
        setNom(description);
    }

    public String getLieu() {
        return ExtracteurHashMap.extraire_string(get(NOM_LIEU));
    }

    public void setLieu(String description) {
        inserer_valeur(NOM_LIEU, description);
    }

    public LocalDateTime getDebut() {
        return ExtracteurHashMap.extraire_dateHeure(get(nomDEBUT));
    }

    /**
     * Modifie l'heure de début de l'événement Il est important que
     * le début précède chronologiquement la fin sinon la requête n'est pas
     * exécutée du tout!
     *
     * @param debut la nouvelle dateHeure de début
     */
    public void setDebut(LocalDateTime debut) {
        if (isFinSet) {
            LocalDateTime fin = getFin();
            if (fin.isBefore(debut)) {
                String texte = "Attention setDebut/EvenementIHM " + getClass().getSimpleName() + " " + getDescription()
                        + " reçoit une date de début (" + debut + ") après la date "
                        + "de fin (" + fin + ". Demande ignorée ! => " + this;
                System.err.println(texte);
                return;
            }
        }
        super.inserer_valeur(nomDEBUT, debut);
        isDebutSet = true;
        if (dureeTmp != null) {
            super.inserer_valeur(nomFIN, debut.plusMinutes(dureeTmp));
            isFinSet = true;
            dureeTmp = null;
        }
    }

    /**
     * Modifie l'heure de début et la durée en minutes, de l'événement Il est
     * important que la durée soit positive sinon la requête n'est pas exécutée
     * du tout!
     *
     * @param debut   la nouvelle dateHeure de début
     * @param minutes la nouvelle durée en minutes
     */
    public void setDebut_Duree(LocalDateTime debut, int minutes) {
        isDebutSet = false;
        isFinSet = false;
        LocalDateTime fin = debut.plusMinutes(minutes);
        setDebut(debut);
        setFin(fin);
    }

    /**
     * Modifie l'heure de début et celle de fin, de l'événement Il est
     * important que la durée soit positive sinon seul le début est pris en compte !
     *
     * @param debut la nouvelle dateHeure de début
     * @param fin   la nouvelle date de fin
     */
    public void setDebut_Fin(LocalDateTime debut, LocalDateTime fin) {
        isDebutSet = false;
        isFinSet = false;
        setDebut(debut);
        setFin(fin);
    }

    public LocalDateTime getFin() {
        return ExtracteurHashMap.extraire_dateHeure(get(nomFIN));
    }

    /**
     * Modifie l'heure de fin de l'événement Il est important que
     * le début précède chronologiquement la fin sinon la requête n'est pas
     * exécutée du tout!
     *
     * @param fin la nouvelle dateHeure de début
     */
    public void setFin(LocalDateTime fin) {
        if (isDebutSet) {
            LocalDateTime debut = getDebut();
            if (fin.isBefore(debut)) {
                String texte = "Attention setDebut/EvenementIHM " + getClass().getSimpleName() + " " + getDescription()
                        + " reçoit une date de fin (" + fin + ") précédant la date "
                        + "de début (" + debut + ". Demande ignorée ! => " + this;
                System.err.println(texte);
                return;
            }
        }
        super.inserer_valeur(nomFIN, fin);
        isFinSet = true;
        if (dureeTmp != null) {
            super.inserer_valeur(nomDEBUT, fin.minusMinutes(dureeTmp));
            isDebutSet = true;
            dureeTmp = null;
        }
    }

    public int getIdProprietaire() {
        return ExtracteurHashMap.extraire_int(get(nomIDPROPRIETAIRE));
    }

    public void setIdProprietaire(int idProprietaire) {
        inserer_valeur(nomIDPROPRIETAIRE, idProprietaire);
    }

    /**
     * Permet de comparer deux événements par rapport à la date de début ! Si la
     * date de début est la même, on compare par rapport aux dates de fin Si
     * encore égalité, on compare le hash
     *
     * @param o Autre événement
     * @return -1 si ancien, 1 si ultérieur, 0 dans les autres cas
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof Evenement) {
            Evenement autreEvenement = (Evenement) o;
            int i = getDebut().compareTo(autreEvenement.getDebut());
            if (i == 0) {
                int j = getFin().compareTo(autreEvenement.getFin());
                if (j == 0) {
                    Integer monHash = hashCode();
                    Integer sonHash = autreEvenement.hashCode();
                    return monHash.compareTo(sonHash);
                }
                return j;
            }
            return i;
        }
        return 0;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof Evenement) {
            Evenement autre = (Evenement) o;
            boolean memeHoraire = getDebut().equals(autre.getDebut()) &&
                    getFin().equals(autre.getFin());
            boolean memeID = getId() == autre.getId();
            boolean memeDescription = getDescription().equals(autre.getDescription());
            boolean memeEtat = isVerrouille() == autre.isVerrouille();
            boolean memeProprio = getIdProprietaire() == autre.getIdProprietaire();

            return memeDescription && memeEtat && memeHoraire && memeID && memeProprio;
        }
        return false;
    }

    public String getStringDebut() {
        try {
            return getDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        } catch (Exception e) {
            return "null";
        }
    }

    public String getStringFin() {
        try {
            return getFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        } catch (Exception e) {
            return "null";
        }
    }

    public String getStringHeureDebut() {
        try {
            return getDebut().format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return "null";
        }
    }

    public String getStringHeureFin() {
        try {
            return getFin().format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return "null";
        }
    }

    /**
     * Retourne une nouvelle instance, possédant les mêmes caractéristiques, y
     * compris l'ID ! Par contre, la variable evenementLie n'est pas copiée ! La
     * variable modifie n'est pas copiée non plus, ni l'état de verrouillage !
     *
     * @return Un nouvel événement contenant les mêmes données que celui-ci
     */
    public Evenement getCopyOf() {
        Evenement appointment = new Evenement(getType(),
                nomID,
                nomNOM,
                COULEUR_TEXTE,
                nomIDPROPRIETAIRE,
                nomDEBUT,
                nomFIN,
                COULEUR_CADRE,
                COULEUR_FOND,
                NOM_LIEU, informateurObjet);
        appointment.setData(getData(), true);
        return appointment;
    }

    @Override
    public String toString() {
        //System.out.println("* toString de ElementsBD.Evenement = " + sortie);
        return getDescription() + " DU " + getStringDebut() + " AU " + getStringFin();
    }

    @Override
    public ObjetDi getObjetFromType(String nomType) {
        if (informateurObjet == null) {
            System.err.println("*** Evenement : Didier, il faut indiquer comment générer un HashObjet");
            return null;
        }
        return informateurObjet.getObjetFromType(nomType);
    }

    public Boolean isWholeDay() {
        return ExtracteurHashMap.extraire_booleen(get("allday"));
    }

    public Integer getDuree() {
        if (isFinSet && isDebutSet) {
            return Math.toIntExact(ChronoUnit.MINUTES.between(getDebut(), getFin()));
        }
        if (dureeTmp == null) {
            System.err.println("*** EvenementIHM/getDuree : Impossible de calculer la durée car " +
                    "il n'y a pas de début ou de fin ! Je renvoie NULL !");
            return null;
        }
        return dureeTmp;
    }

    /**
     * Modifie la durée en minutes, de l'événement Il est
     * important que la durée soit positive sinon la requête n'est pas exécutée
     * du tout !
     *
     * @param minutes la nouvelle durée en minutes
     */
    public void setDuree(int minutes) {
        if (minutes <= 0) {
            String texte = "Attention setDebut_duree. " + getClass().getSimpleName() + " " + getDescription()
                    + " reçoit une durée négative ou nulle. "
                    + "Demande ignorée ! => " + this;
            System.err.println(texte);
        }
        if (isDebutSet) {
            setDebut(getDebut().plusMinutes(minutes));
            return;
        }
        if (isFinSet) {
            setDebut(getFin().minusMinutes(minutes));
            return;
        }
        dureeTmp = minutes;
    }

    public String toExport() {
        return getDescription() + " (" + getId() + ") "
                + getDebut().format(DateTimeFormatter.ofPattern("dd/MM/yy-HH:mm"))
                + " -> " + getFin().format(DateTimeFormatter.ofPattern("dd/MM/yy-HH:mm"));
    }

    public String getCalendarString() {
        return getDescription() + "\nDE " + getStringHeureDebut() + " À " + getStringHeureFin();
    }

    @Override
    public void addModification(String nom) {
        if (nom.equals(EXTERNAL_DUREE)) {
            super.addModification(nomFIN);
            super.addModification(nomDEBUT);
            return;
        }
        super.addModification(nom);
    }

    @Override
    public Boolean inserer_valeur(String nom, Object donnee) {
        if (nom == null) {
            return false;
        }
        if (nom.equals(EXTERNAL_DUREE)) {
            int donneeInt = 0;
            if (donnee instanceof Integer) {
                donneeInt = (int) donnee;
            } else if (donnee instanceof Long) {
                donneeInt = ((Long) donnee).intValue();
            } else {
                System.err.println("*** EvenementIHM/put : bug probable : " +
                        "La durée externe (" + EXTERNAL_DUREE + ") ne peut gérer la valeur " +
                        donnee + " qui aurait dû être un long ou un int !\nDemande ignorée !");
                return false;
            }

            if (isDebutSet) {
                super.inserer_valeur(nomFIN, getDebut().plusMinutes(donneeInt));
                isFinSet = true;
//                System.out.println("===> je mets dans dateAu " + donneeInt +
//                        " minutes de plus que dateDu => " + getDateAu());
                return true;
            }
            if (isFinSet) {
                super.inserer_valeur(nomDEBUT, getFin().minusMinutes(donneeInt));
                isDebutSet = true;
//                System.out.println("===> je mets dans dateDu " + donneeInt +
//                        " minutes de moins que dateAu => " + getDateDu());
                return true;
            }
            dureeTmp = donneeInt;
            //           System.out.println("===> je mets  " + donneeInt +
            //                 " dans dureeTmp car il n'y a pas de début ni de fin ");
            return false;
        }
        if (nomDEBUT.equals(nom)) {
            LocalDateTime dateTime = null;
            if (donnee instanceof LocalDateTime) {
                dateTime = (LocalDateTime) donnee;
            } else if (donnee instanceof Timestamp) {
                dateTime = ((Timestamp) donnee).toLocalDateTime();
            } else if (donnee instanceof String) {
                try {
                    dateTime = LocalDateTime.parse(donnee + "");
                } catch (Exception e) {
                    System.err.println("** EvenementIHM/put : je refuse de mettre à jour la donnée " + nomDEBUT +
                            ", car le String ne peut être transformé en date ! J'ai un reçu : " +
                            donnee);
                    return false;
                }
            } else {
                System.err.println("** EvenementIHM/put : je refuse de mettre à jour la donnée " + nomDEBUT +
                        ", car j'attends un localDateTime, un String ou un TimeStamp et j'ai un " + donnee.getClass().getSimpleName());
                return false;
            }
            super.inserer_valeur(nomDEBUT, dateTime);
            isDebutSet = true;
            //           System.out.println("===> je mets dans dateDu " +
            //                   dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm")));
            if (dureeTmp != null) {
                super.inserer_valeur(nomFIN, dateTime.plusMinutes(dureeTmp));
                isFinSet = true;
                dureeTmp = null;
                //              System.out.println("===> je mets dans dateAu " + dureeTmp +
                //                      " minutes de plus que dateDu => " + getDateAu());
            }
        }
        if (nomFIN.equals(nom)) {
            LocalDateTime dateTime = null;
            if (donnee instanceof LocalDateTime) {
                dateTime = (LocalDateTime) donnee;
            } else if (donnee instanceof Timestamp) {
                dateTime = ((Timestamp) donnee).toLocalDateTime();
            } else if (donnee instanceof String) {
                try {
                    dateTime = LocalDateTime.parse(donnee + "");

                } catch (Exception e) {
                    System.err.println("** EvenementIHM/put : je refuse de mettre à jour la donnée " + nomFIN +
                            ", car le String ne peut être transformé en date ! J'ai un reçu : " +
                            donnee);
                    return false;
                }
            } else {
                System.err.println("** EvenementIHM/put : je refuse de mettre à jour la donnée " + nomFIN +
                        ", car j'attends un localDateTime et j'ai un " + donnee.getClass().getSimpleName());
                return false;
            }
            super.inserer_valeur(nomFIN, dateTime);
            isFinSet = true;
            //           System.out.println("===> je mets dans dateAu " +
            //                   dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm")));
            if (dureeTmp != null) {
                super.inserer_valeur(nomDEBUT, dateTime.minusMinutes(dureeTmp));
                isDebutSet = true;
                dureeTmp = null;
                //             System.out.println("===> je mets dans dateDu " + dureeTmp +
                //                     " minutes de moins que dateAu => " + getDateDu());
            }
            return true;
        }
        return super.inserer_valeur(nom, donnee);
    }

    public int getCouleurTexte() {
        return ExtracteurHashMap.extraire_int(get(COULEUR_TEXTE));
    }

    public void setCouleurTexte(int couleur) {
        inserer_valeur(COULEUR_TEXTE, couleur);
    }

    public void setCouleurTexte(Color couleur) {
        inserer_valeur(COULEUR_TEXTE, (new RGBConverter().convert_color_2_int(couleur)));
    }

    @Override
    public HashMap<String, Object> getData() {
        HashMap<String, Object> retour = super.getData();
        if (EXTERNAL_DUREE == null) {
            return retour;
        }
        retour.put(EXTERNAL_DUREE, getDuree());
        return retour;
    }

    public void setData(HashMap<String, Object> input, boolean toutPrendre) {
        isDebutSet = false;
        isFinSet = false;
        dureeTmp = null;
        super.setData(input, null, true, false);
    }

    @Override
    public HashMap<String, Object> getHashModifications(boolean x) {
        HashMap<String, Object> retour = super.getHashModifications(x);
        if (EXTERNAL_DUREE == null) {
            return retour;
        }
        if (retour.containsKey(nomDEBUT) || retour.containsKey(nomFIN)) {
            retour.put(EXTERNAL_DUREE, getDuree());
        }
        return retour;
    }

    public int getCouleurFond() {
        return ExtracteurHashMap.extraire_int(get(COULEUR_FOND));
    }

    public void setCouleurFond(int couleur) {
        inserer_valeur(COULEUR_FOND, couleur);
    }

    public void setCouleurFond(Color couleur) {
        inserer_valeur(COULEUR_FOND, (new RGBConverter().convert_color_2_int(couleur)));
    }

    public int getCouleurCadre() {
        return new ExtracteurHashMap().extraire_int(get(COULEUR_CADRE));
    }


    public void setCouleurCadre(Color couleur) {
        inserer_valeur(COULEUR_CADRE, new RGBConverter().convert_color_2_int(couleur));
    }

    public void setCouleurCadre(int couleur) {
        inserer_valeur(COULEUR_CADRE, couleur);
    }

    public void setModifie(boolean b) {
        if (b) {
            addModification(nomDEBUT);
            addModification(nomFIN);
            return;
        }
        clearModifications();
    }
}

