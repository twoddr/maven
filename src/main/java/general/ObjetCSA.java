package general;

import java.awt.Color;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import util.SimpleLogger;

public abstract class ObjetCSA implements Comparable, Serializable {
    // Logger pour la classe
    private static final SimpleLogger logger = SimpleLogger.getLogger(ObjetCSA.class);
    private final LinkedHashMap<String, ObjetCSA> hashFKs = new LinkedHashMap<>(),
            hashFKIntermediaire = new LinkedHashMap<>();
    /**
     * ce sont les sous-objets qui contiennent l'objet courant comme sous-objet
     */
    private final LinkedHashMap<String, LinkedHashMap<String, Object>> hashDependants = new LinkedHashMap<>();
    /**
     * Sert à stocker le nom de la table d'un objet dépendant car hashDependants ne
     * stocke que les rawdata
     */
    private final LinkedHashMap<String, String> paires_NomsTables_NomsFKDependants = new LinkedHashMap<>();
    /**
     * Id du sous-objet dépendant, indexé par nom de table. Permet à
     * aspirer_donnees d'émettre les dépendants comme des FK sans jamais
     * instancier l'objet dépendant (cf. losange de la mort).
     */
    private final LinkedHashMap<String, Integer> idsDependants = new LinkedHashMap<>();
    private final LinkedHashMap<String, Object> hashDonnees = new LinkedHashMap<>();
    /**
     * ceci est indispensable si on veut avoir des données null mais garder le type
     * !
     * *
     */
    private final LinkedHashMap<String, Class> hashClassesDonnees = new LinkedHashMap<>();
    private final LinkedHashMap<String, Object> hashDonneesNonAttendues = new LinkedHashMap<>();
    private final LinkedHashMap<String, ArrayList<ObjetCSA>> hashListes = new LinkedHashMap<>();
    private final LinkedHashMap<String, GestionnaireImbrications> gestionnairesImbrications = new LinkedHashMap<>();
    private final LinkedHashSet<String> modifications = new LinkedHashSet<>();
    protected String TYPE, nomNOM, nomID;
    protected String COULEUR = "£couleur"; // £ exprès car ce n'est pas une variable à afficher
    // par défaut dans les fenêtres. Les variables commençant par 'id' subissent le
    // même sort
    protected LinkedHashSet<String> mandatories = new LinkedHashSet<>();
    protected ArrayList<String> variablesAAfficher = new ArrayList<>();
    protected LinkedHashMap<String, String> nomsPublics = new LinkedHashMap<>();
    private String publicName = "Objet non défini";
    private boolean textColorEnable = true;

    /**
     * Pour créer un ObjetCSA, il faut lui donner les noms des variables nécessaires
     * Les informations nécessaires permettent d'identifier, dans l'hashmap de
     * données,
     * quelles sont celles qu'on observe généralement dans une interface graphique
     * L'id et l'id intermédiaire sont utiles lors des sélections et échanges de
     * liste...
     *
     * @param type     le nom de la table de l'objet représenté
     * @param nomID    le nom de la variable qui stocke l'id
     * @param nomObjet le nom du nom de la variable nom
     */
    public ObjetCSA(String type, String nomID, String nomObjet) {

        TYPE = type.isEmpty() ? "Objet" : type;
        this.nomID = nomID.isEmpty() ? "id" : nomID;
        nomNOM = nomObjet.isEmpty() ? "nom" : nomObjet;

        initialiser_objetCSA();

        variablesAAfficher.add(nomNOM);
    }

    /**
     * Pour créer un HashObjet, il faut lui donner les noms des variables
     * nécessaires
     * Les informations nécessaires permettent d'identifier, dans l'hashmap de
     * données,
     * quelles sont celles qu'on observe généralement dans une interface graphique
     * L'id et l'id intermédiaire sont utiles lors des sélections et échanges de
     * liste...
     *
     * @param nomsVariables c'est la liste des noms de variables
     *                      d'après l'ordre ci-dessous :
     *                      * nomType le nom de la classe de l'objet représenté
     *                      * nomID le nom de la variable qui stocke l'id
     *                      * nomObjet le nom du nom de la variable nom
     *                      * nomCouleur le nom de la variable qui contient la
     *                      couleur texte
     *                      * nomIDIntermediaire le nom de l'id intermédiaire
     *                      * nomDateDu le nom de la variable dateDu
     *                      * nomDateAu le nom de la variable dateAu
     */
    public ObjetCSA(ArrayList<String> nomsVariables) {
        int taille = nomsVariables.size();
        TYPE = nomsVariables.isEmpty() ? "Objet" : nomsVariables.get(0);
        nomID = taille < 2 ? "id" : nomsVariables.get(1);
        nomNOM = taille < 3 ? "nom" : nomsVariables.get(2);

        initialiser_objetCSA();

        variablesAAfficher.add(nomNOM);
    }

    public static ObjetCSA creer_objet_vide() {
        return new ObjetCSA("Objet Vide", "id", "Empty Object") {
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
            public void reinitialiser() {

            }
        };
    }

    public Object chercher_valeur(String cle) {
        // recherche dans les champs simples
        Object objet = getSimpleData(cle);
        if (objet == null) { // recherche dans les classes; si existe, alors objet initialisé à NULL
            if (getVariableClass(cle) != null) {
                return null;
            }
        }
        if (objet == null)
            objet = getObjetIntermediaire(cle); // recherche dans les intermédiaires
        if (objet == null)
            objet = getFK(cle); // recherche des fk
        if (objet == null)
            objet = getObjetDependant(cle); // objets dépendants
        if (objet == null)
            objet = getListe(cle); // dans les noms de listes
        if (objet == null)
            objet = chercherDansFKIs(cle); // recherche dans les fkIntermediaires
        if (objet == null)
            objet = chercherDansFKs(cle); // recherche dans les fk
        if (objet == null)
            objet = chercherDansDependants(cle); // recherche dans les objets dépendants
        if (objet == null)
            objet = chercherDansGestionnaires(cle); // recherche dans les gestionnaires d'imbrication

        return objet;
    }

    private Object chercherDansFKIs(String cle) {
        for (ObjetCSA fki : getFKIs().values()) {
            Object hypothese = fki.chercher_valeur(cle);
            if (hypothese != null)
                return hypothese;
        }
        return null;
    }

    private Object chercherDansFKs(String cle) {
        for (ObjetCSA fk : getFKs().values()) {
            Object hypothese = fk.chercher_valeur(cle);
            if (hypothese != null)
                return hypothese;
        }
        return null;
    }

    private Object chercherDansDependants(String cle) {
        for (LinkedHashMap<String, Object> dependant : getHashmapDependants().values()) {
            Object hypothese = dependant.get(cle);
            if (hypothese != null)
                return hypothese;
        }
        return null;
    }

    private Object chercherDansGestionnaires(String cle) {
        for (GestionnaireImbrications gestionnaire : getGestionnairesImbrications().values()) {
            Object hypothese = gestionnaire.chercher_valeur(cle);
            if (hypothese != null)
                return hypothese;
        }
        return null;
    }

    /**
     * Associe un nomFk de quartier à un gestionnaire d'imbrications.
     * À partir de l'id du quartier, le gestionnaire est capable de rechercher les
     * villes et pays
     * pour affichage dans des combos par exemple
     *
     * @param nomFk        le nom du champ idquartier
     * @param gestionnaire le gestionnaire de localités
     * @param nomPublic    Le nom de la variable, visible par l'utilisateur lambda
     */
    public void addGestionnaire(String nomFk, GestionnaireImbrications gestionnaire, String nomPublic) {
        gestionnairesImbrications.put(nomFk, gestionnaire);
        modifications.add(nomFk);
        nomsPublics.put(nomFk, nomPublic);
    }

    /**
     * Donne l'hashObjet stocké à la clé rentrée
     *
     * @param cle nom de la variable
     * @return null si un tel objet n'existe pas !
     */
    public ObjetCSA getFK(String cle) {
        return hashFKs.get(cle);
    }

    /**
     * Donne l'hashObjet intermédiaire stocké à la clé rentrée
     *
     * @param cle nom de la variable
     * @return null si un tel objet n'existe pas !
     */
    public ObjetCSA getObjetIntermediaire(String cle) {
        return hashFKIntermediaire.get(cle);
    }

    /**
     * Donne l'hashObjet de données brutes stocké à la clé rentrée
     *
     * @param cle nom de la variable
     * @return null si un tel objet n'existe pas !
     */
    public ObjetCSA getObjetDependant(String cle) {
        if (cle == null) {
            return null;
        }

        String nomType;
        LinkedHashMap<String, Object> hashMap;

        // 1) cle est peut-être directement un nomType (clé de hashDependants)
        hashMap = hashDependants.get(cle);
        if (hashMap != null) {
            nomType = cle;
        } else {
            // 2) cle est peut-être un nomFKD → on retrouve le nomType correspondant
            nomType = null;
            for (Map.Entry<String, String> paire : paires_NomsTables_NomsFKDependants.entrySet()) {
                if (cle.equals(paire.getValue())) {
                    nomType = paire.getKey();
                    break;
                }
            }
            if (nomType == null) {
                return null;
            }
            hashMap = hashDependants.get(nomType);
            if (hashMap == null) {
                return null;
            }
        }

        ObjetCSA objetDi = getObjetFromType(nomType);
        if (objetDi == null) {
            System.err.println("ObjetCSA/getObjetDependant : L'objet à la clé " + cle + " contient un objetDi null !");
            return null;
        }
        objetDi.setData(new HashMap<>(hashMap), null, true, true);
        return objetDi;
    }

    public boolean isTextColorEnable() {
        return textColorEnable;
    }

    public void setTextColorEnable(boolean textColorEnable) {
        this.textColorEnable = textColorEnable;
    }

