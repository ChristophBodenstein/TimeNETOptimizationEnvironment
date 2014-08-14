/*
 * Web based simulator of SCPNs
 * download SCPNs as xml files, start local simulation, upload the results


* To be modified by: Group studies 2014


 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import timenetexperimentgenerator.Parser;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.support;

/**
 *
 * @author sse
 */
public class SimulatorWebSlave implements Runnable{
private boolean shouldEnd=false;
String pathToTimeNet;
String actualSimulationLogFile="";//actual log-file for one local simulation
private final String nameOfTempDirectory="14623786483530251523506521233052";

    public void run() {
        while(true){
        this.pathToTimeNet=support.getPathToTimeNet();//  pathToTimeNetTMP;
        //Request the server Api to get the Status Code and response body.
       // Getting the status code.
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:8080/timenetws-server/rest/api/downloads/ND");     
        HttpResponse response = null;
        String responseString = null;
            try {
                response = client.execute(httpGet);
                responseString = new BasicResponseHandler().handleResponse(response);
            } catch (IOException ex) {
                Logger.getLogger(SimulatorWebSlave.class.getName()).log(Level.SEVERE, null, ex);
            }
                        
        int statusCode = response.getStatusLine().getStatusCode();

        
        List<String> lines = null;
         
     FileWriter fileWriter = null;
        try {

            String filename = response.getFirstHeader("filename").getValue();
            System.out.println("filename======="+filename);
            String exportFileName=this.pathToTimeNet+File.separator+filename;
            File newTextFile = new File(exportFileName);
            fileWriter = new FileWriter(newTextFile);
            fileWriter.write(responseString);
            fileWriter.close();
            startLocalSimulation(exportFileName);
            SimulationType myResults=new SimulationType();//create new SimulationResults
                    //here the SimType has to get Data From Parser;
                    Parser myParser = new Parser();
                    myResults = myParser.parse(actualSimulationLogFile);//parse Log-file and xml-file
                    
                    if(myParser.isParsingSuccessfullFinished())
                        {
                            support.log("Parsing successful.");
                            //listOfCompletedSimulationParsers.add(myResults);
                            //if(this.log)
                            //{
                                //support.addLinesToLogFile(myResults, logFileName);
                            //}
                            //this.listOfCompletedSimulationParsers.add(myResults);//add parser to local list of completed simulations
                        }
                    else
                    {
                        support.log("Error Parsing the Simulation results. Maybe Simulation failure?");
                    }
        } catch (IOException ex) {
               } finally {
            try {
                fileWriter.close();
            } catch (IOException ex) {
               
            }
        }
     /*       String content = "Hello File!";
            String path = "file:///C://Downloads/veer.txt";
            try {
                Files.write( Paths.get(path), responseString.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException ex) {
                Logger.getLogger(SimulatorWebSlave.class.getName()).log(Level.SEVERE, null, ex);
            }   */
            try {
                Thread.sleep(2000);
                
                //get URL!
                //support.getReMoteAddress();
                
            } catch (InterruptedException ex) {
                support.log("Error while sleeping Thread of Slave Web simulator.");
            }
        support.log("Dummy Thread started to download and simulate SCPNs.");
        
            if(this.shouldEnd){
            support.log("Slave Thread will end now.");
            this.shouldEnd=false;
            return;
            }
        
        }
    }

    /**
     * @return the shouldEnd
     */
    public boolean isShouldEnd() {
        return shouldEnd;
    }

    /**
     * @param shouldEnd the shouldEnd to set
     */
    public void setShouldEnd(boolean shouldEnd) {
        this.shouldEnd = shouldEnd;
    }
    private void startLocalSimulation(String exportFileName){
        try {
        // Execute command
        java.lang.ProcessBuilder processBuilder = new java.lang.ProcessBuilder("java","-jar", "TimeNET.jar",exportFileName, "autostart=true", "autostop=true", "secmax="+support.getIntStringValueFromFileName(exportFileName, "MaxTime"), "endtime="+support.getIntStringValueFromFileName(exportFileName, "EndTime") , "seed="+ support.getIntStringValueFromFileName(exportFileName, "Seed"), "confidence="+support.getIntStringValueFromFileName(exportFileName, "ConfidenceIntervall"), "epsmax="+support.getValueFromFileName(exportFileName, "MaxRelError"));
        processBuilder.directory(new java.io.File(this.pathToTimeNet));
        support.log("Command is: "+processBuilder.command().toString());

        // Start new process
        long timeStamp=Calendar.getInstance().getTimeInMillis();


        java.lang.Process p = processBuilder.start();
        
        java.util.Scanner s = new java.util.Scanner( p.getInputStream() ).useDelimiter( "\\Z" );//Scans output of process
        support.log( s.next() );//prints output of process into System.out
            try {
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

}
