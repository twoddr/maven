package general.calendrier;


import java.util.TreeSet;

public class FusionneurEvenements {

    Evenement nonPrioritaireGauche, nonPrioritaireDroite, prioritaire;

    /**
     * Fait interagir deux événements de manière à éviter les chevauchements
     * Il ne modifie pas les événements rentrés, donc il sort des clones
     *
     * @param prio           événement jugé prioritaire
     * @param nonPrioritaire événement jugé non-prioritaire
     * @return une liste triée de max 3 événements, en fonction du résultat du
     * télescopage
     */
    public TreeSet<Evenement> telescoper(Evenement prio, Evenement nonPrioritaire) {
        //System.out.println("DEBUG : Rencontre de " + prio.toExport() + " avec " + nonPrioritaire.toExport());
        prioritaire = prio.getCopyOf();
        Evenement nonPrio = nonPrioritaire.getCopyOf();
        TreeSet<Evenement> treeSetResultat = new TreeSet<>();

        boolean avant = prio.getDebut().isBefore(nonPrioritaire.getDebut())
                && (prio.getFin().isBefore(nonPrioritaire.getDebut())
                || prio.getFin().equals(nonPrioritaire.getDebut()));

        if (avant) {
            // System.out.println("DEBUG : => PAS DE COLLISION !");
            return treeSetResultat;
        }

        boolean apres = (prio.getDebut().isAfter(nonPrioritaire.getFin())
                || prio.getDebut().equals(nonPrioritaire.getFin()))
                && prio.getFin().isAfter(nonPrioritaire.getFin());

        if (apres) {
            //System.out.println("DEBUG : => PAS DE COLLISION !");
            return treeSetResultat;
        }

        //System.out.println("DEBUG : => COLLISION !");
        // puisqu'il y a collision, quoiqu'il arrive, le prioritaire sera retourné
        treeSetResultat.add(prioritaire);

        if (nonPrio.getDebut().isBefore(prio.getDebut())) {
            if (nonPrio.getFin().isAfter(prio.getFin())) { // englobé
                Evenement clone = nonPrio.getCopyOf();
                clone.setDebut_Fin(prio.getFin(), nonPrio.getFin());
                nonPrioritaireDroite = clone;
                nonPrioritaireGauche = nonPrio.getCopyOf();
                nonPrioritaireGauche.setDebut_Fin(nonPrio.getDebut(), prio.getDebut());
                treeSetResultat.add(nonPrioritaireDroite);
                treeSetResultat.add(nonPrioritaireGauche);
                return treeSetResultat;
            }
            // découpage inférieur
            nonPrio.setDebut_Fin(nonPrio.getDebut(), prio.getDebut());
            nonPrioritaireGauche = nonPrio.getCopyOf();
            nonPrioritaireDroite = null;
            treeSetResultat.add(nonPrioritaireGauche);
            return treeSetResultat;
        }

        if (nonPrio.getFin().isAfter(prio.getFin())) {  // découpage supérieur
            nonPrio.setDebut_Fin(prio.getFin(), nonPrio.getFin());
            nonPrioritaireDroite = nonPrio.getCopyOf();
            nonPrioritaireGauche = null;
            treeSetResultat.add(nonPrioritaireDroite);
            return treeSetResultat;
        }

        return treeSetResultat;
    }

    public Evenement getNonPrioritaireGauche() {
        return nonPrioritaireGauche;
    }

    public Evenement getNonPrioritaireDroite() {
        return nonPrioritaireDroite;
    }

    public Evenement getPrioritaire() {
        return prioritaire;
    }

}
