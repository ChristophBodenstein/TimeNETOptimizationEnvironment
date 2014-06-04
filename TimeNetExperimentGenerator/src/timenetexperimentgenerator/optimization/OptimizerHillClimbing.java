/*
 * Generic Optimizer
 * Needs List of Parameter (Table) and step-sizes, Directory for xml and log files, Implementation of Opti-Algorithm, Path to orininal-file, PAth to Timenet
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
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.SimOptiFactory;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.parser;
import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein
 *
 * needs: path to timenet, path for tmp-files, path/filename of original, access to tModel with parameters, access to jTabbedPane with Measurement-forms
 *
 */
public class OptimizerHillClimbing implements Runnable, Optimizer{
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
String logFileName="";
int abortLimit=5;//4 time the same calculated distance is allowed, else break and end the optimization
int abortCounter=abortLimit;

    /**
     * Constructor
     * 
     */
    public OptimizerHillClimbing(){
    logFileName=support.getTmpPath()+File.separator+"Optimizing_with_HillClimbing"+Calendar.getInstance().getTimeInMillis()+"ALL"+".csv";
    }

    /**
     * Init the optimization, loads the default values and targets from support-class and starts optimization
     *
     */
    public void initOptimizer(){
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
        support.log("Parameterbase for Parameter " + parameterBase[i].getName() + " is " + parameterBase[i].getValue());
        }
  
    this.filename=support.getOriginalFilename();// originalFilename;
    //Ask for Tmp-Path
    
