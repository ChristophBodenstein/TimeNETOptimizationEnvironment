/*
 * Parser reads one log file of SCPN-Simulation
 * After Log-File reading it contains all Measurement-data and can be asked for

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.datamodel;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import timenetexperimentgenerator.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimulationType {
private String logName;
private String SimulationType;
private double SimulationTime;
private ArrayList<MeasureType> Measures=new ArrayList();
private ArrayList<String> tmpStrings=new ArrayList();
private ArrayList<parameter> parameterList=null;
private int parseStatus=0;
private double CPUTime=0;
private String xmlFileName="";
private boolean isFromCache=false;//is true, if from cache and false if logfile is parsed
private boolean isFromDistributedSimulation=false;//Is False, if local simulated, true if simulated via Web

    /**
     * the default constructor for parser-objects
     */
    public SimulationType()
    {
        this.logName = "";
        this.SimulationType = "";
        this.SimulationTime = 0.0;
        this.Measures=new ArrayList();
        this.tmpStrings=new ArrayList();
        this.parseStatus=0;
        this.CPUTime=0;
        this.parameterList=null;
        this.xmlFileName="";
        this.isFromCache=false;
        this.isFromDistributedSimulation=false;
    }

    /**
     * the copy-constructor for parser objects
     * @param originalParser the parser to be copied
     */
    public SimulationType(SimulationType originalParser)
    {
        this.logName = originalParser.logName;
        this.SimulationType = originalParser.SimulationType;
        this.Measures = new ArrayList<MeasureType>();
        for (int i = 0; i<originalParser.getMeasures().size(); ++i)
        {
            MeasureType newMeasure = new MeasureType(originalParser.getMeasures().get(i));
            this.Measures.add(newMeasure);
        }
        this.tmpStrings = originalParser.tmpStrings;
        this.parseStatus = originalParser.parseStatus;
        this.CPUTime = originalParser.CPUTime;
        
        this.parameterList = new ArrayList<parameter>();
        ArrayList<parameter> originalParamterArray = originalParser.getListOfParameters();
        for (int i = 0; i<originalParamterArray.size(); ++i)
        {
            try
            {
                parameter p = (parameter)originalParamterArray.get(i).clone();
                parameterList.add(p);
            }
            catch (CloneNotSupportedException e)
            {
                support.log(e.getMessage());
            }

        }
        this.xmlFileName = originalParser.xmlFileName;
        this.isFromCache = originalParser.isFromCache;
        this.isFromDistributedSimulation = originalParser.isFromDistributedSimulation;
    }


    public double getDistance()
    {
        double distance=0;
        for(int measureCount=0;measureCount<Measures.size();measureCount++)
        {
            MeasureType activeMeasure = getMeasureByName(Measures.get(measureCount).getMeasureName());
            if (activeMeasure.getTargetKindOf() != null)
            {
                if(activeMeasure.getTargetKindOf().equals("value"))
                {
                    distance=activeMeasure.getDistanceFromTarget();
                }
                else if(activeMeasure.getTargetKindOf().equals("min"))
                {
                    distance=activeMeasure.getMeanValue();
                }
                else if(activeMeasure.getTargetKindOf().equals("max"))
                {
                    distance=0-activeMeasure.getMeanValue();
                }
                else
                {
                    //TODO error handling for unknown target-type
                }
            }
        }
        return distance;
    }
    
    /**
     * @return the Measures
     */
    public ArrayList<MeasureType> getMeasures() {
        return Measures;
    }

    /** 
     returns Mean Value of Measure by given Name
     */
    public double getMeasureValueByMeasureName(String name){
    double returnValue=0.0;

        for(int i=0;i<this.Measures.size();i++){
            if(this.Measures.get(i).getMeasureName().equals(name)){
            returnValue=this.Measures.get(i).getMeanValue();
            }
        }
    return returnValue;
    }

    public MeasureType getMeasureByName(String name){
    MeasureType returnValue=new MeasureType();
        for(int i=0;i<this.Measures.size();i++){
            if(this.Measures.get(i).getMeasureName().equals(name)){
            returnValue=this.Measures.get(i);
            }
        }
    return returnValue;

    }
    
    /** 
     * returns Array of Parameters incl. actual used Values, with empty fields for start/stop/step
     * @return Array of parameters
     */
    public ArrayList<parameter> getListOfParameters()
    {
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
     * returns the Parameterset incl. used Values and filled with Start-End-Stepping from Baseparameterset
     */
    public ArrayList<parameter> getListOfParametersFittedToBaseParameterset(){
    return support.fitParametersetToBaseParameterset(this.parameterList);
    }


    /**
     * Sets List of parameters, internal stored as ArrayList
     * Should not be used, if logfile is parsed, only in cached-mode
     * @param p Array of parameters
     * @see getListOfParameters
     */
    public void setListOfParameters(ArrayList<parameter> pList){
//    this.parameterList=new ArrayList<parameter>();
//        for(int i=0;i<p.length;i++){
//        this.parameterList.add((parameter)p[i]);
//        }
        this.parameterList = pList;
    }

    /**
     * @param Measures the Measures to set
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
     * @return the CPUTime
     */
    //public double getCPUTime() {
    //    return CPUTime;
    //}

    public double getFloatString(String testString){
        if(!testString.equals("nan")){
        return Double.valueOf(testString);
        }else{
        return 0;
        }
    }

    /**
     * @param CPUTime the CPUTime to set
     */
    //public void setCPUTime(int CPUTime) {
    //    this.CPUTime = CPUTime;
    //}

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
