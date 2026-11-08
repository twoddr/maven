package general;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formatage et lecture des nombres à la française.
 * <p>
 * Les symboles sont fixés explicitement (indépendamment de la {@link Locale} de
 * la machine et de la Locale par défaut) :
 * <ul>
 * <li>séparateur de milliers : espace insécable {@code U+00A0} ;</li>
 * <li>séparateur décimal : virgule {@code ','}.</li>
 * </ul>
 * L'espace insécable est choisie (plutôt que l'espace fine insécable
 * {@code U+202F} fournie par le JDK pour {@code Locale.FRANCE}) car
 * {@code U+202F} est ignorée (donc perdue : « 15000 ») par les polices PDF
 * classiques (Helvetica / WinAnsi d'iText), alors que {@code U+00A0} est rendue
 * correctement partout (JavaFX comme PDF).
 * <p>
 * Les valeurs de type entier ({@link Integer}, {@link Long}, ...) ne sont
 * jamais groupées pour ne pas transformer une année (« 2024 ») ou un code en
 * « 2 024 » / « 1 234 ».
 *
 * @author twoddr
 */
public final class FormatNombre {

    /** Séparateur de milliers utilisé partout : espace insécable. */
    public static final String SEPARATEUR_MILLIERS = "\u00A0";

    /** Séparateur décimal français. */
    public static final char SEPARATEUR_DECIMAL = ',';

    /** Motif sans zéro inutile, jusqu'à 16 décimales (précision d'un double). */
    private static final String MOTIF_DECIMAL = "#,##0.################";

    /** Motif sans décimale (montants entiers). */
    private static final String MOTIF_ENTIER = "#,##0";

    /**
     * {@link DecimalFormat} n'est pas thread-safe : une instance par fil.
     */
    private static final ThreadLocal<DecimalFormat> FORMAT_DECIMAL =
            ThreadLocal.withInitial(() -> creerFormateur(MOTIF_DECIMAL));

    private static final ThreadLocal<DecimalFormat> FORMAT_ENTIER =
            ThreadLocal.withInitial(() -> creerFormateur(MOTIF_ENTIER));

    private FormatNombre() {
        // classe utilitaire
    }

    private static DecimalFormat creerFormateur(String motif) {
        DecimalFormatSymbols symboles = DecimalFormatSymbols.getInstance(Locale.FRANCE);
        symboles.setGroupingSeparator(SEPARATEUR_MILLIERS.charAt(0));
        symboles.setDecimalSeparator(SEPARATEUR_DECIMAL);
        DecimalFormat format = new DecimalFormat(motif, symboles);
        format.setGroupingUsed(true);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format;
    }

    /**
     * Formate un nombre pour l'affichage : milliers groupés (sauf pour les types
     * entiers) et virgule décimale, sans zéro inutile.
     * <p>
     * Exemples : {@code 1,2} ; {@code 123,4} ; {@code 1 234,5} ;
     * {@code 1 234 567,8} ; {@code 15 000} ; {@code 0,125}.
     *
     * @param valeur le nombre à formater ({@code null} accepté)
     * @return la représentation française, ou "" si {@code valeur} est null
     */
    public static String nombre(Number valeur) {
        if (valeur == null) {
            return "";
        }
        if (valeur instanceof Integer || valeur instanceof Long
                || valeur instanceof Short || valeur instanceof Byte
                || valeur instanceof BigInteger) {
            return String.valueOf(valeur.longValue());
        }
        if (valeur instanceof Float) {
            return nombre(((Float) valeur).floatValue());
        }
        if (valeur instanceof BigDecimal) {
            return FORMAT_DECIMAL.get().format(valeur);
        }
        return nombre(valeur.doubleValue());
    }

    /**
     * Formate un nombre décimal : milliers groupés et virgule décimale.
     *
     * @param valeur le nombre à formater
     * @return la représentation française (ex : {@code 1 234 567,8})
     */
    public static String nombre(double valeur) {
        return FORMAT_DECIMAL.get().format(valeur);
    }

    /**
     * Formate un nombre décimal (variante {@code float}).
     * <p>
     * La conversion passe par {@link BigDecimal} construit sur la
     * représentation courte du {@code float} ({@link Float#toString(float)})
     * afin d'éviter le bruit binaire : {@code 18.33f} donne {@code 18,33} et non
     * {@code 18,3299999...}.
     *
     * @param valeur le nombre à formater
     * @return la représentation française (ex : {@code 1 234,5})
     */
    public static String nombre(float valeur) {
        return FORMAT_DECIMAL.get().format(new BigDecimal(Float.toString(valeur)));
    }

    /**
     * Formate un nombre décimal en limitant le nombre de décimales affichées
     * (arrondi « half up »), milliers groupés et virgule décimale. À utiliser
     * pour les valeurs calculées (moyennes, taux, ratios).
     *
     * @param valeur    le nombre à formater
     * @param decimales nombre maximal de décimales (0 = pas de décimale)
     * @return la représentation française (ex : {@code 22,9})
     */
    public static String nombre(double valeur, int decimales) {
        if (decimales <= 0) {
            return entier(valeur);
        }
        return creerFormateur("#,##0." + "#".repeat(decimales)).format(valeur);
    }

    /**
     * Formate un nombre sans décimale, milliers groupés (montants FCFA, etc.).
     *
     * @param valeur le nombre à formater
     * @return la représentation française arrondie à l'unité (ex : {@code 15 000})
     */
    public static String entier(double valeur) {
        return FORMAT_ENTIER.get().format(valeur);
    }

    /**
     * Formate un pourcentage.
     *
     * @param valeur le pourcentage (ex : 18.5)
     * @return la représentation française suivie de « % » (ex : {@code 18,5 %})
     */
    public static String pourcent(double valeur) {
        return nombre(valeur, 1) + " %";
    }

    /**
     * Formate un montant en francs CFA (sans décimale).
     *
     * @param valeur le montant
     * @return la représentation française suivie de « FCFA » (ex : {@code 15 000 FCFA})
     */
    public static String montantFCFA(double valeur) {
        return entier(valeur) + " FCFA";
    }

    /**
     * Formate un nombre suivi d'une unité (tailles de fichiers, durées, ...) en
     * limitant l'affichage à une décimale.
     *
     * @param valeur le nombre
     * @param unite  l'unité (ex : "KB", "ans")
     * @return la représentation française suivie de l'unité (ex : {@code 1,5 KB})
     */
    public static String avecUnite(double valeur, String unite) {
        return avecUnite(valeur, 1, unite);
    }

    /**
     * Formate un nombre suivi d'une unité en limitant le nombre de décimales.
     *
     * @param valeur    le nombre
     * @param decimales nombre maximal de décimales
     * @param unite     l'unité (ex : "KB", "ans")
     * @return la représentation française suivie de l'unité (ex : {@code 1,5 KB})
     */
    public static String avecUnite(double valeur, int decimales, String unite) {
        return nombre(valeur, decimales) + " " + unite;
    }

    /**
     * Normalise un texte saisi ou affiché pour le rendre analysable par
     * {@link Float#parseFloat(String)} / {@link Integer#parseInt(String)} :
     * suppression des espaces (insécables, fines ou normaux), des symboles
     * d'unité et des lettres ; conversion de la virgule décimale en point.
     * <p>
     * Exemples : {@code "15 000 FCFA"} → {@code "15000"} ;
     * {@code "1 234,5"} → {@code "1234.5"} ; {@code "-18,5 %"} → {@code "-18.5"}.
     *
     * @param texte le texte à normaliser ({@code null} accepté)
     * @return une chaîne au format « anglais », ou {@code null} si le texte ne
     *         contient aucun chiffre
     */
    public static String normaliser(String texte) {
        if (texte == null) {
            return null;
        }
        String nettoye = texte
                .replace('\u00A0', ' ')
                .replace('\u202F', ' ')
                .replace('\u2009', ' ');
        boolean virguleDecimale = nettoye.indexOf(',') >= 0;
        StringBuilder resultat = new StringBuilder(nettoye.length());
        boolean separateurDecimal = false;
        for (int i = 0; i < nettoye.length(); i++) {
            char c = nettoye.charAt(i);
            if (c >= '0' && c <= '9') {
                resultat.append(c);
            } else if ((c == '-' || c == '+') && resultat.length() == 0) {
                resultat.append(c);
            } else if (c == ',' && virguleDecimale && !separateurDecimal) {
                resultat.append('.');
                separateurDecimal = true;
            } else if (c == '.' && !virguleDecimale && !separateurDecimal) {
                resultat.append('.');
                separateurDecimal = true;
            }
        }
        String valeur = resultat.toString();
        if (valeur.isEmpty() || valeur.equals("-") || valeur.equals("+") || valeur.equals(".")) {
            return null;
        }
        return valeur;
    }

    /**
     * Indique si un texte représente un nombre exploitable.
     *
     * @param texte le texte à tester
     * @return vrai si {@link #normaliser(String)} renvoie une valeur
     */
    public static boolean estNumerique(String texte) {
        return normaliser(texte) != null;
    }

    /**
     * Convertit un texte (éventuellement formaté à la française) en
     * {@link Float}.
     *
     * @param texte le texte à convertir
     * @return le {@link Float} correspondant, ou {@code null} si le texte n'est
     *         pas un nombre
     */
    public static Float versFloat(String texte) {
        String valeur = normaliser(texte);
        if (valeur == null) {
            return null;
        }
        try {
            return Float.valueOf(valeur);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convertit un texte (éventuellement formaté à la française) en
     * {@link Double}.
     *
     * @param texte le texte à convertir
     * @return le {@link Double} correspondant, ou {@code null} si le texte n'est
     *         pas un nombre
     */
    public static Double versDouble(String texte) {
        String valeur = normaliser(texte);
        if (valeur == null) {
            return null;
        }
        try {
            return Double.valueOf(valeur);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convertit un texte (éventuellement formaté à la française) en
     * {@link Integer}. Une partie décimale éventuelle est arrondie.
     *
     * @param texte le texte à convertir
     * @return l'{@link Integer} correspondant, ou {@code null} si le texte n'est
     *         pas un nombre
     */
    public static Integer versInteger(String texte) {
        String valeur = normaliser(texte);
        if (valeur == null) {
            return null;
        }
        try {
            return Integer.valueOf(valeur);
        } catch (NumberFormatException e) {
            // peut-être une partie décimale : "15.0"
        }
        try {
            return (int) Math.round(Double.parseDouble(valeur));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

