/**
 * Simulator which uses log data from already done simulations and returns them.
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import toe.MeasurementForm;
import toe.datamodel.parameter;
import toe.datamodel.SimulationType;
import toe.datamodel.MeasureType;
import toe.helper.SimOptiCallback;
import toe.support;
import toe.typedef;
import toe.typedef.typeOfLogLevel;
import toe.typedef.typeOfTarget;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read
 * log-data
 *
 * @author Christoph Bodenstein
 */
public class SimulatorCached extends Thread implements Simulator, SimOptiCallback {

    //Temporary simulation cache, to store current simulation results. Results find in this cache are taged as "isCache", other results are tagged as fresh simulations
    SimulationCache myTmpSimulationCache = support.getTmpSimulationCache();
    ArrayList<SimulationType> myListOfSimulations = null;
    final String logFileName;
    ArrayList<ArrayList<parameter>> listOfParameterSetsTMP = null;
    boolean log = false;
    private SimOptiCallback listener = null;
    private SimulationType calculatedOptimum = null;

    /**
     * Constructor
     */
    public SimulatorCached() {
        logFileName = support.getTmpPath() + File.separator + "SimLog_" + getClass().getSimpleName() + "_" + Calendar.getInstance().getTimeInMillis() + ".csv";
        support.log("LogfileName:" + logFileName, typeOfLogLevel.INFO);
    }

    /**
     * inits the simulation, this is neccessary and must be implemented
     *
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * increased with every simulation-run
     */
    @Override
    public void initSimulator(ArrayList<ArrayList<parameter>> listOfParameterSetsTMP, boolean log) {
        this.listOfParameterSetsTMP = listOfParameterSetsTMP;
        this.log = log;
        this.myListOfSimulations = new ArrayList<>();
        this.myTmpSimulationCache = support.getTmpSimulationCache();
        //Start this thread
        new Thread(this).start();
    }

    @Override
    public void run() {
        ArrayList<ArrayList<parameter>> listOfUnKnownParametersets = new ArrayList<>();

        if (myTmpSimulationCache != null && support.getMySimulationCache() != null) {
            ArrayList<SimulationType> tmpListOfSimulations = myTmpSimulationCache.getListOfCompletedSimulations(listOfParameterSetsTMP, support.getGlobalSimulationCounter(), listOfUnKnownParametersets);
            if (tmpListOfSimulations != null) {
                support.setGlobalSimulationCounter(support.getGlobalSimulationCounter() + tmpListOfSimulations.size());
                for (int i = 0; i < tmpListOfSimulations.size(); i++) {
                    SimulationType copyOfFoundSimulation = new SimulationType(tmpListOfSimulations.get(i));
                    copyOfFoundSimulation.setIsFromCache(true);
                    support.cacheHitsTmp++;
                    this.myListOfSimulations.add(copyOfFoundSimulation);
                }
            } else {
                myListOfSimulations = new ArrayList<>();
            }
            if (this.myListOfSimulations.size() < listOfParameterSetsTMP.size()) {
                //Some simulation results are missing
                support.log("Not all Simulations found in local Cache.  Will lookup in global cache.", typeOfLogLevel.INFO);
                ArrayList<ArrayList<parameter>> listofUnknownParametersetsInGlobalCache = new ArrayList<>();
                tmpListOfSimulations = support.getMySimulationCache().getListOfCompletedSimulations(listOfUnKnownParametersets, support.getGlobalSimulationCounter(), listofUnknownParametersetsInGlobalCache);

                if (tmpListOfSimulations != null && tmpListOfSimulations.size() > 0) {
                    for (int i = 0; i < tmpListOfSimulations.size(); i++) {
                        tmpListOfSimulations.get(i).setIsFromCache(false);
                        support.cacheHits++;
                    }
                    //Add found results to list of simulations
                    myListOfSimulations.addAll(tmpListOfSimulations);
                }

                if (myListOfSimulations.size() < listOfUnKnownParametersets.size()) {
                    //Still some simulation results are missing...
                    support.log("Not all Simulations found in local Cache.  Will take next possible parametersets from cache.", typeOfLogLevel.INFO);
                    tmpListOfSimulations = support.getMySimulationCache().getNearestSimulationListFromListOfParameterSets(listofUnknownParametersetsInGlobalCache);
                    if (tmpListOfSimulations != null && tmpListOfSimulations.size() > 0) {
                        for (int i = 0; i < tmpListOfSimulations.size(); i++) {
                            tmpListOfSimulations.get(i).setIsFromCache(false);
                            support.cacheHitsNear++;
                        }
                        //Add found results to list of simulations
                        myListOfSimulations.addAll(tmpListOfSimulations);
                    }
                }
            }
        } else {
            support.log("No cache available.", typeOfLogLevel.ERROR);
        }

        if (this.myListOfSimulations != null) {
            for (int i = 0; i < myListOfSimulations.size(); i++) {
                myTmpSimulationCache.addSimulationToCache(new SimulationType(myListOfSimulations.get(i)));
            }

            if (log) {
                //Print out a log file    
                support.addLinesToLogFileFromListOfSimulations(myListOfSimulations, logFileName);
            }
        }

        synchronized (this) {
            notify();
        }
    }

