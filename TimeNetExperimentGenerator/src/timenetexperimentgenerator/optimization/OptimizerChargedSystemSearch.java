/*
 * Optimization-Algorithm implemented by Andy Seidel during Diploma Thesis 2014
 * 
 * Orinal paper [10KT]: 
 * Kaveh, Talatahari:
 * A novel heuristic optimization method: charged system search (2010)
 */

package timenetexperimentgenerator.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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


public class OptimizerChargedSystemSearch implements Runnable, Optimizer{


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

private ArrayList<SimulationType> charges = new ArrayList<SimulationType>();//History of all simulation runs

private int numberOfCharges = 5; //number of active charges to explore design space
private int maxNumberOfOptiCycles = 100; //maximum number of cycles, before optimization terminates
private int maxNumberOfOptiCyclesWithoutImprovement = 10; //how many cycles without improvement until break optimization loop
private int currentNumberOfOptiCyclesWithoutImprovement = 0;

private double[] distances; //current Distance of all objects
private double[] powerOfCharges; // attraction power of every object
private double[][] speedOfCharges; //current speed vector for all objects with all parameters;

private SimulationType topMeasure;//temp top measure before implementing top-List
private double topDistance = Double.POSITIVE_INFINITY;//temp top distance

private final int maxAttraction;//limit of attraction-force of 2 charges

public OptimizerChargedSystemSearch()
{
    this.maxAttraction = 100;
    logFileName=support.getTmpPath()+File.separator+"Optimizing_with_CSS_"+Calendar.getInstance().getTimeInMillis()+"_ALL"+".csv";
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
       
    charges = new ArrayList<SimulationType>();
    distances = new double[numberOfCharges];
    powerOfCharges = new double[numberOfCharges];
    speedOfCharges = new double[numberOfCharges][parameterBase.size()];
    
    //allocate all measurements the same parameterbase
//    if (listOfMeasures.size()>1)
//    {
//        for (int i=1; i<listOfMeasures.size(); ++i)
//        {
//            ArrayList<parameter> parameterList = listOfMeasures.get(0).getParameterList();
//            listOfMeasures.get(i).setParameterList(parameterList);
//        }
//    }

    this.filename=support.getOriginalFilename();// originalFilename;
    
    //Ask for Tmp-Path
    this.tmpPath=support.getTmpPath();
    
    //Start this Thread
    new Thread(this).start();
}
    
