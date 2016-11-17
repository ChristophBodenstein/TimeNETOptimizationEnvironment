/*
 * Christoph Bodenstein
 * Parameter Class for Experiment Parameters

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.datamodel;

import java.util.Arrays;
import toe.support;
import toe.typedef;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author sse
 */
public class parameter implements Cloneable, Comparable<parameter> {

    private String name = "0";
    private double value = 0.0;
    private double startValue = 0.0;
    private double endValue = 0.0;
    private double stepping = 1.0;
    private final String[] externalParameters = {"ConfidenceIntervall", "Seed", "EndTime", "MaxTime", "MaxRelError", "UsedCPUTIME"};
    private long idHash = 0;

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
        this.value = value;
        this.calculateID();
    }

    /**
     * Returns the String repesentation of Value
     *
     * @return String representation of parameter value
     */
    public String getStringValue() {
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
     * If the parameter is an external control-parameter, then true External
     * Parameters are Conf.Intervall, MaxRelError etc. The List of External
     * control parameters is this.externalParameters
     *
     * @return true, if it is an external parameter, ales false
     */
    public boolean isExternalParameter() {
        for (int i = 0; i < this.externalParameters.length; i++) {
            if (this.name.equals(externalParameters[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clone parameter by deep copy
     *
     * @return cloned parameter
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        parameter tmpParameter;
        tmpParameter = (parameter) super.clone();
        tmpParameter.setEndValue(endValue);
        tmpParameter.setStartValue(startValue);
        tmpParameter.setStepping(stepping);
        tmpParameter.setName(new String(name));
        tmpParameter.setValue(value);

        return tmpParameter;
    }

    /**
     * returns the id of this parameter, not useful at the moment
     *
     * @return ID of this parameter
     */
    public long getID() {
        return this.idHash;
    }

    /**
     * Calculates the id of this parameter, at the moment deactiviated. Was used
     * to detect duplicates
     */
    public void calculateID() {
        //String id="End:"+endValue+"Start:"+startValue+"Step:"+stepping+"Name:"+name+"Value:"+value;
        //long longID=  (long)( Float.valueOf(endValue)*10+Float.valueOf(startValue)*10+Float.valueOf(stepping)*10+Float.valueOf(value)*10+(float)name.hashCode());
        this.idHash = 0;//id.hashCode();//Dont create ID, it`s useless and costs CPU-Time
        //support.log("ID: "+id);
        //support.log("IDHash: "+idHash);

    }

    /**
     * Compare Method similar to String comparision. Returns result of comparing
     * names. If names are equal, values are compared
     *
     * @param o other Parameter to be compared with
     * @return integer-value of comparision result
     */
    @Override
    public int compareTo(parameter o) {
        if (this.getName().equals(o.getName())) {
            //Parameters will be sorted by value if name is equal
            return Double.compare(this.value, o.value);
        } else {
            //Parameters will be sorted by name
            return this.getName().compareTo(o.getName());
        }
    }

    /**
     * init the parameter with given values
     *
     * @param name Name of this paramater
     * @param StartValue Start-Value of this parameter (double)
     * @param EndValue End-Value of this parameter (double)
     * @param Stepping Iteration-Stepping for this parameter (double)
     */
    public void initWithValues(String name, double StartValue, double EndValue, double Stepping) {
        this.setName(name);
        this.setStartValue(StartValue);
        this.setEndValue(EndValue);
        this.setStepping(Stepping);
    }

    /**
     * Returns true, if this parameter has different start/endValue and Stepping
     * is smaller then this difference
     *
     * @return true, if parameter can be iterated
     */
    public boolean isIteratable() {
        //TODO This check is not correct!
        //support.log("Checking Iteratability of parameter:"+this.getName());
        //support.log("StartValue: "+this.getStartValue()+" EndValue: "+this.getEndValue()+" Stepping: "+this.getStepping());
        //if((this.endValue>this.startValue)&&(this.stepping<=(this.endValue-this.startValue))&&(this.value<=this.endValue)){
        if ((this.endValue > this.startValue) && (this.stepping <= (this.endValue - this.startValue))) {
            //support.log("Is Iteratable!");
            return true;
        } else {
            //support.log("Is NOT Iteratable!");
            return false;
        }
    }

    /**
     * Same as isIteratable but only true, if parameter is not external
     *
     * @return true, if parameter is internal and iteratable
     */
    public boolean isIteratableAndIntern() {
        //TODO If Parameter is chosen in Opti-Prefs as Precision-Parameter it is not longer internal!!!!!
        return ((!this.isExternalParameter()) && (this.isIteratable()));
    }

    /**
     * Increments the value of this parameter by stepping
     *
     * @return true if it was possible to increment the parameter
     */
    public boolean incValue() {
        double newValue = this.value + this.stepping;
        if (newValue <= this.endValue) {
            this.value = newValue;
            return true;
        } else {
            return false;
        }
    }

    /**
     * decrements Value of this parameter by stepping
     *
     * @return true if it was possible to decrement the parameter
     */
    public boolean decValue() {
        double newValue = this.value - this.stepping;
        if (newValue >= this.startValue) {
            this.value = newValue;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Increments or decrements Value based on given boolean
     *
     * @param direction if true -> increment, false -> decrement
     * @return true -> return result of incrementing, return result of false
     * decrementing
     */
    public boolean incDecValue(boolean direction) {
        if (direction) {
            return this.incValue();
        } else {
            return this.decValue();
        }
    }

    /**
     * Increments or decrements Value n times based on given boolean
     *
     * @param direction if true -> increment, false -> decrement
     * @param multiplier as many time as Value has to be incremented/decremented
     * @return success of inc/dec true -> everything went fine, false -> inc/dec
     * was not possible completely
     */
    public boolean incDecValue(boolean direction, int multiplier) {
        boolean success = true;
        if (multiplier >= 1) {
            for (int i = 0; i < multiplier; i++) {
                success = this.incDecValue(direction);
            }
        }
        return success;
    }

    /**
     * Some Parameters needs to be ignored while searching in the Parameter-DB
     * or during Caching. These parameters are identified by name
     *
     * @return true if parameter should be ignored, false -> font ignore this
     * parameter when searching in cache
     * @see timenetexperimentgenerator.typedef.listOfParametersToIgnore
     */
    public boolean isIgnorable() {
        if (Arrays.asList(typedef.listOfParametersToIgnore).contains(this.getName())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * prints some interesting information about itself to log
     */
    public void printInfo(typeOfLogLevel logLevel) {
        String s = "Parameter " + this.getName() + ": Start=" + this.getStartValue() + ", End=" + this.getEndValue();
        s += ", Stepping=" + this.getStepping() + ", Value=" + this.getValue() + ", is ";
        if (!isIgnorable()) {
            s += "NOT";
        }
        s += " ignorable, is ";
        if (!isIteratable()) {
            s += "NOT";
        }
        s += " iteratable, is ";
        if (!isExternalParameter()) {
            s += "NOT";
        }
        s += " external.";
        support.log(s, logLevel);
    }

}
