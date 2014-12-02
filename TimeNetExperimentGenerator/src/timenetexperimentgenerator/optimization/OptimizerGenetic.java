/*
 * Optimization-Algorithm implemented bei Andy Seidel during Diploma Thesis 2014
 *
 */

package timenetexperimentgenerator.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import timenetexperimentgenerator.SimOptiFactory;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.typedef.typeOfGeneticCrossover;

/**
 *
 * @author A. Seidel
 */

public class OptimizerGenetic extends OptimizerPopulationBased implements Runnable, Optimizer
{
    private int populationSize = support.getOptimizerPreferences().getPref_GeneticPopulationSize(); //size of population after selection-phase
    private double mutationChance = support.getOptimizerPreferences().getPref_GeneticMutationChance(); // chance of genes to Mutate
    private boolean mutateTopMeasure = support.getOptimizerPreferences().getPref_GeneticMutateTopSolution();
    private typeOfGeneticCrossover crossOverStrategy = typeOfGeneticCrossover.MPC;
    private int SBX_n = 2;
    private double MPC_cr = 0.5;
    
    private int numOptiRuns = 50;

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
        ArrayList<SimulationType> optiResults = new ArrayList<SimulationType>();
        ArrayList<Integer> optiTotalSimualtions = new ArrayList<Integer>();
        ArrayList<Integer> optiTotalCachedSimualtions = new ArrayList<Integer>();
        for (int i = 0; i< numOptiRuns; ++i)
        {
        int optiCycleCounter = 0;
        int totalSimulations  = 0;
        int totalCachedSimualtions = 0;
        population = createRandomPopulation(populationSize, false);
        
        Simulator mySimulator = SimOptiFactory.getSimulator();       
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), optiCycleCounter, false);
        support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
        
        ArrayList<SimulationType> simulationResults = mySimulator.getListOfCompletedSimulationParsers();
        for (int r = 0;r<simulationResults.size();++r)
        {
            if (simulationResults.get(0).isIsFromCache())
            {
                ++totalCachedSimualtions;
            }
        }
        
        totalSimulations += simulationResults.size();
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
            totalSimulations += simulationResults.size();
            population = getPopulationFromSimulationResults(simulationResults);
            //support.addLinesToLogFileFromListOfParser(simulationResults, logFileName);
            
            //evaluation phase --------------------------------------------------------------------------
            population = cutPopulation(population);
            updateTopMeasure();
            printPopulationDistances();
            
            ++optiCycleCounter;
        }
        optiResults.add(topMeasure);
        optiTotalSimualtions.add(totalSimulations);
        optiTotalCachedSimualtions.add(totalCachedSimualtions);
        topDistance = Double.POSITIVE_INFINITY;
        currentNumberOfOptiCyclesWithoutImprovement = 0;
        }
        support.addLinesToLogFileFromListOfParser(optiResults, logFileName);
        /*
        String headline = "";
        for (int i = 0; i<parameterBase.size();++i)
        {
            
        }
        */
        for (int i = 0;i<optiResults.size(); ++i)
        {
            String logString = "" + optiTotalSimualtions.get(i) + " " + optiTotalCachedSimualtions.get(i);
            support.log(logString);
        }
        
        
    }
    
    /**
     * creates genetic crossover of mothers and fathers paramter[] to create new child1
     * @param father the father for the genetic parameter mix process
     * @param mother the mother for the genetic parameter mix process
     * @return the resulting child1
     */
    private ArrayList<SimulationType> onePointCrossOver(SimulationType father, SimulationType mother)
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
        SimulationType child1 = new SimulationType(father);
        SimulationType child2 = new SimulationType(mother);
        ArrayList<parameter> fatherDNA = father.getListOfParameters();
        ArrayList<parameter> motherDNA = mother.getListOfParameters();
        ArrayList<parameter> child1DNA = child1.getListOfParameters();
        ArrayList<parameter> child2DNA = child1.getListOfParameters();
             
        //find random crossover-point
        int crossingPoint = randomGenerator.nextInt(father.getListOfParameters().size());
        
        //copy paramters from mother after crossing Point, father-DNA is already included through copy-constructor
        for (int i = crossingPoint; i<fatherDNA.size(); ++i)
        {
            parameter parameterFromMother = null;
            parameter parameterFromFather = null;
            try
            {
                parameterFromMother = (parameter)motherDNA.get(i).clone();
                parameterFromFather = (parameter)fatherDNA.get(i).clone();
            }
            catch (CloneNotSupportedException e)
            {
                support.log(e.getMessage());
            }
            if (parameterFromMother!=null && parameterFromFather !=null) //clone was successful
            {
                child1DNA.set(i, parameterFromMother);
                child2DNA.set(i, parameterFromFather);
            }
        }
        child1.setListOfParameters(child1DNA);
        child2.setListOfParameters(child2DNA);
        
        ArrayList<SimulationType> childList = new ArrayList<SimulationType>();
        childList.add(child1);
        childList.add(child2);
        return childList;
    }
        
    private ArrayList<SimulationType> SBXCrossOver(SimulationType father, SimulationType mother)
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
        SimulationType child1 = new SimulationType(father);
        SimulationType child2 = new SimulationType(father);
        ArrayList<parameter> fatherDNA = father.getListOfParameters();
        ArrayList<parameter> motherDNA = mother.getListOfParameters();
        ArrayList<parameter> child1DNA = child1.getListOfParameters();
        ArrayList<parameter> child2DNA = child2.getListOfParameters();
        
        double beta = 0;
        double u = randomGenerator.nextDouble();
        if (u<=0.5)
        {
            beta = Math.pow(2*u, 1.0/(SBX_n+1.0));
        }
        else
        {
            beta = Math.pow(1/(2-2*u), 1.0/(SBX_n+1.0));
        }
        
        for (int i = 0; i<fatherDNA.size(); ++i)
        {
            parameter p1 = null;
            parameter p2 = null;
            parameter c1 = null;
            parameter c2 = null;
            
            try
            {
                p1 = (parameter)motherDNA.get(i).clone();
                p2 = (parameter)fatherDNA.get(i).clone();
                c1 = (parameter)motherDNA.get(i).clone();
                c2 = (parameter)motherDNA.get(i).clone();
            }
            catch (CloneNotSupportedException e)
            {
                support.log(e.getMessage());
            }
            

            if (p1!=null && p2!=null && c1!=null && c2!= null) //clone was successful
            {
                c1.setValue(beta * Math.abs(p1.getValue()-p2.getValue()) + p1.getValue() - p2.getValue());
                c2.setValue(beta * Math.abs(p1.getValue()-p2.getValue()) - p1.getValue() + p2.getValue());    
                child1DNA.set(i, c1);
                child2DNA.set(i, c2);
            }
        }

        ArrayList<SimulationType> childList = new ArrayList<SimulationType>();
        childList.add(child1);
        childList.add(child2);
        return childList;
    }
    
    private ArrayList<SimulationType> MPCCrossOver(SimulationType parent1, SimulationType parent2, SimulationType parent3)
    {
        if (parent1 == null || parent2 == null || parent3 == null)
        {
            return null;
        }
        
        if (parent1.getListOfParameters().size() != parent2.getListOfParameters().size() || parent1.getListOfParameters().size() != parent3.getListOfParameters().size())
        {
            support.log("Parameter length different. No Crossover Possible");
            return null;
        }
        
        SimulationType child1 = new SimulationType(parent1);
        SimulationType child2 = new SimulationType(parent2);
        SimulationType child3 = new SimulationType(parent3);
        ArrayList<parameter> parent1DNA = parent1.getListOfParameters();
        ArrayList<parameter> parent2DNA = parent2.getListOfParameters();
        ArrayList<parameter> parent3DNA = parent3.getListOfParameters();
        ArrayList<parameter> child1DNA = child1.getListOfParameters();
        ArrayList<parameter> child2DNA = child2.getListOfParameters();
        ArrayList<parameter> child3DNA = child3.getListOfParameters();
        
        double beta = randomGenerator.nextGaussian();
        
        for (int i = 0; i<parent1DNA.size(); ++i)
        {
            parameter p1 = null;
            parameter p2 = null;
            parameter p3 = null;
            parameter c1 = null;
            parameter c2 = null;
            parameter c3 = null;
            
            try
            {
                p1 = (parameter)parent1DNA.get(i).clone();
                p2 = (parameter)parent2DNA.get(i).clone();
                p3 = (parameter)parent3DNA.get(i).clone();
                c1 = (parameter)child1DNA.get(i).clone();
                c2 = (parameter)child2DNA.get(i).clone();
                c3 = (parameter)child3DNA.get(i).clone();
            }
            catch (CloneNotSupportedException e)
            {
                support.log(e.getMessage());
            }
            

            if (p1!=null && p2!=null && p3!=null && c1!=null && c2!= null && c3!= null) //clone was successful
            {
                c1.setValue(p1.getValue() + beta * (p2.getValue()-p3.getValue()));
                c2.setValue(p2.getValue() + beta * (p3.getValue()-p1.getValue()));
                c3.setValue(p3.getValue() + beta * (p1.getValue()-p2.getValue()));
                child1DNA.set(i, c1);
                child2DNA.set(i, c2);
                child3DNA.set(i, c3);
            }
        }
        
        
        ArrayList<SimulationType> childList = new ArrayList<SimulationType>();
        childList.add(child1);
        childList.add(child2);
        childList.add(child3);
        return childList;
    }
    
    private ArrayList< ArrayList<SimulationType> > crossPopulation(ArrayList< ArrayList<SimulationType> > population, int numNewChildren)
    {
            ArrayList<SimulationType> childs = new ArrayList<SimulationType>();
            if (this.crossOverStrategy == typeOfGeneticCrossover.OnePoint)
            {
                for (int i = 0; i<numNewChildren; ++i)
                {     
                    int indexOfFather = randomGenerator.nextInt(populationSize);
                    int indexOfMother = randomGenerator.nextInt(populationSize);
                
                    childs = onePointCrossOver(population.get(indexOfFather).get(0), population.get(indexOfMother).get(0));
                }
            }
            else if (this.crossOverStrategy == typeOfGeneticCrossover.SBX)
            {
                for (int i = 0; i<numNewChildren; ++i)
                {     
                    int indexOfFather = randomGenerator.nextInt(populationSize);
                    int indexOfMother = randomGenerator.nextInt(populationSize);
                
                    childs = SBXCrossOver(population.get(indexOfFather).get(0), population.get(indexOfMother).get(0)); 
                }                
            }
            else if (this.crossOverStrategy == typeOfGeneticCrossover.MPC)
            {
                for (int i = 0; i<population.size(); i+=3)
                {     
                    if (randomGenerator.nextDouble() <= MPC_cr && population.size() > i+2)
                    {
                        childs = MPCCrossOver(population.get(i).get(0), population.get(i+1).get(0), population.get(i+2).get(0));    
                    }
                }      
            }
            else
            {
                //TODO default handling
            }
            
            for (SimulationType child : childs)
            {
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
                        pArray.set(i, mutateRandom(pArray.get(i)));
                    }   
                }
            }
            pArray = roundToStepping(pArray);
            p.setListOfParameters(pArray);
        }      
        return population;
    }
    
    private parameter mutateRandom(parameter genToMutate)
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
