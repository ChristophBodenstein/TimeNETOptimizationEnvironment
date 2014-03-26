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

/**
 *
 * @author sse
 */
public class SimulationCache {
ArrayList<SimulatedExperiment> ListOfExperiments;

    public SimulationCache() {
        this.ListOfExperiments = new ArrayList<SimulatedExperiment>();
    }
    

   
    protected boolean parseSimulationCacheFile(String filename){
    ArrayList <String[]> listOfStringLines=new ArrayList<String[]>();
    //TODO
        //read file
    BufferedReader reader;
    try {
        reader = new BufferedReader(new FileReader(new File(filename)));
        String current = reader.readLine();
        while (current != null) {
            //processCsvLine(current);
            String[] tmpString=current.split(";");
            listOfStringLines.add(tmpString);
            current = reader.readLine();
        }
        reader.close();
        
        //Get Names of Parameters
        String[] listOfParameterNames=new String[listOfStringLines.get(0).length-8];
        for(int i=0;i<listOfParameterNames.length;i++){
        listOfParameterNames[i]=listOfStringLines.get(0)[i+7];
        System.out.println("ParameterName " +(i)+" = "+listOfParameterNames[i]);
        }
        
        //Get Names of Measures
        boolean foundDublicate=false;
        ArrayList<String> listOfMeasureNames=new ArrayList<String>();
        listOfMeasureNames.add(listOfStringLines.get(1)[0]);
        int i=2;
        while(!foundDublicate){
            if(listOfMeasureNames.get(0).equals(listOfStringLines.get(i)[0])){
            foundDublicate=true;
            }else{
            listOfMeasureNames.add(listOfStringLines.get(i)[0]);
            }
            i++;
        }
        //Debug Output of Measurement Names
        for(i=0;i<listOfMeasureNames.size();i++){
        System.out.println("Name of Measure: "+listOfMeasureNames.get(i));
        }
        
        //Generate List of Simulated Experiments
        
        
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
