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

   public long getID(){
   return this.idHash;
   }

   public void calculateID(){
   //String id="End:"+endValue+"Start:"+startValue+"Step:"+stepping+"Name:"+name+"Value:"+value;
   //long longID=  (long)( Float.valueOf(endValue)*10+Float.valueOf(startValue)*10+Float.valueOf(stepping)*10+Float.valueOf(value)*10+(float)name.hashCode());
   this.idHash=0;//id.hashCode();//Dont create ID, it` useless and costs CPU-Time
   //support.log("ID: "+id);
   //support.log("IDHash: "+idHash);
   
   }

    public int compareTo(parameter o) {
    //return this.getID().compareTo(o.getID());//Parameter werden nach ID sortiert
    return this.getName().compareTo(o.getName());//Parameter werden nach Namen sortiert
    }

     public void initWithValues(String name, double StartValue, double EndValue, double Stepping){

    this.setName(name);
    this.setStartValue(StartValue);
    this.setEndValue(EndValue);
    this.setStepping(Stepping);


    }

}
