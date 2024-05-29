package general;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Didier on 14-05-16.
 */
public class PDFManager {

    /**
     * Permet de lancer l'action d'impression d'un fichier
     * si l'action d'impression est supportée
     *
     * @param nomFichier c'est le chemin vers le fichier
     * @return Un message de retour à l'attention de l'appelant
     */
    public String imprimerFichier(String nomFichier) {
        String retour = "";
        if (Desktop.isDesktopSupported()) {
            if (Desktop.getDesktop().isSupported(Desktop.Action.PRINT)) {
                try {
                    Desktop.getDesktop().print(new File(nomFichier));
                    retour = "Impression réussie du fichier " + nomFichier;
                } catch (IOException ex) {
                    // Traitement de l'exception
                    retour = "!!! Problème I/O au moment de l'impression pdf : PDFManager";
                }
            } else {
                // La fonction n'est pas supportée par votre système
                // d'exploitation
                retour = "L'impression pdf n'est malheureusement pas supportée par votre système :-(";
            }
        } else {
            // Desktop pas supporté par votre système d'exploitation
            retour = "L'impression pdf n'est malheureusement pas supportée par votre système :-(";
        }
        return retour;
    }

    /**
     * Lancer le fichier dans une application appropriée (pdf, doc, etc)
     *
     * @param nomFichier c'est le chemin vers le fichier
     * @return Un message de retour à l'attention de l'appelant
     */
    public String afficherFichier(String nomFichier) {
        String retour = "";
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(nomFichier));
                retour = "Ouverture réussie du fichier " + nomFichier;
            } catch (IOException ex) {
                // no application registered for PDFs
                retour = "Aucun lanceur pdf trouvé. Veuillez ouvrir le fichier manuellement.";
            }
        }
        return retour;
    }

}
