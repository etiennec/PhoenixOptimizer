package com.ppm.phoenix;

import lib.jebt.xlsx.JebtXlsxReader;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import solver.api.model.*;
import solver.impl.SolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PhoenixProjectOptimizer {

    private static final int NUMBER_OF_ROUNDS = 4;

    public static final void main(String[]args) throws IOException, InvalidFormatException {

        String docPath = ".\\src\\main\\resources\\PhoenixProject.xlsx";
        String templatePath = ".\\src\\main\\resources\\PhoenixProjectTemplate.xlsx";

        if (args == null || args.length == 0) {
            // Using default values
        } else if  (args.length == 1) {
            docPath = args[0];
            // default template path.
        } else {
            docPath = args[0];
            templatePath = args[1];
        }

        System.out.println("Using project file "+docPath);
        System.out.println("Using template file "+templatePath);

        File docF = new File(docPath);
        File templateF = new File(templatePath);

        if (!docF.exists()) {
            throw new RuntimeException("Cannot find project file "+docF.getCanonicalPath());
        }
        if (!templateF.exists()) {
            throw new RuntimeException("Cannot find template file "+templateF.getCanonicalPath());
        }

        XSSFWorkbook template = (XSSFWorkbook)WorkbookFactory.create(templateF);
        InputStream docIS = new FileInputStream(docF);

        System.out.println("Loading Phoenix Project Data...");

        // Loading Phoenix Project data in a Java Object/JSon Object.
        JebtXlsxReader reader = new JebtXlsxReader(template, docIS);
        JSONObject phoenixData = new JSONObject(reader.readData());

        IOUtils.closeQuietly(docIS);

        System.out.println("Retrieved JSON: "+phoenixData.toJSONString());


        System.out.println("Cleaning up JSON...");

        cleanUpData(phoenixData);

        System.out.println("Cleaned Up JSON: "+phoenixData.toJSONString());


        System.out.println("Building Optimization model...");
        MIPModel model = buildMIPModel(phoenixData);

        System.out.println("Solving model...");
        SolverSolution solution = SolverFactory.getDefaultSolver().solveMIP(model);

        System.out.println("Writing best solution...");
        System.out.println("Objective Value: "+solution.getObjectiveValue());

        List<Map.Entry<String, Double>> results = new ArrayList(solution.getResults().entrySet());
        Collections.sort(results, new Comparator<Map.Entry<String, Double>>() {
            @Override public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o1.getKey().compareTo(o2.getKey());            }
        });

        for (Map.Entry<String, Double> entry:results) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }

        System.out.println("Done.");
    }

    /**
     * Some data might come incorrect from Excel, so we clean it up here.
     */
    private static void cleanUpData(JSONObject phoenixData) {


        List<Map> initiatives = (List<Map>)phoenixData.get("initiatives");

        for (Map initiative:  initiatives) {

            // Value should never be empty or null.
            List<Integer> values = (List<Integer> )initiative.get("value");

            for (int i = 0 ; i < values.size() ; i++) {
                if (i >= values.size()) {
                    values.add(0);
                }
                if (values.get(i) == null || "".equals(values.get(i))) {
                    values.set(i, 0);
                }
            }

            // Cando must be true or false.
            List<Boolean> canDos = ( List<Boolean> )initiative.get("cando");

            for (int i = 0 ; i < NUMBER_OF_ROUNDS ; i++) {
                if (i >= canDos.size()) {
                    canDos.add(Boolean.FALSE);
                }
                if (canDos.get(i) == null || "".equals(canDos.get(i)) || !(canDos.get(i) instanceof Boolean) ) {
                    canDos.set(i, Boolean.FALSE);
                }
            }

            Map<String, Long> efforts = (Map<String, Long> )initiative.get("efforts");
            for (String key : efforts.keySet()) {
                if (efforts.get(key) == null || "".equals(efforts.get(key))) {
                    efforts.put(key, 0L);
                }
            }

            // We have to compute the APP effort demand from APPDEV + APPSCRIPT
            long appEffort = 0;
            if (efforts.get("APPDEV") != null) {
                appEffort += efforts.get("APPDEV");
            }
            if (efforts.get("APPSCRIPT") != null) {
                appEffort += efforts.get("APPSCRIPT");
            }
            if (appEffort > 0) {
                efforts.put("APP", appEffort);
            }

            // We have to compute the BUSINESS effort demand from RO + HR + CFO
            long businessEffort = 0;
            if (efforts.get("RO") != null) {
                businessEffort += efforts.get("RO");
            }
            if (efforts.get("HR") != null) {
                businessEffort += efforts.get("HR");
            }
            if (efforts.get("CFO") != null) {
                businessEffort += efforts.get("CFO");
            }

            if (businessEffort > 0) {
                efforts.put("BUSINESS", businessEffort);
            }


        }

    }

    private static MIPModel buildMIPModel(JSONObject phoenixData) {
        MIPModel model = new MIPModel();

        model.setModelName("PhoenixOptimization");

        Goal goal = new Goal(Goal.GOAL_SENSE.MAXIMIZE);
        model.setGoal(goal);

        List<Map> initiatives = ( List<Map>)phoenixData.get("initiatives");

        // Map<Initiative ID, [round1 var, round2 var, round3 var, round4 var]>
        Map<Integer, Variable[]> roundInitiativeBinaryVariables = new HashMap<>();

        // Map<ROLE, Map<Initiative ID, Effort demand>>
        Map<String, Map<Integer, Long>> effortDemand = new HashMap<>();
        Map<String, Long> capacities =  (Map<String, Long> )phoenixData.get("capacities");
        for (String role:capacities.keySet()) {
            effortDemand.put(role, new HashMap<Integer, Long>());
        }

        // One binary variable for each initiative in a round where it can be done, along with the value it brings.
        for (Map initiative:  initiatives) {
            List<Boolean> canDos = (List<Boolean>)initiative.get("cando");
            List<Long> values = (List<Long>)initiative.get("value");

            int id = ((Long)initiative.get("nr")).intValue();

            Variable[] roundsVar = new Variable[4];
            roundInitiativeBinaryVariables.put(id, roundsVar);



            List<Variable> varsForInitiativeInRound = new ArrayList<>();

            for (int i = 0 ; i < NUMBER_OF_ROUNDS ; i++) {
                boolean canDo = canDos.get(i);
                if (canDo) {
                    double value = 0.0d;
                    for (int j = i ; j < NUMBER_OF_ROUNDS ; j++) {
                        value += values.get(j);
                    }

                    Variable initiativeInRound = model.addBinaryVariable(value, "initiative_"+id+"_rnd"+(i+1));
                    roundsVar[i] = initiativeInRound;
                    varsForInitiativeInRound.add(initiativeInRound);
                }

            }

            // Constraint: one initiative is executed at one round at most
            LinearExpression initiativesExpr = new LinearExpression();
            for (Variable var: varsForInitiativeInRound) {
                initiativesExpr.addTerm(1.0, var);
            }
            Constraint c = new Constraint(initiativesExpr, Model.Comparison.LESS_EQUAL, 1.0d, "initiative_"+id+"_at_most_one_round");
            model.addConstraint(c);

            // Store variable information for capacity constraints.
            Map<String, Long> efforts = (Map<String, Long>)initiative.get("efforts");

            for (String role:capacities.keySet()) {
                Long demand = efforts.get(role);

                if (demand != null && demand > 0) {
                    effortDemand.get(role).put(id, demand);
                }
            }

        }


        // Capacity constraints for each round and each capacity.
        for (int i = 0 ; i < NUMBER_OF_ROUNDS ; i ++) {
            for (String role : capacities.keySet()) {
                Map<Integer, Long> effortDemandPerInitative = effortDemand.get(role);

                LinearExpression demands = new LinearExpression();

                for (Map.Entry<Integer, Long> initiativeDemand : effortDemandPerInitative.entrySet()) {
                    if (initiativeDemand.getValue()  == null || initiativeDemand.getValue() <= 0) {
                        continue;
                    }

                    Variable v = roundInitiativeBinaryVariables.get(initiativeDemand.getKey())[i];
                    if (v != null) {
                        // We only include this initiative in the capacity constraint if it can be executed in this round.
                        demands.addTerm(initiativeDemand.getValue(), v);
                    }
                }

                Long capacity = capacities.get(role);
                if (!demands.getTerms().isEmpty()) {
                    // There's demand for this role & round, so we need a constraint.
                    Constraint c = new Constraint(demands, Model.Comparison.LESS_EQUAL, capacity.doubleValue(),
                            "Capacity_" + role + "_rnd" + i);
                    model.addConstraint(c);
                }
            }
        }

        return model;

    }

}
