package solver.api.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by canaud on 9/29/2015.
 */
public class SolverSolution {

    private STATUS status;

    private Map<String, Double> results = new HashMap<>();

    private double objectiveValue = 0.0;

    public SolverSolution(STATUS status) {
        this.status = status;
    }

    public STATUS getStatus() {
        return status;
    }

    public void addResult(String variableName, Double value) {
        results.put(variableName, value);
    }

    public  Map<String, Double> getResults() {
        return new HashMap<>(results);
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public SolverSolution setObjectiveValue(double objectiveValue) {
        this.objectiveValue = objectiveValue;
        return this;
    }

    public enum STATUS {
        OPTIMAL,
        INFEASIBLE,
        INTERRUPTED
    }
}
