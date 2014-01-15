/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator;

import java.util.ArrayList;

/**
 *
 * @author sse
 */
public class MeasureType {
private String MeasureName;
private boolean AccuraryReached=false;
private float MeanValue;
private float Variance;
private float[] ConfidenceInterval;
private float Epsilon;
private ArrayList<parameter> parameterList=new ArrayList<parameter>();
private float targetValue;
private String targetKindOf;


    /**
     * @set targetValue and/or (min/max/value)
     */
     public void setTargetValue(float value, String kindOfTarget){

        if(kindOfTarget.equals("min")||kindOfTarget.equals("max")){
        this.targetKindOf=kindOfTarget;
        }else{
        this.targetKindOf="value";
        }
     this.targetValue=value;
     }


     public float getDistanceFromTarget(){
     return Math.abs(this.MeanValue-this.targetValue);
     }

     public String getTargetKindOf(){
     return this.targetKindOf;
     }
     public float getTargetValue(){
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
    public float getMeanValue() {
        return MeanValue;
    }

    /**
     * @param MeanValue the MeanValue to set
     */
    public void setMeanValue(float MeanValue) {
        this.MeanValue = MeanValue;
    }

    /**
     * @return the Variance
     */
    public float getVariance() {
        return Variance;
    }

    /**
     * @param Variance the Variance to set
     */
    public void setVariance(float Variance) {
        this.Variance = Variance;
    }

    /**
     * @return the ConfidenceInterval
     */
    public float[] getConfidenceInterval() {
        return ConfidenceInterval;
    }

    /**
     * @param ConfidenceInterval the ConfidenceInterval to set
     */
    public void setConfidenceInterval(float[] ConfidenceInterval) {
        this.ConfidenceInterval = ConfidenceInterval;
    }

    /**
     * @return the Epsilon
     */
    public float getEpsilon() {
        return Epsilon;
    }

    /**
     * @param Epsilon the Epsilon to set
     */
    public void setEpsilon(float Epsilon) {
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
}
