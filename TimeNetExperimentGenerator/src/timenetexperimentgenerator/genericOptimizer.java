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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/**
 *
 * @author sse
 *
 * needs: path to timenet, path for tmp-files, path/filename of original, access to tModel with parameters, access to jTabbedPane with Measurement-forms
 *
 */
public class genericOptimizer implements Runnable{
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

    //Constructor
    genericOptimizer(String originalFilename, MainFrame parentTMP, JTabbedPane MeasureFormPaneTMP, String pathToTimeNetTMP, JLabel infoLabel){
    this.infoLabel=infoLabel;
    this.pathToTimeNet=pathToTimeNetTMP;
    this.MeasureFormPane=MeasureFormPaneTMP;
    this.parent=parentTMP;
    this.parameterBase=parent.getParameterBase();
    this.listOfMeasures=parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
    System.out.println("# of Measures to be optimized: "+this.listOfMeasures.size());

        for(int i=0;i<MeasureFormPane.getComponentCount();i++){
            if(MeasureFormPane.getComponent(i) instanceof MeasurementForm){
                if(((MeasurementForm)MeasureFormPane.getComponent(i)).isActive()){
                String targetName=((MeasurementForm)MeasureFormPane.getComponent(i)).getNameOfChosenMeasurement();
                float targetValue=((MeasurementForm)MeasureFormPane.getComponent(i)).getCustomTargetValue();
                String targetKind=((MeasurementForm)MeasureFormPane.getComponent(i)).getOptimizationTarget();

                //Aus Liste der Measurements das richtig raussuchen und das Target setzen
                    for(int c=0;c<this.listOfMeasures.size();c++){
                        if(this.listOfMeasures.get(c).getMeasureName().equals(targetName)){
                        this.listOfMeasures.get(c).setTargetValue(targetValue, targetKind);
                        }
                    }
                System.out.println("Name of Measures to be optimized: "+targetName);
                System.out.println("Kind of Measures to be optimized: "+targetKind);
                System.out.println("Value of Measures to be optimized: "+targetValue);

                }
            }
        }
    //Alle Steppings auf Standard setzen
    arrayOfIncrements=new float[parameterBase.length];
        for(int i=0;i<parameterBase.length;i++){
        arrayOfIncrements[i]=getFloat(parameterBase[i].getStepping());
        }


    this.parent=parentTMP;
    this.filename=originalFilename;
    //Ask for Tmp-Path
    this.tmpPath=getTmpPathByDialog();
    //Start this Thread
    new Thread(this).start();
    }



