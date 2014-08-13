/*
 * Starts simulations local
 * Needs: Path to timenet, Set of Parameters, Path to original-File
 * Returns Set of measurements, corresponding to given Set of Parameters

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import timenetexperimentgenerator.Parser;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.helper.ProcMon;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimulatorLocal implements Runnable, Simulator{
ArrayList< ArrayList<parameter> > listOfParameterSets;
ArrayList<SimulationType> listOfCompletedSimulationParsers;
String originalFilename;
String pathToTimeNet;
String tmpFilePath;
private int status=0; //Status of simulations, 0..100%
private int simulationCounter=0;//Startvalue for count of simulations, will be in the filename of sim and log
boolean cancelSimulations=false;
String logFileName;
String actualSimulationLogFile="";//actual log-file for one local simulation
private final String nameOfTempDirectory="14623786483530251523506521233052";
boolean log=true;

    /**
     * Constructor
     */
    public SimulatorLocal(){
    logFileName=support.getTmpPath()+File.separator+"SimLog_LocalSimulation_without_Cache"+Calendar.getInstance().getTimeInMillis()+".csv";
    }

    /**
     * inits the simulator
     * If simulationCounter is set to less then 0, the old value wil be used and continouusly increased
     * @param listOfParameterSetsTMP List of Parameter-sets to be simulated
     * @param simulationCounterTMP start value of simulation counter
    */
    public void initSimulator(ArrayList< ArrayList<parameter> > listOfParameterSetsTMP, int simulationCounterTMP, boolean log){
    this.status=0;
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

    /**
     * Run Method to start simulations and collect the data
     * simulats the SCPNs, main routine
     */
    public void run(){
    this.status=0;
    this.listOfCompletedSimulationParsers=new ArrayList<SimulationType>();
    String line="";
    int numberOfSimulations=0;
        if(support.checkTimeNetPath()){
            try{
            support.log("Timenet-Path ok, starting local simulations.");
            
            support.log("Logfilename is:"+logFileName);
            //Open Logfile and write first line
            FileWriter fw;
                if(listOfParameterSets.size()>0){
                    for(int i=0;i<listOfParameterSets.size();i++){
                    //fw = new FileWriter(logFileName, true);
                    if(cancelSimulations) return;
                    ArrayList<parameter> actualParameterSet=listOfParameterSets.get(i);//get actual parameterset
                    String actualParameterFileName=createLocalSimulationFile(actualParameterSet, this.simulationCounter);//create actual SCPN xml-file and save it in tmp-folder
                    support.log("Simulating file:"+actualParameterFileName);
                    startLocalSimulation(actualParameterFileName);//Returns, when Simulation has ended
                    SimulationType myResults=new SimulationType();//create new SimulationResults
                    //here the SimType has to get Data From Parser;
                    Parser myParser = new Parser();
                    myResults = myParser.parse(actualSimulationLogFile);//parse Log-file and xml-file
                    
                    if(myParser.isParsingSuccessfullFinished())
                        {
                            support.log("Parsing successful.");
                            listOfCompletedSimulationParsers.add(myResults);
                            if(this.log)
                            {
                                support.addLinesToLogFile(myResults, logFileName);
                            }
                            this.listOfCompletedSimulationParsers.add(myResults);//add parser to local list of completed simulations
                        }
                    else
                    {
                        support.log("Error Parsing the Simulation results. Maybe Simulation failure?");
                    }
                    numberOfSimulations++;//increment local simulation counter
                    
                    this.status=numberOfSimulations*100 / listOfParameterSets.size(); //update status of local simulations (in %)
                    this.simulationCounter++;//increment given global simulation counter

                    //fw.close();
                    }

                }
                
               }catch(Exception e){
               support.log("Error while creating local simulation file or log-file.");
               }

        }else{
        support.log("Timenet-Path NOT ok!");
        }
        
    }


    /**
     * Creates the local file for simulation, including all information in file and filename
     * @param p parameterset to be simulated
     * @param simulationNumber number of simulation
     * @return Name of simulation file incl. path
     */
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
     * starts the local simulation run, returns, when simulation has ended
     * @param exportFileName Filename of SCPN to be simulated with TimeNet
     */
    private void startLocalSimulation(String exportFileName){
        try {
        // Execute command
        java.lang.ProcessBuilder processBuilder = new java.lang.ProcessBuilder("java","-jar", "TimeNET.jar",exportFileName, "autostart=true", "autostop=true", "secmax="+support.getIntStringValueFromFileName(exportFileName, "MaxTime"), "endtime="+support.getIntStringValueFromFileName(exportFileName, "EndTime") , "seed="+ support.getIntStringValueFromFileName(exportFileName, "Seed"), "confidence="+support.getIntStringValueFromFileName(exportFileName, "ConfidenceIntervall"), "epsmax="+support.getValueFromFileName(exportFileName, "MaxRelError"));
        processBuilder.directory(new java.io.File(this.pathToTimeNet));
        support.log("Command is: "+processBuilder.command().toString());

        // Start new process
        long timeStamp=Calendar.getInstance().getTimeInMillis();


        final java.lang.Process p = processBuilder.start();
        

        java.util.Scanner s = new java.util.Scanner( p.getInputStream() ).useDelimiter( "\\Z" );//Scans output of process
        support.log( s.next() );//prints output of process into System.out
            try {
                createProcMon(p);
                p.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(SimulatorLocal.class.getName()).log(Level.SEVERE, null, ex);
            }
        timeStamp=(Calendar.getInstance().getTimeInMillis()-timeStamp) / 1000;//Time for calculation in seconds


        //Copy results.log
        String sourceFile=support.removeExtention(exportFileName)+".result"+File.separator+"results.log";
        String sinkFile=support.removeExtention(exportFileName)+"simTime_"+timeStamp+".log";
            if (support.copyFile(sourceFile, sinkFile, false)){
            support.log("Coppied log-file. Now delete the directory and original log-file.");
            File tmpFile=new File(sourceFile);
            tmpFile.delete();
            tmpFile=new File(support.removeExtention(exportFileName)+".result");
            tmpFile.delete();
            support.log("Deleted original Log-file and directory.");
            this.actualSimulationLogFile=sinkFile;
            }
        File file = new File(exportFileName);
        String path=file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)) +File.separator+nameOfTempDirectory;
    
        support.log("Delete Path to tmp files: "+path);
        support.del(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<SimulationType> getListOfCompletedSimulationParsers(){
    return this.listOfCompletedSimulationParsers;
    }


    /**
     * Delete content of tmp-folder, all xml-files, log-files and generated source-code
     */
    public void deleteTmpFiles(){
    File[] listOfFile=new File(this.tmpFilePath).listFiles();
        for(int i=0;i<listOfFile.length;i++){
        support.del(listOfFile[i]);
        }
    }


    /**
     * cancels all remaining simulations and aborts the actual one
    */
    private void cancelSimulations(){
    this.cancelSimulations=true;
    }

    public ProcMon createProcMon(Process proc) {
    ProcMon procMon = new ProcMon(proc);
    Thread t = new Thread(procMon);
    t.start();
    return procMon;
    }

    /**
     * Returns actual number of simulation
     * @return number of actual simulation
     */
    public int getSimulationCounter(){
    return this.simulationCounter;
    }
    
    /**
     * sets the number of simulation to be started with
     * @param i number of simulation for simulation counter
     */
    public void setSimulationCounter(int i){
    this.simulationCounter=i;
    }

    
    /**
     * Returns the status of simulations
     * @return % of simulations that are finished
     */
    public int getStatus(){
    return this.status;
    }
    

}
