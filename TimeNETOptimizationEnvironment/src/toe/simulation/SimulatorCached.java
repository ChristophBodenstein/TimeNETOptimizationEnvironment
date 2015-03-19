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

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read
 * log-data
 *
 * @author Christoph Bodenstein
 */
public class SimulatorCached implements Simulator {

    private SimulationCache mySimulationCache = null;
    private ArrayList<SimulationType> myListOfSimulations = null;
    private String logFileName;

    /**
     * Constructor
     */
    public SimulatorCached() {
        logFileName = support.getTmpPath() + File.separator + "SimLog_LocalSimulation_Only_Cache" + Calendar.getInstance().getTimeInMillis() + ".csv";
        support.log("LogfileName:" + logFileName);
    }

    /**
     * inits the simulation, this is neccessary and must be implemented
     *
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param simulationCounterTMP actual Number of simulation, will be
     * increased with every simulation-run
     */
    public void initSimulator(ArrayList<ArrayList<parameter>> listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
        if (mySimulationCache != null) {
            this.myListOfSimulations = mySimulationCache.getListOfCompletedSimulationParsers(listOfParameterSetsTMP, support.getGlobalSimulationCounter());
            //this.simulationCounter=mySimulationCache.getLocalSimulationCounter();
            support.setGlobalSimulationCounter(mySimulationCache.getLocalSimulationCounter());
        } else {
            support.log("No local Simulation file loaded. Simulation not possible.");
        }

        if ((this.myListOfSimulations == null) || (this.myListOfSimulations.size() != listOfParameterSetsTMP.size())) {
            support.log("Not all Simulations found in local Cache.  Will take next possible parametersets from cache.");
            myListOfSimulations = this.mySimulationCache.getNearestParserListFromListOfParameterSets(listOfParameterSetsTMP);
        }

        if (this.myListOfSimulations != null) {
            //copy parameterList of measure to parameter[] of SimulationType for later use
            if (myListOfSimulations.size() > 0) {
                //take first measure in SimulationType for list of parameters
                if (myListOfSimulations.get(0).getMeasures().size() > 0) {
                    for (int i = 0; i < myListOfSimulations.size(); ++i) {
                        //MeasureType firstMeasure = myListOfSimulations.get(i).getMeasures().get(0);
                        myListOfSimulations.get(i).setListOfParameters(listOfParameterSetsTMP.get(i));
                    }
                } else {
                    support.log("No Measures found in parser.");
                }
            } else {
                support.log("List of parsers is empty.");
            }

            if (log) {
                //Print out a log file    
                support.addLinesToLogFileFromListOfParser(myListOfSimulations, logFileName);
            }
        }

    }

    /**
     * Returns the actual status of all simulations
     *
     * @return % of simulatiions that are finished
     */
    public int getStatus() {
        if (this.myListOfSimulations != null) {
            return 100;
        } else {
            return 0;
        }
    }

    /**
     * Returns the actual simulation Counter
     *
     * @return actual simulation counter
     */
    public int getSimulationCounter() {
        return support.getGlobalSimulationCounter();
    }

    /**
     * Gets the list of completed simulations, should be used only if
     * getStatus() returns 100
     *
     * @return list of completed simulations (parsers) which contain all data
     * from the log-files
     */
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
        return this.myListOfSimulations;
    }

    /**
     * Returns the data-source for simulated simulation-runs
     *
     * @return the mySimulationCache
     */
    public SimulationCache getMySimulationCache() {
        return mySimulationCache;
    }

    /**
     * Sets the data-source for simulated simulation-runs
     *
     * @param mySimulationCache the mySimulationCache to set
     */
    public void setMySimulationCache(SimulationCache mySimulationCache) {
        this.mySimulationCache = mySimulationCache;
    }

    /**
     * Returns the calulated optimimum For Benchmark-Functions and Cache-only
     * simulations this can be caluclated. For other simulators, this must be
     * given by user.
     *
     * @return
     */
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {
        //iterate through all cached sims and look for best solution 
        support.log("SimulatorCached: Getting absolute optimum simulation from Cache.");
        ArrayList<SimulationType> mySimulationList = this.mySimulationCache.getSimulationList();
        double distance = Double.MAX_VALUE;
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;

        int numberOfOptimalSimulation = Integer.MIN_VALUE;
        for (int i = 0; i < mySimulationList.size(); i++) {
            SimulationType tmpSim = mySimulationList.get(i);
            MeasureType tmpMeasure = tmpSim.getMeasureByName(targetMeasure.getMeasureName());
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
            support.log("Found optimal Simulation for Measure " + targetMeasure.getMeasureName());

        } else {
            support.log("No Optimum Solution for " + targetMeasure.getMeasureName() + " could be found in cache.");
        }
        MeasureType tmpMeasure = mySimulationList.get(numberOfOptimalSimulation).getMeasureByName(targetMeasure.getMeasureName());
        tmpMeasure.setMinValue(minValue);
        tmpMeasure.setMaxValue(maxValue);
        support.log(support.padRight("Min", 10) + " | " + support.padRight("Mean", 10) + " | " + support.padRight("Max", 10));
        support.log(support.padRight(Double.toString(tmpMeasure.getMinValue()), 10) + " | " + support.padRight(Double.toString(tmpMeasure.getMeanValue()), 10) + " | " + support.padRight(Double.toString(tmpMeasure.getMaxValue()), 10));

        return mySimulationList.get(numberOfOptimalSimulation);
    }

    @Override
    public int cancelAllSimulations() {
    return 0;   
    }
}