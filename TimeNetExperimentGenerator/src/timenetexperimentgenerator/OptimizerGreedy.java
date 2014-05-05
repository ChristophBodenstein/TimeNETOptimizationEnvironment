/*
 * Generic Optimizer
 * Needs List of Parameter (Table) and step-sizes, Directory for xml and log files, Implementation of Opti-Algorithm, Path to orininal-file, PAth to Timenet
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/**
 *
 * @author Christoph Bodenstein
 *
 * needs: path to timenet, path for tmp-files, path/filename of original, access to tModel with parameters, access to jTabbedPane with Measurement-forms
 *
 */
public class OptimizerGreedy implements Runnable, Optimizer{
String tmpPath="";
String filename="";//Original filename
String pathToTimeNet="";
MainFrame parent=null;
JTabbedPane MeasureFormPane;
ArrayList<MeasureType> listOfMeasures=new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
ArrayList<parser> historyOfParsers=new ArrayList<parser>();//History of all simulation runs
parameter[] parameterBase;//Base set of parameters, start/end-value, stepping, etc.
float[] arrayOfIncrements;
boolean optimized=false;//False until Optimization is ended
JLabel infoLabel;
float simulationTimeSum=0;
float cpuTimeSum=0;

    /**
     * Constructor
     * 
     */
    OptimizerGreedy(){
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
    arrayOfIncrements=new float[parameterBase.length];
        for(int i=0;i<parameterBase.length;i++){
        arrayOfIncrements[i]=support.getFloat(parameterBase[i].getStepping());
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
                    float arrayOfDistances[][]=new float[listOfMeasures.size()][historyOfParsers.size()];
                    float arrayOfDistanceSums[]=new float[historyOfParsers.size()];

                    //Distance-Sum init
                    for(int historyCount=0;historyCount<historyOfParsers.size();historyCount++){
                        arrayOfDistanceSums[historyCount]=(float)0.0;
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
                        float distance=0;

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
                    if(arrayOfDistanceSums[arrayOfDistanceSums.length-1]<=arrayOfDistanceSums[arrayOfDistanceSums.length-2]){
                    parameter[] newParameterSet=getCopyOfParameterSet(parameterBase);
                    applyArrayOfIncrements(arrayOfIncrements, newParameterSet);
                    returnValue.add(newParameterSet);
                    }else{
                    //Gesamtdistanz des letzten Wertes ist nicht kleiner --> lokales Minimum gefunden
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

                    parameter[] newParameterSet=getCopyOfParameterSet(this.parameterBase);
                    //Für alle Parameter, die keine Externen Parameter sind erhöhe um Step

                        for(int i=0;i<newParameterSet.length;i++){
                        arrayOfIncrements[i]=0;}

                        for(int i=0;i<newParameterSet.length;i++){
                            if(!newParameterSet[i].isExternalParameter()){

                                if(support.getFloat(newParameterSet[i].getValue())<support.getFloat(newParameterSet[i].getEndValue())){                                
                                arrayOfIncrements[i]=support.getFloat(newParameterSet[i].getStepping());                                
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
    void applyArrayOfIncrements(float[] arrayOfIncrements, parameter[] newParameterSet){
    support.log("Applying Array of Increments.");
        for(int i=0;i<newParameterSet.length;i++){
        System.out.print(newParameterSet[i].getName()+"="+newParameterSet[i].getValue()+" will be incremented by: "+arrayOfIncrements[i]+" and is now:");
        newParameterSet[i].setValue(  support.getString(support.round( Math.min(arrayOfIncrements[i]+support.getFloat(newParameterSet[i].getValue()),support.getFloat(newParameterSet[i].getEndValue()))) ) );
        support.log(newParameterSet[i].getValue());
        }
    this.parameterBase=newParameterSet;
    }

    /**
     * Creates and returns a set of Parameters made by deep-copying
     * @param parameterBase the array of parameters to be dublicated
     * @return array of parameters, the copy of input
     */
    parameter[] getCopyOfParameterSet(parameter[] parameterBase){
        parameter[] newParameterSet=new parameter[parameterBase.length];
        for(int i=0;i<parameterBase.length;i++){

        newParameterSet[i]=new parameter();
        newParameterSet[i].setName(parameterBase[i].getName());
        newParameterSet[i].setStartValue(parameterBase[i].getStartValue());
        newParameterSet[i].setStepping(parameterBase[i].getStepping());
        newParameterSet[i].setEndValue(parameterBase[i].getEndValue());
        //newParameterSet[i].setValue(Float.toString((historyOfParsers.get(historyOfParsers.size()-1)).getMeasureValueByMeasureName(parameterBase[i].getName())));
        newParameterSet[i].setValue(parameterBase[i].getValue());
        }
    return newParameterSet;
    }


    /**
     * Run method, the main optimization loop
     */
    public void run() {
    ArrayList<parameter[]> mySimulationList=getNextSimulations(historyOfParsers);
    int simulationCounter=0;
    String logFileName=this.tmpPath+File.separator+"SimLog"+Calendar.getInstance().getTimeInMillis()+"ALL"+".csv";
        try{
            while(mySimulationList.size()>0){
            //batchSimulator myBatchSimulator = new batchSimulator(mySimulationList, this.filename, this.infoLabel, parent);
            support.log("Retrieve Simulator.");
            Simulator myGenericSimulator=SimOptiFactory.getSimulator();
            support.log("init Simulator.");
            myGenericSimulator.initSimulator(mySimulationList, simulationCounter);
            support.log("wait for Simulator has 100% completed.");
            this.infoLabel.setText("Simulations started.");
                while(myGenericSimulator.getStatus()<100){
                Thread.sleep(1000);
                this.infoLabel.setText("Done "+ myGenericSimulator.getStatus() +"% ");
                simulationCounter=myGenericSimulator.getSimulationCounter();
                this.parent.updateSimulationCounterLabel(simulationCounter);
                System.out.print("Simulation status:"+myGenericSimulator.getStatus() +"%");
                support.log("Simulation Counter: "+simulationCounter);
                }
                this.infoLabel.setText("Done "+ myGenericSimulator.getStatus() +"%");
                support.log("Size of Simulation-Result-List: "+myGenericSimulator.getListOfCompletedSimulationParsers().size());
                if(myGenericSimulator.getListOfCompletedSimulationParsers().size()>0){
                historyOfParsers.addAll(myGenericSimulator.getListOfCompletedSimulationParsers());
                support.addLinesToLogFileFromListOfParser(myGenericSimulator.getListOfCompletedSimulationParsers(), logFileName);
                }

            mySimulationList=getNextSimulations(historyOfParsers);
            }
            printStatistics();
            this.infoLabel.setText("Optimization ended. See Log.");
        }catch(InterruptedException e){
        support.log("InterruptedException in main loop of optimization. Optimization aborted.");
        this.infoLabel.setText("Aborted / Error");
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * prints some statistics of optimization run
     */
    private void printStatistics() {
    this.simulationTimeSum=0;
    this.cpuTimeSum=0;
        for (parser historyOfParser : historyOfParsers) {
            this.simulationTimeSum += historyOfParser.getSimulationTime();
            this.cpuTimeSum += historyOfParser.getCPUTime();
        }
    support.log(this.cpuTimeSum+" sec of CPU-Time used for "+historyOfParsers.size()+" Simulations with "+ this.simulationTimeSum+" Simulation steps.");
    }


}
