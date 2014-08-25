/*
 * Optimization-Algorithm implemented bei Andy Seidel during Diploma Thesis 2014
 *
 * Original paper [07KB]:
 * Karaboga, Basturk:
 * A powerful and efficient algorithm for numerical
 * function optimization: artificial bee colony (ABC)
 * algorithm (2007)
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
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.support;

/**
 *
 * @author A. Seidel
 */
public class OptimizerABC implements Runnable, Optimizer
{
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
    
    private ArrayList<ArrayList<SimulationType>> foodSources = new ArrayList<ArrayList<SimulationType>>();
    private ArrayList<Integer> updateCyclesWithoutImprovementList = new ArrayList<Integer>();
    
    private int numEmployedBees = 10;
    private int numOnlookerBees = 10;
    private int numScoutBees = 1;
    
    private int maxNumberOfOptiCycles = 100; //maximum number of cycles, before optimization terminates
    private int maxNumberOfOptiCyclesWithoutImprovement = 10; //how many cycles without improvement until break optimization loop
    private int maxNumberOfFoodUpdateCyclesWithoutImprovement = 3;
    
    private int currentNumberOfOptiCyclesWithoutImprovement = 0;
    
    private Random randomGenerator;
    
    private SimulationType topMeasure;//temp top measure before implementing top-List
    private double topDistance = Double.POSITIVE_INFINITY;//temp top distance
    
    public OptimizerABC()
    {
        logFileName = support.getTmpPath() + File.separator+"Optimizing_with_ABC_" + Calendar.getInstance().getTimeInMillis() + "_ALL" + ".csv";
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
        
        //Ask for Tmp-Path
        this.tmpPath=support.getTmpPath();
        
        //Start this Thread
        randomGenerator = new Random();
        new Thread(this).start();
        
    }
    
    private ArrayList<ArrayList<SimulationType>> createRandomFoodSources(int numFoodSources, boolean ignoreStepping)
    {
        ArrayList<ArrayList<SimulationType>> newFoodSources = new ArrayList<ArrayList<SimulationType>>();
        
        for (int i=0; i<numFoodSources; ++i)
        {
            newFoodSources.add( new ArrayList<SimulationType>());
            SimulationType newSim = new SimulationType();
            
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
            
            newSim.setListOfParameters(pArray);
            newFoodSources.get(i).add(newSim);
        }
        //reset updateCycles without improvement for every foodSource
        for (int i = 0; i<newFoodSources.size(); ++i)
        {
            updateCyclesWithoutImprovementList.add(0);
        }
        
        return newFoodSources;
    }
    
    private ArrayList<ArrayList<SimulationType>> employedBeePhase(ArrayList<ArrayList<SimulationType>> foodSources, boolean ignoreStepping)
    {
        for (ArrayList<SimulationType> source : foodSources)
        {
            source.add(getNewFoodSource(source.get(0), ignoreStepping));
        }
        
        return foodSources;
    }
    
    private ArrayList<ArrayList<SimulationType>> onlookerBeePhase(ArrayList<ArrayList<SimulationType>> foodSources, int numOnlookerBees, boolean ignoreStepping)
    {
        foodSources = sortFoodSources(foodSources);
        double distanceSum = getDistanceSum(foodSources);
        
        for (ArrayList<SimulationType> source : foodSources)
        {
            double sourceQuality = 1 / source.get(0).getDistance();
            int numOnlookersOnSource = (int)(numOnlookerBees * (sourceQuality / distanceSum)) + 1;
            for (int i = 0; i<numOnlookerBees; ++i)
            {
                source.add(getNewFoodSource(source.get(0), ignoreStepping));    
            }            
        }        
        return foodSources;
    }
    
