package general;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class LecteurFichier {
    private static ArrayList<String> lignes = new ArrayList<>();
    private static File fichier;

    public static ArrayList<String> getLignes(String cheminComplet) {
        fichier = new File(cheminComplet);
        if (fichier.exists()) {
            lignes.clear();
            try {
                lignes.addAll(Files.readAllLines(Paths.get(fichier.getAbsolutePath())));
            } catch (IOException e) {
                System.err.println("** LecteurFichierTexte/constructeur : la lecture de ligne a échoué !");
            }
        } else {
            System.err.println("*** LecteurFichierTexte/constructeur : le fichier " + cheminComplet
                    + " n'existe pas ! Impossible de le lire, donc c'est la fin");
        }
        return lignes;
    }

    public static String getLigne(String cheminComplet, int numero) {
        lignes = getLignes(cheminComplet);
        if (numero <= 0) {
            System.err.println("*** LecteurFichierTexte/constructeur : le numéro de ligne doit être " +
                    "un nombre positif !");
            return "";
        }
        if (numero > lignes.size()) {
            System.err.println("*** LecteurFichierTexte/constructeur : pas de ligne " + numero + " dans" +
                    " le fichier " + fichier.getAbsolutePath());
            return "";
        }
        return lignes.get(numero - 1);
    }
}
