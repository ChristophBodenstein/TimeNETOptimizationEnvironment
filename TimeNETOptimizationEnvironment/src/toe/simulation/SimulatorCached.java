/**
 * Simulator which uses log data from already done simulations and returns them.
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import toe.datamodel.parameter;
import toe.datamodel.SimulationType;
import toe.datamodel.MeasureType;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read
 * log-data
 *
 * @author Christoph Bodenstein
 */
public class SimulatorCached extends Thread implements Simulator {

    //Temporary simulation cache, to story current simulation results. Results find in this cache are taged as "isCache", other results are tagged as fresh simulations
    SimulationCache myTmpSimulationCache = support.getTmpSimulationCache();
    ArrayList<SimulationType> myListOfSimulations = null;
    final String logFileName;
    ArrayList<ArrayList<parameter>> listOfParameterSetsTMP = null;
    boolean log = false;

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
     * Returns the calculated optimum For Benchmark-Functions and Cache-only
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
                pTmp.setEndValue(support.getParameterByName(support.getParameterBase(), pTmp.getName()).getEndValue());
                pTmp.setStartValue(support.getParameterByName(support.getParameterBase(), pTmp.getName()).getStartValue());
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
}
