/*
 * Provides some supporting methods to convert things...
 * Stores some references in static fields
 */

package timenetexperimentgenerator;

import java.io.File;
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
            System.out.println("choosen dir: "+outputDir);
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
    //System.out.println("  --  Formated float is "+returnValue);
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
    //System.out.println("  --  Formated String is "+returnValue);
    return returnValue;
    }

    
    /**
     * Returns a String containing a float with point as decimal delimiter
     * @param f String containing a Float with commma as decimal delimiter
     * @return String conaining a Float with point as decimal delimiter
     */
    public static String getPointFloat(String f){
    String returnValue=f.replace(",", ".");
    //System.out.println("  --  Formated String is "+returnValue);
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
    System.out.println("Printing of Measure not possible. Measure is null.");
        return;
    }    
        System.out.println(header);
        System.out.println("***** Start of Measure "+m.getMeasureName()+" ******");
        System.out.println("Mean Value: "+support.getCommaFloat(m.getMeanValue()));
        System.out.println("Variance: "+support.getCommaFloat(m.getVariance()));
        System.out.println("Confidence-Min: "+support.getCommaFloat(m.getConfidenceInterval()[0]));
        System.out.println("Confidence-Max: "+support.getCommaFloat(m.getConfidenceInterval()[1]));
        System.out.println("Epsilon: "+support.getCommaFloat(m.getEpsilon()));
        
        if(m.getParameterList()!=null){
        System.out.println("---Printing parameterlist---");
        ArrayList<parameter> pList=m.getParameterList();
            for(int i=0;i<pList.size();i++){
            System.out.println("Value of "+pList.get(i).getName() +" is: "+pList.get(i).getValue());
            }
        System.out.println("---End of parameterlist---");
        }
        
        System.out.println("***** End of Measure "+m.getMeasureName()+" ******");
        System.out.println(footer);

    }
}


