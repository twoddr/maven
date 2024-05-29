package general;


import java.util.ArrayList;

public class DetecteurPresence {

    private Object objet;
    private ArrayList<Object> liste;

    public DetecteurPresence(Object o, ArrayList list) {
        objet = o;
        liste = list;
    }

    public Boolean isIn() {
        for (Object elt : liste) {
            if ((elt instanceof ObjetDi) && (objet instanceof ObjetDi)) {
                ObjetDi dboElt = (ObjetDi) elt;
                ObjetDi dbo = (ObjetDi) objet;
                if (dbo.getId() == dboElt.getId() && elt.toString().equals(objet.toString())) {
                    return true;
                }
            } else if (elt.toString().equals(objet.toString())) {
                return true;
            }
        }
        return false;
    }
}
