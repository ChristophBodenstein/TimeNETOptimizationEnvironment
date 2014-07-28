/*
 * Provides some supporting methods to convert things...
 * Stores some references in static fields
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
 

package timenetexperimentgenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import timenetexperimentgenerator.datamodel.*;
import timenetexperimentgenerator.helper.StatisticAggregator;
import timenetexperimentgenerator.optimization.OptimizerPreferences;
import timenetexperimentgenerator.simulation.SimulationCache;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.typedef.*;



/**
 *
 * @author Christoph Bodenstein
 */
public class support {

//Define some program-wide default values
public static final double DEFAULT_STEPPING=1.0;
public static final long DEFAULT_TIMEOUT=10000;
public static final int DEFAULT_WRONG_SOLUTIONS_IN_A_ROW=30;
public static final int DEFAULT_WRONG_SOLUTION_PER_DIRECTION=9;
public static final int DEFAULT_SIZE_OF_NEIGHBORHOOD=5;
public static final typeOfAnnealing DEFAULT_TYPE_OF_ANNEALING=typeOfAnnealing.FastAnnealing;
public static final typeOfStartValueEnum DEFAULT_TYPE_OF_STARTVALUE =typeOfStartValueEnum.random;
public static final typeOfNeighborhoodEnum DEFAULT_TYPE_OF_NEIGHBORHOOD =typeOfNeighborhoodEnum.StepForwardBackward;


public static final double DEFAULT_T_RATIO_SCALE=0.00001;
public static final double DEFAULT_T_ANNEAL_SCALE=100;
public static final double DEFAULT_MAXTEMP_PARAMETER=1.0;
public static final double DEFAULT_MAXTEMP_COST=1.0;
public static final double DEFAULT_EPSILON=0.01;


//End of program-wide default value definition


private static JLabel statusLabel=null;//The label for showing status information
private static String originalFilename=null;//The original SCPN source file to fork for every simulation
private static MainFrame mainFrame=null;//The Main Frame of the program
private static JTabbedPane measureFormPane=null;//The tabbed pane with some Measurement-forms inside to select the optimization targets
private static String pathToTimeNet=null;//The path to TimeNet.jar
private static String pathToR=null;//The path to R
private static String tmpPath=null;//The path, where all simulation files (xml), source files and logs will be stored
private static SimulationCache mySimulationCache=null;  
private static boolean cachedSimulationAvailable=false;
private static boolean distributedSimulationAvailable=false;
private static boolean isRunningAsSlave=false;
private static String remoteAddress=null;
private static Integer chosenOptimizerType=0;//0=Greedy, 1=?, 2=?
private static Integer chosenSimulatorType=0;//0=local, 1=cached, 2=distributed
private static LogFrame myLogFrame=new LogFrame();

public static final String[] SIMTYPES={"Local Sim.","Cache Only Sim.","Cache & Local","Web Sim."};
public static final String[] OPTITYPES={"Hillclimbing","Sim. Annealing","ChargedSystemSearch","GeneticSearch","A.Seidel-3"};

    /**
     * @return the myOptimizerPreferences a Reference to the Preferences-Frame
     */
    public static OptimizerPreferences getOptimizerPreferences() {
        return myOptimizerPreferences;
    }


private static final OptimizerPreferences myOptimizerPreferences = new OptimizerPreferences();

    /**
     * @return the typeOfStartValue
     */
//    public static typedef.typeOfStartValueEnum getTypeOfStartValue() {
//        return typeOfStartValue;
//    }

    /**
     * @param aTypeOfStartValue the typeOfStartValue to set
     */
//    public static void setTypeOfStartValue(typedef.typeOfStartValueEnum aTypeOfStartValue) {
//        typeOfStartValue = aTypeOfStartValue;
//    }



private static boolean logToConsole=false;
 
//private static typedef.typeOfStartValueEnum typeOfStartValue=typedef.typeOfStartValueEnum.start;

