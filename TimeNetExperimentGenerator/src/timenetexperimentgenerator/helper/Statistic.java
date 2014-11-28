/*
 * Christoph Bodenstein
 * Statistic Class 
 * One instance per Simulator is used, it is named by the logfile

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package timenetexperimentgenerator.helper;

import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein
 */
public class Statistic {

    private String name = "";
    private long CPUTimeFromCache = 0;
    private long CPUTimeFromLocal = 0;
    private long CPUTimeFromWeb = 0;
    private long numberOfSimulationsTotal = 0;
    private long numberOfSimulationsFromCache = 0;
    private long numberOfSimulationsFromWeb = 0;
    private long numberOfSimulationsFromLocal = 0;
    private double simulationTimeTotal = 0;
    private double simulationTimeFromCache = 0;
    private double simulationTimeFromWeb = 0;
    private double simulationTimeFromLocal = 0;
    SimulationType foundOptimum;
    private SimulationType calculatedOptimum = null;
    private boolean optimization = false;

    /**
     * Constructor with given name of this statistic
     *
     * @param name Name of this statistic. usally use the logfile-name here
     */
    public Statistic(String name) {
        this.name = name;
    }

    /**
     * adds the relevant data from given SimulationType to statistics (count of
     * Simulations etc.)
     *
     * @param p SimulationType with data about one simulation
     */
    public void addSimulation(SimulationType p) {
        MeasureType statisticMeasure = p.getMeasures().get(0);//Take the first Measure as dummy incl. all nec. information    
        setNumberOfSimulationsTotal(getNumberOfSimulationsTotal() + 1);
        simulationTimeTotal += statisticMeasure.getSimulationTime();
        if (p.isIsFromCache()) {
            numberOfSimulationsFromCache++;
            CPUTimeFromCache += statisticMeasure.getCPUTime();
            simulationTimeFromCache += statisticMeasure.getSimulationTime();
        } else {

            if (p.isIsFromDistributedSimulation()) {
                numberOfSimulationsFromWeb++;
                CPUTimeFromWeb += statisticMeasure.getCPUTime();
                simulationTimeFromWeb += statisticMeasure.getSimulationTime();
            } else {
                numberOfSimulationsFromLocal++;
                CPUTimeFromLocal += statisticMeasure.getCPUTime();
                simulationTimeFromLocal += statisticMeasure.getSimulationTime();
            }
        }

    }

    /**
     * Adds the found optimum and the calculated optimum to statistics. Useful
     * for statistics of optimization runs
     *
     * @param foundOptimum The simulation incl. parameterset which was found by
     * optimization algorithm
     * @param calculatedOptimum The calculated optimum parameterset to calulate
     * the distance
     */
    public void addFoundOptimum(SimulationType foundOptimum, SimulationType calculatedOptimum) {
        support.log("Adding found and calculated optimum (if exists) to List of Statistics.");
        this.foundOptimum = foundOptimum;
        this.calculatedOptimum = calculatedOptimum;
        if (this.foundOptimum != null) {
            this.setOptimization(true);
        }
    }

    /**
     * Prints out Statistics about optimization to Log
     */
    public void printOptimizerStatisticsToLog() {
        if (isOptimization() && this.calculatedOptimum != null) {
            support.log("****Start*Optimization-Statistics****");
            //support.log("Distance to Optimum: "+this.getDistanceToTargetValue());
            support.log("Distance to Optimum in Definition range: " + this.getRelativeDistanceToOptimumInDefinitionRange() + " %");
            support.log("Distance to Optimum in Value range: " + this.getRelativeDistanceToOptimumInValueRange() + " %");
            support.log("****End*Optimization-Statistics****");
        }
    }

    /**
     * Returns Distance of found optimum to target value (mostly the theoretical
     * optimum, given by user)
     *
     * @return distance to target value (is calculated by SimulationType
     * foundOptimum)
     */
    public double getDistanceToTargetValue() {
        return this.foundOptimum.getDistanceToTargetValue();
    }

    /**
     * Returns Distance of actual parameterValues to given set of
     * parameterValues (calculated Optimum) in definition space (domain)
     *
     * @return distance to target value in Design space
     * @see SimulationType
     */
    public double getRelativeDistanceToOptimumInDefinitionRange() {
        return this.foundOptimum.getRelativeDistanceInDefinitionRange(this.getCalculatedOptimum());
    }

