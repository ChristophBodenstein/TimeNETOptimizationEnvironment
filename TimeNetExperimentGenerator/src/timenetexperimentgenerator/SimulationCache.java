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
 * @author Christoph Bodenstein
 */
public class SimulationCache {
ArrayList<SimulatedExperiment> ListOfExperiments;
String[] listOfCachedParameterNames;
float[] listOfCachedParameterMin;
float[] listOfCachedParameterMax;
float[] listOfCachedParameterStepping;


    public SimulationCache() {
        this.ListOfExperiments = new ArrayList<SimulatedExperiment>();
    }
    

   
    protected boolean parseSimulationCacheFile(String filename, ArrayList<MeasureType> listOfMeasures, parameterTableModel myParameterTableModel, MainFrame myParentFrame){
    ArrayList <String[]> listOfStringLines=new ArrayList<String[]>();
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
        listOfCachedParameterNames=new String[listOfStringLines.get(0).length-8];
        listOfCachedParameterMin=new float[listOfStringLines.get(0).length-8];
        listOfCachedParameterMax=new float[listOfStringLines.get(0).length-8];
        listOfCachedParameterStepping=new float[listOfStringLines.get(0).length-8];
        float tmpValue=(float)0.0;
        int column=0;

        //Check length of List of Parameter
        if(listOfCachedParameterNames.length!=myParameterTableModel.getRowCount()){
            System.out.println("Count of cached Parameters differs from Count of given Parameters.");
        return false;
        }   System.out.println("Count of cached Parameters seems correct.");


        for(int i=0;i<listOfCachedParameterNames.length;i++){
        column=i+7;
        listOfCachedParameterNames[i]=support.translateParameterNameFromLogFileToTable(listOfStringLines.get(0)[column]);
        listOfCachedParameterMax[i]=(float)Float.NEGATIVE_INFINITY;
        listOfCachedParameterMin[i]=(float)Float.POSITIVE_INFINITY;
        listOfCachedParameterStepping[i]=support.DEFAULT_STEPPING;
            //Walk through a column and get Min and Max Values
            for(int line=1;line<listOfStringLines.size();line++){
            tmpValue=support.getFloatFromString(listOfStringLines.get(line)[column]);
            listOfCachedParameterMax[i]=Math.max(tmpValue, listOfCachedParameterMax[i]);
            listOfCachedParameterMin[i]=Math.min(tmpValue, listOfCachedParameterMin[i]);
                if(line<listOfStringLines.size()-1){
                    try{
                    listOfCachedParameterStepping[i]=Math.max(listOfCachedParameterStepping[i], Math.abs(tmpValue-support.getFloatFromString(listOfStringLines.get(line+1)[column])));
                    }catch(Exception e){
                    System.out.println("Maybe there was an error getting the stepping from cache file.");
                    }
                }
            }

        System.out.println("ParameterName " +(i)+" = "+listOfCachedParameterNames[i]+" with Min="+listOfCachedParameterMin[i]+" and Max="+listOfCachedParameterMax[i]+" and Stepping="+listOfCachedParameterStepping[i]);
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
        System.out.println("Name of read Measures are: ");
        for(i=0;i<(listOfCachedMeasureNames.size())-1;i++){
        System.out.print(listOfCachedMeasureNames.get(i)+", ");
        }
        System.out.println(listOfCachedMeasureNames.get(listOfCachedMeasureNames.size()-1));

        
        //Check Length of List of Measurements
        if(listOfCachedMeasureNames.size()!=listOfMeasures.size()){
            System.out.println("Count of cached Measures differs from Count of given Measures.");
        return false;
        }   System.out.println("Count of cached Measures seems correct.");

        //Calc real number of experiments
        numberOfExperiments=numberOfExperiments/listOfCachedMeasureNames.size();
        
        
        
        //So the number of parameters seems ok, let` check the names
        for(i=0;i<listOfCachedParameterNames.length;i++){
            //System.out.println("Checking  "+listOfCachedParameterNames[i]+" ... number "+i);
            if((myParameterTableModel.getValueByName(listOfCachedParameterNames[i], "StartValue"))==null){
            //System.out.println((myParameterTableModel.getValueByName(listOfCachedParameterNames[i], "StartValue"))!=null );
            //One Value is "" --> Parameter is not available --> Exit
            System.out.println("The parameter "+listOfCachedParameterNames[i]+" seems not available in table.");
            return false;
            }
        }   System.out.println("All parameters seem available in table.");

        
        
        //Generate List of Simulated Experiments
        SimulatedExperiment[] myExperiments=new SimulatedExperiment[numberOfExperiments];
        for(i=0;i<numberOfExperiments;i++){
            ArrayList<MeasureType> tmpMeasureList=new ArrayList<MeasureType>();
            for(int c=0;c<listOfCachedMeasureNames.size();c++){
            MeasureType tmpMeasure=new MeasureType();
            //Line number in read cache file
            int lineNumber=i*listOfCachedMeasureNames.size()+c+1;
            tmpMeasure.setMeasureName(listOfStringLines.get(lineNumber)[0]);
            tmpMeasure.setMeanValue(support.getFloatFromString(listOfStringLines.get(lineNumber)[1]));
            tmpMeasure.setVariance(support.getFloatFromString(listOfStringLines.get(lineNumber)[2]));
            float[] tmpConf={support.getFloatFromString(listOfStringLines.get(lineNumber)[3]),support.getFloatFromString(listOfStringLines.get(lineNumber)[4])};
            tmpMeasure.setConfidenceInterval(tmpConf);
            tmpMeasure.setEpsilon(support.getFloatFromString(listOfStringLines.get(lineNumber)[5]));
            //TODO Parameterliste erstellen, SimulatedExperiments sind überflüssig
            ArrayList<parameter> tmpParameterList=new ArrayList<parameter>();
                for(int i1=0;i1<listOfCachedParameterNames.length;i1++){
                column=i1+7;
                parameter tmpParameter=new parameter();
                tmpParameter.setName(listOfCachedParameterNames[i1]);
                tmpParameter.setEndValue(Float.toString(listOfCachedParameterMax[i1]));
                tmpParameter.setStartValue(Float.toString(listOfCachedParameterMin[i1]));
                tmpParameter.setStepping(Float.toString(listOfCachedParameterStepping[i1]));
                //Get and save Value of this Parameter
                //It is in the correct Column of the actual line
                //We did not change the order of Parameters (it`s the same like in the raw file)
                tmpParameter.setValue(support.translateParameterNameFromLogFileToTable(listOfStringLines.get(lineNumber)[column]));
                tmpParameterList.add(tmpParameter);
                }

            tmpMeasure.setParameterList(tmpParameterList);
            
            tmpMeasureList.add(tmpMeasure);
            
            }
        }

        //Reformat Parameter-Table
        this.reformatParameterTable(myParameterTableModel);
        //Refresh Design Space label of MainFrame
        myParentFrame.calculateDesignSpace();

     } catch (IOException ex) {
        System.out.println("Error while reading the Simulation Cache File.");
    }
        //Get all parameters
        //Get all Results
        //Check if Parameters fit to given SCPN, if not, return false
        

    return true;
    }
    
