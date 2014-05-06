/*
 * Reads the Simulation cache file
 * Checks the data and reformat the parameter table
 * Provides Simulation results

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
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
String[] listOfCachedParameterNames;
double[] listOfCachedParameterMin;
double[] listOfCachedParameterMax;
double[] listOfCachedParameterStepping;
ArrayList<MeasureType> MeasureList;
private int localSimulationCounter=0;


    public SimulationCache() {
        this.MeasureList=new ArrayList<MeasureType>();
    }
    

   
    protected boolean parseSimulationCacheFile(String filename, ArrayList<MeasureType> listOfMeasures, parameterTableModel myParameterTableModel, MainFrame myParentFrame){
    ArrayList <String[]> listOfStringLines=new ArrayList<String[]>();
        //read file
    BufferedReader reader;
    try {
        reader = new BufferedReader(new FileReader(new File(filename)));
        String current = reader.readLine();
        //Number of Experiments, first number of lines is counted
        int numberOfExperiments=1;//Don`t count first line
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
        listOfCachedParameterMin=new double[listOfStringLines.get(0).length-8];
        listOfCachedParameterMax=new double[listOfStringLines.get(0).length-8];
        listOfCachedParameterStepping=new double[listOfStringLines.get(0).length-8];
        double tmpValue=0.0;
        int column=0;

        //Check length of List of Parameter
        if(listOfCachedParameterNames.length!=myParameterTableModel.getRowCount()){
            support.log("Count of cached Parameters differs from Count of given Parameters.");
        return false;
        }   support.log("Count of cached Parameters seems correct.");


        for(int i=0;i<listOfCachedParameterNames.length;i++){
        column=i+7;
        listOfCachedParameterNames[i]=support.translateParameterNameFromLogFileToTable(listOfStringLines.get(0)[column]);
        listOfCachedParameterMax[i]=Double.NEGATIVE_INFINITY;
        listOfCachedParameterMin[i]=Double.POSITIVE_INFINITY;
        listOfCachedParameterStepping[i]=support.DEFAULT_STEPPING;
            //Walk through a column and get Min and Max Values
            for(int line=1;line<listOfStringLines.size();line++){
            tmpValue=support.getDouble(listOfStringLines.get(line)[column]);
            listOfCachedParameterMax[i]=Math.max(tmpValue, listOfCachedParameterMax[i]);
            listOfCachedParameterMin[i]=Math.min(tmpValue, listOfCachedParameterMin[i]);
                if(line<listOfStringLines.size()-1){
                    try{
                    //listOfCachedParameterStepping[i]=Math.max(listOfCachedParameterStepping[i], Math.abs(tmpValue-support.getFloatFromString(listOfStringLines.get(line+1)[column])));
                    double tmpValue2=support.getDouble(listOfStringLines.get(line+1)[column]);
                    //TODO: Make this a setting in prefernces frame, how many digits to be used!
                    tmpValue2=(Math.abs(tmpValue-tmpValue2));
                    tmpValue2=support.round(tmpValue2);
                    if(tmpValue2>0){
                    listOfCachedParameterStepping[i]=tmpValue2;
                    //support.log("Result of round: "+tmpValue2);
                    //support.log("Setting new Value for Stepping.");
                    }
                    
                    }catch(Exception e){
                    support.log("Maybe there was an error getting the stepping from cache file.");
                    }
                }
            }
        if(listOfCachedParameterStepping[i]<=0.0){listOfCachedParameterStepping[i]=(float)1.0;}
        //support.log("ParameterName " +(i)+" = "+listOfCachedParameterNames[i]+" with Min="+listOfCachedParameterMin[i]+" and Max="+listOfCachedParameterMax[i]+" and Stepping="+listOfCachedParameterStepping[i]);
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
        support.log("Name of read Measures are: ");
        for(i=0;i<(listOfCachedMeasureNames.size())-1;i++){
        System.out.print(listOfCachedMeasureNames.get(i)+", ");
        }
        support.log(listOfCachedMeasureNames.get(listOfCachedMeasureNames.size()-1));

        
        //Check Length of List of Measurements
        if(listOfCachedMeasureNames.size()!=listOfMeasures.size()){
            support.log("Count of cached Measures differs from Count of given Measures.");
        return false;
        }   support.log("Count of cached Measures seems correct.");

        //Calc real number of experiments
        numberOfExperiments=numberOfExperiments/listOfCachedMeasureNames.size();
        
        
        
        //So the number of parameters seems ok, let` check the names
        for(i=0;i<listOfCachedParameterNames.length;i++){
            //support.log("Checking  "+listOfCachedParameterNames[i]+" ... number "+i);
            if((myParameterTableModel.getValueByName(listOfCachedParameterNames[i], "StartValue"))==null){
            //support.log((myParameterTableModel.getValueByName(listOfCachedParameterNames[i], "StartValue"))!=null );
            //One Value is "" --> Parameter is not available --> Exit
            support.log("The parameter "+listOfCachedParameterNames[i]+" seems not available in table.");
            return false;
            }
        }   support.log("All parameters seem available in table.");

        
        this.MeasureList=new ArrayList<MeasureType>();
        //Generate List of Simulated Experiments
        for(i=0;i<numberOfExperiments;i++){
            
            for(int c=0;c<listOfCachedMeasureNames.size();c++){
            MeasureType tmpMeasure=new MeasureType();
            //Line number in read cache file
            int lineNumber=i*listOfCachedMeasureNames.size()+c+1;
            tmpMeasure.setMeasureName(listOfStringLines.get(lineNumber)[0]);
            tmpMeasure.setMeanValue(support.getDouble(listOfStringLines.get(lineNumber)[1]));
            tmpMeasure.setVariance(support.getDouble(listOfStringLines.get(lineNumber)[2]));
            double[] tmpConf={support.getDouble(listOfStringLines.get(lineNumber)[3]),support.getDouble(listOfStringLines.get(lineNumber)[4])};
            tmpMeasure.setConfidenceInterval(tmpConf);
            tmpMeasure.setEpsilon(support.getDouble(listOfStringLines.get(lineNumber)[5]));
            tmpMeasure.setSimulationTime(support.getDouble(listOfStringLines.get(lineNumber)[6]));
            //CPU-Time is in last column
            tmpMeasure.setCPUTime(support.getDouble(listOfStringLines.get(lineNumber)[7+listOfCachedParameterNames.length]));
            //support.log("CPU-Time of "+tmpMeasure.getMeasureName() +" is " +tmpMeasure.getCPUTime()+".");
            
            ArrayList<parameter> tmpParameterList=new ArrayList<parameter>();
                for(int i1=0;i1<listOfCachedParameterNames.length;i1++){
                column=i1+7;
                parameter tmpParameter=new parameter();
                tmpParameter.setName(support.translateParameterNameFromLogFileToTable(listOfCachedParameterNames[i1]));
                tmpParameter.setEndValue(support.round(listOfCachedParameterMax[i1]));
                tmpParameter.setStartValue(support.round(listOfCachedParameterMin[i1]));
                tmpParameter.setStepping(support.round(listOfCachedParameterStepping[i1]));
                //Get and save Value of this Parameter
                //It is in the correct Column of the actual line
                //We did not change the order of Parameters (it`s the same like in the raw file)
                //tmpParameter.setValue(support.translateParameterNameFromLogFileToTable(listOfStringLines.get(lineNumber)[column]));
                tmpParameter.setValue(support.round(support.getDouble(listOfStringLines.get(lineNumber)[column])));
                
                tmpParameterList.add(tmpParameter);
                }

            tmpMeasure.setParameterList(tmpParameterList);
            
            this.MeasureList.add(tmpMeasure);
            
            }
        }

        //Reformat Parameter-Table
        this.reformatParameterTable(myParameterTableModel);
        //Refresh Design Space label of MainFrame
        myParentFrame.calculateDesignSpace();

     } catch (IOException ex) {
        support.log("Error while reading the Simulation Cache File.");
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
            myTableModel.setValueByName(listOfCachedParameterNames[i], "StartValue", listOfCachedParameterMin[i]);
            myTableModel.setValueByName(listOfCachedParameterNames[i], "EndValue", listOfCachedParameterMax[i]);
            myTableModel.setValueByName(listOfCachedParameterNames[i], "Stepping", listOfCachedParameterStepping[i]);

        }
    }
    
    public boolean checkIfAllParameterMatchTable(parameterTableModel myTableModel){
    if (listOfCachedParameterNames==null){return false;}
        //Names are equal --> format the table so that Start-,End-,Stepping-Value match
        for(int i=0;i<listOfCachedParameterNames.length;i++){
            if(myTableModel.getDoubleValueByName(listOfCachedParameterNames[i], "StartValue") == (listOfCachedParameterMin[i])){}else{return false;}
            if(myTableModel.getDoubleValueByName(listOfCachedParameterNames[i], "EndValue") == (listOfCachedParameterMax[i])){}else{return false;}
            if(myTableModel.getDoubleValueByName(listOfCachedParameterNames[i], "Stepping") == (listOfCachedParameterStepping[i])){}else{return false;}
        }
    return true;
    }
    
    /**
    * Returns one Measure(given by name), selected by one parameterset(ParameterList)
    * @param parameterList given Set of Parameters to by virtually simulated
    * @param MeasureName given Name of Measure to get the simulaion value (needle)
    * @return Measure
    */
    public MeasureType getMeasureByParameterList(ArrayList<parameter> parameterList, String MeasureName){
    MeasureType tmpMeasure;
        //Go through all Measures, find the one with the same parameterlist
        for(int i=0;i<this.MeasureList.size();i++){
        tmpMeasure=this.MeasureList.get(i);
            if((tmpMeasure.getMeasureName().equals(MeasureName))&&(compareParameterList(parameterList, tmpMeasure.getParameterList()))){
            return tmpMeasure;
            }
        }
    //TODO: If not found, then find the nearest one       
    //Return null, if not found
    return null;
    }

    /**
    * Returns all Measures, selected by one parameterset(ParameterList)
    * @param parameterList given Set of Parameters to by virtually simulated
    * @return ArrayList of MeasureTypes
    */
    public ArrayList<MeasureType> getAllMeasuresWithParameterList(ArrayList<parameter> parameterList){
    MeasureType tmpMeasure;
    ArrayList<MeasureType> myTmpList=new ArrayList();
    
    support.log("Size of All Measures from File: "+MeasureList.size());
        //Go through all Measures, find the one with the same parameterlist
        for(int i=0;i<this.MeasureList.size();i++){
        tmpMeasure=this.MeasureList.get(i);
            if(compareParameterList(parameterList, tmpMeasure.getParameterList())){
            myTmpList.add(tmpMeasure);
            }
        }
    return myTmpList;
    //TODO: If not found, then find the nearest one       
    //Return null, if not found
    }
    
    
    /**
     * Checks if two parametersets are equal
     * @param listA will be compered to
     * @param listB 
     * @return true if parametersets (only the values and names) are equal, else false
     */
    private boolean compareParameterList(ArrayList<parameter> listA, ArrayList<parameter> listB){
    String nameA="";
    parameter tmpParameterA, tmpParameterB;
        if(listA.size()!=listB.size()){
        support.log("Size of needle-ParameterList is different from haystack-ParameterList.");
        return false;
        }
        
        for(int i=0;i<listA.size();i++){
        tmpParameterA=listA.get(i);
        nameA=support.translateParameterNameFromLogFileToTable(tmpParameterA.getName());
            tmpParameterB=this.findParameterInListByName(nameA, listB);
                if(tmpParameterB==null){
                support.log("ParameterB is null.");
                return false;
                }
            //Parameter found, now check the values of this parameter
                if(support.round(support.getDouble(tmpParameterA.getValue()))!=support.round(support.getDouble(tmpParameterB.getValue()))){
                //support.log("Parameter Values differ.");
                return false;
                }
        }     
    return true;
    }
    
    
    /**
     * finds a parameter by Name in an ArrayList of parameters
     * @param name Name of Parameter to search (Needle)
     * @param list ArrayList of Paramater (Haystack)
     * @return parameter with the given name or null
     */
    private parameter findParameterInListByName(String name, ArrayList<parameter> list){
    parameter testParameter;
        for (int i=0;i<list.size();i++){
            testParameter=list.get(i);
            if(testParameter.getName().equals(name)){
            return testParameter;
            }
        }
    return null;
    }
    
    
    
    /**
     * Same function like in real simulator
     * Given a list of parametersets, it returns a list of Simulation results (parsers)
     * 
     * @param parameterListArray List of Parameter-Arrays to be "simulated"
     * @param simulationCounter Counter of Simulations
     * @return List of parsers with Simulation-Results, like in real simulation
     */
    public ArrayList<parser> getListOfCompletedSimulationParsers(ArrayList<parameter[]> parameterListArray, int simulationCounter){
    setLocalSimulationCounter(simulationCounter);
    ArrayList<parser> myParserList=new ArrayList<parser>();
    
    
        for(int i=0;i<parameterListArray.size();i++){
            //Create Arraylist from aray of parameters
            ArrayList<parameter> tmpParameterList=new ArrayList<parameter>();
            for(int c=0;c<parameterListArray.get(i).length;c++){
            tmpParameterList.add(parameterListArray.get(i)[c]);
            }
            
            //Get local simulation results
            ArrayList<MeasureType> listOfMeasureWithGivenParameters=this.getAllMeasuresWithParameterList(tmpParameterList);
            //support.log("Size of ParameterList: "+ tmpParameterList.size() + " results in " +listOfMeasureWithGivenParameters.size()+ " Measurements.");
            
            //append if listSize is > 0
            if(listOfMeasureWithGivenParameters.size()>0){
            parser tmpParser=new parser();
            tmpParser.setMeasures(listOfMeasureWithGivenParameters);
            tmpParser.setSimulationTime(listOfMeasureWithGivenParameters.get(i).getSimulationTime());
            tmpParser.setCPUTime(support.getInt(listOfMeasureWithGivenParameters.get(i).getCPUTime()));
            myParserList.add(tmpParser);
            simulationCounter++;
            }
            
        }
    if(myParserList.size()==0){return null;}
    return myParserList;
    }

    /**
     * @return the localSimulationCounter
     */
    public int getLocalSimulationCounter() {
        return localSimulationCounter;
    }

    /**
     * @param localSimulationCounter the localSimulationCounter to set
     */
    public void setLocalSimulationCounter(int localSimulationCounter) {
        this.localSimulationCounter = localSimulationCounter;
    }
    
    
}
