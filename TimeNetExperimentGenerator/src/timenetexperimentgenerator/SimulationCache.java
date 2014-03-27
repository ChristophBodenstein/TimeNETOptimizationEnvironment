/*
 * Reads the Simulation cache file
 * Checks the data and reformat the parameter table
 * Provides Simulation results
 */

package timenetexperimentgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.table.TableModel;

/**
 *
 * @author sse
 */
public class SimulationCache {
ArrayList<SimulatedExperiment> ListOfExperiments;

    public SimulationCache() {
        this.ListOfExperiments = new ArrayList<SimulatedExperiment>();
    }
    

   
    protected boolean parseSimulationCacheFile(String filename, ArrayList<MeasureType> listOfMeasures, parameterTableModel myParameterTableModel ){
    ArrayList <String[]> listOfStringLines=new ArrayList<String[]>();
    //TODO
        //read file
    BufferedReader reader;
    try {
        reader = new BufferedReader(new FileReader(new File(filename)));
        String current = reader.readLine();
        //Number of Experiments, first number of lines is counted
        int numberOfExperiments=1;//Don` count first line
        while (current != null) {
            //processCsvLine(current);
            String[] tmpString=current.split(";");
            listOfStringLines.add(tmpString);
            numberOfExperiments++;
            current = reader.readLine();
        }
        reader.close();
        
        //Get Names of Parameters
        String[] listOfCachedParameterNames=new String[listOfStringLines.get(0).length-8];
        for(int i=0;i<listOfCachedParameterNames.length;i++){
        listOfCachedParameterNames[i]=listOfStringLines.get(0)[i+7];
        System.out.println("ParameterName " +(i)+" = "+listOfCachedParameterNames[i]);
        }
        
        //Get Names of Measures
        boolean foundDublicate=false;
        ArrayList<String> listOfCachedMeasureNames=new ArrayList<String>();
        listOfCachedMeasureNames.add(listOfStringLines.get(1)[0]);
        int i=2;
        while(!foundDublicate){
            if(listOfCachedMeasureNames.get(0).equals(listOfStringLines.get(i)[0])){
            foundDublicate=true;
            }else{
            listOfCachedMeasureNames.add(listOfStringLines.get(i)[0]);
            }
            i++;
        }
        //Debug Output of Measurement Names
        for(i=0;i<listOfCachedMeasureNames.size();i++){
        System.out.println("Name of Measure: "+listOfCachedMeasureNames.get(i));
        }
        //Check Length of List of Measurements
        if(listOfCachedMeasureNames.size()!=listOfMeasures.size()){
            System.out.println("Count of cached Measures differs from Count of given Measures");
        return false;
        }
        //Calc real number of experiments
        numberOfExperiments=numberOfExperiments/listOfCachedMeasureNames.size();
        
        //Check length of List of Parameter
        if(listOfCachedParameterNames.length!=myParameterTableModel.getRowCount()){
            System.out.println("Count of cached Parameters differs from Count of given Parameters");
        return false;
        }
        
        //Generate List of Simulated Experiments
        SimulatedExperiment[] myExperiments=new SimulatedExperiment[numberOfExperiments];
        for(i=0;i<numberOfExperiments;i++){
            ArrayList<MeasureType> tmpMeasureList=new ArrayList<MeasureType>();
            for(int c=0;c<listOfCachedMeasureNames.size();c++){
            MeasureType tmpMeasure=new MeasureType();
            //Line number in read cache file
            int lineNumber=i*listOfCachedMeasureNames.size()+c+1;
            tmpMeasure.setMeasureName(listOfStringLines.get(lineNumber)[0]);
            tmpMeasure.setMeanValue(Float.parseFloat(listOfStringLines.get(lineNumber)[1]));
            tmpMeasure.setVariance(Float.parseFloat(listOfStringLines.get(lineNumber)[2]));
            float[] tmpConf={Float.parseFloat(listOfStringLines.get(lineNumber)[3]),Float.parseFloat(listOfStringLines.get(lineNumber)[4])};
            tmpMeasure.setConfidenceInterval(tmpConf);
            tmpMeasure.setEpsilon(Float.parseFloat(listOfStringLines.get(lineNumber)[5]));
            //TODO Parameterliste erstellen, SimulatedExperiments sind überflüssig
            tmpMeasure.setParameterList(null);
            
            tmpMeasureList.add(tmpMeasure);
            
            }
            
            myExperiments[i]=new SimulatedExperiment();
            
        }
        
        
        
        
     } catch (IOException ex) {
        System.out.println("Error while reading the Simulation Cache File.");
    }
        //Get all parameters
        //Get all Results
        //Check if Parameters fit to given SCPN, if not, return false
        //Reformat Parameter-Table
    return true;
    }
    
}
