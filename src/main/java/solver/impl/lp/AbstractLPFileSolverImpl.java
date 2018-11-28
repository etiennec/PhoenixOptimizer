package solver.impl.lp;

import solver.api.SolverService;
import solver.api.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * This class can be used by any solver that can take an LP file as input. All the logic to generate the LP file is already there.
 */
public abstract class AbstractLPFileSolverImpl implements SolverService {

    public static final String LEADING_SPACES = "  ";

    @Override
    public SolverSolution solveMIP(MIPModel mipModel) {
        return solve(mipModel);
    }

    @Override
    public SolverSolution solveLP(LPModel lpModel) {
        return solve(lpModel);
    }

    protected SolverSolution solve(LPModel model) {

        File f = getLPFile(model.getModelName());

        FileWriter fw = null;

        try {
            fw = new FileWriter(f);

            fw.write("\\ LP File " + f.getCanonicalPath() + "\n\n");

            switch (model.getGoal().getSense()) {
                case MINIMIZE:
                    fw.write("Minimize\n");
                    break;
                default:
                    fw.write("Maximize\n");
                    break;
            }

            boolean first = true;
            for (Variable var : model.getVariables()) {

                if (var.getObjectiveValue() == 0.0) {
                    continue;
                }

                fw.write(LEADING_SPACES);
                if (first) {
                    first = false;
                } else {
                    fw.write("+ ");
                }

                if (var.getObjectiveValue() != 1.0) {
                    fw.write(String.valueOf(var.getObjectiveValue()));
                }
                fw.write(" " + var.getName() + System.lineSeparator());
            }

            fw.write("Subject To\n");

            model.normalizeConstraints();

            for (Constraint constraint : model.getConstraints()) {
                fw.write(LEADING_SPACES);
                fw.write(constraint.toString() + System.lineSeparator());
            }

            fw.write("Bounds\n");

            for (Variable var : model.getVariables()) {
                if (var.getType() == Variable.Type.BINARY) {
                    continue;
                }
                fw.write(LEADING_SPACES);
                fw.write(var.getLowerBound() + " <= " + var.getName() + " <= " +var.getUpperBound() + System.lineSeparator());
            }

            if (model.hasBinaryVariable()) {
                fw.write("Binaries\n");

                for (Variable var : model.getVariables()) {
                    if (var.getType() == Variable.Type.BINARY) {
                        fw.write(LEADING_SPACES);
                        fw.write(var.getName() + System.lineSeparator());
                    }
                }
            }

            if (model.hasIntegerVariable()) {
                fw.write("General\n");

                for (Variable var : model.getVariables()) {
                    if (var.getType() == Variable.Type.INTEGER) {
                        fw.write(LEADING_SPACES);
                        fw.write(var.getName() + System.lineSeparator());
                    }
                }
            }

            fw.write("End\n");

        } catch (IOException e) {
            throw new RuntimeException("Error when creating LP File", e);
        } finally {
            if (fw != null) {
                try {
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error when closing fileWriter", e);
                }
            }
        }


        SolverSolution solution = solveLPFile(f, model);

        Map<String, Double> results = solution.getResults();

        for (Variable v : model.getVariables()) {
            if (!results.containsKey(v.getName())) {
                solution.addResult(v.getName(), 0.0);
            }
        }

        return solution;
    }

    private File getLPFile(String modelName) {
        File f = new File(getLPFilePath(modelName));
        if (f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Error when creating LP File", e);
        }
        return f;
    }

    protected String getLPFilePath(String modelName) {
        modelName = modelName.replaceAll("\\W+", "");
        return "c:\\temp\\"+modelName+".lp";
    }

    /**
     * Method to implement in order to solve the LP File with whatever solver we have.
     */
    protected abstract SolverSolution solveLPFile(File f, LPModel model);
}
