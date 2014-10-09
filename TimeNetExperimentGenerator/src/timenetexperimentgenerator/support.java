/*
 * Provides some supporting methods to convert things...
 * Stores some references in static fields
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
 

package timenetexperimentgenerator;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import timenetexperimentgenerator.datamodel.*;
import timenetexperimentgenerator.helper.SimOptiCallback;
import timenetexperimentgenerator.helper.StatisticAggregator;
import timenetexperimentgenerator.optimization.Optimizer;
import timenetexperimentgenerator.optimization.OptimizerPreferences;
import timenetexperimentgenerator.simulation.SimulationCache;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.typedef.*;



/**
 *
 * @author Christoph Bodenstein
 */
public class support {

//This Version of TimeNetExperimentGenerator
public static final String VERSION ="0.9.250";


//Define some program-wide default values
public static final double DEFAULT_STEPPING=1.0;
public static final long DEFAULT_TIMEOUT=10000;
public static final int DEFAULT_WRONG_SOLUTIONS_IN_A_ROW=30;
public static final int DEFAULT_WRONG_SOLUTION_PER_DIRECTION=9;
public static final int DEFAULT_SIZE_OF_NEIGHBORHOOD=5;
public static final typeOfAnnealing DEFAULT_TYPE_OF_ANNEALING=typeOfAnnealing.FastAnnealing;
public static final typeOfStartValueEnum DEFAULT_TYPE_OF_STARTVALUE =typeOfStartValueEnum.random;
public static final typeOfNeighborhoodEnum DEFAULT_TYPE_OF_NEIGHBORHOOD =typeOfNeighborhoodEnum.StepForwardBackward;
public static final typeOfSimulator DEFAULT_TYPE_OF_SIMULATOR = typeOfSimulator.Local;
public static final typeOfOptimization DEFAULT_TYPE_OF_OPTIMIZER = typeOfOptimization.HillClimbing;
public static final typeOfBenchmarkFunction DEFAULT_TYPE_OF_BENCHMARKFUNCTION = typeOfBenchmarkFunction.Ackley;

//default values for distributed simulation
public static final int DEFAULT_SLEEPING_TIME=2000;
public static final int DEFAULT_NUMBER_OF_SLEEPING_TIMES_AS_TIMEOUT = 20;
public static final int DEFAULT_CONNECTION_TIMEOUT=2000;


public static final double DEFAULT_T_RATIO_SCALE=0.00001;
public static final double DEFAULT_T_ANNEAL_SCALE=100;
public static final double DEFAULT_MAXTEMP_PARAMETER=1.0;
public static final double DEFAULT_MAXTEMP_COST=1.0;
public static final double DEFAULT_EPSILON=0.01;
public static final typeOfAnnealingParameterCalculation DEFAULT_CALC_NEXT_PARAMETER=typeOfAnnealingParameterCalculation.Standard;

//default values for genetic Optimization
public static final int DEFAULT_GENETIC_POPULATION_SIZE = 10;
public static final double DEFAULT_GENETIC_MUTATION_CHANCE = 20;
public static final boolean DEFAULT_GENETIC_MUTATE_TOP_SOLUTION = false;

//default values for CSS Optimization
public static final int DEFAULT_CSS_POPULATION_SIZE = 10;
public static final double DEFAULT_CSS_MAX_ATTRACTION = 100;

//default values for ABC Optimization
public static final int DEFAULT_ABC_NumEmployedBees = 10;
public static final int DEFAULT_ABC_NumOnlookerBees = 10;
public static final int DEFAULT_ABC_NumScoutBees = 2;
public static final int DEFAULT_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement = 3;


public static final int DEFAULT_CACHE_STUCK=100;//Optimizer can ask 2 times for simulating the same parameterset in a row. Then optimization will be aborted!
public static final int DEFAULT_LOCAL_SIMULATION_ATTEMPTS=5;//Local simulation is tried so many times until break
public static final int DEFAULT_TIME_BETWEEN_LOCAL_SIMULATIONS=3000;//in ms


public static final int DEFAULT_NumberOfPhases=2;//Default number of phases in multi-stage-optimization
public static final typeOfOptimization DEFAULT_typeOfUsedMultiPhaseOptimization=typeOfOptimization.HillClimbing;//default type of optimization in multi-stage-opti
public static final int DEFAULT_ConfidenceIntervallStart=85;
public static final int DEFAULT_ConfidenceIntervallEnd=99;
public static final int DEFAULT_MaxRelErrorStart=1;
public static final int DEFAULT_MaxRelErrorEnd=1;
public static final int DEFAULT_InternalParameterStart=0;
public static final int DEFAULT_InternalParameterEnd=0;
public static final boolean DEFAULT_KeepDesignSpaceAndResolution=true;


public static final int DEFAULT_MINIMUM_DESIGNSPACE_SIZE_PER_PARAMETER=10;//Minimum Steps per Parameter.
public static final int DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION=50;//If DS is smaller, no Optimization is possible


public static final String NAME_OF_PREF_DIR=System.getProperty("user.home") + File.separatorChar +".TNGenerator"+ File.separatorChar ;//The dir in which all pref-files will be stored
public static final String NAME_OF_LOGFILE=NAME_OF_PREF_DIR + "TimeNETLogFile.log";//the name of the program logfile, if logging to file is active
public static final String NAME_OF_PREFERENCES_FILE=NAME_OF_PREF_DIR+"ApplicationPreferences.prop";//name of the pref-file for program-wide prefs
public static final String NAME_OF_OPTIMIZER_PREFFERENCES_FILE=NAME_OF_PREF_DIR+"OptimizerPreferences.prop";//name of the pref file for optimization parameters



public static final boolean DEFAULT_LOG_TO_WINDOW=true;
public static final boolean DEFAULT_LOG_TO_FILE=false;

public static final char DEFAULT_PLOT_CHAR='.';//Default Char to be used in R-Plot-Scripts

public static final int DEFAULT_MEMORYPRINT_INTERVALL=1;//in seconds. Default Interval between updating the memory-usage-progressbar

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
private static boolean distributedSimulationAvailable=true;
private static boolean isRunningAsSlave=false;
private static String remoteAddress=null;
private static typedef.typeOfOptimization chosenOptimizerType=DEFAULT_TYPE_OF_OPTIMIZER;//0=Greedy, 1=?, 2=?
private static typeOfSimulator chosenSimulatorType=DEFAULT_TYPE_OF_SIMULATOR;//0=local, 1=cached, 2=distributed
private static LogFrame myLogFrame=new LogFrame();
private static ArrayList<parameter> parameterBase=null;//Base set of parameters, start/end-value, stepping, etc.
private static ArrayList<parameter> originalParameterBase=null;//Base set of parameters, This will remain unchanged even in Multistage-Mode

private static boolean cancelEverything=false;//If set to true, everything is cancelled
private static typeOfBenchmarkFunction chosenBenchmarkFunction=DEFAULT_TYPE_OF_BENCHMARKFUNCTION;
private static long lastTimeOfSpinning=0;

