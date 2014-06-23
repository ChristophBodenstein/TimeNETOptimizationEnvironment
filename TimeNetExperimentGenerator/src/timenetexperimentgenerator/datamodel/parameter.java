/*
 * Christoph Bodenstein
 * Parameter Class for Experiment Parameters

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.datamodel;

import timenetexperimentgenerator.support;

/**
 *
 * @author sse
 */
public class parameter implements Cloneable, Comparable<parameter>{
private String name="0";
private double value=0.0;
private double startValue=0.0;
private double endValue=0.0;
private double stepping=1.0;
private final String[] externalParameters={"ConfidenceIntervall","Seed","EndTime","MaxTime","MaxRelError"};
private long idHash=0;



    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
        this.calculateID();
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value=value;
        this.calculateID();
    }

    /**
     * Returns the String repesentation of Value
     */
    public String getStringValue(){
    return support.getString(value);
    }



    /**
     * @return the startValue
     */
    public double getStartValue() {
        return startValue;
    }

    /**
     * @param startValue the startValue to set
     */
    public void setStartValue(double startValue) {
        this.startValue = startValue;
        this.calculateID();
    }

    /**
     * @return the endValue
     */
    public double getEndValue() {
        return endValue;
    }

    /**
     * @param endValue the endValue to set
     */
    public void setEndValue(double endValue) {
        this.endValue = endValue;
        this.calculateID();
    }

    /**
     * @return the stepping
     */
    public double getStepping() {
        return stepping;
    }

    /**
     * @param stepping the stepping to set
     */
    public void setStepping(double stepping) {
        this.stepping = stepping;
        this.calculateID();
    }


    /**
     * If the parameter is an external control-parameter, then true
     * External Parameters are Conf.Intervall, MaxRelError etc.
     * The List of External control parameters is this.externalParameters
     * @return true, if it is an external parameter, ales false
     */
    public boolean isExternalParameter(){
        for(int i=0; i<this.externalParameters.length;i++){
            if(this.name.equals(externalParameters[i])){
            return true;
            }
        }
    return false;
    }

   @Override
   public Object clone() throws CloneNotSupportedException
    {
    parameter tmpParameter=new parameter();
    tmpParameter.setEndValue(endValue);
    tmpParameter.setStartValue(startValue);
    tmpParameter.setStepping(stepping);
    tmpParameter.setName(new String(name));
    tmpParameter.setValue(value);

    return tmpParameter;
    }

   /**
    * returns the id of this parameter, not useful at the moment
    * @return ID of this parameter
    */
   public long getID(){
   return this.idHash;
   }

   
   /**
    * Calculates the id of this parameter, at the moment deactiviated. Was used to detect duplicates
    */
   public void calculateID(){
   //String id="End:"+endValue+"Start:"+startValue+"Step:"+stepping+"Name:"+name+"Value:"+value;
   //long longID=  (long)( Float.valueOf(endValue)*10+Float.valueOf(startValue)*10+Float.valueOf(stepping)*10+Float.valueOf(value)*10+(float)name.hashCode());
   this.idHash=0;//id.hashCode();//Dont create ID, it`s useless and costs CPU-Time
   //support.log("ID: "+id);
   //support.log("IDHash: "+idHash);
   
   }

   
   /**
    * Compare Method simailar to String compareision. It`s based on the names of Parameters
    * @param o other Parameter to be compared with
    * @return integer-value of compariosion result
    */
    public int compareTo(parameter o) {
    return this.getName().compareTo(o.getName());//Parameter werden nach Namen sortiert
    }

    
    /**
     * init the parameter with given values
     * @param name Name of this paramater
     * @param StartValue Start-Value of this parameter (double)
     * @param EndValue End-Value of this parameter (double)
     * @param Stepping Iteration-Stepping for this parameter (double)
     */
    public void initWithValues(String name, double StartValue, double EndValue, double Stepping){
    this.setName(name);
    this.setStartValue(StartValue);
    this.setEndValue(EndValue);
    this.setStepping(Stepping);
    }

     
    /**
     * Returns true, if this parameter has different start/endValue and Stepping is smaller then this difference
     * @return true, if parameter can be iterated
     */ 
    public boolean isIteratable(){
        if((this.endValue>this.startValue)&&(this.stepping<=(this.endValue-this.startValue))&&(this.value<this.endValue)){
        return true;
        }else{
        return false;
        }
    }
    
    /**
     * Same as isIteratable but only true, if parameter is not external
     * @return true, if parameter is internal and iteratable
     */
    public boolean isIteratableAndIntern(){
    return ((!this.isExternalParameter())&&(this.isIteratable()) ); 
    }
    
    /**
     * Increments the value of this parameter by stepping
     * @return true if it was possible to increment the parameter
     */
    public boolean incValue(){
    double newValue=this.value+this.stepping;
        if(newValue<=this.endValue){
        this.value=newValue;
        return true;
        }else{
        return false;
        }
    }
    
    /**
     * decrements Value of this parameter by stepping
     * @return true if it was possible to decrement the parameter
     */
    public boolean decValue(){
    double newValue=this.value-this.stepping;
        if(newValue>=this.startValue){
        this.value=newValue;
        return true;
        }else{
        return false;
        }
    }
    
    /**
     * Increments or decrements Value based on given boolean
     * @param direction if true-> increment, false->decrement
     */
    public boolean incDecValue(boolean direction){
        if(direction){
        return this.incValue();
        }else{
        return this.decValue();
        }
    }
}
