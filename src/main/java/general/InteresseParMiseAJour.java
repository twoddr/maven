package general;

public abstract class InteresseParMiseAJour {
    private final String typeEcoute;

    public InteresseParMiseAJour(String type) {
        typeEcoute = type;
    }

    public abstract void go();

    public String getTypeEcoute() {
        return typeEcoute;
    }
}
