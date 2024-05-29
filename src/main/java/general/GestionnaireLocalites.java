package general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GestionnaireLocalites {
    HashMap<String, Object> hashQuartier = new HashMap<>(),
            hashVille = new HashMap<>(),
            hashPays = new HashMap<>();
    ArrayList<HashMap<String, Object>> listeHashVilles = new ArrayList<>(),
            listeHashQuartiers = new ArrayList<>();

    /**
     * Permet de générer la matrice de localité à partir de la liste levis et l'id quartier
     *
     * @param nomPays                nom de la table pays
     * @param nomIdPays              nom de l'idPays dans sa propre table
     * @param nomIdPaysDansVille     nom de l'id pays dans la ville
     * @param nomVille               nom de la table ville
     * @param nomIdVilleDansQuartier nom de l'idville dans le quartier
     * @param nomQuartier            nom de la table quartier
     * @param fkquartier             l'id du quartier recherché
     * @param hashLevis              la source d'informations au format Levis
     */
    public GestionnaireLocalites(String nomPays, String nomIdPays, String nomIdPaysDansVille, String nomVille,
                                 String nomIdVilleDansQuartier, String nomQuartier, int fkquartier,
                                 HashMap<String, Object> hashLevis) {
        Object hypo_quartier = hashLevis.get(nomQuartier + "-" + fkquartier);
        if (hypo_quartier instanceof ArrayList &&
                ((ArrayList<?>) hypo_quartier).size() > 1) {
            ArrayList listeLevis = (ArrayList<?>) hypo_quartier;
            String nomLevis = listeLevis.get(0) + "";
            Object hypo_hashQuartier = listeLevis.get(1);
            if (nomLevis.equals(nomQuartier)) {
                if (hypo_hashQuartier instanceof HashMap) {
                    hashQuartier = (HashMap<String, Object>) hypo_hashQuartier;
                    if (hashQuartier.get(nomIdVilleDansQuartier) instanceof Integer) {
                        int fkville = (int) hashQuartier.get(nomIdVilleDansQuartier);
                        chercher_ville(nomPays, nomIdPays, nomIdPaysDansVille, nomVille, fkville, nomQuartier, hashLevis);
                    } else {
                        System.err.println("** Gestionnaire de localités : Le nom d'idVille attendu <" + nomIdVilleDansQuartier +
                                "> n'est pas un int !\nIl n'y aura pas de ville ou pays !");
                    }
                } else {
                    System.err.println("** Gestionnaire de localités : Le quartier reçu de Levis" + hypo_hashQuartier +
                            " n'est pas une hashMap \nIl n'y aura aucune localité !");
                }
            } else {
                System.err.println("** Gestionnaire de localités : Le nom de quartier attendu <" + nomQuartier +
                        "> ne correspond pas à : " + nomLevis + "\nIl n'y aura aucune localité !");
            }
        } else {
            System.err.println("** Gestionnaire de localités : je ne trouve pas dans la hashLevis, l'objet" +
                    " associé à : " + nomQuartier + "-" + fkquartier + "\nIl n'y aura aucune localité !");
        }
    }

    /**
     * Permet de générer la matrice de localité à partir de la liste levis et l'id ville
     *
     * @param nomPays            nom de la table pays
     * @param nomIdPays          nom de l'idPays dans sa propre table
     * @param nomIdPaysDansVille nom de l'id pays dans la ville
     * @param nomVille           nom de la table ville
     * @param fkville            l'id de la ville recherchée
     * @param hashLevis          la source d'informations au format Levis
     */
    public GestionnaireLocalites(String nomPays, String nomIdPays,
                                 String nomIdPaysDansVille, String nomVille, int fkville,
                                 HashMap<String, Object> hashLevis) {
        chercher_ville(nomPays, nomIdPays, nomIdPaysDansVille, nomVille, fkville, "", hashLevis);
    }

    private void chercher_ville(String nomPays, String nomIdPays, String nomIdPaysDansVille,
                                String nomVille, int fkville, String nomQuartier,
                                HashMap<String, Object> hashLevis) {
        Object hypo_ville = hashLevis.get(nomVille + "-" + fkville);
        hashVille = new HashMap<>();
        hashVille.put(nomVille, fkville);
        if (hypo_ville instanceof ArrayList &&
                ((ArrayList<?>) hypo_ville).size() > 1) {
            ArrayList listeLevis = (ArrayList<?>) hypo_ville;
            String nomLevis = listeLevis.get(0) + "";
            Object hypo_hashVille = listeLevis.get(1);
            if (nomLevis.equals(nomVille)) {
                if (hypo_hashVille instanceof HashMap) {
                    hashVille = (HashMap<String, Object>) hypo_hashVille;
                }
            } else {
                System.err.println("** Gestionnaire de localités : Le nom de ville attendu <" + nomVille +
                        "> ne correspond pas à : " + nomLevis + "\nIl n'y aura aucune ville !");
            }
        } else {
            System.err.println("** Gestionnaire de localités : je ne trouve pas dans la hashLevis, l'objet" +
                    " associé à : " + nomVille + "-" + fkville + "\nIl n'y aura aucune ville !");
        }
        if (!nomQuartier.isEmpty()) {
            hashVille.put(nomQuartier,
                    chercher_liste_quartiers(nomVille + "_" + fkville,
                            nomQuartier,
                            hashLevis));
        }
        if (hashVille.get(nomIdPaysDansVille) instanceof Integer) {
            int fkpays = (int) hashVille.get(nomIdPaysDansVille);
            chercher_pays(nomPays, nomIdPays, fkpays, nomVille, hashLevis);
        } else {
            System.err.println("** Gestionnaire de localités : Le nom d'idPays attendu <" + nomIdPaysDansVille +
                    "> n'est pas un int !\nIl n'y aura pas de pays !");
        }
    }

    private void chercher_pays(String nomPays, String nomIdPays, int fkpays, String nomVille, HashMap<String, Object> hashLevis) {
        // le pays est préalablement chargé dans le système !
        // l'id seul devrait suffire !
        hashPays = new HashMap<>();
        hashPays.put(nomIdPays, fkpays);

        /*Object hypo_pays = hashLevis.get(nomPays + "-" + fkpays);
        hashPays = new HashMap<>();
        hashPays.put(nomPays, fkpays);
        if (hypo_pays instanceof ArrayList &&
                ((ArrayList<?>) hypo_pays).size() > 1) {
            ArrayList listeLevis = (ArrayList<?>) hypo_pays;
            String nomLevis = listeLevis.get(0) + "";
            Object hypo_hashPays = listeLevis.get(1);
            if (nomLevis.equals(nomPays)) {
                if (hypo_hashPays instanceof HashMap) {
                    hashPays = (HashMap<String, Object>) hypo_hashPays;
                } else {
                    System.err.println("** Gestionnaire de localités : Le nom de pays attendu <" + nomPays +
                            "> ne correspond pas à : " + nomLevis + "\nIl n'y aura aucun pays !");
                }
            } else {
                System.err.println("** Gestionnaire de localités : Le nom de pays attendu <" + nomPays +
                        "> ne correspond pas à : " + nomLevis + "\nIl n'y aura aucun pays !");
            }
        } else {
            System.err.println("** Gestionnaire de localités : je ne trouve pas dans la hashLevis, l'objet" +
                    " associé à : " + nomPays + "-" + fkpays + "\nIl n'y aura aucun pays !");
        }*/
        hashPays.put(nomVille,
                chercher_liste_villes(nomPays + "_" + fkpays,
                        nomVille,
                        hashLevis));
    }

    private ArrayList<HashMap<String, Object>> chercher_liste_villes(String cleLevis, String nomVille, HashMap<String, Object> hashLevis) {
        Object hypo_liste = hashLevis.get(cleLevis);
        if (hypo_liste instanceof ArrayList &&
                ((ArrayList<?>) hypo_liste).size() > 0) {
            ArrayList listeLevis = (ArrayList<?>) hypo_liste;
            String nomLevis = listeLevis.get(0) + "";
            List hypo_listeVilles = listeLevis.subList(1, listeLevis.size());
            if (nomLevis.equals(nomVille)) {
                listeHashVilles = new ArrayList<>(hypo_listeVilles);
                return new ArrayList<>(hypo_listeVilles);
            }
        }
        return new ArrayList<>();
    }

    private ArrayList<HashMap<String, Object>> chercher_liste_quartiers(String cleLevis, String nomQuartier,
                                                                        HashMap<String, Object> hashLevis) {
        Object hypo_liste = hashLevis.get(cleLevis);
        if (hypo_liste instanceof ArrayList &&
                ((ArrayList<?>) hypo_liste).size() > 0) {
            ArrayList listeLevis = (ArrayList<?>) hypo_liste;
            String nomLevis = listeLevis.get(0) + "";
            List hypo_listeQuartiers = listeLevis.subList(1, listeLevis.size());
            if (nomLevis.equals(nomQuartier)) {
                listeHashQuartiers = new ArrayList<>(hypo_listeQuartiers);
                return new ArrayList<>(hypo_listeQuartiers);
            }
        }
        return new ArrayList<>();
    }

    public HashMap<String, Object> getHashPays() {
        return hashPays;
    }

    public HashMap<String, Object> getHashVille() {
        return hashVille;
    }

    public HashMap<String, Object> getHashQuartier() {
        return hashQuartier;
    }

    public ArrayList<HashMap<String, Object>> getListeHashVille() {
        return listeHashVilles;
    }

    public ArrayList<HashMap<String, Object>> getListeHashQuartier() {
        return listeHashQuartiers;
    }
}
