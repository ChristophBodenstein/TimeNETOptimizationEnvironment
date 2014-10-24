/*
 * Christoph Bodenstein
 * Aggregator Class for statistics
 * It holds instances for every Simulation/Optimization
 * With every log-file-entry the information is added to the statistic-instance with the corresponding name

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.helper;

import java.util.ArrayList;
import timenetexperimentgenerator.Parser;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.plot.RPlugin;

/**
 *
 * @author Christoph Bodenstein
 */
public class StatisticAggregator {
private static ArrayList<Statistic> listOfStatistics=new ArrayList<Statistic>();  

    /**
     * Constructor
     */
    public StatisticAggregator() {
    listOfStatistics=new ArrayList<Statistic>();
    }
    
    /**
     * Adds one simulation to statistic which is identified by its logfilename
     * @param filename name of the logfile of statistic
     * @param p simulation to be added to statistic
     */
    public static void addToStatistics(SimulationType p, String filename){
    Statistic myStatistic=getStatisticByName(filename);
        myStatistic.addSimulation(p);
        //support.getMainFrame().updateSimulationCounterLabel(myStatistic.getNumberOfSimulationsTotal());
        //update cached list in PlotFrameController
        RPlugin.updateCachedListOfStatistics();
    }
    
    /**
     * returns the statistic with given logfilename
     * @param filename logfilename of statistic to be returned
     * @return requested Statistic
     */
    public static Statistic getStatisticByName(String filename){
    Statistic returnValue=null;
    
    
        for(int i=0;i<listOfStatistics.size();i++){
            if(listOfStatistics.get(i).getName().equals(filename)){
            returnValue=listOfStatistics.get(i);
            }
        }
        if(returnValue==null){
        returnValue=new Statistic(filename);
        listOfStatistics.add(returnValue);
        }
    return returnValue;
    }
    
    /**
     * Prints all Statistics, available in this Aggregator (collected since program start)
     */
    public static void printAllStatistics(){
        
        if((listOfStatistics==null)||(listOfStatistics.size()<1)){
        support.log("No Statistics Available!");    
        return;
        }
        
        
        for(int i=0;i<listOfStatistics.size();i++){
        listOfStatistics.get(i).printStatisticToLog();
        }
    }
    
    /**
     * Prints Statistics with specified name (name of log-file)
     * @param name Name of log-File to identify the statistic
     */
    public static void printStatistic(String name){
        getStatisticByName(name).printStatisticToLog();
    }
    
    /**
     * Prints the last statistic
     */
    public static void printLastStatistic(){
        if((listOfStatistics==null)||(listOfStatistics.size()<1)){
        support.log("No Statistics Available!");    
        }else{
        listOfStatistics.get(listOfStatistics.size()-1).printStatisticToLog();
        }
    }
    
    /**
     *
     * @return listOfStatistics
     */
    public static ArrayList<Statistic> getListOfStatistics()
    {
        return listOfStatistics;
    }
    
}