    /**
     * Returns the actual status of all simulations
     *
     * @return % of simulatiions that are finished Will return either 0 or 100
     */
    @Override
    public int getStatus() {
        if (this.myListOfSimulations != null) {
            return 100;
        } else {
            return 0;
        }
    }

    /**
     * Gets the list of completed simulations, should be used only if
     * getStatus() returns 100
     *
     * @return list of completed simulations (parsers) which contain all data
     * from the log-files
     */
    @Override
    public ArrayList<SimulationType> getListOfCompletedSimulations() {
        return this.myListOfSimulations;
    }

    /**
     * Returns the calculated optimum for Benchmark-Functions and Cache-only
     * simulations this can be calculated. For other simulators, this must be
     * given by user.
     *
     * @return
     */
    @Override
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {
        //iterate through all cached sims and look for best solution 
        support.log("SimulatorCached: Getting absolute optimum simulation from Cache.", typeOfLogLevel.INFO);
        ArrayList<SimulationType> mySimulationList = support.getMySimulationCache().getSimulationList();
        double distance = Double.POSITIVE_INFINITY;//Maximum absolute value
        double minValue = Double.POSITIVE_INFINITY;//Maximum absolute value
        double maxValue = Double.NEGATIVE_INFINITY;//Maximum absolute value * -1
        MeasureType tmpMeasure;
        SimulationType tmpSim;

        try {
            tmpMeasure = mySimulationList.get(0).getMeasureByName(targetMeasure.getMeasureName());
            minValue = tmpMeasure.getMeanValue();
            maxValue = tmpMeasure.getMeanValue();
        } catch (Exception e) {
            //INFINITY-Values will be used
        }

        int numberOfOptimalSimulation = Integer.MIN_VALUE;
        for (int i = 0; i < mySimulationList.size(); i++) {
            tmpSim = mySimulationList.get(i);
            tmpMeasure = tmpSim.getMeasureByName(targetMeasure.getMeasureName());
            tmpMeasure.setTargetValue(targetMeasure.getTargetValue(), targetMeasure.getTargetTypeOf());
            if (tmpMeasure.getDistanceFromTarget() <= distance) {
                distance = tmpMeasure.getDistanceFromTarget();
                numberOfOptimalSimulation = i;
            }
            double tmpV = tmpMeasure.getMeanValue();
            if (tmpV <= minValue) {
                minValue = tmpV;
            }
            if (tmpV >= maxValue) {
                maxValue = tmpV;
            }
        }

        if (numberOfOptimalSimulation >= 0) {
            support.log("Found optimal Simulation for Measure " + targetMeasure.getMeasureName(), typeOfLogLevel.INFO);

        } else {
            support.log("No Optimum Solution for " + targetMeasure.getMeasureName() + " could be found in cache.", typeOfLogLevel.INFO);
            return null;
        }
        tmpMeasure = mySimulationList.get(numberOfOptimalSimulation).getMeasureByName(targetMeasure.getMeasureName());
        tmpMeasure.setMinValue(minValue);
        tmpMeasure.setMaxValue(maxValue);
        tmpMeasure.setTargetValue(targetMeasure.getTargetValue(), targetMeasure.getTargetTypeOf());
        support.log(support.padRight("Min", 10) + " | " + support.padRight("Mean", 10) + " | " + support.padRight("Max", 10), typeOfLogLevel.INFO);
        support.log(support.padRight(Double.toString(tmpMeasure.getMinValue()), 10) + " | " + support.padRight(Double.toString(tmpMeasure.getMeanValue()), 10) + " | " + support.padRight(Double.toString(tmpMeasure.getMaxValue()), 10), typeOfLogLevel.INFO);

        SimulationType resultSimulation = mySimulationList.get(numberOfOptimalSimulation);
        /*
         * Set start-end value for every parameter based on Parameterbase
         * This is a workaround, it should be set during read of cache-file
         */
        for (int i = 0; i < resultSimulation.getListOfParameters().size(); i++) {
            try {
                parameter pTmp = resultSimulation.getListOfParameters().get(i);
                pTmp.setEndValue(support.getParameterByName(support.getOriginalParameterBase(), pTmp.getName()).getEndValue());
                pTmp.setStartValue(support.getParameterByName(support.getOriginalParameterBase(), pTmp.getName()).getStartValue());
            } catch (Exception e) {
                support.log("Error setting values for optimum.", typeOfLogLevel.ERROR);
                support.log(e.getLocalizedMessage(), typeOfLogLevel.ERROR);
            }
        }
        return resultSimulation;
    }

