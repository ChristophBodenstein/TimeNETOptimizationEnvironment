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
    
    public static void addToStatistics(parser p){
    
    }
    
}
