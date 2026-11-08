package general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class TransformeurObjets {

    /**
     * Surcharge avec un résolveur de type fonctionnel (sans MaitreFX).
     *
     * @param typeResolver la fonction pour créer une instance vide du type donné
     */
    public static ArrayList<ObjetCSA> transformer_en_objetsCSA(ArrayList<?> donnees,
            String nomTable,
            HashMap<String, Object> hashlevis,
            Function<String, ObjetCSA> typeResolver) {
        InformateurObjet adaptateur = typeResolver != null ? typeResolver::apply : t -> null;
        return transformer_en_objetsCSA(donnees, nomTable, hashlevis, (Object) adaptateur);
    }

    public static ArrayList<ObjetCSA> transformer_en_objetsCSA(ArrayList<?> donnees,
            String nomTable,
            HashMap<String, Object> hashlevis,
            Object informateur) {
        ArrayList<ObjetCSA> terug = new ArrayList<>();
        for (Object donnee : donnees) {
            terug.add(transformer_en_objetCSA(donnee, nomTable, hashlevis, informateur));
        }
        return terug;
    }

    /**
     * Transforme une liste formatée d'hashmaps en liste formatée d'objetDis
     *
     * @param paquetStandard une liste de type (code, nomTable, (liste
     *                       d'hashmaps),hashmap)
     *                       la hasmap est utilisée pour recréer les détails des
     *                       objets
     *                       créés
     * @param informateur    celui qui connait le nom des tables
     * @return retourne (code, nomTable, (liste d'objetDis))
     */
    public static ArrayList<Object> transformerPaquetStandard(ArrayList<Object> paquetStandard,
            InformateurObjet informateur) {
        if (paquetStandard.size() > 2 &&
                paquetStandard.get(0) instanceof Integer code) {
            String nomTable = String.valueOf(paquetStandard.get(1));
            if (paquetStandard.get(2) instanceof ArrayList liste) {
                HashMap<String, Object> hashLevis = null;
                if (paquetStandard.size() > 3 && paquetStandard.get(3) instanceof HashMap hashMap) {
                    hashLevis = hashMap;
                }
                ArrayList<Object> retour = new ArrayList<>();
                retour.add(code);
                retour.add(nomTable);
                retour.add(transformerListePure(liste, nomTable, hashLevis, informateur));
                return retour;
            }
            if (paquetStandard.get(2) instanceof HashMap hashMap) {
                HashMap<String, Object> hashLevis = (paquetStandard.size() > 3
                        && paquetStandard.get(3) instanceof HashMap)
                                ? (HashMap<String, Object>) paquetStandard.get(3)
                                : null;
                ObjetCSA objetDi = transformer_en_objetCSA(hashMap, nomTable, hashLevis, informateur);
                ArrayList<Object> retour = new ArrayList<>();
                retour.add(code);
                retour.add(nomTable);
                retour.add(objetDi);
                return retour;
            }
            if (paquetStandard.get(2) instanceof ObjetCSA objetDi) {
                ArrayList<Object> retour = new ArrayList<>();
                retour.add(code);
                retour.add(nomTable);
                retour.add(objetDi);
                return retour;
            }
        }
        return null;
    }

    /**
     * Transforme une liste d'hashmaps en objetDis
     *
     * @param objects     les hashmaps
     * @param hashLevis   les données détaillées
     * @param informateur un maitreFX ou un informateurObjets
     * @return liste vide si pas d'hashmap
     */
    private static ArrayList<ObjetCSA> transformerListePure(ArrayList<Object> objects, String nomTable,
            HashMap<String, Object> hashLevis,
            Object informateur) {
        ArrayList<ObjetCSA> terug = new ArrayList<>();
        for (Object object : objects) {
            terug.add(transformer_en_objetCSA(object, nomTable, hashLevis, informateur));
        }
        return terug;
    }

    /**
     * Transforme en ObjetCSA, un objetIHM ou une hashmap
     *
     * @param element     peut importe ce que c'est, on fera ce qu'on pourra
     * @param nomTable    le nom de la table concernée
     * @param hashlevis   une hashmap de détails
     * @param informateur celui qui connait les tables
     * @return un objetDi avec les paramètres fournis
     */
    public static ObjetCSA transformer_en_objetCSA(Object element,
            String nomTable,
            HashMap<String, Object> hashlevis,
            Object informateur) {
        if (element instanceof ObjetCSA) {
            ObjetCSA objetDi = (ObjetCSA) element;
            objetDi.clearModifications();
            return objetDi;
        }
        if (element instanceof HashMap hashMap) {
            if (informateur instanceof InformateurObjet) {
                InformateurObjet informateurObjet = (InformateurObjet) informateur;
                ObjetCSA hashObjet = informateurObjet.getObjetFromType(nomTable);
                hashObjet.setData(hashMap, hashlevis, true, false);
                hashObjet.clearModifications();
                return hashObjet;
            }
            System.err.println(
                    "TransfomateurObjets : Je n'ai pas d'informateur pour créer un objetDi àpd " + nomTable +
                            " et " + element);
            return null;
        }
        return null;
    }

}
