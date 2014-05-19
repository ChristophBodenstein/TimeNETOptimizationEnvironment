/*
 * Optimizer using the simulated annealing algorithm
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.optimization;

import java.io.File;
import java.util.ArrayList;
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
 * @author Christoph Bodenstein
 */
public class OptimizerSimAnnealing implements Runnable, Optimizer{
private int SimI=1,SimT=0;
private double maxTemp=20;
private int stepCountTemp=100;
private double sizeOfNeighborhood=10;//in percent
private double Fx;//Current Distance
private double Fy;//New Distance?
private int typeOfNeighbordood=0;

private int simulationCounter=0;
parser currentSolution;
parser nextSolution;
String tmpPath="";
String filename="";//Original filename
String pathToTimeNet="";
MainFrame parent=null;
JTabbedPane MeasureFormPane;
ArrayList<MeasureType> listOfMeasures=new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
ArrayList<parser> historyOfParsers=new ArrayList<parser>();//History of all simulation runs
parameter[] parameterBase;//Base set of parameters, start/end-value, stepping, etc.
double[] arrayOfIncrements;
boolean optimized=false;//False until Optimization is ended
JLabel infoLabel;
double simulationTimeSum=0;
double cpuTimeSum=0;
String logFileName;
int wrongSolutionCounter=10;

    /**
     * Constructor
     *
     */
    public OptimizerSimAnnealing() {
    logFileName=support.getTmpPath()+File.separator+"Optimizing_with_Sim.Annealing"+Calendar.getInstance().getTimeInMillis()+".csv";
    support.log("LogfileName:"+logFileName);
    }

    
    /**
     * Init the optimization, loads the default values and targets from support-class and starts optimization
     *
     */
    public void initOptimizer() {
    this.infoLabel=support.getStatusLabel();//  infoLabel;
    this.pathToTimeNet=support.getPathToTimeNet();// pathToTimeNetTMP;
    this.MeasureFormPane=support.getMeasureFormPane();//MeasureFormPaneTMP;
    this.parent=support.getMainFrame();// parentTMP;
    this.parameterBase=parent.getParameterBase();
    this.listOfMeasures=parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
    support.log("# of Measures to be optimized: "+this.listOfMeasures.size());

    //Alle Steppings auf Standard setzen
    arrayOfIncrements=new double[parameterBase.length];
        for(int i=0;i<parameterBase.length;i++){
        arrayOfIncrements[i]=support.getDouble(parameterBase[i].getStepping());
        }

    this.filename=support.getOriginalFilename();// originalFilename;
    //Ask for Tmp-Path

    this.tmpPath=support.getTmpPath();
    //Start this Thread
    new Thread(this).start();
    }

    public void run() {
        //Simulator init with initional parameterset
        Simulator mySimulator=SimOptiFactory.getSimulator();
        
        mySimulator.initSimulator(getNextParametersetAsArrayList(null), simulationCounter, false);
        //Wait until Simulator has ended
        support.waitForEndOfSimulator(mySimulator, simulationCounter, 600);
        support.addLinesToLogFileFromListOfParser(mySimulator.getListOfCompletedSimulationParsers(), logFileName);
        this.historyOfParsers = support.appendListOfParsers(historyOfParsers, mySimulator.getListOfCompletedSimulationParsers());
        currentSolution=mySimulator.getListOfCompletedSimulationParsers().get(0);
        Fx=this.getActualDistance(currentSolution);
        
            while(!optimized){
            mySimulator.initSimulator(getNextParametersetAsArrayList(currentSolution.getListOfParameters()), simulationCounter, false);
            support.waitForEndOfSimulator(mySimulator, simulationCounter, 600);
            support.addLinesToLogFileFromListOfParser(mySimulator.getListOfCompletedSimulationParsers(), logFileName);
            this.historyOfParsers = support.appendListOfParsers(historyOfParsers, mySimulator.getListOfCompletedSimulationParsers());
            nextSolution=mySimulator.getListOfCompletedSimulationParsers().get(0);
            Fy=this.getActualDistance(nextSolution);
                
                //If next Solution is better or Probability is above 0.5 then take it as actual best solution
                if((Fy<Fx)||(getProbabylity(Fy, Fx)>0.5)){
                support.log("Choosing next solution for Sim Annealing. Probability over 0.5");
                Fx=Fy;
                currentSolution=nextSolution;
                }else{
                //Count up the Solutions which are not taken
                //After X wrong solutions exit
                wrongSolutionCounter--;
                    if(wrongSolutionCounter<=1){
                    optimized=true;
                    }
                }
            }
        support.log("Simulated Annealing has ended, printing optimal value:");
        support.printOptimizedMeasures(currentSolution, this.listOfMeasures);    
    }


    /**
     * Get probability for chosing the actual parameterset as next solution
     * @return probaility that actual Solution Fy is chosen as the next Fx
     */
    private double getProbabylity(double Fy, double Fx){
    return Math.exp( -(Fy-Fx)/getNextTemperature(SimT) );
    }


