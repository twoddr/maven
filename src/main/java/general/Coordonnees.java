package general;

import java.util.ArrayList;

public class Coordonnees {
    private int x = 0;
    private int y = 0;

    public Coordonnees() {

    }

    public Coordonnees(int i, int j) {
        x = i;
        y = j;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coordonnees) {
            Coordonnees autre = (Coordonnees) obj;
            return autre.getX() == x && autre.getY() == y;
        }
        return super.equals(obj);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public ArrayList<Integer> getCoordonnees() {
        ArrayList<Integer> terug = new ArrayList<>();
        terug.add(x);
        terug.add(y);
        return terug;
    }
}
