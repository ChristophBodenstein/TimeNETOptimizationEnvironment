/*
 * Christoph Bodenstein
 * Aggregator Class for statistics
 * It holds instances for every Simulation/Optimization
 * With every log-file-entry the information is added to the statistic-instance with the corresponding name

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.helper;

import java.util.ArrayList;
import java.util.Iterator;
import toe.datamodel.MeasureType;
import toe.datamodel.SimulationType;
import toe.support;
import toe.plot.RPlugin;
import toe.typedef;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Christoph Bodenstein
 */
public class StatisticAggregator {

    private static ArrayList<Statistic> listOfStatistics = new ArrayList<Statistic>();

    /**
     * Constructor
     */
    public StatisticAggregator() {
        listOfStatistics = new ArrayList<Statistic>();
    }

    /**
     * Adds one simulation to statistic which is identified by its logfilename
     *
     * @param filename name of the logfile of statistic
     * @param p simulation to be added to statistic
     */
    public static void addToStatistics(SimulationType p, String filename) {
        Statistic myStatistic = getStatisticByName(filename);
        myStatistic.addSimulation(p);
        //support.getMainFrame().updateSimulationCounterLabel(myStatistic.getNumberOfSimulationsTotal());
        //update cached list in PlotFrameController
        RPlugin.updateCachedListOfStatistics();
    }

    /**
     * returns the statistic with given logfilename
     *
     * @param filename logfilename of statistic to be returned
     * @return requested Statistic
     */
    public static Statistic getStatisticByName(String filename) {
        Statistic returnValue = null;

        for (Statistic listOfStatistic : listOfStatistics) {
            if (listOfStatistic.getName().equals(filename)) {
                returnValue = listOfStatistic;
            }
        }
        if (returnValue == null) {
            returnValue = new Statistic(filename);
            listOfStatistics.add(returnValue);
        }
        return returnValue;
    }

    /**
     * Prints all Statistics, available in this Aggregator (collected since
     * program start)
     */
    public static void printAllStatistics() {

        if ((listOfStatistics == null) || (listOfStatistics.size() < 1)) {
            support.log("No Statistics Available!", typeOfLogLevel.ERROR);
            return;
        }

        for (Statistic listOfStatistic : listOfStatistics) {
            listOfStatistic.printStatisticToLog();
        }
        //Print all Optimization Statistics if available
        printOptiStatistics();
    }

    /**
     * Prints Statistics with specified name (name of log-file)
     *
     * @param name Name of log-File to identify the statistic
     */
    public static void printStatistic(String name) {
        getStatisticByName(name).printStatisticToLog();
    }

    /**
     * Prints the last statistic
     */
    public static void printLastStatistic() {
        if ((listOfStatistics == null) || (listOfStatistics.size() < 1)) {
            support.log("No Statistics Available!", typeOfLogLevel.ERROR);
        } else {
            listOfStatistics.get(listOfStatistics.size() - 1).printStatisticToLog();
        }
    }

    /**
     *
     * @return listOfStatistics
     */
    public static ArrayList<Statistic> getListOfStatistics() {
        return listOfStatistics;
    }

