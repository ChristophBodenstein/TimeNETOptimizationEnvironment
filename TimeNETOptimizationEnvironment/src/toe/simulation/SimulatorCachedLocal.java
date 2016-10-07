/**
 * Simulator which uses log data from already done simulations and returns them.
 * If not all parametersets can be found in cache, the rest is simulated
 * distributed
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.util.ArrayList;
import toe.datamodel.parameter;
import toe.datamodel.SimulationType;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read
 * log-data
 *
 * @author Christoph Bodenstein
 */
public class SimulatorCachedLocal extends SimulatorCached {

    private ArrayList<SimulationType> myListOfSimulationParsers = null;
    private int status;
    private final Simulator myLocalSimulator = getNoCacheSimulator();
    private ArrayList< ArrayList<parameter>> remainingParametersets = new ArrayList<>();
    ArrayList< ArrayList<parameter>> listOfParameterSetsTMP;
    private boolean log = true;

    /**
     * Constructor
     */
    public SimulatorCachedLocal() {
        super();
    }

    /**
     * inits the simulation, this is neccessary and must be implemented
     *
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * increased with every simulation-run
     * @param log true if own logfile should be written, else false
     */
    @Override
    public void initSimulator(ArrayList< ArrayList<parameter>> listOfParameterSetsTMP, boolean log) {

        this.myListOfSimulationParsers = null;
        remainingParametersets = new ArrayList<>();
        status = 0;
        this.listOfParameterSetsTMP = listOfParameterSetsTMP;
        this.log = log;
        //Start this thread
        new Thread(this).start();

    }

    /**
     * Run Method to start simulations and collect the data simulats the SCPNs,
     * main routine
     */
    @Override
    public void run() {
        if (mySimulationCache != null) {
            support.log("Will load available results from simulation cache.", typeOfLogLevel.INFO);
            this.myListOfSimulationParsers = mySimulationCache.getListOfCompletedSimulationParsers(listOfParameterSetsTMP, support.getGlobalSimulationCounter());
        } else {
            support.log("No local Simulation file loaded. Will build my own cache from scratch.", typeOfLogLevel.INFO);
            this.mySimulationCache = support.getMySimulationCache();
        }

        if (this.myListOfSimulationParsers == null) {
            support.log("No Simulation found in local Cache. Starting simulation.", typeOfLogLevel.INFO);
            this.myListOfSimulationParsers = new ArrayList<>();
        }

        status = myListOfSimulationParsers.size() * 100 / listOfParameterSetsTMP.size();
        support.setStatusText("Simulating " + myListOfSimulationParsers.size() + "/" + listOfParameterSetsTMP.size());
        //Increase Simulationcounter only if Simulation results are found in cache
        //Local or benchmark-simulators will update the counter on their own
        support.setGlobalSimulationCounter(support.getGlobalSimulationCounter() + myListOfSimulationParsers.size());

        if (this.myListOfSimulationParsers.size() < listOfParameterSetsTMP.size()) {
            support.log("Some simulations were missing in cache. Will simulate them local/distributed.", typeOfLogLevel.INFO);

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
            support.log("Size of Remaining ParameterList is " + remainingParametersets.size(), typeOfLogLevel.INFO);
            //Find simulations that are not already simulated
            support.log("Will simulate " + remainingParametersets.size() + " local/distributed.", typeOfLogLevel.INFO);

            myLocalSimulator.initSimulator(remainingParametersets, false);
            //support.waitForEndOfSimulator(myLocalSimulator, support.getGlobalSimulationCounter(), support.DEFAULT_TIMEOUT);

            synchronized (myLocalSimulator) {
                try {
                    myLocalSimulator.wait();
                } catch (InterruptedException ex) {
                    support.log("Problem waiting for end of non-cache-simulator.", typeOfLogLevel.ERROR);
                }
            }

            myListOfSimulationParsers.addAll(myLocalSimulator.getListOfCompletedSimulationParsers());
            status = myListOfSimulationParsers.size() * 100 / listOfParameterSetsTMP.size();

            support.log("Size of resultList is " + myListOfSimulationParsers.size(), typeOfLogLevel.INFO);

            this.mySimulationCache.addListOfSimulationsToCache(myLocalSimulator.getListOfCompletedSimulationParsers());
            support.log("Size of SimulationCache: " + this.mySimulationCache.getCacheSize(), typeOfLogLevel.INFO);
        }

        if (this.myListOfSimulationParsers != null) {
            support.log("Adding " + myListOfSimulationParsers.size() + " Results to logfile.", typeOfLogLevel.INFO);
            //Print out a log file
            support.addLinesToLogFileFromListOfParser(myListOfSimulationParsers, logFileName);
            support.log("SimulationCounter is now: " + support.getGlobalSimulationCounter(), typeOfLogLevel.INFO);
        }

        this.status = 100;
        synchronized (this) {
            notify();
        }
    }

    /**
     * Returns the current status of all simulations
     *
     * @return % of simulations that are finished
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
