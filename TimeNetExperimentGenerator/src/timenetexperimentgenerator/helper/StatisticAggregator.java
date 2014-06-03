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
import timenetexperimentgenerator.datamodel.parser;

/**
 *
 * @author Christoph Bodenstein
 */
public class StatisticAggregator {
private static ArrayList<Statistic> listOfStatistics;  

    public StatisticAggregator() {
    listOfStatistics=new ArrayList<Statistic>();
    }
    
    public static void addToStatistics(parser p, String filename){
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
    
}
