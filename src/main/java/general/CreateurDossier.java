package general;

import java.io.File;

public class CreateurDossier {

    public boolean creer_si_n_existe_pas(String nomDossier) {
        File dossier = new File(nomDossier);
        if (!dossier.isDirectory()) {
            if (dossier.mkdir()) {
                System.out.println("* CréateurDossier : Le dossier " + nomDossier +
                        " n'existait pas ! Il a été créé :-)");
                return true;
            }
            System.err.println("** CréateurDossier : Le dossier " + nomDossier +
                    " n'existait pas ! Il n'a pas pu être créé :-)");
        }
        return false;
    }
}
