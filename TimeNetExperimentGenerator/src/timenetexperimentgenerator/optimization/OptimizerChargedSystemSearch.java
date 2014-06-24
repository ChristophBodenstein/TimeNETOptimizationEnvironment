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
import timenetexperimentgenerator.datamodel.parser;
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
private parameter[] parameterBase;//Base set of parameters, start/end-value, stepping, etc.
private JLabel infoLabel;
private double simulationTimeSum = 0;
private double cpuTimeSum = 0;

private ArrayList<parser> charges = new ArrayList<parser>();//History of all simulation runs

private int numberOfCharges = 5; //number of active charges to explore design space
private int maxNumberOfOptiCycles = 100; //maximum number of cycles, before optimization terminates
private int maxNumberOfOptiCyclesWithoutImprovement = 10; //how many cycles without improvement until break optimization loop
private int currentNumberOfOptiCyclesWithoutImprovement = 0;

private double[] distances; //current Distance of all objects
private double[] powerOfCharges; // attraction power of every object
private double[][] speedOfCharges; //current speed vector for all objects with all parameters;

private parser topMeasure;//temp top measure before implementing top-List
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
       
    charges = new ArrayList<parser>();
    distances = new double[numberOfCharges];
    powerOfCharges = new double[numberOfCharges];
    speedOfCharges = new double[numberOfCharges][parameterBase.length];
    
    //allocate all measurements the same parameterbase
    if (listOfMeasures.size()>1)
    {
        for (int i=1; i<listOfMeasures.size(); ++i)
        {
            ArrayList<parameter> parameterList = listOfMeasures.get(0).getParameterList();
            listOfMeasures.get(i).setParameterList(parameterList);
        }
    }

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
        charges = new ArrayList<parser>();
        //init of supporting-arrays
        distances = new double[numOfCharges];
        Arrays.fill(distances, 0);
        speedOfCharges = new double[numOfCharges][parameterBase.length];
        for (int i=0; i<numOfCharges; ++i)
        {
            double[] newArray = new double[parameterBase.length];
            Arrays.fill(newArray, 0);
            speedOfCharges[i] = newArray;
        }
        powerOfCharges = new double[numOfCharges];
        Arrays.fill(powerOfCharges, 0);
        
        
        //fill population with random values
        for(int i=0; i<numOfCharges; ++i)
        {
            parser p = new parser();
            charges.add(p);
            MeasureType newMeasure = new MeasureType();
            
            parameter[] pArray = support.getCopyOfParameterSet(parameterBase);
            for (int j=0; j<pArray.length; ++j)
            {
                //creates a random value between start and end value for each parameter
                double newValue = pArray[j].getStartValue() + Math.random() * (pArray[j].getEndValue() - pArray[j].getStartValue());
                pArray[j].setValue(newValue);
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
    private parameter[] roundToStepping(parameter[] p)
    {
        double currentValue = 0;
        double currentStepping = 0;
        for (int i=0; i<p.length; ++i)
        {
            currentValue = p[i].getValue();
            currentStepping = p[i].getStepping();
            
            currentValue = Math.round(currentValue / currentStepping) * currentStepping;
            
            if (currentValue < p[i].getStartValue())
            {
                currentValue = p[i].getStartValue();
            }
            else if (currentValue > p[i].getEndValue())
            {
                currentValue = p[i].getEndValue();
            }
            
            
            p[i].setValue(currentValue);
        }
        return p;
    }
    
    private void calculateDistances()
    {            
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
            ArrayList<parameter> currentMeasure = getParameters(currentChargeNumber);
            
            for(int compareChargeNumber=0; compareChargeNumber<numberOfCharges;++compareChargeNumber)
            {
                ArrayList<parameter> compareMeasure = getParameters(compareChargeNumber);
                if (currentChargeNumber!=compareChargeNumber)
                {
                    double attraction = powerOfCharges[compareChargeNumber] / powerOfCharges[currentChargeNumber];
                    
                    if(attraction>maxAttraction)
                    {
                        attraction = maxAttraction;
                    }
                    
                    for (int parameterNumber = 0; parameterNumber<speedOfCharges[currentChargeNumber].length; ++parameterNumber)
                    {
                        if (currentMeasure.get(parameterNumber).isIteratableAndIntern())
                        {
                            double currentValue = currentMeasure.get(parameterNumber).getValue();
                            double compareValue = compareMeasure.get(parameterNumber).getValue();
                            double currentSpeed = speedOfCharges[currentChargeNumber][parameterNumber];
                        
                            double diffSpeed = (compareValue - currentValue) * attraction / maxAttraction;
                            currentSpeed += diffSpeed;
                            currentValue += currentSpeed;
                        
                            //safety check to prevent charges to "fly" over the border
                            if (currentValue < currentMeasure.get(parameterNumber).getStartValue())
                            {
                                currentValue = currentMeasure.get(parameterNumber).getStartValue();
                            }
                            else if (currentValue > currentMeasure.get(parameterNumber).getEndValue())
                            {
                            currentValue = currentMeasure.get(parameterNumber).getEndValue();
                            }
                        
                            speedOfCharges[currentChargeNumber][parameterNumber] = currentSpeed;
                            currentMeasure.get(parameterNumber).setValue(currentValue);
                        } 
                    }
                }
            }
            setParameters(currentMeasure, currentChargeNumber);
        }
        
        //copy back to charges
//        charges.clear();
//        for(int measureCount=0; measureCount<tempMeasure.size(); ++measureCount)
//        {
//            charges.add(Arrays.copyOf(tempMeasure.get(measureCount), tempMeasure.get(measureCount).length));
//        }
    }
    
    private void updateTopMeasure()
    {
        boolean newTopMeasurefound = false;
        for (int i=0; i<distances.length; ++i)
        {
            if(distances[i]<topDistance)
            {
                topDistance = distances[i];
                topMeasure = new parser(charges.get(i));
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
        support.waitForEndOfSimulator(mySimulator, optiCycleCounter, 600);
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
            
            ArrayList<parameter[]> parameterList = getNextParameterSetAsArrayList();
            
            for (parameter[] pArray : parameterList)
            {
                pArray =  roundToStepping(pArray);
            }
            
            System.out.println("Number of Parameters in: " + parameterList.get(0).length);
            
            mySimulator = SimOptiFactory.getSimulator();
            mySimulator.initSimulator(parameterList, simulationCounter, false);
            support.waitForEndOfSimulator(mySimulator, simulationCounter, 6000);
            simulationCounter = mySimulator.getSimulationCounter();
            System.out.println("NumMeasuresOut: " + mySimulator.getListOfCompletedSimulationParsers().get(0).getMeasures().size());
            charges = mySimulator.getListOfCompletedSimulationParsers();
            
            calculateDistances();
            updateTopMeasure();
            printDistances();
            ++optiCycleCounter;
        }
        for(int measureCount=0;measureCount<listOfMeasures.size();measureCount++)
        {
            String measureName  = listOfMeasures.get(measureCount).getMeasureName();
            MeasureType activeMeasure = topMeasure.getMeasureByName(measureName);
            System.out.println(activeMeasure.getStateAsString());
        }
        support.log("CCS Finished");
        
    }
    
    //TODO: make only one function-->init charges with parameter, so could leave paramter of parser blank (Why we need parameter[] in parser?
    //parser has an ArrayList of Measure, which contains the paramter[] already...)
    private ArrayList<parameter[]> getNextParameterSetAsArrayList()
    {
        ArrayList<parameter[]> myParametersetList = new ArrayList<parameter[]>();
        for (parser p : charges)
        {
            parameter[] pArray = p.getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }
    private ArrayList<parameter[]> getNextParameterSetAsArrayListFromMeasures()
    {
        ArrayList<parameter[]> myParamterList = new ArrayList<parameter[]>();
        for (parser p : charges)
        {
            ArrayList<MeasureType> measures = p.getMeasures();
            parameter[] pArray = support.convertArrayListToArray(measures.get(0).getParameterList());
            myParamterList.add(pArray);
        }
        return myParamterList;
    }
    
    private ArrayList<parameter> getParameters(int indexOfCharge)
    {
        parameter pArray[] = charges.get(indexOfCharge).getListOfParameters();
        if (pArray == null)
        {
            pArray = support.convertArrayListToArray(charges.get(indexOfCharge).getMeasures().get(0).getParameterList());//cruel coding style... only temp
        }
        
        ArrayList<parameter> paraList = new  ArrayList<parameter>();
        for (int i = 0; i < pArray.length ; ++i)
        {
            paraList.add(pArray[i]);
        }
        
        return paraList;
    }
    
    private void setParameters(ArrayList<parameter> parameterList, int indexOfCharge)
    {
        parameter pArray[] = new parameter[parameterList.size()];
        
        for (int i = 0; i < parameterList.size(); ++i)
        {
            pArray[i] = parameterList.get(i);
        }
        
        charges.get(indexOfCharge).setListOfParameters(pArray);        
    }
    

    
    private void printDistances()
    {
        for (int i = 0; i<distances.length; ++i)
        {
            String message = "Charge " + i + ": " + distances[i];
            //support.log(message);
            System.out.println(message);
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

}
