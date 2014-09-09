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
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.typedef.*;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read log-data
 * @author Christoph Bodenstein
 */
public class SimulatorBenchmark implements Simulator{
private SimulationCache mySimulationCache=null;
private ArrayList<SimulationType> myListOfSimulations=null;
private int simulationCounter=0;
private String logFileName;
private typeOfBenchmarkFunction benchmarkFunction=typeOfBenchmarkFunction.Sphere;
    

    /**
     * Constructor
     */
     public SimulatorBenchmark(){
     logFileName=support.getTmpPath()+File.separator+"SimLog_Benchmark"+Calendar.getInstance().getTimeInMillis()+".csv";
     support.log("LogfileName:"+logFileName);
     }

    /**
     * inits and starts the simulation, this is neccessary and must be implemented
     * In Benchmark we don`t use a local cache
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param simulationCounterTMP actual Number of simulation, will be increased with every simulation-run
     */
    public void initSimulator(ArrayList<ArrayList <parameter> > listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {

        myListOfSimulations=new ArrayList<SimulationType>();
        
        for(int i=0;i<listOfParameterSetsTMP.size();i++){
        SimulationType tmpSimulation=new SimulationType();
        ArrayList<parameter>  tmpParameterList=listOfParameterSetsTMP.get(i);
        

        }






        
        if((this.myListOfSimulations==null)||(this.myListOfSimulations.size()!=listOfParameterSetsTMP.size())){
        support.log("Not all Simulations found in local Cache.  Will take next possible parametersets from cache.");
        myListOfSimulations=this.mySimulationCache.getNearestParserListFromListOfParameterSets(listOfParameterSetsTMP);
        }
        
        if(this.myListOfSimulations!=null){
            //copy parameterList of measure to parameter[] of SimulationType for later use
            if (myListOfSimulations.size() > 0)
            {
                //take first measure in SimulationType for list of parameters
                if (myListOfSimulations.get(0).getMeasures().size() > 0)
                {
                    for (int i=0; i<myListOfSimulations.size(); ++i)
                    {
                        //MeasureType firstMeasure = myListOfSimulations.get(i).getMeasures().get(0);
                        myListOfSimulations.get(i).setListOfParameters(listOfParameterSetsTMP.get(i));
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
            support.addLinesToLogFileFromListOfParser(myListOfSimulations, logFileName);
            }
        }
        
    }

    /**
     * Returns the actual status of all simulations 
     * @return % of simulatiions that are finished
     */
    public int getStatus() {
        if (this.myListOfSimulations!=null){
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
        return this.myListOfSimulations;
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
