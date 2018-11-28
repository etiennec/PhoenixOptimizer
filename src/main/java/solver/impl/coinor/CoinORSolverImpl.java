package solver.impl.coinor;

import solver.api.model.Goal;
import solver.api.model.LPModel;
import solver.api.model.MIPModel;
import solver.api.model.SolverSolution;
import solver.impl.lp.AbstractLPFileSolverImpl;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by libingc on 10/21/2015.
 */
public class CoinORSolverImpl extends AbstractLPFileSolverImpl {


    /*an example:
    cbc "C:\Program Files (x86)\COIN-OR\1.8.0\doc\Data\Sample\exmip1.lp" solve solu "C:\Program Files (x86)\COIN-OR\1.8.0\doc\Data\Sample\exmip1.lpslol.txt"
    * */
    public static final String COINOR_CMD = "c:\\Tools\\cbc\\bin\\cbc \"%s\" %s solve solution \"%s\"";

    private int maxDurationInSeconds = -1;

    public CoinORSolverImpl() {
    }

    public CoinORSolverImpl(int maxDurationInSeconds) {
        this.maxDurationInSeconds = maxDurationInSeconds;
    }

    class ProcessStreamReader extends Thread {
        String name;
        InputStream is;

        ProcessStreamReader(InputStream is, String name) {
            this.is = is;
            this.name = name;
        }

        public void run() {
            StringBuffer sb = new StringBuffer();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Exception when run command about " + this.name, e);
            }
        }
    }


    protected SolverSolution solveLPFile(File f, LPModel model) {
        try {
            String inputFilePath = f.getCanonicalPath();
            File solutionFile = new File(f.getCanonicalPath()+".sol");
            String solutionFilePath = solutionFile.getCanonicalPath();
            String cmd = getCoinORCommandLine(inputFilePath, solutionFilePath);
            System.out.println("Running solver command: "+cmd);
            Process proc = Runtime.getRuntime().exec(cmd);
            ProcessStreamReader errorReader = new ProcessStreamReader(proc.getErrorStream(), "error");
            ProcessStreamReader outputReader = new ProcessStreamReader(proc.getInputStream(), "output");
            errorReader.start();
            outputReader.start();
            int exitVal = proc.waitFor();
            if (exitVal == 0) {
                return getSolverSolutionFromFile(solutionFilePath, model);
            } else {
                return new SolverSolution(SolverSolution.STATUS.INTERRUPTED);
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception when run COIN-OR command on file", e);
        }
    }

    private String getCoinORCommandLine(String inputFilePath, String solutionFilePath) {
        String timeLimitCommand = "";
        if (maxDurationInSeconds > 0) {
            timeLimitCommand = " sec " + maxDurationInSeconds + " ";
        }
        return String.format(COINOR_CMD, inputFilePath, timeLimitCommand, solutionFilePath);
    }

    private final static int columnIndexOfKey = 2;
    private final static int columnIndexOfValue = 3;

    public static SolverSolution getSolverSolutionFromFile(String solutionFilePath, LPModel model) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(solutionFilePath)));
            SolverSolution solution = null;

            // First line
            String line = br.readLine();

            if (StringUtils.isBlank(line)) {
                throw new RuntimeException("Couldn't retrieve the first line of the CoinOR results file " + solutionFilePath);
            }

            double objValue = 0.0;

            if (line.startsWith("Optimal")) {
                solution = new SolverSolution(SolverSolution.STATUS.OPTIMAL);
                objValue = NumberFormat.getInstance(Locale.US).parse(line.substring(line.indexOf("objective value")+"objective value".length()).trim()).doubleValue();
            } else if (line.startsWith("Stopped on time")) {
                solution = new SolverSolution(SolverSolution.STATUS.INTERRUPTED);
                objValue = NumberFormat.getInstance(Locale.US).parse(line.substring(line.indexOf("objective value")+"objective value".length()).trim()).doubleValue();
            } else {
                solution = new SolverSolution(SolverSolution.STATUS.INFEASIBLE);
            }

            if (model.getGoal().getSense() == Goal.GOAL_SENSE.MAXIMIZE) {
                objValue = - objValue;
            }

            solution.setObjectiveValue(objValue);

            while ((line = br.readLine()) != null) {

                if (!StringUtils.isBlank(line)) {
                    String[] columns = line.split("\\s+");
                    //data format example:
                    //for(String x:columns) System.out.print("["+x+"]");
                    //[][0][Project_25778_Start_1][1][-42.377899]
                    //[][1][Project_26496_Start_1][1][-27.302564]
                    solution.addResult(columns[columnIndexOfKey], Double.parseDouble(columns[columnIndexOfValue]));
                }
            }
            return solution;
        } catch (IOException e) {
            throw new RuntimeException("IOException when get solver solution from file " + solutionFilePath, e);
        } catch (ParseException pe) {
            throw new RuntimeException("Couldn't parse the objective value number in file " + solutionFilePath, pe);
        }
    }

    //this main is used for test
    public static void main(String args[]) throws Exception {
        File inFile = new File("C:\\Program Files (x86)\\COIN-OR\\1.8.0\\doc\\Data\\Sample\\exmip1.lp");
        CoinORSolverImpl t = new CoinORSolverImpl();
        //SolverSolution slu = t.solveLPFile(inFile);
        //System.out.println(slu.getStatus());
        //SolverSolution s = CommandLineSolverImpl.getSolverSolutionFromFile("C:\\Program Files (x86)\\COIN-OR\\1.8.0\\doc\\Data\\Sample\\exmip1.lpslo.txt");

        SolverSolution s = CoinORSolverImpl.getSolverSolutionFromFile("C:\\Kintana\\PPO\\benchmark\\medium_ppo_coin_short.txt", new MIPModel());
        Map<String, Double> map = s.getResults();
        Iterator<String> itr = map.keySet().iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            System.out.println(key + " -> " + map.get(key));
        }
    }
}
