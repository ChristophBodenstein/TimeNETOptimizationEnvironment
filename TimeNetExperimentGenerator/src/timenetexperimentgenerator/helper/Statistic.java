/*
 * Christoph Bodenstein
 * Statistic Class 
 * One instance per Simulator is used, it is named by the logfile

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.helper;

import timenetexperimentgenerator.datamodel.parser;

/**
 *
 * @author Christoph Bodenstein
 */
public class Statistic {
String name="";
long CPUTimeTheoretical=0;
long CPUTimeReal=0;//Without cached CPU Time
long numberOfSimulationsTotal=0;
long numberOfSimulationsFromCache=0;
long numberOfSimulationsFromWeb=0;


    public Statistic(String name){
    this.name=name;
    }
    
    public void addSimulation(parser p){
    
    }
    
}
