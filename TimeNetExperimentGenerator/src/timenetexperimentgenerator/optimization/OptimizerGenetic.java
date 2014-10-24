/*
 * Optimization-Algorithm implemented bei Andy Seidel during Diploma Thesis 2014
 *
 */

package timenetexperimentgenerator.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import timenetexperimentgenerator.SimOptiFactory;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.support;

/**
 *
 * @author A. Seidel
 */

public class OptimizerGenetic extends OptimizerPopulationBased implements Runnable, Optimizer
{
    private int populationSize = support.getOptimizerPreferences().getPref_GeneticPopulationSize(); //size of population after selection-phase
    private double mutationChance = support.getOptimizerPreferences().getPref_GeneticMutationChance(); // chance of genes to Mutate
    private boolean mutateTopMeasure = support.getOptimizerPreferences().getPref_GeneticMutateTopSolution();

    /**
     * returnes the population size used for optimization
     * @return the population size
     */
    public int getPopulationSize()
    {
        return this.populationSize;
    }
    
    /**
     * sets the population size, if its a least one. zero or negative values are ignored
     * @param newPopulationSize the new number of charges
     */
    public void setPopulationSize(int newPopulationSize)
    {
        if (newPopulationSize > 0)
        {
            this.populationSize = newPopulationSize;
        }
    }
    
    /**
     * return maximum number of optimization cycles before breaking up
     * @return the current maximum number of optimization cycles
     */
    public int getMaxNumberOfOptiCycles()
    {
        return this.maxNumberOfOptiCycles;
    }
    
    /**
     * sets maximum number of optimization cycles. Has to be at least 1, otherwise it is ignored.
     * @param newMaxNumberOfOtpiCycles the new maximum number of optimization cycles
     */
    public void setMaxNumberOfOptiCycles(int newMaxNumberOfOtpiCycles)
    {
        if (newMaxNumberOfOtpiCycles > 0)
        {
            this.maxNumberOfOptiCycles = newMaxNumberOfOtpiCycles;
        }
    }
    
    /**
     * gets maximum number of optimization cycles without improvement, before breaking optimization loop.
     * @return the maximum number of optimization cycles without improvemet
     */
    public int getMaxNumberOfOptiCyclesWithoutImprovement()
    {
        return this.maxNumberOfOptiCyclesWithoutImprovement;
    }
    
    /**
     * sets maximum number of optimization cycles without improvement. Has to be at least 1, otherwise it is ignored.
     * @param newMaxNumberOfOptiCyclesWithoutImprovement the new maximum number of optimization cycles without improvement
     */
    public void setMaxNumberOfOptiCyclesWithoutImprovement(int newMaxNumberOfOptiCyclesWithoutImprovement)
    {
        if (newMaxNumberOfOptiCyclesWithoutImprovement > 0)
        {
            this.maxNumberOfOptiCyclesWithoutImprovement = newMaxNumberOfOptiCyclesWithoutImprovement;
        }
    }
    
    public double getMutatuionChance()
    {
        return this.mutationChance;
    }
    
    public void setMutationChance(double newMutationChance)
    {
        this.mutationChance = newMutationChance;
    }
            

    public OptimizerGenetic()
    {
        logFileName=support.getTmpPath()+File.separator+"Optimizing_with_Genetic_Algorithm_"+Calendar.getInstance().getTimeInMillis()+"_ALL"+".csv";
    }
    
