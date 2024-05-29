package general;


import java.awt.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ObjetDi
        implements Comparable, Serializable {
    private final HashMap<String, ObjetDi> hashFKs = new HashMap<>(),
            hashFKIntermediaire = new HashMap<>();
    /**
     * ce sont les sous-objets qui contiennent l'objet courant comme sous-objet
     */
    private final HashMap<String, ObjetDi> hashDependants = new HashMap<>();
    private final HashMap<String, String> paires_NomsTables_NomsSpeciaux = new HashMap<>();
    private final HashMap<String, Object> hashDonnees = new HashMap<>();

    /**
     * ceci est indispensable si on veut avoir des données null mais garder le type !
     * *
     */
    private final HashMap<String, Class> hashClassesDonnees = new HashMap<>();
    private final HashMap<String, Object> hashDonneesNonAttendues = new HashMap<>();
    private final HashMap<String, ArrayList<ObjetDi>> hashListes = new HashMap<>();
    private final HashMap<String, GestionnaireImbrications> gestionnairesImbrications = new HashMap<>();
    protected String TYPE, nomNOM, nomID;
    protected String COULEUR = "couleur";
    protected LinkedHashSet<String> mandatories = new LinkedHashSet<>();
    protected ArrayList<String> variablesVisibles = new ArrayList<>();
    protected HashMap<String, String> nomsPublics = new HashMap<>();
    private LinkedHashSet<String> modifications = new LinkedHashSet<>();
    private String publicName = "Objet non défini";
    private boolean estParent = false;


    /**
     * Pour créer un ObjetDi, il faut lui donner les noms des variables nécessaires
     * Les informations nécessaires permettent d'identifier, dans l'hashmap de données,
     * quelles sont celles qu'on observe généralement dans une interface graphique
     * L'id et l'id intermédiaire sont utiles lors des sélections et échanges de liste...
     *
     * @param type     le nom de la table de l'objet représenté
     * @param nomID    le nom de la variable qui stocke l'id
     * @param nomObjet le nom du nom de la variable nom
     */
    public ObjetDi(String type,
                   String nomID,
                   String nomObjet) {

        TYPE = type.isEmpty() ? "Objet" : type;
        this.nomID = nomID.isEmpty() ? "id" : nomID;
        nomNOM = nomObjet.isEmpty() ? "nom" : nomObjet;

        initialiser_objetDi();

        variablesVisibles.add(nomNOM);
    }

    /**
     * Pour créer un HashObjet, il faut lui donner les noms des variables nécessaires
     * Les informations nécessaires permettent d'identifier, dans l'hashmap de données,
     * quelles sont celles qu'on observe généralement dans une interface graphique
     * L'id et l'id intermédiaire sont utiles lors des sélections et échanges de liste...
     *
     * @param nomsVariables c'est la liste des noms de variables
     *                      d'après l'ordre ci-dessous :
     *                      * nomType            le nom de la classe de l'objet représenté
     *                      * nomID              le nom de la variable qui stocke l'id
     *                      * nomObjet           le nom du nom de la variable nom
     *                      *  nomCouleur         le nom de la variable qui contient la couleur texte
     *                      * nomIDIntermediaire le nom de l'id intermédiaire
     *                      * nomDateDu          le nom de la variable dateDu
     *                      *  nomDateAu          le nom de la variable dateAu
     */
    public ObjetDi(ArrayList<String> nomsVariables) {
        int taille = nomsVariables.size();
        TYPE = nomsVariables.isEmpty() ? "Objet" : nomsVariables.get(0);
        nomID = taille < 2 ? "id" : nomsVariables.get(1);
        nomNOM = taille < 3 ? "nom" : nomsVariables.get(2);

        initialiser_objetDi();

        variablesVisibles.add(nomNOM);
    }

    /**
     * Associe un nomFk de quartier à un gestionnaire d'imbrications.
     * À partir de l'id du quartier, le gestionnaire est capable de rechercher les villes et pays
     * pour affichage dans des combos par exemple
     *
     * @param nomFk        le nom du champ idquartier
     * @param gestionnaire le gestionnaire de localités
     */
    public void addGestionnaire(String nomFk, GestionnaireImbrications gestionnaire) {
        gestionnairesImbrications.put(nomFk, gestionnaire);
    }


    /**
     * Donne l'hashObjet stocké à la clé rentrée
     *
     * @param cle nom de la variable
     * @return null si un tel objet n'existe pas !
     */
    public ObjetDi getObjet(String cle) {
        return hashFKs.get(cle);
    }

    /**
     * Donne l'hashObjet intermédiaire stocké à la clé rentrée
     *
     * @param cle nom de la variable
     * @return null si un tel objet n'existe pas !
     */
    public ObjetDi getObjetIntermediaire(String cle) {
        return hashFKIntermediaire.get(cle);
    }

    /**
     * Donne l'hashObjet de données brutes stocké à la clé rentrée
     *
     * @param cle nom de la variable
     * @return null si un tel objet n'existe pas !
     */
    public ObjetDi getObjetDependant(String cle) {
        return hashDependants.get(cle);
    }

    public void putAll(Map<? extends String, ?> m) {
        for (Map.Entry<? extends String, ?> entry : m.entrySet()) {
            String cle = entry.getKey();
            Object valeur = entry.getValue();
            inserer_valeur(cle, valeur);
        }
    }

    /**
     * Stocke la liste fournie uniquement si ce sont des objetsDi
     * Les non-objetDis sont ignorés
     *
     * @param nomTable le type des objetsDi
     * @param objets   liste d'objets quelconques
     * @return vrai si tous les objets étaient des objetsDi et que l'enregistrement s'est bien passé
     */
    public boolean inserer_liste(String nomTable, ArrayList objets) {
        if (!hashListes.containsKey(nomTable)) {
            comment("inserer_liste : Cet objet n'attend pas de liste de " + nomTable + " => " +
                    "Je rajoute !", "out");
        }
        AtomicBoolean retour = new AtomicBoolean(true);
        ArrayList<ObjetDi> objetDis = new ArrayList<>();
        objets.forEach(objet -> {
            if (objet instanceof ObjetDi) {
                objetDis.add((ObjetDi) objet);
            } else {
                retour.set(false);
            }
        });
        hashListes.put(nomTable, objetDis);
        addModification(nomTable);
        return retour.get();
    }

    /**
     * rajoute des éléments dans une liste stockée
     * Attention : les doublons sont ignorés !
     *
     * @param nomTable
     * @param objetDis
     * @return vrai si tout se passe bien
     */
    public boolean ajouter_dans_liste(String nomTable, ArrayList<ObjetDi> objetDis) {
        if (!hashListes.containsKey(nomTable)) {
            comment("rajouter_dans_liste : Cet objet n'attend pas de liste de " + nomTable + " !\n" +
                    "J'en crée une nouvelle, comme l'aurait fait inserer_liste !", "err");
            return inserer_liste(nomTable, objetDis);
        }
        TreeSet<ObjetDi> oldList = new TreeSet<>(hashListes.get(nomTable));
        oldList.addAll(objetDis);
        return inserer_liste(nomTable, new ArrayList(oldList));
    }

    public boolean inserer_fk(String nomFK, ObjetDi objetDi) {
        if (objetDi == null) {
            throw new UnsupportedOperationException("ObjetDi " + this + " /inserer_fk : L'objetDi rentré ne peut être NULL !");
        }
        if (hashFKs.containsKey(nomFK)) {
            ObjetDi ancienObjet = hashFKs.get(nomFK);
            if (!objetDi.equals(ancienObjet)) {
                addModification(nomFK);
            }
            hashFKs.put(nomFK, objetDi);
            return true;
        }
        return false;
    }

    public void inserer_dependant(String nomTable, ObjetDi objetDi) {
        if (!hashDependants.containsKey(nomTable)) {
            comment("inserer_dependant : Cet objet [" + this + "] n'attend pas de sousObjet de nom " + nomTable + " !\n" +
                    "J'ignore l'insertion !", "err");
            return;
        }
        addModification(nomTable);
        hashDependants.put(nomTable, objetDi);
    }


    public boolean inserer_valeur_intermediaire(String tableIntermediaire, String nomVariable, Object valeur) {
        if (!hashFKIntermediaire.containsKey(tableIntermediaire)) {
            comment("inserer_intermediaire : Cet objet n'attend pas de d'intermédiaire de nom " + tableIntermediaire + " !\n" +
                    "J'ignore l'insertion de " + valeur + " !", "err");
            return false;
        }
        ObjetDi objetIntermediaire = hashFKIntermediaire.get(nomVariable);
        return objetIntermediaire.inserer_valeur(nomVariable, valeur);
    }

    public boolean inserer_dans_gestionnaireImbrication(String nomFK, int id) {
        if (gestionnairesImbrications.containsKey(nomFK)) {
            GestionnaireImbrications gestionnaireTrouve = gestionnairesImbrications.get(nomFK);
            boolean change = gestionnaireTrouve.remplir_donnees(id, null);
            if (change) {
                addModification(nomFK);
            }
            return true;
        }
        comment("ObjetDi " + this + " /inserer_dans_gestionnaire : Cet objet : " + this + " n'attend pas de nom " + nomFK + " !\n" +
                "J'ignore l'insertion !", "err");
        return false;
    }

    public boolean inserer_dans_gestionnaireImbrication(String nomFK, ObjetDi objetDi) {
        if (gestionnairesImbrications.containsKey(nomFK)) {
            GestionnaireImbrications gestionnaireTrouve = gestionnairesImbrications.get(nomFK);
            HashMap<String, Object> hashLevis = new HashMap<>();
            String cleLevis = objetDi.getType() + "-" + objetDi.getId();
            ArrayList<Object> couple = new ArrayList<>();
            couple.add(objetDi.getType());
            couple.add(objetDi.getSimpleData());
            hashLevis.put(cleLevis, couple);
            boolean change = gestionnaireTrouve.remplir_donnees(objetDi.getId(), hashLevis);
            if (change) {
                addModification(nomFK);
            }
            return true;
        }
        comment("ObjetDi " + this + " /inserer_dans_gestionnaire : Cet objet : " + this + " n'attend pas de nom " + nomFK + " !\n" +
                "J'ignore l'insertion !", "err");
        return false;
    }

    public void putAllExpected(Map<? extends String, ?> m) {
        for (Map.Entry<? extends String, ?> entry : m.entrySet()) {
            String cle = entry.getKey();
            Object valeur = entry.getValue();
            if (hashDonnees.containsKey(cle)) {
                inserer_valeur(cle, valeur);
            }
        }
    }

    public boolean set_single_data(String nom, Object valeur) {
        if (hashDonnees.containsKey(nom)) {
            return inserer_valeur(nom, valeur);
        }

        // fk intermédiaires
        for (Map.Entry<String, ObjetDi> paire : hashFKIntermediaire.entrySet()) {
            ObjetDi objetIntermediaire = paire.getValue();
            boolean insertionReussie = objetIntermediaire.set_single_data(nom, valeur);
            if (insertionReussie) {
                return true;
            }
        }

        // fks
        if (hashFKs.containsKey(nom)) {
            ObjetDi sousObjet = hashFKs.get(nom);
            // ça ne peut être qu'un id
            if (valeur instanceof Integer && sousObjet != null) {
                int nouvelId = (int) valeur;
                if (nouvelId != sousObjet.getId()) {
                    sousObjet.reinitialiser();
                    sousObjet.setId(nouvelId);
                    return true;
                }
            }
        }

        // listes
        if (hashListes.containsKey(nom)) {
            ArrayList<ObjetDi> sousObjets = hashListes.get(nom);
            if (valeur instanceof ArrayList) {
                sousObjets.clear();
                sousObjets.addAll((Collection) valeur);
                return true;
            }
        }

        // gestionnaires d'imbrication
        // les infos se trouvent toujours dans la hashLevis
        if (gestionnairesImbrications.containsKey(nom)) {
            GestionnaireImbrications gestionnaire = gestionnairesImbrications.get(nom);
            if (valeur instanceof Integer) {
                gestionnaire.remplir_donnees((int) valeur, null);
                return true;
            }
        }

        System.err.println("*** ObjetDi " + this + " /set_single_data : la donnée (" + nom + ", " + valeur + ")" +
                " n'a pas pu être insérée dans l'objet " + this);
        return false;
    }

    public boolean isModifie(String nom) {
        return modifications.contains(nom);
    }

    private void initialiser_objetDi() {
        hashDonnees.clear();
        hashDonneesNonAttendues.clear();
        initialiser(nomID, 0);
        initialiser(nomNOM, "");
        initialiser(COULEUR, RGBConverter.convert_color_2_int(Color.DARK_GRAY));

        mandatories.add(nomNOM);
        mandatories.add(nomID);
    }

    public Object remove(String key) {
        mandatories.remove(key);
        modifications.remove(key);
        Object objetNonAttendu = hashDonneesNonAttendues.remove(key);
        if (objetNonAttendu == null) {
            return hashDonnees.remove(key);
        }
        return objetNonAttendu;
    }

    public final String getType() {
        return TYPE;
    }

    public void setType(String texte) {
        if (texte.isEmpty()) {
            System.err.println("*** ObjetDi " + this + " /setType : vous ne pouvez pas insérer un type vide !\n" +
                    "Ignoré !");
            return;
        }
        TYPE = texte;
    }

    private void remplacer_noms_stockes(String ancien, String nouveau) {
        if (mandatories.remove(ancien)) {
            mandatories.add(nouveau);
            return;
        }
        if (modifications.remove(ancien)) {
            modifications.add(nouveau);
            return;
        }
        if (variablesVisibles.remove(ancien)) {
            variablesVisibles.add(nouveau);
            return;
        }
    }

    public int getCouleur() {
        return ExtracteurHashMap.extraire_int(get(COULEUR));
    }

    public void setCouleur(int couleurInt) {
        inserer_valeur(COULEUR, couleurInt);
    }

    /**
     * Récupère l'ID de l'élément actuel
     *
     * @return l'id pour la BD
     */
    public int getId() {
        return ExtracteurHashMap.extraire_int(hashDonnees.get(nomID));
    }

    public void setId(int idExt) {
        inserer_valeur(nomID, idExt);
    }
/*

    public int getIdParent() {
        return ExtracteurHashMap.extraire_int(hashDonnees.get(nomIDParent));
    }

    public void setIdParent(int idExt) {
        inserer_valeur(nomIDParent, idExt);
    }
*/

    /**
     * Donne l'id du parent éventuel (sert pour les treeview par exemple)
     *
     * @return 0 sauf si la méthode a été overridée
     */
    public int getIdParent() {
        return 0;
    }

    public boolean isParent() {
        return estParent;
    }

    public void setParent(boolean parent) {
        estParent = parent;
    }

    /**
     * Indique si l'objet est modifié en regardant la liste des modifications
     *
     * @return vrai si "modifications" n'est pas vide
     */
    public boolean isModifie() {
        return !modifications.isEmpty();
    }

    public String getNom() {
        return ExtracteurHashMap.extraire_string(hashDonnees.get(nomNOM));
    }

    public final void setNom(String name) {
        inserer_valeur(nomNOM, name);
    }

    /**
     * Cette méthode vérifie d'abord que la valeur rentrée est du
     * type attendu avant d'effectuer l'insertion. Autrement, c'est ignoré !
     * Egalement ignoré si la valeur vaut NULL
     * Cette méthode modifie : hashDonnees et nonAttendus
     *
     * @param nom    le nom de la variable ; ne peut être vide
     * @param valeur la valeur à insérer; ne peut être null
     * @return vrai si succès, faux sinon
     */
    public Boolean inserer_valeur(String nom, Object valeur) {
        if (nom.isEmpty()) {
            System.err.println("** ObjetDi " + this + "  : L'insertion d'une clé vide pour la donnée <" +
                    valeur + "> n'est pas autorisé !");
            throw new UnsupportedOperationException("L'insertion d'une clé vide pour la donnée <" +
                    valeur + "> n'est pas autorisé !");
        }

        if (valeur instanceof ObjetDi || valeur instanceof HashMap) {
            throw new UnsupportedOperationException("!!! ObjetDi " + this + " /inserer_valeur : on ne peut inserer ce type ici : " + valeur +
                    " dans " + this + "\n" + "Il faut passer par setData !");
        }
        return inserer_prudemment(nom, valeur);

    }

    public boolean inserer_souplement_valeur(String cle, Object valeur) {
        Object ancienneValeur = get(cle);
        if (ancienneValeur == null && valeur != null) {
            Class classeVariable = getVariableClass(cle);
            if (classeVariable != null && classeVariable.equals(valeur.getClass())) {
                return inserer_valeur(cle, valeur);
            }
            if (classeVariable != null) {
                System.err.println("ObjetDi " + this + " /inserer_souplement_valeur : nouvelle valeur " + valeur +
                        ", non insérée car le type attendu est " + classeVariable.getSimpleName());
            }
            return false;
        }
        if (ancienneValeur != null && valeur != null &&
                ancienneValeur.getClass().equals(valeur.getClass())) {
            return inserer_valeur(cle, valeur);
        }
        if (ancienneValeur instanceof Float) {
            return inserer_valeur(cle, extraire_float(valeur));
        }
        if (ancienneValeur instanceof Integer) {
            return inserer_valeur(cle, extraire_int(valeur));
        }
        if (ancienneValeur instanceof LocalDate) {
            if (valeur instanceof LocalDate || valeur instanceof LocalDateTime) {
                return inserer_valeur(cle, valeur);
            }
            return inserer_valeur(cle, extraire_date(String.valueOf(valeur)));
        }
        if (ancienneValeur instanceof LocalDateTime) {
            if (valeur instanceof LocalDate) {
                return inserer_valeur(cle, ((LocalDate)valeur).atStartOfDay());
            }
            return inserer_valeur(cle, extraire_dateTime(String.valueOf(valeur)));
        }
        // conversion date vers string
        if (ancienneValeur instanceof String &&
                (valeur instanceof LocalDate || valeur instanceof LocalDateTime)) {
            String nouveauString = String.valueOf(valeur);
            return inserer_valeur(cle, nouveauString);
        }
        System.err.println("ObjetDi " + this + " /inserer_souplement : je n'ai pas pu insérer " + valeur + " dans " +
                "la clé " + cle + " (l'ancienne valeur était : " + ancienneValeur + ")");
        return false;
    }

    private float extraire_float(Object value) {
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private float extraire_int(Object value) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public abstract ObjetDi getObjetFromType(String nomType);


    /**
     * Permet de faire appel au clear des hashmaps,
     * puis recharge les variables nécessaires à tout hashobjet
     */
    public final void clear() {
        hashDonnees.clear();
        initialiser_objetDi();
        reinitialiser();
    }

    public abstract void reinitialiser();

    /**
     * Cette méthode permet d'initialiser/reinitialiser les variables
     *
     * @param nom    le nom de la variable ; ne peut être vide
     * @param valeur la valeur à insérer; ne peut être null
     */
    protected void initialiser(String nom, Object valeur) {
        if (nom.isEmpty()) {
            System.err.println("** ObjetDi " + this + " /initialiser : L'insersion de <" + valeur + "> dans une variable vide de l'objet " +
                    this + " n'est pas autorisé ! Demande ignorée !");
            return;
        }
        if (valeur == null && hashClassesDonnees.get(nom) == null) {
            throw new UnsupportedOperationException("ObjetDi/initialiser : on ne peut initialiser " +
                    "une variable (" + nom + ") à null sans avoir initialisé le type (setClass)");
        }

        hashDonnees.put(nom, valeur);
        if (valeur != null) {
            hashClassesDonnees.put(nom, valeur.getClass());
        }
    }

    public void setVariableClass(String nomVariable, Class classe) {
        hashClassesDonnees.put(nomVariable, classe);
    }

    public Class getVariableClass(String nomVariable) {
        return hashClassesDonnees.get(nomVariable);
    }

    /**
     * Cette méthode permet d'initialiser/reinitialiser les listes
     *
     * @param nom le nom de la variable ; ne peut être vide
     */
    protected void initialiser_liste(String nom) {
        if (nom.isEmpty()) {
            System.err.println("** ObjetDi " + this + "  : Initialisation d'une liste sans nom de variable se l'objet " +
                    this + ". Demande ignorée !");
            return;
        }

        hashListes.put(nom, new ArrayList<>());
    }

    /**
     * Cette méthode permet d'informer de la présence d'un sous-objet
     * identifié par son fk
     *
     * @param nomfk   le nom de la fk ; ne peut être vide
     * @param nomType le nom du type de variable dont l'id est le fk
     */
    public void setFK(String nomfk, String nomType) {
        ObjetDi sousObjet = getObjetFromType(nomType);
        if (sousObjet == null) {
            System.err.println("** ObjetDi " + this + "  : il n'existe pas de sous-objet au nom " + nomType +
                    " ! Demande ignorée !");
            return;
        }
        if (nomfk.isEmpty()) {
            System.err.println("** ObjetDi " + this + " /setFK : Le nomFK attribué à la table " + nomType + " est vide ! Demande ignorée !");
            return;
        }

        hashFKs.put(nomfk, sousObjet);
    }

    /**
     * Cette méthode permet d'informer de la présence d'un sous-objet
     * qui provient d'une table intermédiaire. Cet objet est complet lorque
     * le sous-objet intermediaire est également fourni !
     *
     * @param nomType le nom du type de variable dont l'id est le fk
     */
    public void setFKIntermediaire(String nomType) {
        ObjetDi sousObjet = getObjetFromType(nomType);
        if (sousObjet == null) {
            System.err.println("** ObjetDi " + this + " /setFKIntermediaire : il n'existe pas de sous-objet au nom " + nomType + "" +
                    " ! Demande ignorée !");
            return;
        }

        hashFKIntermediaire.put(sousObjet.getType(), sousObjet);
    }

    /**
     * Cette méthode permet d'informer de la présence d'un sous-objet
     * qui ne peut être instancié ici, car dépend de l'objet actuel.
     *
     * @param nomType le nom du type de variable dont à créer plus tard
     * @param nomFK   le nom de la variable dans l'objet parent
     */
    public void setDependant(String nomFK, String nomType) {
        if (nomFK.isEmpty()) {
            System.err.println("** ObjetDi " + this + " /setDependant : Le nomFK attribué à la table " + nomType + " est vide ! Demande ignorée !");
            return;
        }
        paires_NomsTables_NomsSpeciaux.put(nomType, nomFK);
        hashDependants.put(nomType, null);
    }


    /**
     * Réinitialise tous les sous-objets
     */
    public void clearSousObjets() {
        for (Map.Entry<String, ObjetDi> fks : hashFKs.entrySet()) {
            fks.getValue().reinitialiser();
        }
    }

    /**
     * Efface tous les sous-objets
     */
    public void deleteSousObjets() {
        hashFKs.clear();
    }

    /**
     * Permet d'insérer la valeur à la variable stockée
     * S'il n'existe pas de variable avec le nom indiqué, une nouvelle est créée
     * avec le type de l'objet valeur. Si le nom existe déjà, le type est vérifié
     *
     * @param nom    le nom de la variable
     * @param valeur la nouvelle valeur à attribuer
     * @return Vrai si tout s'est bien passé, false sinon
     */
    private boolean inserer_prudemment(String nom, Object valeur) {
        Object ancienneValeur = hashDonnees.get(nom);
        if (ancienneValeur == null) {
            ancienneValeur = hashDonneesNonAttendues.get(nom);
        }
        Object nouvelleValeur;

        Class classe = hashClassesDonnees.get(nom);
        if (ancienneValeur == null && classe != null) {
            if (valeur != null && valeur.getClass().equals(classe)) {
                inserer_directement(nom, valeur, null);
                return true;
            }
            System.err.println("** ObjetDi " + this + " /inserer_prudemment : le type de la nouvelle valeur de " + nom +
                    " de l'objet " + this
                    + " est : " + valeur + " alors que le type attendu est" +
                    " : " + classe.getSimpleName() + ". Insertion ignorée !");
            return false;
        }
        // adaptation de types
        // on reçoit un int alors qu'un booléen est attendu
        if (ancienneValeur instanceof Boolean &&
                valeur instanceof Integer) {
            int valeurInt = (int) valeur;
            nouvelleValeur = valeurInt != 0;
        }
        // on reçoit un long alors qu'un int est attendu
        else if (ancienneValeur instanceof Integer &&
                valeur instanceof Long) {
            nouvelleValeur = ((Long) valeur).intValue();
        }
        // on reçoit un float alors qu'un int est attendu
        else if (ancienneValeur instanceof Integer &&
                valeur instanceof Float) {
            nouvelleValeur = ((Float) valeur).intValue();
        }
        // idem pour les dates
        else if (ancienneValeur instanceof LocalDateTime &&
                valeur instanceof String) {
            nouvelleValeur = extraire_dateTime(String.valueOf(valeur));
        } else if (ancienneValeur instanceof LocalDate &&
                valeur instanceof String) {
            nouvelleValeur = extraire_date(String.valueOf(valeur));
        } else if (ancienneValeur instanceof LocalDateTime &&
                valeur instanceof LocalDate) {
            nouvelleValeur = ((LocalDate) valeur).atTime(0, 0);
        } else if (ancienneValeur instanceof LocalDate &&
                valeur instanceof LocalDateTime) {
            nouvelleValeur = ((LocalDateTime) valeur).toLocalDate();
        }
        // pour tous les autres
        else {
            nouvelleValeur = valeur;
        }

        if (nouvelleValeur != null && ancienneValeur != null &&
                !nouvelleValeur.getClass().getSimpleName().equals(ancienneValeur.getClass().getSimpleName())) {
            System.err.println("** ObjetDi " + this + " /inserer_prudemment : le type de la nouvelle valeur de " + nom +
                    " de l'objet " + this
                    + " est : " + nouvelleValeur.getClass().getSimpleName() + " alors que l'ancienne " +
                    "valeur est de type : " + ancienneValeur.getClass().getSimpleName() +
                    ". Insertion ignorée !");
            return false;
        }

        inserer_directement(nom, nouvelleValeur, ancienneValeur);

        return Boolean.TRUE;
    }

    private void inserer_directement(String nom, Object nouvelleValeur, Object ancienneValeur) {
        if (hashDonnees.containsKey(nom)) {
            hashDonnees.put(nom, nouvelleValeur);
        } else {
            hashDonneesNonAttendues.put(nom, nouvelleValeur);
        }
        if (nouvelleValeur != null) {
            hashClassesDonnees.put(nom, nouvelleValeur.getClass());
        }
        boolean egaux = (String.valueOf(nouvelleValeur).equals(String.valueOf(ancienneValeur)));
        if (!egaux) {
            addModification(nom);
        }
    }


    /**
     * renvoie une copie de la liste des variables remplies
     * mais non prévues dans le hashobjet
     *
     * @return l'hashMap de données non-attendues mais enregistrées quand même
     */
    public HashMap<String, Object> getNonAttendus() {
        return hashDonneesNonAttendues;
    }

    private LocalDateTime extraire_dateTime(String s) {
        String dateString = s;
        if (s.length() > 19) {
            dateString = s.substring(0, 19);
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            comment("Echec de conversion de dateTime pour " + this +
                    ". Je m'attendais à un format yyyy-MM-dd HH:mm:ss, mais " +
                    "j'ai : " + s + ", et je ne prends que les 19 premiers symboles", "err");
            return LocalDateTime.of(1900, 1, 1, 0, 0);
        }
    }

    private LocalDate extraire_date(String s) {
        String dateString = s;
        if (s.length() > 10) {
            dateString = s.substring(0, 10);
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            comment("Echec de conversion de dateTime pour " + this +
                    ". Je m'attendais à un format yyyy-MM-dd, mais " +
                    "j'ai : " + s, "err");
            return null;
        }
    }

    /**
     * Permet de dire si deux eltIHM sont égaux ou pas.
     * Les critères d'égalité sont (dans l'ordre) :
     * * le type (obligatoire)
     * * l'id (si ok, on regarde l'intermédiaire)
     * * l'id intermédiaire (ok si les deux id sont égaux)
     * * le nom, si les ids sont nuls, et les noms non-vides
     *
     * @param o l'autre eltIHM
     * @return vrai si les conditions précédentes sont vérifiées
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ObjetDi) {
            ObjetDi autre = (ObjetDi) o;
            boolean memeType = autre.getType().equals(getType());
            if (!memeType) {
                return false;
            }
            int monId = getId();
            int autreId = autre.getId();
            if (monId > 0 || autreId > 0) {
                return autreId == monId;
            }
            if (getNom().isEmpty() && autre.getNom().isEmpty()) {
                return true;
            }
            return getNom().equals(autre.getNom());
        }
        return super.equals(o);
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * L'id de l'objet actuel est fourni systématiquement !
     *
     * @param nomChamps ce sont les noms dont les valeurs sont à retourner
     * @return une hashmap de (nom, donnée)
     */
    public HashMap<String, Object> getData(ArrayList<String> nomChamps) {
        HashMap<String, Object> donnees = new HashMap<>();
        donnees.put(nomID, getId());
        nomChamps.forEach(cle -> {
            if (hashDonnees.containsKey(cle)) {
                donnees.put(cle, hashDonnees.get(cle));
            } else if (hashFKs.containsKey(cle)) {
                ObjetDi sousObjet = hashFKs.get(cle);
                int sonId = sousObjet.getId();
                donnees.put(cle, sonId);
                donnees.put(sousObjet.getType() + "-" + sonId, sousObjet.getData(nomChamps));
            } else if (hashFKIntermediaire.containsKey(cle)) {
                ObjetDi objetIntermediaire = hashFKIntermediaire.get(cle);
                int sonId = objetIntermediaire.getId();
                donnees.put(cle, sonId);
                donnees.put(objetIntermediaire.getType() + "-" + sonId, objetIntermediaire.getData(nomChamps));
            } else if (hashDependants.containsKey(cle)) {
                ObjetDi objetEtranger = hashDependants.get(cle);
                if (objetEtranger != null) {
                    int sonId = objetEtranger.getId();
                    donnees.put(cle, objetEtranger.getId());
                    donnees.put(objetEtranger.getType() + "-" + sonId, objetEtranger.getData(nomChamps));
                }
            } else if (hashListes.containsKey(cle)) {
                ArrayList<ObjetDi> liste = hashListes.get(cle);
                ArrayList<Object> hashmaps = new ArrayList<>();
                liste.forEach(objet -> {
                    hashmaps.add(objet.getData(nomChamps));
                });
                donnees.put(cle, hashmaps);
            }
        });
        donnees.putAll(hashDonneesNonAttendues);
        return donnees;
    }


    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * Un sous-élément éventuel sera également fourni sous forme de hashmap
     *
     * @return une hashmap de (nom, donnée)
     */
    public HashMap<String, Object> getAllData() {
        HashMap<String, Object> donnees = new HashMap<>();
        // les données de base
        donnees.put("base", hashDonnees);

        // les données non-attendues
        donnees.put("non-attendus", hashDonneesNonAttendues);

        // fks
        HashMap<String, HashMap<String, Object>> fks = new HashMap<>();
        hashFKs.forEach((nomFk, sousObjet) -> {
            fks.put(nomFk, sousObjet.getAllData());
        });
        gestionnairesImbrications.forEach((nomFkQuartier, gestionnaire) -> {
            fks.put(nomFkQuartier, gestionnaire.getQuartier().getAllData());
        });
        donnees.put("fks", new HashMap<>(fks));

        // intermediaires
        HashMap<String, HashMap<String, Object>> intermediaires = new HashMap<>();
        hashFKIntermediaire.forEach((nomTable, sousObjet) -> {
            intermediaires.put(nomTable, sousObjet.getAllData());
        });
        donnees.put("intermediaires", new HashMap<>(intermediaires));

        // étrangers
        donnees.put("dependants", new HashMap<>(hashDependants));

        // listes
        HashMap<String, ArrayList<HashMap<String, Object>>> hashmaps = new HashMap<>();
        hashListes.forEach((nomTable, objetsDi) -> {
            ArrayList<HashMap<String, Object>> listeHashmaps = new ArrayList<>();
            objetsDi.forEach(objetDi -> {
                listeHashmaps.add(objetDi.getAllData());
            });
            hashmaps.put(nomTable, listeHashmaps);
        });
        donnees.put("listes", new HashMap<>(hashmaps));

        return donnees;
    }


    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * Seuls les champs, les fk et la jointure intermédiaire sont fournis !
     *
     * @return une hashmap de (nom, donnée)
     */
    public HashMap<String, Object> getData() {
        // les données de base
        HashMap<String, Object> donnees = new HashMap<>(hashDonnees);
        donnees.putAll(hashDonneesNonAttendues);

        // fks
        hashFKs.forEach((nomFk, sousObjet) -> {
            donnees.put(nomFk, sousObjet.getId());
        });
        // imbrications
        gestionnairesImbrications.forEach((nomFkQuartier, gestionnaire) -> {
            if (gestionnaire.getQuartier() != null) {
                donnees.put(nomFkQuartier, gestionnaire.getQuartier().getId());
            }
        });

        // intermediaires
        hashFKIntermediaire.forEach((nomTable, sousObjet) -> {
            donnees.putAll(sousObjet.getData());
        });

        // étrangers
        hashDependants.forEach((nomTable, sousObjet) -> {
            if (sousObjet != null) {
                donnees.putAll(sousObjet.getData());
            }
        });

        // listes
        hashListes.forEach((nomTable, liste) -> {
            ArrayList<HashMap<String, Object>> hashMaps = new ArrayList<>();
            liste.forEach(objetDi -> {
                hashMaps.add(objetDi.getSimpleData());
            });
            donnees.put(nomTable, hashMaps);
        });

        return donnees;
    }


    /**
     * Permet de charger les variables àpd'une liste de (nom, donnée). Si une
     * variable attendue doit être modifiée alors que le type n'est pas bon,
     * cette insertion sera ignorée. Si une variable nouvelle apparaît, elle
     * sera rajoutée à la liste des variables.
     * Seront affectés : hashDonnees, hashFKIntermediaire et hashFK.
     * Il vaut mieux vérifier le contenu de l'objet après cette opération.
     *
     * @param input       l'hashmap de données à rentrer comme variables
     * @param toutPrendre si oui, même les données non attendues sont prises
     * @param hashLevis   C'est la liste de détails selon la structure csa-soft
     * @param verbose     On affiche des commentaires ?
     * @return la liste des noms qui ont trouvé preneur
     */
    public ArrayList<String> setData(HashMap<String, Object> input,
                                     final HashMap<String, Object> hashLevis,
                                     boolean toutPrendre,
                                     boolean verbose) {
        boolean hashLevisFourni = hashLevis != null && !hashLevis.isEmpty();
        final HashMap<String, Object> hashDetails;
        hashDetails = hashLevis == null ? new HashMap<>() : new HashMap<>(hashLevis);
        // rajoute les données principales dans les secondaires (car une fois en profondeur,
        // on perd les données principales)
        int idNouveau = ExtracteurHashMap.extraire_int(input.get(this.nomID));
        if (hashLevisFourni && idNouveau > 0) {
            String cle = getType() + "-" + idNouveau;
            if (!hashLevis.containsKey(cle)) {
                ArrayList<Object> liste = new ArrayList<>();
                liste.add(getType());
                liste.add(extraire_donnees_attendues(input));
                hashDetails.put(cle, liste);
                if (verbose) System.out.println("Rajout dans le 3e élément => (" + cle + ", " + liste + ")");
            }
        }

        ArrayList<String> clesUtilisees = new ArrayList<>();
        HashMap<String, Object> reducedInput = new HashMap<>(input);

        // charité bien ordonnée commence par soi-même
        input.forEach((nom, valeur) -> {
            if (hashDonnees.containsKey(nom)) {
                inserer_valeur(nom, valeur);
                reducedInput.remove(nom);
                clesUtilisees.add(nom);
            }
        });

        // fks
        for (Map.Entry<String, ObjetDi> paire : hashFKs.entrySet()) {
            String nomFk = paire.getKey();
            ObjetDi sousObjet = paire.getValue();
            Object hypoId = reducedInput.get(nomFk);
            if (hypoId instanceof Integer) {
                int id = (Integer) hypoId;
                sousObjet.setId(id);
                if (hashLevisFourni && id > 0) {
                    sousObjet.setData(extraire_fk(sousObjet.getType(), id, hashDetails),
                            hashDetails, false, verbose);
                }
                reducedInput.remove(nomFk);
                clesUtilisees.add(nomFk);
            }
        }

        // fk intermédiaires après les fk car s'il y a des noms similaires, il faut
        // donner la priorité à l'objet principal
        hashFKIntermediaire.forEach((nomTable, sousObjet) -> {
            clesUtilisees.addAll(sousObjet.setData(reducedInput, hashDetails, false, verbose));
            clesUtilisees.forEach(reducedInput::remove);
        });
        // objets étrangers (non initialisables)
        for (Map.Entry<String, String> paire : paires_NomsTables_NomsSpeciaux.entrySet()) {
            String nomTable = paire.getKey();
            String nomSpecial = paire.getValue();
            Object hypothese = reducedInput.get(nomSpecial);
            if (hypothese instanceof Integer && ((int) hypothese) > 0) {
                int id = (int) hypothese;
                String cleLevis = nomTable + "-" + id;
                Object hypoLevis = hashLevisFourni ? hashDetails.get(cleLevis) : null;
                if (hypoLevis instanceof ArrayList) {
                    clesUtilisees.add(nomSpecial);
                    reducedInput.remove(nomSpecial);
                    ArrayList<Object> listeLevis = (ArrayList<Object>) hypoLevis;
                    if (listeLevis.size() > 1) {
                        String nomTableLevis = String.valueOf(listeLevis.get(0));
                        ObjetDi sousObjet = getObjetFromType(nomTable);
                        if (nomTableLevis.equals(nomTable) && listeLevis.get(1) instanceof HashMap) {
                            clesUtilisees.addAll(sousObjet.setData((HashMap<String, Object>) listeLevis.get(1),
                                    null, toutPrendre, verbose));
                            hashDependants.put(nomTable, sousObjet);
                        } else {
                            comment("setData/étrangers (" + nomTable + ", " + nomSpecial + ") : Trouvé un détail liste " + cleLevis +
                                            " dont le nom " + nomTableLevis + " ne correspond pas à " + nomTable + " " +
                                            "suivi de hashmap. J'obtiens : " + listeLevis,
                                    "err");
                        }
                    } else {
                        comment("setData/étrangers (" + nomTable + ", " + nomSpecial + "): Trouvé un détail liste " + cleLevis + " non conforme : " + listeLevis,
                                "err");
                    }
                } else {
                    comment("setData/étrangers (" + nomTable + ", " + nomSpecial + ") : Trouvé un détail liste " + cleLevis + " qui n'est pas une liste ! => " + hypoLevis,
                            "err");
                }
            }
        }

        // listes
        for (Map.Entry<String, ArrayList<ObjetDi>> paire : hashListes.entrySet()) {
            String nomTable = paire.getKey();
            ArrayList<ObjetDi> anciensObjets = paire.getValue();
            int monId = getId();
            String monType = getType();
            if (hashLevisFourni) {
                String cleLevis = monType + "_" + nomTable + "_" + monId;
                Object objetLevis = hashDetails.get(cleLevis);
                if (objetLevis != null) {
                    if (objetLevis instanceof ArrayList &&
                            !((ArrayList<?>) objetLevis).isEmpty() &&
                            ((ArrayList<?>) objetLevis).get(0) instanceof String) {
                        ArrayList<Object> listeLevis = (ArrayList<Object>) objetLevis;
                        String nomTableLevis = String.valueOf(listeLevis.get(0));
                        if (nomTableLevis.equals(nomTable)) {
                            ArrayList<Object> listeRecue = new ArrayList<>(listeLevis.subList(1, listeLevis.size()));
                            ArrayList<ObjetDi> arrayList = transformer_liste(nomTable, listeRecue, hashDetails);
                            anciensObjets.clear();
                            anciensObjets.addAll(arrayList);
                            clesUtilisees.add(nomTable);
                        } else {
                            comment("setData : Trouvé un détail liste " + cleLevis +
                                            " qui est une liste de " + nomTableLevis + " et non  " + nomTable,
                                    "err");
                        }
                    } else {
                        comment("setData : Trouvé un détail liste " + cleLevis +
                                        " qui n'est pas une liste dont le 1er elt est un String, mais => " + objetLevis,
                                "err");
                    }
                } else {
                    if (monId > 0) {
                        comment("setData : Pas trouvé la liste " + cleLevis +
                                        " dans l'élément 3 (hashLevis)",
                                "err");
                    }
                }
            }
        }

        // gestionnaires d'imbrication
        // les infos se trouvent toujours dans la hashLevis
        for (Map.Entry<String, GestionnaireImbrications> paire : gestionnairesImbrications.entrySet()) {
            String nomFk = paire.getKey();
            GestionnaireImbrications gestionnaire = paire.getValue();
            Object fk = reducedInput.get(nomFk);
            if (fk instanceof Integer &&
                    ((int) fk) > 0) {
                gestionnaire.remplir_donnees((int) fk, hashDetails);
                reducedInput.remove(nomFk);
                clesUtilisees.add(nomFk);
            }
        }

        if (toutPrendre) {
            hashDonneesNonAttendues.putAll(reducedInput);
        }
        if (verbose) {
            ecrire_rapport(input, clesUtilisees);
        }
        return clesUtilisees;
    }

    private HashMap<String, Object> extraire_donnees_attendues(HashMap<String, Object> input) {
        HashMap<String, Object> terug = new HashMap<>();
        input.forEach((cle, valeur) -> {
            if (hashDonnees.containsKey(cle)) {
                terug.put(cle, valeur);
            }
        });
        return terug;
    }


    /**
     * Permet de charger les variables àpd'une liste de (nom, donnée). Si une
     * variable attendue doit être modifiée alors que le type n'est pas bon,
     * cette insertion sera ignorée. Si une variable nouvelle apparaît, elle
     * sera rajoutée à la liste des variables.
     * Seront affectés : hashDonnees, hashFKIntermediaire et hashFK.
     * Il vaut mieux vérifier le contenu de l'objet après cette opération.
     *
     * @param toutPrendre si oui, même les données non attendues sont prises
     * @param hashLevis   C'est la liste de détails selon la structure csa-soft
     * @param verbose     On affiche des commentaires ?
     * @param id          c'est l'id du détail à chercher dans la hashLevis. La clé de recherche sera ceType-id
     * @return la liste des noms qui ont trouvé preneur
     */
    public ArrayList<String> setDataFromHashLevis(HashMap<String, Object> hashLevis,
                                                  int id,
                                                  boolean toutPrendre,
                                                  boolean verbose) {
        if (hashLevis == null || hashLevis.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<String> clesUtilisees = new ArrayList<>();
        String cle = getType() + "-" + id;
        Object details = hashLevis.get(cle);
        if (details instanceof ArrayList && ((ArrayList<?>) details).size() > 1) {
            ArrayList<Object> biliste = (ArrayList<Object>) details;
            String nomLevis = String.valueOf(biliste.get(0));
            if (nomLevis.equals(getType())) {
                if (biliste.get(1) instanceof HashMap) {
                    HashMap<String, Object> donnees = (HashMap<String, Object>) biliste.get(1);
                    return setData(donnees, hashLevis, toutPrendre, verbose);
                }
                comment("setDataFromHashLevis : le contenu de la clé : " + cle + " est mal formaté. J'attends : "
                                + " (nomTable, HashMap), mais j'obtiens : " + biliste,
                        "err");
                return clesUtilisees;
            }
            comment("setDataFromHashLevis : la clé : " + cle + " ne contient pas le type " + getType() + ", " +
                            "mais le type " + nomLevis,
                    "err");
            return clesUtilisees;
        }
        if (id > 0) {
            comment("setDataFromHashLevis : Pas de détail trouvé pour la clé : " + cle, "err");
        }
        return clesUtilisees;
    }

    private void ecrire_rapport(HashMap<String, Object> hashMap, ArrayList<String> clesUtilisees) {
        /*comment("\n***Rapport d'enregistrement de données de : " + this, "out");
        comment("Liste des éléments non attendus : " + hashDonneesNonAttendues.keySet(), "out");
        ArrayList<String> nonUtilises = new ArrayList<>(hashMap.keySet());
        clesUtilisees.forEach(nonUtilises::remove);
        comment("Liste des données qui n'ont pas trouvé preneur : " + nonUtilises, "out");
        comment("*** Fin Rapport", "out");*/
    }

    private ArrayList<ObjetDi> transformer_liste(String nomTable,
                                                 ArrayList<Object> listeRecue,
                                                 HashMap<String, Object> hashLevis) {
        ArrayList<ObjetDi> terug = new ArrayList<>();
        for (Object elt : listeRecue) {
            if (elt instanceof HashMap && !(((HashMap) elt).isEmpty())) {
                ObjetDi objetDi = getObjetFromType(nomTable);
                objetDi.setData((HashMap<String, Object>) elt, hashLevis, false, true);
                terug.add(objetDi);
            } else if (elt instanceof ObjetDi) {
                terug.add((ObjetDi) elt);
            }
        }
        return terug;
    }

    /**
     * Permet de voir si l'objet actuel contient un fk du type rentré
     *
     * @return null s'il ne trouve pas ou le nomFK du type rentré
     */
    public String getNomFK(String nomType) {
        for (Map.Entry<String, ObjetDi> paireFK : hashFKs.entrySet()) {
            if (paireFK.getValue().getType().equals(nomType)) {
                return paireFK.getKey();
            }
        }
        return null;
    }

    private ArrayList<ObjetDi> transformer_en_objet(String nomTable, ArrayList<?> hypoListe) {
        ArrayList<ObjetDi> terug = new ArrayList<>();
        hypoListe.forEach(elt -> {
            if (elt instanceof ObjetDi) {
                terug.add((ObjetDi) elt);
            } else if (elt instanceof HashMap) {
                ObjetDi objetDi = getObjetFromType(nomTable);
                terug.add(objetDi);
            }
        });
        return terug;
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * Seuls les champs sont fournis ; pas les fk ni même les intermédiaires !
     *
     * @return une hashmap de (nom, donnée)
     */
    public HashMap<String, Object> getSimpleData() {
        HashMap<String, Object> terug = new HashMap<>(hashDonnees);
        terug.putAll(hashDonneesNonAttendues);
        return terug;
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * Seuls les ids sont fournis ; pas les fk ni même les intermédiaires !
     *
     * @return une hashmap de (nom, donnée)
     */
    public HashMap<String, Object> getOnlyIDs() {
        HashMap<String, Object> terug = new HashMap<>();
        terug.put(this.nomID, getId());
        hashFKIntermediaire.forEach((nom, sousObjet) -> {
            terug.put(sousObjet.nomID, sousObjet.getId());
        });
        return terug;
    }

    private HashMap<String, Object> extraire_fk(String nomTable, int id,
                                                HashMap<String, Object> hashLevis) {
        String cle = nomTable + "-" + id;
        Object objetLevis = hashLevis.get(cle);
        if (objetLevis == null) {
            comment("extraire_Fk : Le détail de " + cle + " n'est pas fourni !", "err");
            return new HashMap<>();
        }
        if (objetLevis instanceof ArrayList &&
                ((ArrayList<?>) objetLevis).size() > 1) {
            String nomTableLevis = String.valueOf(((ArrayList<?>) objetLevis).get(0));
            if (nomTableLevis.equals(nomTable) &&
                    ((ArrayList<?>) objetLevis).get(1) instanceof HashMap) {
                return (HashMap<String, Object>) ((ArrayList<?>) objetLevis).get(1);
            }
            if (id > 0) {
                comment("extraire_fk : Mauvais nom de table pour détail de " + cle + ". J'attends " + nomTable +
                        "+ hashmap, mais j'obtiens : " + objetLevis, "err");
            }
            return new HashMap<>();
        }
        if (id > 0) {
            comment("extraire_fk : Mauvais format pour détail de " + cle + ". J'attends (nomTable,hasmap), " +
                    "mais j'obtiens : " + objetLevis, "err");
        }
        return new HashMap<>();
    }


    public ObjetDi getCopy() {
        ObjetDi clone = getNewInstance();
        clone.hashDonnees.putAll(hashDonnees);
        clone.hashClassesDonnees.putAll(hashClassesDonnees);
        clone.hashDonneesNonAttendues.putAll(hashDonneesNonAttendues);
        clone.hashListes.putAll(hashListes);
        clone.hashDependants.putAll(hashDependants);
        clone.paires_NomsTables_NomsSpeciaux.putAll(paires_NomsTables_NomsSpeciaux);
        clone.hashFKIntermediaire.putAll(hashFKIntermediaire);
        clone.hashFKs.putAll(hashFKs);
        clone.mandatories.addAll(mandatories);
        // les modifications ne sont pas copiées
        return clone;
    }


    /**
     * Sert à récupérer les données modifiées de l'objet courant Sous forme de
     * paires (nomBD, valeur)
     * L'ID et les champs intermédiaires sont fournis également !
     * Les listes internes ne sont pas fournies, les sous-objets non plus !
     *
     * @param stricte si oui, les mandatories ne sont pas fournis
     * @return une hashmap de (nom, donnéee) correspondant aux modifications
     */
    public HashMap<String, Object> getHashModifications(boolean stricte) {
        HashMap<String, Object> modifs = new HashMap<>();
        ArrayList<String> mix = new ArrayList<>(modifications);
        if (!stricte) {
            mix.addAll(mandatories);
        }
        // données d'imbrication (localités)
        for (Map.Entry<String, GestionnaireImbrications> paire : gestionnairesImbrications.entrySet()) {
            GestionnaireImbrications gestionnaireImbrication = paire.getValue();
            String nomLePlusBas = paire.getKey();
            if (mandatories.contains(nomLePlusBas) || gestionnaireImbrication.aChange()) {
                modifs.put(nomLePlusBas, gestionnaireImbrication.getFk());
            }
        }

        for (String nom : mix) {
            Object valeur = hashDonnees.get(nom);
            if (valeur != null) {
                modifs.put(nom, valeur);
            } else {
                // données reçues, mais non attendues
                valeur = hashDonneesNonAttendues.get(nom);
                if (valeur != null && !modifs.containsKey(nom)) {
                    modifs.put(nom, valeur);
                }
            }
        }
        // modifications observées dans les sous-objets
        for (Map.Entry<String, ObjetDi> paireIntermediaire : hashFKIntermediaire.entrySet()) {
            ObjetDi sousObjetIntermediaire = paireIntermediaire.getValue();
            // idintermédiaire obligatoire !
            modifs.putAll(sousObjetIntermediaire.getHashModifications(stricte));
        }
        // FKs
        for (Map.Entry<String, ObjetDi> paire : hashFKs.entrySet()) {
            ObjetDi sousObjet = paire.getValue();
            String cleFK = paire.getKey();
            if (mix.contains(cleFK)) {
                modifs.put(cleFK, sousObjet.getId());
                if (!sousObjet.getNomsModifications().isEmpty()) {
                    modifs.put(sousObjet.getType(), sousObjet.getHashModificationsSimple());
                }
            }
        }
        // sous-objets dépendants (non initialisables)
        for (Map.Entry<String, ObjetDi> paireEtrangere : hashDependants.entrySet()) {
            ObjetDi sousObjet = paireEtrangere.getValue();
            String nomTable = paireEtrangere.getKey();
            if (sousObjet != null) {
                String cleSpeciale = paires_NomsTables_NomsSpeciaux.get(nomTable);
                if (mix.contains(nomTable)) {
                    modifs.put(cleSpeciale, sousObjet.getId());
                    if (!sousObjet.getNomsModifications().isEmpty()) {
                        modifs.put(sousObjet.getType(), sousObjet.getHashModificationsSimple());
                    }
                }
            }
        }
        // listes de sous-objets
        for (Map.Entry<String, ArrayList<ObjetDi>> paire : hashListes.entrySet()) {
            ArrayList<ObjetDi> sousObjets = paire.getValue();
            String nomTable = paire.getKey();
            ArrayList<HashMap<String, Object>> sousListe = new ArrayList<>();
            if (mix.contains(nomTable)) {
                sousObjets.forEach(sousObjet -> {
                    if (!sousObjet.getNomsModifications().isEmpty()) {
                        sousListe.add(sousObjet.getHashModificationsSimple());
                    }
                });
            }
            if (!sousListe.isEmpty()) {
                modifs.put(nomTable, sousListe);
            }
        }

        modifs.put(nomID, getId());
        return modifs;
    }

    /**
     * renvoie des données plus light que getHashModifications
     *
     * @return les mandatories sont traités, mais il n'y a pas de listes et les
     * sous objets ont juste leur id, sans la hashmap du sous-objet
     */
    private HashMap<String, Object> getHashModificationsSimple() {
        HashMap<String, Object> modifs = new HashMap<>();
        ArrayList<String> mix = new ArrayList<>(modifications);
        mix.addAll(mandatories);

        for (String nom : mix) {
            Object valeur = hashDonnees.get(nom);
            if (valeur != null) {
                modifs.put(nom, valeur);
            } else {
                // données reçues, mais non attendues
                valeur = hashDonneesNonAttendues.get(nom);
                if (valeur != null) {
                    modifs.put(nom, valeur);
                }
            }
        }
        // modifications observées dans les sous-objets
        for (Map.Entry<String, ObjetDi> paireIntermediaire : hashFKIntermediaire.entrySet()) {
            ObjetDi sousObjetIntermediaire = paireIntermediaire.getValue();
            // idintermédiaire obligatoire !
            modifs.putAll(sousObjetIntermediaire.getHashModificationsSimple());
        }
        // FKs
        for (Map.Entry<String, ObjetDi> paire : hashFKs.entrySet()) {
            ObjetDi sousObjet = paire.getValue();
            String cleFK = paire.getKey();
            if (!sousObjet.getNomsModifications().isEmpty()) {
                modifs.put(cleFK, sousObjet.getId());
            }
        }
        // sous-objets dépendants (non initialisables)
        for (Map.Entry<String, ObjetDi> paireEtrangere : hashDependants.entrySet()) {
            ObjetDi sousObjet = paireEtrangere.getValue();
            if (sousObjet != null) {
                String cleFK = paires_NomsTables_NomsSpeciaux.get(paireEtrangere.getKey());
                if (!sousObjet.getNomsModifications().isEmpty()) {
                    modifs.put(cleFK, sousObjet.getId());
                }
            }
        }

        modifs.put(nomID, getId());
        return modifs;
    }


    public void setModifications(Collection<String> modifs) {
        modifications.clear();
        modifications.addAll(modifs);
    }

    /**
     * Fournit une copie de la liste des noms des variables obligatoires
     *
     * @return une copie de la liste des noms des champs obligatoires
     */
    public LinkedHashSet<String> getomsMandatories() {
        return new LinkedHashSet<>(mandatories);
    }

    /**
     * Fournit une copie de la liste des noms des variables dont la valeur a changé
     *
     * @return une copie de la liste des noms des champs modifiés
     */
    public LinkedHashSet<String> getNomsModifications() {
        return new LinkedHashSet<>(modifications);
    }

    /**
     * Retourne la liste des données obligatoires
     *
     * @return les données toujours fournies même si pas de modification
     */
    public HashMap<String, Object> getHashMandatories() {
        HashMap<String, Object> obliges = new HashMap<>();
        mandatories.forEach(cle -> {
            if (hashDonnees.containsKey(cle)) {
                obliges.put(cle, hashDonnees.get(cle));
            } else if (hashFKs.containsKey(cle)) {
                obliges.put(cle, (hashFKs.get(cle)).getId());
            } else {
                for (ObjetDi objetIntermediaire : hashFKIntermediaire.values()) {
                    Object match = objetIntermediaire.hashDonnees.get(cle);
                    if (match != null) {
                        obliges.put(cle, match);
                    }
                }
            }
        });
        obliges.putAll(hashDonneesNonAttendues);

        obliges.put(nomID, getId());
        return obliges;
    }

    public void setMandatories(ArrayList<String> obligatoires) {
        mandatories = new LinkedHashSet<>(obligatoires);
    }


    @Override
    public String toString() {
        return getNom().toUpperCase() + " (" + getType() + " - " + getId() + ")";
    }

    public void clearModifications() {
        modifications.clear();
        hashFKIntermediaire.forEach((nom, sousObjet) -> {
            sousObjet.clearModifications();
        });
        hashFKs.forEach((nom, sousObjet) -> {
            sousObjet.clearModifications();
        });
        hashDependants.forEach((nom, sousObjet) -> {
            if (sousObjet != null) {
                sousObjet.modifications.clear();
            }
        });
        hashListes.forEach((nom, liste) -> {
            for (ObjetDi sousObjet : liste) {
                sousObjet.clearModifications();
            }
        });
    }

    public void clearMandatories() {
        mandatories.clear();
    }

    /**
     * Rajoute une variable dont le contenu sera considéré comme modifié Si un
     * nomBD est utilisé, alors il sera remplacé par le nom réel de la variable
     *
     * @param nom nom de la variable modifiée
     */
    public void addModification(String nom) {
        modifications.add(nom);
    }

    public boolean removeModification(String nom) {
        return modifications.remove(nom);
    }

    public boolean removeMandatory(String nom) {
        return mandatories.remove(nom);
    }

    /**
     * Rajoute une variable dont le contenu sera considéré comme obligé
     *
     * @param nom nom de la variable modifiée
     */
    public void addMandatory(String nom) {
        mandatories.add(nom);
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ObjetDi) {
            int compareNom = toString().compareTo(o.toString());
            if (compareNom == 0) {
                return getId() - ((ObjetDi) o).getId();
            }
            return compareNom;
        }
        return 0;
    }

    public String getNomPublic(String name) {
        String reponse = nomsPublics.get(name);
        return reponse == null ? "*" : reponse;
    }

    /**
     * renvoie une COPIE de la liste des variables à afficher
     *
     * @return une copie des variables visibles
     */
    public ArrayList<String> getVariablesVisibles() {
        return new ArrayList<>(variablesVisibles);
    }

    /**
     * Créé une liste de variables visibles. Attention : un filtre est effectué
     * par rapport aux variables existantes
     *
     * @param visibles la liste de variables à rendre visibles
     */
    public void setVariablesVisibles(ArrayList<String> visibles) {
        variablesVisibles.clear();
        for (String elt : visibles) {
            if (hashDonnees.containsKey(elt)) {
                variablesVisibles.add(elt);
            }
        }
    }

    public HashMap<String, ObjetDi> getFKs() {
        return new HashMap<>(hashFKs);
    }

    public HashMap<String, ObjetDi> getFKIs() {
        return new HashMap<>(hashFKIntermediaire);
    }

    public HashMap<String, Object> getHashListes() {
        return new HashMap<>(hashListes);
    }

    public HashMap<String, ObjetDi> getHashDependants() {
        return new HashMap<>(hashDependants);
    }

    public HashMap<String, GestionnaireImbrications> getGestionnairesImbrications() {
        return new HashMap<>(gestionnairesImbrications);
    }

    /**
     * renvoie une COPIE de la liste des paires (nom, nom public)
     *
     * @return
     */
    public HashMap<String, String> getListeNomsPublics() {
        return new HashMap<>(nomsPublics);
    }

    public boolean add_nom_public(String nomVariable, String nomPublic) {
        if (hashDonnees.containsKey(nomVariable)) {
            nomsPublics.put(nomVariable, nomPublic);
            return true;
        }
        for (Map.Entry<String, ObjetDi> couple : hashFKIntermediaire.entrySet()) {
            ObjetDi fk = couple.getValue();
            if (fk.getSimpleData().containsKey(nomVariable)) {
                nomsPublics.put(nomVariable, nomPublic);
                return true;
            }
        }
        // Faut pas aller à l'intérieur des fks à mon avis !
        /*for (Map.Entry<String, ObjetDi> couple : hashFKs.entrySet()) {
            ObjetDi fk = couple.getValue();
            if (fk.getSimpleData().containsKey(nomVariable)) {
                nomsPublics.put(nomVariable, nomPublic);
                return true;
            }
        }*/
        if (hashFKs.containsKey(nomVariable)) {
            nomsPublics.put(nomVariable, nomPublic);
            return true;
        }
        for (Map.Entry<String, GestionnaireImbrications> couple : gestionnairesImbrications.entrySet()) {
            GestionnaireImbrications gestionnaireImbrications = couple.getValue();
            String nomID = couple.getKey();
            if (nomID.equals(nomVariable)) {
                nomsPublics.put(nomVariable, nomPublic);
                return true;
            }
        }

        System.err.println("** ObjetDi " + this + " : nom public <" + nomPublic + "> non attribué car variable <" + nomVariable +
                "> inconnue !");
        return false;
    }

    public void add_variable_visible(String nomVariable) {
        if (hashDonnees.containsKey(nomVariable)) {
            ajouter_variable(nomVariable, variablesVisibles);
            return;
        }
        for (Map.Entry<String, ObjetDi> couple : hashFKIntermediaire.entrySet()) {
            ObjetDi fk = couple.getValue();
            if (fk.getSimpleData().containsKey(nomVariable)) {
                ajouter_variable(nomVariable, variablesVisibles);
                return;
            }
        }
        for (Map.Entry<String, ObjetDi> couple : hashFKs.entrySet()) {
            ObjetDi fk = couple.getValue();
            if (fk.getSimpleData().containsKey(nomVariable)) {
                ajouter_variable(nomVariable, variablesVisibles);
                return;
            }
        }
        for (Map.Entry<String, GestionnaireImbrications> couple : gestionnairesImbrications.entrySet()) {
            GestionnaireImbrications gestionnaireImbrications = couple.getValue();
            String nomID = couple.getKey();
            if (nomID.equals(nomVariable)) {
                ajouter_variable(nomVariable, variablesVisibles);
                return;
            }
        }
    }

    private void ajouter_variable(String nomVariable, ArrayList<String> arrayList) {
        arrayList.remove(nomVariable);
        arrayList.add(nomVariable);
    }

    public Class getTypeOfVariable(String nomVariable) {
        Class classe = hashClassesDonnees.get(nomVariable);

        if (classe == null) {
            System.err.println("** ObjetDi " + this + " /getTypeOfVariable : le type de variable est inconnu pour " + nomVariable +
                    ". Je le remplace par Object");
            return Object.class;
        }
        return classe;
    }

    /**
     * Donne la valeur stockée dans le nom de variable rentré
     * Si pas trouvé dans les données simples, il y a une recherche dans les fk
     * Si toujours pas trouvé, il recherche des listes, des objetDi dépendants, etc...
     *
     * @param nomVariable c'est le nom de variable dont on veut le contenu, s'il existe
     * @return null si pas trouvé; autrement, on peut avoir un primitif, un objetDi ou une liste !
     */
    public Object getData(String nomVariable) {
        Object objet = hashDonnees.get(nomVariable);
        if (objet != null) {
            return objet;
        }
        // au cas où la variable serait null elle même!
        if (hashDonnees.containsKey(nomVariable)) {
            return null;
        }
        // fks
        ObjetDi objetDi = getObjet(nomVariable);
        if (objetDi != null) {
            return objetDi;
        }
        // fk intermédiaire
        ObjetDi objetIntermediaire = getObjetIntermediaire(nomVariable);
        if (objetIntermediaire != null) {
            return objetIntermediaire;
        }
        // objet dépendant
        ObjetDi objetDependant = getObjetDependant(nomVariable);
        if (objetDependant != null) {
            return objetDependant;
        }
        // liste
        return getListe(nomVariable);
    }

    /**
     * Indique si le nom de variable est connu dans cet objetDi
     *
     * @param nomVariable c'est le nom de variable dont on veut le contenu, s'il existe
     * @return si trouvé une telle variable dans les variables et sous-objets
     */
    public boolean variable_existe(String nomVariable) {
        if (hashClassesDonnees.containsKey(nomVariable)) {
            return true;
        }
        ObjetDi objetDi = getObjet(nomVariable);
        if (objetDi != null) {
            return true;
        }
        ObjetDi objetIntermediaire = getObjetIntermediaire(nomVariable);
        if (objetIntermediaire != null) {
            return true;
        }
        ObjetDi objetDependant = getObjetDependant(nomVariable);
        if (objetDependant != null) {
            return true;
        }
        ArrayList liste = getListe(nomVariable);
        return liste != null;
    }


    /**
     * Permet de modifier le nom d'une variable.
     * Le contenu est reporté à la nouvelle variable.
     * Il faut que l'ancien existe déjà et que le nouveau soit non vide
     *
     * @param ancien  l'ancien nom
     * @param nouveau le nouveau nom qui doit être non vide, sinon IGNORÉ !
     * @return vrai si l'opération réussit
     */
    public boolean changer_nom_variable(String ancien, String nouveau) {
        if (nouveau.isEmpty() || ancien.equals(nouveau)) {
            return false;
        }
        Object contenu = remove(ancien);
        if (contenu == null) {
//            System.out.println("** HashObjet/changer_nom_variable : impossible de changer " +
//                    "le nom de de la variable : " + ancien + " n'existe pas dans " + getType() + " !");
            return false;
        }
        initialiser(nouveau, contenu);
        // remplacer les nom stockés dans les listes : mandatories, modifs, etc
        remplacer_noms_stockes(ancien, nouveau);

        // les variables id ont un nom fixe par objetDi, donc il faut les updater
        if (nomID.equals(ancien)) {
            nomID = nouveau;
            return true;
        }
        /*if (nomIDParent.equals(ancien)) {
            nomIDParent = nouveau;
        }*/
        return true;
    }


    /**
     * Permet de changer le nom de l'id de variable représenté par ce hashObjet
     *
     * @param nouveau c'est le nouveau nom
     * @return true si l'opération réussit
     */
    public boolean changer_nom_id(String nouveau) {
        if (nouveau.isEmpty()) {
            System.out.println("* ObjetDi " + this + " /changer_nom_type : impossible de changer puisque " +
                    "le nom de de la variable rentrée est vide !");
            return false;
        }
        Object contenu = remove(nomID);
        remplacer_noms_stockes(nomID, nouveau);
        nomID = nouveau;
        initialiser(nomID, contenu);
        return true;
    }


    /**
     * Permet de changer le nom du libelle de variable représenté par ce hashObjet
     *
     * @param nouveau c'est le nouveau nom
     * @return true si l'opération réussit
     */
    public boolean changer_nom_nom(String nouveau) {
        if (nouveau.isEmpty()) {
            System.err.println("** ObjetDi " + this + " /changer_nom_type : impossible de changer puisque " +
                    "le nom de de la variable rentrée est vide !");
            return false;
        }
        Object contenu = remove(nomNOM);
        remplacer_noms_stockes(nomNOM, nouveau);
        nomNOM = nouveau;
        initialiser(nomNOM, contenu);
        return true;
    }

    /**
     * Permet de mettre à jour une liste à partir d'une hashLevis de détails
     *
     * @param type      le type de la liste
     * @param hashLevis liste de Levis
     */
    public void updater_liste(String type,
                              HashMap<String, Object> hashLevis) {
        int monId = getId();
        // Besoin de l'id pour trouver sa clé
        String cle = getType() + "_" + monId;
        if (hashLevis != null) {
            Object liste_levis = hashLevis.get(cle);
            if (liste_levis instanceof ArrayList) {
                if (((ArrayList<?>) liste_levis).size() > 1) {
                    // on a trouvé une liste de taille > 1
                    // le premier elt de la liste reçue est le nom de la sous-table
                    // les deux types doivent correspondre, évidemment puisqu'un objet peut avoir plusieurs listes
                    ArrayList<Object> arrayList = (ArrayList<Object>) liste_levis;
                    String nomSousTable = String.valueOf(arrayList.get(0));
                    if (nomSousTable.equals(type)) {
                        ArrayList<ObjetDi> listeUpdatee = generer_hashobjets(arrayList.subList(1, arrayList.size()),
                                type, hashLevis);
                        hashListes.put(nomSousTable, listeUpdatee);
                    }
                } else {
                    comment("... Liste reçue de Levis non conforme (nomTable, hashmap,...) " +
                                    " je reçois => " + liste_levis + "\nModification ignorée !",
                            "err");
                }
            }
        }
        if (monId > 0) {
            comment("updater_liste : je ne trouve pas le détail de " + cle, "out");
        }
    }

    private ArrayList<ObjetDi> generer_hashobjets(List<Object> list, String nomTable,
                                                  HashMap<String, Object> hashLevis) {
        ArrayList<ObjetDi> listeRetour = new ArrayList<>();
        for (Object objet : list) {
            if (objet instanceof HashMap) {
                ObjetDi nouvelObjet = getObjetFromType(nomTable);
                nouvelObjet.setData((HashMap<String, Object>) objet, hashLevis, true, false);
                listeRetour.add(nouvelObjet);
            }
        }
        return listeRetour;
    }


    private void comment(String msg, String outCode) {
        if (outCode.contains("err")) {
            System.err.println("*** ObjetDi [" + this + "] : " + msg);
        } else {
            System.out.println("* ObjetDi [" + this + "] : " + msg);
        }
    }


    /**
     * Donne une copie de la liste contenant les éléments du type désiré
     *
     * @param nomTable ne nom de la variable désirée
     * @return null si ce nom-là ne contient pas de liste
     */
    public ArrayList<ObjetDi> getListe(String nomTable) {
        Object valeur = hashListes.get(nomTable);
        if (valeur != null) {
            return new ArrayList<>((ArrayList) valeur);
        }
        return null;
    }


    public String getIdName() {
        return nomID;
    }

    public ObjetDi getNewInstance() {
        return getObjetFromType(getType());
    }

    public void charger_donnees(HashMap<String, Object> input, HashMap<String, Object> hashLevis) {

    }

    /**
     * Donne les valeurs stockées dans les données simples et les
     * données non-attendues
     *
     * @param nomVariable nom de variable
     * @return null si pas trouvé dans hashDonnees ou hashDonneesNonAttendues
     */
    public Object get(String nomVariable) {
        Object donneeAttendue = hashDonnees.get(nomVariable);
        if (donneeAttendue != null) {
            return donneeAttendue;
        }
        return hashDonneesNonAttendues.get(nomVariable);
    }

}
