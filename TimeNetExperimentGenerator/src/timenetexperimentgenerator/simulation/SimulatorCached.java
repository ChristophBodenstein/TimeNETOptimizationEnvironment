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
import java.util.logging.Level;
import java.util.logging.Logger;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.parser;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.support;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read log-data
 * @author Christoph Bodenstein
 */
public class SimulatorCached implements Simulator{
private SimulationCache mySimulationCache=null;
private ArrayList<parser> myListOfSimulationParsers=null;
private int simulationCounter=0;
private String logFileName;
    

    /**
     * Constructor
     */
     public SimulatorCached(){
     logFileName=support.getTmpPath()+File.separator+"SimLog_LocalSimulation_Only_Cache"+Calendar.getInstance().getTimeInMillis()+".csv";
     support.log("LogfileName:"+logFileName);
     }

    /**
     * inits the simulation, this is neccessary and must be implemented
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param simulationCounterTMP actual Number of simulation, will be increased with every simulation-run
     */
    public void initSimulator(ArrayList<parameter[]> listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
        if(mySimulationCache!=null){
        this.myListOfSimulationParsers=mySimulationCache.getListOfCompletedSimulationParsers(listOfParameterSetsTMP, simulationCounter);
        this.simulationCounter=mySimulationCache.getLocalSimulationCounter();
        }else{
        support.log("No local Simulation file loaded. Simulation not possible.");
        }
        
        if((this.myListOfSimulationParsers==null)||(this.myListOfSimulationParsers.size()!=listOfParameterSetsTMP.size())){
        support.log("Not all Simulations found in local Cache.  Will take next possible parametersets from cache.");
        myListOfSimulationParsers=this.mySimulationCache.getNearestParserListFromListOfParamaeterSets(listOfParameterSetsTMP);
        }
        
        if(this.myListOfSimulationParsers!=null){
            //copy parameterList of measure to parameter[] of parser for later use
            if (myListOfSimulationParsers.size() > 0)
            {
                //take first measure in parser for list of parameters
                if (myListOfSimulationParsers.get(0).getMeasures().size() > 0)
                {
                    for (int i=0; i<myListOfSimulationParsers.size(); ++i)
                    {
                        MeasureType firstMeasure = myListOfSimulationParsers.get(i).getMeasures().get(0);
                        parameter[] pArray = support.convertArrayListToArray(firstMeasure.getParameterList());
                        myListOfSimulationParsers.get(i).setListOfParameters(pArray);
                    }
                }
                else
                {
                    support.log("No Measures found in parser.");
                } 
            }
            else
            {
                support.log("List of parsers is empty.");
            }

            if(log){
            //Print out a log file    
            support.addLinesToLogFileFromListOfParser(myListOfSimulationParsers, logFileName);
            }
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
