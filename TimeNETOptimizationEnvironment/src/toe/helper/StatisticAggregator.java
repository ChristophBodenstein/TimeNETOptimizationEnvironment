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
                s.printOptimizerStatisticsToLog(typeOfLogLevel.INFO);
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
            support.log(p.getPref_HC_WrongSimulationsUntilBreak() + "   & " + p.getPref_HC_SizeOfNeighborhood() + "\\%     &" + p.getPref_HC_WrongSimulationsPerDirection() + "      &" + support.round(averageDistanceToOptimumInValueRange, 3) + "\\%     &" + support.round(averageDistanceToOptimumInDefinitionRange, 3) + "\\%    &" + averageNumberOfSimulations + "    &" + averageCPUTimeTotal + "     &" + support.round(averageCacheRatio, 3) * 100 + "\\%  \\\\  \\hline", typeOfLogLevel.RESULT);
            //TODO: Export as Arrlaylist of Strings to create files easily
            ArrayList<String> tmpStatistics = new ArrayList();
            ArrayList<String> tmpHeadline = new ArrayList();

            //Hill Climbing
            tmpStatistics.add(Integer.toString(p.getPref_HC_WrongSimulationsUntilBreak()));
            tmpHeadline.add("WrongSimulationsUntilBreak");
            tmpStatistics.add(Integer.toString(p.getPref_HC_SizeOfNeighborhood()));
            tmpHeadline.add("SizeOfNeighborhood");
            tmpStatistics.add(Integer.toString(p.getPref_HC_WrongSimulationsPerDirection()));
            tmpHeadline.add("WrongSimulationsPerDirection");
            tmpStatistics.add(p.getPref_HC_NeighborhoodType().toString());
            tmpHeadline.add("NeighborhoodType");

            //Simulated Annealing, phase 0
            tmpStatistics.add(p.getPref_SA_Cooling(0).toString());
            tmpHeadline.add("SA_0_Cooling");
            tmpStatistics.add(p.getPref_SA_CalculationOfNextParameterset(0).toString());
            tmpHeadline.add("SA_0_CalculationOfNextParameterset");
            tmpStatistics.add(Long.toString(p.getPref_SA_NumberOfEstimatedSASimulations(0)));
            tmpHeadline.add("SA_0_EstimatedSASimulations");

            //Simulated Annealing, phase 1
            tmpStatistics.add(p.getPref_SA_Cooling(1).toString());
            tmpHeadline.add("SA_1_Cooling");
            tmpStatistics.add(p.getPref_SA_CalculationOfNextParameterset(1).toString());
            tmpHeadline.add("SA_1_CalculationOfNextParameterset");
            tmpStatistics.add(Long.toString(p.getPref_SA_NumberOfEstimatedSASimulations(1)));
            tmpHeadline.add("SA_1_EstimatedSASimulations");

            //Population based
            tmpStatistics.add(p.getPref_Genetic_TypeOfCrossover().toString());
            tmpHeadline.add("GeneticTypeOfCrossover");
            tmpStatistics.add(Integer.toString(p.getPref_Genetic_PopulationSize()));
            tmpHeadline.add("GeneticPopulationSize");
            tmpStatistics.add(Double.toString(p.getPref_Genetic_MutationChance()));
            tmpHeadline.add("GeneticMutationChance");
            tmpStatistics.add(Integer.toString(p.getPref_Genetic_MaximumOptirunsWithoutSolution()));
            tmpHeadline.add("GeneticMaximumOptirunsWithoutSolution");
            tmpStatistics.add(Boolean.toString(p.getPref_Genetic_MutateTopSolution()));
            tmpHeadline.add("GeneticMutateTopSolution");
            tmpStatistics.add(Integer.toString(p.getPref_Genetic_NumberOfCrossings()));
            tmpHeadline.add("GeneticNumberOfCrossings");

            //Multiphase
            tmpStatistics.add(Integer.toString(p.getPref_MP_NumberOfPhases()));
            tmpHeadline.add("MP_NumberOfPhases");
            tmpStatistics.add(p.getPref_MP_typeOfUsedMultiPhaseOptimization().toString());
            tmpHeadline.add("MP_typeOfUsedMultiPhaseOptimization");
            tmpStatistics.add(Double.toString(p.getPref_MP_ConfidenceIntervallStart()));
            tmpHeadline.add("MP_ConfidenceIntervallStart");
            tmpStatistics.add(Double.toString(p.getPref_MP_ConfidenceIntervallEnd()));
            tmpHeadline.add("MP_ConfidenceIntervallEnd");
            tmpStatistics.add(Double.toString(p.getPref_MP_MaxRelErrorStart()));
            tmpHeadline.add("MP_MaxRelErrorStart");
            tmpStatistics.add(Double.toString(p.getPref_MP_MaxRelErrorEnd()));
            tmpHeadline.add("MP_MaxRelErrorEnd");
            tmpStatistics.add(Boolean.toString(p.getPref_MP_KeepDesignSpaceAndResolution()));
            tmpHeadline.add("MP_KeepDesignSpaceAndResolution");
            tmpStatistics.add(Double.toString(p.getPref_MP_InternalParameterStart()));
            tmpHeadline.add("MP_InternalParameterStart");
            tmpStatistics.add(Double.toString(p.getPref_MP_InternalParameterEnd()));
            tmpHeadline.add("MP_InternalParameterEnd");
            tmpStatistics.add(p.getPref_MP_InternalParameterToIterateInMultiphase().toString());
            tmpHeadline.add("MP_InternalParameterToIterateInMultiphase");

            //Results, quality of found optima
            tmpStatistics.add(Double.toString(support.round(averageDistanceToOptimumInValueRange, 3)));
            tmpHeadline.add("averageDistanceToOptimumInValueRange");
            tmpStatistics.add(Double.toString(support.round(averageDistanceToOptimumInDefinitionRange, 3)));
            tmpHeadline.add("averageDistanceToOptimumInDefinitionRange");
            tmpStatistics.add(Double.toString(support.round(averageNumberOfSimulations, 3)));
            tmpHeadline.add("averageNumberOfSimulations");
            tmpStatistics.add(Double.toString(support.round(averageCPUTimeTotal, 3)));
            tmpHeadline.add("averageCPUTimeTotal");
            tmpStatistics.add(Double.toString(support.round(averageCacheRatio, 3) * 100) + "%");
            tmpHeadline.add("averageCacheRatio");

            support.addOptiStatistics(tmpStatistics);

            support.log("NeighborhoodType: " + p.getPref_HC_NeighborhoodType().toString(), typeOfLogLevel.RESULT);
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
