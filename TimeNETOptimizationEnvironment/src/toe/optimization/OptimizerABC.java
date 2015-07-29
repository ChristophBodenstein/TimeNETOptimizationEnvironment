/*
 * Optimization-Algorithm implemented bei Andy Seidel during Diploma Thesis 2014
 *
 * Original paper [07KB]:
 * Karaboga, Basturk:
 * A powerful and efficient algorithm for numerical
 * function optimization: artificial bee colony (ABC)
 * algorithm (2007)
 */
package toe.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import toe.SimOptiFactory;
import toe.datamodel.SimulationType;
import toe.datamodel.parameter;
import toe.simulation.Simulator;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author A. Seidel
 */
public class OptimizerABC extends OptimizerPopulationBased implements Runnable, Optimizer {

    private ArrayList<Integer> updateCyclesWithoutImprovementList = new ArrayList<>(); //to abandom individual food sources

    private int numEmployedBees = support.getOptimizerPreferences().getPref_ABC_NumEmployedBees();
    private int numOnlookerBees = support.getOptimizerPreferences().getPref_ABC_NumOnlookerBees();
    private int numScoutBees = support.getOptimizerPreferences().getPref_ABC_NumScoutBees();
    private int maxNumberOfFoodUpdateCyclesWithoutImprovement = support.getOptimizerPreferences().getPref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement();

    //************************ Constructors ****************************************************************************************************
    public OptimizerABC() {
        super();
        logFileName = support.getTmpPath() + File.separator + "Optimizing_with_ABC_" + Calendar.getInstance().getTimeInMillis() + "_ALL" + ".csv";
    }

    // ************************ get/set for options menu ****************************************************************************************************
    public int getNumEmployedBees() {
        return numEmployedBees;
    }

    public void setNumEmployedBees(int numEmployedBees) {
        this.numEmployedBees = numEmployedBees;
    }

    public int getNumOnlookerBees() {
        return numOnlookerBees;
    }

    public void setNumOnlookerbees(int numOnlookerBees) {
        this.numOnlookerBees = numOnlookerBees;
    }

    public int getNumScoutBees() {
        return numScoutBees;
    }

    public void setNumScoutBees(int numScoutBees) {
        this.numScoutBees = numScoutBees;
    }

    public int getMaxNumberOfFoodUpdateCyclesWithoutImprovement() {
        return maxNumberOfFoodUpdateCyclesWithoutImprovement;
    }

    public void setMaxNumberOfFoodUpdateCyclesWithoutImprovement(int maxNumberOfFoodUpdateCyclesWithoutImprovement) {
        this.maxNumberOfFoodUpdateCyclesWithoutImprovement = maxNumberOfFoodUpdateCyclesWithoutImprovement;
    }

    // ************************ the 3 main phases of ABC - algoritm ******************************************************************************************
    /**
     * the phase every employed bee flies to her foodsource. On the way to it,
     * she sees a new one and collects information about it
     *
     * @param foodSources the used foodsources so far
     * @param ignoreStepping if preset stepping should be ignored
     * @return foodsources with new candidate solutions added
     */
    private ArrayList<ArrayList<SimulationType>> employedBeePhase(ArrayList<ArrayList<SimulationType>> foodSources, boolean ignoreStepping) {
        for (ArrayList<SimulationType> source : foodSources) {
            source.add(getNewFoodSource(source.get(0), ignoreStepping));
        }

        return foodSources;
    }

    /**
     * in this phase the onlookers fly to known foodsources, based on the
     * solution-information so far
     *
     * @param foodSources the used foodsources so far
     * @param numOnlookerBees the number of onlookers used
     * @param ignoreStepping if preset stepping should be ignored
     * @return foodsources with new candidate solutions added
     */
    private ArrayList<ArrayList<SimulationType>> onlookerBeePhase(ArrayList<ArrayList<SimulationType>> foodSources, int numOnlookerBees, boolean ignoreStepping) {
        foodSources = sortPopulation(foodSources);
        double distanceSum = getDistanceSum(foodSources);

        for (ArrayList<SimulationType> source : foodSources) {
            double sourceQuality = 1 / source.get(0).getDistanceToTargetValue();
            int numOnlookersOnSource = (int) (numOnlookerBees * (sourceQuality / distanceSum)) + 1;
            for (int i = 0; i < numOnlookerBees; ++i) {
                source.add(getNewFoodSource(source.get(0), ignoreStepping));
            }
        }
        return foodSources;
    }

