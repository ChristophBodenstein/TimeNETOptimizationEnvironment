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
private String name="";
long CPUTimeFromCache=0;
long CPUTimeFromLocal=0;
long CPUTimeFromWeb=0;
long numberOfSimulationsTotal=0;
long numberOfSimulationsFromCache=0;
long numberOfSimulationsFromWeb=0;
long numberOfSImulationsFromLocal=0;


    public Statistic(String name){
    this.name=name;
    }
    
    public void addSimulation(parser p){
    numberOfSimulationsTotal++;
        if(p.isIsFromCache()){
        numberOfSimulationsFromCache++;
        CPUTimeFromCache+=p.getCPUTime();
        }else{
            
            if(p.isIsFromDistributedSimulation()){
            numberOfSimulationsFromWeb++;
            CPUTimeFromWeb+=p.getCPUTime();
            }else{
                numberOfSImulationsFromLocal++;
                CPUTimeFromLocal+=p.getCPUTime();
            }
        }
        
        
    
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
