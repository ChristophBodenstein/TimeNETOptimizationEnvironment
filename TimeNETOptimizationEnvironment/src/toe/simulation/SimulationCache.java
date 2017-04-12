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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
    //Variables for search in simulationCache
    private BigInteger tmpHash = BigInteger.ZERO;
    private SimulationType tmpSimulationForSearch;
    private ArrayList<MeasureType> myTmpList = new ArrayList();
    private HashMap<BigInteger, SimulationType> simulationHashmap;
    
    public SimulationCache() {
        //this.MeasureList = new ArrayList<MeasureType>();
        this.simulationList = new ArrayList<>();
        this.simulationHashmap = new HashMap<>();
    }
    
    public boolean parseSimulationCacheFile(String filename, ArrayList<MeasureType> listOfMeasures, parameterTableModel myParameterTableModel, MainFrame myParentFrame) {
        ArrayList<String[]> listOfStringLines = new ArrayList<String[]>();
        support.setStatusText("Reading cache-file...");

        //read file
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(new File(filename)));
            String current = reader.readLine();
            //Number of Experiments, first number of lines is counted
            int numberOfExperiments = 0;
            while (current != null) {
                String[] tmpString = current.split(";");
                listOfStringLines.add(tmpString);
                current = reader.readLine();
                support.spinInLabel();
                Thread.yield();
                //Eject if user requested to cancel
                if (support.isCancelEverything()) {
                    support.emptyCache();
                    return false;
                }
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
                    support.spinInLabel();
                    Thread.yield();
                    
                    tmpValue = support.getDouble(listOfStringLines.get(line)[column]);
                    listOfCachedParameterMax[i] = Math.max(tmpValue, listOfCachedParameterMax[i]);
                    listOfCachedParameterMin[i] = Math.min(tmpValue, listOfCachedParameterMin[i]);
                    if (line < listOfStringLines.size() - 1) {
                        try {
                            //listOfCachedParameterStepping[i]=Math.max(listOfCachedParameterStepping[i], Math.abs(tmpValue-support.getFloatFromString(listOfStringLines.get(line+1)[column])));
                            double tmpValue2 = support.getDouble(listOfStringLines.get(line + 1)[column]);
                            //TODO: Make this a setting in prefernces frame, how many digits to be used!
                            tmpValue2 = (Math.abs(tmpValue - tmpValue2));
                            tmpValue2 = support.round(tmpValue2, 3);
                            if (tmpValue2 > 0) {
                                listOfCachedParameterStepping[i] = Math.min(tmpValue2, listOfCachedParameterStepping[i]);
                                //support.log("Result of round: "+tmpValue2);
                                //support.log("Setting new Value for Stepping.");
                            }
                            
                        } catch (Exception e) {
                            support.log("Maybe there was an error getting the stepping from cache file.", typeOfLogLevel.ERROR);
                        }
                    }
                    //Eject if user requested to cancel
                    if (support.isCancelEverything()) {
                        support.emptyCache();
                        return false;
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
                support.spinInLabel();
                support.setStatusText("Reading cache-file: " + (i * 100 / numberOfExperiments) + "%");
                Thread.yield();
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
                            tmpParameter.setEndValue(support.round(listOfCachedParameterMax[i1], 3));
                            tmpParameter.setStartValue(support.round(listOfCachedParameterMin[i1], 3));
                            tmpParameter.setStepping(support.round(listOfCachedParameterStepping[i1], 3));
                            //Get and save Value of this Parameter
                            //It is in the correct Column of the actual line
                            //We did not change the order of Parameters (it`s the same like in the raw file)
                            //tmpParameter.setValue(support.translateParameterNameFromLogFileToTable(listOfStringLines.get(lineNumber)[column]));
                            tmpParameter.setValue(support.round(support.getDouble(listOfStringLines.get(lineNumber)[column]), 3));
                            
                            tmpParameterList.add(tmpParameter);
                        }

                        //tmpMeasure.setParameterList(tmpParameterList);
                        tmpSimulation.setListOfParameters(tmpParameterList);
                    }
                    
                    tmpSimulation.getMeasures().add(tmpMeasure);
                    //this.MeasureList.add(tmpSimulation);

                }
                //getSimulationList().add(tmpSimulation);
                addSimulationToCache(tmpSimulation);

                //Eject if user requested to cancel
                if (support.isCancelEverything()) {
                    support.emptyCache();
                    return false;
                }
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
            boolean[] strike = {true, true, true};//if all 3 are true, this parameter is cached correctly
            /*
             0->Stepping is ok
             1->StartValue is ok
             2->EndValue is ok
             */

            //Check stepping separately but first
            //Check if p.getStepping() modulo cached-Stepping is 0
            if (Double.compare((p.getStepping() % listOfCachedParameterStepping[i]), 0.0) == 0.0) {
                //The original Stepping is bigger but reachable with the new stepping
                strike[0] = true;
            }

            //Go through all possible values
            //if startvalue is reachable ->true
            //if end-value is reachable ->true
            double tmpValue = support.round(listOfCachedParameterMin[i], 3);
            
            while (tmpValue <= listOfCachedParameterMax[i] * 1.1) {
                if (Double.compare(tmpValue, p.getStartValue()) == 0) {
                    strike[1] = true;
                }
                if (Double.compare(tmpValue, p.getEndValue()) == 0) {
                    strike[2] = true;
                }
                tmpValue = support.round(tmpValue + listOfCachedParameterStepping[i], 3);
            }
            
            if (!strike[0] || !strike[1] || !strike[2]) {
                support.log("Parameter " + p.getName() + " does not fit!", typeOfLogLevel.ERROR);
                if (!strike[0]) {
                    support.log("Stepping wrong.", typeOfLogLevel.ERROR);
                }
                support.log("Stepping: " + Double.toString(p.getStepping()) + " ---- " + Double.toString(listOfCachedParameterStepping[i]), typeOfLogLevel.INFO);
                if (!strike[1]) {
                    support.log("Start wrong.", typeOfLogLevel.ERROR);
                }
                support.log("Start: " + Double.toString(p.getStartValue()) + " ---- " + Double.toString(listOfCachedParameterMin[i]), typeOfLogLevel.INFO);
                
                if (!strike[2]) {
                    support.log("End wrong.", typeOfLogLevel.ERROR);
                    
                }
                support.log("End: " + Double.toString(p.getEndValue()) + " ---- " + Double.toString(listOfCachedParameterMax[i]), typeOfLogLevel.INFO);
                
                support.log("Start: " + p.getStartValue(), typeOfLogLevel.INFO);
                support.log("End: " + p.getEndValue(), typeOfLogLevel.INFO);
                support.log("Stepping: " + p.getStepping(), typeOfLogLevel.INFO);
                
                return false;
            }//If less then all 3 conditions are met, then exit with false    
        }
        //format the table so that Start-,End-,Stepping-Value match
        return true;
    }

    /**
     * Returns all Measures, selected by one parameterset(ParameterList).
     *
     * It will just compare the hash values of the sorted parameterLists!
     *
     * @param parameterList given Set of Parameters to by virtually simulated
     * @return ArrayList of MeasureTypes, null if not found
     */
    public ArrayList<MeasureType> getAllMeasuresWithParameterList(ArrayList<parameter> parameterList) {
        myTmpList = null;
        support.addDummyParameterCPUTimeIfNeeded(parameterList);
        tmpHash = support.getHashValueForParameterList(parameterList);

        //Go through all Simulations, find the one with the same parameterlist
        for (int i = 0; i < this.getSimulationList().size(); i++) {
            tmpSimulationForSearch = this.getSimulationList().get(i);
            if (tmpHash.equals(tmpSimulationForSearch.getHashValue())) {
                myTmpList = tmpSimulationForSearch.getMeasures();
            }
        }
        return myTmpList;
    }

    /**
     * Returns SimulationType, which is the nearest to the given parameterSet
     * (if exact match doesn`t exist)
     *
     * @param parameterList given parameterSet for simulated simulation...
     * @return Measure which is nearest one to given parameterset
     */
    public SimulationType getNearestSimulationWithParameterList(ArrayList<parameter> parameterList) {
        int indexOfSmallestDistance = 0;
        double valueOfSmallestDistance = Double.MAX_VALUE;
        double currentDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < this.getSimulationList().size(); i++) {
            currentDistance = getDistanceOfParameterLists(this.getSimulationList().get(i).getListOfParameters(), parameterList);
            
            if (currentDistance <= valueOfSmallestDistance) {
                indexOfSmallestDistance = i;
                valueOfSmallestDistance = currentDistance;
            }
        }
        return this.getSimulationList().get(indexOfSmallestDistance);
    }

    /**
     * Wrapper. Return List of parsers for given list of parametersets
     * (fuzzy-search) It is used for Cache-Only Optimization
     *
     * @param pList List of Parametersets (ArrayList of Arrays)
     * @return ArrayList of parsers
     */
    public ArrayList<SimulationType> getNearestSimulationListFromListOfParameterSets(ArrayList< ArrayList<parameter>> pList) {
        ArrayList<SimulationType> returnList = new ArrayList<>();
        for (int i = 0; i < pList.size(); i++) {
            returnList.add(getNearestSimulationWithParameterList(pList.get(i)));
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
            }
        }
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
            if (support.round(support.getDouble(tmpParameterA.getValue()), 3) != support.round(support.getDouble(tmpParameterB.getValue()), 3)) {
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
     * Same function like in real simulator. Given a list of parametersets, it
     * returns a list of Simulation results (parsers)
     *
     * @param parameterSetList List of Parameter-Arrays to be "simulated"
     * @param simulationCounter Counter of Simulations
     * @param listOfUnknownParametersets given by reference, this list will
     * contain all parametersets not found in cache
     * @return List of parsers with Simulation-Results, like in real simulation
     */
    public ArrayList<SimulationType> getListOfCompletedSimulations(ArrayList<ArrayList<parameter>> parameterSetList, int simulationCounter, ArrayList<ArrayList<parameter>> listOfUnknownParametersets) {
        setLocalSimulationCounter(simulationCounter);
        ArrayList<SimulationType> mySimulationList = new ArrayList<>();
        ArrayList<parameter> parameterSet;
        
        for (int i = 0; i < parameterSetList.size(); i++) {
            parameterSet = parameterSetList.get(i);
            support.addDummyParameterCPUTimeIfNeeded(parameterSet);
            
            support.spinInLabel();
            support.setStatusText("Searching in cache: " + i * 100 / parameterSetList.size() + " %");

            //Get cached simulation results
            SimulationType foundSimulation = simulationHashmap.get(support.getHashValueForParameterList(parameterSet));
            if (foundSimulation != null) {
                foundSimulation.setIsFromCache(true);
                mySimulationList.add(foundSimulation);
                simulationCounter++;
            } else {
                listOfUnknownParametersets.add(parameterSet);
            }
            
            if (support.isCancelEverything()) {
                return mySimulationList;
            }
        }
        if (mySimulationList.isEmpty()) {
            return null;
        }
        return mySimulationList;
    }

    /**
     * Returns a SimulationType-object containing all given MeasureTypes
     *
     * @param mList List of MeasureType-Objects to be converted into one
     * SimulationType
     * @return one SimulationType-object
     */
    private SimulationType getSimulationFromListOfMeasures(ArrayList<MeasureType> mList) {
        if (mList.size() > 0) {
            SimulationType tmpSimulation = new SimulationType();
            for (int i = 0; i < mList.size(); i++) {
                tmpSimulation.setMeasures(mList);
                //tmpSimulation.setSimulationTime(mList.get(i).getSimulationTime());
                //tmpSimulation.setCPUTime(support.getInt(mList.get(i).getCPUTime()));
            }
            tmpSimulation.setIsFromCache(true);
            return tmpSimulation;
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
        SimulationToAdd.setIsFromCache(true);
        support.addDummyParameterCPUTimeIfNeeded(SimulationToAdd.getListOfParameters());
        SimulationToAdd.updateHashValue();
        if (!simulationHashmap.containsKey(SimulationToAdd.getHashValue())) {
            support.log("Will add Simulation with hash: " + SimulationToAdd.getHashValue() + " to cache.", typeOfLogLevel.VERBOSE);
            support.printListOfParameters(SimulationToAdd.getListOfParameters(), typeOfLogLevel.VERBOSE);
            
            if (this.getSimulationList().size() >= 1) {
                support.log("Cache contains Hash: " + this.getSimulationList().get(0).getHashValue(), typeOfLogLevel.VERBOSE);
                support.printListOfParameters(this.getSimulationList().get(0).getListOfParameters(), typeOfLogLevel.VERBOSE);
            }
            
            this.getSimulationList().add(SimulationToAdd);
            simulationHashmap.put(SimulationToAdd.getHashValue(), SimulationToAdd);
            
        } else {
            support.log("Simulation with hash: " + SimulationToAdd.getHashValue() + " already in cache.", typeOfLogLevel.VERBOSE);
        }
        
    }

    /**
     * Removes all ignorable parameters from list It`s needed for comparision of
     * parameterlist because some data is stored as a parameter but is just
     * metadata
     *
     * @param pList List of parameters to be filtered
     * @return List of parameters without ignorable ones
     */
    public ArrayList<parameter> getParameterListWithoutIgnorableParameters(ArrayList<parameter> pList) {
        ArrayList<parameter> returnList = new ArrayList<>();
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
        return simulationHashmap.size();
    }
}
