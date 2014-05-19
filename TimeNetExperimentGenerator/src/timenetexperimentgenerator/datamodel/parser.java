/*
 * Parser reads one log file of SCPN-Simulation
 * After Log-File reading it contains all Measurement-data and can be asked for

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.datamodel;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.datamodel.*;

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
private String xmlFile="";


    public boolean parse(String filename, String XMLFileName){
        if(!XMLFileName.equals("")){
        this.xmlFile=XMLFileName;
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

    if(this.xmlFile.equals("")){
        support.log("Searching corresponding xml-file for: "+filename);
        String[] tmpFilenameArray=filename.split("simTime");
        xmlFilename=tmpFilenameArray[0]+".xml";
        support.log("XML-Filename is:"+xmlFilename);    
        }   else{
            support.log("XML-Filename given: "+this.xmlFile);
            xmlFilename=xmlFile;
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

            for(int i=0;i<parameterList.getLength();i++){
            parameter tmpParameter=new parameter();

            support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            tmpParameter.setName(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            tmpParameter.setValue(support.getDouble(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue()));
            tmpParameterList.add(tmpParameter);
            }
        
        }catch(Exception e){
        e.printStackTrace();
        }


    support.log("Parsing log-file: "+filename);
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
	}



    segs=tmpStrings.get(0).split(" ");
    SimulationType=segs[2];
    segs = tmpStrings.get(1).split(" ");
    setSimulationTime(Float.valueOf(segs[2]));


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
            parameter tmpP=new parameter();
            tmpP.setName("Used CPUTime");
            //String[] tmpSegs=segs[i+1].split(".");
            tmpP.setValue(support.getDouble(segs[i+1].substring(0,segs[i+1].indexOf("."))));
            tmpParameterList.add(tmpP);
            }
            
        }

     this.parameterList=tmpParameterList;

    //Begin parsing rest of file
    MeasureType tmpMeasure=new MeasureType();
    tmpMeasure.setParameterList(tmpParameterList);
    String tmpConfidence="";
    for(int i=0;i<tmpStrings.size();i++){
        switch(parseStatus){
            case 0:
                if(tmpStrings.get(i).split(" ")[0].equalsIgnoreCase("Measure:")){
                tmpMeasure=new MeasureType();
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
     */
    public parameter[] getListOfParameters(){
    parameter[] pArray=new parameter[parameterList.size()];
        for(int i=0;i<parameterList.size();i++){
        pArray[i]=parameterList.get(i);
        }
    return pArray;
    }

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
     * @return the logName
     */
    public String getLogName() {
        return logName;
    }

    /**
     * @return the CPUTime
     */
    public double getCPUTime() {
        return CPUTime;
    }

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
    public void setCPUTime(int CPUTime) {
        this.CPUTime = CPUTime;
    }
}
