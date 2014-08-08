/*
 * Optimization-Algorithm implemented bei Andy Seidel during Diploma Thesis 2014
 *
 */

package timenetexperimentgenerator.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import timenetexperimentgenerator.MainFrame;
import timenetexperimentgenerator.SimOptiFactory;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.support;

/**
 *
 * @author A. Seidel
 */

public class OptimizerGenetic implements Runnable, Optimizer{

    private String tmpPath = "";
    private String filename = "";//Original filename
    private String pathToTimeNet = "";
    private String logFileName = "";
    private MainFrame parent = null;
    private JTabbedPane MeasureFormPane;
    private ArrayList<MeasureType> listOfMeasures = new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
    private ArrayList<parameter> parameterBase;//Base set of parameters, start/end-value, stepping, etc.
    private JLabel infoLabel;
    private double simulationTimeSum = 0;
    private double cpuTimeSum = 0;
    
    private int populationSize = 10; //size of population after selection-phase
    private int maxNumberOfOptiCycles = 100; //maximum number of cycles, before optimization terminates
    private int maxNumberOfOptiCyclesWithoutImprovement = 10; //how many cycles without improvement until break optimization loop
    private int currentNumberOfOptiCyclesWithoutImprovement = 0;
    
    private double mutationChance = 0.2; // chance of genes to Mutate
    private boolean mutateTopMeasure = false;
    
    private ArrayList<SimulationType> population;
    private SimulationType bestKnownSolution = null;
    
    Random randomGenerator;

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
    
    public void initOptimizer()
    {
        this.infoLabel=support.getStatusLabel();//  infoLabel;
        this.pathToTimeNet=support.getPathToTimeNet();// pathToTimeNetTMP;
        this.MeasureFormPane=support.getMeasureFormPane();//MeasureFormPaneTMP;
        this.parent=support.getMainFrame();// parentTMP;
        this.parameterBase=parent.getParameterBase();
        this.listOfMeasures=parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
        support.log("# of Measures to be optimized: "+this.listOfMeasures.size());        
        this.filename=support.getOriginalFilename();// originalFilename;
        this.tmpPath=support.getTmpPath(); //Ask for Tmp-Path
        
        population = new ArrayList<SimulationType>();
        bestKnownSolution = new SimulationType();
        
        randomGenerator = new Random(System.currentTimeMillis());

        new Thread(this).start();//Start this Thread
    }