    @Override
    public int cancelAllSimulations() {
        return 0;
    }

    @Override
    public String getLogfileName() {
        return this.logFileName;
    }

    @Override
    public boolean isOptimumCalculated() {
        return (this.calculatedOptimum != null);
    }

    @Override
    public void startCalculatingOptimum(SimOptiCallback listener) {
        this.listener = listener;
        //Check if all parametersets are generated (ListOfParameterSetsToBeWritten)
        if (support.getMainFrame().getListOfParameterSetsToBeWritten().size() <= support.DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION) {
            support.setStatusText("Not enaugh Parametersets to simulate for target check.");
            support.log("No Parametersets to simulate for target check.", typeOfLogLevel.INFO);
            listener.operationFeedback("Generate DS first!", typedef.typeOfProcessFeedback.TargetCheckFailed);
            return;
        } else {
            //If not, ask to generate it (not necessary, button is disabled in this case)
        }
        support.setParameterBase(support.getMainFrame().getParameterBase());
        support.setOriginalParameterBase(support.getMainFrame().getParameterBase());
        this.initSimulator(support.getMainFrame().getListOfParameterSetsToBeWritten(), false);
        support.waitForSimulatorAsynchronous(this, this);
    }

    @Override
    public void stopCalculatingOptimum(SimOptiCallback listener) {
        listener.operationFeedback("Targetcheck canceled.", typedef.typeOfProcessFeedback.TargetDiscarded);
        this.calculatedOptimum = null;
    }

    @Override
    public void discardCalculatedOptimum() {
        support.log("CalculatedOptimum will be discarded.", typeOfLogLevel.INFO);
        this.calculatedOptimum = null;
    }