    /**
     * Returns the next parameterset in neighborhood
     * Next parameterset is chosen randomly within the neighborhood
     * @param actualParameterset  actual parameterset, if null, then first parameterset is calculated
     * @return next parameterset to be simulated
     */
    private parameter[] getNextParameterset(parameter[] actualParameterset){
    
        if(actualParameterset==null){
        //Calulate first parameterset, the mean value of all parameters, with respect to stepping
        parameter[] newParameterset=support.getCopyOfParameterSet(parameterBase);
            if(this.typeOfNeighbordood==0){
            //For this chooseing strategy, the first element must be minimum
                for(int i=0;i<newParameterset.length;i++){
                    parameter p=newParameterset[i];
                    if(p.getEndValue()>p.getStartValue()){
                    p.setValue(p.getStartValue());
                    }
                }
            return newParameterset;
            }
        
            for(int i=0;i<newParameterset.length;i++){
                parameter p=newParameterset[i];
                if(p.getEndValue()>p.getStartValue()){
                double distance=p.getEndValue()-p.getStartValue();
                distance=Math.round(0.5*distance/p.getStepping())*p.getStepping()+p.getStartValue();
                p.setValue(distance);
                }
            }
        return newParameterset;
        }else{
        parameter[] newParameterset=support.getCopyOfParameterSet(actualParameterset);
        //TODO: 
        //1 Calculate neighborhood for each parameter and choose one of the values randomly
        //2 choose next value randomly from complete design-space
        
            switch(typeOfNeighbordood){
                case 0://0 choose the next neighbor based on stepping forward
                        for(int i=0;i<newParameterset.length;i++){
                        parameter p=newParameterset[i];
                            if(p.getEndValue()>p.getStartValue()){
                            double nextValue=Math.min(p.getValue()+p.getStepping(),p.getEndValue());
                            p.setValue(nextValue);
                            }
                        }
                        break;
                case 1://Step back and forward randomly based on stepping
                        for(int i=0;i<newParameterset.length;i++){
                        parameter p=newParameterset[i];
                            if(p.getEndValue()>p.getStartValue()){
                            double nextValue=0.0;
                                if(Math.random()>=0.5){
                                nextValue=Math.min(p.getValue()+p.getStepping(),p.getEndValue());
                                }else{
                                nextValue=Math.max(p.getValue()-p.getStepping(),p.getStartValue());
                                }
                            p.setValue(nextValue);
                            }
                        }
                        break;
                case 2://Calculate neighborhood and choose next value randomly 
                        for(int i=0;i<newParameterset.length;i++){
                        parameter p=newParameterset[i];
                            if(p.getEndValue()>p.getStartValue()){
                            double nextValue=0.0;
                            double stepCount=(p.getEndValue()-p.getStartValue())/p.getStepping();
                            nextValue=p.getStepping()*Math.round(Math.random()*stepCount*this.sizeOfNeighborhood/100);
                                if(Math.random()>=0.5){
                                nextValue=Math.min(p.getValue()+nextValue,p.getEndValue());
                                }else{
                                nextValue=Math.max(p.getValue()-nextValue,p.getStartValue());
                                }
                            p.setValue(nextValue);
                            }
                        }
                        break;
                case 3://Choose Value randomly out of complete designspace
                        for(int i=0;i<newParameterset.length;i++){
                        parameter p=newParameterset[i];
                            if(p.getEndValue()>p.getStartValue()){
                            double nextValue=0.0;
                            double stepCount=(p.getEndValue()-p.getStartValue())/p.getStepping();
                            nextValue=p.getStartValue() + Math.round(Math.random()*stepCount);
                            p.setValue(nextValue);
                            }
                        }
                    
                        break;
                case 4: //Calculate neighborhood and choose next value randomly, Ignore Stepping!
                        for(int i=0;i<newParameterset.length;i++){
                        parameter p=newParameterset[i];
                            if(p.getEndValue()>p.getStartValue()){
                            double nextValue=0.0;
                            double range=(p.getEndValue()-p.getStartValue());
                            nextValue=Math.round(Math.random()*range*this.sizeOfNeighborhood/100);
                                if(Math.random()>=0.5){
                                nextValue=Math.min(p.getValue()+nextValue,p.getEndValue());
                                }else{
                                nextValue=Math.max(p.getValue()-nextValue,p.getStartValue());
                                }
                            p.setValue(nextValue);
                            }
                        }
                        break;
                default: 
                        //Dont change the parameterset
                        break;
        
            }
        return newParameterset;
        }
    
    }

    
    /**
     * Wrapper, returns next Parameterset as ArrayList with one member
     * @param actualParameterset Base Parameterset to calculate the next one
     * @return ArrayList of Parametersets
     */
    private ArrayList<parameter[]> getNextParametersetAsArrayList(parameter[] actualParameterset){
    ArrayList<parameter[]> myParametersetList=new ArrayList<parameter[]>();
    myParametersetList.add(getNextParameterset(actualParameterset));
    return myParametersetList;
    }
    
    /**
     * Returns the fitness value of actual Paremeterset/Measure
     * Sums up all distances from Measures
     * @return distance (Fx)
     */
    private double getActualDistance(parser p){
    double distance=0;
        for(int measureCount=0;measureCount<listOfMeasures.size();measureCount++){
                MeasureType activeMeasure=p.getMeasureByName(listOfMeasures.get(measureCount).getMeasureName());
                MeasureType activeMeasureFromInterface=listOfMeasures.get(measureCount);//Contains Optimization targets
                activeMeasure.setTargetValue(activeMeasureFromInterface.getTargetValue(), activeMeasureFromInterface.getTargetKindOf());
                    if(activeMeasure.getTargetKindOf().equals("value")){
                    distance=activeMeasure.getDistanceFromTarget();
                    }else{
                        if(activeMeasure.getTargetKindOf().equals("min")){
                        distance=activeMeasure.getMeanValue();
                        }else{
                            if(activeMeasure.getTargetKindOf().equals("max")){
                            distance=0-activeMeasure.getMeanValue();
                            }
                        }
                    }
            }
    return distance;
    }

    /**
     * Calculates the next temperature from max to min
     * The higher SimT is, the lower the temperature is
     */
    private double getNextTemperature(int t){
        if(t>=stepCountTemp-1){
            return 0.001;
        }else{
        
        return (maxTemp - (maxTemp/stepCountTemp)*t);
        }
    }

}
