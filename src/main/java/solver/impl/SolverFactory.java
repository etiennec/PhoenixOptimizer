package solver.impl;

import solver.api.SolverService;
import solver.impl.coinor.CoinORSolverImpl;

/**
 * Created by canaud on 10/8/2015.
 */
public class SolverFactory {

    public static SolverService getDefaultSolver() {
        SolverService coinOR = new CoinORSolverImpl();
        return coinOR;
    }
}
