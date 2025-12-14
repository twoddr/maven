package general;

public class EUTVAManager {
    /**
     * Fournit le code de catégorie standard européen (pas complet et sujet à modifications)
     *
     * @param pourcentageTVA Le pourcentage qu'on compte appliquer
     * @param suggestion     une aide pour trouver le bon code ?
     *                       Si vide, c'est E
     *                       Si contient VATEX, c'est K
     *                       Sinon, c'est Z
     * @return S pour standard, si rien de spécial trouvé
     */
    public static String getCategoryCode(float pourcentageTVA, String suggestion) {
        if (pourcentageTVA < 1f) {
            // Soit on est exempté soit c'est pour hors UE
            if (suggestion.isEmpty()) {
                return "E";
            }
            if (suggestion.contains("VATEX")) {
                return "K";
            }
            // Hors UE ou livres français
            return "Z";
        }
        if (pourcentageTVA < 6.5f) {
            // Taux réduit
            return "B";
        }
        if (pourcentageTVA < 12.5f) {
            // Taux réduit
            return "A";
        }
        // Taux standard
        return "S";
    }

    /**
     * Fournit le code de catégorie standard européen (pas complet et sujet à modifications)
     *
     * @param pourcentageTVA Le pourcentage qu'on compte appliquer
     * @param suggestion     une aide pour trouver le bon code ?
     *                       Si vide, c'est E
     *                       Si contient VATEX, c'est K
     *                       Sinon, c'est Z
     * @return S pour standard, si rien de spécial trouvé
     */
    public static String getCategoryName(float pourcentageTVA, String suggestion) {
        if (pourcentageTVA < 1f) {
            // Soit on est exempté soit c'est pour hors UE
            if (suggestion.isEmpty()) {
                return "Exempt from VAT";
            }
            if (suggestion.contains("VATEX")) {
                return "VAT exempt for intra-community supply";
            }
            // Hors UE ou livres français
            return "Zero rate";
        }
        if (pourcentageTVA < 6.5f) {
            // Taux réduit
            return "Reduced rate (reduced rate 2)";
        }
        if (pourcentageTVA < 12.5f) {
            // Taux réduit
            return "Reduced rate (reduced rate 1)";
        }
        // Taux standard
        return "Standard rate";
    }

    /**
     * Utile si on connait le code de catégorie VAT de l'UE et pas le taux appliqué
     *
     * @param code c'est un code qui tient sur une lettre majuscule
     * @return Si le code est inconnu (ou vaut S), ce sera 21% (taux de Belgique)
     */
    public static float getPourcentageFromCode(String code) {
        switch (code) {
            case "Z":
            case "E":
            case "K":
                return 0f;
            case "A":
                return 12f;
            case "B":
                return 6f;
            default:
                return 21f;
        }
    }
}
