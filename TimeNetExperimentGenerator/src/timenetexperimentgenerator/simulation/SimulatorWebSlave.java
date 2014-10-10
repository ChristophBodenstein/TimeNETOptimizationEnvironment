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
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import timenetexperimentgenerator.support;

/**
 *
 * @author sse
 */
public class SimulatorWebSlave implements Runnable{
private boolean shouldEnd=false;
String pathToTimeNet;
String simid="";
String actualSimulationLogFile="";//actual log-file for one local simulation
private final String nameOfTempDirectory="14623786483530251523506521233052";

public void run() {
    while(!shouldEnd){
        this.pathToTimeNet=support.getPathToTimeNet();//  pathToTimeNetTMP;
        //Request the server Api to get the Status Code and response body.
        // Getting the status code.
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(support.getReMoteAddress() + "/rest/api/downloads/ND");
        HttpResponse response = null;
        String responseString = null;
        try {
            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            support.log("Response for "+httpGet.toString() +"is "+statusCode);
            if(statusCode == 200) {
                responseString = new BasicResponseHandler().handleResponse(response);
                FileWriter fileWriter = null;
                String filename = response.getFirstHeader("filename").getValue();
                simid = response.getFirstHeader("simid").getValue();
                support.log("Downloaded simulation filename======="+filename);
                String exportFileName=support.getTmpPath()+File.separator+filename;
                File newTextFile = new File(exportFileName);
                fileWriter = new FileWriter(newTextFile);
                fileWriter.write(responseString);
                fileWriter.close();
                support.log("File written. Try to start simulation.");
                startLocalSimulation(exportFileName);

            }
        } catch (IOException ex) {
            support.log("IOException during HTTP-Request for simulations.");
        }
        try {
            Thread.sleep(support.DEFAULT_SLEEPING_TIME);

            //get URL!
            //support.getReMoteAddress();

        } catch (InterruptedException ex) {
            support.log("Error while sleeping Thread of Slave Web simulator.");
        }
        //support.log("Dummy Thread started to download and simulate SCPNs.");
        support.spinInLabel();
        support.setStatusText("Waiting for simulation tasks.");

        if(this.shouldEnd){
            support.log("Slave Thread will end now.");
            support.setStatusText("");
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
            File logFile=new File(sinkFile);
            executeMultiPartRequest(support.getReMoteAddress() + "/rest/log/upload",logFile,logFile.getName(), "File Uploaded :: WORDS", simid) ;
        }
        File file = new File(exportFileName);
        String path=file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf(File.separator)) +File.separator+nameOfTempDirectory;
    
        support.log("Delete Path to tmp files: "+path);
        support.del(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(SimulatorWebSlave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void executeMultiPartRequest(String urlString, File file, String fileName, String fileDescription, String simid) throws Exception 
    {
    	HttpClient client = new DefaultHttpClient() ;
        HttpPost postRequest = new HttpPost (urlString) ;
        try {
            //Set various attributes 
            MultipartEntity multiPartEntity = new MultipartEntity () ;
            multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : "")) ;
            multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName())) ;
            multiPartEntity.addPart("simid", new StringBody(simid)) ;
 
            //FileBody fileBody = new FileBody(file, "application/octect-stream") ;
            FileBody fileBody = new FileBody(file, "multipart/form-data");
            //Prepare payload
            multiPartEntity.addPart("attachment", fileBody) ;
 
            //Set to request body
            postRequest.setEntity(multiPartEntity) ;
            
            //Send request
            HttpResponse response = client.execute(postRequest) ;
            
            //Verify response if any
            if (response != null) {
                System.out.println(response.getStatusLine().getStatusCode());
            }
        } catch (Exception ex) {
            ex.printStackTrace() ;
        }
    }
}
