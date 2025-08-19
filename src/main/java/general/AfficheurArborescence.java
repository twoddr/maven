package general;

import java.util.ArrayList;

public class AfficheurArborescence {
    /**
     * Attention : ne peut pas fonctionner correctement car on travaille sur les
     * ids et idsParent qui peuvent être égaux dans des profondeurs différentes !!!
     * À METTRE À JOUR !!!
     *
     * @param objetDis les objets à afficher
     * @param espace   l'espace de décalage entre parents et enfants
     * @return La liste comme elle apparait dans le TreeView par exemple
     */
    @Deprecated
    public static String afficher(ArrayList<? extends ObjetDi> objetDis, String espace) {
        StringBuilder terug = new StringBuilder();
        ArrayList<ObjetDi> reducedList = new ArrayList<>(objetDis);
        ArrayList<ObjetDi> parents = extraire_parents(reducedList, 0);
        for (ObjetDi parent : parents) {
            terug.append(espace).append(parent.toString()).append("\n")
                    .append(afficher(extraire_parents(reducedList, parent.getId()), "    "))
                    .append("\n");
        }
        return terug.toString();
    }

    private static ArrayList<ObjetDi> extraire_parents(ArrayList<ObjetDi> reducedList,
                                                       int idParent) {
        ArrayList<ObjetDi> terug = new ArrayList<>();
        for (ObjetDi objetDi : reducedList) {
            if (objetDi.getIdParent() == idParent) {
                terug.add(objetDi);
            }
        }
        reducedList.removeAll(terug);
        return terug;
    }
}
