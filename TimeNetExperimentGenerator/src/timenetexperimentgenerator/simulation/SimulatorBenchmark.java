/**
 * Simulator which uses log data from already done simulations and returns them.
 * 
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import timenetexperimentgenerator.MainFrame;
import timenetexperimentgenerator.MeasurementForm;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.typedef.*;
import timenetexperimentgenerator.typedef.typeOfBenchmarkFunction;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read log-data
 * @author Christoph Bodenstein
 */
public class SimulatorBenchmark implements Simulator{
private SimulationCache mySimulationCache=null;
private ArrayList<SimulationType> myListOfSimulations=null;
private int simulationCounter=0;
private String logFileName;
private typeOfBenchmarkFunction benchmarkFunction=typeOfBenchmarkFunction.Sphere;
int status=0;
    

    /**
     * Constructor
     */
     public SimulatorBenchmark(){
     logFileName=support.getTmpPath()+File.separator+"SimLog_Benchmark"+Calendar.getInstance().getTimeInMillis()+".csv";
     support.log("LogfileName:"+logFileName);
     }

    /**
     * inits and starts the simulation, this is neccessary and must be implemented
     * In Benchmark we don`t use a local cache
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param simulationCounterTMP actual Number of simulation, will be increased with every simulation-run
     */
    public void initSimulator(ArrayList<ArrayList <parameter> > listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
    double limitLower=-5.0, limitUpper=5.0;//The limits of the used benchmark function

        switch(benchmarkFunction){
            case Ackley:
                limitLower=-5.0;
                limitUpper=5.0;
                break;
            case Rosenbrock:
                limitLower=-5.0;
                limitUpper=5.0;
                break;
            case Sphere:
                limitLower=-5.0;
                limitUpper=5.0;
                break;
            case Booth:
                limitUpper=10.0;
                limitLower=-10.0;
                break;
            case Matya:
                limitUpper=10.0;
                limitLower=-10.0;
                break;
            default:
                break;
        }


        myListOfSimulations=new ArrayList<SimulationType>();
        
        for(int i=0;i<listOfParameterSetsTMP.size();i++){
        SimulationType tmpSimulation=new SimulationType();
        ArrayList<parameter> tmpParameterList=listOfParameterSetsTMP.get(i);
        ArrayList<parameter> tmpListOfChangableParameter=support.getListOfChangableParameters(tmpParameterList);
            //TODO make deep copy of Parameterlist

        double sum=0.0;
        int dimension=tmpListOfChangableParameter.size();
        double xNew=0.0;

        switch(benchmarkFunction){
                case Ackley:

                    break;
                case Rosenbrock:

                    break;
                case Sphere:
                    sum=0.0;
                    for(int c=0;c<dimension;c++){
                    parameter p=tmpListOfChangableParameter.get(c);
                    //Check Range and align the value to map constraints
                    xNew=(p.getValue()-p.getStartValue()) /(p.getEndValue()-p.getStartValue()) ;
                    xNew=xNew*(limitUpper-limitLower)+limitLower;
                    sum+=(xNew*xNew);
                    }//End of for-c-loop
                    break;
                case Booth:
                    break;
                case Matya:

                    break;
                default:
                    sum=0.0;
                    break;
                }


           


        ArrayList<MeasureType> tmpListOfMeasurements=((MeasurementForm)support.getMeasureFormPane().getComponentAt(0)).getMeasurements();
           //All Measure will have the same result value

        ArrayList<MeasureType> newListOfMeasurements=new ArrayList<MeasureType>();

            for(int d=0;d<tmpListOfMeasurements.size();d++){
            //make deep copy of old Measurement
            MeasureType tmpMeasurement=new MeasureType(tmpListOfMeasurements.get(d));
            tmpMeasurement.setAccuraryReached(true);
            tmpMeasurement.setMeanValue(sum);
            //TODO fill out all other imformation
            newListOfMeasurements.add(tmpMeasurement);

            }

        tmpSimulation.setListOfParameters(tmpParameterList);
        tmpSimulation.setMeasures(newListOfMeasurements);
        myListOfSimulations.add(tmpSimulation);

        
        this.status=(100/listOfParameterSetsTMP.size())*i;
        }//End of for-i-loop
        this.status=100;


        if(log){
        //Print out a log file
        support.addLinesToLogFileFromListOfParser(myListOfSimulations, logFileName);
        }
        
        
    }

    /**
     * Returns the actual status of all simulations 
     * @return % of simulatiions that are finished
     */
    public int getStatus() {
        return this.status;
    }

    
    /**
     * Returns the actual simulation Counter
     * @return actual simulation counter
     */
    public int getSimulationCounter() {
        return this.simulationCounter;
    }

    
    
    /**
     * Gets the list of completed simulations, should be used only if getStatus() returns 100
     * @return list of completed simulations (parsers) which contain all data from the log-files
     */
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
        return this.myListOfSimulations;
    }

    
    /**
     * Returns the data-source for simulated simulation-runs
     * @return the mySimulationCache
     */
    public SimulationCache getMySimulationCache() {
        return mySimulationCache;
    }

    /**
     * Sets the data-source for simulated simulation-runs
     * @param mySimulationCache the mySimulationCache to set
     */
    public void setMySimulationCache(SimulationCache mySimulationCache) {
        this.mySimulationCache = mySimulationCache;
    }


    /**
     * Calculated the approx number of simulation steps for one simulation
     * @return approx number of simulation steps
     * @param confidenceIntervall given ConfidenceIntervall
     * @param maxRelError given Maximum relative Error
     */
    private double getSimulationSteps(double confidenceIntervall, double maxRelError){
    return 10;
    }

    /**
     * Calulated the approx CPU Time for one Simulation run
     * @return approx CPU Time for one simulation run
     * @param confidenceIntervall given ConfidenceIntervall
     * @param maxRelError given Maximum relative Error
     */
    private double getCPUTime(double confidenceIntervall, double maxRelError){
    return 10;
    }
}