    /**
     * Prints all Optimization statistics and a summary of the last
     * optimizations
     */
    public static void printOptiStatistics() {
        boolean logToWindow = support.isLogToWindow();
        support.setLogToWindow(true);//Activate log to Window, no matter what user checked in ui
        int numberOfAllOptimizationRuns;
        double averageDistanceToOptimumAbsolute = 0;
        double averageDistanceToOptimumInValueRange = 0;
        double averageDistanceToOptimumInDefinitionRange = 0;
        double averageNumberOfSimulations = 0;
        double averageCPUTimeTotal = 0;
        double averageCPUTimeLocal = 0;
        double averageCPUTimeWeb = 0;
        double averageCPUTimeCache = 0;
        double averageNumberOfSimulationsLocal = 0;
        double averageNumberOfSimulationsWeb = 0;
        double averageNumberOfSimulationsCache = 0;
        double averageSimulationTimeFromLocal = 0;
        double averageSimulationTimeFromWeb = 0;
        double averageSimulationTimeFromCache = 0;
        double averageSimulationTimeTotal = 0;
        double averageCacheRatio = 0;
        double tmpRelativeDistanceInValueRange = 101;
        double tmpRelativeDistanceInDefinitionRange;

        int numberOfFoundOptimaWithinRangeClass[] = new int[support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES];
        int numberOfFoundOptimaWithinRangeClassDS[] = new int[support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES];

        //init RangeClasses
        for (int i = 0; i < support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES; i++) {
            numberOfFoundOptimaWithinRangeClass[i] = 0;
            numberOfFoundOptimaWithinRangeClassDS[i] = 0;
        }

        ArrayList<Statistic> optiSimulationList = new ArrayList<Statistic>();
        //Fill new ArrayList with Statistic that are Optimization
        for (Statistic s : listOfStatistics) {
            if (s.isOptimization()) {
                optiSimulationList.add(s);
                averageDistanceToOptimumAbsolute += s.getDistanceToTargetValue();
                averageNumberOfSimulations += s.getNumberOfSimulationsTotal();
                averageNumberOfSimulationsLocal += s.getNumberOfSimulationsFromLocal();
                averageNumberOfSimulationsWeb += s.getNumberOfSimulationsFromWeb();
                averageNumberOfSimulationsCache += s.getNumberOfSimulationsFromCache();

                averageCPUTimeTotal += s.getCPUTimeTotal();
                averageCPUTimeLocal += s.getCPUTimeFromLocal();
                averageCPUTimeWeb += s.getCPUTimeFromWeb();
                averageCPUTimeCache += s.getCPUTimeFromCache();

                averageSimulationTimeFromLocal += s.getSimulationTimeFromLocal();
                averageSimulationTimeFromCache += s.getSimulationTimeFromCache();
                averageSimulationTimeFromWeb += s.getSimulationTimeFromWeb();
                averageSimulationTimeTotal += s.getSimulationTimeTotal();

                averageCacheRatio += s.getCacheRatio();

                if (s.getCalculatedOptimum() != null) {

                    averageDistanceToOptimumInValueRange += s.getRelativeDistanceToOptimumInValueRange();
                    averageDistanceToOptimumInDefinitionRange += s.getRelativeDistanceToOptimumInDefinitionRange();
                    //Calculate RangeClasses
                    tmpRelativeDistanceInValueRange = s.getRelativeDistanceToOptimumInValueRange();
                    tmpRelativeDistanceInDefinitionRange = s.getRelativeDistanceToOptimumInDefinitionRange();

                    for (int i = 1; i <= support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES; i++) {
                        if (tmpRelativeDistanceInValueRange <= i * 100 / support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES) {
                            numberOfFoundOptimaWithinRangeClass[i - 1]++;
                        }
                        if (tmpRelativeDistanceInDefinitionRange <= i * 100 / support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES) {
                            numberOfFoundOptimaWithinRangeClassDS[i - 1]++;
                        }
                    }
                } else {
                    //No Calculated Optimum exists
                    support.log("Calculated optimum does not exist. So some statistics are not available.", typeOfLogLevel.RESULT);
                }

            }
        }
        numberOfAllOptimizationRuns = optiSimulationList.size();
        support.log("Number of optimizations: " + numberOfAllOptimizationRuns, typeOfLogLevel.RESULT);
        if (numberOfAllOptimizationRuns >= 1) {
            averageDistanceToOptimumAbsolute = averageDistanceToOptimumAbsolute / numberOfAllOptimizationRuns;
            averageDistanceToOptimumInValueRange = averageDistanceToOptimumInValueRange / numberOfAllOptimizationRuns;
            averageDistanceToOptimumInDefinitionRange = averageDistanceToOptimumInDefinitionRange / numberOfAllOptimizationRuns;
            averageNumberOfSimulations = averageNumberOfSimulations / numberOfAllOptimizationRuns;
            averageNumberOfSimulationsLocal = averageNumberOfSimulationsLocal / numberOfAllOptimizationRuns;
            averageNumberOfSimulationsWeb = averageNumberOfSimulationsWeb / numberOfAllOptimizationRuns;
            averageNumberOfSimulationsCache = averageNumberOfSimulationsCache / numberOfAllOptimizationRuns;
            averageCPUTimeTotal = averageCPUTimeTotal / numberOfAllOptimizationRuns;
            averageCPUTimeLocal = averageCPUTimeLocal / numberOfAllOptimizationRuns;
            averageCPUTimeWeb = averageCPUTimeWeb / numberOfAllOptimizationRuns;
            averageCPUTimeCache = averageCPUTimeCache / numberOfAllOptimizationRuns;
            averageSimulationTimeFromLocal = averageSimulationTimeFromLocal / numberOfAllOptimizationRuns;
            averageSimulationTimeFromWeb = averageSimulationTimeFromWeb / numberOfAllOptimizationRuns;
            averageSimulationTimeFromCache = averageSimulationTimeFromCache / numberOfAllOptimizationRuns;
            averageSimulationTimeTotal = averageSimulationTimeTotal / numberOfAllOptimizationRuns;
            averageCacheRatio = averageCacheRatio / numberOfAllOptimizationRuns;

            MeasureType targetMeasure = support.getOptimizationMeasure();

            support.log("++++ Start of Optimization Statistics ++++", typeOfLogLevel.RESULT);
            support.log("Number of Optimization runs: " + numberOfAllOptimizationRuns, typeOfLogLevel.RESULT);
            support.log("Average distance to target value (" + targetMeasure.getTargetValue() + "/" + targetMeasure.getTargetTypeOf() + "): " + averageDistanceToOptimumAbsolute, typeOfLogLevel.RESULT);

            support.log("Average number of Simulations: " + averageNumberOfSimulations + " ( " + averageNumberOfSimulationsLocal + " | " + averageNumberOfSimulationsWeb + " | " + averageNumberOfSimulationsCache + " )" + "  (Local|Web|Cache)", typeOfLogLevel.RESULT);
            support.log("Average CPU Time used: " + averageCPUTimeTotal + " ( " + averageCPUTimeLocal + " | " + averageCPUTimeWeb + " | " + averageCPUTimeCache + " )" + "  (Local|Web|Cache)", typeOfLogLevel.RESULT);
            support.log("Average Simulation Steps: " + averageSimulationTimeTotal + " ( " + averageSimulationTimeFromLocal + " | " + averageSimulationTimeFromWeb + " | " + averageSimulationTimeFromCache + " )" + "  (Local|Web|Cache)", typeOfLogLevel.RESULT);
            support.log("Average Cache Ratio (All/Total): " + averageCacheRatio, typeOfLogLevel.RESULT);

            if (tmpRelativeDistanceInValueRange < 101) {
                support.log("Average relative distance to calculated optimum in value range: " + averageDistanceToOptimumInValueRange + " %", typeOfLogLevel.RESULT);
                support.log("Average relative (" + support.getChosenTypeOfRelativeDistanceCalculation().toString() + ") distance to calculated optimum in definition range " + averageDistanceToOptimumInDefinitionRange + " %", typeOfLogLevel.RESULT);
                String tmpOutHead = "Radius:    ";
                String tmpOutValue = "Values:    ";
                String tmpOutDSValue = "DS-Values: ";
                for (int i = 1; i <= support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES; i++) {
                    tmpOutHead += "| " + support.padLeft(String.valueOf(i * 100 / support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES) + "%", 10);

                    //support.log("# of Optima within Radius of " +(i*100/support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES)+" around Calculated Optimum: "+ numberOfFoundOptimaWithinRangeClass[i-1] +" --  "+(numberOfFoundOptimaWithinRangeClass[i-1]*100/numberOfAllOptimizationRuns)+"%");    
                    tmpOutValue += "| " + support.padLeft(numberOfFoundOptimaWithinRangeClass[i - 1] + "--" + (numberOfFoundOptimaWithinRangeClass[i - 1] * 100 / numberOfAllOptimizationRuns) + "%", 10);
                    //support.log("# of Optima within DS-Radius of " +(i*100/support.DEFAULT_NUMBER_OF_OPTI_PROB_CLASSES)+" around Calculated Optimum: "+ numberOfFoundOptimaWithinRangeClassDS[i-1] +" --  "+(numberOfFoundOptimaWithinRangeClassDS[i-1]*100/numberOfAllOptimizationRuns)+"%");        
                    tmpOutDSValue += "| " + support.padLeft(numberOfFoundOptimaWithinRangeClassDS[i - 1] + "--" + (numberOfFoundOptimaWithinRangeClassDS[i - 1] * 100 / numberOfAllOptimizationRuns) + "%", 10);
                }
                support.log(tmpOutHead, typeOfLogLevel.RESULT);
                support.log(tmpOutValue, typeOfLogLevel.RESULT);
                support.log(tmpOutDSValue, typeOfLogLevel.RESULT);
            }
            //Latex-Friendly output
            //support.log("Sim#, Distance, DistancsDS, CPU-Time");
            //support.log("&"+averageNumberOfSimulations+"    &"+averageDistanceToOptimumInValueRange+"\\%  &"+averageDistanceToOptimumInDefinitionRange+"\\% &"+averageCPUTimeTotal);
            support.log("WrongSolutionInARow, WrongSolutionsPerDir, Distance, DistancsEUKLID, Sim#, CPU-Time, CHR", typeOfLogLevel.RESULT);
            toe.optimization.OptimizerPreferences p = support.getOptimizerPreferences();
            support.log("Outputting Optimization Config: "
                    + "WrongSimulationsUntilBreak   "
                    + "SizeOfNeighborhood \\%"
                    + "&WrongSimulationsPerDirection "
                    + "&averageDistanceToOptimumInValueRange\\%     "
                    + "&averageDistanceToOptimumInDefinitionRange\\%    "
                    + "&averageNumberOfSimulations "
                    + "&averageCPUTimeTotal "
                    + "&averageCacheRatio*100", typeOfLogLevel.RESULT);
            support.log(p.getPref_WrongSimulationsUntilBreak() + "   & " + p.getPref_SizeOfNeighborhood() + "\\%     &" + p.getPref_WrongSimulationsPerDirection() + "      &" + support.round3(averageDistanceToOptimumInValueRange) + "\\%     &" + support.round3(averageDistanceToOptimumInDefinitionRange) + "\\%    &" + averageNumberOfSimulations + "    &" + averageCPUTimeTotal + "     &" + support.round3(averageCacheRatio) * 100 + "\\%  \\\\  \\hline", typeOfLogLevel.RESULT);
            support.log("NeighborhoodType: " + p.getPref_NeighborhoodType().toString(), typeOfLogLevel.RESULT);
            support.log("Optimizer: " + support.getChosenOptimizerType().toString(), typeOfLogLevel.RESULT);
            support.log("Simulator: " + support.getChosenSimulatorType().toString(), typeOfLogLevel.RESULT);
            if (support.getChosenSimulatorType().equals(typedef.typeOfSimulator.Benchmark)) {
                support.log("Benchmarkfunction: " + support.getChosenBenchmarkFunction().toString(), typeOfLogLevel.RESULT);
            }

            support.log("++++ End of Optimization Statistics ++++", typeOfLogLevel.RESULT);
        }
        support.setLogToWindow(logToWindow);//Set Window-logging to original value
    }

    /**
     * Removes all optimization statistics from List of statistics
     */
    public static void removeOldOptimizationsFromList() {
        Iterator<Statistic> it = listOfStatistics.iterator();

        while (it.hasNext()) {
            Statistic s = it.next();
            if (s.isOptimization()) {
                it.remove();
            }
        }

    }
}
