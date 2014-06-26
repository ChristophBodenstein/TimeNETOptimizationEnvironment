/**
 * Simulator which uses log data from already done simulations and returns them.
 * 
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.support;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read log-data
 * @author Christoph Bodenstein
 */
public class SimulatorCachedLocal implements Simulator{
private SimulationCache mySimulationCache=null;
private ArrayList<SimulationType> myListOfSimulationParsers=null;
private int simulationCounter=0;
private String logFileName;
    

    /**
     * Constructor
     */
     public SimulatorCachedLocal(){
     logFileName=support.getTmpPath()+File.separator+"SimLog_LocalSimulation_with_Cache"+Calendar.getInstance().getTimeInMillis()+".csv";
     support.log("LogfileName:"+logFileName);
     }

    /**
     * inits the simulation, this is neccessary and must be implemented
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param simulationCounterTMP actual Number of simulation, will be increased with every simulation-run
     */
    public void initSimulator(ArrayList< ArrayList<parameter> > listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
    Simulator myLocalSimulator=null;
    this.myListOfSimulationParsers=null;
    this.simulationCounter=simulationCounterTMP;
    
        if(mySimulationCache!=null){
        this.myListOfSimulationParsers=mySimulationCache.getListOfCompletedSimulationParsers(listOfParameterSetsTMP, simulationCounter);
        //this.simulationCounter=mySimulationCache.getLocalSimulationCounter();
        }else{
        support.log("No local Simulation file loaded. Will build my own cache from scratch.");
        this.mySimulationCache=new SimulationCache();
        }
        
        if(this.myListOfSimulationParsers==null){
        support.log("Simulations not found in local Cache. Starting local simulation.");
        if(myLocalSimulator==null){myLocalSimulator=new SimulatorLocal();}
        myLocalSimulator.initSimulator(listOfParameterSetsTMP, this.simulationCounter, false);
            support.waitForEndOfSimulator(myLocalSimulator, simulationCounter, support.DEFAULT_TIMEOUT);
            
        myListOfSimulationParsers=myLocalSimulator.getListOfCompletedSimulationParsers();
        this.mySimulationCache.addListOfSimulationsToCache(myListOfSimulationParsers);
        }
        if(this.myListOfSimulationParsers!=null){
        //Print out a log file    
        support.addLinesToLogFileFromListOfParser(myListOfSimulationParsers, logFileName);
        //Count up simulation-counter
        this.simulationCounter+=myListOfSimulationParsers.size();
        support.log("SimulationCounter is now: "+this.simulationCounter);
        }
        
    }

    /**
     * Returns the actual status of all simulations 
     * @return % of simulatiions that are finished
     */
    public int getStatus() {
        if (this.myListOfSimulationParsers!=null){
        return 100;
        }else {return 0;}
    }

    
    /**
     * Returns the actual simulation Counter
     * @return actual simulation counter
     */
    public int getSimulationCounter() {
        return this.simulationCounter;
    }

    
    
    /**
     * Gets the list of completed simulations, should be used only if getStatus() returns 100
     * @return list of completed simulations (parsers) which contain all data from the log-files
     */
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
        return this.myListOfSimulationParsers;
    }

    
    /**
     * Returns the data-source for simulated simulation-runs
     * @return the mySimulationCache
     */
    public SimulationCache getMySimulationCache() {
        return mySimulationCache;
    }

    /**
     * Sets the data-source for simulated simulation-runs
     * @param mySimulationCache the mySimulationCache to set
     */
    public void setMySimulationCache(SimulationCache mySimulationCache) {
        this.mySimulationCache = mySimulationCache;
    }
    
}