    @Override
    public void operationFeedback(String message, typedef.typeOfProcessFeedback feedback) {
        try {
            switch (feedback) {
                case SimulationSuccessful:
                    ArrayList<SimulationType> simulationResults = this.getListOfCompletedSimulations();
                    double oldDistance = 0.0;
                    ArrayList<SimulationType> foundOptima = new ArrayList<>();

                    MeasureType targetMeasure = support.getOptimizationMeasure();
                    typeOfTarget selectedTypeOfTarget = targetMeasure.getTargetTypeOf();
                    switch (selectedTypeOfTarget) {
                        case min:
                            oldDistance = simulationResults.get(0).getMeasureValueByMeasureName(targetMeasure.getMeasureName());
                            foundOptima.add(simulationResults.get(0));
                            for (int i = 1; i < simulationResults.size(); i++) {
                                support.spinInLabel();
                                support.log("Value of measure is: " + String.valueOf(simulationResults.get(i).getMeasureValueByMeasureName(targetMeasure.getMeasureName())), typeOfLogLevel.VERBOSE);
                                if (oldDistance > simulationResults.get(i).getMeasureValueByMeasureName(targetMeasure.getMeasureName())) {
                                    foundOptima.clear();
                                    foundOptima.add(simulationResults.get(i));
                                    oldDistance = simulationResults.get(i).getMeasureValueByMeasureName(targetMeasure.getMeasureName());
                                } else if (Math.abs(oldDistance - simulationResults.get(i).getMeasureValueByMeasureName(targetMeasure.getMeasureName())) < support.DEFAULT_TARGET_STEPPING) {
                                    foundOptima.add(simulationResults.get(i));
                                }
                            }
                            break;
                        case max:
                            oldDistance = simulationResults.get(0).getMeasureValueByMeasureName(targetMeasure.getMeasureName());
                            foundOptima.add(simulationResults.get(0));
                            for (int i = 1; i < simulationResults.size(); i++) {
                                support.spinInLabel();
                                support.log("Value of measure is: " + String.valueOf(simulationResults.get(i).getMeasureValueByMeasureName(targetMeasure.getMeasureName())), typeOfLogLevel.VERBOSE);
                                if (oldDistance < simulationResults.get(i).getMeasureValueByMeasureName(targetMeasure.getMeasureName())) {
                                    foundOptima.clear();
                                    foundOptima.add(simulationResults.get(i));
                                    oldDistance = simulationResults.get(i).getMeasureValueByMeasureName(targetMeasure.getMeasureName());
                                } else if (Math.abs(oldDistance - simulationResults.get(i).getMeasureValueByMeasureName(targetMeasure.getMeasureName())) < support.DEFAULT_TARGET_STEPPING) {
                                    foundOptima.add(simulationResults.get(i));
                                }
                            }
                            break;
                        case value:
                            oldDistance = simulationResults.get(0).getDistanceToTargetValue();
                            foundOptima.add(simulationResults.get(0));
                            for (int i = 1; i < simulationResults.size(); i++) {
                                support.spinInLabel();
                                support.log("Distance is: " + String.valueOf(simulationResults.get(i).getDistanceToTargetValue()), typeOfLogLevel.VERBOSE);
                                if (oldDistance > simulationResults.get(i).getDistanceToTargetValue()) {
                                    foundOptima.clear();
                                    foundOptima.add(simulationResults.get(i));
                                    oldDistance = simulationResults.get(i).getDistanceToTargetValue();
                                } else if (Math.abs(oldDistance - simulationResults.get(i).getDistanceToTargetValue()) < support.DEFAULT_TARGET_STEPPING) {
                                    foundOptima.add(simulationResults.get(i));
                                }
                            }
                            break;
                    }

                    support.log("Target value(s) found at: ", typeOfLogLevel.RESULT);
                    for (int i = 0; i < foundOptima.size(); i++) {
                        support.log("Parameterset: " + i, typeOfLogLevel.RESULT);
                        for (int c = 0; c < foundOptima.get(i).getListOfParameters().size(); c++) {
                            if (foundOptima.get(i).getListOfParameters().get(c).isIteratable()) {
                                foundOptima.get(i).getListOfParameters().get(c).printInfo(typeOfLogLevel.INFO);
                            }
                        }
                    }
                    support.log("Targetvalue #0 will be used for optimization and statistics.", typeOfLogLevel.RESULT);
                    calculatedOptimum = foundOptima.get(0);
                    support.log("Will set targetvalue to: " + calculatedOptimum.getMeasureValueByMeasureName(targetMeasure.getMeasureName()), typeOfLogLevel.RESULT);
                    MeasurementForm tmpMeasurementForm = (MeasurementForm) support.getMeasureFormPane().getComponent(0);
                    tmpMeasurementForm.setTargetValue(calculatedOptimum.getMeasureValueByMeasureName(targetMeasure.getMeasureName()));

                    if (foundOptima.size() < 1) {
                        //Error checking the target
                        listener.operationFeedback("Opticheck failed.", typedef.typeOfProcessFeedback.TargetCheckFailed);
                        support.log("No optimum found, targetcheck failed.", typeOfLogLevel.ERROR);
                    } else if (foundOptima.size() > 1) {
                        //Not unique
                        listener.operationFeedback("Selected Target is not unique There are " + foundOptima.size() + " same targets.", typedef.typeOfProcessFeedback.TargetValueNotUnique);
                        support.log("The distance to target is: " + oldDistance, typeOfLogLevel.INFO);
                    } else if (foundOptima.size() == 1) {
                        //Exactly one optimum with selected target value was found
                        listener.operationFeedback("Target is unique!", typedef.typeOfProcessFeedback.TargetCheckSuccessful);
                    }

                    break;
                case SimulationCanceled:
                    support.log("Simulation aborted during targetcheck.", typeOfLogLevel.INFO);
                    listener.operationFeedback("Targetecheck aborted.", typedef.typeOfProcessFeedback.TargetCheckFailed);
                    break;
                default:
                    support.log("Unexpected feedback from simulator during targetcheck.", typeOfLogLevel.ERROR);
                    listener.operationFeedback("Unexpected feedback.", typedef.typeOfProcessFeedback.TargetCheckFailed);
            }
        } catch (Exception e) {
            support.log("General error during targetcheck. Maybe Reference to simulator missing?", typeOfLogLevel.ERROR);
            listener.operationFeedback("Error during targetcheck.", typedef.typeOfProcessFeedback.TargetCheckFailed);
            support.log(e.getMessage(), typeOfLogLevel.ERROR);
        }
    }
}
