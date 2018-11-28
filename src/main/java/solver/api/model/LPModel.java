package solver.api.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by canaud on 9/29/2015.
 */
public class LPModel implements Model {

    private String modelName = "Model";

    protected Map<String, Variable> variables = new LinkedHashMap<>();

    protected Map<String, Constraint> constraints = new LinkedHashMap<>();

    protected Goal goal = new Goal(Goal.GOAL_SENSE.MAXIMIZE);

    public Constraint addConstraint(Constraint constraint) {
        if (constraints.containsKey(constraint.getName())) {
            throw new RuntimeException("There is already a constraint existing named " + constraint.getName());
        }
        constraints.put(constraint.getName(), constraint);
        return constraint;
    }

    public Variable addContinuousVariable(double lowerBound, double upperBound, double objectiveValue, String name) {
        return addVariable(lowerBound, upperBound, objectiveValue, name, Variable.Type.CONTINUOUS);
    }

    protected Variable addVariable(double lowerBound, double upperBound, double objectiveValue, String name, Variable.Type type) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("There is already a variable existing named " + name);
        }
        Variable v = new Variable(lowerBound, upperBound, objectiveValue, name, type);
        variables.put(name, v);
        return v;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public Collection<Variable> getVariables() {
        return variables.values();
    }

    public Variable getVariable(String variableName) {
        if (variableName == null) {
            return null;
        }
        return variables.get(variableName);
    }

    public Collection<Constraint> getConstraints() {
        return constraints.values();
    }

    public boolean hasBinaryVariable() {
        return false;
    }

    public boolean hasIntegerVariable() {
        return false;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Ensures that each constraint only has constant on its Right-Hand-Side
     */
    public void normalizeConstraints() {
        for (Map.Entry<String, Constraint> cons : constraints.entrySet()) {
            constraints.put(cons.getKey(), normalizeConstraint(cons.getValue()));
        }
    }

    private Constraint normalizeConstraint(Constraint constraint) {
        switch (constraint.getType()) {
            case EXPR_EXPR: {
                LinearExpression newExpression = new LinearExpression();
                newExpression.addLinearExpression(constraint.getLeftExpression());
                newExpression.minusLinearExpression(constraint.getRightExpression());

                return convertToExprVal(newExpression, constraint.getComparison(), constraint.getName());
            }

            case EXPR_VAR: {
                LinearExpression newExpression = new LinearExpression();
                newExpression.addLinearExpression(constraint.getLeftExpression());
                LinearExpression tmpExpression = new LinearExpression();
                tmpExpression.addTerm(1, constraint.getRightVariable());
                newExpression.minusLinearExpression(tmpExpression);
                return convertToExprVal(newExpression, constraint.getComparison(), constraint.getName());
            }
            case EXPR_VAL:
                LinearExpression newExpression = new LinearExpression();
                newExpression.addLinearExpression(constraint.getLeftExpression());
                LinearExpression tmpExpression = new LinearExpression();
                tmpExpression.addConstant(constraint.getRightValue());
                newExpression.minusLinearExpression(tmpExpression);
                return convertToExprVal(newExpression, constraint.getComparison(), constraint.getName());
            case VAR_VAR: {
                LinearExpression leftExpression = new LinearExpression();
                leftExpression.addTerm(1, constraint.getLeftVariable());
                LinearExpression rightExpression = new LinearExpression();
                rightExpression.addTerm(1, constraint.getRightVariable());
                leftExpression.minusLinearExpression(rightExpression);
                return convertToExprVal(leftExpression, constraint.getComparison(), constraint.getName());
            }
            case VAR_VAL: {
                LinearExpression leftExpression = new LinearExpression();
                leftExpression.addTerm(1, constraint.getLeftVariable());
                LinearExpression rightExpression = new LinearExpression();
                rightExpression.setConstant(constraint.getRightValue());
                leftExpression.minusLinearExpression(rightExpression);
                return convertToExprVal(leftExpression, constraint.getComparison(), constraint.getName());
            }
            default:
                throw new RuntimeException("Unknown Constraint Type " + constraint.getType().toString());
        }
    }

    private Constraint convertToExprVal(LinearExpression expression, Model.Comparison comparison, String name) {
        Double constant = expression.getConstant();
        expression.setConstant(0);
        return new Constraint(expression, comparison, -constant, name);
    }

    public LPModel clone() {
        return doClone(new LPModel());
    }


    protected LPModel doClone(LPModel clone) {
        clone.setGoal(this.goal);
        clone.setModelName(this.modelName);

        for (Variable v : variables.values()) {
            clone.addVariable(v.getLowerBound(), v.getUpperBound(), v.getObjectiveValue(), v.getName(), v.getType());
        }

        for (Constraint c : constraints.values()) {
            clone.addConstraint(c.cloneForModel(clone));
        }

        return clone;
    }
}
