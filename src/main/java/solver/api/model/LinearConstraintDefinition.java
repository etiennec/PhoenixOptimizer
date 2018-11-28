package solver.api.model;


/**
 * Created by lopeziv on 7/31/2015.
 */
public class LinearConstraintDefinition {
    private LinearExpression lhsExpr;
    private LinearExpression rhsExpr;
    private Double rhsValue;
    private Double lhsValue;

    public LinearConstraintDefinition(){
        this.lhsExpr = new LinearExpression();
        this.rhsExpr = new LinearExpression();
        this.rhsValue = 0.0;
        this.lhsValue = 0.0;
    }

    public LinearExpression getLhsExpr() {
        return lhsExpr;
    }

    public void setLhsExpr(LinearExpression lhsExpr) {
        this.lhsExpr = lhsExpr;
    }

    public LinearExpression getRhsExpr() {
        return rhsExpr;
    }

    public void setRhsExpr(LinearExpression rhsExpr) {
        this.rhsExpr = rhsExpr;
    }

    public Double getRhsValue() {
        return rhsValue;
    }

    public void setRhsValue(Double rhsValue) {
        this.rhsValue = rhsValue;
    }

    public Double getLhsValue() {
        return lhsValue;
    }

    public void setLhsValue(Double lhsValue) {
        this.lhsValue = lhsValue;
    }

    public void addRhsValue(Double value){
        this.rhsValue += value;
    }

    public void addLhsValue(Double value){
        this.lhsValue += value;
    }

    public void addRhsTerm(Double coeff, Variable var){
        this.rhsExpr.addTerm(coeff, var);
    }

    public void addLhsTerm(Double coeff, Variable var){
        this.lhsExpr.addTerm(coeff, var);
    }
}
