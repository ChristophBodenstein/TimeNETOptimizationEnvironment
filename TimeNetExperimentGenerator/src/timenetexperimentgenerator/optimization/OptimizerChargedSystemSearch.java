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


String tmpPath="";
String filename="";//Original filename
String pathToTimeNet="";
String logFileName="";
MainFrame parent=null;
JTabbedPane MeasureFormPane;
ArrayList<MeasureType> listOfMeasures=new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
ArrayList<parser> charges=new ArrayList<parser>();//History of all simulation runs
parameter[] parameterBase;//Base set of parameters, start/end-value, stepping, etc.
double[] arrayOfIncrements;
boolean optimized=false;//False until Optimization is ended
JLabel infoLabel;
double simulationTimeSum=0;
double cpuTimeSum=0;

parser solution;

int numberOfCharges = 5; //number of active charges to explore design space
int numberOfHistoryCharges = 3; //size of memory for known top solutions;
//ArrayList<MeasureType> historyCharges = new ArrayList<MeasureType>(); //array for the so far known top solutions;


//ArrayList<ArrayList<MeasureType>> chargeMeasures = new ArrayList<ArrayList<MeasureType>>();
//ArrayList<parameter[]> charges = new ArrayList<parameter[]>(); //the current space of objects
double[] distances; //current Distance of all objects
double[] powerOfCharges; // attraction power of every object
double[][] speedOfCharges; //current speed vector for all objects with all parameters;

parameter[] topMeasure;//temp top measure before implementing top-List
double topDistance = 0;//temp top distance

final int maxAttraction = 100;

