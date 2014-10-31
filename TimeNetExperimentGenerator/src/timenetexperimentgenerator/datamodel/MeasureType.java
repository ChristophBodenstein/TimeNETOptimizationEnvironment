/*
 * A MeasureType-object represents one Measure.
 * It can also represent a whole Simulation-Result for one Measure, if a list of Parameters incl. Values is given
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein
 */
public class MeasureType {
private String MeasureName="";
private boolean AccuraryReached=false;
private double MeanValue=0.0;
private double Variance=0.0;
private double[] ConfidenceInterval=new double[2];
private double Epsilon=0.0;;
//private ArrayList<parameter> parameterList=new ArrayList<parameter>();
private double targetValue=0.0;
private String targetKindOf="min";
private double SimulationTime=0.0;
private double CPUTime=0;
private double minValue,maxValue=0.0;

    public MeasureType() {
        ConfidenceInterval[0]=0.0;
        ConfidenceInterval[1]=0.0;

    }
    
    public MeasureType(MeasureType originalMeasure)
    {
        this.MeasureName = originalMeasure.MeasureName;
        this.AccuraryReached = originalMeasure.AccuraryReached;
        this.MeanValue = originalMeasure.MeanValue;
        this.Variance = originalMeasure.Variance;
        this.ConfidenceInterval = Arrays.copyOf(originalMeasure.ConfidenceInterval, originalMeasure.ConfidenceInterval.length);
        this.Epsilon = originalMeasure.Epsilon;
//        this.parameterList = new ArrayList<parameter>();
//        for (int i = 0; i<originalMeasure.getParameterListSize(); ++i)
//        {
//            try
//            {
//                parameter tmpParameter = (parameter)originalMeasure.getParameterList().get(i).clone();
//                this.parameterList.add(tmpParameter);
//            }
//            catch (CloneNotSupportedException e)
//            {
//                support.log(e.getMessage());
//            }
//        }
        this.targetValue = originalMeasure.targetValue;
        this.targetKindOf = originalMeasure.targetKindOf;
        this.CPUTime = originalMeasure.CPUTime;
        this.SimulationTime = originalMeasure.CPUTime;
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
    
    public String getStateAsString()
    {
        String state = "";
        state += "MeasureName\tMeanValue\tVariance\tTargetValue\tTargetKindOf\n";
        state += this.MeasureName + " \t" + this.MeanValue + " \t" + this.Variance + " \t" + this.targetValue + " \t" + this.targetKindOf + "\n\n";
        
        state += "ParameterName\tCurrentValue\tStartValue\tEndValue\tStepping\n";
//        for (int i = 0; i<this.parameterList.size(); ++i)
//        {
//            parameter p = this.parameterList.get(i);
//            state += p.getName() + " \t" + p.getValue() + " \t" + p.getStartValue() + " \t" + p.getEndValue() + " \t" + p.getStepping() + "\n";
//        }
        
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
