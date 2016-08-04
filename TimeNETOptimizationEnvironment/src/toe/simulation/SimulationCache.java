/*
 * Reads the Simulation cache file
 * Checks the data and reformat the parameter table
 * Provides Simulation results

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.simulation;

import toe.support;
import toe.MainFrame;
import toe.helper.parameterTableModel;
import toe.datamodel.MeasureType;
import toe.datamodel.SimulationType;
import toe.datamodel.parameter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JOptionPane;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimulationCache {

    String[] listOfCachedParameterNames;
    double[] listOfCachedParameterMin;
    double[] listOfCachedParameterMax;
    double[] listOfCachedParameterStepping;
//ArrayList<MeasureType> MeasureList;
    private ArrayList<SimulationType> simulationList;
    private int localSimulationCounter = 0;

    public SimulationCache() {
        //this.MeasureList = new ArrayList<MeasureType>();
        this.simulationList = new ArrayList<SimulationType>();
    }

    public boolean parseSimulationCacheFile(String filename, ArrayList<MeasureType> listOfMeasures, parameterTableModel myParameterTableModel, MainFrame myParentFrame) {
        ArrayList<String[]> listOfStringLines = new ArrayList<String[]>();
        //read file
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(new File(filename)));
            String current = reader.readLine();
            //Number of Experiments, first number of lines is counted
            int numberOfExperiments = 0;
            while (current != null) {
                //processCsvLine(current);
                String[] tmpString = current.split(";");
                listOfStringLines.add(tmpString);
                current = reader.readLine();
            }
            reader.close();

            //Get Names of Parameters
            listOfCachedParameterNames = new String[listOfStringLines.get(0).length - 8];
            listOfCachedParameterMin = new double[listOfStringLines.get(0).length - 8];
            listOfCachedParameterMax = new double[listOfStringLines.get(0).length - 8];
            listOfCachedParameterStepping = new double[listOfStringLines.get(0).length - 8];
            double tmpValue = 0.0;
            int column = 0;

            //Check length of List of Parameter
            if (listOfCachedParameterNames.length != myParameterTableModel.getRowCount()) {
                support.log("Count of cached Parameters differs from Count of given Parameters.", typeOfLogLevel.ERROR);
                support.log(listOfCachedParameterNames.length + " parameters in cache while " + myParameterTableModel.getRowCount() + " in table.", typeOfLogLevel.INFO);
                support.log("List of Parameters in Cache:", typeOfLogLevel.INFO);
                for (int i = 0; i < listOfCachedParameterNames.length; i++) {
                    support.log(support.translateParameterNameFromLogFileToTable(listOfStringLines.get(0)[i + 7]), typeOfLogLevel.INFO);
                }
                support.log("End of List of Parameters in Cache.", typeOfLogLevel.INFO);
                return false;
            }
            support.log("Count of cached Parameters seems correct.", typeOfLogLevel.INFO);

            for (int i = 0; i < listOfCachedParameterNames.length; i++) {
                column = i + 7;
                listOfCachedParameterNames[i] = support.translateParameterNameFromLogFileToTable(listOfStringLines.get(0)[column]);
                listOfCachedParameterMax[i] = Double.NEGATIVE_INFINITY;
                listOfCachedParameterMin[i] = Double.POSITIVE_INFINITY;
                listOfCachedParameterStepping[i] = Double.POSITIVE_INFINITY;
                //Walk through a column and get Min and Max Values
                for (int line = 1; line < listOfStringLines.size(); line++) {
                    tmpValue = support.getDouble(listOfStringLines.get(line)[column]);
                    listOfCachedParameterMax[i] = Math.max(tmpValue, listOfCachedParameterMax[i]);
                    listOfCachedParameterMin[i] = Math.min(tmpValue, listOfCachedParameterMin[i]);
                    if (line < listOfStringLines.size() - 1) {
                        try {
                            //listOfCachedParameterStepping[i]=Math.max(listOfCachedParameterStepping[i], Math.abs(tmpValue-support.getFloatFromString(listOfStringLines.get(line+1)[column])));
                            double tmpValue2 = support.getDouble(listOfStringLines.get(line + 1)[column]);
                            //TODO: Make this a setting in prefernces frame, how many digits to be used!
                            tmpValue2 = (Math.abs(tmpValue - tmpValue2));
                            tmpValue2 = support.round3(tmpValue2);
                            if (tmpValue2 > 0) {
                                listOfCachedParameterStepping[i] = Math.min(tmpValue2, listOfCachedParameterStepping[i]);
                                //support.log("Result of round: "+tmpValue2);
                                //support.log("Setting new Value for Stepping.");
                            }

                        } catch (Exception e) {
                            support.log("Maybe there was an error getting the stepping from cache file.", typeOfLogLevel.ERROR);
                        }
                    }
                }
                //listOfCachedParameterStepping[i]=support.round( Math.abs( (listOfCachedParameterMax[i]-listOfCachedParameterMin[i])/(listOfStringLines.size()-1)) ) ; 
                if (listOfCachedParameterStepping[i] == Double.POSITIVE_INFINITY) {
                    listOfCachedParameterStepping[i] = support.DEFAULT_STEPPING;
                }
                //support.log("ParameterName " +(i)+" = "+listOfCachedParameterNames[i]+" with Min="+listOfCachedParameterMin[i]+" and Max="+listOfCachedParameterMax[i]+" and Stepping="+listOfCachedParameterStepping[i]);
            }

            //Get Names of Measures
            boolean foundDublicate = false;
            ArrayList<String> listOfCachedMeasureNames = new ArrayList<String>();
            listOfCachedMeasureNames.add(listOfStringLines.get(1)[0]);
            int i = 2;
            while (!foundDublicate) {
                if (listOfCachedMeasureNames.get(0).equals(listOfStringLines.get(i)[0])) {
                    foundDublicate = true;
                } else {
                    listOfCachedMeasureNames.add(listOfStringLines.get(i)[0]);
                }
                i++;
            }

            //Debug Output of Measurement Names
            support.log("Name of read Measures are: ", typeOfLogLevel.INFO);
            for (i = 0; i < (listOfCachedMeasureNames.size()) - 1; i++) {
                support.log(listOfCachedMeasureNames.get(i) + ", ", typeOfLogLevel.INFO);
            }
            support.log(listOfCachedMeasureNames.get(listOfCachedMeasureNames.size() - 1), typeOfLogLevel.INFO);

            //Check Length of List of Measurements
            if (listOfCachedMeasureNames.size() != listOfMeasures.size()) {
                support.log("Count of cached Measures differs from Count of given Measures.", typeOfLogLevel.INFO);
                return false;
            }
            support.log("Count of cached Measures seems correct.", typeOfLogLevel.INFO);

            //Calc real number of experiments
            numberOfExperiments = (listOfStringLines.size() - 1) / listOfCachedMeasureNames.size();
            support.log("Number of experiments:" + numberOfExperiments, typeOfLogLevel.INFO);

            //So the number of parameters seems ok, let` check the names
            for (i = 0; i < listOfCachedParameterNames.length; i++) {
                //support.log("Checking  "+listOfCachedParameterNames[i]+" ... number "+i);
                if ((myParameterTableModel.getValueByName(listOfCachedParameterNames[i], "StartValue")) == null) {
                    //support.log((myParameterTableModel.getValueByName(listOfCachedParameterNames[i], "StartValue"))!=null );
                    //One Value is "" --> Parameter is not available --> Exit
                    support.log("The parameter " + listOfCachedParameterNames[i] + " seems not available in table.", typeOfLogLevel.ERROR);
                    return false;
                }
            }
            support.log("All parameters seem available in table.", typeOfLogLevel.INFO);

            support.log("Number of Measures:" + listOfCachedMeasureNames.size(), typeOfLogLevel.INFO);
            //Generate List of Simulated Experiments
            for (i = 0; i < numberOfExperiments; i++) {
                SimulationType tmpSimulation = new SimulationType();
                for (int c = 0; c < listOfCachedMeasureNames.size(); c++) {
                    MeasureType tmpMeasure = new MeasureType();
                    //Line number in read cache file
                    int lineNumber = i * listOfCachedMeasureNames.size() + c + 1;
                    tmpMeasure.setMeasureName(listOfStringLines.get(lineNumber)[0]);
                    tmpMeasure.setMeanValue(support.getDouble(listOfStringLines.get(lineNumber)[1]));
                    tmpMeasure.setVariance(support.getDouble(listOfStringLines.get(lineNumber)[2]));
                    double[] tmpConf = {support.getDouble(listOfStringLines.get(lineNumber)[3]), support.getDouble(listOfStringLines.get(lineNumber)[4])};
                    tmpMeasure.setConfidenceInterval(tmpConf);
                    tmpMeasure.setEpsilon(support.getDouble(listOfStringLines.get(lineNumber)[5]));
                    tmpMeasure.setSimulationTime(support.getDouble(listOfStringLines.get(lineNumber)[6]));
                    //CPU-Time is in last column
                    //support.log("printing CPU-Time for experiment:"+i);
                    //support.log("LineNumber: "+lineNumber);
                    //support.log("CPUTime is in Col: "+(7+listOfCachedParameterNames.length));
                    tmpMeasure.setCPUTime(support.getDouble(listOfStringLines.get(lineNumber)[7 + listOfCachedParameterNames.length]));
                    //support.log("CPU-Time of "+tmpMeasure.getMeasureName()+" is " +tmpMeasure.getCPUTime()+".");

                    if (tmpSimulation.getListOfParameters() == null) //list of parameters for current simulation not set
                    {
                        ArrayList<parameter> tmpParameterList = new ArrayList<>();
                        for (int i1 = 0; i1 < listOfCachedParameterNames.length; i1++) {
                            column = i1 + 7;
                            parameter tmpParameter = new parameter();
                            tmpParameter.setName(support.translateParameterNameFromLogFileToTable(listOfCachedParameterNames[i1]));
                            tmpParameter.setEndValue(support.round3(listOfCachedParameterMax[i1]));
                            tmpParameter.setStartValue(support.round3(listOfCachedParameterMin[i1]));
                            tmpParameter.setStepping(support.round3(listOfCachedParameterStepping[i1]));
                            //Get and save Value of this Parameter
                            //It is in the correct Column of the actual line
                            //We did not change the order of Parameters (it`s the same like in the raw file)
                            //tmpParameter.setValue(support.translateParameterNameFromLogFileToTable(listOfStringLines.get(lineNumber)[column]));
                            tmpParameter.setValue(support.round3(support.getDouble(listOfStringLines.get(lineNumber)[column])));

                            tmpParameterList.add(tmpParameter);
                        }

                        //tmpMeasure.setParameterList(tmpParameterList);
                        tmpSimulation.setListOfParameters(tmpParameterList);
                    }

                    tmpSimulation.getMeasures().add(tmpMeasure);
                    //this.MeasureList.add(tmpSimulation);

                }
                getSimulationList().add(tmpSimulation);
            }

            //Do not reformat Parameter-Table
            //this.reformatParameterTable(myParameterTableModel);
            //Refresh Design Space label of MainFrame
            myParentFrame.calculateDesignSpace();

        } catch (Exception ex) {
            support.log("Error while reading the Simulation Cache File.", typeOfLogLevel.ERROR);
            JOptionPane.showMessageDialog(null, "Could not read simulation cache file!");
        }
        //Get all parameters
        //Get all Results
        //Check if Parameters fit to given SCPN, if not, return false

        return true;
    }

    public void reformatParameterTable(parameterTableModel myTableModel) {
        if (listOfCachedParameterNames == null) {
            return;
        }
        //Names are equal --> format the table so that Start-,End-,Stepping-Value match
        for (int i = 0; i < listOfCachedParameterNames.length; i++) {
            myTableModel.setValueByName(listOfCachedParameterNames[i], "StartValue", listOfCachedParameterMin[i]);
            support.log("Setting StartValue to " + listOfCachedParameterMin[i], typeOfLogLevel.INFO);
            myTableModel.setValueByName(listOfCachedParameterNames[i], "EndValue", listOfCachedParameterMax[i]);
            support.log("Setting EndValue to " + listOfCachedParameterMax[i], typeOfLogLevel.INFO);
            myTableModel.setValueByName(listOfCachedParameterNames[i], "Stepping", listOfCachedParameterStepping[i]);
            support.log("Setting Stepping to " + listOfCachedParameterStepping[i], typeOfLogLevel.INFO);
        }
    }

    public boolean checkIfAllParameterMatchTable(parameterTableModel myTableModel) {
        if (listOfCachedParameterNames == null) {
            support.log("ListOfCachedParameterNames=NULL.", typeOfLogLevel.INFO);
            return false;
        }
        ArrayList<parameter> parameterListFromTable = myTableModel.getListOfParameter();

        //Names are equal (this should be checked before)--> now check if End-Start-Step-Value match or are subset of cache
        for (int i = 0; i < listOfCachedParameterNames.length; i++) {
            parameter p = support.getParameterByName(parameterListFromTable, listOfCachedParameterNames[i]);
            boolean[] strike = {false, false, false};//if all 3 are true, this parameter is cached correctly
            /*
             0->Stepping is ok
             1->StartValue is ok
             2->EndValue is ok
             */

            //Check stepping separately but first
            //Check if p.getStepping() modulo cached-Stepping is 0
            if ((p.getStepping() % listOfCachedParameterStepping[i]) == 0.0) {
                //The original Stepping is bigger but reachable with the new stepping
                strike[0] = true;
            }

            //Go through all possible values
            //if startvalue is reachable ->true
            //if end-value is reachable ->true
            double tmpValue = listOfCachedParameterMin[i];

            while (tmpValue <= listOfCachedParameterMax[i]) {
                if (tmpValue == p.getStartValue()) {
                    strike[1] = true;
                }
                if (tmpValue == p.getEndValue()) {
                    strike[2] = true;
                }
                tmpValue = tmpValue + listOfCachedParameterStepping[i];
            }

            if (!strike[0] || !strike[1] || !strike[2]) {
                support.log("Parameter " + p.getName() + " does not fit!", typeOfLogLevel.ERROR);
                return false;
            }//If less then all 3 conditions are met, then exit with false    
        }
        //format the table so that Start-,End-,Stepping-Value match
        return true;
    }

    /**
     * Returns all Measures, selected by one parameterset(ParameterList)
     *
     * @param parameterList given Set of Parameters to by virtually simulated
     * @return ArrayList of MeasureTypes
     */
    public ArrayList<MeasureType> getAllMeasuresWithParameterList(ArrayList<parameter> parameterList) {
        SimulationType tmpSimulation;
        ArrayList<MeasureType> myTmpList = new ArrayList();

        //support.log("Size of All Measures from File: "+MeasureList.size());
        //Go through all Measures, find the one with the same parameterlist
        for (int i = 0; i < this.getSimulationList().size(); i++) {
            tmpSimulation = this.getSimulationList().get(i);
            if (compareParameterList(parameterList, tmpSimulation.getListOfParameters())) {
                myTmpList = tmpSimulation.getMeasures(); //assumming cache-file did not have experiments with same Parameterset
            }
        }
        return myTmpList;
    }

    /**
     * Returns SimulationType, which is the nearest to the given parameterSet
     * (if exact match doesn`t exist)
     *
     * @param parameterList given parameterSet for simulated simulation...
     * @return Measure which is nearest one to given parameterset TODO build
     * into getListOfCompletedSimulationParsers()
     */
    public SimulationType getNearestParserWithParameterList(ArrayList<parameter> parameterList) {
        ArrayList<Double[]> distArrayList = new ArrayList<>();

        for (int i = 0; i < this.getSimulationList().size(); i++) {
            Double[] tmpDist = new Double[2];//0->Dist, 1->Index
            tmpDist[0] = getDistanceOfParameterLists(this.getSimulationList().get(i).getListOfParameters(), parameterList);
            tmpDist[1] = (double) i;
            distArrayList.add(tmpDist);
        }

        Collections.sort(distArrayList, new Comparator<Double[]>() {
            @Override
            public int compare(Double[] a, Double[] b) {
                return a[0].compareTo(b[0]);
            }
        });
        //Now it`s sorted, we should find the one with distance >= 0
        int indexOfZeroDistance = 0;

        //Debug, output Distance-List
        //for(int c=0;c<distArrayList.size();c++){
        //support.log("Distance # "+c + " is "+ distArrayList.get(c)[0]);
        //}
        //indexOfZeroDistance should contain the index of the Distance >=0
        ArrayList<MeasureType> listOfMeasureWithGivenParameters = this.getAllMeasuresWithParameterList(this.getSimulationList().get(distArrayList.get(indexOfZeroDistance)[1].intValue()).getListOfParameters());
        if (listOfMeasureWithGivenParameters.size() > 0) {

            this.setLocalSimulationCounter(this.getLocalSimulationCounter() + 1);
            return (this.getParserFromListOfMeasures(listOfMeasureWithGivenParameters));
        }
        return null;
    }

    /**
     * Wrapper. Return List of parsers for given list of parametersets
     * (fuzzy-search) It is used for Cache-Only Optimization
     *
     * @param pList List of Parametersets (ArrayList of Arrays)
     * @return ArrayList of parsers
     */
    public ArrayList<SimulationType> getNearestParserListFromListOfParameterSets(ArrayList< ArrayList<parameter>> pList) {
        ArrayList<SimulationType> returnList = new ArrayList<>();
        for (int i = 0; i < pList.size(); i++) {
            ArrayList<parameter> tmpPList = new ArrayList();
            for (int c = 0; c < pList.get(i).size(); c++) {
                tmpPList.add(pList.get(i).get(c));
            }
            returnList.add(getNearestParserWithParameterList(tmpPList));
        }
        return returnList;
    }

    /**
     * Returns distance of whole parameterset (sum of all parameter-Values)
     *
     * @param listA first list to be compared
     * @param listB second list to be compared
     * @return distance of all parameter-values combined. if A<B negative, A>B
     * positive
     */
    private double getDistanceOfParameterLists(ArrayList<parameter> listA, ArrayList<parameter> listB) {
        double sum[] = new double[2];
        ArrayList<parameter> list[] = new ArrayList[2];
        list[0] = this.getParameterListWithoutIgnorableParameters(listA);
        list[1] = this.getParameterListWithoutIgnorableParameters(listB);
        for (int i = 0; i < 2; i++) {
            sum[i] = 0;
            for (int c = 0; c < list[i].size(); c++) {
                sum[i] += list[i].get(c).getValue();
                //support.log("Size of List "+i+" is "+list[i].size());
                //support.log("Value of Parameter "+c+" is "+list[i].get(c).getValue());

            }
        }
        //support.log("Check For Disctance of parameterset---");
        //support.log("Distance-A "+sum[0]);
        //support.log("Distance-B "+sum[1]);

        return Math.abs(sum[0] - sum[1]);
    }

    /**
     * Checks if two parametersets are equal
     *
     * @param mylistA will be compared to
     * @param mylistB
     * @return true if parametersets (only the values and names) are equal, else
     * false
     */
    public boolean compareParameterList(ArrayList<parameter> mylistA, ArrayList<parameter> mylistB) {
        String nameA = "";
        parameter tmpParameterA, tmpParameterB;
        ArrayList<parameter> listA = this.getParameterListWithoutIgnorableParameters(mylistA);
        ArrayList<parameter> listB = this.getParameterListWithoutIgnorableParameters(mylistB);

        if (listA.size() != listB.size()) {
            support.log("Size of needle-ParameterList is different from haystack-ParameterList.", typeOfLogLevel.INFO);
            support.log("Size of A: " + listA.size() + " vs. Size of B: " + listB.size(), typeOfLogLevel.INFO);
            support.log("List of ParameterNames of A is:", typeOfLogLevel.INFO);
            for (int i = 0; i < listA.size(); i++) {
                support.log(listA.get(i).getName(), typeOfLogLevel.INFO);
            }

            support.log("List of ParameterNames of B is:", typeOfLogLevel.INFO);
            for (int i = 0; i < listB.size(); i++) {
                support.log(listB.get(i).getName(), typeOfLogLevel.INFO);
            }

            return false;
        }

        for (int i = 0; i < listA.size(); i++) {
            tmpParameterA = listA.get(i);
            nameA = support.translateParameterNameFromLogFileToTable(tmpParameterA.getName());
            tmpParameterB = this.findParameterInListByName(nameA, listB);
            if (tmpParameterB == null) {
                support.log("ParameterB is null.", typeOfLogLevel.INFO);
                return false;
            }
            //Parameter found, now check the values of this parameter
            if (support.round3(support.getDouble(tmpParameterA.getValue())) != support.round3(support.getDouble(tmpParameterB.getValue()))) {
                //support.log("Parameter Values differ.");
                return false;
            }
        }
        return true;
    }

    /**
     * finds a parameter by Name in an ArrayList of parameters
     *
     * @param name Name of Parameter to search (Needle)
     * @param list ArrayList of Paramater (Haystack)
     * @return parameter with the given name or null
     */
    private parameter findParameterInListByName(String name, ArrayList<parameter> list) {
        parameter testParameter;
        for (int i = 0; i < list.size(); i++) {
            testParameter = list.get(i);
            if (testParameter.getName().equals(name)) {
                return testParameter;
            }
        }
        return null;
    }

    /**
     * Same function like in real simulator Given a list of parametersets, it
     * returns a list of Simulation results (parsers)
     *
     * @param parameterSetList List of Parameter-Arrays to be "simulated"
     * @param simulationCounter Counter of Simulations
     * @return List of parsers with Simulation-Results, like in real simulation
     */
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers(ArrayList<ArrayList<parameter>> parameterSetList, int simulationCounter) {
        setLocalSimulationCounter(simulationCounter);
        ArrayList<SimulationType> myParserList = new ArrayList<SimulationType>();

        for (ArrayList<parameter> parameterSet : parameterSetList) {
            //Create Arraylist from array of parameters
            ArrayList<parameter> tmpParameterList = new ArrayList<parameter>();
            for (parameter myParameter : parameterSet) {
                tmpParameterList.add(myParameter);
            }
            //Get local simulation results
            ArrayList<MeasureType> listOfMeasureWithGivenParameters = this.getAllMeasuresWithParameterList(tmpParameterList);
            //support.log("Size of ParameterList: "+ tmpParameterList.size() + " results in " +listOfMeasureWithGivenParameters.size()+ " Measurements.");
            //append if listSize is > 0
            if (listOfMeasureWithGivenParameters.size() > 0) {
                /*SimulationType tmpParser=new SimulationType();
                 tmpParser.setMeasures(listOfMeasureWithGivenParameters);
                 tmpParser.setSimulationTime(listOfMeasureWithGivenParameters.get(i).getSimulationTime());
                 tmpParser.setCPUTime(support.getInt(listOfMeasureWithGivenParameters.get(i).getCPUTime()));
                 */
                SimulationType tmpParser = this.getParserFromListOfMeasures(listOfMeasureWithGivenParameters);
                tmpParser.setListOfParameters(parameterSet);
                myParserList.add(tmpParser);
                simulationCounter++;
            }
        }
        if (myParserList.isEmpty()) {
            return null;
        }
        return myParserList;
    }

    /**
     * Returns a SimulationType-object containing all given MeasureTypes
     *
     * @param mList List of MeasureType-Objects to be converted into one
     * SimulationType
     * @return one SimulationType-object
     */
    private SimulationType getParserFromListOfMeasures(ArrayList<MeasureType> mList) {
        if (mList.size() > 0) {
            SimulationType tmpParser = new SimulationType();
            for (int i = 0; i < mList.size(); i++) {
                tmpParser.setMeasures(mList);
                //tmpParser.setSimulationTime(mList.get(i).getSimulationTime());
                //tmpParser.setCPUTime(support.getInt(mList.get(i).getCPUTime()));
            }
            tmpParser.setIsFromCache(true);
            return tmpParser;
        }
        return null;
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

    /**
     * Adds every given SimulationType into list of simulations to local cache
     *
     * @param SimulationListToAdd List of simulations to be added to local cache
     */
    public void addListOfSimulationsToCache(ArrayList<SimulationType> SimulationListToAdd) {
        for (int i = 0; i < SimulationListToAdd.size(); i++) {
            this.addSimulationToCache(SimulationListToAdd.get(i));
            //this.simulationList.add(SimulationListToAdd.get(i));
        }
    }

    /**
     * Adds one Simulation(Type) to list of simulations (to local cache)
     *
     * @see addListOfSimulationsToCache(ArrayList<SimulationType>
     * SimulationListToAdd)
     * @param SimulationToAdd Simulation to be added
     */
    public void addSimulationToCache(SimulationType SimulationToAdd) {
        this.getSimulationList().add(SimulationToAdd);
    }

    /**
     * Removes all ignorable parameters from list It`s needed for comparision of
     * parameterlist because some data is stored as a parameter but is just
     * metadata
     */
    public ArrayList<parameter> getParameterListWithoutIgnorableParameters(ArrayList<parameter> pList) {
        ArrayList<parameter> returnList = new ArrayList<parameter>();
        //Add only parameters that are not ignorable
        for (int i = 0; i < pList.size(); i++) {
            parameter tmpParameter = pList.get(i);
            if (!tmpParameter.isIgnorable()) {
                returnList.add(tmpParameter);
            }
        }
        return returnList;
    }

    /**
     * @return the simulationList
     */
    public ArrayList<SimulationType> getSimulationList() {
        return simulationList;
    }

    /**
     * Return actual size of simulation cache
     *
     * @return Size of SimulationList
     */
    public int getCacheSize() {
        return this.simulationList.size();
    }
}
