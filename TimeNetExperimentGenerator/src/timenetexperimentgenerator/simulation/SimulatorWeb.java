/*
 * Web based simulator of SCPNs
 * build SCPNs, upload them, wait for result, download results, return results


* To be modified by: Group studies 2014


 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import timenetexperimentgenerator.Parser;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.support;
/**
 *
 * @author Christoph Bodenstein & ...
 */
public class SimulatorWeb implements Runnable, Simulator{
String logFileName="";
ArrayList< ArrayList<parameter> > listOfParameterSets;
ArrayList<SimulationType> listOfCompletedSimulationParsers;
String originalFilename;
String pathToTimeNet;
String tmpFilePath;
String actualSimulationLogFile="";//actual log-file for one local simulation
String simid;
private int status=0; //Status of simulations, 0..100%
private int simulationCounter=0;//Startvalue for count of simulations, will be in the filename of sim and log
boolean cancelSimulations=false;
boolean log=true;
boolean keepSimulationFiles=false;


    /**
     * Constructor
     */
    public SimulatorWeb(){
    logFileName=support.getTmpPath()+File.separator+"SimLog_DistributedSimulation"+Calendar.getInstance().getTimeInMillis()+".csv";  
    }
    
    public void run(){
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    this.status=0;
    this.listOfCompletedSimulationParsers=new ArrayList<SimulationType>();
    //this.listOfCompletedSimulationParsers=new ArrayList<SimulationType>();
    String line="";
    simid = Long.toString(Calendar.getInstance().getTimeInMillis());
    //int numberOfSimulations=0;
        if(support.checkTimeNetPath()){
            try{
            support.log("Timenet-Path ok, starting local simulations.");
            
            support.log("Logfilename is:"+logFileName);
            //Open Logfile and write first line
            //FileWriter fw;
                if(listOfParameterSets.size()>0){
                    for(int i=0;i<listOfParameterSets.size();i++){
                    //fw = new FileWriter(logFileName, true);
                    if(cancelSimulations) return;
                    ArrayList<parameter> actualParameterSet=listOfParameterSets.get(i);//get actual parameterset
                    
                    String actualParameterFileName=createLocalSimulationFile(actualParameterSet, this.simulationCounter);//create actual SCPN xml-file and save it in tmp-folder
                    File file = new File(actualParameterFileName);
                        try {
        //Upload the file
        executeMultiPartRequest(support.getReMoteAddress() + "/rest/file/upload",file,file.getName(), "File Uploaded :: WORDS",simid) ;
    } catch (Exception ex) {
        Logger.getLogger(SimulatorWeb.class.getName()).log(Level.SEVERE, null, ex);
    }}
                }
               }catch(Exception e){
               support.log("Error while creating local simulation file or log-file.");
               }
int i=0;
while(i < listOfParameterSets.size()){
           HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(support.getReMoteAddress() + "/rest/api/downloads/log/"+simid);
        HttpResponse response = null;
        String responseString = null;
            try {
                response = client.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode == 200) {
                    i++;
                    responseString = new BasicResponseHandler().handleResponse(response);
                    List<String> lines = null;
                    FileWriter fileWriter = null;

                    String filename = response.getFirstHeader("filename").getValue();
                    System.out.println("filename======="+filename);
                    String exportFileName=tmpFilePath+File.separator+filename;
                    File newTextFile = new File(exportFileName);
                    fileWriter = new FileWriter(newTextFile);
                    fileWriter.write(responseString);
                    fileWriter.close();
                    actualSimulationLogFile=exportFileName;
                    //SimulationType myResults=new SimulationType();//create new SimulationResults
                    //here the SimType has to get Data From Parser;
                    Parser myParser = new Parser();
                    SimulationType myResults= myParser.parse(actualSimulationLogFile);//parse Log-file and xml-file

                    if(myParser.isParsingSuccessfullFinished())
                        {
                            support.log("Parsing successful.");
                            //listOfCompletedSimulationParsers.add(myResults);
                            if(this.log)
                            {
                                support.addLinesToLogFile(myResults, logFileName);
                            }

                            this.listOfCompletedSimulationParsers.add(myResults);//add parser to local list of completed simulations

                            if(!keepSimulationFiles){
                            support.log("Will delete XML-File and log-File.");
                            String[] tmpFilenameArray=actualSimulationLogFile.split("simTime");
                            String actualParameterFileName=tmpFilenameArray[0]+".xml";
                            support.del(new File(actualParameterFileName));
                            support.del(new File(actualSimulationLogFile));
                            }
                        }
                }
                else {
                    try {
                       Thread.sleep(2000);         
                    } catch (InterruptedException ex) {
                    support.log("Error while sleeping Thread of Web simulator.");
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(SimulatorWebSlave.class.getName()).log(Level.SEVERE, null, ex);
            }

}
        }else{
        support.log("Timenet-Path NOT ok!");
        }
     
    }

    public void initSimulator(ArrayList< ArrayList<parameter> > listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        this.listOfParameterSets=listOfParameterSetsTMP;
        this.log=log;
        this.originalFilename=support.getOriginalFilename();//  originalFilenameTMP;
        this.pathToTimeNet=support.getPathToTimeNet();//  pathToTimeNetTMP;
        this.tmpFilePath=support.getTmpPath();// tmpFilePathTMP;
        if(simulationCounterTMP>=0){
            this.simulationCounter=simulationCounterTMP;}

        //Start this thread
        new Thread(this).start();
        
        

    }
    private String createLocalSimulationFile(ArrayList<parameter> p, int simulationNumber){
    String fileNameOfLocalSimulationFile="";
    File f = new File(this.originalFilename);
    try{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(this.originalFilename);
        NodeList parameterList=doc.getElementsByTagName("parameter");
        String ConfidenceIntervall="90", Seed="0", EndTime="0", MaxTime="0",MaxRelError="5";
            for(int parameterNumber=0; parameterNumber< p.size();parameterNumber++){
                        if(!p.get(parameterNumber).isExternalParameter()){
                            for(int i=0;i<parameterList.getLength();i++){
                                if(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(p.get(parameterNumber).getName())){
                                parameterList.item(i).getAttributes().getNamedItem("defaultValue").setNodeValue(p.get(parameterNumber).getStringValue());
                                }
                                //support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                            }
                        }else{
                            if(p.get(parameterNumber).getName().equals("MaxTime")){
                            MaxTime=p.get(parameterNumber).getStringValue();
                            }
                            if(p.get(parameterNumber).getName().equals("EndTime")){
                            EndTime=p.get(parameterNumber).getStringValue();
                            }
                            if(p.get(parameterNumber).getName().equals("Seed")){
                            Seed=p.get(parameterNumber).getStringValue();
                            }
                            if(p.get(parameterNumber).getName().equals("ConfidenceIntervall")){
                            ConfidenceIntervall=p.get(parameterNumber).getStringValue();
                            }
                            if(p.get(parameterNumber).getName().equals("MaxRelError")){
                            MaxRelError=p.get(parameterNumber).getStringValue();
                            }

                        }
                    }
                
                //Dateiname bilden
                String exportFileName=this.tmpFilePath+File.separator+support.removeExtention(f.getName())+"_n_"+simulationNumber+"_MaxTime_"+MaxTime+"_EndTime_"+EndTime+"_Seed_"+Seed+"_ConfidenceIntervall_"+ConfidenceIntervall+"_MaxRelError_"+MaxRelError+"_.xml";
                //Exportieren
                support.log("File to export: "+exportFileName);

                TransformerFactory tFactory =
                TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();

                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(exportFileName));
                transformer.transform(source, result);

                return exportFileName;

    }catch(Exception e){
        return fileNameOfLocalSimulationFile;
    }finally{}
    
    }

    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getSimulationCounter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   public void executeMultiPartRequest(String urlString, File file, String fileName, String fileDescription,String simid) throws Exception 
    {
    	HttpClient client = new DefaultHttpClient() ;
        HttpPost postRequest = new HttpPost (urlString) ;
        try
        {
            
        	//Set various attributes 
            MultipartEntity multiPartEntity = new MultipartEntity () ;
            multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : "")) ;
            multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName())) ;
            multiPartEntity.addPart("simid", new StringBody(simid));
 
            
            //FileBody fileBody = new FileBody(file, "application/octect-stream") ;
            FileBody fileBody = new FileBody(file, "multipart/form-data");
            //Prepare payload
            multiPartEntity.addPart("attachment", fileBody) ;
 
            //Set to request body
            postRequest.setEntity(multiPartEntity) ;
            
            //Send request
            HttpResponse response = client.execute(postRequest) ;
            
            //Verify response if any
            if (response != null)
            {
                System.out.println(response.getStatusLine().getStatusCode());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace() ;
        }
    }
}