    /**
     * Stocke la liste fournie uniquement si ce sont des objetsDi
     * Les non-objetDis sont ignorés
     *
     * @param nomTexte le type des objetsDi
     * @param objets   liste d'objets quelconques
     * @return vrai si tous les objets étaient des objetsDi et que l'enregistrement
     *         s'est bien passé
     */
    public boolean inserer_liste(String nomTexte, ArrayList<?> objets) {
        if (objets == null) {
            comment("inserer_liste : liste null interdite pour " + nomTexte, "err");
            return false;
        }

        ArrayList<ObjetCSA> objetDis = new ArrayList<>();
        ArrayList<Object> nonAttendus = new ArrayList<>();

        for (Object objet : objets) {
            if (objet instanceof ObjetCSA) {
                objetDis.add((ObjetCSA) objet);
            } else {
                nonAttendus.add(objet);
            }
        }

        // 1) Liste attendue : on la range normalement dans hashListes
        if (hashListes.containsKey(nomTexte)) {
            hashListes.put(nomTexte, objetDis);
            addModification(nomTexte);

            if (!nonAttendus.isEmpty()) {
                comment("inserer_liste : " + nonAttendus.size()
                        + " élément(s) ignoré(s) car ce ne sont pas des ObjetCSA", "err");
            }
            return nonAttendus.isEmpty();
        }

        // 2) Liste NON attendue : on la met en non-attendue, pas dans hashListes
        String cleNonAttendue = "liste_" + nomTexte;
        hashDonneesNonAttendues.put(cleNonAttendue, creer_hashmaps_objets(objetDis));
        addModification(cleNonAttendue);

        if (!nonAttendus.isEmpty()) {
            comment("inserer_liste : " + nonAttendus.size()
                    + " élément(s) non-ObjetCSA non repris dans la liste non-attendue", "err");
        }

        return nonAttendus.isEmpty();
    }

    /**
     * Cette méthode permet d'initialiser/reinitialiser les listes
     *
     * @param nom       le nom de la variable ; ne peut être vide
     * @param nomPublic c'est le nom public associé à la liste
     */
    protected void initialiser_liste(String nom, String nomPublic) {
        if (nom.isEmpty()) {
            System.err.println("** ObjetCSA " + this + "  : Initialisation d'une liste sans nom de variable se l'objet "
                    + this + ". Demande ignorée !");
            return;
        }

        hashListes.put(nom, new ArrayList<>());
        nomsPublics.put(nom, nomPublic);
    }

    /**
     * rajoute des éléments dans une liste stockée
     * Attention : les doublons sont ignorés !
     *
     * @param nomTable la table faisant l'objet de la liste
     * @param objetDis les éléments à insérer
     * @return vrai si tout se passe bien
     */
    public boolean ajouter_dans_liste(String nomTable, ArrayList<ObjetCSA> objetDis) {
        if (!hashListes.containsKey(nomTable)) {
            comment("rajouter_dans_liste : Cet objet n'attend pas de liste de " + nomTable + " !\n" +
                    "J'en crée une nouvelle, comme l'aurait fait inserer_liste !", "err");
            return inserer_liste(nomTable, objetDis);
        }
        TreeSet<ObjetCSA> oldList = new TreeSet<>(hashListes.get(nomTable));
        oldList.addAll(objetDis);
        return inserer_liste(nomTable, new ArrayList(oldList));
    }

    /**
     * Permet de mettre à jour un sous-objet (fk)
     *
     * @param nomFK   ne nom de sa variable dans l'objet
     * @param objetDi le nouveau sous-objet
     * @return vrai si objetDi n'est pas null et l'objet est attendu
     */
    public boolean inserer_fk(String nomFK, ObjetCSA objetDi) {
        if (objetDi == null) {
            comment("** ObjetCSA/inserer_fk : Tentative d'insertion de NULL dans" + " le fk" + nomFK + " de " + this
                    + ". Action ignorée !", "out");
            return false;
        }
        if (hashFKs.containsKey(nomFK)) {
            ObjetCSA ancienObjet = hashFKs.get(nomFK);
            if (!objetDi.equals(ancienObjet)) {
                addModification(nomFK);
            }
            hashFKs.put(nomFK, objetDi);
            return true;
        }
        return false;
    }

    /**
     * Mettre à jour le gestionnaire d'imbrications
     *
     * @param nomFK        le nom du fkquartier
     * @param gestionnaire le nouveau gestionnaire
     * @return vrai si l'élément est attendu
     */
    public boolean inserer_gestionnaireImbrications(String nomFK, GestionnaireImbrications gestionnaire) {
        if (gestionnaire == null) {
            comment("** ObjetCSA/inserer_gestionnaire : Tentative d'insertion de NULL dans" + " le quartier " + nomFK
                    + " de " + this + ". Action ignorée !", "out");
            return false;
        }
        if (gestionnairesImbrications.containsKey(nomFK)) {
            gestionnairesImbrications.put(nomFK, gestionnaire);
            addModification(nomFK);
            return true;
        }
        System.err.println("ObjetCSA/inserer_gestionnaire : Demande d'insertion de " + gestionnaire
                + " alors que la variable " + nomFK + " n'est pas attendue. Insertion ignorée !");
        return false;
    }

    public void inserer_dependant(String nomFK, ObjetCSA objetDi) {
        if (objetDi == null) {
            System.err.println("ObjetCSA " + this + " /inserer_inserer_dependant : "
                    + "L'objetDi rentré ne peut être NULL. Action ignorée !");
            return;
        }
        String nomTable = getNomTableDependant(nomFK);
        addModification(nomFK);
        if (nomTable != null) {
            hashDependants.put(nomTable, new LinkedHashMap<>(objetDi.getData()));
            idsDependants.put(nomTable, objetDi.getId());
            return;
        }

        comment("inserer_dependant : Cet objet [" + this + "] n'attend pas de sousObjet de nom " + nomTable + " !\n"
                + "Je l'insère dans les non-attendus (uniquement les modifications) !", "out");
        hashDonneesNonAttendues.put(nomFK, objetDi.getHashModifications());
    }

    public boolean inserer_valeur_intermediaire(String tableIntermediaire, String nomVariable, Object valeur) {
        if (!hashFKIntermediaire.containsKey(tableIntermediaire)) {
            comment("inserer_intermediaire : Cet objet n'attend pas de d'intermédiaire de nom " + tableIntermediaire
                    + " !\n" + "J'ignore l'insertion de " + valeur + " !", "err");
            return false;
        }
        addModification(nomVariable);
        ObjetCSA objetIntermediaire = hashFKIntermediaire.get(nomVariable);
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
        comment("ObjetCSA " + this + " /inserer_dans_gestionnaire : Cet objet : " + this + " n'attend pas de nom "
                + nomFK + " !\n" + "J'ignore l'insertion !", "err");
        return false;
    }

    public boolean inserer_dans_gestionnaireImbrication(String nomFK, ObjetCSA objetDi) {
        if (gestionnairesImbrications.containsKey(nomFK)) {
            GestionnaireImbrications gestionnaireTrouve = gestionnairesImbrications.get(nomFK);
            HashMap<String, Object> hashLevis = new HashMap<>();
            String cleLevis = objetDi.getType() + "-" + objetDi.getId();
            ArrayList<Object> couple = new ArrayList<>();
            couple.add(objetDi.getType());
            couple.add(objetDi.getData());
            hashLevis.put(cleLevis, couple);
            boolean change = gestionnaireTrouve.remplir_donnees(objetDi.getId(), hashLevis);
            if (change) {
                addModification(nomFK);
            }
            comment("ObjetCSA " + this + " /inserer_dans_gestionnaire : je n'ai pas pu insérer" +
                    " les données dans le gestionnaire " + nomFK + " !", "err");
            return true;
        }
        comment("ObjetCSA " + this + " /inserer_dans_gestionnaire : Cet objet : " + this +
                " n'attend pas de nom " + nomFK + " !\n" + "J'ignore l'insertion !", "err");
        return false;
    }

    private boolean setSimpleData(String nom, Object valeur) {
        if (valeur == null) {
            comment("ObjetCSA (privé) : on ne peut insérer une valeur nulle dans " + nom, "out");
            return false;
        }
        if (hashDonnees.containsKey(nom)) {
            return ecraser_hashmap(hashDonnees, nom, valeur);
        }
        if (hashDonneesNonAttendues.containsKey(nom)) {
            return ecraser_hashmap(hashDonneesNonAttendues, nom, valeur);
        }
        return false;
    }

    private boolean ecraser_hashmap(LinkedHashMap<String, Object> donnees, String nom, Object valeur) {
        Object ancienneValeur = donnees.get(nom);
        Object nouvelleValeur = ancienneValeur == null ? valeur : check_classes(ancienneValeur, valeur);
        if (nouvelleValeur == null) {
            comment("** ObjetCSA/inserer_xxx : la nouvelle valeur de " + nom + " de l'objet " + this + " est : "
                    + valeur + " alors que l'ancienne " + "valeur est de type : "
                    + (ancienneValeur == null ? "NULL" : ancienneValeur.getClass().getSimpleName())
                    + ". Insertion ignorée !", "out");
            return false;
        }
        donnees.put(nom, nouvelleValeur);
        if (!nouvelleValeur.equals(ancienneValeur))
            modifications.add(nom);
        return true;
    }

    /**
     * Permet de s'assurer que l'ancienne et la nouvelle valeur sont compatibles
     *
     * @param ancienneValeur la valeur préalablement stockée
     * @param valeur         la nouvelle valeur
     * @return la nouvelle valeur adaptée aux contraintes de classe de l'ancienne
     *         valeur et
     *         null si les classes sont non compatibles
     */
    private Object check_classes(Object ancienneValeur, Object valeur) {
        if (ancienneValeur == null) {
            return null;
        }
        Class classe = ancienneValeur.getClass();
        if (classe.equals(valeur.getClass())) {
            return valeur;
        }
        // Si de type différent, adaptation de types
        // on reçoit un int alors qu'un booléen est attendu
        if (classe.equals(Boolean.class) && valeur instanceof Integer) {
            int valeurInt = (int) valeur;
            return valeurInt != 0;
        }
        // on reçoit un long alors qu'un int est attendu
        if (classe.equals(Integer.class) && valeur instanceof Long) {
            return ((Long) valeur).intValue();
        }
        // on reçoit un float alors qu'un int est attendu
        if (classe.equals(Integer.class) && valeur instanceof Float) {
            return ((Float) valeur).intValue();
        }
        // idem pour les dates
        if (classe.equals(LocalDateTime.class) && valeur instanceof String) {
            return extraire_dateTime(String.valueOf(valeur));
        }
        if (classe.equals(LocalDate.class) && valeur instanceof String) {
            return extraire_date(String.valueOf(valeur));
        }
        if (classe.equals(LocalDateTime.class) && valeur instanceof LocalDate) {
            return ((LocalDate) valeur).atTime(12, 0);
        }
        if (classe.equals(LocalDate.class) && valeur instanceof LocalDateTime) {
            return ((LocalDateTime) valeur).toLocalDate();
        }
        if (valeur instanceof String && ancienneValeur instanceof Integer) {
            String normalise = FormatNombre.normaliser(String.valueOf(valeur));
            try {
                return Integer.parseInt(normalise);
            } catch (NumberFormatException | NullPointerException e) {
                return null;
            }
        }
        if (valeur instanceof String && ancienneValeur instanceof Float) {
            String normalise = FormatNombre.normaliser(String.valueOf(valeur));
            try {
                return Float.parseFloat(normalise);
            } catch (NumberFormatException | NullPointerException e) {
                return null;
            }
        }
        // pour tous les autres
        System.err.println("ObjetCSA : échec du check_classes entre " + ancienneValeur + " et " + valeur);
        return null;
    }

