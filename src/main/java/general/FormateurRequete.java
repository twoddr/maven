package general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class FormateurRequete {
    private static final int CODE_AJOUT = 1, CODE_MODIF = 2, CODE_SUPPR = 3, CODE_CONSULT = 4,
            CODE_CONSULT_SPECIAL = 6, CODE_DERNIER_NUMERO = 5, CODE_CONNECT = 0, CODE_LOGIN = 8;
    static Coeur coeur;

    public FormateurRequete(Coeur application) {
        coeur = application;
    }

    /**
     * Construit le contenu de la liste d'hashmaps de la requête pour le rendre
     * conforme à l'envoi vers BD
     *
     * @param objet c'est l'objet de la requête à insérer dans la BD
     * @return une liste contenant l'objet à créer sous forme de
     *         (code,nomTable,(hashmaps))
     *         Seuls les champs modifiés et obligatoires sont inclus dans la hashmap
     */
    public static ArrayList<Object> ajouter(Object objet) {
        return traiterObjet(objet, CODE_AJOUT,
                objetDi -> true,
                FormateurRequete::extraire_modifications_et_mandatories);
    }

    /**
     * Extrait les champs modifiés, mais avec les mandatories !
     * Pas recommandé en cas de code 4 même si dans la plupart des cas, ça
     * fonctionne
     *
     * @param objetDi c'est l'objet dont on espère extraire les informations
     * @return une arraylist => (code, nomTable, hashModifications)
     */
    private static Collection<HashMap<String, Object>> extraire_modifications_et_mandatories(ObjetCSA objetDi) {
        return Collections.singletonList(objetDi.getHashModifications());
    }

    private static Collection<HashMap<String, Object>> extraire_ids(ObjetCSA objetDi) {
        return Collections.singletonList(new HashMap<>(objetDi.getOnlyIDs()));
    }

    private static void affiche_console(String s) {
        coeur.printConsole(" -> Main.FormateurRequete : " + s);
    }

    /**
     * Construit le contenu de la liste d'hashmaps de la requête pour le rendre
     * conforme à l'envoi vers BD
     *
     * @param objet c'est l'objet de la requête à insérer dans la BD. Si c'est un
     *              objetDi, son id doit être positif, sinon le retour est une liste
     *              vide
     * @return (2, nomTable, une liste contenant l ' objet à créer sous forme de
     *         hashmaps)
     *         Seuls les champs modifiés et obligatoires sont inclus dans la hashmap
     */
    public static ArrayList<Object> modifier(Object objet) {
        return traiterObjet(objet, CODE_MODIF,
                objetDi -> objetDi.getId() > 0,
                FormateurRequete::extraire_modifications_et_mandatories);
    }

    /**
     * Construit le contenu de la liste d'hashmaps de la requête pour le rendre
     * conforme à l'envoi vers BD pour une suppression
     *
     * @param objet c'est l'objet de la requête à insérer dans la BD. Si c'est un
     *              objetDi, son id doit être positif, sinon le retour est une liste
     *              vide
     * @return une (3,nomTable,liste contenant l'id des objets à supprimer sous
     *         forme de hashmaps)
     */
    public static ArrayList<Object> supprimer(Object objet) {
        return traiterObjet(objet, CODE_SUPPR,
                objetDi -> objetDi.getId() > 0,
                FormateurRequete::extraire_ids);
    }

    /**
     * Construit la liste pour EnvoiBD contenant l'hashmap de l'objet à consulter
     *
     * @param objet si son id vaut 0, alors on veut la liste d'éléments de ce type
     *              sinon, on veut le détail de l'élément envoyé (c'est la BD qui
     *              gère)
     * @return (4, nomTable, ( hashmaps))
     */
    public static ArrayList<Object> consulter(Object objet) {
        return consulter_plus(objet, CODE_CONSULT);
    }

    /**
     * Construit la liste pour EnvoiBD contenant l'hashmap de l'objet à consulter
     *
     * @param objet devrait être un membre du personnel
     * @return (8, nomTable, ( hashmaps))
     */
    public static ArrayList<?> loguer_utilisateur(Object objet) {
        return consulter_plus(objet, CODE_LOGIN);
    }

    /**
     * Construit la liste pour EnvoiBD contenant l'hashmap de l'objet à consulter
     *
     * @param objet si son id vaut 0, alors on veut la liste d'éléments de ce type
     *              sinon, on veut le détail de l'élément envoyé (c'est la BD qui
     *              gère)
     * @return (6, nomTable, ( hashmaps))
     */
    public static ArrayList<?> consulter_special(Object objet) {
        return consulter_plus(objet, CODE_CONSULT_SPECIAL);
    }

    /**
     * Construit la liste pour EnvoiBD contenant l'hashmap de l'objet à consulter
     *
     * @param objet on veut un élément de ce type avec le dernier numéro disponible
     *              dans la BD
     *              (facture par exemple)
     * @return (5, nomTable, ( hashmap))
     */
    public static ArrayList<?> consulter_dernier_numero(Object objet) {
        return consulter_plus(objet, CODE_DERNIER_NUMERO);
    }

    private static ArrayList<Object> consulter_plus(Object objet, int code) {
        ArrayList<Object> terug = new ArrayList<>();
        terug.add(code);
        if (objet instanceof ObjetCSA) {
            ObjetCSA objetDi = (ObjetCSA) objet;
            terug.add(objetDi.getType());
            terug.addAll(extraire_modifications_et_mandatories(objetDi));
            return terug;
        }
        affiche_console("Consulter table : Je ne traite pas des objets de type " + objet.getClass().getSimpleName() +
                ". Donc je laisse passer tel quel si c'est une liste !");
        return objet instanceof ArrayList<?> ? (ArrayList<Object>) objet : new ArrayList<>();
    }

    /**
     * Permet de générer une requête formatée pour passer dans le connecteurClient
     *
     * @param code  code de transfert
     * @param objet objet à envoyer (objetDi ou liste)
     * @return La requête correctement formatée
     */
    public static ArrayList<Object> generer_requete(int code, Object objet) {
        if (code == 1) {
            return ajouter(objet);
        }
        if (code == 2) {
            return modifier(objet);
        }
        if (code == 3) {
            return supprimer(objet);
        }
        if (code == 4 || code == 5 || code == 6 || code == 8 || code == 40) {
            return consulter_plus(objet, code);
        }
        if (code == 10 || code == 11) {
            ArrayList<Object> retour = new ArrayList<>();
            retour.add(code);
            retour.add(objet);
            return retour;
        }
        if (code >= 10101010) { // commande spéciale
            ArrayList<Object> retour = new ArrayList<>();
            retour.add(code);
            retour.add("*****");
            return retour;
        }
        coeur.printConsole("** Main.FormateurRequete ne reconnait pas le code " + code + ". Requête ignorée !");
        return new ArrayList<>();
    }

    private static ArrayList<Object> traiterObjet(Object objet, int code, Predicate<ObjetCSA> validation,
            Function<ObjetCSA, Collection<HashMap<String, Object>>> extracteur) {
        ArrayList<Object> terug = new ArrayList<>();
        terug.add(code);

        if (objet instanceof ObjetCSA objetDi && validation.test(objetDi)) {
            terug.add(objetDi.getType());
            terug.addAll(extracteur.apply(objetDi));
            return terug;
        }

        if (objet instanceof ArrayList<?> arraylist) {
            String nomTable = "";
            ArrayList<HashMap<String, Object>> hashMaps = new ArrayList<>();
            for (Object object : arraylist) {
                if (object instanceof ObjetCSA objetDi && validation.test(objetDi)) {
                    if (nomTable.isEmpty())
                        nomTable = objetDi.getType();
                    hashMaps.addAll(extracteur.apply(objetDi));
                }
            }
            if (nomTable.isEmpty()) {
                String operation = getOperationName(code);
                affiche_console(operation + " : Je n'ai pas su extraire d'information de la liste " + objet +
                        (code == CODE_MODIF || code == CODE_SUPPR ? "\nIl manque des ids?" : "") +
                        " Donc je retourne une liste vide !");
                return new ArrayList<>();
            }
            terug.add(nomTable);
            terug.add(hashMaps);
            return terug;
        }

        String operation = getOperationName(code);
        affiche_console(operation + " : Je ne traite pas des objets de type " + objet.getClass().getSimpleName() +
                ". Donc je retourne une liste vide !");
        return new ArrayList<>();
    }

    private static String getOperationName(int code) {
        return switch (code) {
            case CODE_AJOUT -> "Ajouter";
            case CODE_MODIF -> "Modifier";
            case CODE_SUPPR -> "Supprimer";
            default -> "Opération";
        };
    }
}
