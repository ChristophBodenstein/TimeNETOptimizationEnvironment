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
import java.util.Iterator;
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
        //Print all Optimization Statistics if available
        printOptiStatistics();
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
    
    /**
     * Prints all Optimization statistics and a summary of the last optimizations
     */
    public static void printOptiStatistics(){
    int numberOfAllOptimizationRuns=0;
    double averageDistanceToOptimumAbsolute=0;
    double averageDistanceToOptimumRelative=0;
    double averageDistanceToOptimumInDesignSpace=0;
    int numberOfFoundOptimaWithinRangeClass[]=new int[support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES];
    int numberOfFoundOptimaWithinRangeClassDS[]=new int[support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES];
    
    double targetDistanceToCalculatedOptimum=1;//in %
    double targetDistanceToCalculatedOptimumDS=1;//in %
    double probabilityThatFoundOptimumIsInDistanceOfXAroundCalculatedOptimum=0;//calc this for different X
    double probabilityThatFoundOptimumIsInDistanceOfXAroundCalculatedOptimumDS=0;//calc this for different X
    
    //init RangeClasses
        for(int i=0;i<support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES;i++){
        numberOfFoundOptimaWithinRangeClass[i]=0;
        numberOfFoundOptimaWithinRangeClassDS[i]=0;
        }
    
    ArrayList<Statistic> optiSimulationList=new ArrayList<Statistic>();
    //Fill new ArrayList with Statistic that are Optimiation
    for (Statistic s : listOfStatistics) {
        if(s.isOptimization()){
            optiSimulationList.add(s);
            averageDistanceToOptimumAbsolute+=s.getDistanceToTargetValue();
            averageDistanceToOptimumRelative+=s.getDistanceRelative();
            averageDistanceToOptimumInDesignSpace+=s.getDistanceToTargetDS();
            //Calculate RangeClasses
            for(int i=1;i<=support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES;i++){
                if(s.getDistanceRelative()<=i*100/support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES){
                numberOfFoundOptimaWithinRangeClass[i-1]++;
                }
                if(s.getDistanceToTargetDS()<=i*100/support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES){
                numberOfFoundOptimaWithinRangeClassDS[i-1]++;
                }
            }
            
        }
    }
    numberOfAllOptimizationRuns=optiSimulationList.size();
    averageDistanceToOptimumAbsolute=averageDistanceToOptimumAbsolute/numberOfAllOptimizationRuns;
    averageDistanceToOptimumRelative=averageDistanceToOptimumRelative/numberOfAllOptimizationRuns;
    averageDistanceToOptimumInDesignSpace=averageDistanceToOptimumInDesignSpace/numberOfAllOptimizationRuns;
    
    
        support.log("++++ Start of Optimization Statistics ++++");
        support.log("Number of Optimization runs: "+numberOfAllOptimizationRuns);
        support.log("Average distance to calculated optimum (absolute): "+averageDistanceToOptimumAbsolute);
        support.log("Average distance to calculated optimum (relative): "+averageDistanceToOptimumRelative +" %");
        support.log("Average distance to calculated optimum in Design Space " +averageDistanceToOptimumInDesignSpace+" %");
        String tmpOutHead=      "Radius:    ";
        String tmpOutValue=     "Values:    ";
        String tmpOutDSValue=   "DS-Values: ";
            for(int i=1;i<=support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES;i++){
            tmpOutHead+="| "+support.padLeft( String.valueOf(i*100/support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES),8) ;
            
            //support.log("# of Optima within Radius of " +(i*100/support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES)+" around Calculated Optimum: "+ numberOfFoundOptimaWithinRangeClass[i-1] +" --  "+(numberOfFoundOptimaWithinRangeClass[i-1]*100/numberOfAllOptimizationRuns)+"%");    
            tmpOutValue+="| "+support.padLeft(numberOfFoundOptimaWithinRangeClass[i-1] +"--"+(numberOfFoundOptimaWithinRangeClass[i-1]*100/numberOfAllOptimizationRuns)+"%",8); 
            //support.log("# of Optima within DS-Radius of " +(i*100/support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES)+" around Calculated Optimum: "+ numberOfFoundOptimaWithinRangeClassDS[i-1] +" --  "+(numberOfFoundOptimaWithinRangeClassDS[i-1]*100/numberOfAllOptimizationRuns)+"%");        
            tmpOutDSValue+="| "+support.padLeft(numberOfFoundOptimaWithinRangeClassDS[i-1] +"--"+(numberOfFoundOptimaWithinRangeClassDS[i-1]*100/numberOfAllOptimizationRuns)+"%",8); 
            }
        support.log(tmpOutHead);
        support.log(tmpOutValue);
        support.log(tmpOutDSValue);
        support.log("++++ End of Optimization Statistics ++++");
    }
    
    /**
     * Removes all optimization statistics from List of statistics
     */
    public static void removeOldOptimizationsFromList(){
    Iterator<Statistic> it=listOfStatistics.iterator();
    
        while(it.hasNext()){
        Statistic s=it.next();
            if(s.isOptimization()){
            it.remove();
            }
        }
    
    }
}