    public boolean set_single_data(String nom, Object valeur) {
        if (hashDonnees.containsKey(nom)) {
            return inserer_valeur(nom, valeur);
        }

        // fk intermédiaires
        for (Map.Entry<String, ObjetCSA> paire : hashFKIntermediaire.entrySet()) {
            ObjetCSA objetIntermediaire = paire.getValue();
            boolean insertionReussie = objetIntermediaire.set_single_data(nom, valeur);
            if (insertionReussie) {
                return true;
            }
        }

        // fks
        if (hashFKs.containsKey(nom)) {
            ObjetCSA sousObjet = hashFKs.get(nom);
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
            ArrayList<ObjetCSA> sousObjets = hashListes.get(nom);
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

        System.err.println("*** ObjetCSA " + this + " /set_single_data : la donnée (" + nom + ", " + valeur + ")"
                + " n'a pas pu être insérée dans l'objet " + this);
        return false;
    }

    public boolean isModifie(String nom) {
        return modifications.contains(nom);
    }

    private void initialiser_objetCSA() {
        hashDonnees.clear();
        hashDonneesNonAttendues.clear();
        initialiser(nomID, 0, "ID");
        initialiser(nomNOM, "", "Intitulé");
        initialiser(COULEUR, RGBConverter.convert_color_2_int(Color.DARK_GRAY), "Couleur de l'élément");

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
            System.err.println(
                    "*** ObjetCSA " + this + " /setType : vous ne pouvez pas insérer un type vide !\n" + "Ignoré !");
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
        if (variablesAAfficher.remove(ancien)) {
            variablesAAfficher.add(nouveau);
            return;
        }
    }

    public int getCouleur() {
        return ExtracteurHashMap.extraire_int(getSimpleData(COULEUR));
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

    /**
     * méthode à surdéfinir !
     *
     * @return tout le temps FALSE !
     */
    public boolean isParent() {
        return false;
    }

    /**
     * Donne l'id du parent éventuel (sert pour les treeview par exemple)
     *
     * @return 0 sauf si la méthode getParent a été overridée
     */
    public final int getIdParent() {
        if (getParent() == null) {
            return 0;
        }
        return getParent().getId();
    }

    public abstract ObjetCSA getParent();

    public abstract void setParent(ObjetCSA parent);

    public String getTypeEnfant() {
        return null;
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
     * L'insertion se fait dans l'objet actuel sinon, continue la recherche dans les
     * sous-objets
     * Ignoré si la valeur rentrée vaut NULL
     *
     * @param nom    le nom de la variable ; ne peut être vide
     * @param valeur la valeur à insérer; ne peut être null; ne peut être un
     *               ObjetCSA
     * @return vrai si succès, faux sinon; auquel cas, les non-attendues prennent
     *         l'info
     */
    public Boolean inserer_valeur(String nom, Object valeur) {
        if (nom.isEmpty()) {
            comment("** ObjetCSA " + this + "  : L'insertion d'une clé vide pour la donnée <" + valeur
                    + "> n'est pas autorisé !", "err");
            return false;
        }

        if (valeur instanceof ObjetCSA || valeur instanceof HashMap) {
            comment("!!! ObjetCSA " + this + " /inserer_valeur : on ne peut inserer ce type ici : " + valeur + " dans "
                    + this + "\n" + "Il faut passer par setData !", "err");
            return false;
        }
        return inserer_precisement(nom, valeur);

    }

    private boolean isExpected(String cle) {
        boolean dansDonnees = hashDonnees.containsKey(cle) || hashClassesDonnees.containsKey(cle);
        boolean dansIntermediare = hashFKIntermediaire.containsKey(cle);
        boolean dansFk = hashFKs.containsKey(cle);
        boolean dansGestionnaire = gestionnairesImbrications.containsKey(cle);

        return dansGestionnaire || dansFk || dansDonnees || dansIntermediare;
    }

    public abstract ObjetCSA getObjetFromType(String nomType);

    /**
     * Permet de faire appel au clear des hashmaps,
     * puis recharge les variables nécessaires à tout hashobjet
     */
    public final void clear() {
        hashDonnees.clear();
        initialiser_objetCSA();
        reinitialiser();
    }

    public abstract void reinitialiser();

    /**
     * Cette méthode permet d'initialiser/reinitialiser les variables
     *
     * @param nomVariable le nom de la variable ; ne peut être vide
     * @param valeur      la valeur à insérer; ne peut être null
     * @param nomPublic   C'est le nom vu par l'utilisateur lambda
     */
    protected void initialiser(String nomVariable, Object valeur, String nomPublic) {
        if (nomPublic == null) {
            throw new UnsupportedOperationException("La variable " + nomVariable + " de l'objet " + this +
                    " doit avoir un nom public. J'avorte !");
        }
        if (nomVariable.isEmpty()) {
            System.err.println("** ObjetCSA " + this + " /initialiser : L'insersion de <" + valeur
                    + "> dans une variable vide de l'objet " + this + " n'est pas autorisé ! Demande ignorée !");
            return;
        }
        Class classe = hashClassesDonnees.get(nomVariable);
        if (classe != null) {
            if (valeur == null) {
                hashDonnees.put(nomVariable, null);
                nomsPublics.put(nomVariable, nomPublic);
                return;
            }
            // on connait la classe à accepter
            if (valeur.getClass().equals(classe)) {
                // tout est ok
                hashDonnees.put(nomVariable, valeur);
                nomsPublics.put(nomVariable, nomPublic);
                return;
            }
            comment("Mauvais type reçu par " + this + ". la variable " + nomVariable + " doit être de type " + classe
                    + ", mais je recois " + valeur.getClass().getSimpleName(), "err");
            return;
        }
        if (valeur == null) {
            throw new UnsupportedOperationException("ObjetCSA/initialiser : on ne peut initialiser " + "une variable ("
                    + nomVariable + ") à null sans avoir initialisé le type (setClass)");
        }
        hashDonnees.put(nomVariable, valeur);
        nomsPublics.put(nomVariable, nomPublic);
        hashClassesDonnees.put(nomVariable, valeur.getClass());
    }

    /**
     * Uniquement si la variable est NULL
     *
     * @param nomVariable clé
     * @param classe      la classe de la variable attendue (NULL pour l'instant)
     * @param nomPublic   c'est le nom visible par l'utilisateur
     */
    public void setVariableClass(String nomVariable, Class classe, String nomPublic) {
        Object variable = hashDonnees.get(nomVariable);
        if (variable == null) {
            hashClassesDonnees.put(nomVariable, classe);
            nomsPublics.put(nomVariable, nomPublic);
            return;
        }
        System.err.println("*** Tentative d'insertion d'une nouvelle classe (" + classe.getSimpleName()
                + ") alors que la variable " + nomVariable + " a déjà été affectée à " + variable);
    }

    public Class getVariableClass(String nomVariable) {
        Class classe = hashClassesDonnees.get(nomVariable);

        /*
         * if (classe == null) {
         * System.err.println("** ObjetCSA [" + this +
         * "]/getVariableClass : le type de variable est inconnu pour " + nomVariable +
         * ". Je le remplace par Object");
         * return Object.class;
         * }
         */
        return classe;
    }

    /**
     * Cette méthode permet d'informer de la présence d'un sous-objet
     * identifié par son fk
     *
     * @param nomfk     le nom de la fk ; ne peut être vide
     * @param nomType   le nom du type de variable dont l'id est le fk
     * @param nomPublic c'est le nom visible par l'utilisateur
     */
    public void setFK(String nomfk, String nomType, String nomPublic) {
        ObjetCSA sousObjet = getObjetFromType(nomType);
        if (sousObjet == null) {
            System.err.println("** ObjetCSA " + this + "  : il n'existe pas de sous-objet au nom " + nomType
                    + " ! Demande ignorée !");
            return;
        }
        if (nomfk.isEmpty()) {
            System.err.println("** ObjetCSA " + this + " /setFK : Le nomFK attribué à la table " + nomType
                    + " est vide ! Demande ignorée !");
            return;
        }

        hashFKs.put(nomfk, sousObjet);
        for (Map.Entry<String, Object> paires : sousObjet.getSimpleData().entrySet()) {
            String cle = paires.getKey();
            nomsPublics.put(nomfk + "_" + cle, sousObjet.getNomPublic(cle) + " " + nomPublic);
        }
    }

    /**
     * Cette méthode permet d'informer de la présence d'un sous-objet
     * qui provient d'une table intermédiaire. Cet objet est complet lorque
     * le sous-objet intermediaire est également fourni !
     *
     * @param nomFk     le nom du type de variable dont l'id est le fk
     * @param nomPublic c'est le nom visible qui lui sera associé
     */
    public void setFKIntermediaire(String nomFk, String nomPublic) {
        ObjetCSA sousObjet = getObjetFromType(nomFk);
        if (sousObjet == null) {
            System.err.println("** ObjetCSA " + this + " /setFKIntermediaire : il n'existe pas de sous-objet au nom "
                    + nomFk + "" + " ! Demande ignorée !");
            return;
        }

        hashFKIntermediaire.put(sousObjet.getType(), sousObjet);
        for (Map.Entry<String, Object> paires : sousObjet.getSimpleData().entrySet()) {
            String cle = paires.getKey();
            nomsPublics.put(nomFk + "_" + cle, sousObjet.getNomPublic(cle) + " " + nomPublic);
        }
    }

    /**
     * Cette méthode permet d'informer de la présence d'un sous-objet
     * qui ne peut être instancié ici, car dépend de l'objet actuel.
     *
     * @param nomType   le nom du type de table de l'objet à ne pas créer
     * @param nomFKD    le nom de la variable (fk) dans l'objet parent
     * @param nomPublic le nom visible
     */
    public void setDependant(String nomType, String nomFKD, String nomPublic) {
        if (nomFKD.isEmpty()) {
            System.err.println("** ObjetCSA " + this + " /setDependant : Le nomFK attribué à la table " + nomType +
                    " est vide ! Demande ignorée !");
            return;
        }

        paires_NomsTables_NomsFKDependants.put(nomType, nomFKD);
        hashDependants.put(nomType, new LinkedHashMap<>());
        idsDependants.remove(nomType);

        String key = nomFKD + "_" + nomType;
        nomsPublics.put(key, nomType + " " + nomPublic);
    }

    /**
     * Indique si la clé fournie désigne un sous-objet dépendant. La clé peut être
     * le nom de la table (clé de hashDependants) ou le nom du FK dans l'objet
     * parent (nomFKD).
     *
     * @param cle le nom de variable à tester
     * @return vrai si cle désigne un dépendant
     */
    private boolean estCleDependant(String cle) {
        return hashDependants.containsKey(cle)
                || paires_NomsTables_NomsFKDependants.containsValue(cle);
    }

    /**
     * Résout le nom de table représenté par la clé fournie (le plus souvent un
     * nomFKD). N'instancie jamais l'objet dépendant.
     *
     * @param cle le nom de variable (nomFKD ou nomTable)
     * @return le nom de table, ou null si aucun dépendant ne correspond
     */
    private String getNomTableDependant(String cle) {
        if (hashDependants.containsKey(cle)) {
            return cle; // cle == nomTable
        }
        for (Map.Entry<String, String> paire : paires_NomsTables_NomsFKDependants.entrySet()) {
            if (cle.equals(paire.getValue())) {
                return paire.getKey();
            }
        }
        return null;
    }

    /**
     * Réinitialise tous les sous-objets
     */
    public void clearSousObjets() {
        for (Map.Entry<String, ObjetCSA> fks : hashFKs.entrySet()) {
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
     * avec le type de l'objet valeur. Si le nom existe déjà, le type est vérifié
     * Toutes les variables de l'objet et ses sous-objets sont passées en revue
     *
     * @param cle    le nom de la variable : ne peut commencer par le caractère #
     * @param valeur la nouvelle valeur à attribuer; elle peut même être un ObjetCSA
     *               car la clé peut être un fk
     * @return Vrai si tout s'est bien passé, false sinon
     */
    private boolean inserer_precisement(String cle, Object valeur) {
        if (cle.startsWith("#")) {
            System.out.println("* ObjetCSA/inserer_xxx : j'ignore volontairement d'insérer la variable " + cle);
            return true;
        }
        // recherche dans les champs simples
        if (setSimpleData(cle, valeur)) {
            return true;
        }

        ObjetCSA objetIntermediaire = getObjetIntermediaire(cle); // recherche dans les intermédiaires
        if (objetIntermediaire != null && valeur instanceof ObjetCSA
                && ((ObjetCSA) valeur).getType().equals(objetIntermediaire.getType())) {
            hashFKIntermediaire.put(cle, (ObjetCSA) valeur);
            return true;
        }
        // Recherche dans les fk intermédiaires
        for (Map.Entry<String, ObjetCSA> paire : getFKIs().entrySet()) {
            ObjetCSA fki = paire.getValue();
            if (fki.setSimpleData(cle, valeur))
                return true;
        }
        ObjetCSA objetFK = getFK(cle); // recherche des fk
        if (objetFK != null && valeur instanceof ObjetCSA && ((ObjetCSA) valeur).getType().equals(objetFK.getType())) {
            hashFKs.put(cle, (ObjetCSA) valeur);
            return true;
        }
        // Recherche dans les fk
        for (Map.Entry<String, ObjetCSA> paire : getFKs().entrySet()) {
            ObjetCSA fk = paire.getValue();
            if (fk.setSimpleData(cle, valeur))
                return true;
        }
        // Objets dépendants : on ne les instancie JAMAIS ici (losange de la mort) :
        // on alimente uniquement les données brutes conservées dans hashDependants.
        // Si la clé est le nom de table du dépendant, on remplace ses données.
        if (valeur instanceof ObjetCSA objetDependant && hashDependants.containsKey(cle)) {
            hashDependants.put(cle, new LinkedHashMap<>(objetDependant.getAllData()));
            idsDependants.put(cle, objetDependant.getId());
            return true;
        }
        // Si la clé appartient aux données d'un dépendant déjà stockées, on met à jour
        // la valeur directement dans sa hashmap (sans l'instancier).
        for (Map.Entry<String, LinkedHashMap<String, Object>> paire : hashDependants.entrySet()) {
            LinkedHashMap<String, Object> donneesDependant = paire.getValue();
            if (donneesDependant != null && donneesDependant.containsKey(cle)) {
                donneesDependant.put(cle,
                        valeur instanceof ObjetCSA objetValeur ? objetValeur.getAllData() : valeur);
                return true;
            }
        }
        // recherche dans les gestionnaires d'imbrication
        for (GestionnaireImbrications gestionnaire : getGestionnairesImbrications().values()) {
            ObjetCSA pays = gestionnaire.getPays();
            if (pays != null && pays.setSimpleData(cle, valeur)) {
                return true;
            }
            ObjetCSA ville = gestionnaire.getVille();
            if (ville != null && ville.setSimpleData(cle, valeur)) {
                return true;
            }
            ObjetCSA quartier = gestionnaire.getQuartier();
            if (quartier != null && quartier.setSimpleData(cle, valeur)) {
                return true;
            }
        }
        /*
         * System.err.println("ObjetCSA : échec de l'insertion de " + valeur +
         * " de type "
         * + (valeur == null ? "*" : valeur.getClass().getSimpleName()) +
         * " dans la variable " + cle +
         * ".\nJe n'ai pas trouvé d'objet ou sous-objet attendant cette variable. Insertion ignorée !"
         * );
         */
        comment("Pas trouvé de preneur pour la variable " + cle + ", je l'insère dans les non-attendues", "out");
        ajouter_aux_nonAttendues(cle, valeur);
        return false;
    }

    private void ajouter_aux_nonAttendues(String cle, Object valeur) {
        if (valeur instanceof ObjetCSA) {
            hashDonneesNonAttendues.put(cle, ((ObjetCSA) valeur).getAllData());
            return;
        }
        hashDonneesNonAttendues.put(cle, valeur);

        addModification(cle);
    }

    public String getNomAFiltrer() {
        return toString();
    }

    /**
     * renvoie une copie de la liste des variables remplies
     * mais non prévues dans le hashobjet
     *
     * @return l'hashMap de données non-attendues mais enregistrées quand même
     */
    public LinkedHashMap<String, Object> getNonAttendus() {
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
            comment("Echec de conversion de dateTime pour " + this
                    + ". Je m'attendais à un format yyyy-MM-dd HH:mm:ss, mais " + "j'ai : " + s
                    + ", et je ne prends que les 19 premiers symboles", "err");
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
            comment("Echec de conversion de dateTime pour " + this + ". Je m'attendais à un format yyyy-MM-dd, mais "
                    + "j'ai : " + s, "err");
            return null;
        }
    }

    /**
     * Permet de dire si deux eltIHM sont égaux ou pas.
     * Les critères d'égalité sont (dans l'ordre) :
     * * le type (obligatoire)
     * * l'id (si ok, on regarde l'intermédiaire)
     *
     * @param o l'autre eltIHM
     * @return vrai si les conditions précédentes sont vérifiées
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof ObjetCSA) {
            ObjetCSA autre = (ObjetCSA) o;
            boolean memeType = autre.getType().equals(getType());
            if (!memeType) {
                return false;
            }
            int monId = getId();
            int autreId = autre.getId();
            // je ne checke que si MON id es positif (permet les sélections combo par
            // défaut)
            if (monId > 0) {
                return autreId == monId;
            }
            String monNom = toString();
            String autreNom = autre.toString();
            if (monNom.isEmpty() && autreNom.isEmpty()) {
                return super.equals(o);
            }
            return monNom.equalsIgnoreCase(autreNom);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int myId = getId();

        // Logic for objects with a positive ID: hash only on the ID and type
        if (myId > 0) {
            return Objects.hash(getType(), myId); // Include type and ID
        }

        // Logic for objects without a positive ID: hash on the type and the lowercase
        // name
        String myName = toString();
        if (myName.isEmpty()) {
            // If the name is empty, fall back to the default identity hash (similar to
            // super.equals(o))
            return System.identityHashCode(this);
        }
        // Use the lowercase version of the name to be consistent with equalsIgnoreCase
        return Objects.hash(getType(), myName.toLowerCase());
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * L'id de l'objet actuel est fourni systématiquement !
     *
     * @param nomChamps ce sont les noms dont les valeurs sont à retourner
     * @return une hashmap de (nom, donnée)
     */
    public LinkedHashMap<String, Object> getData(ArrayList<String> nomChamps) {
        LinkedHashMap<String, Object> donnees = new LinkedHashMap<>();
        donnees.put(nomID, getId());
        nomChamps.forEach(cle -> {
            if (hashDonnees.containsKey(cle)) {
                donnees.put(cle, hashDonnees.get(cle));
            } else if (hashFKs.containsKey(cle)) {
                ObjetCSA sousObjet = hashFKs.get(cle);
                int sonId = sousObjet.getId();
                donnees.put(cle, sonId);
                donnees.put(sousObjet.getType() + "-" + sonId, sousObjet.getData(nomChamps));
            } else if (hashFKIntermediaire.containsKey(cle)) {
                ObjetCSA objetIntermediaire = hashFKIntermediaire.get(cle);
                int sonId = objetIntermediaire.getId();
                donnees.put(cle, sonId);
                donnees.put(objetIntermediaire.getType() + "-" + sonId, objetIntermediaire.getData(nomChamps));
            } else if (hashDependants.containsKey(cle)) {
                LinkedHashMap<String, Object> hashMap = hashDependants.get(cle);
                if (hashMap != null) {
                    ObjetCSA objetEtranger = getObjetFromType(cle);
                    objetEtranger.setData(new HashMap<>(hashMap), null, true, false);
                    int sonId = objetEtranger.getId();
                    donnees.put(cle, objetEtranger.getId());
                    donnees.put(objetEtranger.getType() + "-" + sonId, objetEtranger.getData(nomChamps));
                }
            } else if (hashListes.containsKey(cle)) {
                ArrayList<ObjetCSA> liste = hashListes.get(cle);
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
     * Donne une copie du contenu des variables par paires de (clé, data)
     * les clés sont : base, non-attendues, fks, intermédiares, dépendants et listes
     * Un sous-élément éventuel sera également fourni sous forme de hashmap
     *
     * @return une hashmap de (nom, donnée)
     */
    public LinkedHashMap<String, Object> getAllData() {
        LinkedHashMap<String, Object> donnees = new LinkedHashMap<>();
        // les données de base
        donnees.put("base", hashDonnees);

        // les données non-attendues
        donnees.put("non-attendus", hashDonneesNonAttendues);

        // fks
        LinkedHashMap<String, LinkedHashMap<String, Object>> fks = new LinkedHashMap<>();
        hashFKs.forEach((nomFk, sousObjet) -> {
            fks.put(nomFk, sousObjet.getAllData());
        });
        gestionnairesImbrications.forEach((nomFkQuartier, gestionnaire) -> {
            fks.put(nomFkQuartier, gestionnaire.getQuartier().getAllData());
        });
        donnees.put("fks", new HashMap<>(fks));

        // intermediaires
        LinkedHashMap<String, LinkedHashMap<String, Object>> intermediaires = new LinkedHashMap<>();
        hashFKIntermediaire.forEach((nomTable, sousObjet) -> {
            intermediaires.put(nomTable, sousObjet.getAllData());
        });
        donnees.put("intermediaires", new HashMap<>(intermediaires));

        // étrangers
        donnees.put("dependants", new HashMap<>(hashDependants));

        // listes
        LinkedHashMap<String, ArrayList<LinkedHashMap<String, Object>>> hashmaps = new LinkedHashMap<>();
        hashListes.forEach((nomTable, objetsDi) -> {
            ArrayList<LinkedHashMap<String, Object>> listeHashmaps = new ArrayList<>();
            objetsDi.forEach(objetCSA -> {
                listeHashmaps.add(objetCSA.getAllData());
            });
            hashmaps.put(nomTable, listeHashmaps);
        });
        donnees.put("listes", new HashMap<>(hashmaps));

        return donnees;
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * dans un format aisément interprétable par un bot et aisément xmlable
     * Les noms de variable sont les noms des champs de la BD
     * Un sous-élément éventuel sera également fourni sous forme de hashmap
     *
     * @return une hashmap de (nom, donnée)
     */
    public LinkedHashMap<String, Object> getFormattedData() {
        LinkedHashMap<String, Object> donnees = new LinkedHashMap<>();
        // les données de base
        donnees.putAll(hashDonnees);
        // les données non-attendues
        donnees.putAll(hashDonneesNonAttendues);
        // fks
        hashFKs.forEach((nomFk, sousObjet) -> {
            donnees.put(nomFk, sousObjet.getFormattedData());
        });
        gestionnairesImbrications.forEach((nomFkQuartier, gestionnaire) -> {
            donnees.put(nomFkQuartier, gestionnaire.getFormattedData());
        });

        // intermediaires
        hashFKIntermediaire.forEach((nomTable, sousObjet) -> {
            donnees.put(nomTable, sousObjet.getFormattedData());
        });

        // étrangers
        donnees.putAll(hashDependants);

        // listes
        hashListes.forEach((nomTable, objetsDi) -> {
            ArrayList<Map<String, Object>> listeHashmaps = new ArrayList<>();
            objetsDi.forEach(objetCSA -> {
                listeHashmaps.add(objetCSA.getFormattedData());
            });
            donnees.put(nomTable, listeHashmaps);
        });

        return donnees;
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * Les fks, fkis, fkGestionnaires, listes sont fournis (sans leurs détails)
     *
     * @return une hashmap de (nom, donnée)
     */
    public HashMap<String, Object> getData() {
        // les données de base et non-attendues
        HashMap<String, Object> donnees = getSimpleData();

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
        hashDependants.forEach((nomTable, stringObjectHashMap) -> {
            String nomFK = paires_NomsTables_NomsFKDependants.get(nomTable);
            ObjetCSA objetCSA = getObjetFromType(nomTable); // ce sera un objet avec données minimales !
            objetCSA.setData(stringObjectHashMap, null, true, false);
            donnees.put(nomFK, objetCSA.getId());
        });

        // listes
        hashListes.forEach((nomTable, liste) -> {
            ArrayList<HashMap<String, Object>> hashMaps = creer_hashmaps_objets(liste);
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
            boolean toutPrendre, boolean verbose) {
        boolean hashLevisFourni = hashLevis != null && !hashLevis.isEmpty();
        final HashMap<String, Object> hashDetails;
        hashDetails = hashLevis == null ? new HashMap<>() : new HashMap<>(hashLevis);
        // rajoute les données principales dans les secondaires (car une fois en
        // profondeur,
        // on perd les données principales)
        Object object = input.get(this.nomID); // non nul si Levis fournit l'id
        int idNouveau = object == null ? this.hashCode() : // si pas d'id, on prend le hashcode de l'objet actuel
                ExtracteurHashMap.extraire_int(object);
        if (hashLevisFourni && idNouveau > 0) {
            String cle = getType() + "-" + idNouveau;
            if (!hashLevis.containsKey(cle)) {
                ArrayList<Object> liste = new ArrayList<>();
                liste.add(getType());
                liste.add(extraire_donnees_attendues(input));
                hashDetails.put(cle, liste);
                if (verbose)
                    System.out.println("Rajout dans le 3e élément => (" + cle + ", " + liste + ")");
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

        // fk intermédiaires après/avant (conflit dans les versions???) les fk car s'il
        // y a des noms similaires, il faut
        // donner la priorité à l'objet principal
        hashFKIntermediaire.forEach((nomTable, sousObjet) -> {
            clesUtilisees.addAll(sousObjet.setData(reducedInput, hashDetails, false, verbose));
            clesUtilisees.forEach(reducedInput::remove);
        });

        // fks
        for (Map.Entry<String, ObjetCSA> paire : hashFKs.entrySet()) {
            String nomFk = paire.getKey();
            ObjetCSA sousObjet = paire.getValue();
            Object hypoId = reducedInput.get(nomFk);
            if (hypoId != null) {
                reducedInput.remove(nomFk);
                clesUtilisees.add(nomFk);
                if (hypoId instanceof Number) {
                    int idSousObjet = ((Number) hypoId).intValue();
                    commentIf(hypoId instanceof Long, "** ObjetCSA/setData : le fk rentré (" + nomFk
                            + ") est du type long !!\n"
                            + "Espérons qu'il n'est pas trop long car j'en fais un cast vers int et je continue...",
                            "err");
                    sousObjet.setId(idSousObjet);
                    if (hashLevisFourni && idSousObjet > 0) {
                        sousObjet.setData(extraire_fk(sousObjet.getType(), idSousObjet, hashDetails), hashDetails, true,
                                verbose);
                    }
                } else {
                    comment("*** ObjetCSA/setData : le fk rentré " + nomFk + " n'est pas un nombre mais est du type "
                            + hypoId.getClass().getSimpleName() + " !!!\n" + "Cette information sera ignorée !", "err");
                }
            }
        }

        // objets étrangers (non initialisables)
        for (Map.Entry<String, String> paire : paires_NomsTables_NomsFKDependants.entrySet()) {
            String nomTable = paire.getKey();
            String nomSpecial = paire.getValue();
            Object hypothese = reducedInput.get(nomSpecial);
            if (hypothese instanceof Number && ((Number) hypothese).intValue() > 0) {
                int id = ((Number) hypothese).intValue();
                String cleLevis = nomTable + "-" + id;
                Object hypoLevis = hashLevisFourni ? hashDetails.get(cleLevis) : null;
                clesUtilisees.add(nomSpecial);
                reducedInput.remove(nomSpecial);
                ObjetCSA sousObjet = getObjetFromType(nomTable); // ce sera un objet avec données minimales
                sousObjet.setId(id);
                if (hypoLevis != null) { // détail envoyé
                    if (hypoLevis instanceof ArrayList) {
                        ArrayList<Object> listeLevis = (ArrayList<Object>) hypoLevis;
                        if (listeLevis.size() > 1) {
                            String nomTableLevis = String.valueOf(listeLevis.get(0));
                            // !
                            if (sousObjet != null && nomTableLevis.equals(nomTable)
                                    && listeLevis.get(1) instanceof HashMap) {
                                clesUtilisees.addAll(sousObjet.setData((HashMap<String, Object>) listeLevis.get(1),
                                        null, toutPrendre,
                                        verbose));
                                hashDependants.put(nomTable, new LinkedHashMap<>(sousObjet.getData()));
                                idsDependants.put(nomTable, sousObjet.getId());
                            } else {
                                comment("setData/étrangers (" + nomTable + ", " + nomSpecial
                                        + ") : Trouvé un détail liste " + cleLevis + " dont le nom " + nomTableLevis
                                        + " ne correspond pas à " + nomTable + " " + "suivi de hashmap. J'obtiens : "
                                        + listeLevis, "err");
                            }
                        } else {
                            comment("setData/étrangers (" + nomTable + ", " + nomSpecial + "): Trouvé un détail liste "
                                    + cleLevis + " non conforme : " + listeLevis, "err");
                        }
                    } else {
                        comment("setData/étrangers (" + nomTable + ", " + nomSpecial + ") : Trouvé un détail liste "
                                + cleLevis + " qui n'est pas une liste ! => " + hypoLevis, "err");
                    }
                } else {
                    hashDependants.put(nomTable, new LinkedHashMap<>(sousObjet.getData()));
                    idsDependants.put(nomTable, sousObjet.getId());
                }
            }
        }

        // listes
        for (Map.Entry<String, ArrayList<ObjetCSA>> paire : hashListes.entrySet()) {
            String nomTable = paire.getKey();
            ArrayList<ObjetCSA> anciensObjets = paire.getValue();
            int monId = getId();
            String monType = getType();
            if (hashLevisFourni) {
                String cleLevis = monType + "_" + nomTable + "_" + monId;
                Object objetLevis = hashDetails.get(cleLevis);
                if (objetLevis != null) {
                    if (objetLevis instanceof ArrayList && !((ArrayList<?>) objetLevis).isEmpty()
                            && ((ArrayList<?>) objetLevis).get(0) instanceof String) {
                        ArrayList<Object> listeLevis = (ArrayList<Object>) objetLevis;
                        String nomTableLevis = String.valueOf(listeLevis.get(0));
                        if (nomTableLevis.equals(nomTable)) {
                            ArrayList<Object> listeRecue = new ArrayList<>(listeLevis.subList(1, listeLevis.size()));
                            ArrayList<ObjetCSA> arrayList = transformer_liste(nomTable, listeRecue, hashDetails);
                            anciensObjets.clear();
                            anciensObjets.addAll(arrayList);
                            clesUtilisees.add(nomTable);
                        } else {
                            comment("setData : Trouvé un détail liste " + cleLevis + " qui est une liste de "
                                    + nomTableLevis + " et non  " + nomTable, "out");
                        }
                    } else {
                        comment("setData : Trouvé un détail liste " + cleLevis
                                + " qui n'est pas une liste dont le 1er elt est un String, mais => " + objetLevis,
                                "out");
                    }
                } else {
                    if (monId > 0) {
                        comment("setData : Pas trouvé la liste " + cleLevis + " dans l'élément 3 (hashLevis)", "out");
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
            if (fk != null) {
                reducedInput.remove(nomFk);
                clesUtilisees.add(nomFk);
                if (fk instanceof Integer) {
                    gestionnaire.remplir_donnees((int) fk, hashDetails);
                }
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

    private void commentIf(boolean go, String s, String errout) {
        if (go) {
            comment(s, errout);
        }
    }

    public boolean estSupprimable() {
        return true;
    }

    private LinkedHashMap<String, Object> extraire_donnees_attendues(HashMap<String, Object> input) {
        LinkedHashMap<String, Object> terug = new LinkedHashMap<>();
        input.forEach((cle, valeur) -> {
            if (hashDonnees.containsKey(cle)) {
                terug.put(cle, valeur);
            }
        });
        return terug;
    }

    public String affiche_details() {
        StringBuilder out = new StringBuilder(
                "*** Détails de l'élément " + this + " de la table " + getType() + " ***");
        hashDonnees.forEach((cle, donnee) -> {
            out.append("\n").append(cle).append(" : ").append(donnee);
        });
        if (!hashFKIntermediaire.isEmpty()) {
            out.append("\n------- Table intermédiare --------");
            hashFKIntermediaire.forEach((cle, donnee) -> {
                out.append("\n").append(cle).append(" : ").append(donnee.getNom()).append(" : ").append(donnee.getId());
            });
        }
        out.append("\n------- Table d'objets liés --------");
        hashFKs.forEach((cle, donnee) -> {
            out.append("\n").append(cle).append(" : ").append(donnee.getNom()).append(" : ").append(donnee.getId());
        });
        if (!hashDependants.isEmpty()) {
            out.append("\n------- Objets dépendants --------");
            hashDependants.forEach((cle, donnee) -> {
                out.append("\n").append(cle).append(" : ").append(donnee);
            });
        }
        if (!hashDonneesNonAttendues.isEmpty()) {
            out.append("\n------- Données non-prévues --------");
            hashDonneesNonAttendues.forEach((cle, donnee) -> {
                out.append("\n").append(cle).append(" : ").append(donnee);
            });
        }
        if (!hashListes.isEmpty()) {
            out.append("\n------- Sous-listes --------");
            hashListes.forEach((cle, donnee) -> {
                out.append("\n").append(cle).append(" : ").append(affiche_liste_light(donnee));
            });
        }
        return out.toString();
    }

    private String affiche_liste_light(ArrayList<ObjetCSA> objetDis) {
        StringBuilder terug = new StringBuilder();
        objetDis.forEach(objetDi -> {
            terug.append(objetDi.getId()).append("-").append(objetDi.getNom()).append(",");
        });
        return terug.toString();
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
     * @param id          c'est l'id du détail à chercher dans la hashLevis. La clé
     *                    de recherche sera ceType-id
     * @return la liste des noms qui ont trouvé preneur
     */
    public ArrayList<String> setDataFromHashLevis(HashMap<String, Object> hashLevis, int id, boolean toutPrendre,
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
                        + " (nomTable, HashMap), mais j'obtiens : " + biliste, "err");
                return clesUtilisees;
            }
            comment("setDataFromHashLevis : la clé : " + cle + " ne contient pas le type " + getType() + ", "
                    + "mais le type " + nomLevis, "err");
            return clesUtilisees;
        }
        if (id > 0) {
            comment("setDataFromHashLevis : Pas de détail trouvé pour la clé : " + cle, "err");
        }
        return clesUtilisees;
    }

    private void ecrire_rapport(HashMap<String, Object> hashMap, ArrayList<String> clesUtilisees) {
        comment("\n***Rapport d'enregistrement de données de : " + this, "out");
        comment("Liste des éléments non attendus : " + hashDonneesNonAttendues.keySet(), "out");
        ArrayList<String> nonUtilises = new ArrayList<>(hashMap.keySet());
        clesUtilisees.forEach(nonUtilises::remove);
        comment("Liste des données qui n'ont pas trouvé preneur : " + nonUtilises, "out");
        comment("*** Fin Rapport", "out");
    }

    private ArrayList<ObjetCSA> transformer_liste(String nomTable, ArrayList<Object> listeRecue,
            HashMap<String, Object> hashLevis) {
        ArrayList<ObjetCSA> terug = new ArrayList<>();
        for (Object elt : listeRecue) {
            if (elt instanceof HashMap && !(((HashMap) elt).isEmpty())) {
                ObjetCSA objetDi = getObjetFromType(nomTable);
                objetDi.setData((HashMap<String, Object>) elt, hashLevis, false, true);
                terug.add(objetDi);
            } else if (elt instanceof ObjetCSA) {
                terug.add((ObjetCSA) elt);
            }
        }
        return terug;
    }

    /**
     * Permet de voir si l'objet actuel contient un fk du type rentré
     *
     * @param nomType c'est le nom du fk
     * @return null s'il ne trouve pas ou le nomFK du type rentré
     */
    public String getNomFK(String nomType) {
        for (Map.Entry<String, ObjetCSA> paireFK : hashFKs.entrySet()) {
            if (paireFK.getValue().getType().equals(nomType)) {
                return paireFK.getKey();
            }
        }
        return null;
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * Seuls les champs simples et les non-attendus sont fournis ;
     * pas les fk ni même les intermédiaires !
     *
     * @return une hashmap de (nom, donnée)
     */
    public LinkedHashMap<String, Object> getSimpleData() {
        LinkedHashMap<String, Object> terug = getExpectedSimpleData();
        terug.putAll(hashDonneesNonAttendues);
        return terug;
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * Seuls les champs simples sont fournis ;
     * pas les fk ni même les intermédiaires !
     *
     * @return une hashmap de (nom, donnée)
     */
    public LinkedHashMap<String, Object> getExpectedSimpleData() {
        return new LinkedHashMap<>(hashDonnees);
    }

    /**
     * Donne une copie du contenu des variables par paires de (nomBD, data)
     * Les noms de variable sont les noms des champs de la BD
     * Seuls les ids sont fournis ; pas les fk ni même les intermédiaires !
     *
     * @return une hashmap de (nom, donnée)
     */
    public LinkedHashMap<String, Object> getOnlyIDs() {
        LinkedHashMap<String, Object> terug = new LinkedHashMap<>();
        terug.put(this.nomID, getId());
        hashFKIntermediaire.forEach((nom, sousObjet) -> {
            terug.put(sousObjet.nomID, sousObjet.getId());
        });
        return terug;
    }

    /**
     * C'est un objet qui permet de générer une entité de l'objet en cours...
     *
     * @return la plupart du temps, il retourne this (ce même objet), mais peut
     *         retourner
     *         un autre objet qui permet via setObjetRacine de modifier plus
     *         précisément l'objet actuel
     */
    public ObjetCSA getObjetRacine() {
        return this;
    }

    /**
     * Attention : l'objet actuel voit toutes ses données remplacées sans condition
     * !
     *
     * @param intrus l'objet externe qui va permettre d'écraser les données
     *               actuelles
     */
    public void setObjetRacine(ObjetCSA intrus) {
        hashDonnees.putAll(intrus.hashDonnees);
        hashClassesDonnees.putAll(intrus.hashClassesDonnees);
        hashDonneesNonAttendues.putAll(intrus.hashDonneesNonAttendues);
        hashListes.putAll(intrus.hashListes);
        hashDependants.putAll(intrus.hashDependants);
        idsDependants.putAll(intrus.idsDependants);
        paires_NomsTables_NomsFKDependants.putAll(intrus.paires_NomsTables_NomsFKDependants);
        hashFKIntermediaire.putAll(intrus.hashFKIntermediaire);
        hashFKs.putAll(intrus.hashFKs);
        mandatories.addAll(intrus.mandatories);
        modifications.addAll(intrus.modifications);
    }

    private HashMap<String, Object> extraire_fk(String nomTable, int id, HashMap<String, Object> hashLevis) {
        String cle = nomTable + "-" + id;
        Object objetLevis = hashLevis.get(cle);
        if (objetLevis == null) {
            // comment("extraire_Fk : Le détail de " + cle + " n'est pas fourni !", "err");
            return new HashMap<>();
        }
        if (objetLevis instanceof ArrayList && ((ArrayList<?>) objetLevis).size() > 1) {
            String nomTableLevis = String.valueOf(((ArrayList<?>) objetLevis).get(0));
            if (nomTableLevis.equals(nomTable) && ((ArrayList<?>) objetLevis).get(1) instanceof HashMap) {
                return (HashMap<String, Object>) ((ArrayList<?>) objetLevis).get(1);
            }
            if (id > 0) {
                comment("extraire_fk : Mauvais nom de table pour détail de " + cle + ". J'attends " + nomTable
                        + "+ hashmap, mais j'obtiens : " + objetLevis, "err");
            }
            return new HashMap<>();
        }
        if (id > 0) {
            comment("extraire_fk : Mauvais format pour détail de " + cle + ". J'attends (nomTable,hasmap), "
                    + "mais j'obtiens : " + objetLevis, "out");
        }
        return new HashMap<>();
    }

    public ObjetCSA getCopy() {
        ObjetCSA clone = getNewInstance();

        // Copie des propriétés simples
        clone.TYPE = this.TYPE;
        clone.nomNOM = this.nomNOM;
        clone.nomID = this.nomID;
        clone.COULEUR = this.COULEUR;
        clone.publicName = this.publicName;
        clone.textColorEnable = this.textColorEnable;

        // Collections immuables/atomiques : créer de nouvelles instances de maps/sets
        clone.hashDonnees.putAll(new LinkedHashMap<>(this.hashDonnees));
        clone.hashClassesDonnees.putAll(new LinkedHashMap<>(this.hashClassesDonnees));
        clone.hashDonneesNonAttendues.putAll(new LinkedHashMap<>(this.hashDonneesNonAttendues));

        // Copier les listes d'objets en profondeur (cloner chaque ObjetCSA)
        for (Map.Entry<String, ArrayList<ObjetCSA>> entree : this.hashListes.entrySet()) {
            ArrayList<ObjetCSA> nouvelleListe = new ArrayList<>();
            ArrayList<ObjetCSA> ancienne = entree.getValue();
            if (ancienne != null) {
                for (ObjetCSA elt : ancienne) {
                    nouvelleListe.add(elt == null ? null : elt.getCopy());
                }
            }
            clone.hashListes.put(entree.getKey(), nouvelleListe);
        }

        // Dependants : copier les maps internes
        for (Map.Entry<String, LinkedHashMap<String, Object>> entree : this.hashDependants.entrySet()) {
            clone.hashDependants.put(entree.getKey(), new LinkedHashMap<>(entree.getValue()));
        }
        clone.idsDependants.putAll(new LinkedHashMap<>(this.idsDependants));

        clone.paires_NomsTables_NomsFKDependants.putAll(new LinkedHashMap<>(this.paires_NomsTables_NomsFKDependants));

        // FK intermédiaires et FK : cloner récursivement les objets
        for (Map.Entry<String, ObjetCSA> entree : this.hashFKIntermediaire.entrySet()) {
            clone.hashFKIntermediaire.put(entree.getKey(),
                    entree.getValue() == null ? null : entree.getValue().getCopy());
        }
        for (Map.Entry<String, ObjetCSA> entree : this.hashFKs.entrySet()) {
            clone.hashFKs.put(entree.getKey(), entree.getValue() == null ? null : entree.getValue().getCopy());
        }

        // Copies des métadonnées/collections
        clone.mandatories.addAll(new LinkedHashSet<>(this.mandatories));
        clone.variablesAAfficher = new ArrayList<>(this.variablesAAfficher);
        clone.nomsPublics = new LinkedHashMap<>(this.nomsPublics);

        // Gestionnaires : cloner chaque gestionnaire en profondeur
        for (Map.Entry<String, GestionnaireImbrications> entree : this.gestionnairesImbrications.entrySet()) {
            GestionnaireImbrications gi = entree.getValue();
            clone.gestionnairesImbrications.put(entree.getKey(), gi == null ? null : gi.getCopy());
        }

        // Intentional: ne pas copier 'modifications' (comme auparavant)
        return clone;
    }

    /**
     * Sert à récupérer les données modifiées de l'objet courant Sous forme de
     * paires (nomBD, valeur)
     * L'ID et les champs intermédiaires sont fournis également !
     * Les mandatories sont inclues également
     *
     * @return une hashmap de (nom, donnéee) correspondant aux modifications
     */
    public HashMap<String, Object> getHashModifications() {
        LinkedHashSet<String> mix = new LinkedHashSet<>(modifications);
        mix.addAll(mandatories);
        return aspirer_donnees(mix);
    }

    private void alertIfNotNull(Object nul, String nom) {
        if (nul != null) {
            System.err.println("Objet " + this + " : la variable " + nom + " a déjà trouvé"
                    + " preneur => risque de losange de la mort !");
        }
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
        return aspirer_donnees(mandatories);
    }

    private HashMap<String, Object> aspirer_donnees(LinkedHashSet<String> hashSet) {
        HashMap<String, Object> terug = new HashMap<>();
        hashSet.forEach(cle -> {
            if (hashFKs.containsKey(cle)) {
                ObjetCSA objetFK = hashFKs.get(cle);
                int sonId = objetFK.getId();
                terug.put(cle, sonId > 0 ? sonId : null); // levis interdit les fk nuls !
                terug.put(objetFK.getType() + "-" + cle, objetFK.getData());
            } else if (hashDonnees.containsKey(cle)) {
                terug.put(cle, hashDonnees.get(cle));
            } else if (hashDonneesNonAttendues.containsKey(cle)) {
                terug.put(cle, hashDonneesNonAttendues.get(cle));
            } else if (hashListes.containsKey(cle)) {
                terug.put(cle, creer_hashmaps_objets(hashListes.get(cle)));
            } else if (estCleDependant(cle)) {
                // objet dépendant : émis comme un FK, mais SANS instancier l'objet
                String nomTable = getNomTableDependant(cle);
                if (nomTable != null) {
                    Integer id = idsDependants.get(nomTable);
                    int sonId = id == null ? 0 : id;
                    terug.put(cle, sonId > 0 ? sonId : null); // levis interdit les fk nuls !
                    terug.put(nomTable + "-" + cle,
                            new HashMap<>(hashDependants.getOrDefault(nomTable, new LinkedHashMap<>())));
                }
            } else {
                for (ObjetCSA objetIntermediaire : hashFKIntermediaire.values()) {
                    Object match = objetIntermediaire.hashDonnees.get(cle);
                    if (match != null) {
                        terug.put(cle, match);
                        break;
                    }
                }
                for (Map.Entry<String, GestionnaireImbrications> paire : gestionnairesImbrications.entrySet()) {
                    GestionnaireImbrications gestionnaire = paire.getValue();
                    if (cle.equals(paire.getKey())) {
                        int fk = gestionnaire.getFk();
                        terug.put(cle, fk > 0 ? fk : null); // levis interdit les fk nuls !
                        if (gestionnaire.getQuartier() != null) {
                            terug.put(gestionnaire.getQuartier().getType() + "-" + cle,
                                    gestionnaire.getQuartier().getSimpleData());
                        }
                        break;
                    }
                }
            }
        });
        terug.put(nomID, getId());
        return terug;
    }

    private ArrayList<HashMap<String, Object>> creer_hashmaps_objets(ArrayList<ObjetCSA> csas) {
        ArrayList<HashMap<String, Object>> liste = new ArrayList<>();
        for (ObjetCSA objetCSA : csas) {
            liste.add(objetCSA.getData());
        }
        return liste;
    }

    public void setMandatories(ArrayList<String> obligatoires) {
        mandatories = new LinkedHashSet<>(obligatoires);
    }

    @Override
    public String toString() {
        return getNom().toUpperCase() + " (" + getType() + " - " + getId() + ")";
    }

    public String toExport() {
        if (getId() <= 0 && getNom().isEmpty()) {
            return " - ";
        }
        return getNom().toUpperCase();
    }

    public void clearModifications() {
        modifications.clear();
        hashFKIntermediaire.forEach((nom, sousObjet) -> {
            sousObjet.clearModifications();
        });
        hashFKs.forEach((nom, sousObjet) -> {
            sousObjet.clearModifications();
        });
        /*
         * hashDependants.forEach((nom, sousObjet) -> {
         * if (sousObjet != null) {
         * sousObjet.modifications.clear();
         * }
         * });
         */
        hashListes.forEach((nom, liste) -> {
            for (ObjetCSA sousObjet : liste) {
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
     * @return vrai si on a trouvé la clé nom dans les données de l'objet (pas les
     *         listes)
     */
    public boolean addModification(String nom) {
        modifications.add(nom);
        if (hashDonnees.containsKey(nom) || hashDependants.containsKey(nom) ||
                hashFKs.containsKey(nom) || hashDonneesNonAttendues.containsKey(nom) ||
                gestionnairesImbrications.containsKey(nom) || hashListes.containsKey(nom)) {
            return true;
        }
        for (ObjetCSA intermediaire : hashFKIntermediaire.values()) {
            if (intermediaire.addModification(nom)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Permet de retourner le nom de la variable NOM
     *
     * @return "nom" dans la plupart des cas, sauf si on la change
     */
    public String getNomNOM() {
        return nomNOM;
    }

    /**
     * Rajoute une variable dont le contenu sera considéré comme obligé
     *
     * @param nom nom de la variable modifiée
     * @return vrai si on a trouvé la clé nom dans les données de l'objet (pas les
     *         listes)
     */
    public boolean addMandatory(String nom) {
        String nomTable = getNomTableDependant(nom);

        if (hashDonnees.containsKey(nom) ||
                (nomTable != null && hashDependants.containsKey(nomTable)) ||
                hashFKs.containsKey(nom) ||
                hashDonneesNonAttendues.containsKey(nom) ||
                hashListes.containsKey(nom)) {
            mandatories.add(nom);
            return true;
        }
        for (ObjetCSA intermediaire : hashFKIntermediaire.values()) {
            if (intermediaire.addMandatory(nom)) {
                return true;
            }
        }
        comment("ObjetCSA : la mandatorisation de " + nom + " n'a pas trouvé preneur dans " + this, "err");
        return false;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ObjetCSA autre) {
            int compareType = getType().compareTo(autre.getType());
            if (compareType != 0)
                return compareType;
            int compareNom = getNom().compareToIgnoreCase(autre.getNom());
            if (compareNom != 0)
                return compareNom;
            int compareToString = toString().compareTo(autre.toString());
            if (compareToString != 0)
                return compareToString;
            return getId() - autre.getId();
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
    public ArrayList<String> getVariablesAAfficher() {
        return new ArrayList<>(variablesAAfficher);
    }

    /**
     * Créé une liste de variables visibles. Attention : un filtre est effectué
     * par rapport aux variables existantes
     *
     * @param visibles la liste de variables à rendre visibles
     */
    public void setVariablesAAfficher(ArrayList<String> visibles) {
        variablesAAfficher.clear();
        for (String elt : visibles) {
            if (hashDonnees.containsKey(elt)) {
                variablesAAfficher.add(elt);
            }
        }
    }

    public LinkedHashMap<String, ObjetCSA> getFKs() {
        return new LinkedHashMap<>(hashFKs);
    }

    public HashMap<String, ObjetCSA> getFKIs() {
        return new HashMap<>(hashFKIntermediaire);
    }

    public HashMap<String, Object> getHashListes() {
        return new HashMap<>(hashListes);
    }

    public HashMap<String, ObjetCSA> getHashDependants() {
        HashMap<String, ObjetCSA> retour = new HashMap<>();
        hashDependants.forEach((cle, hashmap) -> {
            ObjetCSA objetDi = getObjetFromType(cle);
            if (objetDi != null) {
                objetDi.setData(new HashMap<>(hashmap), null, true, false);
                retour.put(cle, objetDi);
            }
        });
        return retour;
    }

    public HashMap<String, LinkedHashMap<String, Object>> getHashmapDependants() {
        return new HashMap<>(hashDependants);
    }

    public HashMap<String, GestionnaireImbrications> getGestionnairesImbrications() {
        return new HashMap<>(gestionnairesImbrications);
    }

    /**
     * renvoie une COPIE de la liste des paires (nom, nom public)
     *
     * @return renvoie une COPIE de la liste des paires (nom, nom public)
     */
    public HashMap<String, String> getListeNomsPublics() {
        return new HashMap<>(nomsPublics);
    }

    /*
     * public boolean add_nom_public(String nomVariable, String nomPublic) {
     * if (hashDonnees.containsKey(nomVariable)) {
     * nomsPublics.put(nomVariable, nomPublic);
     * return true;
     * }
     * for (Map.Entry<String, ObjetCSA> couple : hashFKIntermediaire.entrySet()) {
     * ObjetCSA fk = couple.getValue();
     * if (fk.getSimpleData().containsKey(nomVariable)) {
     * nomsPublics.put(nomVariable, nomPublic);
     * return true;
     * }
     * }
     * if (hashDonneesNonAttendues.containsKey(nomVariable)) {
     * nomsPublics.put(nomVariable, nomPublic);
     * return true;
     * }
     * if (hashFKs.containsKey(nomVariable)) {
     * nomsPublics.put(nomVariable, nomPublic);
     * return true;
     * }
     * for (Map.Entry<String, GestionnaireImbrications> couple :
     * gestionnairesImbrications.entrySet()) {
     * GestionnaireImbrications gestionnaireImbrications = couple.getValue();
     * String nomID = couple.getKey();
     * if (nomID.equals(nomVariable)) {
     * nomsPublics.put(nomVariable, nomPublic);
     * return true;
     * }
     * }
     * // On va à l'intérieur des fks !
     * for (Map.Entry<String, ObjetCSA> couple : hashFKs.entrySet()) {
     * ObjetCSA fk = couple.getValue();
     * if (fk.getSimpleData().containsKey(nomVariable)) {
     * nomsPublics.put(nomVariable, nomPublic);
     * return true;
     * }
     * }
     * // Les listes
     * for (Map.Entry<String, ArrayList<ObjetCSA>> couple : hashListes.entrySet()) {
     * if (couple.getKey().equals(nomVariable)) {
     * nomsPublics.put(nomVariable, nomPublic);
     * return true;
     * }
     * }
     * System.err.println("** ObjetCSA " + this + " : nom public <" + nomPublic +
     * "> non attribué car variable <" + nomVariable + "> inconnue !");
     * return false;
     * }
     */

    /**
     * Définit les variables à afficher et si ce sont des sous-objets,
     * la variable stockée sera cle_nomVariable
     *
     * @param nomVariable variable qui existe dans l'objet
     */
    public void add_variable_a_afficher(String nomVariable) {
        if (hashDonnees.containsKey(nomVariable)) {
            ajouter_variable(nomVariable, variablesAAfficher);
            return;
        }
        for (Map.Entry<String, ObjetCSA> couple : hashFKIntermediaire.entrySet()) {
            String cle = couple.getKey();
            ObjetCSA fki = couple.getValue();
            if (nomVariable.equals(cle)) {
                ajouter_variable(nomVariable, variablesAAfficher);
                return;
            }
            for (Map.Entry<String, Object> donnees : fki.getSimpleData().entrySet()) {
                if (nomVariable.equals(cle + "_" + donnees.getKey())) {
                    ajouter_variable(nomVariable, variablesAAfficher);
                    return;
                }
            }
        }
        if (hashDonneesNonAttendues.containsKey(nomVariable)) {
            ajouter_variable(nomVariable, variablesAAfficher);
            return;
        }
        for (Map.Entry<String, ObjetCSA> couple : hashFKs.entrySet()) {
            String cle = couple.getKey();
            ObjetCSA fk = couple.getValue();
            if (nomVariable.equals(cle)) {
                ajouter_variable(nomVariable, variablesAAfficher);
                return;
            }
            for (Map.Entry<String, Object> donnees : fk.getSimpleData().entrySet()) {
                if (nomVariable.equals(cle + "_" + donnees.getKey())) {
                    ajouter_variable(nomVariable, variablesAAfficher);
                    return;
                }
            }
        }
        for (Map.Entry<String, LinkedHashMap<String, Object>> couple : hashDependants.entrySet()) {
            String cle = couple.getKey();
            LinkedHashMap<String, Object> hashMap = couple.getValue();
            if (nomVariable.equals(cle)) {
                ajouter_variable(nomVariable, variablesAAfficher);
                return;
            }
            for (Map.Entry<String, Object> donnees : hashMap.entrySet()) {
                if (nomVariable.equals(cle + "_" + donnees.getKey())) {
                    ajouter_variable(nomVariable, variablesAAfficher);
                    return;
                }
            }
        }
        for (Map.Entry<String, GestionnaireImbrications> couple : gestionnairesImbrications.entrySet()) {
            String nomID = couple.getKey();
            if (nomID.equals(nomVariable)) {
                ajouter_variable(nomVariable, variablesAAfficher);
                return;
            }
        }
    }

    private void ajouter_variable(String nomVariable, ArrayList<String> arrayList) {
        arrayList.remove(nomVariable);
        arrayList.add(nomVariable);
    }

    /**
     * Donne la valeur stockée dans le nom de variable rentré
     * Si pas trouvé dans les données simples, il y a une recherche dans les fk
     * Si toujours pas trouvé, il recherche des listes, des objetDi dépendants,
     * etc...
     *
     * @param nomVariable c'est le nom de variable dont on veut le contenu, s'il
     *                    existe
     * @return null si pas trouvé; autrement, on peut avoir un primitif, un objetDi
     *         ou une liste !
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
        ObjetCSA objetDi = getFK(nomVariable);
        if (objetDi != null) {
            return objetDi;
        }
        // fk intermédiaire
        ObjetCSA objetIntermediaire = getObjetIntermediaire(nomVariable);
        if (objetIntermediaire != null) {
            return objetIntermediaire;
        }
        // objet dépendant
        ObjetCSA objetDependant = getObjetDependant(nomVariable);
        if (objetDependant != null) {
            return objetDependant;
        }
        // liste
        return getListe(nomVariable);
    }

    /**
     * Indique si le nom de variable est connu dans cet objetDi
     *
     * @param nomVariable c'est le nom de variable dont on veut le contenu, s'il
     *                    existe
     * @return si trouvé une telle variable dans les variables et sous-objets
     */
    public boolean variable_existe(String nomVariable) {
        ArrayList liste = getListe(nomVariable);
        return chercher_valeur(nomVariable) != null || liste != null;
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
            // System.out.println("** HashObjet/changer_nom_variable : impossible de changer
            // " +
            // "le nom de de la variable : " + ancien + " n'existe pas dans " + getType() +
            // " !");
            return false;
        }
        initialiser(nouveau, contenu, nomsPublics.get(ancien));
        // remplacer les nom stockés dans les listes : mandatories, modifs, etc
        remplacer_noms_stockes(ancien, nouveau);

        // les variables id ont un nom fixe par objetDi, donc il faut les updater
        if (nomID.equals(ancien)) {
            nomID = nouveau;
            return true;
        }
        /*
         * if (nomIDParent.equals(ancien)) {
         * nomIDParent = nouveau;
         * }
         */
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
            System.out.println("* ObjetCSA " + this + " /changer_nom_type : impossible de changer puisque "
                    + "le nom de de la variable rentrée est vide !");
            return false;
        }
        String ancien = nomID; // ← sauvegarde de l'ancien nom AVANT de le modifier
        Object contenu = remove(ancien);
        remplacer_noms_stockes(ancien, nouveau);
        nomID = nouveau;
        initialiser(nomID, contenu, nomsPublics.get(ancien)); // ← on cherche avec l'ancien nom
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
            System.err.println("** ObjetCSA " + this + " /changer_nom_type : impossible de changer puisque "
                    + "le nom de de la variable rentrée est vide !");
            return false;
        }
        String ancien = nomNOM;
        Object contenu = remove(nomNOM);
        remplacer_noms_stockes(nomNOM, nouveau);
        nomNOM = nouveau;
        initialiser(nomNOM, contenu, nomsPublics.get(ancien));
        return true;
    }

    /**
     * Permet de mettre à jour une liste à partir d'une hashLevis de détails
     *
     * @param type      le type de la liste
     * @param hashLevis liste de Levis
     */
    public void updater_liste(String type, HashMap<String, Object> hashLevis) {
        int monId = getId();
        // Besoin de l'id pour trouver sa clé
        String cle = getType() + "_" + monId;
        if (hashLevis != null) {
            Object liste_levis = hashLevis.get(cle);
            if (liste_levis instanceof ArrayList) {
                if (((ArrayList<?>) liste_levis).size() > 1) {
                    // on a trouvé une liste de taille > 1
                    // le premier elt de la liste reçue est le nom de la sous-table
                    // les deux types doivent correspondre, évidemment puisqu'un objet peut avoir
                    // plusieurs listes
                    ArrayList<Object> arrayList = (ArrayList<Object>) liste_levis;
                    String nomSousTable = String.valueOf(arrayList.get(0));
                    if (nomSousTable.equals(type)) {
                        ArrayList<ObjetCSA> listeUpdatee = generer_hashobjets(arrayList.subList(1, arrayList.size()),
                                type, hashLevis);
                        hashListes.put(nomSousTable, listeUpdatee);
                    }
                } else {
                    comment("... Liste reçue de Levis non conforme (nomTable, hashmap,...) " + " je reçois => "
                            + liste_levis + "\nModification ignorée !", "err");
                }
            }
        }
        /*
         * if (monId > 0) {
         * comment("updater_liste : je ne trouve pas le détail de " + cle, "out");
         * }
         */
    }

    private ArrayList<ObjetCSA> generer_hashobjets(List<Object> list, String nomTable,
            HashMap<String, Object> hashLevis) {
        ArrayList<ObjetCSA> listeRetour = new ArrayList<>();
        for (Object objet : list) {
            if (objet instanceof HashMap) {
                ObjetCSA nouvelObjet = getObjetFromType(nomTable);
                nouvelObjet.setData((HashMap<String, Object>) objet, hashLevis, true, false);
                listeRetour.add(nouvelObjet);
            }
        }
        return listeRetour;
    }

    private void comment(String msg, String outCode) {
        if (outCode.contains("err")) {
            logger.error("*** ObjetCSA [" + this + "] : " + msg);
        } else {
            logger.debug("* ObjetCSA [" + this + "] : " + msg);
        }
    }

    /**
     * Donne une copie de la liste contenant les éléments du type désiré
     *
     * @param nomTable ne nom de la variable désirée
     * @return null si ce nom-là ne contient pas de liste, sinon la liste de
     *         l'objet
     */
    @SuppressWarnings("unchecked")
    public <T extends ObjetCSA> ArrayList<T> getListe(String nomTable) {
        Object valeur = hashListes.get(nomTable);
        if (valeur instanceof ArrayList) {
            return (ArrayList<T>) valeur;
        }
        return null;
    }

    public String getIdName() {
        return nomID;
    }

    public ObjetCSA getNewInstance() {
        return getObjetFromType(getType());
    }

    /**
     * Donne les valeurs stockées dans les données simples et les
     * données non-attendues
     *
     * @param nomVariable nom de variable
     * @return null si pas trouvé dans hashDonnees ou hashDonneesNonAttendues
     */
    public Object getSimpleData(String nomVariable) {
        if (nomVariable == null) {
            return null;
        }
        Object donneeAttendue = hashDonnees.get(nomVariable);
        if (donneeAttendue != null) {
            return donneeAttendue;
        }
        return hashDonneesNonAttendues.get(nomVariable);
    }

}
