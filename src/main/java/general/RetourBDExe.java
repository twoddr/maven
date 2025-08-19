package general;

import java.util.ArrayList;

public class RetourBDExe implements GestionnaireRetour {
    String nomTable;
    int code;
    ThreadRetourBD thread;

    public RetourBDExe(int codeAttendu,
                       String nomTableAttendu,
                       ThreadRetourBD aExecuter) {
        nomTable = nomTableAttendu;
        code = codeAttendu;
        thread = aExecuter;
    }

    @Override
    public void traitementRetour(ArrayList arrayList) {
        if (arrayList.size() < 2) {
            System.err.println("** RetourBDExe/traitementRetour : mauvais paquet reçu. J'attends (code,nom,liste)," +
                    " mais j'obtiens : " + arrayList);
            return;
        }
        if (arrayList.get(0) instanceof Integer &&
                arrayList.get(1) instanceof String) {
            int code = (int) arrayList.get(0);
            String nomTable = String.valueOf(arrayList.get(1));
            thread.go(code, nomTable, arrayList.size() > 2 ? arrayList.get(2) : null);
            return;
        }
        System.err.println("** RetourBDExe/traitementRetour : mauvais paquet reçu. J'attends (int,String,liste)," +
                " mais j'obtiens : " + arrayList);
    }
}
