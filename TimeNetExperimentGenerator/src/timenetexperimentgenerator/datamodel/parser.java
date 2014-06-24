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
public class parser {
private String logName;
private String SimulationType;
private double SimulationTime;
private ArrayList<MeasureType> Measures=new ArrayList();
private ArrayList<String> tmpStrings=new ArrayList();
private int parseStatus=0;
private double CPUTime=0;
private ArrayList<parameter> parameterList=null;
private String xmlFileName="";
private boolean isFromCache=true;//is true, if from cache and false if logfile is parsed
private boolean isFromDistributedSimulation=false;//Is False, if local simulated, true if simulated via Web

    /**
     * the default constructor for parser-objects
     */
    public parser()
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
        this.isFromCache=true;
        this.isFromDistributedSimulation=false;
    }

    /**
     * the copy-constructor for parser objects
     * @param originalParser the parser to be copied
     */
    public parser(parser originalParser)
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
        parameter[] originalParamterArray = originalParser.getListOfParameters();
        for (int i = 0; i<originalParamterArray.length; ++i)
        {
            parameter[] newParameterArray = Arrays.copyOf(originalParamterArray, originalParamterArray.length);//hoping that makes a copy of parameter, overide clone() exists
            ArrayList<parameter> newParameterList = support.convertArrayToArrayList(newParameterArray);
            parameterList = newParameterList;
        }
        this.xmlFileName = originalParser.xmlFileName;
        this.isFromCache = originalParser.isFromCache;
        this.isFromDistributedSimulation = originalParser.isFromDistributedSimulation;
    }

    public boolean parse(String filename, String XMLFileName){
        if(!XMLFileName.equals("")){
        this.xmlFileName=XMLFileName;
        }
    return parse(filename);
    }

    /*
     * parses the log-file
     */
    public boolean parse(String filename){
    String xmlFilename="";
    ArrayList<parameter> tmpParameterList=new ArrayList<parameter>();
    String[] segs;
    this.setIsFromCache(false);
    
    if(this.xmlFileName.equals("")){
        support.log("Searching corresponding xml-file for: "+filename);
        String[] tmpFilenameArray=filename.split("simTime");
        xmlFilename=tmpFilenameArray[0]+".xml";
        support.log("XML-Filename is:"+xmlFilename);    
        }   else{
            support.log("XML-Filename given: "+this.xmlFileName);
            xmlFilename=xmlFileName;
            }


    File xmlFile=new File(xmlFilename);
        if(!xmlFile.exists()){
        support.log("XML-File not found, eject.");
        return false;
        }
        support.log("Parsing XML-File: "+xmlFilename);
    try{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	Document doc = docBuilder.parse(xmlFilename);
        NodeList parameterList=doc.getElementsByTagName("parameter");
        support.log("***Start List of Available parameters in xml-file***");
            for(int i=0;i<parameterList.getLength();i++){
            parameter tmpParameter=new parameter();

            support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            tmpParameter.setName(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            tmpParameter.setValue(support.getDouble(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue()));
            tmpParameterList.add(tmpParameter);
            }
        support.log("***End of List of Available parameters in xml-file***");
        }catch(Exception e){
        support.log("Error while parsing xml-file "+ xmlFilename);
        }

    
    this.logName=filename;
    try {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line = null;
		while ((line = in.readLine()) != null) {
			//support.log("Read line: " + line);
                tmpStrings.add(line);
		}
                in.close();
	} catch (IOException e) {
		//e.printStackTrace();
            support.log("Error while parsing log-file "+ filename);
	}



    segs=tmpStrings.get(0).split(" ");
    SimulationType=segs[2];
    segs = tmpStrings.get(1).split(" ");
    double localSimulationTime=(Float.valueOf(segs[2]));
    double localCPUTime=0;
    
    
    //support.log("SimulationType: "+SimulationType);
    //support.log("SimulationTime: "+getSimulationTime().toString()+" seconds.");
    
    //Add all Parameters from logfile-name
    segs=logName.split("_");
        for(int i=0;i<segs.length;i++){
            if(segs[i].equals("MaxTime")){
            parameter tmpP=new parameter();
            tmpP.setName("MaxTime");
            tmpP.setValue(support.getDouble(segs[i+1]));
            tmpParameterList.add(tmpP);
            }
            if(segs[i].equals("EndTime")){
            parameter tmpP=new parameter();
            tmpP.setName("EndTime");
            tmpP.setValue(support.getDouble(segs[i+1]));
            tmpParameterList.add(tmpP);
            }
            if(segs[i].equals("Seed")){
            parameter tmpP=new parameter();
            tmpP.setName("Seed");
            tmpP.setValue(support.getDouble(segs[i+1]));
            tmpParameterList.add(tmpP);
            }
            if(segs[i].equals("ConfidenceIntervall")){
            parameter tmpP=new parameter();
            tmpP.setName("Configured-ConfidenceIntervall");
            tmpP.setValue(support.getDouble(segs[i+1]));
            tmpParameterList.add(tmpP);
            }
            if(segs[i].equals("MaxRelError")){
            parameter tmpP=new parameter();
            tmpP.setName("MaxRelError");
            tmpP.setValue(support.getDouble(segs[i+1]));
            tmpParameterList.add(tmpP);
            }               
            if(segs[i].equals("simTime")){
            //CPU-Time is called simTime in logfile-Name!
            parameter tmpP=new parameter();
            tmpP.setName("Used CPUTime");
            //String[] tmpSegs=segs[i+1].split(".");
            tmpP.setValue(support.getDouble(segs[i+1].substring(0,segs[i+1].indexOf("."))));
            tmpParameterList.add(tmpP);
            localCPUTime=(support.getDouble(segs[i+1].substring(0,segs[i+1].indexOf("."))));
            }
            
        }
     this.parameterList=tmpParameterList;

    //Begin parsing rest of file
    MeasureType tmpMeasure=new MeasureType();
    tmpMeasure.setSimulationTime(localSimulationTime);
    tmpMeasure.setParameterList(tmpParameterList);
    tmpMeasure.setCPUTime(localCPUTime);
    String tmpConfidence="";
    for(int i=0;i<tmpStrings.size();i++){
        switch(parseStatus){
            case 0:
                if(tmpStrings.get(i).split(" ")[0].equalsIgnoreCase("Measure:")){
                tmpMeasure=new MeasureType();
                tmpMeasure.setParameterList(tmpParameterList);
                tmpMeasure.setCPUTime(localCPUTime);
                tmpMeasure.setSimulationTime(localSimulationTime);
                tmpMeasure.setMeasureName(tmpStrings.get(i).split(" ")[1]);
                parseStatus=1;//Measures found
                support.log("Measures found");
                }
                break;
            case 1://Measures found, name exists
                if(tmpStrings.get(i).contains("Mean Value")){
                String input = tmpStrings.get(i+2);
                Scanner s = new Scanner(input).useDelimiter("\\s+");
                //segs=tmpStrings.get(i+2).split( "\\s*" );
                tmpMeasure.setMeanValue(getFloatString(s.next()));
                tmpMeasure.setVariance(getFloatString(s.next()));
                tmpConfidence=s.next();
                tmpConfidence=tmpConfidence.replaceAll("\\[|\\]", "");
                segs=tmpConfidence.split(Pattern.quote(";"));
                //float[] tmpConf={ (segs[0]),Float.valueOf(segs[1])};
                double[] tmpConf={getFloatString(segs[0]),getFloatString(segs[1])}  ;
                tmpMeasure.setConfidenceInterval(tmpConf);
                tmpMeasure.setEpsilon(getFloatString(s.next()));
                tmpMeasure.setParameterList(tmpParameterList);
                this.getMeasures().add(tmpMeasure);
                parseStatus=0;
                support.log("Measures "+tmpMeasure.getMeasureName()+" has Epsilon of "+tmpMeasure.getEpsilon()+" and Mean of "+tmpMeasure.getMeanValue() );
                }

                if(tmpStrings.get(i).contains("WARNING:")){
                    if(tmpStrings.get(i+1).contains("Has not reached predifined accuracy!")){
                    tmpMeasure.setAccuraryReached(false);
                    }
                }

                break;
            default:break;
        }
    }
    
    return true;
    }

    public double getDistance()
    {
        double distance=0;
        for(int measureCount=0;measureCount<Measures.size();measureCount++)
        {
            MeasureType activeMeasure = getMeasureByName(Measures.get(measureCount).getMeasureName());
            MeasureType activeMeasureFromInterface = Measures.get(measureCount);//Contains Optimization targets
            activeMeasure.setTargetValue(activeMeasureFromInterface.getTargetValue(), activeMeasureFromInterface.getTargetKindOf());
            
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
     returns Array of Parameters incl. actual used Values, with empty fields for start/stop/step
     * @return Array of parameters
     */
    public parameter[] getListOfParameters(){
    if (parameterList == null)
    {
        return null;
    }
    parameter[] pArray=new parameter[parameterList.size()];
        for(int i=0;i<parameterList.size();i++){
        pArray[i]=parameterList.get(i);
        }
    return pArray;
    }

    /**
     * Sets List of parameters, internal stored as ArrayList
     * Should not be used, if logfile is parsed, only in cached-mode
     * @param p Array of parameters
     * @see getListOfParameters
     */
    public void setListOfParameters(parameter[] p){
    this.parameterList=new ArrayList<parameter>();
        for(int i=0;i<p.length;i++){
        this.parameterList.add((parameter)p[i]);
        }
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
