/*
 * Optimizer using the hill climbing algorithm, its a modified and configured version of simmulated annealing
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
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.helper.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class OptimizerHill implements Runnable, Optimizer{
private int SimI=1,SimT=0;
private double maxTemp=20;
private int stepCountTemp=100;
private double sizeOfNeighborhood=10;//in percent
private double Fx;//Current Distance
private double Fy;//New Distance?
private int typeOfNeighborhood=0;

private int simulationCounter=0;
SimulationType currentSolution;
SimulationType nextSolution;
String tmpPath="";
String filename="";//Original filename
String pathToTimeNet="";
MainFrame parent=null;
JTabbedPane MeasureFormPane;
ArrayList<MeasureType> listOfMeasures=new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
ArrayList<SimulationType> historyOfParsers=new ArrayList<SimulationType>();//History of all simulation runs
ArrayList<parameter> parameterBase;//Base set of parameters, start/end-value, stepping, etc.
double[] arrayOfIncrements;
boolean optimized=false;//False until Optimization is ended
JLabel infoLabel;
double simulationTimeSum=0;
double cpuTimeSum=0;
String logFileName;
int wrongSolutionCounter=support.DEFAULT_WRONG_SOLUTIONS_IN_A_ROW;
int wrongSolutionPerDirectionCounter=support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION;
//int numberOfLastChangedParameter=0;
int numberOfChangableParameters=0;
boolean directionOfOptimization=true;//true->increment parameters, false->decrement parameters
boolean directionOfOptimizationChanged=false;//True->direction already changed, False->you can change it one time

    /**
     * Constructor
     *
     */
    public OptimizerHill() {
    logFileName=support.getTmpPath()+File.separator+"Optimizing_with_HillClimbing"+Calendar.getInstance().getTimeInMillis()+".csv";
    support.log("LogfileName:"+logFileName);
    this.wrongSolutionCounter=support.DEFAULT_WRONG_SOLUTIONS_IN_A_ROW;
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
    arrayOfIncrements=new double[parameterBase.size()];
        for(int i=0;i<parameterBase.size();i++){
        arrayOfIncrements[i]=support.getDouble(parameterBase.get(i).getStepping());
        }

    this.filename=support.getOriginalFilename();// originalFilename;
    //Ask for Tmp-Path

    this.tmpPath=support.getTmpPath();
    //Start this Thread
    new Thread(this).start();
    }

    public void run() {
        ArrayList<parameter> lastParameterset;
        //Simulator init with initional parameterset
        Simulator mySimulator=SimOptiFactory.getSimulator();
        
        mySimulator.initSimulator(getNextParametersetAsArrayList(null), simulationCounter, false);
        //Wait until Simulator has ended
        support.waitForEndOfSimulator(mySimulator, simulationCounter, 600);
        support.addLinesToLogFileFromListOfParser(mySimulator.getListOfCompletedSimulationParsers(), logFileName);
        this.historyOfParsers = support.appendListOfParsers(historyOfParsers, mySimulator.getListOfCompletedSimulationParsers());
        currentSolution=mySimulator.getListOfCompletedSimulationParsers().get(0);
        Fx=this.getActualDistance(currentSolution);
        nextSolution=currentSolution;
        lastParameterset=currentSolution.getListOfParameters();
        
            while(!optimized){
            mySimulator.initSimulator(getNextParametersetAsArrayList(lastParameterset), simulationCounter, false);
            support.waitForEndOfSimulator(mySimulator, simulationCounter, 600);
            this.simulationCounter=mySimulator.getSimulationCounter();
            support.addLinesToLogFileFromListOfParser(mySimulator.getListOfCompletedSimulationParsers(), logFileName);
            this.historyOfParsers = support.appendListOfParsers(historyOfParsers, mySimulator.getListOfCompletedSimulationParsers());
            nextSolution=mySimulator.getListOfCompletedSimulationParsers().get(0);
            Fy=this.getActualDistance(nextSolution);
            lastParameterset=nextSolution.getListOfParameters();
                //If next Solution is better then take it as actual best solution
                if((Fy<Fx)){
                support.log("Choosing next solution for Hill Climbing");
                Fx=Fy;
                currentSolution=nextSolution;
                //Reset wrong-solution-counter
                wrongSolutionCounter=support.DEFAULT_WRONG_SOLUTIONS_IN_A_ROW;
                wrongSolutionPerDirectionCounter=support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION;
                }else{
                nextSolution=null;
                //Count up the Solutions which are not taken
                //After X wrong solutions exit
                support.log("Distance was higher, Solution not chosen. Counting up wrong-solution-counter.");
                wrongSolutionCounter--;
                    if(wrongSolutionCounter<=1){
                    optimized=true;
                    }
                }
            //nextSolution is not null --> We are on the right way, else we are wrong and should change
            }
        support.log("Hill Climbing has ended, printing optimal value:");
        support.addLinesToLogFile(currentSolution, logFileName);
        support.getStatusLabel().setText("Optimization ended. See Log.");
        support.printOptimizedMeasures(currentSolution, this.listOfMeasures);
        StatisticAggregator.printLastStatistic();
    }


    /**
     * Returns the next parameterset in neighborhood
     * Next parameterset is chosen randomly within the neighborhood
     * @param actualParameterset  actual parameterset, if null, then first parameterset is calculated
     * @return next parameterset to be simulated
     */
    private ArrayList<parameter> getNextParameterset(ArrayList<parameter> actualParameterset){
    ArrayList<parameter> newParameterset=support.getCopyOfParameterSet(parameterBase);
    ArrayList<parameter> listOfChangableParameters=new ArrayList<parameter>(); 
        //Count the number of changable parameters
        this.numberOfChangableParameters=0;
        for(int i=0;i<newParameterset.size();i++){
                parameter p=newParameterset.get(i);
                if(p.isIteratableAndIntern()){
                this.numberOfChangableParameters++;
                listOfChangableParameters.add(p);
                }
            }
    
        if(actualParameterset==null){
            //calculate the first parameterset
            switch(support.getTypeOfStartValue()){
            
                case start:
                        support.log("Taking Min-Values as Start for every Parameter.");
                            //Calculate first parameterset, set every parameter to start-value
                            //For this choosing strategy, the first element must be minimum
                            for(int i=0;i<newParameterset.size();i++){
                            parameter p=newParameterset.get(i);
                                if(p.isIteratableAndIntern()){
                                p.setValue(p.getStartValue());
                                }
                            }
                            break;
                case middle:
                        support.log("Taking Middle-Values as Start for every Parameter.");
                            //Calulate first parameterset, the mean value of all parameters, with respect to stepping
                            for(int i=0;i<newParameterset.size();i++){
                                parameter p=newParameterset.get(i);
                                if(p.isIteratableAndIntern()){
                                double distance=p.getEndValue()-p.getStartValue();
                                distance=Math.round(0.5*distance/p.getStepping())*p.getStepping()+p.getStartValue();
                                p.setValue(distance);
                                }
                            }
                            break;
                case end:
                        support.log("Taking Max-Values as Start for every Parameter.");
                            //Calculate first parameterset, set every parameter to end-value
                            //For this choosing strategy, the first element must be minimum
                            for(int i=0;i<newParameterset.size();i++){
                            parameter p=newParameterset.get(i);
                                if(p.isIteratableAndIntern()){
                                p.setValue(p.getEndValue());
                                }
                            }
                            break;
                case random:
                        support.log("Taking Random-Values as Start for every Parameter.");
                            //Calulate first parameterset, the random value of all parameters, with respect to stepping
                            for(int i=0;i<newParameterset.size();i++){
                                parameter p = newParameterset.get(i);
                                if(p.isIteratableAndIntern()){
                                double distance=p.getEndValue()-p.getStartValue();
                                double rnd=Math.random();
                                distance=Math.round(rnd*distance/p.getStepping())*p.getStepping()+p.getStartValue();
                                p.setValue(distance);
                                }
                            }
                            break;
            
            }
            return newParameterset;
            
        
        }else{
        //First parameterset exists, calculate the next
        newParameterset=support.getCopyOfParameterSet(actualParameterset);
        //TODO: 
        //1 Calculate neighborhood for each parameter and choose one of the values randomly
        //2 choose next value randomly from complete design-space
        int numberOfParameterToBeChanged=0;//Default, change the first changeable parameter
        int numberOfLastParameter=-1;//Number of last parameter that was changed(in an Array of changable parameters)
            //Check, which parameters can be changed
            if(listOfChangableParameters.size()>1){
            //support.log("List of Changable Parameters has size: "+listOfChangableParameters.size());
            //TODO: Sort List of Parameters (new method in support)    
            SimulationType lastParser=currentSolution;
            //this.historyOfParsers.get(this.historyOfParsers.size()-1);
            
//support.log("History of Parsers has size: "+this.historyOfParsers.size());
            
            ArrayList<parameter> lastParameterList=lastParser.getListOfParameters();
            //For ever Parameter check if it is iteratable and if it was changed last time
            int i=0;
            numberOfLastParameter=-1;//Number of last parameter that was changed(in an Array of changable parameters)
                for(i=0;i<lastParameterList.size();i++){
                    if(lastParameterList.get(i).isIteratableAndIntern()){
                    numberOfLastParameter++;
                    support.log("Iteratable Parameter with number "+numberOfLastParameter+" found.");
                        /*if it was changed, then break, and numberOfLastParameter contains the number of last changed parameter in array of changable parameters*/
                        if(lastParameterList.get(i)!=actualParameterset.get(i)){
                        break;
                        }
                    }
                    
                }
            // At this point, numberOfLastParameter contains the number of last changed parameter in an array of all changeable parameters
            support.log("Number of Last changed Parameter is "+numberOfLastParameter);
            
                if(nextSolution==null){
                support.log("Last Solution was not better then overlast solution. Counting up Wrong Solutions in one dircetion.");
                wrongSolutionPerDirectionCounter--;
                }
            
                if(wrongSolutionPerDirectionCounter<=0){
                wrongSolutionPerDirectionCounter=support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION;
                
                support.log(support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION+" wrong solutions in one direction.");
                    if(this.directionOfOptimization){
                    //Switch direction of Optimization but chnge the same old parameter
                    support.log("Changing direction of Optimization to false(backwards).");
                    this.directionOfOptimization=false;
                    numberOfParameterToBeChanged=numberOfLastParameter;
                    }   else{
                        support.log("Changing direction of Optimization back to true(forward). Taking next parameter to change.");
                        this.directionOfOptimization=true;
                        newParameterset=currentSolution.getListOfParameters();
                        //Select next Parameter to be changed with round-robin
                        numberOfParameterToBeChanged=numberOfLastParameter+1;
                        if(numberOfParameterToBeChanged>=listOfChangableParameters.size()){
                        numberOfParameterToBeChanged=0;
                        
                        //Reset the wrong solution-counter
                        this.wrongSolutionPerDirectionCounter=support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION;
                        
                        }
                        support.log("Last changed Parameter was: "+numberOfLastParameter+", next Parameter to be changed is "+numberOfParameterToBeChanged);
                        support.log("There are "+ listOfChangableParameters.size() +" parameters in list to be changed.");
                    }
                }else{
                //Select old parameter to be changed again
                    support.log("Changing again parameter "+ numberOfLastParameter);
                    numberOfParameterToBeChanged=numberOfLastParameter;
                }
            
            }else{
            numberOfParameterToBeChanged=0;
            }
            
            //Select new Parameter to be changed and change it!
            //Get Parameter by name
            String nameOfParameterToBeChanged=listOfChangableParameters.get(numberOfParameterToBeChanged).getName();
            boolean incResult=false;
            support.log("Number of Parameter to be changed "+numberOfParameterToBeChanged);
            support.log("Name of Parameter to be changed: "+nameOfParameterToBeChanged);
            switch(typeOfNeighborhood){
                case 0://0 choose the next neighbor based on stepping forward
                        //Inc this parameter by standard-increment
                        incResult=support.getParameterByName(newParameterset, nameOfParameterToBeChanged).incDecValue(this.directionOfOptimization);
                        break;
                case 1://Step back and forward randomly based on stepping
                        for(int i=0;i<newParameterset.size();i++){
                        parameter p=newParameterset.get(i);
                            if(p.isIteratableAndIntern()){
                            double nextValue=0.0;
                                if(Math.random()>=0.5){
                                incResult=support.getParameterByName(newParameterset, nameOfParameterToBeChanged).incValue();
                                }else{
                                incResult=support.getParameterByName(newParameterset, nameOfParameterToBeChanged).decValue();
                                }
                            p.setValue(nextValue);
                            }
                        }
                        break;
                case 2://Calculate neighborhood and choose next value randomly 
                        for(int i=0;i<newParameterset.size();i++){
                        parameter p=newParameterset.get(i);
                            if(p.isIteratableAndIntern()){
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
                        for(int i=0;i<newParameterset.size();i++){
                        parameter p=newParameterset.get(i);
                            if(p.isIteratableAndIntern()){
                            double nextValue=0.0;
                            double stepCount=(p.getEndValue()-p.getStartValue())/p.getStepping();
                            nextValue=p.getStartValue() + Math.round(Math.random()*stepCount);
                            p.setValue(nextValue);
                            }
                        }
                    
                        break;
                case 4: //Calculate neighborhood and choose next value randomly, Ignore Stepping!
                        for(int i=0;i<newParameterset.size();i++){
                        parameter p=newParameterset.get(i);
                            if(p.isIteratableAndIntern()){
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
        
            if(incResult){
            support.log("Parameter could be incremented.(or decremented)");
            }else{
            support.log("Parameter could NOT be incremented.(or decremented). Set WrongSolutionsPerDirectionCounter to 0.");
            wrongSolutionPerDirectionCounter=0;
            /*this.directionOfOptimization=!this.directionOfOptimization;
            wrongSolutionPerDirectionCounter=support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION;
            wrongSolutionCounter=support.DEFAULT_WRONG_SOLUTIONS_IN_A_ROW;
            newParameterset=currentSolution.getListOfParameters();//Reset to last good parameterset
                if(this.directionOfOptimizationChanged){
                //Change to new parameter to be changed
                //Select next Parameter to be changed with round-robin
                        numberOfParameterToBeChanged=numberOfLastParameter+1;
                        if(numberOfParameterToBeChanged>=listOfChangableParameters.size()){
                        numberOfParameterToBeChanged=0;
                        }
                this.directionOfOptimizationChanged=false;
                }else{
                    
                    this.directionOfOptimizationChanged=true;
                    }
            */
            }
        return newParameterset;
        }
    
    }

    
    /**
     * Wrapper, returns next Parameterset as ArrayList with one member
     * @param actualParameterset Base Parameterset to calculate the next one
     * @return ArrayList of Parametersets
     */
    private ArrayList< ArrayList<parameter> > getNextParametersetAsArrayList(ArrayList<parameter> actualParameterset){
    ArrayList< ArrayList<parameter> > myParametersetList=new ArrayList< ArrayList<parameter> >();
    myParametersetList.add(getNextParameterset(actualParameterset));
    return myParametersetList;
    }
    
    /**
     * Returns the fitness value of actual Paremeterset/Measure
     * Sums up all distances from Measures
     * @return distance (Fx)
     */
    private double getActualDistance(SimulationType p){
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