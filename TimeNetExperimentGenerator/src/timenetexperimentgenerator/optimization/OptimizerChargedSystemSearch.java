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


public class OptimizerChargedSystemSearch extends OptimizerPopulationBased implements Runnable, Optimizer{


//private String tmpPath = "";
//private String filename = "";//Original filename
//private String pathToTimeNet = "";
//private String logFileName = "";
//private MainFrame parent = null;
//private JTabbedPane MeasureFormPane;
//private ArrayList<MeasureType> listOfMeasures = new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
//private ArrayList<parameter> parameterBase;//Base set of parameters, start/end-value, stepping, etc.
//private JLabel infoLabel;
//private double simulationTimeSum = 0;
//private double cpuTimeSum = 0;

//private ArrayList<SimulationType> charges = new ArrayList<SimulationType>();//History of all simulation runs

private int numberOfCharges = 5; //number of active charges to explore design space
//private int maxNumberOfOptiCycles = 100; //maximum number of cycles, before optimization terminates
//private int maxNumberOfOptiCyclesWithoutImprovement = 10; //how many cycles without improvement until break optimization loop
//private int currentNumberOfOptiCyclesWithoutImprovement = 0;

private double[] distances; //current Distance of all objects
private double[] powerOfCharges; // attraction power of every object
private double[][] speedOfCharges; //current speed vector for all objects with all parameters;

//private SimulationType topMeasure;//temp top measure before implementing top-List
//private double topDistance = Double.POSITIVE_INFINITY;//temp top distance

private final int maxAttraction;//limit of attraction-force of 2 charges

public OptimizerChargedSystemSearch()
{
    this.maxAttraction = 100;
    logFileName = support.getTmpPath() + File.separator+"Optimizing_with_CSS_" + Calendar.getInstance().getTimeInMillis() + "_ALL" + ".csv";
}
    
    
    private void calculatePower()
    {
        //calculate attraction of each charge
        for(int i=0; i<population.size(); ++i)
        {
            powerOfCharges[i] = 1 / population.get(i).get(0).getDistance(); //temporary function for calculating attraction. TODO: test other funktions
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
        }    
    }

    public void run()
    {
        int optiCycleCounter = 0;
        population = createRandomPopulation(numberOfCharges, false);
        distances = new double[numberOfCharges];
        powerOfCharges = new double[numberOfCharges];
        speedOfCharges = new double[numberOfCharges][parameterBase.size()];
        
        Arrays.fill(distances, 0);
        for (int i=0; i<numberOfCharges; ++i)
        {
            double[] newArray = new double[parameterBase.size()];
            Arrays.fill(newArray, 0);
            speedOfCharges[i] = newArray;
        }
        Arrays.fill(powerOfCharges, 0);
        
        Simulator mySimulator = SimOptiFactory.getSimulator();       
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), optiCycleCounter, false);
        support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);
        
        ArrayList<SimulationType> simulationResults = mySimulator.getListOfCompletedSimulationParsers(); 
        population = getPopulationFromSimulationResults(simulationResults);
        
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
            simulationResults = mySimulator.getListOfCompletedSimulationParsers();
            support.addLinesToLogFileFromListOfParser(simulationResults, logFileName);
            population = getPopulationFromSimulationResults(simulationResults);
            
            //calculateDistances();
            updateTopMeasure();
            printPopulationDistances();
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
    
    private ArrayList<parameter> getParameters(int indexOfCharge)
    {
        return population.get(indexOfCharge).get(0).getListOfParameters();
    }
    
    private void setParameters(ArrayList<parameter> parameterList, int indexOfCharge)
    {
        population.get(indexOfCharge).get(0).setListOfParameters(parameterList);        
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