    public void run() 
    {
        int optiCycleCounter = 0;
        createNewRandomPopulation(populationSize, false);
        
        Simulator mySimulator = SimOptiFactory.getSimulator();       
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), optiCycleCounter, false);
        support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
        
        population = mySimulator.getListOfCompletedSimulationParsers();
        
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
            
            population = mySimulator.getListOfCompletedSimulationParsers();
            support.addLinesToLogFileFromListOfParser(population, logFileName);
            
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
    
    private ArrayList<SimulationType> crossPopulation(ArrayList<SimulationType> population, int numNewChildren)
    {
        for (int i = 0; i<numNewChildren; ++i)
        {
            int indexOfFather = randomGenerator.nextInt(populationSize);
            int indexOfMother = randomGenerator.nextInt(populationSize);
        
            SimulationType child = crossOver(population.get(indexOfFather), population.get(indexOfMother));
            population.add(child);   
        }        
        return population;
    }
    
    private ArrayList<SimulationType> mutatePopulation(ArrayList<SimulationType> population, double mutationProbability)
    {
        int mutationStart = 1;
        if (mutateTopMeasure)
            mutationStart = 0;
        
        for (int popCounter = mutationStart; popCounter< population.size(); ++popCounter)
        {
            SimulationType p = population.get(popCounter);
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
            p.setListOfParameters(pArray);
        }
        return population;
    }
    
    private SimulationType mutate(SimulationType mutant)
    {
        ArrayList<parameter> changeableGens = new ArrayList<parameter>();
        
        ArrayList<parameter> pArray = mutant.getListOfParameters();
        for (int i=0; i<pArray.size(); ++i)
        {
            if (pArray.get(i).isIteratableAndIntern())
            {
                changeableGens.add(pArray.get(i));
            }
        }
        
        int mutatedGen = randomGenerator.nextInt(changeableGens.size());
        
        return mutant;
    }
    
    private parameter mutate(parameter genToMutate)
    {
        double newValue = randomGenerator.nextDouble() * (genToMutate.getEndValue() - genToMutate.getStartValue()) + genToMutate.getStartValue();
        genToMutate.setValue(newValue);
        genToMutate = roundToStepping(genToMutate);
                
        return genToMutate;
    }
    
       /**
     * Creates random starting population
     * @param populationSize number of charges to be created
     * @param ignoreStepping set false to round created
     */
    private void createNewRandomPopulation(int populationSize, boolean ignoreStepping)
    {
        population = new ArrayList<SimulationType>();
       
        //fill population with random values
        for(int i=0; i<populationSize; ++i)
        {
            SimulationType p = new SimulationType();
            population.add(p);
            MeasureType newMeasure = new MeasureType();
            
            ArrayList<parameter> pArray = support.getCopyOfParameterSet(parameterBase);
            for (int j=0; j<pArray.size(); ++j)
            {
                //creates a random value between start and end value for each parameter
                double newValue = pArray.get(j).getStartValue() + Math.random() * (pArray.get(j).getEndValue() - pArray.get(j).getStartValue());
                pArray.get(j).setValue(newValue);
            }
            if(!ignoreStepping)
            {
                pArray = roundToStepping(pArray);
            }
            population.get(i).setListOfParameters(pArray);            
        }       
    }
    
    /**
     * cuts back population to population-size. It will dismiss all parsers with higher distance than the one in position of population size
     * @param oldPopulation the old population to cut
     * @return the new population, which is cutted to max population size 
     */
    private ArrayList<SimulationType> cutPopulation(ArrayList<SimulationType> oldPopulation)
    {
        if (oldPopulation == null)
            return null;
        
        ArrayList<SimulationType> newPopulation = oldPopulation; //makes ref on both, only for better understanding of the code
        
        newPopulation = sortPopulationByDistance(newPopulation);
        
        int oldPopulationSize = oldPopulation.size();
        
        for (int i = 0; i < (oldPopulationSize - populationSize); ++i)
        {
            newPopulation.remove(populationSize); //keep removing first entry over population maximum
        }
               
        return newPopulation; 
    }
    
    private parameter roundToStepping(parameter parameterToBeRounded)
    {
        double currentValue = parameterToBeRounded.getValue();
        double currentStepping = parameterToBeRounded.getStepping();
            
        currentValue = Math.round(currentValue / currentStepping) * currentStepping;
            
        if (currentValue < parameterToBeRounded.getStartValue())
        {
            currentValue = parameterToBeRounded.getStartValue();
        }
        else if (currentValue > parameterToBeRounded.getEndValue())
        {
            currentValue = parameterToBeRounded.getEndValue();
        }           
        parameterToBeRounded.setValue(currentValue);
        
        return parameterToBeRounded;
    }
    
    private ArrayList<parameter> roundToStepping(ArrayList<parameter> pArray)
    {
        for (int i = 0; i<pArray.size(); ++i)
        {
            pArray.set(i, roundToStepping(pArray.get(i)));
        }
        return pArray;
    }
    
    private ArrayList< ArrayList<parameter> > getNextParameterSetAsArrayList()
    {
        ArrayList< ArrayList<parameter> > myParametersetList = new ArrayList< ArrayList<parameter> >();
        for (SimulationType p : population)
        {
            ArrayList<parameter> pArray = p.getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }
    
    private ArrayList<SimulationType> sortPopulationByDistance(ArrayList<SimulationType> originalPopulation)
    {
        setMeasureTargets(originalPopulation);
        Collections.sort(originalPopulation, new Comparator<SimulationType>()
        {
            @Override
            public int compare(SimulationType a, SimulationType b) 
            {
                return Double.compare(a.getDistance(), b.getDistance());
            }                    
        });
                
        return originalPopulation;
    }
    
    private void updateTopMeasure()
    {
        boolean newTopMeasurefound = false;
        
        if (bestKnownSolution == null) //no top solution found until know, so take the next you can get
        {
            bestKnownSolution = new SimulationType(population.get(0));
        }
        
        for (SimulationType p : population)
        {
            if(p.getDistance() < bestKnownSolution.getDistance())
            {
                bestKnownSolution = new SimulationType(p);
                newTopMeasurefound = true;
            }
        }
        if (newTopMeasurefound)
        {
            currentNumberOfOptiCyclesWithoutImprovement = 0;
        }
        else
        {
            ++currentNumberOfOptiCyclesWithoutImprovement;
        }
    }
    
    private void setMeasureTargets(ArrayList<SimulationType> pList)
    {
        MeasureType activeMeasure = null;
        MeasureType activeMeasureFromInterface = null;
        for(int measureCount=0;measureCount<listOfMeasures.size();measureCount++)
        {
            for(int populationCount = 0; populationCount < pList.size() ; populationCount++)
            {
                activeMeasure=pList.get(populationCount).getMeasureByName(listOfMeasures.get(measureCount).getMeasureName());
                activeMeasureFromInterface=listOfMeasures.get(measureCount);//Contains Optimization targets
                activeMeasure.setTargetValue(activeMeasureFromInterface.getTargetValue(), activeMeasureFromInterface.getTargetKindOf());
            }
        }
    }
    
    public void printPopulationDistances()
    {
        for (int i = 0; i<population.size(); ++i)
        {
            support.setLogToConsole(true);
            String logString = "Distance " + i + " \t: " + population.get(i).getDistance();
            support.log(logString);
        }
    }

    public SimulationType getOptimum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
