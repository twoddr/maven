package general;

import java.io.Serializable;

/**
 * Created by didiertowe on 12/05/17.
 */
public class Capsule implements Serializable {
    private Object variable;

    public Capsule() {
        variable = new Object();
    }

    public Capsule(Object x) {
        variable = x;
    }

    public Object getVariable() {
        return variable;
    }

    public void setVariable(Object var) {
        variable = var;
    }

    public Class getType() {
        return variable.getClass();
    }
}
