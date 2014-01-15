/*
 * Christoph Bodenstein
 * Parameter Class for Experiment Parameters
 */

package timenetexperimentgenerator;

/**
 *
 * @author sse
 */
public class parameter implements Cloneable, Comparable<parameter>{
private String name="0";
private String value="0";
private String startValue="0";
private String endValue="0";
private String stepping="1";
private String[] externalParameters={"ConfidenceIntervall","Seed","EndTime","MaxTime","MaxRelError"};
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
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
        this.calculateID();
    }

    /**
     * @return the startValue
     */
    public String getStartValue() {
        return startValue;
    }

    /**
     * @param startValue the startValue to set
     */
    public void setStartValue(String startValue) {
        this.startValue = startValue;
        this.calculateID();
    }

    /**
     * @return the endValue
     */
    public String getEndValue() {
        return endValue;
    }

    /**
     * @param endValue the endValue to set
     */
    public void setEndValue(String endValue) {
        this.endValue = endValue;
        this.calculateID();
    }

    /**
     * @return the stepping
     */
    public String getStepping() {
        return stepping;
    }

    /**
     * @param stepping the stepping to set
     */
    public void setStepping(String stepping) {
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
    tmpParameter.setEndValue(new String(endValue));
    tmpParameter.setStartValue(new String(startValue));
    tmpParameter.setStepping(new String(stepping));
    tmpParameter.setName(new String(name));
    tmpParameter.setValue(new String(value));

    return tmpParameter;
    }

   public long getID(){
   return this.idHash;
   }

   public void calculateID(){
   //String id="End:"+endValue+"Start:"+startValue+"Step:"+stepping+"Name:"+name+"Value:"+value;
   //long longID=  (long)( Float.valueOf(endValue)*10+Float.valueOf(startValue)*10+Float.valueOf(stepping)*10+Float.valueOf(value)*10+(float)name.hashCode());
   this.idHash=0;//id.hashCode();//Dont create ID, it` useless and costs CPU-Time
   //System.out.println("ID: "+id);
   //System.out.println("IDHash: "+idHash);
   
   }

    public int compareTo(parameter o) {
    //return this.getID().compareTo(o.getID());//Parameter werden nach ID sortiert
    return this.getName().compareTo(o.getName());//Parameter werden nach Namen sortiert
    }

     public void initWithValues(String name, String StartValue, String EndValue, String Stepping){

    this.setName(name);
    this.setStartValue(StartValue);
    this.setEndValue(EndValue);
    this.setStepping(Stepping);


    }

}
