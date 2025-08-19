package general;

import java.util.List;

public class ExtracteurObjetFromListe {
    public static ObjetDi extraire_par_id(List liste, int id) {
        for (Object objet : liste) {
            if (objet instanceof ObjetDi &&
                    ((ObjetDi) objet).getId() == id) {
                return (ObjetDi) objet;
            }
        }
        return null;
    }
}
