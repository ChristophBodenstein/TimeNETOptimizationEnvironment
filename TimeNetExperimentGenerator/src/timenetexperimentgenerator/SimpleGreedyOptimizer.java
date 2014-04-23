/*
 * Generic Optimizer
 * Needs List of Parameter (Table) and step-sizes, Directory for xml and log files, Implementation of Opti-Algorithm, Path to orininal-file, PAth to Timenet
 * 
 */

package timenetexperimentgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class SimpleGreedyOptimizer implements Runnable, Optimizer{
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

    /**
     * Constructor
     * 
     */
    SimpleGreedyOptimizer(){
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
    System.out.println("# of Measures to be optimized: "+this.listOfMeasures.size());

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
    MeasureType activeMeasureFromInterface=null;
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
                    System.out.println("Counting all Distances for "+listOfMeasures.size()+" Measures.");

                    for(int measureCount=0;measureCount<listOfMeasures.size();measureCount++){
                    //listOfMeasures.get(i).setMeanValue((float) (   historyOfParsers.get(historyOfParsers.size()-1).getMeasureValueByMeasureName(listOfMeasures.get(i).getMeasureName())) );
                        for(int historyCount=0;historyCount<historyOfParsers.size();historyCount++){
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
                        System.out.println("Distance of Measure # "+measureCount +" at History # "+historyCount +" is:"+distance);
                        System.out.println("DistanceSum #"+historyCount +":"+arrayOfDistanceSums[historyCount] + " for all Measures.");
                        }
                    }
                    //Jetzt ist für jeden History-Punkt die Distanz aller gewählten Measures berechnet.
                        
                    //Greedy, wenn gesamtdistanz jetzt kleiner als eben ist, dann appliziere den gleichen Inkrement-Vektor nochmal
                    if(arrayOfDistanceSums[arrayOfDistanceSums.length-1]<arrayOfDistanceSums[arrayOfDistanceSums.length-2]){
                    parameter[] newParameterSet=getCopyOfParameterSet(parameterBase);
                    applyArrayOfIncrements(arrayOfIncrements, newParameterSet);
                    returnValue.add(newParameterSet);
                    }else{
                    //Gesamtdistanz des letzten Wertes ist nicht kleiner --> lokales Minimum gefunden
                    System.out.println("******Local minimum found!*****");
                    support.printMeasureType(activeMeasure, "", "");
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
    System.out.println("Applying Array of Increments.");
        for(int i=0;i<newParameterSet.length;i++){
        System.out.print(newParameterSet[i].getName()+"="+newParameterSet[i].getValue()+" will be incremented by: "+arrayOfIncrements[i]+" and is now:");
        newParameterSet[i].setValue(support.getString(Math.min(arrayOfIncrements[i]+support.getFloat(newParameterSet[i].getValue()),support.getFloat(newParameterSet[i].getEndValue())) ) );
        System.out.println(newParameterSet[i].getValue());
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
            System.out.println("Retrieve Simulator.");
            Simulator myGenericSimulator=SimOptiFactory.getSimulator();
            System.out.println("init Simulator.");
            myGenericSimulator.initSimulator(mySimulationList, simulationCounter);
            System.out.println("wait for Simulator has 100% completed.");
                while(myGenericSimulator.getStatus()<100){
                Thread.sleep(500);
                this.infoLabel.setText("Done "+ myGenericSimulator.getStatus() +"%");
                simulationCounter=myGenericSimulator.getSimulationCounter();
                this.parent.setSimulationCounter(simulationCounter);
                System.out.print("Simulation status:"+myGenericSimulator.getStatus() +"%");
                System.out.println("Simulation Counter: "+simulationCounter);
                }
                
                System.out.println("Size of Simulation-Result-List: "+myGenericSimulator.getListOfCompletedSimulationParsers().size());
                if(myGenericSimulator.getListOfCompletedSimulationParsers().size()>0){
                historyOfParsers.addAll(myGenericSimulator.getListOfCompletedSimulationParsers());
                addLinesToLogFileFromListOfParser(myGenericSimulator.getListOfCompletedSimulationParsers(), logFileName);
                }

            mySimulationList=getNextSimulations(historyOfParsers);
            }
        }catch(InterruptedException e){
        System.out.println("InterruptedException in main loop of optimization. Optimization aborted.");
        this.infoLabel.setText("Aborted / Error");
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Adds Lines to logfile with the data from given parserlist
     * @param pList List of parsers, which includes the data from one simulation each
     * @param logFileName The path and name of the general log file
     */
    private void addLinesToLogFileFromListOfParser(ArrayList<parser> pList, String logFileName){
    boolean writeHeader=false;
    String line;
        try{
        System.out.println("Logfilename is:"+logFileName);
        //Öffnen des Logfiles und Schreiben der ersten Zeile

        File f=new File(logFileName);
        if(!f.exists()){writeHeader=true;}
        FileWriter fw= new FileWriter(logFileName, true);

            if(writeHeader){
                MeasureType exportMeasure=pList.get(0).getMeasures().get(0);//Dummy, es wird das erste Measure abgefragt und die Paramsterliste
                line="MeasureName;Mean Value; Variance; Conf.Interval-Min;Conf.Interval-Max;Epsilon;"+"Simulation Time";
                    for(int i1=0;i1<exportMeasure.getParameterList().size();i1++){
                    line=line+";"+exportMeasure.getParameterList().get(i1).getName();
                    }
                    try {
                        fw.write(line);
                    } catch (IOException ex) {
                        System.out.println("Error writing Header to Summary-log-file.");
                    }
            }


            for(int i=0;i<pList.size();i++){
            parser myParser=pList.get(i);
              try{
              //fw.write(line);
              //fw.append( System.getProperty("line.separator") );
                for(int i1=0;i1<myParser.getMeasures().size();i1++){//Alle Measure schreiben
                MeasureType exportMeasure=myParser.getMeasures().get(i1);
                /*System.out.println("Mean Value= "+support.getCommaFloat(exportMeasure.getMeanValue()));
                System.out.println("Variance= "+support.getCommaFloat(exportMeasure.getVariance()));
                System.out.println("Confidence-Min= "+support.getCommaFloat(exportMeasure.getConfidenceInterval()[0]));
                System.out.println("Confidence-Max= "+support.getCommaFloat(exportMeasure.getConfidenceInterval()[1]));
                System.out.println("Epsilon= "+support.getCommaFloat(exportMeasure.getEpsilon()));
                System.out.println("Simulation-Time= "+support.getCommaFloat(myParser.getSimulationTime()));
                */
                line=exportMeasure.getMeasureName()+";"+support.getCommaFloat(exportMeasure.getMeanValue())+";"+support.getCommaFloat(exportMeasure.getVariance())+";"+support.getCommaFloat(exportMeasure.getConfidenceInterval()[0])+";"+support.getCommaFloat(exportMeasure.getConfidenceInterval()[1])+";"+support.getCommaFloat(exportMeasure.getEpsilon())+";"+support.getCommaFloat(myParser.getSimulationTime());
                    for(int c=0;c<exportMeasure.getParameterList().size();c++){
                    line=line+";"+support.getCommaFloat(exportMeasure.getParameterList().get(c).getValue());
                    }
                fw.write(line);
                fw.append( System.getProperty("line.separator") );
                }
              }catch(IOException e){
                  System.out.println("IOException while appending lines to summary log-file.");
              }

            }


        fw.close();
        }catch(IOException e){
            System.out.println("IOException while writing things to summary log-file.");
        }

    }


}
