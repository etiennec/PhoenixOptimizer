package solver.api;

import solver.api.model.LPModel;
import solver.api.model.MIPModel;
import solver.api.model.SolverSolution;

/**
 * Created by canaud on 9/29/2015.
 */
public interface SolverService {
    public SolverSolution solveMIP(MIPModel mipModel);

    public SolverSolution solveLP(LPModel lpModel);
}
