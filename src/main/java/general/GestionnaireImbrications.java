package general;

import java.util.ArrayList;
import java.util.HashMap;

public class GestionnaireImbrications {
    private final String nomTablePays, nomIdPaysDansVille, nomTableVille,
            nomIdVilleDansQuartier, nomTableQuartier;
    ObjetCSA quartier,
            ville,
            pays;
    private boolean verbose = false;
    private boolean change = false;

    /**
     * Permet de générer la matrice de localité (quartier, ville, pays) à partir de la liste levis et l'id quartier
     * Cet objet a été créé pour générer toutes les infos d'une localité en fonction de la
     * simple connaissance de l'id du quartier
     * Il a été généralisé à tous objets héritant de la même structure
     *
     * @param nomPays                nom de la table pays
     * @param nomIdPaysDansVille     nom de l'id pays dans la ville
     * @param nomTableVille          nom de la table ville
     * @param nomIdVilleDansQuartier nom de l'idville dans le quartier
     * @param nomTableQuartier       nom de la table quartier
     * @param fkquartier             l'id du quartier recherché
     * @param hashLevis              la source d'informations au format Levis
     * @param informateurObjetDi     C'est une Interface (locale) permettant de générer des hashobjets
     *                               à partir du nom de leur table
     */
    public GestionnaireImbrications(String nomPays, String nomIdPaysDansVille,
                                    String nomTableVille, String nomIdVilleDansQuartier,
                                    String nomTableQuartier, int fkquartier,
                                    HashMap<String, Object> hashLevis,
                                    InformateurObjet informateurObjetDi) {
        this.nomTablePays = nomPays;
        this.nomIdPaysDansVille = nomIdPaysDansVille;
        this.nomTableVille = nomTableVille;
        this.nomIdVilleDansQuartier = nomIdVilleDansQuartier;
        this.nomTableQuartier = nomTableQuartier;
        quartier = informateurObjetDi.getObjetFromType(nomTableQuartier);
        ville = informateurObjetDi.getObjetFromType(nomTableVille);
        pays = informateurObjetDi.getObjetFromType(nomPays);

    }

    /**
     * Permet de générer la matrice de localité (ville, pays) à partir de la liste levis et l'id quartier
     * Cet objet a été créé pour générer toutes les infos d'une localité en fonction de la
     * simple connaissance de l'id du quartier
     * Il a été généralisé à tous objets héritant de la même structure
     *
     * @param nomPays            nom de la table pays
     * @param nomIdPaysDansVille nom de l'id pays dans la ville
     * @param nomTableVille      nom de la table ville
     * @param fkville            l'id de la ville recherchée
     * @param hashLevis          la source d'informations au format Levis
     * @param informateurObjetDi C'est une Interface (locale) permettant de générer des hashobjets
     *                           à partir du nom de leur table
     */
    public GestionnaireImbrications(String nomPays, String nomIdPaysDansVille,
                                    String nomTableVille, int fkville,
                                    HashMap<String, Object> hashLevis,
                                    InformateurObjet informateurObjetDi) {
        this.nomTablePays = nomPays;
        this.nomIdPaysDansVille = nomIdPaysDansVille;
        this.nomTableVille = nomTableVille;
        this.nomIdVilleDansQuartier = null;
        this.nomTableQuartier = null;
        quartier = null;
        ville = informateurObjetDi.getObjetFromType(nomTableVille);
        pays = informateurObjetDi.getObjetFromType(nomPays);

    }
    /*

     */

    /**
     * Permet de générer la matrice de localité (quartier, ville, pays) à partir de la liste levis
     * * Il faut utiliser la méthode "remplir_donnees" pour l'alimenter
     * Cet objet a été créé pour générer toutes les infos d'une localité en fonction de la
     * simple connaissance de l'id du quartier
     * Il a été généralisé à tous objets héritant de la même structure
     *
     * @param nomPays                nom de la table pays
     * @param nomIdPaysDansVille     nom de l'id pays dans la ville
     * @param nomTableVille          nom de la table ville
     * @param nomIdVilleDansQuartier nom de l'idville dans le quartier
     * @param nomTableQuartier       nom de la table quartier
     * @param informateurObjetDi     C'est une Interface (locale) permettant de générer des hashobjets
     *                               à partir du nom de leur table
     */

