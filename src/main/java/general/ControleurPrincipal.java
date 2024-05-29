package general;


import java.util.ArrayList;

/**
 * Ce titre est donné à toute fenêtre qui présente des
 * filles ; ces filles doivent obligatoirement passer par leur
 * fenêtre mère
 */
public interface ControleurPrincipal {
    void inscrire_composant(String nomClasse, Object composant, boolean inscrire);

    void inscrire_thread(String nomClasse, InteresseParMiseAJour processus, boolean inscrire);

    void demande_liste(String type, GestionnaireRetourBD appelant);

    void requete_BD(ArrayList<Object> requete);

    void affiche_utilisateur(String texte);

    void affiche_console(String texte);

    void demande_details_pour_edition(Object requete);

    ObjetDi getElementFromType(String typeAffiche);

    /**
     * Permet de notifier d'une action
     *
     * @param source       l'objet à l'origine (par exemple ListTableBD)
     * @param selectedItem l'objetihm sélectionné
     * @param nomColonne   le nom de la colonne cliquée
     */
    void action_utilisateur(Object source,
                            ObjetDi selectedItem,
                            String nomColonne);
}
