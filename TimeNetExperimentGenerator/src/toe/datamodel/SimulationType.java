/*
 * Parser reads one log file of SCPN-Simulation
 * After Log-File reading it contains all Measurement-data and can be asked for

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.datamodel;

import toe.support;
import java.util.ArrayList;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimulationType {

    private String logName;
    private String SimulationType;
    private ArrayList<MeasureType> Measures = new ArrayList();
    private ArrayList<String> tmpStrings = new ArrayList();
    private ArrayList<parameter> parameterList = null;
    private int parseStatus = 0;
    private double CPUTime = 0;
    private String xmlFileName = "";
    private boolean isFromCache = false;//is true, if from cache and false if logfile is parsed
    private boolean isFromDistributedSimulation = false;//Is False, if local simulated, true if simulated via Web

    /**
     * the default constructor for parser-objects
     */
    public SimulationType() {
        this.logName = "";
        this.SimulationType = "";
        this.Measures = new ArrayList();
        this.tmpStrings = new ArrayList();
        this.parseStatus = 0;
        this.CPUTime = 0;
        this.parameterList = null;
        this.xmlFileName = "";
        this.isFromCache = false;
        this.isFromDistributedSimulation = false;
    }

    /**
     * the copy-constructor for parser objects
     *
     * @param originalParser the parser to be copied
     */
    public SimulationType(SimulationType originalParser) {
        this.logName = originalParser.logName;
        this.SimulationType = originalParser.SimulationType;
        this.Measures = new ArrayList<MeasureType>();
        for (int i = 0; i < originalParser.getMeasures().size(); ++i) {
            MeasureType newMeasure = new MeasureType(originalParser.getMeasures().get(i));
            this.Measures.add(newMeasure);
        }
        this.tmpStrings = originalParser.tmpStrings;
        this.parseStatus = originalParser.parseStatus;
        this.CPUTime = originalParser.CPUTime;

        this.parameterList = new ArrayList<parameter>();
        ArrayList<parameter> originalParamterArray = originalParser.getListOfParameters();
        for (int i = 0; i < originalParamterArray.size(); ++i) {
            try {
                parameter p = (parameter) originalParamterArray.get(i).clone();
                parameterList.add(p);
            } catch (CloneNotSupportedException e) {
                support.log(e.getMessage());
            }

        }
        this.xmlFileName = originalParser.xmlFileName;
        this.isFromCache = originalParser.isFromCache;
        this.isFromDistributedSimulation = originalParser.isFromDistributedSimulation;
    }

    /**
     * Returns Distance to target Value (min->distance to 0)
     *
     * @return Sum Distance of Measures#0 to its target Value Only one Measure
     * is supported!
     */
    public double getDistanceToTargetValue() {
        //TODO Only use one MeasureType, the first, to calculate distance

        /*double distance=0;
         for(int measureCount=0;measureCount<Measures.size();measureCount++)
         {
         MeasureType activeMeasure = getMeasureByName(Measures.get(measureCount).getMeasureName());
         if (activeMeasure.getTargetKindOf() != null)
         {
         if(activeMeasure.getTargetKindOf().equals("value"))
         {
         distance+=activeMeasure.getDistanceFromTarget();
         }
         else if(activeMeasure.getTargetKindOf().equals("min"))
         {
         distance+=activeMeasure.getMeanValue();
         }
         else if(activeMeasure.getTargetKindOf().equals("max"))
         {
         distance+=0-activeMeasure.getMeanValue();
         }
         else
         {
         //TODO error handling for unknown target-type
         }
         }
         }*/
        //Get active OptimizationTarget from UI (via support)
        MeasureType activeMeasure = support.getOptimizationMeasure();
        //Set target Value and target Kind for Measure with same name but in list of this simulation
        this.getMeasureByName(activeMeasure.getMeasureName()).setTargetValue(activeMeasure.getTargetValue(), activeMeasure.getTargetTypeOf());
        //Return the distance of local Measure from target, chosen from Master-Measure
        return this.getMeasureByName(activeMeasure.getMeasureName()).getDistanceFromTarget();
    }

    /**
     * Returns distance to target simulation coordinates in design space the
     * distance is relative to the whole design space, So Sum of all
     * parameter-value-ranges equals 100% It works only for few Benchmark
     * functions and for normal petri nets if theroetical optimum is given!
     *
     * @return Sum of relative Distances to target Values
     * @param targetSimulation SimulationType of target(i.e. optimum) Solution
     * to calculate the distance
     */
    public double getRelativeDistanceInDefinitionRange(SimulationType targetSimulation) {
        double rangeSum = 0.0;
        double distanceSum = 0.0;

        switch (support.getChosenTypeOfRelativeDistanceCalculation()) {
            case STANDARD:
                for (int i = 0; i < targetSimulation.getListOfParameters().size(); i++) {
                    parameter pOptimumCalculated = targetSimulation.getListOfParameters().get(i);
                    if (pOptimumCalculated.isIteratableAndIntern()) {
                        parameter pOptimumFound = support.getParameterByName(this.parameterList, pOptimumCalculated.getName());
                        rangeSum = rangeSum + Math.abs(pOptimumCalculated.getEndValue() - pOptimumCalculated.getStartValue());
                        distanceSum = distanceSum + Math.abs(pOptimumCalculated.getValue() - pOptimumFound.getValue());
                    }
                }
                distanceSum = (distanceSum * 100.0 / rangeSum);
                break;
            case EUKLID:
                for (int i = 0; i < targetSimulation.getListOfParameters().size(); i++) {
                    parameter pOptimumCalculated = targetSimulation.getListOfParameters().get(i);
                    if (pOptimumCalculated.isIteratableAndIntern()) {
                        parameter pOptimumFound = support.getParameterByName(this.parameterList, pOptimumCalculated.getName());
                        rangeSum = rangeSum + Math.pow(pOptimumCalculated.getEndValue() - pOptimumCalculated.getStartValue(),2);
                        distanceSum = distanceSum + Math.pow(pOptimumCalculated.getValue() - pOptimumFound.getValue(),2);
                    }
                }
                distanceSum=Math.sqrt(distanceSum);
                rangeSum=Math.sqrt(rangeSum);
                distanceSum = (distanceSum * 100.0 / rangeSum);
                break;
        }

        return distanceSum;
    }

    /**
     * Returns distance of actual MeasureValue to target MeasureValue in
     * relation to Maximum/Minimum of values of measures This is only possible
     * for benchmark-functions or if absolute minimum/maximum is given by user
     *
     * @return distance to targetMeasure in % of possible range
     * @param targetMeasure Measure to calculate the distance to. Must contain
     * Min/Max Values!
     */
    public double getRelativeDistanceToTargetValueInValueRange(MeasureType targetMeasure) {
        //TODO implement this method!
        double distance;
        double range;
        //int numberOfParameters = support.getListOfChangableParameters(parameterList).size();
        /*
         Get Simulator type from support or Prefs
         get min-max-values based on Simulator
         */
//        if (support.getChosenSimulatorType() == typedef.typeOfSimulator.Benchmark) {
//
//            switch (support.getChosenBenchmarkFunction()) {
//                case Ackley:
//                    //Optimum is in the middle of each parameter, its exact at 0,0
//                    //TODO calculate value range fpr Ackley
//                    break;
//                case Sphere:
//                    //Optimum is in the middle of each parameter, its exact at 0,0
//                    range = Math.pow(5 * 5, numberOfParameters);
//                    break;
//                case Matya:
//                    //Optimum is in the middle of each parameter, its exact at 0,0
//                    double x0 = support.DEFAULT_Matya_limitLower;
//                    double x1 = support.DEFAULT_Matya_limitupper;
//                    range = 0.26 * (x0 * x0 + x1 * x1) - 0.48 * x0 * x1;
//                    break;
//                case Schwefel:
//                    //TODO calculate value range for Schwefel
//                    //Optimum is in the middle of each parameter, its exact at 0,0
//                    break;
//                case Rastrigin:
//                    //TODO calculate value range for Rastrigin
//                    //Optimum is in the middle of each parameter, its exact at 0,0
//                    break;
//
//                default:
//
//                    break;
//            }
//
//        } else {
//            if (support.getChosenSimulatorType() == typedef.typeOfSimulator.Cache_Only) {
//                //TODO add Cache-Local-Simulator
        range = targetMeasure.getMaxValue() - targetMeasure.getMinValue();
        //Get copy of actual optimization target (TargetMeasure is found by name)
        MeasureType activeMeasure = new MeasureType(this.getMeasureByName(support.getOptimizationMeasure().getMeasureName()));
        //set target Value of copied optimization target measure
        activeMeasure.setTargetValue(targetMeasure.getMeanValue(), targetMeasure.getTargetTypeOf());

        distance = activeMeasure.getDistanceFromTarget();
        support.log("Absolute Distance to calculated Optimum (" + targetMeasure.getMeanValue() + ") is " + distance + " at a range of " + range + ".");
//            }
//            //TODO Get Min-Max, Opti-Values from somewhere else!
//        }

        return (distance * 100 / range);
    }

    /**
     * @return the Measures
     */
    public ArrayList<MeasureType> getMeasures() {
        return Measures;
    }

    /**
     * returns Mean Value of Measure by given Name
     *
     * @param name Name of the Measure which value should be returneed
     * @return value of Measure, given by name
     */
    public double getMeasureValueByMeasureName(String name) {
        double returnValue = 0.0;

        for (MeasureType Measure : this.Measures) {
            if (Measure.getMeasureName().equals(name)) {
                returnValue = Measure.getMeanValue();
            }
        }
        return returnValue;
    }

    /**
     * Get Measure object by given name. Iterata through list of measures and
     * select the one with given name
     *
     * @param name Name of the Measure to search in list of all measures
     * @return Found Measure by name
     */
    public MeasureType getMeasureByName(String name) {
        MeasureType returnValue = new MeasureType();
        for (MeasureType Measure : this.Measures) {
            if (Measure.getMeasureName().equals(name)) {
                returnValue = Measure;
            }
        }
        return returnValue;

    }

    /**
     * returns Array of Parameters incl. actual used Values, with empty fields
     * for start/stop/step
     *
     * @return Array of parameters
     */
    public ArrayList<parameter> getListOfParameters() {
//    if (parameterList == null)
//    {
//        return null;
//    }
//    parameter[] pArray=new parameter[parameterList.size()];
//        for(int i=0;i<parameterList.size();i++){
//        pArray[i]=parameterList.get(i);
//        }
//    return pArray;

        return this.parameterList;
    }

    /**
     * returns the Parameterset incl. used Values and filled with
     * Start-End-Stepping from Baseparameterset
     *
     * @return parameterset wich complete filled parameters (start/end value
     * etc.)
     */
    public ArrayList<parameter> getListOfParametersFittedToBaseParameterset() {
        return support.fitParametersetToBaseParameterset(this.parameterList);
    }

    /**
     * Sets List of parameters, internal stored as ArrayList Should not be used,
     * if logfile is parsed, only in cached-mode
     *
     * @param pList Array of parameters
     * @see getListOfParameters
     */
    public void setListOfParameters(ArrayList<parameter> pList) {
//    this.parameterList=new ArrayList<parameter>();
//        for(int i=0;i<p.length;i++){
//        this.parameterList.add((parameter)p[i]);
//        }
        this.parameterList = pList;
    }

    /**
     * @param Measure the list of Measures to set
     */
    public void setMeasures(ArrayList<MeasureType> Measure) {
        this.Measures = Measure;
    }

    /**
     * @return the SimulationTime
     */
    //public double getSimulationTime() {
    //    return SimulationTime;
    //}
    /**
     * @param SimulationTime the SimulationTime to set
     */
    //public void setSimulationTime(double SimulationTime) {
    //    this.SimulationTime = SimulationTime;
    //}
    /**
     * @return the logName
     */
    public String getLogName() {
        return logName;
    }

    /**
     * @param testString String to be converted to double value if not NaN
     * @return the double value of goven testString
     */
    /*public double getFloatString(String testString) {
     if (!testString.equalsIgnoreCase("nan")) {
     return Double.valueOf(testString);
     } else {
     return 0;
     }
     }
     */
    /**
     * @return the isFromCache
     */
    public boolean isIsFromCache() {
        return isFromCache;
    }

    /**
     * @param isFromCache the isFromCache to set
     */
    public void setIsFromCache(boolean isFromCache) {
        this.isFromCache = isFromCache;
    }

    /**
     * @return the isFromDistributedSimulation
     */
    public boolean isIsFromDistributedSimulation() {
        return isFromDistributedSimulation;
    }

    /**
     * @param isFromDistributedSimulation the isFromDistributedSimulation to set
     */
    public void setIsFromDistributedSimulation(boolean isFromDistributedSimulation) {
        this.isFromDistributedSimulation = isFromDistributedSimulation;
    }
}
