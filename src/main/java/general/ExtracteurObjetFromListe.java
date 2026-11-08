package general;

import java.util.List;

public class ExtracteurObjetFromListe {
    public static ObjetCSA extraire_par_id(List liste, int id) {
        for (Object objet : liste) {
            if (objet instanceof ObjetCSA &&
                    ((ObjetCSA) objet).getId() == id) {
                return (ObjetCSA) objet;
            }
        }
        return null;
    }
}
