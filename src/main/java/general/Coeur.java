package general;

import java.util.ArrayList;

public interface Coeur {
    /**
     * Permet d'imprimer dans la barre d'info et dans une console log
     *
     * @param s Chaine à afficher
     */
    void print(String s);

    /**
     * Permet d'afficher dans la console, sans afficher dans la barre
     * d'informations
     *
     * @param string
     */
    void printConsole(String string);


    /**
     * Retourne vers l'appelant avec la fenetre de connexion /!\ Ne pas oublier
     * de fermer cette fenêtre avec la méthode dispose() !
     *
     * @param liste_BD_ou_Tables soit la liste des tables pour lancer le programme,
     *                           soit la liste des BD disponibles
     */
    void action_retourChercheurConnexion(ArrayList<Object> liste_BD_ou_Tables);

    /**
     * Exécute le code de retour d'une fenêtre login
     *
     * @param fenetre c'est la fenêtre login qui doit être fermée
     * @param liste   la liste contient les informations de login (souvent un membre du personnel)
     */
    void action_retour_login(Object fenetre, Object liste);

    void traitement_broadcast(ArrayList arrayList);

    void traitement_retour_normal(ArrayList<?> arrayList);

    boolean isTest();
}
