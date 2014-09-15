/*
 * abstract class for population-based optimizers 
 * included because of a lot repeating code
 */

package timenetexperimentgenerator.optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import timenetexperimentgenerator.MainFrame;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.support;

/**
 *
 * @author Andy
 */
public abstract class OptimizerPopulationBased implements Runnable, Optimizer
{
    private String tmpPath = "";
    private String filename = "";//Original filename
    private String pathToTimeNet = "";
    private MainFrame parent = null;
    private JTabbedPane MeasureFormPane;
    protected ArrayList<MeasureType> listOfMeasures = new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
    protected ArrayList<parameter> parameterBase;//Base set of parameters, start/end-value, stepping, etc.
    private JLabel infoLabel;
    private double simulationTimeSum = 0;
    private double cpuTimeSum = 0;
    
    protected String logFileName = "";
    
    protected Random randomGenerator;
    
    protected ArrayList<Integer> parametersToModify = new ArrayList<Integer>();
    
    //ArrayList of ArrayList used, to make sublists (for ABC...) possible
    protected ArrayList<ArrayList<SimulationType>> population = new ArrayList<ArrayList<SimulationType>>();
    
    protected SimulationType topMeasure;//temp top measure before implementing top-List
    protected double topDistance = Double.POSITIVE_INFINITY;//temp top distance
    
    protected int maxNumberOfOptiCycles = 100; //maximum number of cycles, before optimization terminates
    protected int maxNumberOfOptiCyclesWithoutImprovement = 10; //how many cycles without improvement until break optimization loop
    protected int currentNumberOfOptiCyclesWithoutImprovement = 0;
    
    public OptimizerPopulationBased()
    {
        
    }
    
    public void initOptimizer() 
    {
        this.pathToTimeNet=support.getPathToTimeNet();// pathToTimeNetTMP;
        this.MeasureFormPane=support.getMeasureFormPane();//MeasureFormPaneTMP;
        this.parent=support.getMainFrame();// parentTMP;
        this.parameterBase=parent.getParameterBase();
        this.listOfMeasures=parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
        support.log("# of Measures to be optimized: "+this.listOfMeasures.size());
        
        this.filename=support.getOriginalFilename();// originalFilename;
        
        //Ask for Tmp-Path
        this.tmpPath=support.getTmpPath();
        
        randomGenerator = new Random();
        
        //check for iteratable parameters
        for (int i = 0; i<this.parameterBase.size(); ++i)
        {
            if (this.parameterBase.get(i).isIteratable())
            {
                parametersToModify.add(i);
            }
        }
        
        //Start this Thread
        new Thread(this).start();        
    }
    
    public SimulationType getOptimum()
    {
        return topMeasure;
    }
    
    protected ArrayList<ArrayList<SimulationType>> createRandomPopulation(int populationSize, boolean ignoreStepping)
    {
        ArrayList<ArrayList<SimulationType>> newPopulation = new ArrayList<ArrayList<SimulationType>>();
        
        for (int i=0; i<populationSize; ++i)
        {
            newPopulation.add( new ArrayList<SimulationType>());
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
            newPopulation.get(i).add(newSim);
        }

        
        return newPopulation;
    }
    
    protected void setMeasureTargets(ArrayList<SimulationType> pList)
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
    
    protected ArrayList< ArrayList<parameter> > getNextParameterSetAsArrayList()
    {
        ArrayList< ArrayList<parameter> > myParametersetList = new ArrayList< ArrayList<parameter> >();
        for (ArrayList<SimulationType> p : population)
        {
            ArrayList<parameter> pArray = p.get(0).getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }
    
    protected ArrayList< ArrayList<parameter> > getNextParameterSetAsArrayList(ArrayList<SimulationType> simulationData)
    {
        ArrayList< ArrayList<parameter> > myParametersetList = new ArrayList< ArrayList<parameter> >();        
        for (SimulationType simulation : simulationData)
        {
            ArrayList<parameter> pArray = simulation.getListOfParameters();
            myParametersetList.add(pArray);
        }        
        return myParametersetList;
    }
    
    protected ArrayList< ArrayList<SimulationType> > getPopulationFromSimulationResults(ArrayList<SimulationType> results)
    {
        ArrayList< ArrayList<SimulationType> > newPopulation = new ArrayList<ArrayList<SimulationType>>();
        
        for (SimulationType result : results)
        {
            ArrayList<SimulationType> newIndividual = new ArrayList<SimulationType>();
            newIndividual.add(result);
            newPopulation.add(newIndividual);
        }        
        return newPopulation;
    }
    
     /**
     * 
     * @param p array of parameters to be rounded
     * @return 
     */    
    protected ArrayList<parameter> roundToStepping(ArrayList<parameter> p)
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
    
        
 
     /**
     * Sorts the food sources by rank
     * @param population  ArrayList of food sources to be sorted
     * @return 
     */  
    protected ArrayList<ArrayList<SimulationType>> sortPopulation(ArrayList<ArrayList<SimulationType>> population)
    {
        Collections.sort(population, new Comparator<ArrayList<SimulationType>>()
        {
            @Override
            public int compare(ArrayList<SimulationType> a, ArrayList<SimulationType> b) 
            {
                return Double.compare(a.get(0).getDistance(), b.get(0).getDistance());
            }                    
        });
        return population;
    }
    
    public void printPopulationDistances()
    {
        for (int i = 0; i<population.size(); ++i)
        {
            String logString = "Distance " + i + " \t: " + population.get(i).get(0).getDistance();
            support.log(logString);
        }
    }
    
    protected void updateTopMeasure()
    {
        boolean newTopMeasurefound = false;
        for (int i=0; i<population.size(); ++i)
        {
            if(population.get(i).get(0).getDistance()<topDistance)
            {
                topDistance = population.get(i).get(0).getDistance();
                topMeasure = new SimulationType(population.get(i).get(0));
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
    
    public abstract void run();
}
