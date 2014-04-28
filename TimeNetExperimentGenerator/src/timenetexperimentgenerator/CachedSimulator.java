/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator;

import java.util.ArrayList;

/**
 *
 * @author sse
 */
public class CachedSimulator implements Simulator{
private SimulationCache mySimulationCache=null;
private ArrayList<parser> myListOfSimulationParsers=null;
private int simulationCounter=0;
    
    public void initSimulator(ArrayList<parameter[]> listOfParameterSetsTMP, int simulationCounterTMP) {
        if(mySimulationCache!=null){
        this.myListOfSimulationParsers=mySimulationCache.getListOfCompletedSimulationParsers(listOfParameterSetsTMP, simulationCounter);
        this.simulationCounter=mySimulationCache.getLocalSimulationCounter();
        }else{
        support.log("No local Simulation file loaded. Simulation not possible.");
        }
        
    }

    public int getStatus() {
        if (this.myListOfSimulationParsers!=null){
        return 100;
        }else {return 0;}
    }

    public int getSimulationCounter() {
        return this.simulationCounter;
    }

    public ArrayList<parser> getListOfCompletedSimulationParsers() {
        return this.myListOfSimulationParsers;
    }

    
    /**
     * @return the mySimulationCache
     */
    public SimulationCache getMySimulationCache() {
        return mySimulationCache;
    }

    /**
     * @param mySimulationCache the mySimulationCache to set
     */
    public void setMySimulationCache(SimulationCache mySimulationCache) {
        this.mySimulationCache = mySimulationCache;
    }
    
}
