/*
 * Parser reads one log file of SCPN-Simulation
 * After Log-File reading it contains all Measurement-data and can be asked for
 */

package timenetexperimentgenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class parser {
private String logName;
private String SimulationType;
private Float SimulationTime;
private ArrayList<MeasureType> Measures=new ArrayList();
private ArrayList<String> tmpStrings=new ArrayList();
private int parseStatus=0;
private int CPUTime=0;
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
        System.out.println("Searching corresponding xml-file for: "+filename);
        String[] tmpFilenameArray=filename.split("simTime");
        xmlFilename=tmpFilenameArray[0]+".xml";
        System.out.println("XML-Filename is:"+xmlFilename);    
        }   else{
            System.out.println("XML-Filename given: "+this.xmlFile);
            xmlFilename=xmlFile;
            }


    File xmlFile=new File(xmlFilename);
        if(!xmlFile.exists()){
        System.out.println("XML-File not found, eject.");
        return false;
        }
        System.out.println("Parsing XML-File: "+xmlFilename);
    try{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	Document doc = docBuilder.parse(xmlFilename);
        NodeList parameterList=doc.getElementsByTagName("parameter");

            for(int i=0;i<parameterList.getLength();i++){
            parameter tmpParameter=new parameter();

            System.out.println(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            tmpParameter.setName(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            tmpParameter.setValue(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue());
            tmpParameterList.add(tmpParameter);
            }
        
        }catch(Exception e){
        e.printStackTrace();
        }


    System.out.println("Parsing log-file: "+filename);
    this.logName=filename;
    try {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line = null;
		while ((line = in.readLine()) != null) {
			//System.out.println("Read line: " + line);
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


    //System.out.println("SimulationType: "+SimulationType);
    //System.out.println("SimulationTime: "+getSimulationTime().toString()+" seconds.");
    
    //Add all Parameters from logfile-name
    segs=logName.split("_");
        for(int i=0;i<segs.length;i++){
            if(segs[i].equals("MaxTime")){
            parameter tmpP=new parameter();
            tmpP.setName("MaxTime");
            tmpP.setValue(segs[i+1]);
            tmpParameterList.add(tmpP);
            }
            if(segs[i].equals("EndTime")){
            parameter tmpP=new parameter();
            tmpP.setName("EndTime");
            tmpP.setValue(segs[i+1]);
            tmpParameterList.add(tmpP);
            }
            if(segs[i].equals("Seed")){
            parameter tmpP=new parameter();
            tmpP.setName("Seed");
            tmpP.setValue(segs[i+1]);
            tmpParameterList.add(tmpP);
            }
            if(segs[i].equals("ConfidenceIntervall")){
            parameter tmpP=new parameter();
            tmpP.setName("Configured-ConfidenceIntervall");
            tmpP.setValue(segs[i+1]);
            tmpParameterList.add(tmpP);
            }
            if(segs[i].equals("MaxRelError")){
            parameter tmpP=new parameter();
            tmpP.setName("MaxRelError");
            tmpP.setValue(segs[i+1]);
            tmpParameterList.add(tmpP);
            }               
            if(segs[i].equals("simTime")){
            parameter tmpP=new parameter();
            tmpP.setName("Used CPUTime");
            //String[] tmpSegs=segs[i+1].split(".");
            tmpP.setValue(segs[i+1].substring(0,segs[i+1].indexOf(".")));
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
                System.out.println("Measures found");
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
                float[] tmpConf={getFloatString(segs[0]),getFloatString(segs[1])}  ;
                tmpMeasure.setConfidenceInterval(tmpConf);
                tmpMeasure.setEpsilon(getFloatString(s.next()));
                tmpMeasure.setParameterList(tmpParameterList);
                this.getMeasures().add(tmpMeasure);
                parseStatus=0;
                System.out.println("Measures "+tmpMeasure.getMeasureName()+" has Epsilon of "+tmpMeasure.getEpsilon()+" and Mean of "+tmpMeasure.getMeanValue() );
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
    public float getMeasureValueByMeasureName(String name){
    float returnValue=(float)0.0;

        for(int i=0;i<this.Measures.size();i++){
            if(this.Measures.get(i).getMeasureName().equals(name)){
            returnValue=Float.valueOf(this.Measures.get(i).getMeanValue());
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
    return (parameter[])parameterList.toArray();
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
    public Float getSimulationTime() {
        return SimulationTime;
    }

    /**
     * @param SimulationTime the SimulationTime to set
     */
    public void setSimulationTime(Float SimulationTime) {
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
    public int getCPUTime() {
        return CPUTime;
    }

    public float getFloatString(String testString){
        if(!testString.equals("nan")){
        return Float.valueOf(testString);
        }else{
        return 0;
        }
    }
}
