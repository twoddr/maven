package general;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AXZ {
    Integer i;
    
    public AXZ(Integer input) {
        i = input;
    }
    
    public Boolean verif(String x) {
        return x != null && x.equals(g_k());
    }
    
    private String g_k() {
        int reste = i;
        ArrayList<Integer> listeReponses = new ArrayList<>();
        listeReponses.add(0);
        for (int nombre = 2; nombre <= i; nombre++) {
            if (isPremier(nombre)) {
                while (reste % nombre == 0) {
                    incrementer_dernier(listeReponses);
                    reste = reste / nombre;
                    if (reste == 1) {
                        return extraire_nombre(listeReponses);
                    }
                }
                listeReponses.add(0);
            }
        }
        
        return "0";
    }
    
    private String extraire_nombre(ArrayList<Integer> integers) {
        AtomicReference<String> string = new AtomicReference<>("");
        AtomicInteger nombreEnCours = new AtomicInteger(-1);
        AtomicInteger compte = new AtomicInteger(1);
        
        for (int indice = 0; indice < integers.size(); indice++) {
            int nombre = integers.get(indice);
            if (nombre == nombreEnCours.get()) {
                compte.updateAndGet(x -> x + 1);
                if (indice == integers.size() - 1) {
                    string.updateAndGet(v -> v + compte.get());
                }
            } else {
                String suffixe = string.get().isEmpty() ? "" : compte.get() + " ";
                string.updateAndGet(v -> v + suffixe + nombre);
                nombreEnCours.set(nombre);
                compte.set(1);
            }
        }
        
        return string.get();
    }
    
    private void incrementer_dernier(ArrayList<Integer> reponses) {
        if (!reponses.isEmpty()) {
            int dernier = reponses.size() - 1;
            int ancien = reponses.get(dernier);
            if (ancien < 9) {
                Integer nouveau = ancien + 1;
                reponses.remove(dernier);
                reponses.add(nouveau);
            }
        }
    }
    
    private boolean isPremier(int n) {
        boolean isPremier = true;
        if (n < 0) {
            isPremier = false;
        } else if (n != 0 && n != 1) {
            for (int i = 2; i <= n / 2; i++) {
                if (n != i && n % i == 0) {
                    isPremier = false;
                    break;
                }
            }
        }
        return isPremier;
    }
}