    public void reformatParameterTable(parameterTableModel myTableModel){
    if (listOfCachedParameterNames==null){return;}
        //Names are equal --> format the table so that Start-,End-,Stepping-Value match
        for(int i=0;i<listOfCachedParameterNames.length;i++){
            myTableModel.setValueByName(listOfCachedParameterNames[i], "StartValue", Float.toString(listOfCachedParameterMin[i]));
            myTableModel.setValueByName(listOfCachedParameterNames[i], "EndValue", Float.toString(listOfCachedParameterMax[i]));
            myTableModel.setValueByName(listOfCachedParameterNames[i], "Stepping", Float.toString(listOfCachedParameterStepping[i]));

        }
    }
    
    public boolean checkIfAllParameterMatchTable(parameterTableModel myTableModel){
    if (listOfCachedParameterNames==null){return false;}
        //Names are equal --> format the table so that Start-,End-,Stepping-Value match
        for(int i=0;i<listOfCachedParameterNames.length;i++){
            if(myTableModel.getValueByName(listOfCachedParameterNames[i], "StartValue").equals(Float.toString(listOfCachedParameterMin[i]))){}else{return false;}
            if(myTableModel.getValueByName(listOfCachedParameterNames[i], "EndValue").equals(Float.toString(listOfCachedParameterMax[i]))){}else{return false;}
            if(myTableModel.getValueByName(listOfCachedParameterNames[i], "Stepping").equals(Float.toString(listOfCachedParameterStepping[i]))){}else{return false;}

        }
    return true;
    }
    
    /*
    * Returns one float value of a Measure, selected by one parameterset(ParameterList)
    */
    public float getMeasureValueByParameterList(ArrayList<parameter> parameterList){
    return (float) 1.0;
    }

    
    
    
}
