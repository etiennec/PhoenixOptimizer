package solver.api.model;

/**
 * Created by lopeziv on 7/31/2015.
 */
public class Constraint {

    private LinearExpression leftExpression;
    private LinearExpression rightExpression;
    private Variable leftVariable;
    private Variable rightVariable;
    private Double rightValue;
    private final Model.Comparison comparison;
    private final String name;
    private final Type type;

    /**
     * Used for cloning only.
     */
    protected Constraint(LinearExpression leftExpression, LinearExpression rightExpression, Variable leftVariable, Variable rightVariable, Double rightValue, Model.Comparison comparison, String name, Type type) {

        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        this.leftVariable = leftVariable;
        this.rightVariable = rightVariable;
        this.rightValue = rightValue;
        this.comparison = comparison;
        this.name = name;
        this.type = type;
    }

    public Constraint(LinearExpression leftExpression, Model.Comparison comparison, LinearExpression rightExpression, String name) {
        this.type = Type.EXPR_EXPR;
        this.leftExpression = leftExpression;
        this.comparison = comparison;
        this.rightExpression = rightExpression;
        this.name = name;
    }

    public Constraint(LinearExpression leftExpression, Model.Comparison comparison, Variable rightVariable, String name) {
        this.type = Type.EXPR_VAR;
        this.leftExpression = leftExpression;
        this.comparison = comparison;
        this.rightVariable = rightVariable;
        this.name = name;
    }

    public Constraint(LinearExpression leftExpression, Model.Comparison comparison, double rightValue, String name) {
        this.type = Type.EXPR_VAL;
        this.leftExpression = leftExpression;
        this.comparison = comparison;
        this.rightValue = rightValue;
        this.name = name;
    }

    public Constraint(Variable leftVariable, Model.Comparison comparison, Variable rightVariable, String name) {
        this.type = Type.VAR_VAR;
        this.leftVariable = leftVariable;
        this.comparison = comparison;
        this.rightVariable = rightVariable;
        this.name = name;
    }

    public Constraint(Variable leftVariable, Model.Comparison comparison, double rightValue, String name) {
        this.type = Type.VAR_VAL;
        this.leftVariable = leftVariable;
        this.comparison = comparison;
        this.rightValue = rightValue;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LinearExpression getLeftExpression() {
        return leftExpression;
    }

    public LinearExpression getRightExpression() {
        return rightExpression;
    }

    public Variable getLeftVariable() {
        return leftVariable;
    }

    public Variable getRightVariable() {
        return rightVariable;
    }

    public Double getRightValue() {
        return rightValue;
    }

    public Type getType() {
        return type;
    }

    public Model.Comparison getComparison() {
        return comparison;
    }

    /**
     * Clone this constraint using variables from the model passed in parameter.
     */
    public Constraint cloneForModel(LPModel model) {
        return new Constraint(
                leftExpression != null ? leftExpression.cloneForModel(model) : null,
                rightExpression != null ? rightExpression.cloneForModel(model) : null,
                leftVariable != null ? model.getVariable(leftVariable.getName()) : null,
                rightVariable != null ? model.getVariable(rightVariable.getName()) : null,
                this.getRightValue(),
                this.getComparison(),
                this.getName(),
                this.getType());
    }

    public enum Type {
        EXPR_EXPR,
        EXPR_VAR,
        EXPR_VAL,
        VAR_VAR,
        VAR_VAL
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName() + ": ");

        if (leftExpression != null) {
            sb.append(leftExpression.toString());
        } else if (leftVariable != null) {
            sb.append(leftVariable.getName());
        }
        sb.append(" " + getComparison().getSign() + " ");
        if (rightExpression != null) {
            sb.append(rightExpression.toString());
        } else if (rightVariable != null) {
            sb.append(rightVariable.getName());
        } else if (rightValue != null) {
            sb.append(rightValue.toString());
        }

        return sb.toString();
    }
}