   /**
     * Creates random starting population
     * @param numOfCharges number of charges to be created
     * @param ignoreStepping set false to round created
     */
    private void createNewRandomPopulation(int numOfCharges, boolean ignoreStepping)
    {
        //init of needed data structures
        charges = new ArrayList<SimulationType>();
        //init of supporting-arrays
        distances = new double[numOfCharges];
        Arrays.fill(distances, 0);
        speedOfCharges = new double[numOfCharges][parameterBase.size()];
        for (int i=0; i<numOfCharges; ++i)
        {
            double[] newArray = new double[parameterBase.size()];
            Arrays.fill(newArray, 0);
            speedOfCharges[i] = newArray;
        }
        powerOfCharges = new double[numOfCharges];
        Arrays.fill(powerOfCharges, 0);
        
        
        //fill population with random values
        for(int i=0; i<numOfCharges; ++i)
        {
            SimulationType p = new SimulationType();
            charges.add(p);
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
            charges.get(i).setListOfParameters(pArray);            
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
    
    private void calculateDistances()
    {   
        setMeasureTargets(charges);
        for (int parserCount = 0; parserCount < charges.size(); ++ parserCount)
        {
            distances[parserCount] = charges.get(parserCount).getDistance();
        }        
    }
    

    
    private void calculatePower()
    {
        calculateDistances();
        
        //calculate attraction of each charge
        for(int i=0; i<distances.length; ++i)
        {
            powerOfCharges[i] = 1 / distances[i]; //temporary function for calculating attraction. TODO: test other funktions
        }
    }
    
    private void updatePositions()
    {
        calculatePower();
        
        //currentCharge = the charge to be moved
        //compareCharge = the charge, which attracts the currentCharge
        for(int currentChargeNumber=0; currentChargeNumber<numberOfCharges; ++currentChargeNumber)
        {
            ArrayList<parameter> currentParameterSet = getParameters(currentChargeNumber);
            
            for(int compareChargeNumber=0; compareChargeNumber<numberOfCharges;++compareChargeNumber)
            {
                ArrayList<parameter> compareParemeterSet = getParameters(compareChargeNumber);
                if (currentChargeNumber!=compareChargeNumber)
                {
                    double attraction = powerOfCharges[compareChargeNumber] / powerOfCharges[currentChargeNumber];
                    
                    if(attraction>maxAttraction)
                    {
                        attraction = maxAttraction;
                    }
                    
                    for (int parameterNumber = 0; parameterNumber<speedOfCharges[currentChargeNumber].length; ++parameterNumber)
                    {
                        if (currentParameterSet.get(parameterNumber).isIteratableAndIntern())
                        {
                            double currentValue = currentParameterSet.get(parameterNumber).getValue();
                            double compareValue = compareParemeterSet.get(parameterNumber).getValue();
                            double currentSpeed = speedOfCharges[currentChargeNumber][parameterNumber];
                        
                            double diffSpeed = (compareValue - currentValue) * attraction / maxAttraction;
                            currentSpeed += diffSpeed;
                            currentValue += currentSpeed;
                        
                            //safety check to prevent charges to "fly" over the border
                            if (currentValue < currentParameterSet.get(parameterNumber).getStartValue())
                            {
                                currentValue = currentParameterSet.get(parameterNumber).getStartValue();
                            }
                            else if (currentValue > currentParameterSet.get(parameterNumber).getEndValue())
                            {
                            currentValue = currentParameterSet.get(parameterNumber).getEndValue();
                            }
                        
                            speedOfCharges[currentChargeNumber][parameterNumber] = currentSpeed;
                            currentParameterSet.get(parameterNumber).setValue(currentValue);
                        } 
                    }
                }
            }
            setParameters(currentParameterSet, currentChargeNumber);
        }    }
    
    private void updateTopMeasure()
    {
        boolean newTopMeasurefound = false;
        for (int i=0; i<distances.length; ++i)
        {
            if(distances[i]<topDistance)
            {
                topDistance = distances[i];
                topMeasure = new SimulationType(charges.get(i));
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
        createNewRandomPopulation(numberOfCharges,false);
        
        Simulator mySimulator = SimOptiFactory.getSimulator();       
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), optiCycleCounter, false);
        support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
        //support.addLinesToLogFileFromListOfParser(mySimulator.getListOfCompletedSimulationParsers(), logFileName);
                

        System.out.println("NumMeasuresOut: " + mySimulator.getListOfCompletedSimulationParsers().get(0).getMeasures().size());
        charges = mySimulator.getListOfCompletedSimulationParsers();
        
        
        calculateDistances();
        printDistances();
        
        int simulationCounter = 0;
        while(optiCycleCounter < this.maxNumberOfOptiCycles)
        {
            updatePositions();
            if (currentNumberOfOptiCyclesWithoutImprovement >= maxNumberOfOptiCyclesWithoutImprovement)
            {
                support.log("Too many optimization cycles without improvement. Ending optimization.");
                break;
            }
            
            ArrayList< ArrayList<parameter> > parameterList = getNextParameterSetAsArrayList();
            
            for (ArrayList<parameter> pArray : parameterList)
            {
                pArray =  roundToStepping(pArray);
            }
            
            System.out.println("Number of Parameters in: " + parameterList.get(0).size());
            
            mySimulator = SimOptiFactory.getSimulator();
            mySimulator.initSimulator(parameterList, simulationCounter, false);
            support.waitForEndOfSimulator(mySimulator, simulationCounter, support.DEFAULT_TIMEOUT);
            simulationCounter = mySimulator.getSimulationCounter();
            support.log("NumMeasuresOut: " + mySimulator.getListOfCompletedSimulationParsers().get(0).getMeasures().size());
            charges = mySimulator.getListOfCompletedSimulationParsers();
            support.addLinesToLogFileFromListOfParser(charges, logFileName);
            
            calculateDistances();
            updateTopMeasure();
            printDistances();
            ++optiCycleCounter;
        }
        for(int measureCount=0;measureCount<listOfMeasures.size();measureCount++)
        {
            String measureName  = listOfMeasures.get(measureCount).getMeasureName();
            MeasureType activeMeasure = topMeasure.getMeasureByName(measureName);
            support.log(activeMeasure.getStateAsString());
        }
        support.log("CCS Finished");
        
    }
    
    //TODO: make only one function-->init charges with parameter, so could leave paramter of SimulationType blank (Why we need parameter[] in SimulationType?
    //parser has an ArrayList of Measure, which contains the paramter[] already...)
    private ArrayList< ArrayList<parameter> > getNextParameterSetAsArrayList()
    {
        ArrayList< ArrayList<parameter> > myParametersetList = new ArrayList< ArrayList<parameter> >();
        for (SimulationType p : charges)
        {
            ArrayList<parameter> pArray = p.getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }
    
    private ArrayList<parameter> getParameters(int indexOfCharge)
    {
        return charges.get(indexOfCharge).getListOfParameters();
    }
    
    private void setParameters(ArrayList<parameter> parameterList, int indexOfCharge)
    {
        charges.get(indexOfCharge).setListOfParameters(parameterList);        
    }
    

    
    private void printDistances()
    {
        for (int i = 0; i<distances.length; ++i)
        {
            String message = "Charge " + i + ": " + distances[i];
            //support.log(message);
            support.log(message);
        }
    }
    
    /**
     * returnes the number of charges used for optimization
     * @return the number of charges
     */
    public int getNumberOfCharges()
    {
        return this.numberOfCharges;
    }
    
    /**
     * sets the number of charges, if its a least one. zero or negative values are ignored
     * @param newNumberOfCharges the new number of charges
     */
    public void setNumberOfCharges(int newNumberOfCharges)
    {
        if (newNumberOfCharges > 0)
        {
            this.numberOfCharges = newNumberOfCharges;
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

    public SimulationType getOptimum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
