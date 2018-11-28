package solver.api.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by canaud on 9/29/2015.
 */
public class Variable {

    private double lowerBound;
  private double upperBound;
    private double objectiveValue;
    private String name;
    private Type type;

    public Variable(double lowerBound, double upperBound, double objectiveValue, String name, Type type) {
        if (StringUtils.isBlank(name)) {
            throw new RuntimeException("Variable name cannot be empty or null");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.objectiveValue = objectiveValue;
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable)) return false;

        Variable variable = (Variable) o;

        return name.equals(variable.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        CONTINUOUS,
        BINARY,
        INTEGER
    }
}
