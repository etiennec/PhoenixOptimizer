package solver.api.model;

/**
 * Created by canaud on 9/29/2015.
 */
public class MIPModel extends LPModel implements Cloneable {

    public Variable addIntegerVariable(long lowerBound, long upperBound, double objectiveValue, String name) {
        return addVariable(lowerBound, upperBound, objectiveValue, name, Variable.Type.INTEGER);
    }

    public Variable addBinaryVariable(double objectiveValue, String name) {
        return addVariable(0, 1, objectiveValue, name, Variable.Type.BINARY);
    }

    public boolean hasBinaryVariable() {
        for (Variable v: variables.values()) {
            if (v.getType() == Variable.Type.BINARY) {
                return true;
            }
        }

        return false;
    }

    public boolean hasIntegerVariable() {
        for (Variable v: variables.values()) {
            if (v.getType() == Variable.Type.INTEGER) {
                return true;
            }
        }

        return false;

    }

    public MIPModel clone() {
        return (MIPModel)super.doClone(new MIPModel());
    }


}
