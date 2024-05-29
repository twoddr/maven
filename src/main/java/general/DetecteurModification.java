package general;


import java.util.ArrayList;

public class DetecteurModification {

    private ArrayList<Object> listeInitiale;
    private ArrayList<Object> listeModifiee;
    private ArrayList<Object> aSupprimer = new ArrayList<>();
    private ArrayList<Object> aAjouter = new ArrayList<>();
    private ArrayList<Object> rescapes = new ArrayList<>();

    public DetecteurModification(ArrayList<Object> initiale, ArrayList<Object> modifiee) {
        listeInitiale = initiale;
        listeModifiee = modifiee;

        //generer_listes();
        generer_listes_simplement();
    }

    private void generer_listes_simplement() {
        listeModifiee.forEach((Object o) -> {
            if (estDansListe(o, listeInitiale)) {
                rescapes.add(o);
            } else {
                aAjouter.add(o);
            }
        });

        listeInitiale.forEach((Object o) -> {
            if (!estDansListe(o, listeModifiee)) {
                aSupprimer.add(o);
            }
        });
    }

    private boolean estDansListe(Object o, ArrayList<Object> liste) {
        return liste.contains(o);
    }

   /* private void generer_listes() {
        Boolean plusGrand = listeInitiale.size() < listeModifiee.size();
        Boolean memeTaille = listeInitiale.size() == listeModifiee.size();

        if (memeTaille) {
            rescapes = listeInitiale;
        } else if (plusGrand) {
            Integer taillePetite = listeInitiale.size();
            for (int i = 0; i < listeModifiee.size(); i++) {
                if (i < taillePetite) {
                    rescapes.add(listeModifiee.get(i));
                } else {
                    aAjouter.add(listeModifiee.get(i));
                }
            }
        } else {
            Integer taillePetite = listeModifiee.size();
            for (int i = 0; i < listeInitiale.size(); i++) {
                if (i < taillePetite) {
                    rescapes.add(listeModifiee.get(i));
                } else {
                    aSupprimer.add(listeInitiale.get(i));
                }
            }
        }
    }*/

    public ArrayList<Object> getListeASupprimer() {
        return aSupprimer;
    }

    public ArrayList<Object> getListeAAjouter() {
        return aAjouter;
    }

    public ArrayList<Object> getListeRescapes() {
        return rescapes;
    }
}
