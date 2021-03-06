/**
 * Simulator which uses log data from already done simulations and returns them.
 * If not all parametersets can be found in cache, the rest is simulated
 * with local TimeNET
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

    private ArrayList<SimulationType> myListOfCompletedSimulations = null;
    private int status;
    private final Simulator myLocalSimulator = getNoCacheSimulator();
    private ArrayList< ArrayList<parameter>> remainingParametersets = new ArrayList<>();
    ArrayList< ArrayList<parameter>> listOfParameterSetsTMP;

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

        this.myListOfCompletedSimulations = null;
        remainingParametersets = new ArrayList<>();
        status = 0;
        this.listOfParameterSetsTMP = listOfParameterSetsTMP;
        //Start this thread
        new Thread(this).start();

    }

    /**
     * Run Method to start simulations and collect the data simulats the SCPNs,
     * main routine
     */
    @Override
    public void run() {
        if ((support.getMySimulationCache() != null) && (support.getMySimulationCache().getCacheSize() >= 1)) {
            support.log("Will load available results from simulation cache.", typeOfLogLevel.INFO);
            this.myListOfCompletedSimulations = support.getMySimulationCache().getListOfCompletedSimulations(listOfParameterSetsTMP, support.getGlobalSimulationCounter(), remainingParametersets);
        } else {
            support.log("No local Simulation file loaded. Will build my own cache from scratch.", typeOfLogLevel.INFO);
            support.emptyCache();
        }
        
        if (this.myListOfCompletedSimulations == null) {
            support.log("No Simulation found in local Cache. Starting simulation.", typeOfLogLevel.INFO);
            this.myListOfCompletedSimulations = new ArrayList<>();
            remainingParametersets = listOfParameterSetsTMP;
        }

        status = myListOfCompletedSimulations.size() * 100 / listOfParameterSetsTMP.size();
        support.setStatusText("Simulating " + myListOfCompletedSimulations.size() + "/" + listOfParameterSetsTMP.size());
        //Increase Simulationcounter only if Simulation results are found in cache
        //Local or benchmark-simulators will update the counter on their own
        support.setGlobalSimulationCounter(support.getGlobalSimulationCounter() + myListOfCompletedSimulations.size());

        if (this.myListOfCompletedSimulations.size() < listOfParameterSetsTMP.size()) {
            support.log("Some simulations were missing in cache. Will simulate them local/distributed.", typeOfLogLevel.INFO);
            //Find simulations that are not already simulated
            support.log("Will simulate " + remainingParametersets.size() + " local/distributed.", typeOfLogLevel.INFO);
            if (support.isCancelEverything()) {
                return;
            }

            synchronized (myLocalSimulator) {
                try {
                    support.addLinesToLogFileFromListOfSimulations(myListOfCompletedSimulations, myLocalSimulator.getLogfileName());
                    myLocalSimulator.initSimulator(remainingParametersets, support.isCreateseparateLogFilesForEverySimulation());
                    myLocalSimulator.wait();
                } catch (InterruptedException ex) {
                    support.log("Problem waiting for end of non-cache-simulator.", typeOfLogLevel.ERROR);
                }
            }

            myListOfCompletedSimulations.addAll(myLocalSimulator.getListOfCompletedSimulations());
            status = myListOfCompletedSimulations.size() * 100 / listOfParameterSetsTMP.size();

            support.log("Size of resultList is " + myListOfCompletedSimulations.size(), typeOfLogLevel.INFO);

            support.getMySimulationCache().addListOfSimulationsToCache(myLocalSimulator.getListOfCompletedSimulations());
            support.log("Size of SimulationCache: " + support.getMySimulationCache().getCacheSize(), typeOfLogLevel.INFO);
        }

        if (this.myListOfCompletedSimulations != null) {
            support.log("Adding " + myListOfCompletedSimulations.size() + " Results to logfile.", typeOfLogLevel.INFO);
            //Print out a log file
            support.addLinesToLogFileFromListOfSimulations(myListOfCompletedSimulations, logFileName);
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
    public ArrayList<SimulationType> getListOfCompletedSimulations() {
        return this.myListOfCompletedSimulations;
    }


    @Override
    public int cancelAllSimulations() {
        this.myLocalSimulator.cancelAllSimulations();
        return 0;
    }

    @Override
    public String getLogfileName() {
        return this.logFileName;
    }
}
