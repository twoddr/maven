package general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FormateurObjets {
    private static final int BROADCAST_CODE = 40;
    private final Coeur coeur;
    private final InformateurObjet informateur;

    public FormateurObjets(Coeur appli, InformateurObjet informateurObjet) {
        this.coeur = Objects.requireNonNull(appli, "coeur ne peut être null");
        this.informateur = Objects.requireNonNull(informateurObjet, "informateur ne peut être null");
    }

    public ArrayList<Object> convertir_liste(ArrayList<?> arrayList) {
        if (!isValidFormat(arrayList)) {
            return new ArrayList<>();
        }

        int code = (Integer) arrayList.get(0);
        String nomTable = (String) arrayList.get(1);

        if (arrayList.size() < 3) {
            printConsole(
                    "Aucun élément à traiter car la liste reçue ne contient pas au moins 3 éléments ! => " + arrayList);
            return new ArrayList<>();
        }

        Object paquet = arrayList.get(2);
        HashMap<String, Object> hashLevis = extractHashLevis(arrayList);

        ArrayList<ObjetCSA> objetsConvertis = convertirPaquetEnObjets(paquet, nomTable, hashLevis);

        if (code == BROADCAST_CODE) {
            coeur.traitement_broadcast(new ArrayList<>(objetsConvertis));
            return new ArrayList<>(List.of("Retour code 40 déjà géré"));
        }

        return new ArrayList<>(objetsConvertis);
    }

    private boolean isValidFormat(ArrayList<?> arrayList) {
        if (arrayList.isEmpty() || !(arrayList.get(0) instanceof Integer)) {
            System.err.println(
                    "*** FormateurObjets.FormateurObjets : paquet reçu non conforme ! Le premier élt doit être integer, mais je reçois => "
                            + arrayList);
            return false;
        }

        if (arrayList.size() <= 1 || !(arrayList.get(1) instanceof String)) {
            System.err.println(
                    "*** FormateurObjets.FormateurObjets/convertir_liste : je m'attendais à un string en position 1, mais j'ai : "
                            + arrayList.get(1) + "\nJe rejete !");
            return false;
        }

        return true;
    }

    private HashMap<String, Object> extractHashLevis(ArrayList<?> arrayList) {
        return arrayList.size() > 3 && arrayList.get(3) instanceof HashMap
                ? (HashMap<String, Object>) arrayList.get(3)
                : null;
    }

    private ArrayList<ObjetCSA> convertirPaquetEnObjets(Object paquet, String nomTable,
            HashMap<String, Object> hashLevis) {
        if (paquet instanceof ArrayList<?> listeRecue) {
            return listeRecue.stream()
                    .filter(objet -> objet instanceof HashMap)
                    .map(objet -> creerObjetCSA((HashMap<String, Object>) objet, nomTable, hashLevis))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else if (paquet instanceof HashMap hashMap) {
            ObjetCSA objet = creerObjetCSA(hashMap, nomTable, hashLevis);
            return objet != null ? new ArrayList<>(List.of(objet)) : new ArrayList<>();
        }

        printConsole("Impossible de créer un objet <" + nomTable + "> car je n'ai pas une hashmap, mais => " + paquet);
        return new ArrayList<>();
    }

    private ObjetCSA creerObjetCSA(HashMap<String, Object> data, String nomTable, HashMap<String, Object> hashLevis) {
        ObjetCSA newObjetCSA = informateur.getObjetFromType(nomTable);
        if (newObjetCSA == null) {
            printConsole("Impossible de créer un objet àpd : " + nomTable);
            return null;
        }

        newObjetCSA.setData(data, hashLevis, true, coeur.isTest());
        return newObjetCSA;
    }

    private void printConsole(String message) {
        coeur.printConsole("* FormateurObjets.FormateurObjets : " + message);
    }
}