    private ArrayList<ArrayList<SimulationType>> scoutBeePhase(ArrayList<ArrayList<SimulationType>> foodSources, boolean ignoreStepping)
    {
        for (int i = 0; i < numScoutBees; ++i)
        {
            
        }
        return foodSources;
    }
    
 
     /**
     * Sorts the food sources by rank
     * @param foodSources  ArrayList of food sources to be sorted
     * @return 
     */  
    private ArrayList<ArrayList<SimulationType>> sortFoodSources(ArrayList<ArrayList<SimulationType>> foodSources)
    {
        Collections.sort(foodSources, new Comparator<ArrayList<SimulationType>>()
        {
            @Override
            public int compare(ArrayList<SimulationType> a, ArrayList<SimulationType> b) 
            {
                return Double.compare(a.get(0).getDistance(), b.get(0).getDistance());
            }                    
        });
        return foodSources;
    }
    
    /**
     * Sorts the food sources by rank, also filters neighbourhood of single source to get the best one
     * @param foodSources  ArrayList of food sources to be sorted
     * @return 
     */    
    private ArrayList<ArrayList<SimulationType>> sortAndFilterFoodSources(ArrayList<ArrayList<SimulationType>> foodSources)
    {
        foodSources = filterBestSolutionInNeighbourhood(foodSources);
        foodSources = sortFoodSources(foodSources);       
        return foodSources;
    }
    