    /**
     * Returns distance of found optimum to given optimum in relation to
     * Maximum/Minimum of values of measures This is only possible for
     * benchmark-functions or if absolute minimum/maximum is given by user Calls
     * the getRelativeDistanceToOptimumInValueRange of SimulationType
     *
     * @return distance to theoretical optimum in % of possible range If an
     * optimum was found and given to this statistic the distance to the
     * calculated optimum is calculated. Not the distance to target value as
     * given in user interface! For calculation
     * getRelativeDistanceToTargetValueInValueRange is called from
     * this.foundOptimum
     */
    public double getRelativeDistanceToOptimumInValueRange() {
        MeasureType myOptiTargetMeasure = support.getOptimizationMeasure();//Take only the first Measure.
        MeasureType myOptiMeasure = this.getCalculatedOptimum().getMeasureByName(myOptiTargetMeasure.getMeasureName());
        //support.log("Calculating Distance to "+myOptiMeasure.getMeanValue() + " for "+myOptiMeasure.getMeasureName());
        return this.foundOptimum.getRelativeDistanceToTargetValueInValueRange(myOptiMeasure);
    }

    /**
     * Prints all relevant statistic data to support.log
     */
    public void printStatisticToLog() {
        support.log("-----Statistics of Simulation: " + this.getName() + " ----- Start -----");
        support.log("Total Number of Simulations: " + this.getNumberOfSimulationsTotal());
        support.log("Number of Cached Simulations: " + this.getNumberOfSimulationsFromCache());
        support.log("Number of Web-Based Simulations: " + this.getNumberOfSimulationsFromWeb());
        support.log("Number of local Simulations: " + this.getNumberOfSimulationsFromLocal());
        support.log("Ratio of Cached Simulations (Cache/Total): " + ((double) this.getNumberOfSimulationsFromCache() / (double) this.getNumberOfSimulationsTotal()));
        support.log("Theoretical used CPU-Time: " + (this.getCPUTimeFromCache() + this.getCPUTimeFromLocal() + this.getCPUTimeFromWeb()));
        support.log("Local used CPU-Time: " + this.getCPUTimeFromLocal());
        support.log("Web-Based CPU-Time: " + this.getCPUTimeFromWeb());
        support.log("Cache CPU-Time: " + this.getCPUTimeFromCache());
        support.log("Total needed SimulationTime: " + this.getSimulationTimeTotal());
        support.log("SimulationTime from Web: " + this.getSimulationTimeFromWeb());
        support.log("SimulationTime from Cache: " + this.getSimulationTimeFromCache());
        support.log("SimulationTime from Local: " + this.getSimulationTimeFromLocal());
        support.log("-----Statistics of Simulation: " + this.getName() + " ----- End -----");
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

    /**
     * @return the optimization
     */
    public boolean isOptimization() {
        return optimization;
    }

    /**
     * @param optimization the optimization to set
     */
    public void setOptimization(boolean optimization) {
        this.optimization = optimization;
    }

    /**
     * @return the CPUTimeFromCache
     */
    public long getCPUTimeFromCache() {
        return CPUTimeFromCache;
    }

    /**
     * @return the CPUTimeFromLocal
     */
    public long getCPUTimeFromLocal() {
        return CPUTimeFromLocal;
    }

    /**
     * @return the CPUTimeFromWeb
     */
    public long getCPUTimeFromWeb() {
        return CPUTimeFromWeb;
    }

    /**
     * @return the total CPUTime (Sum of all CPU-Times)
     */
    public long getCPUTimeTotal() {
        return (CPUTimeFromCache + CPUTimeFromLocal + CPUTimeFromWeb);
    }

    /**
     * @return the numberOfSimulationsFromCache
     */
    public long getNumberOfSimulationsFromCache() {
        return numberOfSimulationsFromCache;
    }

    /**
     * @return the numberOfSimulationsFromWeb
     */
    public long getNumberOfSimulationsFromWeb() {
        return numberOfSimulationsFromWeb;
    }

    /**
     * @return the numberOfSimulationsFromLocal
     */
    public long getNumberOfSimulationsFromLocal() {
        return numberOfSimulationsFromLocal;
    }

    /**
     * @return the simulationTimeTotal
     */
    public double getSimulationTimeTotal() {
        return simulationTimeTotal;
    }

    /**
     * @return the simulationTimeFromCache
     */
    public double getSimulationTimeFromCache() {
        return simulationTimeFromCache;
    }

    /**
     * @return the simulationTimeFromWeb
     */
    public double getSimulationTimeFromWeb() {
        return simulationTimeFromWeb;
    }

    /**
     * @return the simulationTimeFromLocal
     */
    public double getSimulationTimeFromLocal() {
        return simulationTimeFromLocal;
    }

    /**
     * @return the Ratio of Cache/Total Simulations
     */
    public double getCacheRatio() {
        return (((double) this.getNumberOfSimulationsFromCache() / (double) this.getNumberOfSimulationsTotal()));
    }

    /**
     * @return the calculatedOptimum
     */
    public SimulationType getCalculatedOptimum() {
        return calculatedOptimum;
    }

}