    /**
     * @return the myOptimizerPreferences a Reference to the Preferences-Frame
     */
    public static OptimizerPreferences getOptimizerPreferences() {
        return myOptimizerPreferences;
    }


private static final OptimizerPreferences myOptimizerPreferences = new OptimizerPreferences();


private static boolean logToWindow=DEFAULT_LOG_TO_WINDOW;
private static boolean logToFile=DEFAULT_LOG_TO_FILE;

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
        if(s.equals("UsedCPUTIME")){
        return "UsedCPUTIME";
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
    //public static JLabel getStatusLabel() {
    //    return statusLabel;
    //}

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
        //If a tmp-path is given, return it
        if(tmpPath!=null){return tmpPath;}
        //If no tmp path is given, but a SCPN-path is available
        if(new File(getOriginalFilename()).getPath() !=null){
        return (new File(getOriginalFilename()).getPath());
        }
        //If no path is available take the dir of jar-file
        String t=support.class.getProtectionDomain().getCodeSource().getLocation().getPath()+File.separator+"tmp";
        //Try to create a tmp-dir and return this
        File tfile=new File(t);
        if( tfile.exists() ){
            if(tfile.isDirectory()){
            return t;
            }else{
            support.log(t+" is a file, will return "+support.class.getProtectionDomain().getCodeSource().getLocation().getPath()+" as tmp path.");
            return support.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            }
        }else{
        support.log("Try to create tmp dir:"+t);
            try{
            tfile.mkdir();
            }catch(Exception e){
            //If dir-creation fails, show warning
            support.log("Problem creting a tmp dir!");return null;
            }
        return t;
        }
        
        
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
        fileChooser.setCurrentDirectory(f.getParentFile());
        fileChooser.setSelectedFile(f);
    }
    fileChooser.setDialogTitle(title);
        if (fileChooser.showSaveDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            if(fileChooser.getSelectedFile().isDirectory() ){
                outputDir=fileChooser.getSelectedFile().toString();
            }else{
                //outputDir=fileChooser.getCurrentDirectory().toString();
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
        //Empty Cache/Create new object, if not already done
        if(mySimulationCache==null)emptyCache();
        return mySimulationCache;
    }

    /**
     * @param aMySimulationCache the mySimulationCache to set
     */
    public static void setMySimulationCache(SimulationCache aMySimulationCache) {
        mySimulationCache = aMySimulationCache;
    }

    /**
     * Creates a new Cache for all simulations
     * Used to "emptyY the cache-Object
     */
    public static void emptyCache(){
        mySimulationCache=new SimulationCache();
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
    
        parameter dummyParameterForCPUTime=new parameter();
        dummyParameterForCPUTime.setName("UsedCPUTIME");
        dummyParameterForCPUTime.setValue(0.0);
        dummyParameterForCPUTime.setStartValue(0.0);
        dummyParameterForCPUTime.setEndValue(0.0);
    
        try{
        //support.log("Number of Simulationtypes to add is "+pList.size());
            
        //Check if list is null, then exit
        if(pList==null){
        support.log("List of Simulations to add to logfile is null. Exit");
        return;
        }
            
            
        File f=new File(logFileName);
        if(!f.exists()){writeHeader=true;}
        FileWriter fw= new FileWriter(logFileName, true);

        

            if(writeHeader){
                //Write header of logfile
                
                //Add empty CPU-Time-Parameter for compatibility
                if (support.getParameterByName(pList.get(0).getListOfParameters(), "UsedCPUTIME")==null){
                    pList.get(0).getListOfParameters().add(dummyParameterForCPUTime);
                }
                
                MeasureType exportMeasure=pList.get(0).getMeasures().get(0);//First Measure will be used to determine the lsit of Parameters
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
            //set indicator
            setStatusText("Writing: "+(i+1) + "/"+ pList.size());
            SimulationType myParser=pList.get(i);
            
            //Add empty CPU-Time-Parameter for compatibility
            if (support.getParameterByName(myParser.getListOfParameters(), "UsedCPUTIME")==null){
                        myParser.getListOfParameters().add(dummyParameterForCPUTime);
            }
            
            
            
            StatisticAggregator.addToStatistics(myParser, logFileName);
              try{
                for(int i1=0;i1<myParser.getMeasures().size();i1++){//Alle Measure schreiben
                MeasureType exportMeasure=myParser.getMeasures().get(i1);
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
        }catch(Exception e){
            support.log("Exception while writing things to summary log-file.");
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
    String timeStamp = new SimpleDateFormat("yyyy:MM.dd_HH:mm:ss").format(Calendar.getInstance().getTime());
    s=timeStamp+": "+s;

        if(isLogToWindow()){
        myLogFrame.addText(s);
        }

        if(isLogToFile()){
            try {
                FileWriter fw = new FileWriter(NAME_OF_LOGFILE, true);
                fw.append(s+System.getProperty("line.separator") );
                fw.close();
            } catch (IOException ex) {
                log("Error while saving logfile.");
            }
        }

    }

    /**
     * deletes the log file of application
     */
    public static void deleteLogFile(){
    File f=new File(NAME_OF_LOGFILE);
        if(f.exists()){
        del(f);
        }
    }

    /**
     * determines if log to console or somewhere else
     * @param logToConsole_ logging to console or not
     */
    public static void setLogToConsole(boolean logToConsole_)
    {
        setLogToWindow(logToConsole_);
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
    
    public static void setRemoteAddress(String address) throws IOException{
    remoteAddress=address;
    //Check if address is correct.
    //distributedSimulationAvailable = checkRemoteAddress(address);
    //mainFrame.updateComboBoxSimulationType();        
    //If Address is correct, set distributedSimulationAvailable to TRUE!
    //e.i. call the method checkRemoteAddress(String ere)
    //After that, please call updateComboBoxSimulationType() in MainFrame.java
    }
    
    /**
     * Checks, if the given remoteAddress (URL to Sim.-Server) is correct
     * @param urlString The URL as String to be checked, if this is the available Sim.-Server
     * @return True if URL is correct and server is working, else false
     * To be modified by: Group studies 2014
     */
    public static boolean checkRemoteAddress(String urlString) throws IOException{
        support.log("Given remote-URL: "+urlString);
        if(urlString.endsWith("/") || urlString.endsWith("\\")){
            urlString = urlString.substring(0, urlString.length() - 1);
            remoteAddress=urlString;
        }
        
    try {
        try{
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(DEFAULT_CONNECTION_TIMEOUT);
            conn.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            //System.out.println(String.format("Fetching %s ...", url));

            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 500) {
                //System.out.println(String.format("Site is up, content length = %s", conn.getHeaderField("content-length")));
                support.log("URL seems correct. Distributed Simulation will be activated.");
                return true;
            } else {
                //System.out.println(String.format("Site is up, but returns non-ok status = %d", responseCode));
                support.log("URL seems wrong or server is not available.");
                return false;
            }
            
        }   catch(java.net.MalformedURLException e){
            support.log("Wrong URL format, maybe protocol is missing.");
            }
        
    }   catch (Exception e) {
        support.log("Site is down or network error.");
        }
    return false;
    }
    
    
    /**
     * Checks, if the given remoteAddress (URL to Sim.-Server) is correct
     * @param urlString The URL as String to be checked, if this is the available Sim.-Server
     * @return True if URL is correct and server is working, else false
     * To be modified by: Group studies 2014
     */
    public static boolean getRemoteAddress(String urlString){
        //TODO DISTRIBUTEDSERVER
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
    public static typedef.typeOfOptimization getChosenOptimizerType() {
        return chosenOptimizerType;
    }

    /**
     * @param aChosenOptimizerType the chosenOptimizerType to set
     */
    public static void setChosenOptimizerType(typedef.typeOfOptimization aChosenOptimizerType) {
        chosenOptimizerType = aChosenOptimizerType;
    }

    /**
     * @return the chosenSimulatorType
     */
    public static typeOfSimulator getChosenSimulatorType() {
        return chosenSimulatorType;
    }

    /**
     * @param aChosenSimulatorType the chosenSimulatorType to set
     */
    public static void setChosenSimulatorType(typeOfSimulator aChosenSimulatorType) {
        chosenSimulatorType = aChosenSimulatorType;
        getMainFrame().setBenchmarkFunctionComboboxEnabled(chosenSimulatorType.equals(typeOfSimulator.Benchmark));

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
    //Shortcut for benchmark-Simulators
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            support.log("InterruptedException in main loop of optimization. Optimization aborted.");
        }
    if(mySimulator.getStatus()>=100)return true;
    //End of shortcut
            setStatusText("Simulations started.");
                while(mySimulator.getStatus()<100){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        support.log("InterruptedException in main loop of optimization. Optimization aborted.");
                        statusLabel.setText("Aborted / Error");
                    }
                //setStatusText("Done "+ mySimulator.getStatus() +"% ");
                
                
                timeoutCounter--;
                    //Break if timeout is reached
                    if (timeoutCounter<=1){
                        support.log("Timeout for simulation reached. Aborting simulation.");
                        return false;}

                    if(support.isCancelEverything()){
                        support.log("Waiting for Simulator canceled by user.");
                        return false;
                    }
                }
                simulationCounter=mySimulator.getSimulationCounter();
                //getMainFrame().updateSimulationCounterLabel(simulationCounter);
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
     * Fits every Parameter in ArrayList to BaseParameterset
     * Start-End-Value and Stepping is set
     * Name is changed if external parameter
     *
     */
    public static ArrayList<parameter> fitParametersetToBaseParameterset(ArrayList<parameter> pList){
    ArrayList<parameter> baseList = getParameterBase();

    if(baseList==null){
    log("ParameterBase is NULL! Please set it before starting Optimization Algorithm.");
    return null;
    }

        for(int i=0;i<pList.size();i++){
        parameter tmpP=pList.get(i);

        //Change the names
        tmpP.setName(translateParameterNameFromLogFileToTable(tmpP.getName()));
        parameter baseP=getParameterByName(baseList, tmpP.getName());
            if(baseP!=null){
            tmpP.setStartValue(baseP.getStartValue());
            tmpP.setEndValue(baseP.getEndValue());
            tmpP.setStepping(baseP.getStepping());
            }
        }
    return pList;
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

    /**
     * @return the parameterBase
     */
    public static ArrayList<parameter> getParameterBase() {
        return parameterBase;
    }

    /**
     * @param aParameterBase the parameterBase to set
     */
    public static void setParameterBase(ArrayList<parameter> aParameterBase) {
        parameterBase = aParameterBase;
    }

    /**
     * @return the cancelEverything
     */
    public static boolean isCancelEverything() {
        return cancelEverything;
    }

    /**
     * @param aCancelEverything the cancelEverything to set
     */
    public static void setCancelEverything(boolean aCancelEverything) {
        if(aCancelEverything){
        setStatusText("All Operations will be canceled.");
        }
        
        cancelEverything = aCancelEverything;
    }

    /**
     * @return the logToFile
     */
    public static boolean isLogToFile() {
        return logToFile;
    }

    /**
     * @param aLogToFile the logToFile to set
     */
    public static void setLogToFile(boolean aLogToFile) {
        logToFile = aLogToFile;
    }

    /**
     * Shrink an ArrayList to it` first member
     * needed by some Opti-Algorithms
     */
    public static ArrayList shrinkArrayListToFirstMember(ArrayList l){
    if (l==null) {return null;}

        if(l.size()>1){
            for(int i=1;i<l.size()-1;i++){
            l.remove(i);
            }
        }
    return l;
    }

    /**
     * Returns a list of Parameters that are intern and changable
     */
    public static ArrayList<parameter> getListOfChangableParameters(ArrayList<parameter> sourceList){
    //ArrayList<parameter> parameterset = support.getCopyOfParameterSet(sourceList);
    ArrayList<parameter> listOfChangableParameters = new ArrayList<parameter>();
        //Count the number of changable parameters
        //this.numberOfChangableParameters=0;
        for(int i=0;i<sourceList.size();i++){
                parameter p=sourceList.get(i);
                if(p.isIteratableAndIntern()){
                //this.numberOfChangableParameters++;
                listOfChangableParameters.add(p);
                }
            }
    //support.log("There are "+listOfChangableParameters.size()+" changable parameter.");
    return listOfChangableParameters;
    }

    /**
     * Prints stats abut memory usage to logfile and log-window
     */
    public static void printMemoryStats(){
    int MegaBytes = 1024*1024;
    long freeMemory = Runtime.getRuntime().freeMemory()/MegaBytes;
    long totalMemory = Runtime.getRuntime().totalMemory()/MegaBytes;
    long maxMemory = Runtime.getRuntime().maxMemory()/MegaBytes;
    
    log("Memory usage: "+(maxMemory-freeMemory)*100/maxMemory +"% of "+maxMemory+" Mb . Init was "+totalMemory+" Mb.");
    }
    
    /**
     * Update an info label or spinner
     */
    public static void updateMemoryPrograssbar(){
    int MegaBytes = 1024*1024;
    long freeMemory = Runtime.getRuntime().freeMemory()/MegaBytes;
    long totalMemory = Runtime.getRuntime().totalMemory()/MegaBytes;
    long maxMemory = Runtime.getRuntime().maxMemory()/MegaBytes;
    
    getMainFrame().setMemoryProgressbar((int) ((maxMemory-freeMemory)*100/maxMemory) );
    
    }
    

    /**
     * @return the logToWindow
     */
    public static boolean isLogToWindow() {
        return logToWindow;
    }

    /**
     * @param aLogToWindow the logToWindow to set
     */
    public static void setLogToWindow(boolean aLogToWindow) {
        logToWindow = aLogToWindow;
    }

    /**
     * @return the chosenBenchmarkFunction
     */
    public static typeOfBenchmarkFunction getChosenBenchmarkFunction() {
        return chosenBenchmarkFunction;
    }

    /**
     * @param aChosenBenchmarkFunction the chosenBenchmarkFunction to set
     */
    public static void setChosenBenchmarkFunction(typeOfBenchmarkFunction aChosenBenchmarkFunction) {
        chosenBenchmarkFunction = aChosenBenchmarkFunction;
    }


    /**
     * Shows a little spinning indicator in a given JLabel
     * useful to show, that something is going on...
     * @param l JLabel to show the spinning characters
     */
    public static void spinInLabel(){
    mainFrame.jLabelSpinning.setText(getNextSpinningChar(mainFrame.jLabelSpinning.getText()));
    lastTimeOfSpinning=(Calendar.getInstance().getTimeInMillis());
    }

    /**
     * Checks if spinning label should be seen anymore
     * If spinning showtime is over, spinning-labe is set to empty
     */
    public static void checkSpinningShowTime(){
        if(((Calendar.getInstance().getTimeInMillis())-lastTimeOfSpinning)>=DEFAULT_MEMORYPRINT_INTERVALL){
        mainFrame.jLabelSpinning.setText("");
        }
    }
    
    
    /**
     * Returns next spinning char for displaying a spinning wheel in a label
     * @param oldChar actual char in label to calculate the next
     * @return next char to be displayed in label
     */
    public static String getNextSpinningChar(String oldChar){
    if(oldChar.equals("-")){return "\\";}
    if(oldChar.equals("\\")){return "|";}
    if(oldChar.equals("|")){return "/";}
    if(oldChar.equals("/")){return "-";}
    return "-";
    }

    /**
     * @return the originalParameterBase
     */
    public static ArrayList<parameter> getOriginalParameterBase() {
        return originalParameterBase;
    }

    /**
     * @param aOriginalParameterBase the originalParameterBase to set
     */
    public static void setOriginalParameterBase(ArrayList<parameter> aOriginalParameterBase) {
        originalParameterBase = aOriginalParameterBase;
    }
    
    /**
     * Sets the text of status label
     * 
     */
    public static void setStatusText(String s){
    statusLabel.setText(s);
    }
    
    public static void simOptiOperationSuccessfull(String message){
    mainFrame.operationSucessfull(message);
    }
    
    public static void simOptiOperationCanceled(String message){
    mainFrame.operationCanceld(message);
    }
    
    public static void waitForSimulatorAsynchronous(final Simulator mySimulator, final SimOptiCallback listener){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mySimulator.getStatus()>=100){
                listener.operationSucessfull("The end.");
                this.cancel();
                }
                if(support.isCancelEverything()){
                listener.operationCanceld("Operation canceled by user.");
                this.cancel();
                }
            }
        }, 1000*support.DEFAULT_MEMORYPRINT_INTERVALL, 1000*support.DEFAULT_MEMORYPRINT_INTERVALL);

    }
    
    public static void waitForOptimizerAsynchronous(final Optimizer myOptimizer, final SimOptiCallback listener){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (myOptimizer.getOptimum()!=null){
                listener.operationSucessfull("The end.");
                this.cancel();
                }
                if(support.isCancelEverything()){
                listener.operationCanceld("Operation canceled by user.");
                this.cancel();
                }
            }
        }, 1000*support.DEFAULT_MEMORYPRINT_INTERVALL, 1000*support.DEFAULT_MEMORYPRINT_INTERVALL);

    
    }
}