public OptimizerChargedSystemSearch()
{
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

    //Alle Steppings auf Standard setzen
    arrayOfIncrements=new double[parameterBase.length];
    for(int i=0; i<parameterBase.length; i++)
    {
        arrayOfIncrements[i]=support.getDouble(parameterBase[i].getStepping());
    }
        
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
            p[i].setValue(currentValue);
        }
        return p;
    }
    
    private void calculateDistances()
    {            
        for (int parserCount = 0; parserCount < charges.size(); ++ parserCount)
        {
            distances[parserCount] = getActualDistance(charges.get(parserCount));
        }        
    }
    
    private double getActualDistance(parser p)
    {
        double distance=0;
        for(int measureCount=0;measureCount<listOfMeasures.size();measureCount++)
        {
            MeasureType activeMeasure = p.getMeasureByName(listOfMeasures.get(measureCount).getMeasureName());
            MeasureType activeMeasureFromInterface = listOfMeasures.get(measureCount);//Contains Optimization targets
            activeMeasure.setTargetValue(activeMeasureFromInterface.getTargetValue(), activeMeasureFromInterface.getTargetKindOf());
            
            if(activeMeasure.getTargetKindOf().equals("value"))
            {
                distance=activeMeasure.getDistanceFromTarget();
            }
            else if(activeMeasure.getTargetKindOf().equals("min"))
            {
                distance=activeMeasure.getMeanValue();
            }
            else if(activeMeasure.getTargetKindOf().equals("max"))
            {
                distance=0-activeMeasure.getMeanValue();
            }
            else
            {
                //TODO error handling for unknown target-type
            }
        }
        return distance;
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
                        double currentValue = currentMeasure.get(parameterNumber).getValue();
                        double compareValue = compareMeasure.get(parameterNumber).getValue();
                        double currentSpeed = speedOfCharges[currentChargeNumber][parameterNumber];
                        
                        double diffSpeed = (compareValue - currentValue) * attraction / maxAttraction;
                        currentSpeed += diffSpeed;
                        currentValue += currentSpeed;
                        speedOfCharges[currentChargeNumber][parameterNumber] = currentSpeed;
                        currentMeasure.get(parameterNumber).setValue(currentValue);
                    }
                }
            }
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
        for (int i=0; i<distances.length; ++i)
        {
            if(distances[i]<topDistance)
            {
                topDistance = distances[i];
                //topMeasure = charges.get(i); //TODO: implement deepcopy
            }
        }
    }

    public void run()
    {
        int simulationCounter=0;
        createNewRandomPopulation(numberOfCharges,false);
        
        Simulator mySimulator = SimOptiFactory.getSimulator();
        
        mySimulator.initSimulator(getNextParametersetAsArrayList(), simulationCounter, false);
        support.waitForEndOfSimulator(mySimulator, simulationCounter, 600);
        //support.addLinesToLogFileFromListOfParser(mySimulator.getListOfCompletedSimulationParsers(), logFileName);
        
        for (int i=0; i<charges.size(); ++i)
        {
            charges.get(i).setMeasures(mySimulator.getListOfCompletedSimulationParsers().get(i).getMeasures());
        }
        
        charges = mySimulator.getListOfCompletedSimulationParsers();
        //charges = solution.getMeasures();
        
        while(simulationCounter<100)
        {
            mySimulator.initSimulator(getNextParametersetAsArrayList(), simulationCounter, false);
            support.waitForEndOfSimulator(mySimulator, simulationCounter, 600);
            //charges = solution.getMeasures();
            
            updatePositions();
            ++simulationCounter;
            

        }
        support.log("CCS Finished");
    }
    
    private ArrayList<parameter[]> getNextParametersetAsArrayList()
    {
        ArrayList<parameter[]> myParametersetList = new ArrayList<parameter[]>();
        for (parser p : charges)
        {
            parameter[] pArray = p.getListOfParameters();
            myParametersetList.add(pArray);
        }
        return myParametersetList;
    }
    
    private MeasureType getDeepCopy(MeasureType originalMeasure)//TODO: implement deepCopy/clone in MeasureType-Class
    {
        MeasureType newMeasure = new MeasureType();
        
        newMeasure.setCPUTime(originalMeasure.getCPUTime());
        if (originalMeasure.getConfidenceInterval() != null)
        {
            newMeasure.setConfidenceInterval(Arrays.copyOf(originalMeasure.getConfidenceInterval(),originalMeasure.getConfidenceInterval().length));
        }
        newMeasure.setEpsilon(originalMeasure.getEpsilon());
        newMeasure.setMeanValue(originalMeasure.getMeanValue());
        newMeasure.setMeasureName(originalMeasure.getMeasureName());
        newMeasure.setSimulationTime(originalMeasure.getSimulationTime());
        newMeasure.setTargetValue(originalMeasure.getTargetValue(), originalMeasure.getTargetKindOf());
        newMeasure.setVariance(originalMeasure.getVariance());
        
        ArrayList<parameter> newParameterList = new ArrayList<parameter>();
        for (int i=0; i<originalMeasure.getParameterList().size(); ++i) //TODO: implement paramterListsize
        {
            newParameterList.add(originalMeasure.getParameterList().get(i));
        }
        newMeasure.setParameterList(newParameterList);
               
        return newMeasure;
    }
    
    private ArrayList<parameter> getParameters(int indexOfCharge)
    {
        parameter pArray[] = charges.get(indexOfCharge).getListOfParameters();
        
        ArrayList<parameter> paraList = new  ArrayList<parameter>();
        for (int i = 0; i < pArray.length ; ++i)
        {
            paraList.add(pArray[i]);
        }
        
        return paraList;
    }
    
//    private void setParameters(ArrayList<parameter> parameterList, int indexOfCharge)
//    {
//        parameter pArray[] = new parameter[parameterList.size()];
//        
//        for (int i = 0; i < parameterList.size(); ++i)
//        {
//            pArray[i] = parameterList.get(i);
//        }
//        
//        charges.get(indexOfCharge).setListOfParameters(pArray);        
//    }
    
    private ArrayList<parameter> convertArrayToArrayList(parameter p[])
    {
        ArrayList<parameter> paraList = new ArrayList<parameter>();
        for (int i=0; i<p.length; ++i)
        {
            paraList.add(p[i]);
        }
        return paraList;
    }
    
    private parameter[] convertArrayListToArray(ArrayList<parameter> list)
    {
        parameter pArray[] = new parameter[list.size()];
        for (int i = 0; i < list.size(); ++i)
        {
            pArray[i] = list.get(i);
        }
        return pArray;
    }
    
    private void printMeanValues()
    {
        for (int i = 0; i<charges.size(); ++i)
        {
            
        }
    }

}
