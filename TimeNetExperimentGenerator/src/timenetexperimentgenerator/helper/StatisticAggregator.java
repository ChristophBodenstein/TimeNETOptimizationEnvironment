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

/**
 *
 * @author Christoph Bodenstein
 */
public class StatisticAggregator {
private static ArrayList<Statistic> listOfStatistics=new ArrayList<Statistic>();  

    public StatisticAggregator() {
    listOfStatistics=new ArrayList<Statistic>();
    }
    
    public static void addToStatistics(SimulationType p, String filename){
    Statistic myStatistic=getStatisticByName(filename);
        myStatistic.addSimulation(p);
    }
    
    
    static Statistic getStatisticByName(String filename){
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
