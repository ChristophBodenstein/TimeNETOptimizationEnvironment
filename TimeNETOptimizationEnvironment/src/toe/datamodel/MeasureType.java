/*
 * A MeasureType-object represents one Measure.
 * It can also represent a whole Simulation-Result for one Measure, if a list of Parameters incl. Values is given
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.datamodel;

import java.util.Arrays;
import toe.support;
import toe.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class MeasureType {

    private String MeasureName = "";
    private boolean AccuraryReached = false;
    private double MeanValue = 0.0;
    private double Variance = 0.0;
    private double[] ConfidenceInterval = new double[2];
    private double Epsilon = 0.0;
    ;
private double targetValue = 0.0;
    private typeOfTarget targetTypeOf = typeOfTarget.min;
    private double SimulationTime = 0.0;
    private double CPUTime = 0;
    private double minValue, maxValue = 0.0;

    /**
     * Constructor
     */
    public MeasureType() {
        ConfidenceInterval[0] = 0.0;
        ConfidenceInterval[1] = 0.0;

    }

    /**
     * Special constructor, copies all internal values from given measuretype
     * object to new object
     *
     * @param originalMeasure
     */
    public MeasureType(MeasureType originalMeasure) {
        this.MeasureName = originalMeasure.MeasureName;
        this.AccuraryReached = originalMeasure.AccuraryReached;
        this.MeanValue = originalMeasure.MeanValue;
        this.Variance = originalMeasure.Variance;
        this.ConfidenceInterval = Arrays.copyOf(originalMeasure.ConfidenceInterval, originalMeasure.ConfidenceInterval.length);
        this.Epsilon = originalMeasure.Epsilon;
        this.targetValue = originalMeasure.targetValue;
        this.targetTypeOf = originalMeasure.targetTypeOf;
        this.CPUTime = originalMeasure.CPUTime;
        this.SimulationTime = originalMeasure.CPUTime;
        this.minValue = originalMeasure.minValue;
        this.maxValue = originalMeasure.maxValue;
    }

    /**
     * @param value targetvalue (double) for optimization and to calculate
     * distance of measure
     * @param typeOfTarget
     * @set targetValue and/or (min/max/value)
     */
    public void setTargetValue(double value, typeOfTarget typeOfTarget) {

        if (typeOfTarget.equals(typeOfTarget.min) || typeOfTarget.equals(typeOfTarget.max)) {
            this.targetTypeOf = typeOfTarget;
        } else {
            this.targetTypeOf = typeOfTarget.value;
        }
        this.targetValue = value;
    }

    /**
     * Returns distance from Target value abs(actual value - target value)
     *
     * @return distance to target value
     */
    public double getDistanceFromTarget() {

        switch (targetTypeOf) {
            case value:
                return Math.abs(this.targetValue - this.MeanValue);
            case max:
                return Math.abs(support.getOPTIMIZATION_TARGET_MAX() - this.MeanValue);
            default:
            case min:
                return Math.abs(support.getOPTIMIZATION_TARGET_MIN() - this.MeanValue);
        }

    }

    /**
     * Return Type of Target (min, max, value)
     *
     * @return type of Target
     */
    public typeOfTarget getTargetTypeOf() {
        return this.targetTypeOf;
    }

    /**
     * Return Target value to optimize for (double)
     *
     * @return target value
     */
    public double getTargetValue() {
        return this.targetValue;
    }

    /**
     * @return the MeasureName
     */
    public String getMeasureName() {
        return MeasureName;
    }

    /**
     * @param MeasureName the MeasureName to set
     */
    public void setMeasureName(String MeasureName) {
        this.MeasureName = MeasureName;
    }

    /**
     * @return the AccuraryReached
     */
    public boolean isAccuraryReached() {
        return AccuraryReached;
    }

    /**
     * @param AccuraryReached the AccuraryReached to set
     */
    public void setAccuraryReached(boolean AccuraryReached) {
        this.AccuraryReached = AccuraryReached;
    }

    /**
     * @return the MeanValue
     */
    public double getMeanValue() {
        return MeanValue;
    }

    /**
     * @param MeanValue the MeanValue to set
     */
    public void setMeanValue(double MeanValue) {
        this.MeanValue = MeanValue;
    }

    /**
     * @return the Variance
     */
    public double getVariance() {
        return Variance;
    }

    /**
     * @param Variance the Variance to set
     */
    public void setVariance(double Variance) {
        this.Variance = Variance;
    }

    /**
     * @return the ConfidenceInterval
     */
    public double[] getConfidenceInterval() {
        return ConfidenceInterval;
    }

    /**
     * @param ConfidenceInterval the ConfidenceInterval to set
     */
    public void setConfidenceInterval(double[] ConfidenceInterval) {
        this.ConfidenceInterval = ConfidenceInterval;
    }

    /**
     * @return the Epsilon
     */
    public double getEpsilon() {
        return Epsilon;
    }

    /**
     * @param Epsilon the Epsilon to set
     */
    public void setEpsilon(double Epsilon) {
        this.Epsilon = Epsilon;
    }

//    /**
//     * @return the parameterList
//     */
//    public ArrayList<parameter> getParameterList() {
//        return parameterList;
//    }
//
//    /**
//     * @param parameterList the parameterList to set
//     */
//    public void setParameterList(ArrayList<parameter> parameterList) {
//        this.parameterList = parameterList;
//    }
    /**
     * @return the size of the paramterList
     */
//    public int getParameterListSize()
//    {
//        if (parameterList != null)
//        {
//            return this.parameterList.size();
//        }
//        return 0;
//    }
    /**
     * @return the SimulationTime
     */
    public double getSimulationTime() {
        return SimulationTime;
    }

    /**
     * @param SimulationTime the SimulationTime to set
     */
    public void setSimulationTime(double SimulationTime) {
        this.SimulationTime = SimulationTime;
    }

    /**
     * @return the CPUTime
     */
    public double getCPUTime() {
        return CPUTime;
    }

    /**
     * @param CPUTime the CPUTime to set
     */
    public void setCPUTime(double CPUTime) {
        this.CPUTime = CPUTime;
    }

    /**
     * returns some infroamtion about measure object as string for logging etc.
     *
     * @return String containing useful information about Measure (Name,
     * targetvalue, value, etc.)
     */
    public String getStateAsString() {
        String state = "";
        state += "MeasureName\tMeanValue\tVariance\tTargetValue\tTargetTypeOf\n";
        state += this.MeasureName + " \t" + this.MeanValue + " \t" + this.Variance + " \t" + this.targetValue + " \t" + this.targetTypeOf + "\n\n";

        state += "ParameterName\tCurrentValue\tStartValue\tEndValue\tStepping\n";
        return state;
    }

    /**
     * @return the minValue
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
}
