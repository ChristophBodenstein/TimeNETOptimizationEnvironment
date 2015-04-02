/*
 * abstract class for population-based optimizers 
 * included because of a lot repeating code
 */
package toe.optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import toe.MainFrame;
import toe.datamodel.MeasureType;
import toe.datamodel.SimulationType;
import toe.datamodel.parameter;
import toe.support;

/**
 *
 * @author Andy Seidel
 */
public abstract class OptimizerPopulationBased implements Runnable, Optimizer {

    private MainFrame parent = null;

    /**
     * List of all measres to be optimized
     */
    protected ArrayList<MeasureType> listOfMeasures = new ArrayList<>();

    /**
     * Base set of parameters, start/end-value, stepping, etc.
     */
    protected ArrayList<parameter> parameterBase;

    /**
     * Name of logfile for Optimization
     */
    protected String logFileName = "";

    /**
     * RandomGenerator object to be used for rnd
     */
    protected Random randomGenerator;

    /**
     * List of parameters which can be modified by opti algo
     */
    protected ArrayList<Integer> parametersToModify = new ArrayList<>();
    //ArrayList of ArrayList used, to make sublists (for ABC...) possible

    /**
     * actual population, a list of Simulations (incl. result and parametersets)
     */
    protected ArrayList<ArrayList<SimulationType>> population = new ArrayList<>();

    /**
     * temp top measure before implementing top-List If Optimization has ended
     * this is the result (found optimum)
     */
    protected SimulationType topMeasure;

    /**
     * distance of top measure to target
     */
    protected double topDistance = Double.POSITIVE_INFINITY;

    /**
     * maximum number of cycles, before optimization terminates, even if better
     * solutions are found
     */
    protected int maxNumberOfOptiCycles = 100;

    /**
     * How many cycles without improvement until optimization is aborted
     */
    protected int maxNumberOfOptiCyclesWithoutImprovement = support.DEFAULT_GENETIC_MAXWRONGOPTIRUNS;

    /**
     * Counter fo opti cycles without improvement
     */
    protected int currentNumberOfOptiCyclesWithoutImprovement = 0;

    /**
     * If this is set to true, the last topMeasure can be accessed via
     * getOptimum
     */
    protected boolean optimized = false;//Will be set to true, if optimization has finished

    /**
     * Constructor, does nothing special at the moment
     */
    public OptimizerPopulationBased() {
    }

    /**
     * Init the optimization, loads the default values and targets from
     * support-class and starts optimization
     *
     */
    @Override
    public void initOptimizer() {
        this.parent = support.getMainFrame();// parentTMP;
        this.parameterBase = parent.getParameterBase();
        this.listOfMeasures = parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
        support.log("# of Measures to be optimized: " + this.listOfMeasures.size());

        randomGenerator = new Random();

        //check for iteratable parameters
        for (int i = 0; i < this.parameterBase.size(); ++i) {
            if (this.parameterBase.get(i).isIteratable()) {
                parametersToModify.add(i);
            }
        }

        //Start this Thread
        new Thread(this).start();
    }

    /**
     * Returns Optimum. Remeber, it should return null until Optimum is found
     *
     * @return found optimum
     */
    @Override
    public SimulationType getOptimum() {
        if (optimized) {
            return topMeasure;
        } else {
            return null;
        }
    }

    /**
     * Create random poulation (list of simulationtypes) at start of
     * optimization
     *
     * @param populationSize Size of population to create
     * @param ignoreStepping false -> parameters use given stepping, true ->
     * parameters can have every real value in range
     * @return list of SimulationTypeLists (start population)
     */
    protected ArrayList<ArrayList<SimulationType>> createRandomPopulation(int populationSize, boolean ignoreStepping) {
        ArrayList<ArrayList<SimulationType>> newPopulation = new ArrayList<>();

        for (int i = 0; i < populationSize; ++i) {
            newPopulation.add(new ArrayList<SimulationType>());
            SimulationType newSim = new SimulationType();

            ArrayList<parameter> pArray = support.getCopyOfParameterSet(parameterBase);
            for (int j = 0; j < pArray.size(); ++j) {
                //creates a random value between start and end value for each parameter
                double newValue = pArray.get(j).getStartValue() + Math.random() * (pArray.get(j).getEndValue() - pArray.get(j).getStartValue());
                pArray.get(j).setValue(newValue);
            }
            if (!ignoreStepping) {
                pArray = roundToStepping(pArray);
            }

            newSim.setListOfParameters(pArray);
            newPopulation.get(i).add(newSim);
        }

        return newPopulation;
    }

    /**
     * Within a list of SimulationTypes it will set the optimization target
     * values to every measure. So the remaining distance can be calculated.
     *
     * @param pList ArrayList of SimulationTypes
     */
    protected void setMeasureTargets(ArrayList<SimulationType> pList) {
        MeasureType activeMeasure;
        MeasureType activeMeasureFromInterface;
        for (MeasureType listOfMeasure : listOfMeasures) {
            for (SimulationType pList1 : pList) {
                activeMeasure = pList1.getMeasureByName(listOfMeasure.getMeasureName());
                activeMeasureFromInterface = listOfMeasure; //Contains Optimization targets
                activeMeasure.setTargetValue(activeMeasureFromInterface.getTargetValue(), activeMeasureFromInterface.getTargetTypeOf());
            }
        }
    }