    public GestionnaireImbrications(String nomPays, String nomIdPaysDansVille,
                                    String nomTableVille, String nomIdVilleDansQuartier,
                                    String nomTableQuartier,
                                    InformateurObjet informateurObjetDi) {
        this.nomTablePays = nomPays;
        this.nomIdPaysDansVille = nomIdPaysDansVille;
        this.nomTableVille = nomTableVille;
        this.nomIdVilleDansQuartier = nomIdVilleDansQuartier;
        this.nomTableQuartier = nomTableQuartier;
        quartier = informateurObjetDi.getObjetFromType(nomTableQuartier);
        ville = informateurObjetDi.getObjetFromType(nomTableVille);
        pays = informateurObjetDi.getObjetFromType(nomPays);
    }

    /**
     * Permet de générer la matrice de localité (ville, pays) à partir de la liste levis
     * Il faut utiliser la méthode "remplir_donnees" pour l'alimenter
     * Cet objet a été créé pour générer toutes les infos d'une localité en fonction de la
     * simple connaissance de l'id du quartier
     * Il a été généralisé à tous objets héritant de la même structure
     *
     * @param nomPays            nom de la table pays
     * @param nomIdPaysDansVille nom de l'id pays dans la ville
     * @param nomTableVille      nom de la table ville
     * @param informateurObjetDi C'est une Interface (locale) permettant de générer des hashobjets
     *                           à partir du nom de leur table
     */

