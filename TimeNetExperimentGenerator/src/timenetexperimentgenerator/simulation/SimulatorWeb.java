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
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import timenetexperimentgenerator.Parser;
import timenetexperimentgenerator.datamodel.MeasureType;
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
    ArrayList<String> listOfUnproccessedFilesNames ;
    String originalFilename;
    String tmpFilePath;
    String actualSimulationLogFile="";//actual log-file for one local simulation
    String simid;
    private int status=0; //Status of simulations, 0..100%
    private int simulationCounter=0;//Startvalue for count of simulations, will be in the filename of sim and log
    boolean cancelSimulations=false;
    boolean log=true;
    boolean keepSimulationFiles=false;
    HttpClient client = new DefaultHttpClient() ;


    /**
     * Constructor
     */
    public SimulatorWeb(){
        logFileName=support.getTmpPath()+File.separator+"SimLog_DistributedSimulation"+Calendar.getInstance().getTimeInMillis()+".csv";  
        listOfUnproccessedFilesNames = new ArrayList<String>();
    }
    
    public void run(){
        long targetTime=0;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        this.status=0;
        this.listOfCompletedSimulationParsers=new ArrayList<SimulationType>();
        boolean uploadSuccessful=false;//To handle upload errors
        //this.listOfCompletedSimulationParsers=new ArrayList<SimulationType>();
        String line="";
        simid = Long.toString(Calendar.getInstance().getTimeInMillis());
        //int numberOfSimulations=0;
        if(support.isDistributedSimulationAvailable()){
            try{
                support.log("Distributed Simulation available, starting distributed simulations.");

                support.log("Logfilename is:"+logFileName);
                //Open Logfile and write first line
                //FileWriter fw;
                if(listOfParameterSets.size()>0){
                    //Upload XML files
                    for(int i=0;i<listOfParameterSets.size();i++){
                        support.setStatusText("Uploading "+i+"/"+listOfParameterSets.size());
                        //fw = new FileWriter(logFileName, true);
                        if(cancelSimulations) return;
                        ArrayList<parameter> actualParameterSet=listOfParameterSets.get(i);//get actual parameterset

                        String actualParameterFileName=createLocalSimulationFile(actualParameterSet, support.getGlobalSimulationCounter());//create actual SCPN xml-file and save it in tmp-folder
                        File file = new File(actualParameterFileName);
                        while(!uploadSuccessful){
                            try {
                                //Upload the file
                                executeMultiPartRequest(support.getReMoteAddress() + "/rest/file/upload",file,file.getName(), "File Uploaded :: WORDS",simid) ;
                                uploadSuccessful=true;
                                support.log("Upload succesful. Will wait for results.");
                            } catch (Exception ex) {
                                //Logger.getLogger(SimulatorWeb.class.getName()).log(Level.SEVERE, null, ex);
                                support.log("Upload error, will try again in "+support.DEFAULT_SLEEPING_TIME+" ms.");
                                support.waitSingleThreaded(support.DEFAULT_SLEEPING_TIME);
                            }
                            
                        }
                        //add the file to the unprocessed files names 
                        listOfUnproccessedFilesNames.add(support.removeExtention(file.getName()));
                        uploadSuccessful=false;
                        this.simulationCounter++;
                        support.incGlobalSimulationCounter();
                        if(support.isCancelEverything())break;
                    }
                }
            }catch(Exception e){
                support.log("Error while creating local simulation file or log-file.");
            }
            //end of the first phase which is uploading the XML files
            
            //do not start the timer unless we get the first log file
            int i=0,j=Integer.MIN_VALUE;
            //Start sasking the server for log files
            while((i < listOfParameterSets.size())&&(!support.isCancelEverything())) {
                support.setStatusText("Waiting for results.("+i+"/"+listOfParameterSets.size()+")");
                support.spinInLabel();
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(support.getReMoteAddress() + "/rest/api/downloads/log/"+simid);
                HttpResponse response = null;
                String responseString = null;
                try {
                    response = client.execute(httpGet);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if(statusCode == 200) {
                        //reset the timer each time we recieve a new log file
                        j = 0;
                        responseString = new BasicResponseHandler().handleResponse(response);
                        String filename = response.getFirstHeader("filename").getValue(); 
                        String[] tmpFilenameArray=filename.split("simTime");                       
                        String filenameWithoutExtension=tmpFilenameArray[0];
                        //Check wether we already got the same file before if yes discard it else process it 
                        if (listOfUnproccessedFilesNames.contains(filenameWithoutExtension)){
                            
                            List<String> lines = null;
                            FileWriter fileWriter = null;

                            support.log("Downloading filename======="+filename);
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
                            
                            if(myParser.isParsingSuccessfullFinished()) {
                                i++;
                                listOfUnproccessedFilesNames.remove(filenameWithoutExtension);
                                support.log("Parsing successful.");
                                myResults.setIsFromDistributedSimulation(true);
                                listOfCompletedSimulationParsers.add(myResults);
                                if(this.log) {
                                    support.addLinesToLogFile(myResults, logFileName);
                                }

                                this.listOfCompletedSimulationParsers.add(myResults);//add parser to local list of completed simulations

                                if(!keepSimulationFiles){
                                    support.log("Will delete XML-File and log-File.");
                                    tmpFilenameArray=actualSimulationLogFile.split("simTime");                       
                                    String actualParameterFileName=tmpFilenameArray[0]+".xml";
                                    support.del(new File(actualParameterFileName));
                                    support.del(new File(actualSimulationLogFile));
                                }
                            //Update status
                            this.status=100*i/listOfParameterSets.size();
                            }else{
                                //TODO trigger simulation again (send request to server)
                                support.log("The recieved file has been ignored because of problems parsing the result logfile " + filenameWithoutExtension);
                            }
                        }
                    }else{
                        try{
                        EntityUtils.consume(response.getEntity());
                        }catch(Exception e){
                        support.log("Error consuming the http-response while asking for results.");
                        }
                        
                        support.log("Response from server was negative. Will wait "+support.DEFAULT_SLEEPING_TIME+" ms.");
                        //Wait with full force
                        support.waitSingleThreaded(support.DEFAULT_SLEEPING_TIME);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(SimulatorWebSlave.class.getName()).log(Level.SEVERE, null, ex);
                    support.log("Problem connecting to server. please check your network preferences.");
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
    
    /**
     * Returns the status of simulations
     * @return % of simulations that are finished
     */
    public int getStatus() {
        return this.status;
        
    }
    
    /**
     * Returns actual number of simulation
     * @return number of actual simulation
     */
    public int getSimulationCounter() {
        return this.simulationCounter;
        
    }

    /**
     * Returns List of completed simulation parsers.
     * @return List of completed simulation parsers
     */
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
        return this.listOfCompletedSimulationParsers;
    }
    
    /**
     * Execute Multipart rquest to http-server for uploading files and text data
     * 
     * @param urlString complete Adress+dir for uploading file and addition data (http://example.com/dir)
     * @param file File to be uploaded
     * @param fileName Filename to be used in multipart-text-field fileName
     * @param fileDescription fileDescription to be used in multipart-text-field fileDescription
     * @param simid simid to be used in multipart-text-field simid, identifies th starting simulator
     * @throws java.lang.Exception
     */
   public void executeMultiPartRequest(String urlString, File file, String fileName, String fileDescription,String simid) throws Exception 
    {
    	//HttpClient client = new DefaultHttpClient() ;
        HttpPost postRequest = new HttpPost (urlString) ;
        try
        {
            support.log("Try to connect "+urlString);
            
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

            EntityUtils.consume(response.getEntity());
            
        }
        catch (Exception ex)
        {
            support.log(ex.getLocalizedMessage());
            throw(ex);
        }
    }
   
   /**
     * Returns the calulated optimimum
     * For Benchmark-Functions this can be calculated.
     * For other simulators, this must be given by user.
     */
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {
    support.log("SimulatorWeb: Getting absolute optimum simulation from Cache. Will return null.");
        return null;
    }
}
