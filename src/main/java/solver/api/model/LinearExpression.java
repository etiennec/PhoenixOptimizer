package solver.api.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by canaud on 9/29/2015.
 */
public class LinearExpression {

    private double constant = 0.0;

    private Map<Variable, Double> terms = new HashMap<>();

    public void addTerm(double coeff, Variable var) {
        if (terms.get(var) != null) {
            terms.put(var, terms.get(var) + coeff);
        } else {
            terms.put(var, coeff);
        }
    }

    public List<Pair<Double, Variable>> getTerms() {
        List<Pair<Double, Variable>> termsList = new ArrayList<>();
        for (Map.Entry<Variable, Double> term : terms.entrySet()) {
            termsList.add(Pair.of(term.getValue(), term.getKey()));
        }
        return termsList;
    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }

    public void addConstant(double constant) {
        this.constant += constant;
    }

    public void addLinearExpression(LinearExpression expr) {
        this.addConstant(expr.getConstant());
        for (Pair<Double, Variable> term : expr.getTerms()) {
            this.addTerm(term.getKey(), term.getValue());
        }
    }

    public void minusLinearExpression(LinearExpression expr) {
        this.addConstant(-expr.getConstant());
        for (Pair<Double, Variable> term : expr.getTerms()) {
            this.addTerm(-term.getKey(), term.getValue());
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean firstTerm = true;
        for (Map.Entry<Variable, Double> term : terms.entrySet()) {
            double coeff = term.getValue();
            Variable var = term.getKey();

            if (coeff >= 0.0) {
                if (!firstTerm) {
                    sb.append(" + ");
                }
            } else {
                sb.append(" - ");
                coeff = 0 - coeff;
            }

            firstTerm = false;

            sb.append(coeff + " " + var.getName());
        }
        if (constant > 0.0) {
            sb.append(" + " + constant);
        } else if (constant < 0.0) {
            sb.append(" - " + (0 - constant));
        }
        return sb.toString();
    }

    public LinearExpression cloneForModel(LPModel model) {
        LinearExpression clone = new LinearExpression();
        clone.setConstant(this.getConstant());
        for (Pair<Double, Variable> term : this.getTerms()) {
            clone.addTerm(term.getKey(), model.getVariable(term.getValue().getName()));
        }
        return clone;
    }
}
