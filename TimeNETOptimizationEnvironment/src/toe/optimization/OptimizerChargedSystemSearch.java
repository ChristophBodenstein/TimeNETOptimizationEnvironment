/*
 * Optimization-Algorithm implemented by Andy Seidel during Diploma Thesis 2014
 * 
 * Orinal paper [10KT]: 
 * Kaveh, Talatahari:
 * A novel heuristic optimization method: charged system search (2010)
 */
package toe.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import toe.SimOptiFactory;
import toe.datamodel.MeasureType;
import toe.datamodel.parameter;
import toe.datamodel.SimulationType;
import toe.simulation.Simulator;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author A. Seidel
 */
public class OptimizerChargedSystemSearch extends OptimizerPopulationBased implements Runnable, Optimizer {

    private int numberOfCharges = support.getOptimizerPreferences().getPref_CSS_PopulationSize(); //number of active charges to explore design space
    private double maxAttraction = support.getOptimizerPreferences().getPref_CSS_MaxAttraction();//limit of attraction-force of 2 charges

    private double[] distances; //current Distance of all objects
    private double[] powerOfCharges; // attraction power of every object
    private double[][] speedOfCharges; //current speed vector for all objects with all parameters;

    public OptimizerChargedSystemSearch() {
        logFileName = support.getTmpPath() + File.separator + "Optimizing_with_CSS_" + Calendar.getInstance().getTimeInMillis() + "_ALL" + ".csv";
    }

    private void calculatePower() {
        //calculate attraction of each charge
        for (int i = 0; i < population.size(); ++i) {
            powerOfCharges[i] = 1 / population.get(i).get(0).getDistanceToTargetValue(); //temporary function for calculating attraction. TODO: test other funktions
        }
    }

    private void updatePositions() {
        calculatePower();

        //currentCharge = the charge to be moved
        //compareCharge = the charge, which attracts the currentCharge
        for (int currentChargeNumber = 0; currentChargeNumber < numberOfCharges; ++currentChargeNumber) {
            ArrayList<parameter> currentParameterSet = getParameters(currentChargeNumber);

            for (int compareChargeNumber = 0; compareChargeNumber < numberOfCharges; ++compareChargeNumber) {
                ArrayList<parameter> compareParemeterSet = getParameters(compareChargeNumber);
                if (currentChargeNumber != compareChargeNumber) {
                    double attraction = powerOfCharges[compareChargeNumber] / powerOfCharges[currentChargeNumber];

                    if (attraction > maxAttraction) {
                        attraction = maxAttraction;
                    }

                    for (int parameterNumber = 0; parameterNumber < speedOfCharges[currentChargeNumber].length; ++parameterNumber) {
                        if (currentParameterSet.get(parameterNumber).isIteratableAndIntern()) {
                            double currentValue = currentParameterSet.get(parameterNumber).getValue();
                            double compareValue = compareParemeterSet.get(parameterNumber).getValue();
                            double currentSpeed = speedOfCharges[currentChargeNumber][parameterNumber];

                            double diffSpeed = (compareValue - currentValue) * attraction / maxAttraction;
                            currentSpeed += diffSpeed;
                            currentValue += currentSpeed;

                            //safety check to prevent charges to "fly" over the border
                            if (currentValue < currentParameterSet.get(parameterNumber).getStartValue()) {
                                currentValue = currentParameterSet.get(parameterNumber).getStartValue();
                            } else if (currentValue > currentParameterSet.get(parameterNumber).getEndValue()) {
                                currentValue = currentParameterSet.get(parameterNumber).getEndValue();
                            }

                            speedOfCharges[currentChargeNumber][parameterNumber] = currentSpeed;
                            currentParameterSet.get(parameterNumber).setValue(currentValue);
                        }
                    }
                }
            }
            setParameters(currentParameterSet, currentChargeNumber);
        }
    }

