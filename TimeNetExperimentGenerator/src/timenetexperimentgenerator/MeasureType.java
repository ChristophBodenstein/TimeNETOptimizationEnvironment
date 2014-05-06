/*
 * A MeasureType-object represents one Measure.
 * It can also represent a whole Simulation-Result for one Measure, if a list of Parameters incl. Values is given
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import java.util.ArrayList;

/**
 *
 * @author Christoph Bodenstein
 */
public class MeasureType {
private String MeasureName;
private boolean AccuraryReached=false;
private double MeanValue;
private double Variance;
private double[] ConfidenceInterval;
private double Epsilon;
private ArrayList<parameter> parameterList=new ArrayList<parameter>();
private double targetValue;
private String targetKindOf;
private double SimulationTime;
private double CPUTime;

    public MeasureType() {
        this.CPUTime = (double)0.0;
        this.SimulationTime = (double)0.0;
    }


    /**
     * @set targetValue and/or (min/max/value)
     */
     public void setTargetValue(double value, String kindOfTarget){

        if(kindOfTarget.equals("min")||kindOfTarget.equals("max")){
        this.targetKindOf=kindOfTarget;
        }else{
        this.targetKindOf="value";
        }
     this.targetValue=value;
     }


     public double getDistanceFromTarget(){
     return Math.abs(this.MeanValue-this.targetValue);
     }

     public String getTargetKindOf(){
     return this.targetKindOf;
     }
     public double getTargetValue(){
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

    /**
     * @return the parameterList
     */
    public ArrayList<parameter> getParameterList() {
        return parameterList;
    }

    /**
     * @param parameterList the parameterList to set
     */
    public void setParameterList(ArrayList<parameter> parameterList) {
        this.parameterList = parameterList;
    }

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
}
