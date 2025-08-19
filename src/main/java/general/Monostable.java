package general;

public class Monostable {
    private final MutableObject objet = new MutableObject(false);
    private int duree = 1;
    private Object etatStable;
    private Object etatInstable;

    /**
     * Affiche une valeur pendant une certaine durée
     *
     * @param etatInstable valeur initiale à attribuer
     * @param etatStable   valeur finale après le délai (défaut : 1s)
     */
    public Monostable(Object etatInstable, Object etatStable) {
        this.etatInstable = etatInstable;
        this.etatStable = etatStable;
    }

    /**
     * Met la valeur instable, attend la durée puis remet l'état stable
     * et puis c'est tout
     */
    public void go() {
        new Thread() {
            @Override
            public void run() {
                setValue(etatInstable);
                try {
                    Thread.sleep(1000L * duree);  // Wait for the specified duration (duree in seconds)
                } catch (InterruptedException e) {
                    System.err.println("*** Monostable/go : La temporisation de " + objet +
                            " a échoué !");
                }
                setValue(etatStable);
            }
        }.start();
    }

    private void setValue(Object etat) {
        if (objet.getType().equals(etat.getClass())) {
            objet.setValeur(etat);
            return;
        }
        System.err.println("** Monostable/setValue : La valeur rentrée " + etat +
                " ne correspond pas au type attendu " + objet.getType().getSimpleName());
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        if (duree > 0) {
            this.duree = duree;
            return;
        }
        System.err.println("** Monostable/setDuree : Échec ! La durée doit être positive et "
                + "j'ai reçu : " + duree);
    }

    public Object getEtatStable() {
        return etatStable;
    }

    public void setEtatStable(Object etatStable) {
        this.etatStable = etatStable;
    }

    public Object getEtatInstable() {
        return etatInstable;
    }

    public void setEtatInstable(Object etatInstable) {
        this.etatInstable = etatInstable;
    }

    public boolean isStable() {
        return etatStable.equals(objet.getBoolean());
    }
}