    /**
     * Translates Parameternames from logfile to internal used Strings
     * because in log file some parameters might have other names then internal
     * @param s Name of the parameter in log file
     * @return Name of the parameter used internal in this program
     */
    public final static String translateParameterNameFromLogFileToTable(String s){
        if(s.equals("Configured-ConfidenceIntervall")){
        return "ConfidenceIntervall";
        }
        
    return s;
    }

    /**
     * Removes the file ending from a given filename/path
     * @param filePath original name of the file
     * @return Name of the file without filename-extension
     */
    public static final String removeExtention(String filePath) {
    File f = new File(filePath);
        if (f.isDirectory()) {return filePath;}
        String name = f.getName();
        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0)
        {return filePath;}
        else
        {File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
         return renamed.getPath();
        }
    }

    /**
     * Returns reference to status-label
     * @return the statusLabel
     */
    public static JLabel getStatusLabel() {
        return statusLabel;
    }

    /**
     * sets the reference to status-label
     * @param aStatusLabel the statusLabel to set
     */
    public static void setStatusLabel(JLabel aStatusLabel) {
        statusLabel = aStatusLabel;
    }

    /**
     * Returns the filename of the original SCPN to be used for simulations
     * @return the originalFilename
     */
    public static String getOriginalFilename() {
        return originalFilename;
    }

    /**
     * Sets the filname of the original SCPN to be used for simulations
     * @param aOriginalFilename the originalFilename to set
     */
    public static void setOriginalFilename(String aOriginalFilename) {
        originalFilename = aOriginalFilename;
    }

    /**
     * Returns reference to MainFrame (sometimes helpful)
     * @return the mainFrame
     */
    public static MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Sets the reference to MainFrame
     * @param aMainFrame the mainFrame to set
     */
    public static void setMainFrame(MainFrame aMainFrame) {
        mainFrame = aMainFrame;
    }

    /**
     * Returns reference to MeasureFormPane which contains MeasurementForms
     * @return the measureFormPane
     * @see MeasurementForm
     */
    public static JTabbedPane getMeasureFormPane() {
        return measureFormPane;
    }

    /**
     * Sets reference to MeasureFormPane which contains MeasurementForms
     * @param aMeasureFormPane the measureFormPane to set
     * @see MeasurementForm
     */
    public static void setMeasureFormPane(JTabbedPane aMeasureFormPane) {
        measureFormPane = aMeasureFormPane;
    }

    /**
     * Returns path to TimeNet executable, needed for local simulations
     * @return the pathToTimeNet
     */
    public static String getPathToTimeNet() {
        return pathToTimeNet;
    }
    
    /**
     * Returns path to R executable, needed for plots
     * @return the pathToR
     */
    public static String getPathToR() {
        return pathToR;
    }

    /**
     * Sets path to TimeNet executable, needed for local simulations
     * @param aPathToTimeNet the pathToTimeNet to set
     */
    public static void setPathToTimeNet(String aPathToTimeNet) {
        pathToTimeNet = aPathToTimeNet;
    }
    
    /**
     * Sets path to R executable, needed for plots
     * @param aPathToR the pathToR to set
     */
    public static void setPathToR(String aPathToR) {
        pathToR = aPathToR;
    }

    /**
     * Returns tmp-path for temporary SCNPs, even Main log-file is stored there
     * @return the tmpPath
     */
    public static String getTmpPath() {
        return tmpPath;
    }

    /**
     * Sets tmp-path for temporary SCNPs, even Main log-file is stored there
     * @param aTmpPath the tmpPath to set
     */
    public static void setTmpPath(String aTmpPath) {
        tmpPath = aTmpPath;
    }
    
    /**
     * Asks user for a directory to store data
     * @param title Title of Dialog, which path do you need?
     * @param startPath starting Path for dialog
     * @return Chosen path from dialog
     */
    public static String getPathToDirByDialog(String title, String startPath){
    
    String outputDir=null;
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setControlButtonsAreShown(true);
    if(startPath!=null){
        File f = new File(startPath);
        fileChooser.setCurrentDirectory(f);
    }
    fileChooser.setDialogTitle(title);
        if (fileChooser.showSaveDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            if(fileChooser.getSelectedFile().isDirectory() ){
                outputDir=fileChooser.getSelectedFile().toString();
            }else{
                outputDir=fileChooser.getCurrentDirectory().toString();
            }
            support.log("choosen dir: "+outputDir);
        }
    return outputDir;
    }
    
    
    /**
     * Rounds a float value to x digits
     * 
     * @param valueToRound The input Vlaue to be round
     * @return round Value with x precise digits
     */
    public static double round(double valueToRound){
    return (double)Math.round(valueToRound * 1000) / 1000;
    }
    
     /**
     * Some float functions for parameter casting, will be overloaded
     * @param f float to be returned normalized and casted
     * @return float value
     */
    public static double getDouble(double f){
    return f;
    }
    
    /**
     * Casts a String to float and returns this float
     * @param s String to be converted into float
     * @return float value of input String
     */
    public static double getDouble(String s){
    return Double.parseDouble(s.replace(',', '.'));
    }

    /**
     * Casts an int to float
     * @param i int value to cast to float
     * @return float value of given int
     */
    public static double getDouble(int i){
    return (double)i;
    }
    
    
    /**
     * Casts a float to int
     * @param f float value to cast to int
     * @return int value of given float
     */
    public static int getInt(double f){
        return (int)f;
    }
    
    /**
     * Converts a float into a String
     * @param f float value to be converted into String
     * @return String which represents the input float value
     */
    public static String getString(double f){
    return String.valueOf(f);
    }
    
     /**
     * Returns a String with float value, where comma is used instead of point as decimal-separator
     * @param f Float value to be converted into a String with comma as decimal-point
     * @return String representing the float value with comma as decimal-point
     */
    public static String getCommaFloat(double f){
    //System.out.print("UnFormated float is "+f);
    String returnValue=getCommaFloat( Double.toString(f) ) ;
    //support.log("  --  Formated float is "+returnValue);
    return returnValue;
    }
    
    /**
     * Returns a String with float value, where comma is used as decimal-point
     * @param f String representing a float with decimal-point
     * @return String representing the float value with comma as decimal-point
     */
    public static String getCommaFloat(String f){
    //System.out.print("UnFormated String is "+f);
    String returnValue=f.replace(".", ",");
    //support.log("  --  Formated String is "+returnValue);
    return returnValue;
    }

    
    /**
     * Returns a String containing a float with point as decimal delimiter
     * @param f String containing a Float with commma as decimal delimiter
     * @return String conaining a Float with point as decimal delimiter
     */
    public static String getPointFloat(String f){
    String returnValue=f.replace(",", ".");
    //support.log("  --  Formated String is "+returnValue);
    return returnValue;
    }
    
    /**
     * @return the mySimulationCache
     */
    public static SimulationCache getMySimulationCache() {
        return mySimulationCache;
    }

    /**
     * @param aMySimulationCache the mySimulationCache to set
     */
    public static void setMySimulationCache(SimulationCache aMySimulationCache) {
        mySimulationCache = aMySimulationCache;
    }

    /**
     * @return the cachedSimulationAvailable
     */
    public static boolean isCachedSimulationAvailable() {
        return cachedSimulationAvailable;
    }

    /**
     * @param aCachedSimulationEnabled the cachedSimulationAvailable to set
     */
    public static void setCachedSimulationEnabled(boolean aCachedSimulationEnabled) {
        cachedSimulationAvailable = aCachedSimulationEnabled;
    }
    
    
    /**
     * Prints the data from MeasureType to log console
     * @param m Measure to be printed
     * @param header will be printed before MeasureType data
     * @param footer will be printed after MeasureType data
     */
    public static void printMeasureType(MeasureType m, String header, String footer){
    if(m==null){
    support.log("Printing of Measure not possible. Measure is null.");
        return;
    }    
        support.log(header);
        support.log("***** Start of Measure "+m.getMeasureName()+" ******");
        support.log("Mean Value: "+support.getCommaFloat(m.getMeanValue()));
        support.log("Variance: "+support.getCommaFloat(m.getVariance()));
        support.log("Confidence-Min: "+support.getCommaFloat(m.getConfidenceInterval()[0]));
        support.log("Confidence-Max: "+support.getCommaFloat(m.getConfidenceInterval()[1]));
        support.log("Epsilon: "+support.getCommaFloat(m.getEpsilon()));
        
//        if(m.getParameterList()!=null)
//        {
//        support.log("---Printing parameterlist---");
//        ArrayList<parameter> pList=m.getParameterList();
//            for(int i=0;i<pList.size();i++){
//            support.log("Value of "+pList.get(i).getName() +" is: "+pList.get(i).getValue());
//            }
//        support.log("---End of parameterlist---");
//        }
        support.log("Used CPU-Time: " +m.getCPUTime());
        support.log("***** End of Measure "+m.getMeasureName()+" ******");
        support.log(footer);

    }
    
     /**
     * Adds Lines to logfile with the data from given parserlist
     * @param pList List of parsers, which includes the data from one simulation each
     * @param logFileName The path and name of the general log file
     */
    public static void addLinesToLogFileFromListOfParser(ArrayList<SimulationType> pList, String logFileName){
    boolean writeHeader=false;
    String line;
        try{
        support.log("Logfilename is:"+logFileName);
        //Öffnen des Logfiles und Schreiben der ersten Zeile

        File f=new File(logFileName);
        if(!f.exists()){writeHeader=true;}
        FileWriter fw= new FileWriter(logFileName, true);

            if(writeHeader){
                MeasureType exportMeasure=pList.get(0).getMeasures().get(0);//Dummy, es wird das erste Measure abgefragt und die Parameterliste
                line="MeasureName;Mean Value; Variance; Conf.Interval-Min;Conf.Interval-Max;Epsilon;"+"Simulation Time";
                    for(int i1=0;i1<pList.get(0).getListOfParameters().size();i1++)
                    {
                    line=line+";"+pList.get(0).getListOfParameters().get(i1).getName();
                    }
                    try {
                        fw.write(line);
                        fw.append( System.getProperty("line.separator") );
                    } catch (IOException ex) {
                        support.log("Error writing Header to Summary-log-file.");
                    }
            }


            for(int i=0;i<pList.size();i++){
            SimulationType myParser=pList.get(i);
            StatisticAggregator.addToStatistics(myParser, logFileName);
              try{
              //fw.write(line);
              //fw.append( System.getProperty("line.separator") );
                for(int i1=0;i1<myParser.getMeasures().size();i1++){//Alle Measure schreiben
                MeasureType exportMeasure=myParser.getMeasures().get(i1);
                /*support.log("Mean Value= "+support.getCommaFloat(exportMeasure.getMeanValue()));
                support.log("Variance= "+support.getCommaFloat(exportMeasure.getVariance()));
                support.log("Confidence-Min= "+support.getCommaFloat(exportMeasure.getConfidenceInterval()[0]));
                support.log("Confidence-Max= "+support.getCommaFloat(exportMeasure.getConfidenceInterval()[1]));
                support.log("Epsilon= "+support.getCommaFloat(exportMeasure.getEpsilon()));
                support.log("Simulation-Time= "+support.getCommaFloat(myParser.getSimulationTime()));
                */
                line=exportMeasure.getMeasureName()+";"+support.getCommaFloat(exportMeasure.getMeanValue())+";"+support.getCommaFloat(exportMeasure.getVariance())+";"+support.getCommaFloat(exportMeasure.getConfidenceInterval()[0])+";"+support.getCommaFloat(exportMeasure.getConfidenceInterval()[1])+";"+support.getCommaFloat(exportMeasure.getEpsilon())+";"+support.getCommaFloat(exportMeasure.getSimulationTime());
                    for(int c=0;c<myParser.getListOfParameters().size();c++)
                    {
                        line=line+";"+support.getCommaFloat(myParser.getListOfParameters().get(c).getValue());
                    }
                fw.write(line);
                fw.append( System.getProperty("line.separator") );
                }
              }catch(IOException e){
                  support.log("IOException while appending lines to summary log-file.");
              }

            }


        fw.close();
        }catch(IOException e){
            support.log("IOException while writing things to summary log-file.");
        }
    }
    
    /**
     * Adds Lines to logfile with the data from given SimulationType
     * @see addLinesToLogFileFromListOfParser
     * @param p SimulationType with data from simgle logfile
     * @param logFileName name of logfile, data will be appended
     */
    public static void addLinesToLogFile(SimulationType p, String logFileName){
    ArrayList<SimulationType> myParserList=new ArrayList<SimulationType>();
    myParserList.add(p);
        addLinesToLogFileFromListOfParser(myParserList, logFileName);
    }
    
    
    /**
     * logs the data either to file, System-log, etc.
     * 
     * @param s String to be logged.
     */
    public static void log(String s){
        if(logToConsole){
        System.out.println(s);
        }else{
        myLogFrame.addText(s);
        }
    }
    
    /**
     * determines if log to console or somewhere else
     * @param logToConsole_ logging to console or not
     */
    public static void setLogToConsole(boolean logToConsole_)
    {
        logToConsole = logToConsole_;
    }
    
    /**
     * copies a file from source to sink appending file contents is possible
     * @param source name/path of source file
     * @param sink name/path of sin file
     * @param append true to append file content, else false
     * @return true if copy process succeeded, else false
     */
    public static boolean copyFile(String source, String sink, boolean append){
    try{
          File f1 = new File(source);
          File f2 = new File(sink);
          InputStream in = new FileInputStream(f1);
          OutputStream out;
          if(append){
          //For Append the file.
          out = new FileOutputStream(f2,true);
          } else{
            //For Overwrite the file.
            out = new FileOutputStream(f2);
            }

          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
          }
          in.close();
          out.close();
          support.log("File copied.");
          return true;
        }
        catch(FileNotFoundException ex){
          support.log(ex.getMessage() + " in the specified directory.");
          return false;
        }
        catch(IOException e){
          support.log(e.getMessage());
          return false;
        }
    }
    
    /**
     * returns address of simulation server incl. path
     * @return address url to simulation server as String
     */
    public static String getReMoteAddress(){
    if(remoteAddress==null)return "";else return remoteAddress;
    }
    
    /**
     * Sets the address of simulation server incl. path
     * @param address url to simulation server
     */
    public static void setRemoteAddress(String address){
    remoteAddress=address;
    }
    
    /**
     * Checks, if the given remoteAddress (URL to Sim.-Server) is correct
     * @param urlString The URL as String to be checked, if this is the available Sim.-Server
     * @return True if URL is correct and server is working, else false
     * To be modified by: Group studies 2014
     */
    public static boolean checkRemoteAddress(String urlString){
    return true;
    }
    
    /**
     * Checks, if Timenet is availabel at given Path, otherwise simulation run is not possible
     * @return True, if TimeNet-Path is correct, else return false
    */
    public static boolean checkTimeNetPath(){
    File tmpFile=new File(pathToTimeNet+File.separator+"TimeNET.jar");
    support.log("Check if Timenet is here:"+tmpFile.toString());
        if(tmpFile.exists()){
        return true;
        }else{
        return false;
        }
    }
    
    /**
      * Deletes a file or directory recursive
      * @param dir File or directory to be deleted recursively
      * @return true if deletion was successful, else false
    */
    public static boolean del(File dir){
        if (dir.isDirectory()){
        File[] files = dir.listFiles();
            for (File aktFile: files){
            del(aktFile);
            }
        }
    return dir.delete();
    }
    
    
    /**
     * searches for values of corresponding parameters, whicht are set in the filename
     * @param fileName String containing parameternames and values, devided by "_"
     * @param needle String to be found (name of the parameter)
     * @return String with found value
    */
    public static String getValueFromFileName(String fileName, String needle){
    String[] stringList=fileName.split("_");

        for(int i=0; i<stringList.length;i++){
            if(stringList[i].equals(needle)){
            return stringList[i+1];
            }
        }
    return "";
    }

    /**
     * searches for values of parameters, encoded in filenames(Strings) and returns integer-value as String
     * @param fileName String containing parameternames and values, devided by "_"
     * @param needle String to be found (name of the parameter)
     * @return String with Integer-value of found parameter (without decimal digits)
     */
    public static String getIntStringValueFromFileName(String fileName, String needle){
    String tmpString=getValueFromFileName(fileName, needle);
    return String.valueOf(Float.valueOf(tmpString).intValue());
    }

    /**
     * @return the chosenOptimizerType
     */
    public static Integer getChosenOptimizerType() {
        return chosenOptimizerType;
    }

    /**
     * @param aChosenOptimizerType the chosenOptimizerType to set
     */
    public static void setChosenOptimizerType(Integer aChosenOptimizerType) {
        chosenOptimizerType = aChosenOptimizerType;
    }

    /**
     * @return the chosenSimulatorType
     */
    public static Integer getChosenSimulatorType() {
        return chosenSimulatorType;
    }

    /**
     * @param aChosenSimulatorType the chosenSimulatorType to set
     */
    public static void setChosenSimulatorType(Integer aChosenSimulatorType) {
        chosenSimulatorType = aChosenSimulatorType;
    }

    /**
     * @return the distributedSimulationAvailable
     * It needs to return true to be able to select this type of Simualtion
     * Group studies students: this.distributedSimulationAvailable should be true, if URL is correct.
     */
    public static boolean isDistributedSimulationAvailable() {
        return distributedSimulationAvailable;
    }

    /**
     * @param aDistributedSimulationAvailable the distributedSimulationAvailable to set
     */
    public static void setDistributedSimulationAvailable(boolean aDistributedSimulationAvailable) {
        distributedSimulationAvailable = aDistributedSimulationAvailable;
    }

        /**
     * Creates and returns a set of Parameters made by deep-copying
     * @param parameterBase the array of parameters to be dublicated
     * @return array of parameters, the copy of input
     */
    public static ArrayList<parameter> getCopyOfParameterSet(ArrayList<parameter> parameterBase){
        ArrayList<parameter> newParameterSet = new ArrayList<parameter>();
        for(int i=0;i<parameterBase.size();i++){

        parameter p = new parameter();
        p.setName(parameterBase.get(i).getName());
        p.setStartValue(parameterBase.get(i).getStartValue());
        p.setStepping(parameterBase.get(i).getStepping());
        p.setEndValue(parameterBase.get(i).getEndValue());
        //newParameterSet[i].setValue(Float.toString((historyOfParsers.get(historyOfParsers.size()-1)).getMeasureValueByMeasureName(parameterBase[i].getName())));
        p.setValue(parameterBase.get(i).getValue());
        newParameterSet.add(p);
        }
    return newParameterSet;
    }

    /**
     * @return the isRunningAsSlave
     */
    public static boolean isIsRunningAsSlave() {
        return isRunningAsSlave;
    }

    /**
     * @param aIsRunningAsSlave the isRunningAsSlave to set
     */
    public static void setIsRunningAsSlave(boolean aIsRunningAsSlave) {
        isRunningAsSlave = aIsRunningAsSlave;
    }
    
    /**
     * Blocks the program and waits until the simulation has ended or timeout
     * 
     * @param mySimulator Simulator to wait for
     * @param simulationCounter Simulation counter to show in the info-label
     * @param timeout Timeout in Seconds!
     * @return true if simulation was sucessful. false if timeout
     */
    public static boolean waitForEndOfSimulator(Simulator mySimulator, int simulationCounter, long timeout){
    long timeoutCounter=timeout;
    support.log("wait for Simulator has 100% completed.");
            getStatusLabel().setText("Simulations started.");
                while(mySimulator.getStatus()<100){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        support.log("InterruptedException in main loop of optimization. Optimization aborted.");
                        statusLabel.setText("Aborted / Error");
                    }
                getStatusLabel().setText("Done "+ mySimulator.getStatus() +"% ");
                
                
                timeoutCounter--;
                    //Break if timeout is reached
                    if (timeoutCounter<=1){
                        support.log("Timeout for simulation reached. Aborting simulation.");
                        return false;}
                }
                simulationCounter=mySimulator.getSimulationCounter();
                getMainFrame().updateSimulationCounterLabel(simulationCounter);
                //support.log("Simulation status:"+mySimulator.getStatus() +"%");
                support.log("Simulation Counter: "+simulationCounter);
    return true;
    }
    
    /**
     * Append a list of parsers to another list of parsers
     * No dublication chack is made
     * @param mainList Main List of parsers
     * @param listToBeAdded List of Parsers to be added to Main List
     * @return List of parsers containing all elements from both lists
     */
    public static ArrayList<SimulationType> appendListOfParsers(ArrayList<SimulationType> mainList, ArrayList<SimulationType> listToBeAdded){
        for(int i=0;i<listToBeAdded.size();i++){
        mainList.add(listToBeAdded.get(i));
        }
    return mainList;
    }
    
    /**
     * Prints out info for every Measure in given List (List of Measures to be optimized)
     * @param p Parser containing all Measures from Simulation
     * @param measureList List of Measures to be optimized / to print.
     */
    public static void printOptimizedMeasures(SimulationType p, ArrayList<MeasureType> measureList){
    double distance=0;
        for(int measureCount=0;measureCount<measureList.size();measureCount++){
                MeasureType activeMeasure=p.getMeasureByName(measureList.get(measureCount).getMeasureName());
                MeasureType activeMeasureFromInterface=measureList.get(measureCount);//Contains Optimization targets
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
                support.printMeasureType(activeMeasure, "**** Optimizd Value for Measure is ****", "---------------------------");
            }
    support.log("Whole remaining distance of all Measures is:"+distance);
    }

    /**
     * @return the myLogFrame
     */
    public static LogFrame getMyLogFrame() {
        return myLogFrame;
    }

    /**
     * @param aMyLogFrame the myLogFrame to set
     */
    public static void setMyLogFrame(LogFrame aMyLogFrame) {
        myLogFrame = aMyLogFrame;
    }
    
    /**
     * 
     * @param p the array to be converted to ArrayList
     * @return the converted ArrayList
     */
    public static ArrayList<parameter> convertArrayToArrayList(parameter p[])
    {
        ArrayList<parameter> paraList = new ArrayList<parameter>();
        for (int i=0; i<p.length; ++i)
        {
            paraList.add(p[i]);
        }
        return paraList;
    }
    
    /**
     * 
     * @param list ArrayList to be converted
     * @return parameter[] of the list
     */
    public static parameter[] convertArrayListToArray(ArrayList<parameter> list)
    {
        parameter pArray[] = new parameter[list.size()];
        for (int i = 0; i < list.size(); ++i)
        {
            pArray[i] = list.get(i);
        }
        return pArray;
    }
    
    /**
     * Selects the parameter with given name from Array of parameters
     * @return parameter with given name (first finding) or null, if not found
     * @param pList Array of parameter
     * @param name Name of parameter to be found in array
     */
    public static parameter getParameterByName(ArrayList<parameter> pList, String name){
    parameter outputValue=null;
    
        for(int i=0;i<pList.size();i++){
            if(pList.get(i).getName().equals(name)){
            outputValue=pList.get(i);
            }
        }
    return outputValue;
    }
    
    /**
     * Return double value of loaded property
     * If any error occurs, the given default value is returned
     * @param name Name of the property to be loaded
     * @param defaultValue The default to be returned, if error occurs
     * @param auto The Properties-Object to load data from
     * @return double value of property
     */
    public static double loadDoubleFromProperties(String name, double defaultValue, Properties auto){
        try{
        return Double.valueOf(auto.getProperty(name));
        }catch(Exception e){
        support.log("Error loading property: "+name);
        return defaultValue;
        }
    }
    
    
    /**
     * Return int value of loaded property
     * If any error occurs, the given default value is returned
     * @param name Name of the property to be loaded
     * @param defaultValue The default to be returned, if error occurs
     * @return double value of property
     */
    public static int loadIntFromProperties(String name, int defaultValue, Properties auto){
        try{
        return Integer.valueOf(auto.getProperty(name));
        }catch(Exception e){
        support.log("Error loading property: "+name +". Setting to default Value: "+ defaultValue);
        return defaultValue;
        }
    }
}