    //TODO: fill, should do nearly the same like in start phase  
    /**
     * in this phase the scouts fly to random solutions in the design space end
     * explore their quality
     *
     * @param foodSources the used foodsources so far
     * @param numScoutBees the number of scouts used
     * @param ignoreStepping if preset stepping should be ignored
     * @return
     */
    private ArrayList<ArrayList<SimulationType>> scoutBeePhase(ArrayList<ArrayList<SimulationType>> foodSources, int numScoutBees, boolean ignoreStepping) {
        for (int i = 0; i < numScoutBees; ++i) {

        }
        return foodSources;
    }

    /**
     * Sorts the food sources by rank, also filters neighbourhood of single
     * source to get the best one
     *
     * @param foodSources ArrayList of food sources to be sorted
     * @return
     */
    private ArrayList<ArrayList<SimulationType>> sortAndFilterFoodSources(ArrayList<ArrayList<SimulationType>> foodSources) {
        foodSources = filterBestSolutionInNeighbourhood(foodSources);
        foodSources = sortPopulation(foodSources);
        return foodSources;
    }

    private ArrayList<ArrayList<SimulationType>> filterBestSolutionInNeighbourhood(ArrayList<ArrayList<SimulationType>> foodSources) {
        ArrayList<ArrayList<SimulationType>> newFoodList = new ArrayList<ArrayList<SimulationType>>();
        for (ArrayList<SimulationType> neighbourhood : foodSources) {
            setMeasureTargets(neighbourhood);
            Collections.sort(neighbourhood, new Comparator<SimulationType>() {
                @Override
                public int compare(SimulationType a, SimulationType b) {
                    return Double.compare(a.getDistanceToTargetValue(), b.getDistanceToTargetValue());
                }
            });
            ArrayList<SimulationType> topSource = new ArrayList<SimulationType>();
            topSource.add(neighbourhood.get(0));
            newFoodList.add(topSource);
        }
        return newFoodList;
    }

