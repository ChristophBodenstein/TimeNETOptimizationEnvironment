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
    protected ArrayList<MeasureType> listOfMeasures = new ArrayList<>();//Liste aller Measures, abfragen von MeasureFormPane
    protected ArrayList<parameter> parameterBase;//Base set of parameters, start/end-value, stepping, etc.
    protected String logFileName = "";
    protected Random randomGenerator;
    protected ArrayList<Integer> parametersToModify = new ArrayList<>();
    //ArrayList of ArrayList used, to make sublists (for ABC...) possible
    protected ArrayList<ArrayList<SimulationType>> population = new ArrayList<>();
    protected SimulationType topMeasure;//temp top measure before implementing top-List
    protected double topDistance = Double.POSITIVE_INFINITY;//temp top distance
    protected int maxNumberOfOptiCycles = 100; //maximum number of cycles, before optimization terminates, even if better solutions are found
    protected int maxNumberOfOptiCyclesWithoutImprovement = support.DEFAULT_GENETIC_MAXWRONGOPTIRUNS;//how many cycles without improvement until break optimization loop
    protected int currentNumberOfOptiCyclesWithoutImprovement = 0;

    protected boolean optimized = false;//Will be set to true, if optimization has finished

    /**
     * Constructor, does nothing special at the moment
     */
    public OptimizerPopulationBased() {
    }

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

    protected void setMeasureTargets(ArrayList<SimulationType> pList) {
        MeasureType activeMeasure;
        MeasureType activeMeasureFromInterface;
        for (int measureCount = 0; measureCount < listOfMeasures.size(); measureCount++) {
            for (int populationCount = 0; populationCount < pList.size(); populationCount++) {
                activeMeasure = pList.get(populationCount).getMeasureByName(listOfMeasures.get(measureCount).getMeasureName());
                activeMeasureFromInterface = listOfMeasures.get(measureCount);//Contains Optimization targets
                activeMeasure.setTargetValue(activeMeasureFromInterface.getTargetValue(), activeMeasureFromInterface.getTargetTypeOf());
            }
        }
    }

    protected ArrayList< ArrayList<parameter>> getNextParameterSetAsArrayList() {
        ArrayList< ArrayList<parameter>> myParametersetList = new ArrayList< >();
        for (ArrayList<SimulationType> p : population) {
            ArrayList<parameter> pArray = p.get(0).getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }

    protected ArrayList< ArrayList<parameter>> getNextParameterSetAsArrayList(ArrayList<SimulationType> simulationData) {
        ArrayList< ArrayList<parameter>> myParametersetList = new ArrayList< >();
        for (SimulationType simulation : simulationData) {
            ArrayList<parameter> pArray = simulation.getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }

    protected ArrayList< ArrayList<parameter>> getNextParameterSetAsArrayList(SimulationType simulation) {
        ArrayList< ArrayList<parameter>> myParametersetList = new ArrayList< >();
        ArrayList<parameter> pArray = simulation.getListOfParameters();
        myParametersetList.add(pArray);
        return myParametersetList;
    }

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