    private ArrayList<ArrayList<SimulationType>> filterBestSolutionInNeighbourhood(ArrayList<ArrayList<SimulationType>> foodSources)
    {
        ArrayList<ArrayList<SimulationType>> newFoodList = new ArrayList<ArrayList<SimulationType>>();
        for (ArrayList<SimulationType> neighbourhood : foodSources)
        {
            setMeasureTargets(neighbourhood);
            Collections.sort(neighbourhood, new Comparator<SimulationType>()
            {
                @Override
                public int compare(SimulationType a, SimulationType b) 
                {
                    return Double.compare(a.getDistance(), b.getDistance());
                }                    
            });
            ArrayList<SimulationType> topSource = new ArrayList<SimulationType>();
            topSource.add(neighbourhood.get(0));
            newFoodList.add(topSource);
        }
        return newFoodList;
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
    
    /**
     * 
     * @param p array of parameters to be rounded
     * @return 
     */    
    private ArrayList<parameter> roundToStepping(ArrayList<parameter> p)
    {
        double currentValue = 0;
        double currentStepping = 0;
        for (int i=0; i<p.size(); ++i)
        {
            currentValue = p.get(i).getValue();
            currentStepping = p.get(i).getStepping();
            
            currentValue = Math.round(currentValue / currentStepping) * currentStepping;
            
            if (currentValue < p.get(i).getStartValue())
            {
                currentValue = p.get(i).getStartValue();
            }
            else if (currentValue > p.get(i).getEndValue())
            {
                currentValue = p.get(i).getEndValue();
            }
            
            
            p.get(i).setValue(currentValue);
        }
        return p;
    }
    
    private void updateTopMeasure()
    {
        boolean newTopMeasurefound = false;
        for (int i=0; i<foodSources.size(); ++i)
        {
            if(foodSources.get(i).get(0).getDistance()<topDistance)
            {
                topDistance = foodSources.get(i).get(0).getDistance();
                topMeasure = new SimulationType(foodSources.get(i).get(0));
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

    public void run()
    {
        int optiCycleCounter=0;
        foodSources = createRandomFoodSources(numEmployedBees, false);
        
        Simulator mySimulator = SimOptiFactory.getSimulator();       
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), optiCycleCounter, false);
        support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
        int simulationCounter = 0;
        
        ArrayList<SimulationType> simulationResults = mySimulator.getListOfCompletedSimulationParsers();
        foodSources = getFoodSourcesFromSimulationResults(simulationResults);
        
        while(optiCycleCounter < this.maxNumberOfOptiCycles)
        {
            if (currentNumberOfOptiCyclesWithoutImprovement >= maxNumberOfOptiCyclesWithoutImprovement)
            {
                support.log("Too many optimization cycles without improvement. Ending optimization.");
                break;
            }
            
            foodSources = employedBeePhase(foodSources, false);
            foodSources = onlookerBeePhase(foodSources, numOnlookerBees, false);
            foodSources = scoutBeePhase(foodSources, false);
            
            for (ArrayList<SimulationType> source : foodSources)
            {
                mySimulator.initSimulator(getNextParameterSetAsArrayList(source), optiCycleCounter, false);
                support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
                source = mySimulator.getListOfCompletedSimulationParsers();
                support.addLinesToLogFileFromListOfParser(simulationResults, logFileName);
            }
                
            foodSources = sortAndFilterFoodSources(foodSources);
            
            //cut back num foodSources, eliminates the bad solutions 
            int numFoodSourcesToCut = foodSources.size() - numEmployedBees; 
            for (int i = 0; i<numFoodSourcesToCut; ++i)
            {
                foodSources.remove(foodSources.size()-1); //cuts the last one
            }
            
            printfoodSourceDistances();
            updateTopMeasure();
            ++optiCycleCounter;
            ++simulationCounter;
        }
    }
    
    public SimulationType getOptimum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private ArrayList< ArrayList<parameter> > getNextParameterSetAsArrayList()
    {
        ArrayList< ArrayList<parameter> > myParametersetList = new ArrayList< ArrayList<parameter> >();
        for (ArrayList<SimulationType> p : foodSources)
        {
            ArrayList<parameter> pArray = p.get(0).getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }
    
    private ArrayList< ArrayList<parameter> > getNextParameterSetAsArrayList(ArrayList<SimulationType> simulationData)
    {
        ArrayList< ArrayList<parameter> > myParametersetList = new ArrayList< ArrayList<parameter> >();        
        for (SimulationType simulation : simulationData)
        {
            ArrayList<parameter> pArray = simulation.getListOfParameters();
            myParametersetList.add(pArray);
        }        
        return myParametersetList;
    }
    
    private ArrayList< ArrayList<SimulationType> > getFoodSourcesFromSimulationResults(ArrayList<SimulationType> results)
    {
        ArrayList< ArrayList<SimulationType> > food = new ArrayList<ArrayList<SimulationType>>();
        
        for (SimulationType result : results)
        {
            ArrayList<SimulationType> newFood = new ArrayList<SimulationType>();
            newFood.add(result);
            food.add(newFood);
        }        
        return food;
    }

    private SimulationType getNewFoodSource(SimulationType originalSource, boolean ignoreStepping) 
    {       
        //get new radom reference food source
        int refFoodSourceNumber = randomGenerator.nextInt(foodSources.size());
        int paramaterNumberToModify = randomGenerator.nextInt(originalSource.getListOfParameters().size());
        SimulationType refFoodSource = foodSources.get(refFoodSourceNumber).get(0);
        
        ArrayList<parameter> originalParameterSet = originalSource.getListOfParameters();
        double originalParameterValue = originalSource.getListOfParameters().get(paramaterNumberToModify).getValue();
        double refParameterValue = refFoodSource.getListOfParameters().get(paramaterNumberToModify).getValue();        
        double scalingFactor = 2 * randomGenerator.nextDouble() - 1;
        double newParameterValue = originalParameterValue + scalingFactor * (originalParameterValue - refParameterValue);
        
        originalParameterSet.get(paramaterNumberToModify).setValue(newParameterValue);
        originalParameterSet = roundToStepping(originalParameterSet);
        originalSource.setListOfParameters(originalParameterSet);
             
        return originalSource;
    }
    
    private double getDistanceSum(ArrayList< ArrayList<SimulationType> > foodSources)
    {
        double distanceSum = 0;
        
        for (ArrayList<SimulationType> foodSource : foodSources)
        {
            distanceSum += 1 / foodSource.get(0).getDistance(); //to get big weight to small distances
        }               
        return distanceSum;
    }
    
    public void printfoodSourceDistances()
    {
        for (int i = 0; i<foodSources.size(); ++i)
        {
            support.setLogToConsole(true);
            String logString = "Distance " + i + " \t: " + foodSources.get(i).get(0).getDistance();
            support.log(logString);
        }
    }

}
