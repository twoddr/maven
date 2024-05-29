package general;

import java.util.ArrayList;

public class AfficheurRequete {

    public static String getDetails(ArrayList in) {
        StringBuilder retour = new StringBuilder("{");
        int i = 0;
        for (Object elt : in) {
            if (elt instanceof ArrayList) {
                retour.append("\n").append(getDetails((ArrayList) elt));
            } else {
                retour.append(elt).append(i < in.size() - 1 ? ", " : "");
            }
            i++;
        }
        retour.append("}\n");
        return retour.toString();
    }

    /**
     * Permet de vérifier le formatage CSA de la requête (int, String, (arrayList))
     *
     * @param requete requête à tester
     * @param verbose commentaires affichés dans la console si true
     * @return conforme ou pas conforme
     */
    public static Boolean estBienFormatee(ArrayList<?> requete, boolean verbose) {
        int taille = requete.size();
        if (taille < 2) {
            if (verbose) System.err.println("*** Requête trop petite trouvée !" +
                    "\n=> " + requete + " Il me faut une taille de 2 au moins : Un code et" +
                    " un nom de table !");
            return false;
        }
        Object premier = requete.get(0);
        Object deuxieme = requete.get(1);
        if (!(premier instanceof Integer)) {
            if (verbose) System.err.println("*** Le premier élément de la requête n'est pas" +
                    " un INT ! J'ai un " + premier.getClass().getSimpleName() + " : " + premier);
            return false;
        }
        if (!(deuxieme instanceof String)) {
            if (verbose) System.out.println("*** Le 2e élément de la requête n'est pas" +
                    " un STRING ! J'ai un " + premier.getClass().getSimpleName() + " : " + deuxieme);
            return false;
        }
        int code = (int) premier;
        String nomTable = deuxieme + "";
        if (verbose) System.out.println("* AfficheurRequete : J'ai reçu un paquet conforme : " +
                "(" + code + ", " + nomTable + ", " +
                affiche_nombre_elts(taille - 2) + ")");

        return true;
    }

    private static String affiche_nombre_elts(int i) {
        return i + " " + (i > 1 ? "elts" : "elt");
    }
}