    /***
     Asks user for tmp-directory for files, logfiles and master-logfile
     */
    private String getTmpPathByDialog(){
    File f = new File(this.filename);
    String outputDir="";
    JFileChooser fileChooser = new JFileChooser(f.getParent());
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setControlButtonsAreShown(true);
    fileChooser.setCurrentDirectory(f);
    fileChooser.setDialogTitle("Dir for export TMP-Files and log.\n "+"Go INTO the dir to choose it!");
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            if(fileChooser.getSelectedFile().isDirectory() ){
                outputDir=fileChooser.getSelectedFile().toString();
            }else{
                outputDir=fileChooser.getCurrentDirectory().toString();
            }
            System.out.println("choosen outputdir: "+outputDir);
        }
    return outputDir;
    }


    /**
      Creates new List of Parametersets to be simulated, based on actual history of simulation-results

     */
    public ArrayList<parameter[]> getNextSimulations(ArrayList<parser> historyOfParsers){
    ArrayList<parameter[]> returnValue=new ArrayList<parameter[]>();

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
                        MeasureType activeMeasure=historyOfParsers.get(historyCount).getMeasureByName(listOfMeasures.get(measureCount).getMeasureName());
                        MeasureType activeMeasureFromInterface=listOfMeasures.get(measureCount);//Contains Optimization targets
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

                                if(getFloat(newParameterSet[i].getValue())<getFloat(newParameterSet[i].getEndValue())){                                
                                arrayOfIncrements[i]=getFloat(newParameterSet[i].getStepping());                                
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
     */
    void applyArrayOfIncrements(float[] arrayOfIncrements, parameter[] newParameterSet){
    System.out.println("Applying Array of Increments.");
        for(int i=0;i<newParameterSet.length;i++){
        System.out.print(newParameterSet[i].getName()+"="+newParameterSet[i].getValue()+" will be incremented by: "+arrayOfIncrements[i]+" and is now:");
        newParameterSet[i].setValue(getString(Math.min(arrayOfIncrements[i]+getFloat(newParameterSet[i].getValue()),getFloat(newParameterSet[i].getEndValue())) ) );
        System.out.println(newParameterSet[i].getValue());
        }
    this.parameterBase=newParameterSet;
    }

    /*
     * Creates and returns a set of Parameters made by deep-copying
     **/
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

    
    /*
     Some float functions for parameter casting, will be overloaded
     */
    float getFloat(float f){
    return f;
    }

    float getFloat(String s){
    return Float.parseFloat(s);
    }

    String getString(float f){
    return String.valueOf(f);
    }

    public void run() {
    ArrayList<parameter[]> mySimulationList=getNextSimulations(historyOfParsers);
    int simulationCounter=0;
    String logFileName=this.tmpPath+File.separator+"SimLog"+Calendar.getInstance().getTimeInMillis()+"ALL"+".csv";
        try{
            while(mySimulationList.size()>0){
            //batchSimulator myBatchSimulator = new batchSimulator(mySimulationList, this.filename, this.infoLabel, parent);
            genericSimulator myGenericSimulator = new genericSimulator(mySimulationList, this.filename, this.pathToTimeNet, tmpPath, false, simulationCounter);
                while(myGenericSimulator.getStatus()<100){
                Thread.sleep(500);
                this.infoLabel.setText("Done "+ myGenericSimulator.getStatus() +"%");
                simulationCounter=myGenericSimulator.getSimulationCounter();
                this.parent.setSimulationCounter(simulationCounter);
                //System.out.print("Simulation status:"+myGenericSimulator.status +"%");
                }

                if(myGenericSimulator.getListOfCompletedSimulationParsers().size()>0){
                historyOfParsers.addAll(myGenericSimulator.getListOfCompletedSimulationParsers());
                addLinesToLogFileFromListOfParser(myGenericSimulator.getListOfCompletedSimulationParsers(), logFileName);
                }

            mySimulationList=getNextSimulations(historyOfParsers);
            //Warte bis simulation fertig ist (oder TimeOut)
            //frage nach Ergebnissen und packe sie in History
            }
        }catch(Exception e){
        e.printStackTrace();
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }


    private void addLinesToLogFileFromListOfParser(ArrayList<parser> pList, String logFileName){
    boolean writeHeader=false;
    String line="";
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
                        ex.printStackTrace();
                    }
            }


            for(int i=0;i<pList.size();i++){
            parser myParser=pList.get(i);
              try{
              //fw.write(line);
              //fw.append( System.getProperty("line.separator") );
                for(int i1=0;i1<myParser.getMeasures().size();i1++){//Alle Measure schreiben
                MeasureType exportMeasure=myParser.getMeasures().get(i1);
                line=exportMeasure.getMeasureName()+";"+getCommaFloat(exportMeasure.getMeanValue())+";"+getCommaFloat(exportMeasure.getVariance())+";"+getCommaFloat(exportMeasure.getConfidenceInterval()[0])+";"+getCommaFloat(exportMeasure.getConfidenceInterval()[1])+";"+getCommaFloat(exportMeasure.getEpsilon())+";"+getCommaFloat(myParser.getSimulationTime());
                    for(int c=0;c<exportMeasure.getParameterList().size();c++){
                    line=line+";"+getCommaFloat(exportMeasure.getParameterList().get(c).getValue());
                    }
                fw.write(line);
                fw.append( System.getProperty("line.separator") );
                }
              }catch(Exception e){e.printStackTrace();}

            }


        fw.close();
        }catch(Exception e){e.printStackTrace();}

    }

//Returns a String with float value, where comma is used instead of point as decimal-separator
    public String getCommaFloat(float f){
    //System.out.print("UnFormated float is "+f);
    String returnValue=getCommaFloat( Float.toString(f) ) ;
    //System.out.println("  --  Formated float is "+returnValue);
    return returnValue;
    }
    public String getCommaFloat(String f){
    //System.out.print("UnFormated String is "+f);
    String returnValue=f.replace(".", ",");
    //System.out.println("  --  Formated String is "+returnValue);

    return returnValue;
    }



}
