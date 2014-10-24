/*
 * Christoph Bodenstein
 * Statistic Class 
 * One instance per Simulator is used, it is named by the logfile

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.helper;

import java.util.ArrayList;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein
 */
public class Statistic {
private String name="";
long CPUTimeFromCache=0;
long CPUTimeFromLocal=0;
long CPUTimeFromWeb=0;
private long numberOfSimulationsTotal=0;
long numberOfSimulationsFromCache=0;
long numberOfSimulationsFromWeb=0;
long numberOfSimulationsFromLocal=0;
double simulationTimeTotal=0;
double simulationTimeFromCache=0;
double simulationTimeFromWeb=0;
double simulationTimeFromLocal=0;
SimulationType foundOptimum,calulatedOptimum=null;


    public Statistic(String name){
    this.name=name;
    }
    
    
    /**
     * adds the relevant data from given SimulationType to statistics (count of Simulations etc.)
     * @param p SimulationType with data about one simulation
     */
    public void addSimulation(SimulationType p){
    MeasureType statisticMeasure=p.getMeasures().get(0);//Take the first Measure as dummy incl. all nec. information    
        setNumberOfSimulationsTotal(getNumberOfSimulationsTotal() + 1);
    simulationTimeTotal+=statisticMeasure.getSimulationTime();
        if(p.isIsFromCache()){
        numberOfSimulationsFromCache++;
        CPUTimeFromCache+=statisticMeasure.getCPUTime();
        simulationTimeFromCache+=statisticMeasure.getSimulationTime();
        }else{
            
            if(p.isIsFromDistributedSimulation()){
            numberOfSimulationsFromWeb++;
            CPUTimeFromWeb+=statisticMeasure.getCPUTime();
            simulationTimeFromWeb+=statisticMeasure.getSimulationTime();
            }else{
                numberOfSimulationsFromLocal++;
                CPUTimeFromLocal+=statisticMeasure.getCPUTime();
                simulationTimeFromLocal+=statisticMeasure.getSimulationTime();
            }
        }
    
    }

    /**
     * Adds the found optimum and the calculated optimum to statistics.
     * Useful for statistics of optimization runs
     * @param foundOptimum The simulation incl. parameterset which was found by optimization algorithm
     * @param optimumParameterset The calulated optimium parameterset to calulate the distance
     */
    public void addFoundOptimum(SimulationType foundOptimum, SimulationType calulatedOptimum){
    this.foundOptimum=foundOptimum;
    this.calulatedOptimum=calulatedOptimum;
    }
    
    /**
     * Prints out Statistics about optimization to Log
     */
    public void printOptimizerStatisticsToLog(){
        if(this.foundOptimum!=null && this.calulatedOptimum!=null){
        support.log("****Optimization-Statistics****");
        }
    }
    
    /**
     * Prints all relevant statistic data to support.log
     */
    public void printStatisticToLog(){
        support.log("-----Statistics of Simulation: "+this.getName()+" ----- Start -----");
        support.log("Total Number of Simulations: "+ this.getNumberOfSimulationsTotal());
        support.log("Number of Cached Simulations: "+ this.numberOfSimulationsFromCache);
        support.log("Number of Web-Based Simulations: "+ this.numberOfSimulationsFromWeb);
        support.log("Number of local Simulations: "+ this.numberOfSimulationsFromLocal);
        support.log("Ratio of Cached Simulations (Cache/Total): "+ ((double)this.numberOfSimulationsFromCache/(double)this.getNumberOfSimulationsTotal()));
        support.log("Theoretical used CPU-Time: " +(this.CPUTimeFromCache+this.CPUTimeFromLocal+this.CPUTimeFromWeb));
        support.log("Local used CPU-Time: " +this.CPUTimeFromLocal);
        support.log("Web-Based CPU-Time: " +this.CPUTimeFromWeb);
        support.log("Cache CPU-Time: " +this.CPUTimeFromCache);
        support.log("Total needed SimulationTime: " +this.simulationTimeTotal);
        support.log("SimulationTime from Web: " + this.simulationTimeFromWeb);
        support.log("SimulationTime from Cache: " + this.simulationTimeFromCache);
        support.log("SimulationTime from Local: " + this.simulationTimeFromLocal);
        support.log("-----Statistics of Simulation: "+this.getName()+" ----- End -----");
        printOptimizerStatisticsToLog();
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

    /**
     * @return the numberOfSimulationsTotal
     */
    public long getNumberOfSimulationsTotal() {
        return numberOfSimulationsTotal;
    }

    /**
     * @param numberOfSimulationsTotal the numberOfSimulationsTotal to set
     */
    public void setNumberOfSimulationsTotal(long numberOfSimulationsTotal) {
        this.numberOfSimulationsTotal = numberOfSimulationsTotal;
    }
    
}
