package general;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Individu extends ObjetCSA {
    private String SEXE = "sexe", PRENOM = "prenom", TEL = "telephone",
            GSM = "gsm", ADRESSE = "adresse", DATE_NAISSANCE = "dateNaissance",
            CODE_POSTAL = "codePostal", COURRIEL = "courriel", FAX = "fax", GRADE = "grade",
            MOT_DE_PASSE = "password", TITRE = "titre", URL = "url", TVA = "tva",
            LOGIN = "login", COMMENTAIRES = "commentaires";

    public Individu() {
        super("Individu", "id", "nom");
        reinitialiser_individu();
    }

    /**
     * Nouvel individu avec les paramètres de base
     *
     * @param prenom prénom
     * @param nom    nom
     * @param email  mail
     */
    public Individu(String prenom, String nom, String email) {
        super("Individu", "id", "nom");
        reinitialiser();
        setNom(nom);
        setPrenom(prenom);
        setCourriel(email);
    }

    @Override
    public ObjetCSA getParent() {
        return null;
    }

    @Override
    public void setParent(ObjetCSA parent) {

    }

    @Override
    public ObjetCSA getObjetFromType(String nomType) {
        return null;
    }

    @Override
    public ObjetCSA getNewInstance() {
        return new Individu("", "", "");
    }

    @Override
    public boolean changer_nom_variable(String ancien, String nouveau) {
        boolean reussi = super.changer_nom_variable(ancien, nouveau);
        if (reussi) {
            if (SEXE.equals(ancien)) {
                SEXE = nouveau;
            } else if (PRENOM.equals(ancien)) {
                PRENOM = nouveau;
            } else if (TEL.equals(ancien)) {
                TEL = nouveau;
            } else if (GSM.equals(ancien)) {
                GSM = nouveau;
            } else if (ADRESSE.equals(ancien)) {
                ADRESSE = nouveau;
            } else if (DATE_NAISSANCE.equals(ancien)) {
                DATE_NAISSANCE = nouveau;
            } else if (CODE_POSTAL.equals(ancien)) {
                CODE_POSTAL = nouveau;
            } else if (COURRIEL.equals(ancien)) {
                COURRIEL = nouveau;
            } else if (FAX.equals(ancien)) {
                FAX = nouveau;
            } else if (GRADE.equals(ancien)) {
                GRADE = nouveau;
            } else if (MOT_DE_PASSE.equals(ancien)) {
                MOT_DE_PASSE = nouveau;
            } else if (TITRE.equals(ancien)) {
                TITRE = nouveau;
            } else if (URL.equals(ancien)) {
                URL = nouveau;
            } else if (TVA.equals(ancien)) {
                TVA = nouveau;
            } else if (LOGIN.equals(ancien)) {
                LOGIN = nouveau;
            } else if (COMMENTAIRES.equals(ancien)) {
                COMMENTAIRES = nouveau;
            }
            return true;
        }
        return false;
    }


    public String getSexe() {
        return (ExtracteurHashMap.extraire_string(getSimpleData(SEXE)));
    }

    /**
     * Le sexe de la personne actuelle
     *
     * @param s 0 si indéterminé, 1 si m, et 2 si f
     * @return false si l'entier rentré n'est pas dans les 3 attendus
     */
    public void setSexe(Integer s) {
        Boolean reponse = false;
        switch (s) {
            case 0:
                setSexe("Indéterminé");
                break;
            case 1:
                setSexe("Masculin");
                break;
            case 2:
                setSexe("Féminin");
                break;
            default:
                setSexe("");
        }
    }

    public void setSexe(String name) {
        super.inserer_valeur(SEXE, name);
    }

    @Override
    public void reinitialiser() {
        reinitialiser_individu();
    }

    public void reinitialiser_individu() {
        initialiser(LOGIN, "");
        initialiser(nomNOM, "");
        initialiser(PRENOM, "");
        initialiser(ADRESSE, "");
        initialiser(TVA, "");
        initialiser(TEL, "");
        initialiser(GSM, "");
        initialiser(FAX, "");
        initialiser(URL, "");
        initialiser(COURRIEL, "");
        initialiser(TITRE, "");
        initialiser(MOT_DE_PASSE, "");
        initialiser(GRADE, 0);
        initialiser(CODE_POSTAL, 0);
        initialiser(DATE_NAISSANCE, LocalDateTime.now());
        initialiser(SEXE, "");
        initialiser(COMMENTAIRES, "");

        add_nom_public(COURRIEL, "Courriel");
        add_variable_visible(COURRIEL);
        add_nom_public(nomNOM, "Nom");
        add_nom_public(PRENOM, "Prénom");
        add_variable_visible(PRENOM);
    }


    @Override
    public String toString() {
        try {
            if (getNom().isEmpty()) {
                return "Individu";
            }
            return getNom().toUpperCase() + " " + getPrenom().toUpperCase();
        } catch (NullPointerException e) {
            return "ElementsBD.Individu sans nom !";
        }
    }

    public Integer getCodePostal() {
        return ExtracteurHashMap.extraire_int(getSimpleData(CODE_POSTAL));
    }

    public void setCodePostal(Integer c) {
        if (c > 0) {
            inserer_valeur(CODE_POSTAL, c);
        }
    }

    public Integer getIdLocalite() {
        return (ExtracteurHashMap.extraire_int(getSimpleData("idLocalite")));
    }

    public void setIdLocalite(Integer c) {
        if (c > 0) {
            inserer_valeur("idLocalite", c);
        }
    }


    public String getTel() {
        return (ExtracteurHashMap.extraire_string(getSimpleData(TEL)));
    }

    public void setTel(String tel1) {
        inserer_valeur(TEL, tel1);
    }

    public String getGsm() {
        return ExtracteurHashMap.extraire_string(getSimpleData(GSM));
    }

    /**
     * Si le string rentré comporte plus de 5 caractères, le booléen estDetaille
     * passe également à true!
     *
     * @param gsm1
     */
    public void setGsm(String gsm1) {
        inserer_valeur(GSM, gsm1);
    }

    public String getFax() {
        return ExtracteurHashMap.extraire_string(getSimpleData(FAX));
    }

    /**
     * Si le string rentré comporte plus de 5 caractères, le booléen estDetaille
     * passe également à true!
     *
     * @param fax1
     */
    public void setFax(String fax1) {
        inserer_valeur(FAX, fax1);
    }

    public String getCourriel() {
        return (ExtracteurHashMap.extraire_string(getSimpleData(COURRIEL)));
    }

    /**
     * Si le string rentré comporte plus de 5 caractères, le booléen estDetaille
     * passe également à true!
     *
     * @param courriel1
     */
    public void setCourriel(String courriel1) {
        inserer_valeur(COURRIEL, courriel1);
    }

    public String getUrl() {
        return (ExtracteurHashMap.extraire_string(getSimpleData(URL)));
    }

    public void setUrl(String url1) {
        inserer_valeur(URL, url1);
    }

    public String getDivers() {
        return (ExtracteurHashMap.extraire_string(getSimpleData(COMMENTAIRES)));
    }

    public void setDivers(String divers1) {
        inserer_valeur(COMMENTAIRES, divers1);
    }

    public String getTva() {
        return (ExtracteurHashMap.extraire_string(getSimpleData(TVA)));
    }

    public void setTva(String nTva) {
        inserer_valeur(TVA, nTva);
    }

    public String getTitre() {
        return ExtracteurHashMap.extraire_string(getSimpleData(TITRE));
    }

    /**
     * @param s
     */
    private void setTitre(String s) {
        inserer_valeur(TITRE, s);
    }


    public String getPrenom() {
        return ExtracteurHashMap.extraire_string(getSimpleData(PRENOM));
    }

    public void setPrenom(String s) {
        inserer_valeur(PRENOM, s);
    }

    public String getAdresse() {
        return ExtracteurHashMap.extraire_string(getSimpleData(ADRESSE));
    }

    /**
     * Si l'adresse rentrée contient plus de 2 caractères, alors le booléen
     * estDetaille passe à true! Sinon estDetaille passe à false!
     *
     * @param s c'est l'adresse rentrée
     */
    public void setAdresse(String s) {
        inserer_valeur(ADRESSE, s);

    }

    public Integer getAge() {
        return (int) ChronoUnit.YEARS.between(getDateNaissance(), LocalDateTime.now());
    }

    /**
     * Rentre l'âge d'une personne; date d'anniversaire = 1er janvier
     *
     * @param age
     * @return vrai si l'age rentré est positif
     */
    public Boolean setAge(Integer age) {
        Boolean reussi = false;
        LocalDateTime today = LocalDateTime.now();
        if (age >= 0) {
            inserer_valeur(DATE_NAISSANCE, today.minusYears(age));
            reussi = true;
        }
        return reussi;
    }

    /**
     * Détermine l'age de l'individu actuel: en années si plus d'un an; en mois
     * si né après le mois en cours; en jours si né avant aujourd'hui; Dans tous
     * les autres cas, l'age vaut zéro
     *
     * @return l'age de l'individu en cours
     */
    public String getAgeString() {
        LocalDateTime now = LocalDateTime.now();
        long nombreJours = ChronoUnit.DAYS.between(getDateNaissance(), now);
        long nombreMois = ChronoUnit.MONTHS.between(getDateNaissance(), now);
        Boolean aMoinsDUnAn = nombreJours < 366;
        if (aMoinsDUnAn) {
            Integer nbreJoursTotal = (int) nombreJours;
            if (nbreJoursTotal < 31) {
                return nbreJoursTotal + (nbreJoursTotal > 1 ? " jours" : " jour");
            }

            int nbreJours = (int) (nombreJours - 30 * nombreMois);
            return nombreMois + " mois, " + nbreJours + (nbreJours > 1 ? " jours" : " jour");
        }
        Long ageInt = ChronoUnit.YEARS.between(getDateNaissance(), now);
        if (ageInt < 6) {
            int nbreMois = (int) (nombreMois - 12 * ageInt);
            if (nbreMois < 0) {
                ageInt--;
                nbreMois = 12 + nbreMois;
            }
            String out = ageInt + (ageInt > 1 ? " ans" : " an");
            return out + ", " + nbreMois + " mois";
        }
        return ageInt + " ans";
    }

    public LocalDateTime getDateNaissance() {
        return ExtracteurHashMap.extraire_dateHeure(getSimpleData(DATE_NAISSANCE));
    }

    public void setDateNaissance(LocalDateTime c) {
        inserer_valeur(DATE_NAISSANCE, c);
    }

    public void setDateNaissance(LocalDate c) {
        inserer_valeur(DATE_NAISSANCE, c.atTime(12, 0));
    }


    public String printContenu() {
        String out;
        out = "ID : " + getId();
        out += "\nTitre : " + getTitre();
        out = out + "\nNom et prénom : " + getNom() + " " + getPrenom();
        out = out + "\nRue : " + getAdresse();
        out = out + "\nCode postal : " + getCodePostal();
        out = out + "\nTel : " + getTel();
        out = out + "\nGsm : " + getGsm();
        out = out + "\nFax : " + getFax();
        out = out + "\nCourriel : " + getCourriel();
        out = out + "\nSite web : " + getUrl();
        out = out + "\nTVA : " + getTva();
        out = out + "\nDivers : " + getDivers();
        out = out + "\nLogin : " + getLogin();
        out = out + "\nPassword : " + getPswd();
        out = out + "\nDate de naissance : " + getDateNaissance().format(DateTimeFormatter.ISO_DATE);
        out = out + "\nAge : " + getAge();

        return out;
    }

    public String afficheDetails() {
        return printContenu();
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            if (o instanceof Individu) {
                Individu autre = (Individu) o;
                boolean memePrenom = getPrenom().equals(autre.getPrenom());
                boolean memeDateNaissance = getDateNaissance().toLocalDate().equals(autre.getDateNaissance().toLocalDate());
                return memeDateNaissance && memePrenom;
            }
        }
        return false;
    }

    /*@Override
    public void setIntermediaireData(HashMap<String, Object> data) {
        System.err.println("** Individu : Appel de la méthode setIntermédaire ! Normalement, c'est à " +
                "un des objets étendus qui doit l'implémenter => " + getType());
    }*/

    @Override
    public int compareTo(Object o) {
        if (o instanceof Individu) {
            int premierNiveau = getNom().compareTo(((Individu) o).getNom());
            if (premierNiveau == 0) {
                int deuxiemeNiveau = getPrenom().compareTo(((Individu) o).getPrenom());
                if (deuxiemeNiveau == 0) {
                    return getDateNaissance().compareTo(((Individu) o).getDateNaissance());
                }
                return deuxiemeNiveau;
            }
            return premierNiveau;
        }
        return 0;
    }

    /**
     * @return Le mot de passe en md5
     */
    public String getPswd() {
        return ExtracteurHashMap.extraire_string(getSimpleData(MOT_DE_PASSE));
    }

    /**
     * Insère le mot de passe tapé à l'écran
     * Attention : ce mot de passe est transformé en MD5
     * getPswd() ne fournira pas la même valeur, évidemment
     *
     * @param mdp
     */
    public void setMotDePasse(String mdp) {
        MyPassword password = new MyPassword(mdp);
        inserer_valeur(MOT_DE_PASSE, password.getPswd());
    }

    /**
     * Cette méthode est strictement identique à setMotDePasse
     * Le mot de passe est converti en md5 et il n'y a plus moyen de le relire tel quel
     *
     * @param password
     */
    public void setPassword(String password) {
        setMotDePasse(password);
    }

    /**
     * Insère directement le mot de passe en MD5
     * Il ne sera pas modifié et sera le résultat de getPswd()
     * Par mesure de prudence, il vaut mieux utiliser le setPassword
     *
     * @param mdp
     * @deprecated
     */
    @Deprecated
    public void setMD5Pswd(String mdp) {
        inserer_valeur(MOT_DE_PASSE, mdp);
    }

    public int getGrade() {
        return ExtracteurHashMap.extraire_int(getSimpleData(GRADE));
    }

    public void setGrade(int i) {
        inserer_valeur(GRADE, i);
    }


    public String getLogin() {
        return ExtracteurHashMap.extraire_string(getSimpleData(LOGIN));
    }

    public void setLogin(String user) {
        inserer_valeur(LOGIN, user);
    }

    private static class MyPassword {
        private static final long serialVersionUID = -1231411255189583323L;
        private String pswd;

        // constructeurs
        public MyPassword() {
            pswd = hashPassword("");
        }

        public MyPassword(String input) {
            setPswd(input);
        }

        public MyPassword(char[] input) {
            setPswd(input);
        }

        // méthodes
        @Override
        public String toString() {
            String retour = "";
            for (int i = 0; i < pswd.length(); i++) {
                retour = retour + "*";
            }
            return retour;
        }

        public String getPswd() {
            return pswd;
        }

        // méthodes get et set

        /**
         * Introduit le password
         * Attention, il sera crypté en md5. Le String rentré est alors inaccessible !
         *
         * @param input
         */
        public void setPswd(String input) {
            pswd = hashPassword(input);
        }

        public void setPswd(char[] input) {
            String tmp = "";
            for (int i = 0; i < input.length; i++) {
                tmp = tmp + input[i];
            }
            pswd = tmp;
        }


        /**
         * Donne le mot de passe md5
         *
         * @return
         */
        private String getPassword() {
            return pswd;
        }

        /*
         * code pris sur internet pour coder en md5, un string
         * http://workbench.cadenhead
         * .org/news/1428/creating-md5-hashed-passwords-java
         */
        public String hashPassword(String password) {
            String hashword = null;
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(password.getBytes());
                BigInteger hash = new BigInteger(1, md5.digest());
                hashword = hash.toString(16);
            } catch (NoSuchAlgorithmException nsae) {
                nsae.printStackTrace();
            }
            return hashword;
        }
    }

    private class CalculateurAge {
        private LocalDateTime date;

        public CalculateurAge(LocalDateTime d) {
            date = d;
        }

        /**
         * Détermine l'age de l'individu actuel: en années si plus d'un an; en mois
         * si né après le mois en cours; en jours si né avant aujourd'hui; Dans tous
         * les autres cas, l'age vaut zéro
         *
         * @return l'age de l'individu en cours
         */
        public String getAgeString() {
            LocalDateTime now = LocalDateTime.now();
            long deltaJours = ChronoUnit.DAYS.between(now, date);
            Boolean aMoinsDUnAn = deltaJours < 365;
            long nbreMois = ChronoUnit.MONTHS.between(now, date);
            int jourDuMois = date.getDayOfMonth();
            LocalDateTime jourAnniv = LocalDate.of(now.getYear(), now.getMonth(), jourDuMois).atStartOfDay();
            if (aMoinsDUnAn) {
                if (deltaJours < 31) {
                    return deltaJours + (deltaJours > 1 ? " jours" : " jour");
                }

                long nbreJours = ChronoUnit.DAYS.between(jourAnniv, now);
                if (nbreJours < 0) {
                    nbreMois--;
                    nbreJours = ChronoUnit.DAYS.between(jourAnniv.minusMonths(1), now);
                    if (nbreJours == 0) {
                        return nbreMois + " mois";
                    }
                }

                return nbreMois + " mois, " + nbreJours + (nbreJours > 1 ? " jours" : " jour");
            }
            long ageInt = ChronoUnit.YEARS.between(now, date);
            if (ageInt < 6) {
                long deltaMois = ChronoUnit.MONTHS.between(jourAnniv, now);
                if (deltaMois < 0) {
                    ageInt--;
                    deltaMois = ChronoUnit.MONTHS.between(jourAnniv.minusYears(1), now);
                    if (deltaMois == 0) {
                        return ageInt + (ageInt > 1 ? " ans" : " an");
                    }
                }
                String out = ageInt + (ageInt > 1 ? " ans" : " an");
                return out + ", " + nbreMois + " mois";
            }
            return ageInt + " ans";
        }

    }
}