    public void run() 
    {
        int optiCycleCounter = 0;
        population = createRandomPopulation(populationSize, false);
        
        Simulator mySimulator = SimOptiFactory.getSimulator();       
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), optiCycleCounter, false);
        support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
        
        ArrayList<SimulationType> simulationResults = mySimulator.getListOfCompletedSimulationParsers(); 
        population = getPopulationFromSimulationResults(simulationResults);
        
        int simulationCounter = 0;
        while(optiCycleCounter < this.maxNumberOfOptiCycles)
        {
            if (currentNumberOfOptiCyclesWithoutImprovement >= maxNumberOfOptiCyclesWithoutImprovement)
            {
                support.log("Too many optimization cycles without improvement. Ending optimization.");
                break;
            }
            
            //modification phase -----------------------------------------------------------------------
            population = crossPopulation(population, populationSize); //doubles population
            population = mutatePopulation(population, mutationChance); //
            
            
            //simulation phase -----------------------------------------------------------------------
            ArrayList< ArrayList<parameter> > parameterList = getNextParameterSetAsArrayList();
            for (ArrayList<parameter> pArray : parameterList)
            {
                pArray =  roundToStepping(pArray);
            }
            
            mySimulator = SimOptiFactory.getSimulator();
            mySimulator.initSimulator(parameterList, simulationCounter, false);
            support.waitForEndOfSimulator(mySimulator, simulationCounter, support.DEFAULT_TIMEOUT);
            simulationCounter = mySimulator.getSimulationCounter();
            
            simulationResults = mySimulator.getListOfCompletedSimulationParsers();
            population = getPopulationFromSimulationResults(simulationResults);
            support.addLinesToLogFileFromListOfParser(simulationResults, logFileName);
            
            //evaluation phase --------------------------------------------------------------------------
            population = cutPopulation(population);
            updateTopMeasure();
            printPopulationDistances();
            
            ++optiCycleCounter;
        }

    }
    
    /**
     * creates genetic crossover of mothers and fathers paramter[] to create new child
     * @param father the father for the genetic parameter mix process
     * @param mother the mother for the genetic parameter mix process
     * @return the resulting child
     */
    private SimulationType crossOver(SimulationType father, SimulationType mother)
    {
        if (father == null || mother == null)
        {
            return null;
        }            
        if (father.getListOfParameters().size() != mother.getListOfParameters().size())
        {
            support.log("Parameter length of mother and father different. No Crossover Possible");
            return null;
        }
        SimulationType child = new SimulationType(father);
        ArrayList<parameter> fatherDNA = father.getListOfParameters();
        ArrayList<parameter> motherDNA = mother.getListOfParameters();
        ArrayList<parameter> childDNA = child.getListOfParameters();
             
        //find random crossover-point
        int crossingPoint = randomGenerator.nextInt(father.getListOfParameters().size());
        
        //copy paramters from mother after crossing Point, father-DNA is already included through copy-constructor
        for (int i = crossingPoint; i<fatherDNA.size(); ++i)
        {
            parameter parameterFromMother = null;
            try
            {
                parameterFromMother = (parameter)motherDNA.get(i).clone();
            }
            catch (CloneNotSupportedException e)
            {
                support.log(e.getMessage());
            }
            if (parameterFromMother!=null) //clone was successful
            {
                childDNA.set(i, parameterFromMother);
            }
        }
        child.setListOfParameters(childDNA);
               
        return child;
    }
    
    private ArrayList< ArrayList<SimulationType> > crossPopulation(ArrayList< ArrayList<SimulationType> > population, int numNewChildren)
    {
        for (int i = 0; i<numNewChildren; ++i)
        {
            int indexOfFather = randomGenerator.nextInt(populationSize);
            int indexOfMother = randomGenerator.nextInt(populationSize);
        
            SimulationType child = crossOver(population.get(indexOfFather).get(0), population.get(indexOfMother).get(0));
            ArrayList<SimulationType> childList = new ArrayList<SimulationType>();
            childList.add(child);
            population.add(childList);   
        }        
        return population;
    }
    
    private ArrayList< ArrayList<SimulationType> > mutatePopulation(ArrayList< ArrayList<SimulationType> > population, double mutationProbability)
    {
        int mutationStart = 1;
        if (mutateTopMeasure)
            mutationStart = 0;
        
        for (int popCounter = mutationStart; popCounter < population.size(); ++popCounter)
        {
            SimulationType p = population.get(popCounter).get(0);
            ArrayList<parameter> pArray = p.getListOfParameters();
            for (int i = 0; i<pArray.size(); ++i)
            {
                if (pArray.get(i).isIteratableAndIntern())
                {
                    if (randomGenerator.nextDouble() <= mutationProbability)
                    {
                        pArray.set(i, mutate(pArray.get(i)));
                    }   
                }
            }
            pArray = roundToStepping(pArray);
            p.setListOfParameters(pArray);
        }      
        return population;
    }
    
    private parameter mutate(parameter genToMutate)
    {
        double newValue = randomGenerator.nextDouble() * (genToMutate.getEndValue() - genToMutate.getStartValue()) + genToMutate.getStartValue();
        genToMutate.setValue(newValue);
                
        return genToMutate;
    }
       
    /**
     * cuts back population to population-size. It will dismiss all parsers with higher distance than the one in position of population size
     * @param oldPopulation the old population to cut
     * @return the new population, which is cutted to max population size 
     */
    private ArrayList< ArrayList<SimulationType>> cutPopulation(ArrayList< ArrayList<SimulationType>> oldPopulation)
    {
        if (oldPopulation == null)
            return null;
        
        ArrayList< ArrayList<SimulationType>> newPopulation = oldPopulation; //makes ref on both, only for better understanding of the code
        
        newPopulation = sortPopulation(newPopulation);
        
        int oldPopulationSize = oldPopulation.size();
        
        for (int i = 0; i < (oldPopulationSize - populationSize); ++i)
        {
            newPopulation.remove(populationSize); //keep removing first entry over population maximum
        }
               
        return newPopulation; 
    } 
    /**
     * Set the logfilename
     * this is useful for multi-optimization or if you like specific names for your logfiles
     * @param name Name (path) of logfile
     */
    public void setLogFileName(String name){
    this.logFileName=name;
    }
    /**
     * Returns the used logfileName
     * @return name of logfile
     */
    public String getLogFileName() {
    return this.logFileName;
    }
}
