/**
 * Simulator which uses log data from already done simulations and returns them.
 * 
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read log-data
 * @author Christoph Bodenstein
 */
public class SimulatorCached implements Simulator{
private SimulationCache mySimulationCache=null;
private ArrayList<parser> myListOfSimulationParsers=null;
private int simulationCounter=0;
    

    /**
     * inits the simulation, this is neccessary and must be implemented
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param simulationCounterTMP actual Number of simulation, will be increased with every simulation-run
     */
    public void initSimulator(ArrayList<parameter[]> listOfParameterSetsTMP, int simulationCounterTMP) {
        if(mySimulationCache!=null){
        this.myListOfSimulationParsers=mySimulationCache.getListOfCompletedSimulationParsers(listOfParameterSetsTMP, simulationCounter);
        this.simulationCounter=mySimulationCache.getLocalSimulationCounter();
        }else{
        support.log("No local Simulation file loaded. Simulation not possible.");
        }
        
        if(this.myListOfSimulationParsers==null){
        support.log("Simulations not found in local Cache. Starting local simulation.");
        Simulator tmpSim=new SimulatorLocal();
        tmpSim.initSimulator(listOfParameterSetsTMP, simulationCounterTMP);
            while(tmpSim.getStatus()<100){
                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    support.log("Exception while waiting for local simulation.");
                }
            
            }
        myListOfSimulationParsers=tmpSim.getListOfCompletedSimulationParsers();
        
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
    public ArrayList<parser> getListOfCompletedSimulationParsers() {
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