    @Override
    public void run() {
        int optiCycleCounter = 0;
        population = createRandomPopulation(numEmployedBees, false);
        //reset updateCycles without improvement for every foodSource
        for (int fs = 0; fs < population.size(); ++fs) {
            updateCyclesWithoutImprovementList.add(0);
        }

        Simulator mySimulator = SimOptiFactory.getSimulator();
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), false);
        //support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
        synchronized (mySimulator) {
            try {
                mySimulator.wait();
            } catch (InterruptedException ex) {
                support.log("Problem waiting for end of non-cache-simulator.", typeOfLogLevel.ERROR);
            }
        }

        ArrayList<SimulationType> simulationResults = mySimulator.getListOfCompletedSimulationParsers();
        population = getPopulationFromSimulationResults(simulationResults);

        while (optiCycleCounter < this.maxNumberOfOptiCycles) {
            if (currentNumberOfOptiCyclesWithoutImprovement >= maxNumberOfOptiCyclesWithoutImprovement) {
                support.log("Too many optimization cycles without improvement. Ending optimization.", typeOfLogLevel.INFO);
                break;
            }

            population = employedBeePhase(population, false);
            population = onlookerBeePhase(population, numOnlookerBees, false);
            population = scoutBeePhase(population, numScoutBees, false);

            for (int fs = 0; fs < population.size(); ++fs) {
                ArrayList<SimulationType> source = population.get(fs);
                mySimulator.initSimulator(getNextParameterSetAsArrayList(source), false);
                //support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
                synchronized (mySimulator) {
                    try {
                        mySimulator.wait();
                    } catch (InterruptedException ex) {
                        support.log("Problem waiting for end of non-cache-simulator.", typeOfLogLevel.ERROR);
                    }
                }
                source = mySimulator.getListOfCompletedSimulationParsers();
                population.set(fs, source);
                support.addLinesToLogFileFromListOfParser(source, logFileName);
            }

            population = sortAndFilterFoodSources(population);

            //cut back num population, eliminates the bad solutions 
            int numFoodSourcesToCut = population.size() - numEmployedBees;
            for (int fs = 0; fs < numFoodSourcesToCut; ++fs) {
                population.remove(population.size() - 1); //cuts the last one
            }

            printPopulationDistances();
            updateTopMeasure();
            ++optiCycleCounter;
            if (topMeasure.getDistanceToTargetValue() <= 0) {
                break;
            }

        }
        optimized = true;
    }

    /*private void doBatchRun()
     {
     ArrayList<SimulationType> optiResults = new ArrayList<SimulationType>();
     ArrayList<Integer> optiTotalSimualtions = new ArrayList<Integer>();
     ArrayList<Integer> optiTotalCachedSimualtions = new ArrayList<Integer>();
     for (int i = 0; i< numOptiRuns; ++i)
     {
     int optiCycleCounter=0;
     int totalSimulations  = 0;
     int totalCachedSimualtions = 0;
     population = createRandomPopulation(numEmployedBees, false);
     //reset updateCycles without improvement for every foodSource
     for (int fs = 0; fs<population.size(); ++fs)
     {
     updateCyclesWithoutImprovementList.add(0);
     }
        
     Simulator mySimulator = SimOptiFactory.getSimulator();       
     mySimulator.initSimulator(getNextParameterSetAsArrayList(), optiCycleCounter, false);
     support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
     int simulationCounter = 0;
        
     ArrayList<SimulationType> simulationResults = mySimulator.getListOfCompletedSimulationParsers();
     population = getPopulationFromSimulationResults(simulationResults);
        
     while(optiCycleCounter < this.maxNumberOfOptiCycles)
     {
     if (currentNumberOfOptiCyclesWithoutImprovement >= maxNumberOfOptiCyclesWithoutImprovement)
     {
     support.log("Too many optimization cycles without improvement. Ending optimization.");
     break;
     }
            
     population = employedBeePhase(population, false);
     population = onlookerBeePhase(population, numOnlookerBees, false);
     population = scoutBeePhase(population, numScoutBees, false);
            
     for (int fs=0; fs<population.size(); ++fs)
     {
     ArrayList<SimulationType> source = population.get(fs);
     mySimulator.initSimulator(getNextParameterSetAsArrayList(source), optiCycleCounter, false);
     support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
     source = mySimulator.getListOfCompletedSimulationParsers();
     totalSimulations += source.size();
     population.set(fs, source);
     //support.addLinesToLogFileFromListOfParser(source, logFileName);
     }
                
     population = sortAndFilterFoodSources(population);
            
     //cut back num population, eliminates the bad solutions 
     int numFoodSourcesToCut = population.size() - numEmployedBees; 
     for (int fs = 0; fs<numFoodSourcesToCut; ++fs)
     {
     population.remove(population.size()-1); //cuts the last one
     }
            
     //printPopulationDistances();
     updateTopMeasure();
     ++optiCycleCounter;
     ++simulationCounter;
     }
     optiResults.add(topMeasure);
     optiTotalSimualtions.add(totalSimulations);
     optiTotalCachedSimualtions.add(totalCachedSimualtions);
     topDistance = Double.POSITIVE_INFINITY;
     currentNumberOfOptiCyclesWithoutImprovement = 0;
     }
     support.addLinesToLogFileFromListOfSimulationBatchesIncludingNumRuns(optiResults, optiTotalSimualtions, optiTotalCachedSimualtions, logFileName);

     for (int i = 0;i<optiResults.size(); ++i)
     {
     String logString = "" + optiTotalSimualtions.get(i) + " " + optiTotalCachedSimualtions.get(i);
     support.log(logString);
     }            
     }*/
    private SimulationType getNewFoodSource(SimulationType originalSource, boolean ignoreStepping) {
        //get new radom reference food source
        int refFoodSourceNumber = randomGenerator.nextInt(population.size());
        int paramaterNumberToModify = parametersToModify.get(randomGenerator.nextInt(parametersToModify.size()));
        SimulationType refFoodSource = population.get(refFoodSourceNumber).get(0);
        SimulationType newFoodSoure = new SimulationType(originalSource);

        ArrayList<parameter> newlParameterSet = newFoodSoure.getListOfParameters();
        double originalParameterValue = originalSource.getListOfParameters().get(paramaterNumberToModify).getValue();
        double refParameterValue = refFoodSource.getListOfParameters().get(paramaterNumberToModify).getValue();
        double scalingFactor = 2 * randomGenerator.nextDouble() - 1;
        double newParameterValue = originalParameterValue + scalingFactor * (originalParameterValue - refParameterValue);

        newlParameterSet.get(paramaterNumberToModify).setValue(newParameterValue);
        newlParameterSet = roundToStepping(newlParameterSet);
        newFoodSoure.setListOfParameters(newlParameterSet);

        return newFoodSoure;
    }

    private double getDistanceSum(ArrayList< ArrayList<SimulationType>> foodSources) {
        double distanceSum = 0;

        for (ArrayList<SimulationType> foodSource : foodSources) {
            distanceSum += 1 / foodSource.get(0).getDistanceToTargetValue(); //to get big weight to small distances
        }
        return distanceSum;
    }

    /**
     * Set the logfilename this is useful for multi-optimization or if you like
     * specific names for your logfiles
     *
     * @param name Name (path) of logfile
     */
    @Override
    public void setLogFileName(String name) {
        this.logFileName = name;
    }

    /**
     * Returns the used logfileName
     *
     * @return name of logfile
     */
    @Override
    public String getLogFileName() {
        return this.logFileName;
    }
}