    this.tmpPath=support.getTmpPath();
    //Start this Thread
    new Thread(this).start();
    }


    /**
     * Creates new List of Parametersets to be simulated, based on actual history of simulation-results
     * @param historyOfParsers history of Simulation-runs, stored as parser-objects
     * @return List of Parametersets to be simulated next
     */
    public ArrayList<parameter[]> getNextSimulations(ArrayList<parser> historyOfParsers){
    ArrayList<parameter[]> returnValue=new ArrayList<parameter[]>();
    MeasureType activeMeasure=null;
    MeasureType lastActiveMeasure=null;
    MeasureType activeMeasureFromInterface;
        //If history is empty, add parameterbase as startValue
        if(historyOfParsers.size()<1){
        returnValue.add(this.parameterBase);
        }   else{
                //Wenn history mind. 2 lang ist, dann
                if(historyOfParsers.size()>=3){
                    //ArrayList<MeasureType> listOfLastMeasures=historyOfParsers.get(historyOfParsers.size()-1).getMeasures();
                    double arrayOfDistances[][]=new double[listOfMeasures.size()][historyOfParsers.size()];
                    double arrayOfDistanceSums[]=new double[historyOfParsers.size()];

                    //Distance-Sum init
                    for(int historyCount=0;historyCount<historyOfParsers.size();historyCount++){
                        arrayOfDistanceSums[historyCount]=0.0;
                    }

                    //Liste aller Abstände für alle aktiven Measures auslesen
                    support.log("Counting all Distances for "+listOfMeasures.size()+" Measures.");

                    for(int measureCount=0;measureCount<listOfMeasures.size();measureCount++){
                    //listOfMeasures.get(i).setMeanValue((float) (   historyOfParsers.get(historyOfParsers.size()-1).getMeasureValueByMeasureName(listOfMeasures.get(i).getMeasureName())) );
                        for(int historyCount=0;historyCount<historyOfParsers.size();historyCount++){
                        lastActiveMeasure=activeMeasure;
                        activeMeasure=historyOfParsers.get(historyCount).getMeasureByName(listOfMeasures.get(measureCount).getMeasureName());
                        activeMeasureFromInterface=listOfMeasures.get(measureCount);//Contains Optimization targets
                        //float measureValue=historyOfParsers.get(historyCount).getMeasureValueByMeasureName(activeMeasure.getMeasureName());
                        activeMeasure.setTargetValue(activeMeasureFromInterface.getTargetValue(), activeMeasureFromInterface.getTargetKindOf());
                        double distance=0;

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

                        arrayOfDistances[measureCount][historyCount]=distance;//Distanz jedes einzelnen Measures jedes einzelnen Historypunktes
                        arrayOfDistanceSums[historyCount]+=distance;//Gesamtdistanz summieren
                        support.log("Distance of Measure # "+measureCount +" at History # "+historyCount +" is:"+distance);
                        support.log("DistanceSum #"+historyCount +":"+arrayOfDistanceSums[historyCount] + " for all Measures.");
                        }
                    }
                    //Jetzt ist für jeden History-Punkt die Distanz aller gewählten Measures berechnet.
                        
                    //Greedy, wenn gesamtdistanz jetzt kleiner als eben ist, dann appliziere den gleichen Inkrement-Vektor nochmal
                    if((arrayOfDistanceSums[arrayOfDistanceSums.length-1]<=arrayOfDistanceSums[arrayOfDistanceSums.length-2])&&(abortCounter>=1)){
                    parameter[] newParameterSet=support.getCopyOfParameterSet(parameterBase);
                    applyArrayOfIncrements(arrayOfIncrements, newParameterSet);
                    returnValue.add(newParameterSet);
                    
                        //If sum distance is equal, then count up the abort-counter
                        if(arrayOfDistanceSums[arrayOfDistanceSums.length-1]==arrayOfDistanceSums[arrayOfDistanceSums.length-2]){
                        abortCounter--;
                        }
                    
                    }else{
                    //Gesamtdistanz des letzten Wertes ist nicht kleiner --> lokales Minimum gefunden
                    if(abortCounter<abortLimit){
                    //TODO works only for first Measure to optimize
                    lastActiveMeasure=this.historyOfParsers.get(this.historyOfParsers.size()-abortLimit-1).getMeasureByName(listOfMeasures.get(0).getMeasureName());
                    }
                    support.log("******Local minimum found!*****");
                    support.printMeasureType(lastActiveMeasure, "", "");
                    optimized=true;//Abbruch der Optimierung
                    }

                    //Greedy-Vorgehen, wenn letzter Wert besser als vorheriger, dann weiter
                    /*for(int measureCount=0;measureCount<listOfMeasures.size();measureCount++){
                        if(arrayOfDistances[measureCount][arrayOfDistances.length-1]<arrayOfDistances[measureCount][arrayOfDistances.length-2]){
                        //Letzter Abstand ist kleiner als vorletzter, also weiter mit diesen Increments

                        }else{
                        //Abbruch, Stepping auf 0 setzen und nächsten Paramaterincrement setzen
                        //Suche, welches Incrment ungleich 0 ist, dieses auf 0 setzen und dann von dort weitergehen
                            //nächstes mögliche increment setzen und Parameter setzen

                        arrayOfIncrements[]

                                //Wenn alle increments nur noch 0 sein können, dann ende
                                //Es wird immer einer der folgenden Parameter geändert, niemals zurückgesprungen!
                        }
                    }
                     *
                     */
                    //letztes Element nehmen
                    //Nach Zielwerten kucken, ist der Abstand des Zielwertes vom vorletzten zum letzten kleiner geworden
                        //Dann kucken, welcher Parameter wurde geändert und weiter ändern in gleiche Richtung
                    //Sonst diesen Parameter nicht weiter verändern und vorherigen Wert nehmen

                    //kucken, welche Parameter noch verändert werden können und ändern
                }   else{
                    // History of Parsers has less then 3 member, create next one

                    parameter[] newParameterSet=support.getCopyOfParameterSet(this.parameterBase);
                    //Für alle Parameter, die keine Externen Parameter sind erhöhe um Step

                        for(int i=0;i<newParameterSet.length;i++){
                        arrayOfIncrements[i]=0;}

                        for(int i=0;i<newParameterSet.length;i++){
                            if(!newParameterSet[i].isExternalParameter()){

                                if(support.getDouble(newParameterSet[i].getValue())<support.getDouble(newParameterSet[i].getEndValue())){
                                arrayOfIncrements[i]=support.getDouble(newParameterSet[i].getStepping());
                                break;//Abbruch nach erstem änderbaren Parameter
                                }
                            }
                        }
                    applyArrayOfIncrements(arrayOfIncrements, newParameterSet);
                    returnValue.add(newParameterSet);
                    }

            }



    return returnValue;
    }

    /**
    * Applies a list of increments to a list of parameters, used in creating the next parameter-set
    * the new parameterset is the "global" variable parameterBase
    * @param arrayOfIncrements Array of values which will be applied to array of parameters
    * @param newParameterSet Array of parameters which will be increased by values from arrayOfIncrements
    * 
    */
    void applyArrayOfIncrements(double[] arrayOfIncrements, parameter[] newParameterSet){
    support.log("Applying Array of Increments.");
        for(int i=0;i<newParameterSet.length;i++){
        support.log(newParameterSet[i].getName()+"="+newParameterSet[i].getValue()+" will be incremented by: "+arrayOfIncrements[i]+" and is now:");
        newParameterSet[i].setValue((support.round( Math.min(arrayOfIncrements[i]+support.getDouble(newParameterSet[i].getValue()),support.getDouble(newParameterSet[i].getEndValue()))) ) );
        support.log(Double.toString(newParameterSet[i].getValue()));
        }
    this.parameterBase=newParameterSet;
    }




    /**
     * Run method, the main optimization loop
     */
    public void run() {
    ArrayList<parameter[]> mySimulationList=getNextSimulations(historyOfParsers);
    int simulationCounter=0;
    
            while(mySimulationList.size()>0){
            //batchSimulator myBatchSimulator = new batchSimulator(mySimulationList, this.filename, this.infoLabel, parent);
            Simulator myGenericSimulator=SimOptiFactory.getSimulator();
            myGenericSimulator.initSimulator(mySimulationList, simulationCounter, false);
                
                support.waitForEndOfSimulator(myGenericSimulator, simulationCounter, 6000);
                support.log("Simulation Counter: "+simulationCounter);
                simulationCounter=myGenericSimulator.getSimulationCounter();
                
                support.log("Size of Simulation-Result-List: "+myGenericSimulator.getListOfCompletedSimulationParsers().size());
                if(myGenericSimulator.getListOfCompletedSimulationParsers().size()>0){
                historyOfParsers.addAll(myGenericSimulator.getListOfCompletedSimulationParsers());
                support.addLinesToLogFileFromListOfParser(myGenericSimulator.getListOfCompletedSimulationParsers(), logFileName);
                }

            mySimulationList=getNextSimulations(historyOfParsers);
            }
            printStatistics();
            this.infoLabel.setText("Optimization ended. See Log.");
       
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * prints some statistics of optimization run
     */
    private void printStatistics() {
    this.simulationTimeSum=0;
    this.cpuTimeSum=0;
        for (parser historyOfParser : historyOfParsers) {
            this.simulationTimeSum += historyOfParser.getMeasures().get(0).getSimulationTime();
            this.cpuTimeSum += historyOfParser.getMeasures().get(0).getCPUTime();
        }
    support.log(this.cpuTimeSum+" sec of CPU-Time used for "+historyOfParsers.size()+" Simulations with "+ this.simulationTimeSum+" Simulation steps.");
    }


}
