/**
 * Simulator which uses log data from already done simulations and returns them.
 * If not all parametersets can be found in cache, the rest is simulated
 * distributed
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import toe.datamodel.parameter;
import toe.datamodel.SimulationType;
import toe.support;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read
 * log-data
 *
 * @author Christoph Bodenstein
 */
public class SimulatorCachedLocal extends SimulatorCached {

    private SimulationCache mySimulationCache = null;
    private ArrayList<SimulationType> myListOfSimulationParsers = null;
    private String logFileName;
    private int status;
    private Simulator myLocalSimulator = getNoCacheSimulator();

    /**
     * Constructor
     */
    public SimulatorCachedLocal() {
        logFileName = support.getTmpPath() + File.separator + "SimLog_LocalSimulation_with_Cache" + Calendar.getInstance().getTimeInMillis() + ".csv";
        support.log("LogfileName:" + logFileName);
    }

    /**
     * inits the simulation, this is neccessary and must be implemented
     *
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param simulationCounterTMP actual Number of simulation, will be
     * increased with every simulation-run
     * @param log true if own logfile should be written, else false
     */
    @Override
    public void initSimulator(ArrayList< ArrayList<parameter>> listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
        
        this.myListOfSimulationParsers = null;
        ArrayList< ArrayList<parameter>> remainingParametersets = new ArrayList< ArrayList<parameter>>();
        status = 0;
        //this.simulationCounter=simulationCounterTMP;

        if (mySimulationCache != null) {
            this.myListOfSimulationParsers = mySimulationCache.getListOfCompletedSimulationParsers(listOfParameterSetsTMP, support.getGlobalSimulationCounter());
        } else {
            support.log("No local Simulation file loaded. Will build my own cache from scratch.");
            this.mySimulationCache = support.getMySimulationCache();
        }

        if (this.myListOfSimulationParsers == null) {
            support.log("No Simulation found in local Cache. Starting simulation.");
            this.myListOfSimulationParsers = new ArrayList<SimulationType>();
        }

        status = myListOfSimulationParsers.size() * 100 / listOfParameterSetsTMP.size();

        if (this.myListOfSimulationParsers.size() < listOfParameterSetsTMP.size()) {
            support.log("Some simulations were missing in cache. Will simulate them local/distributed.");

            for (ArrayList<parameter> myParameterset : listOfParameterSetsTMP) {
                remainingParametersets.add(myParameterset);
            }

            for (ArrayList<parameter> myParameterset : listOfParameterSetsTMP) {
                for (SimulationType myListOfSimulationParser : this.myListOfSimulationParsers) {
                    if (this.mySimulationCache.compareParameterList(myListOfSimulationParser.getListOfParametersFittedToBaseParameterset(), myParameterset)) {
                        remainingParametersets.remove(myParameterset);
                    }
                }
            }
            support.log("Size of Remaining ParameterList is " + remainingParametersets.size());
            //Find simulations that are not already simulated

            
            myLocalSimulator.initSimulator(remainingParametersets, support.getGlobalSimulationCounter(), false);
            support.waitForEndOfSimulator(myLocalSimulator, support.getGlobalSimulationCounter(), support.DEFAULT_TIMEOUT);

            myListOfSimulationParsers.addAll(myLocalSimulator.getListOfCompletedSimulationParsers());
            status = myListOfSimulationParsers.size() * 100 / listOfParameterSetsTMP.size();

            support.log("Size of resultList is " + myListOfSimulationParsers.size());

            this.mySimulationCache.addListOfSimulationsToCache(myListOfSimulationParsers);
        }

        if (this.myListOfSimulationParsers != null) {
            support.log("Adding " + myListOfSimulationParsers.size() + " Results to logfile.");
            //Print out a log file
            support.addLinesToLogFileFromListOfParser(myListOfSimulationParsers, logFileName);
            //Update global simulation counter
            support.setGlobalSimulationCounter(support.getGlobalSimulationCounter() + myListOfSimulationParsers.size());
            support.log("SimulationCounter is now: " + support.getGlobalSimulationCounter());
        }

    }

    /**
     * Returns the actual status of all simulations
     *
     * @return % of simulatiions that are finished
     */
    @Override
    public int getStatus() {
        return status;
    }

    /**
     * Returns new Simulator-object to be used, if parametersets are not in
     * cache
     *
     * @return Simulator object (local)
     */
    public Simulator getNoCacheSimulator() {
        return new SimulatorLocal();
    }

    /**
     * Returns the actual simulation Counter
     *
     * @return actual simulation counter
     */
    @Override
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
    @Override
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
        return this.myListOfSimulationParsers;
    }

    /**
     * Returns the data-source for simulated simulation-runs
     *
     * @return the mySimulationCache
     */
    @Override
    public SimulationCache getMySimulationCache() {
        return mySimulationCache;
    }

    /**
     * Sets the data-source for simulated simulation-runs
     *
     * @param mySimulationCache the mySimulationCache to set
     */
    @Override
    public void setMySimulationCache(SimulationCache mySimulationCache) {
        this.mySimulationCache = mySimulationCache;
    }
    
    @Override
    public int cancelAllSimulations() {
        this.myLocalSimulator.cancelAllSimulations();
        return 0;
    }

}