    /**
     * Returns created population as ArrayList of Parametersets
     *
     * @return ArrayList of ArrayList of parameters
     */
    protected ArrayList< ArrayList<parameter>> getNextParameterSetAsArrayList() {
        ArrayList< ArrayList<parameter>> myParametersetList = new ArrayList<>();
        for (ArrayList<SimulationType> p : population) {
            ArrayList<parameter> pArray = p.get(0).getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }

    /**
     * Transoforms List of SimulationTypes to ArrayList of Parametersets
     *
     * @param simulationData ArrayList of SimulationTypes to be tranformed into
     * ArrayList of ParameterLists
     * @return ArrayList of ArrayList of parameters
     */
    protected ArrayList< ArrayList<parameter>> getNextParameterSetAsArrayList(ArrayList<SimulationType> simulationData) {
        ArrayList< ArrayList<parameter>> myParametersetList = new ArrayList<>();
        for (SimulationType simulation : simulationData) {
            ArrayList<parameter> pArray = simulation.getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }

    /**
     * Transoforms one SimulationType object to ArrayList of Parametersets (wth
     * one member)
     *
     * @param simulation SimulationType object to be returned as the one member
     * of ArrayList of Parametersets
     * @return ArrayList of ArrayList of parameters
     */
    protected ArrayList< ArrayList<parameter>> getNextParameterSetAsArrayList(SimulationType simulation) {
        ArrayList< ArrayList<parameter>> myParametersetList = new ArrayList<>();
        ArrayList<parameter> pArray = simulation.getListOfParameters();
        myParametersetList.add(pArray);
        return myParametersetList;
    }

    /**
     * Converts a List of SimulationTypes into an ArrayList of
     * SimulationTypeLists
     *
     * @param results List of Simulationtypes
     * @return List of SimulationTypeLists
     *
     */
    protected ArrayList< ArrayList<SimulationType>> getPopulationFromSimulationResults(ArrayList<SimulationType> results) {
        ArrayList< ArrayList<SimulationType>> newPopulation = new ArrayList<>();

        for (SimulationType result : results) {
            ArrayList<SimulationType> newIndividual = new ArrayList<>();
            newIndividual.add(result);
            newPopulation.add(newIndividual);
        }
        return newPopulation;
    }

    /**
     *
     * @param p array of parameters to be rounded
     * @return
     */
    protected ArrayList<parameter> roundToStepping(ArrayList<parameter> p) {
        double currentValue;
        double currentStepping;
        for (int i = 0; i < p.size(); ++i) {
            currentValue = p.get(i).getValue();
            currentStepping = p.get(i).getStepping();

            currentValue = Math.round(currentValue / currentStepping) * currentStepping;

            if (currentValue < p.get(i).getStartValue()) {
                currentValue = p.get(i).getStartValue();
            } else if (currentValue > p.get(i).getEndValue()) {
                currentValue = p.get(i).getEndValue();
            }

            p.get(i).setValue(currentValue);
        }
        return p;
    }

    /**
     * Sorts the food sources by rank
     *
     * @param population ArrayList of food sources to be sorted
     * @return
     */
    protected ArrayList<ArrayList<SimulationType>> sortPopulation(ArrayList<ArrayList<SimulationType>> population) {
        Collections.sort(population, new Comparator<ArrayList<SimulationType>>() {
            @Override
            public int compare(ArrayList<SimulationType> a, ArrayList<SimulationType> b) {
                return Double.compare(a.get(0).getDistanceToTargetValue(), b.get(0).getDistanceToTargetValue());
            }
        });
        return population;
    }

    /**
     * Prints distance to target of every population member
     */
    public void printPopulationDistances() {
        for (int i = 0; i < population.size(); ++i) {
            String logString = "Distance " + i + " \t: " + population.get(i).get(0).getDistanceToTargetValue();
            support.log(logString);
        }
    }

    /**
     * Checks if a new Top Measure exists in population if yes, topMeasure
     * member variable is set new.
     */
    protected void updateTopMeasure() {
        boolean newTopMeasurefound = false;
        for (int i = 0; i < population.size(); ++i) {
            if (population.get(i).get(0).getDistanceToTargetValue() < topDistance) {
                topDistance = population.get(i).get(0).getDistanceToTargetValue();
                topMeasure = new SimulationType(population.get(i).get(0));
                newTopMeasurefound = true;
            }
        }
        if (newTopMeasurefound) {
            currentNumberOfOptiCyclesWithoutImprovement = 0;
        } else {
            ++currentNumberOfOptiCyclesWithoutImprovement;
        }
    }

    @Override
    public abstract void run();
}
