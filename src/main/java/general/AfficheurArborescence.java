package general;

import java.util.ArrayList;

public class AfficheurArborescence {
    public static String afficher(ArrayList<? extends ObjetDi> objetDis, String espace) {
        StringBuilder terug = new StringBuilder();
        ArrayList<? extends ObjetDi> reducedList = new ArrayList<>(objetDis);
        ArrayList<? extends ObjetDi> parents = extraire_parents(reducedList, 0);
        for (ObjetDi parent : parents) {
            terug.append(espace).append(parent.toString()).append("\n")
                    .append(afficher(extraire_parents(reducedList, parent.getId()), "    "))
                    .append("\n");
        }
        return terug.toString();
    }

    private static ArrayList<? extends ObjetDi> extraire_parents(ArrayList<? extends ObjetDi> reducedList,
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
