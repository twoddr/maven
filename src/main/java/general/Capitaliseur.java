package general;

public abstract class Capitaliseur {
    public static String capitaliser_initiales(String s) {
        StringBuilder sortie = new StringBuilder();
        String[] mots = s.split(" ");
        int nombreMots = mots.length;
        int count = 0;
        for (String mot : mots) {
            int longueurMot = mot.length();
            if (longueurMot > 1) {
                sortie.append(mot.substring(0, 1).toUpperCase())
                        .append(mot.toLowerCase(), 1, longueurMot);
            } else if (longueurMot == 1) {
                sortie.append(mot.toUpperCase());
            }
            count++;
            if (count < nombreMots) {
                sortie.append(" ");
            }
        }
        return sortie.toString();
    }
}
