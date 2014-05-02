/*
 * Provides some supporting methods to convert things...
 * Stores some references in static fields
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
 

package timenetexperimentgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/**
 *
 * @author Christoph Bodenstein
 */
public class support {
public static final float DEFAULT_STEPPING=(float)1.0;
private static JLabel statusLabel=null;//The label for showing status information
private static String originalFilename=null;//The original SCPN source file to fork for every simulation
private static MainFrame mainFrame=null;//The Main Frame of the program
private static JTabbedPane measureFormPane=null;//The tabbed pane with some Measurement-forms inside to select the optimization targets
private static String pathToTimeNet=null;//The path to TimeNet.jar
private static String tmpPath=null;//The path, where all simulation files (xml), source files and logs will be stored
private static SimulationCache mySimulationCache=null;  
private static boolean cachedSimulationEnabled=false;
private static String remoteAddress=null;

    

    public final static String translateParameterNameFromLogFileToTable(String s){
        if(s.equals("Configured-ConfidenceIntervall")){
        return "ConfidenceIntervall";
        }
        
    return s;
    }

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
     * @return the statusLabel
     */
    public static JLabel getStatusLabel() {
        return statusLabel;
    }

    /**
     * @param aStatusLabel the statusLabel to set
     */
    public static void setStatusLabel(JLabel aStatusLabel) {
        statusLabel = aStatusLabel;
    }

    /**
     * @return the originalFilename
     */
    public static String getOriginalFilename() {
        return originalFilename;
    }

    /**
     * @param aOriginalFilename the originalFilename to set
     */
    public static void setOriginalFilename(String aOriginalFilename) {
        originalFilename = aOriginalFilename;
    }

    /**
     * @return the mainFrame
     */
    public static MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * @param aMainFrame the mainFrame to set
     */
    public static void setMainFrame(MainFrame aMainFrame) {
        mainFrame = aMainFrame;
    }

    /**
     * @return the measureFormPane
     */
    public static JTabbedPane getMeasureFormPane() {
        return measureFormPane;
    }

    /**
     * @param aMeasureFormPane the measureFormPane to set
     */
    public static void setMeasureFormPane(JTabbedPane aMeasureFormPane) {
        measureFormPane = aMeasureFormPane;
    }

    /**
     * @return the pathToTimeNet
     */
    public static String getPathToTimeNet() {
        return pathToTimeNet;
    }

    /**
     * @param aPathToTimeNet the pathToTimeNet to set
     */
    public static void setPathToTimeNet(String aPathToTimeNet) {
        pathToTimeNet = aPathToTimeNet;
    }

    /**
     * @return the tmpPath
     */
    public static String getTmpPath() {
        return tmpPath;
    }

    /**
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
    public static float round(float valueToRound){
    return (float)Math.round(valueToRound * 1000) / 1000;        
    }
    
     /**
     * Some float functions for parameter casting, will be overloaded
     * @param f float to be returned normalized and casted
     * @return float value
     */
    public static float getFloat(float f){
    return f;
    }
    
    /**
     * Casts a String to float and returns this float
     * @param s String to be converted into float
     * @return float value of input String
     */
    public static float getFloat(String s){
    return Float.parseFloat(s.replace(',', '.'));
    }

    /**
     * Casts an int to float
     * @param i int value to cast to float
     * @return float value of given int
     */
    public static float getFloat(int i){
    return (float)i;
    }
    
    
    /**
     * Casts a float to int
     * @param f float value to cast to int
     * @return int value of given float
     */
    public static int getInt(float f){
        return (int)f;
    }
    
    /**
     * Converts a float into a String
     * @param f float value to be converted into String
     * @return String which represents the input float value
     */
    public static String getString(float f){
    return String.valueOf(f);
    }
    
     /**
     * Returns a String with float value, where comma is used instead of point as decimal-separator
     * @param f Float value to be converted into a String with comma as decimal-point
     * @return String representing the float value with comma as decimal-point
     */
    public static String getCommaFloat(float f){
    //System.out.print("UnFormated float is "+f);
    String returnValue=getCommaFloat( Float.toString(f) ) ;
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
     * @return the cachedSimulationEnabled
     */
    public static boolean isCachedSimulationEnabled() {
        return cachedSimulationEnabled;
    }

    /**
     * @param aCachedSimulationEnabled the cachedSimulationEnabled to set
     */
    public static void setCachedSimulationEnabled(boolean aCachedSimulationEnabled) {
        cachedSimulationEnabled = aCachedSimulationEnabled;
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
        
        if(m.getParameterList()!=null){
        support.log("---Printing parameterlist---");
        ArrayList<parameter> pList=m.getParameterList();
            for(int i=0;i<pList.size();i++){
            support.log("Value of "+pList.get(i).getName() +" is: "+pList.get(i).getValue());
            }
        support.log("---End of parameterlist---");
        }
        support.log("Used CPU-Time: " +m.getCPUTime());
        support.log("***** End of Measure "+m.getMeasureName()+" ******");
        support.log(footer);

    }
    
     /**
     * Adds Lines to logfile with the data from given parserlist
     * @param pList List of parsers, which includes the data from one simulation each
     * @param logFileName The path and name of the general log file
     */
    public static void addLinesToLogFileFromListOfParser(ArrayList<parser> pList, String logFileName){
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
                    for(int i1=0;i1<exportMeasure.getParameterList().size();i1++){
                    line=line+";"+exportMeasure.getParameterList().get(i1).getName();
                    }
                    try {
                        fw.write(line);
                    } catch (IOException ex) {
                        support.log("Error writing Header to Summary-log-file.");
                    }
            }


            for(int i=0;i<pList.size();i++){
            parser myParser=pList.get(i);
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
                line=exportMeasure.getMeasureName()+";"+support.getCommaFloat(exportMeasure.getMeanValue())+";"+support.getCommaFloat(exportMeasure.getVariance())+";"+support.getCommaFloat(exportMeasure.getConfidenceInterval()[0])+";"+support.getCommaFloat(exportMeasure.getConfidenceInterval()[1])+";"+support.getCommaFloat(exportMeasure.getEpsilon())+";"+support.getCommaFloat(myParser.getSimulationTime());
                    for(int c=0;c<exportMeasure.getParameterList().size();c++){
                    line=line+";"+support.getCommaFloat(exportMeasure.getParameterList().get(c).getValue());
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
    
    public static void addLinesToLogFile(parser p, String logFileName){
    ArrayList<parser> myParserList=new ArrayList<parser>();
    myParserList.add(p);
        addLinesToLogFileFromListOfParser(myParserList, logFileName);
    }
    
    
    /**
     * logs the data either to file, System-log, etc.
     * 
     * @param s String to be logged.
     */
    public static void log(String s){
    System.out.println(s);
    }
    
    
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
    
    
    public static String getReMoteAddress(){
    if(remoteAddress==null)return "";else return remoteAddress;
    }
    public static void setRemoteAddress(String s){
    remoteAddress=s;
    }
    
    /**
     * Checks, if Timenet is availabel at giben Path, otherwise simulation run is not possible
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
}


