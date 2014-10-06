/*
 * Optimization-Algorithm implemented bei Andy Seidel during Diploma Thesis 2014
 *
 * Mean-Variance-Mapping-Optimization
 * Original Paper: Erlich, Ganesh, Worawat ; 2010:
 * A Mean-Variance Optimization Algorithm
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
import timenetexperimentgenerator.typedef;
import timenetexperimentgenerator.typedef.typeOfMVMOMutationSelection;
import timenetexperimentgenerator.typedef.typeOfMVMOParentSelection;

/**
 *
 * @author A. Seidel
 */

public class OptimizerMVMO extends OptimizerPopulationBased implements Runnable, Optimizer
{
    private int populationSize = support.getOptimizerPreferences().getPref_GeneticPopulationSize(); //size of population after selection-phase
    private boolean mutateTopMeasure = support.getOptimizerPreferences().getPref_GeneticMutateTopSolution();
    
    private ArrayList<Double> parameterMeanValues;
    private ArrayList<Double> parameterVarianceValues;
    
    private double scalingFactor = 10.0;
    private double asymmetryFactor = 3.0;
    
    private double sd = 75.0;

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
    
            

    public OptimizerMVMO()
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
        int lastGenSelected = 0;
        int numGenesToSelect = 0;
        while(optiCycleCounter < this.maxNumberOfOptiCycles)
        {
            if (currentNumberOfOptiCyclesWithoutImprovement >= maxNumberOfOptiCyclesWithoutImprovement)
            {
                support.log("Too many optimization cycles without improvement. Ending optimization.");
                break;
            }
            
            //modification phase -----------------------------------------------------------------------
            population = sortPopulation(population); //sort them, so the best one is number one
            population = normalize(population);
            parameterMeanValues = getMeanValues(population);
            parameterVarianceValues = getVarianceValues(population);
            SimulationType candidate = createCandidate(
                    population,
                    parameterMeanValues,
                    parameterVarianceValues,
                    typeOfMVMOParentSelection.Best,
                    typeOfMVMOMutationSelection.Random,
                    lastGenSelected, numGenesToSelect);
            
            
            //simulation phase -----------------------------------------------------------------------
            population = denormalize(population);
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
    
    
    
    private ArrayList<Double> getMeanValues(ArrayList<ArrayList<SimulationType>> population)
    {
        ArrayList<Double> meanValues = new ArrayList<Double>();
        int numParameters = population.get(0).get(0).getListOfParameters().size();
        for (int parameterIndex = 0; parameterIndex<numParameters; ++parameterIndex)
        {
            double sum = 0;
            for (int populationIndex = 0; populationIndex<population.size(); ++populationIndex)
            {
                sum += population.get(populationIndex).get(0).getListOfParameters().get(parameterIndex).getValue();
            }
            meanValues.add(sum / population.size());            
        }        
        return meanValues;
    }
    
    private ArrayList<Double> getVarianceValues(ArrayList<ArrayList<SimulationType>> population)
    {
        ArrayList<Double> varianceValues = new ArrayList<Double>();
                int numParameters = population.get(0).get(0).getListOfParameters().size();
        for (int parameterIndex = 0; parameterIndex<numParameters; ++parameterIndex)
        {
            double sum = 0;
            for (int populationIndex = 0; populationIndex<population.size(); ++populationIndex)
            {
                double value = population.get(populationIndex).get(0).getListOfParameters().get(parameterIndex).getValue();
                sum += (value - parameterMeanValues.get(parameterIndex))*(value - parameterMeanValues.get(parameterIndex));
            }
            varianceValues.add(sum / population.size());            
        }  
        return varianceValues;
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
    
    private ArrayList< ArrayList<SimulationType> > mutatePopulation(
            ArrayList< ArrayList<SimulationType> > population,
            typedef.typeOfMVMOMutationSelection mutationStratety)
    {
        int mutationStart = 1;
        if (mutateTopMeasure)
            mutationStart = 0;
        
        for (int popCounter = mutationStart; popCounter< population.size(); ++popCounter)
        {
            SimulationType topMeasure = population.get(popCounter).get(0);
            ArrayList<parameter> pArray = topMeasure.getListOfParameters();
            for (int i = 0; i<pArray.size(); ++i)
            {
                if (pArray.get(i).isIteratableAndIntern())
                {
//                    if (randomGenerator.nextDouble() <= mutationProbability)
//                    {
//                        pArray.set(i, mutate(pArray.get(i)));
//                    }   
                }
            }
            pArray = roundToStepping(pArray);
            topMeasure.setListOfParameters(pArray);
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

    private SimulationType createCandidate(
            ArrayList<ArrayList<SimulationType>> population,
            ArrayList<Double> parameterMeanValues,
            ArrayList<Double> parameterVarianceValues,
            typeOfMVMOParentSelection parentSelection,
            typeOfMVMOMutationSelection mutationSelection,
            int startingIndex,
            int numGenesToMutate)
    {
        SimulationType candidate = new SimulationType();
        //parent selection
        if (parentSelection == typeOfMVMOParentSelection.Best)
        {
            candidate = new SimulationType(population.get(0).get(0));    
        }
        
        //select genes to mutate
        ArrayList<Integer> genesToMutate = new ArrayList<Integer>();
        if (mutationSelection == typeOfMVMOMutationSelection.Random)
        {
            genesToMutate = fillWithRandomIndices(genesToMutate, numGenesToMutate);
        }
        else if (mutationSelection == typeOfMVMOMutationSelection.RandomWithMovingSingle)
        {
            genesToMutate.add(startingIndex);
            genesToMutate = fillWithRandomIndices(genesToMutate, numGenesToMutate);
        }
        else if (mutationSelection == typeOfMVMOMutationSelection.MovingGroupSingleStep
              || mutationSelection == typeOfMVMOMutationSelection.MovingGroupMultiStep )
        {
            for (int i = 0; i<numGenesToMutate; ++i)
            {
                genesToMutate.add((startingIndex + i) % population.size());
            }
        }
        
        //mutation
        double si = 0;
        double si1 = 0;
        double si2 = 0;
        double mean = 0;
        double variance = 0;
        double kd = 0.0505 / candidate.getListOfParameters().size() + 1;
        for (int i=0; i<genesToMutate.size(); ++i)
        {
            parameter p = candidate.getListOfParameters().get(genesToMutate.get(i));
            mean = parameterMeanValues.get(genesToMutate.get(i));
            variance = parameterVarianceValues.get(genesToMutate.get(i));
            si = -1.0 * Math.log(variance) * scalingFactor;
            if (variance == 0)
            {
                if (si < sd)
                {
                    sd = sd * kd;
                }
                else if (si > sd)
                {
                    sd = sd / kd;
                }
                si = sd;
            }
            
            if (p.getValue() < mean)
            {
                si1 = si;
                si2 = si * asymmetryFactor;
            }
            else if (p.getValue() > mean)
            {
                si1 = si * asymmetryFactor;
                si2 = si;
            }
            else
            {
                si1 = si;
                si2 = si;
            }
            

            double xTemp = randomGenerator.nextDouble();
            double x = transform(mean, si1, si2, xTemp) +
                (1 - transform(mean, si1, si2, 1) - transform(mean, si1, si2, 0)) * xTemp -
                transform(mean, si1, si2, 0);
            p.setValue(x);
        }
              
        return candidate;
    }
    
    private double transform(double x_mean, double si1, double si2, double ui)
    {
        double r = 0;
        r = x_mean * (1 - Math.exp(-1.0 * si1 * ui)) + (1 - x_mean) * Math.exp((1 - ui) * si2);
        return r;
    }
    
    private ArrayList<Integer> fillWithRandomIndices(
        ArrayList<Integer> list, int targetSize)
    {
        while (list.size() < targetSize)
        {
            int selectedGen = randomGenerator.nextInt(population.size());
            if (!list.contains(selectedGen))
            {
                list.add(selectedGen);
            }
        }
        return list;
    }
    
    private ArrayList< ArrayList<SimulationType>> normalize(ArrayList< ArrayList<SimulationType>> list)
    {
        for (int i = 0; i<list.size(); ++i)
        {
            SimulationType simulation = list.get(i).get(0);
            ArrayList<parameter> paraList = simulation.getListOfParameters();
            for (int paraNum = 0; paraNum < paraList.size(); ++paraNum)
            {
                parameter p = paraList.get(paraNum);
                double newValue = (p.getValue() - p.getStartValue()) / (p.getEndValue() - p.getStartValue());
                p.setValue(newValue);
            }
        }
        return list;
    }
    
    private ArrayList< ArrayList<SimulationType>> denormalize(ArrayList< ArrayList<SimulationType>> list)
    {
        for (int i = 0; i<list.size(); ++i)
        {
            //SimulationType simulation = list.get(i).get(0);
            //simulation = denormalize(simulation);
            denormalize(list.get(i).get(0));
        }
        return list;
    }
    
    private SimulationType denormalize(SimulationType simulation)
    {
        ArrayList<parameter> paraList = simulation.getListOfParameters();
        for (int paraNum = 0; paraNum < paraList.size(); ++paraNum)
        {
            parameter p = paraList.get(paraNum);
            double newValue = p.getValue() * (p.getEndValue() - p.getStartValue()) + p.getStartValue();
            p.setValue(newValue);
        }
        return simulation;
    }
}