    public GestionnaireImbrications(String nomPays, String nomIdPaysDansVille,
                                    String nomTableVille,
                                    InformateurObjet informateurObjetDi) {
        this.nomTablePays = nomPays;
        this.nomIdPaysDansVille = nomIdPaysDansVille;
        this.nomTableVille = nomTableVille;
        this.nomIdVilleDansQuartier = null;
        this.nomTableQuartier = null;
        quartier = null;
        ville = informateurObjetDi.getObjetFromType(nomTableVille);
        pays = informateurObjetDi.getObjetFromType(nomPays);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Permet de remplir les données : quartier, ville, pays
     * Si le nom de la table quartier est NULL, alors le remplissage se fait pour ville et pays
     *
     * @param id        l'id du quartier. Si pas de quartier, alors id de la ville
     * @param hashLevis c'est la hashmap détails de Levis (nomTable-)
     * @return vrai si un changement a été observé
     */
    public boolean remplir_donnees(int id, HashMap<String, Object> hashLevis) {
        if (id <= 0) {
            comment("** GestionnaireImbrications/remplir_donnees : Attention ID non positif pour " +
                    nomTablePays + "/" + nomTableVille + "/" + nomTableQuartier, "err");
            return false;
        }

        if (nomTableQuartier == null) {
            return chercher_ville(nomTablePays, nomIdPaysDansVille,
                    nomTableVille, id,
                    null, hashLevis);
        }
        change = quartier.getId() != id;
        if (change) {
            quartier.setId(id);
        }
        comment("* GestionnaireImbrications : Quartier reçu avec id " + id, "out");
        Object hypo_quartier = hashLevis == null ? null : hashLevis.get(nomTableQuartier + "-" + quartier.getId());
        if (hypo_quartier instanceof ArrayList &&
                ((ArrayList<?>) hypo_quartier).size() > 1) {
            ArrayList listeLevis = (ArrayList<?>) hypo_quartier;
            String nomLevis = String.valueOf(listeLevis.get(0));
            Object hypo_hashQuartier = listeLevis.get(1);
            if (nomLevis.equals(nomTableQuartier)) {
                if (hypo_hashQuartier instanceof HashMap) {
                    quartier.setData((HashMap<String, Object>) hypo_hashQuartier, hashLevis, true, verbose);
                    if (quartier.getFK(nomIdVilleDansQuartier) != null) {
                        int fkville = quartier.getFK(nomIdVilleDansQuartier).getId();
                        if (fkville > 0) {
                            comment("* GestionnaireImbrications : Ville du quartier " + id +
                                            " reçu avec id " + fkville,
                                    "out");
                        }
                        boolean villeChangee = chercher_ville(nomTablePays, nomIdPaysDansVille,
                                nomTableVille, fkville,
                                nomTableQuartier, hashLevis);
                        return change || villeChangee;
                    }
                }
            }
        }
        return change;
    }

    private void comment(String s, String out) {
        if (out.equals("out")) {
            System.out.println(s);
            return;
        }
        System.err.println(s);
    }

    private boolean chercher_ville(String nomPays, String nomIdPaysDansVille,
                                   String nomVille, int fkville,
                                   String nomQuartier, HashMap<String, Object> hashLevis) {
        change |= ville.getId() != fkville;
        ville.reinitialiser();
        ville.setId(fkville);

        if (hashLevis != null) {
            Object hypo_ville = hashLevis.get(nomVille + "-" + fkville);
            if (hypo_ville instanceof ArrayList &&
                    ((ArrayList<?>) hypo_ville).size() > 1) {
                ArrayList listeLevis = (ArrayList<?>) hypo_ville;
                String nomLevis = String.valueOf(listeLevis.get(0));
                Object hypo_hashVille = listeLevis.get(1);
                if (nomLevis.equals(nomVille)) {
                    if (hypo_hashVille instanceof HashMap) {
                        ville.setData((HashMap<String, Object>) hypo_hashVille, hashLevis, true, verbose);
                        change = true;
                    }
                }
            }
        }
        // si la hashLevis ne contient pas d'info, pas grave, on continue
        if (nomQuartier != null) {
            ville.updater_liste(nomQuartier, hashLevis);
        }
        if (ville.getFK(nomIdPaysDansVille) != null) {
            int fkpays = ville.getFK(nomIdPaysDansVille).getId();
            if (fkpays > 0) {
                comment("* GestionnaireImbrications : Pays trouvé avec id " + fkpays,
                        "out");
            }
            boolean paysChange = chercher_pays(nomPays, fkpays, nomVille, hashLevis);
            return change || paysChange;
        }
        return change;
    }

    private boolean chercher_pays(String nomPays, int fkpays,
                                  String nomVille, HashMap<String, Object> hashLevis) {
        change |= fkpays != pays.getId();
        pays.reinitialiser();
        pays.setId(fkpays);
        pays.updater_liste(nomVille, hashLevis);
        return change;
    }

    public boolean aChange() {
        return change;
    }


    public ObjetCSA getPays() {
        return pays;
    }

    public ObjetCSA getVille() {
        return ville;
    }

    public ObjetCSA getQuartier() {
        return quartier;
    }

    public ArrayList<ObjetCSA> getListeHashVille() {
        ArrayList<ObjetCSA> villes = pays.getListe(nomTableVille);
        if (villes == null) {
            return new ArrayList<>();
        }
        return villes;
    }

    public ArrayList<ObjetCSA> getListeHashQuartier() {
        ArrayList<ObjetCSA> quartiers = ville.getListe(nomTableQuartier);
        if (quartiers == null) {
            return new ArrayList<>();
        }
        return quartiers;
    }

    public int getFk() {
        return nomTableQuartier == null ? ville.getId() : quartier.getId();
    }

    public Object chercher_valeur(String cle) {
        Object retour = null;
        if (pays != null) retour = pays.chercher_valeur(cle);
        if (retour != null) return retour;
        if (ville != null) retour = ville.chercher_valeur(cle);
        if (retour != null) return retour;
        if (quartier != null) retour = quartier.chercher_valeur(cle);
        return retour;
    }
}