    @Override
    public void run() {
        int optiCycleCounter = 0;
        population = createRandomPopulation(numberOfCharges, false);
        distances = new double[numberOfCharges];
        powerOfCharges = new double[numberOfCharges];
        speedOfCharges = new double[numberOfCharges][parameterBase.size()];

        Arrays.fill(distances, 0);
        for (int i = 0; i < numberOfCharges; ++i) {
            double[] newArray = new double[parameterBase.size()];
            Arrays.fill(newArray, 0);
            speedOfCharges[i] = newArray;
        }
        Arrays.fill(powerOfCharges, 0);

        Simulator mySimulator = SimOptiFactory.getSimulator();
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), false);
        //support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
        synchronized (mySimulator) {
            try {
                mySimulator.wait();
            } catch (InterruptedException ex) {
                support.log("Problem waiting for end of non-cache-simulator.", typeOfLogLevel.ERROR);
            }
        }
        ArrayList<SimulationType> simulationResults = mySimulator.getListOfCompletedSimulationParsers();
        population = getPopulationFromSimulationResults(simulationResults);

        //int simulationCounter = 0;
        while (optiCycleCounter < this.maxNumberOfOptiCycles) {
            updatePositions();
            if (currentNumberOfOptiCyclesWithoutImprovement >= maxNumberOfOptiCyclesWithoutImprovement) {
                support.log("Too many optimization cycles without improvement. Ending optimization.", typeOfLogLevel.INFO);
                break;
            }

            ArrayList< ArrayList<parameter>> parameterList = getNextParameterSetAsArrayList();

            for (ArrayList<parameter> pArray : parameterList) {
                pArray = roundToStepping(pArray);
            }

            //System.out.println("Number of Parameters in: " + parameterList.get(0).size());
            mySimulator = SimOptiFactory.getSimulator();
            mySimulator.initSimulator(parameterList, false);
            //support.waitForEndOfSimulator(mySimulator, simulationCounter, support.DEFAULT_TIMEOUT);
            synchronized (mySimulator) {
                try {
                    mySimulator.wait();
                } catch (InterruptedException ex) {
                    support.log("Problem waiting for end of non-cache-simulator.", typeOfLogLevel.ERROR);
                }
            }
            simulationResults = mySimulator.getListOfCompletedSimulationParsers();
            support.addLinesToLogFileFromListOfParser(simulationResults, logFileName);
            population = getPopulationFromSimulationResults(simulationResults);

            //calculateDistances();
            updateTopMeasure();
            printPopulationDistances();
            ++optiCycleCounter;
        }
        for (int measureCount = 0; measureCount < listOfMeasures.size(); measureCount++) {
            String measureName = listOfMeasures.get(measureCount).getMeasureName();
            MeasureType activeMeasure = topMeasure.getMeasureByName(measureName);
            support.log(activeMeasure.getStateAsString(), typeOfLogLevel.INFO);
        }
        support.log("CCS Finished", typeOfLogLevel.INFO);

    }

    private ArrayList<parameter> getParameters(int indexOfCharge) {
        return population.get(indexOfCharge).get(0).getListOfParameters();
    }

    private void setParameters(ArrayList<parameter> parameterList, int indexOfCharge) {
        population.get(indexOfCharge).get(0).setListOfParameters(parameterList);
    }

    /**
     * returnes the number of charges used for optimization
     *
     * @return the number of charges
     */
    public int getNumberOfCharges() {
        return this.numberOfCharges;
    }

    /**
     * sets the number of charges, if its a least one. zero or negative values
     * are ignored
     *
     * @param newNumberOfCharges the new number of charges
     */
    public void setNumberOfCharges(int newNumberOfCharges) {
        if (newNumberOfCharges > 0) {
            this.numberOfCharges = newNumberOfCharges;
        }
    }

    /**
     * return maximum number of optimization cycles before breaking up
     *
     * @return the current maximum number of optimization cycles
     */
    public int getMaxNumberOfOptiCycles() {
        return this.maxNumberOfOptiCycles;
    }

    /**
     * sets maximum number of optimization cycles. Has to be at least 1,
     * otherwise it is ignored.
     *
     * @param newMaxNumberOfOtpiCycles the new maximum number of optimization
     * cycles
     */
    public void setMaxNumberOfOptiCycles(int newMaxNumberOfOtpiCycles) {
        if (newMaxNumberOfOtpiCycles > 0) {
            this.maxNumberOfOptiCycles = newMaxNumberOfOtpiCycles;
        }
    }

    /**
     * gets maximum number of optimization cycles without improvement, before
     * breaking optimization loop.
     *
     * @return the maximum number of optimization cycles without improvemet
     */
    public int getMaxNumberOfOptiCyclesWithoutImprovement() {
        return this.maxNumberOfOptiCyclesWithoutImprovement;
    }

    /**
     * sets maximum number of optimization cycles without improvement. Has to be
     * at least 1, otherwise it is ignored.
     *
     * @param newMaxNumberOfOptiCyclesWithoutImprovement the new maximum number
     * of optimization cycles without improvement
     */
    public void setMaxNumberOfOptiCyclesWithoutImprovement(int newMaxNumberOfOptiCyclesWithoutImprovement) {
        if (newMaxNumberOfOptiCyclesWithoutImprovement > 0) {
            this.maxNumberOfOptiCyclesWithoutImprovement = newMaxNumberOfOptiCyclesWithoutImprovement;
        }
    }

    /**
     * Set the logfilename this is useful for multi-optimization or if you like
     * specific names for your logfiles
     *
     * @param name Name (path) of logfile
     */
    @Override
    public void setLogFileName(String name) {
        this.logFileName = name;
    }

    /**
     * Returns the used logfileName
     *
     * @return name of logfile
     */
    @Override
    public String getLogFileName() {
        return this.logFileName;
    }
}
