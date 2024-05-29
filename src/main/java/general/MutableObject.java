package general;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MutableObject {
    private Object valeur;
    private Class type;

    /**
     * Objet coquille dont le contenu est modifiable
     * L'objet est donc passable à une fonction "par référence"
     *
     * @param value la valeur initiale de l'objet. Les objets y insérés seront
     *              dorénavant du même type, sous peine de rejet !
     */
    public MutableObject(Object value) {
        valeur = value;
        type = value.getClass();
    }

    public Object getValeur() {
        return valeur;
    }

    public boolean getBoolean() {
        if (valeur instanceof Boolean) {
            return (boolean) valeur;
        }
        System.err.println("** MutableObject/getBoolean : " + valeur + " n'est pas de type bool" +
                " donc je retourne false");
        return false;
    }

    public int getInteger() {
        if (valeur instanceof Integer) {
            return (int) valeur;
        }
        System.err.println("** MutableObject/getInteger : " + valeur + " n'est pas de type int" +
                " donc je retourne -1");
        return -1;
    }

    public float getFloat() {
        if (valeur instanceof Float) {
            return (float) valeur;
        }
        System.err.println("** MutableObject/getFloat : " + valeur + " n'est pas de type float" +
                " donc je retourne -0.1");
        return -0.1f;
    }

    public String getString() {
        return valeur.toString();
    }

    public LocalDate getLocalDate() {
        if (valeur instanceof LocalDate) {
            return (LocalDate) valeur;
        }
        System.err.println("** MutableObject/getLocalDate : " + valeur + " n'est pas de type localDate" +
                " donc je retourne aujourd'hui");
        return LocalDate.now();
    }

    public LocalDateTime getLocalDateTime() {
        if (valeur instanceof LocalDateTime) {
            return (LocalDateTime) valeur;
        }
        System.err.println("** MutableObject/getLocalDateTime : " + valeur + " n'est pas de type localDateTime" +
                " donc je retourne aujourd'hui");
        return LocalDateTime.now();
    }

    public boolean setValeur(Object value) {
        if (value.getClass().equals(type)) {
            valeur = value;
            return true;
        }
        System.err.println("** MutableObject/setValeur : Échec car le type attendu est " +
                type.getSimpleName() + " et l'objet rentré est " + value);
        return false;
    }

    public Class getType() {
        return type;
    }
}
