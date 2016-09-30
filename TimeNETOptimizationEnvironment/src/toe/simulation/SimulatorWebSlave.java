/*
 * Web based simulator of SCPNs
 * download SCPNs as xml files, start local simulation, upload the results


 * To be modified by: Group studies 2014


 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.util.EntityUtils;
import toe.HttpFactory;
import toe.support;
import toe.typedef;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Group studies, Christoph Bodenstein
 */
public class SimulatorWebSlave extends Thread {

    private boolean shouldEnd = false;
    String pathToTimeNet;
    String simid = "";
    String actualSimulationLogFile = "";//actual log-file for one local simulation
    private final String nameOfTempDirectory = "14623786483530251523506521233052";
    HttpClient client;
    HttpGet httpGet;
    HttpPost postRequest;
    String clientID;//ID of this client to be marked in server to append to download request
    String clientSkills;//Skills of Client in one String to append to download request

    /**
     * Constructor
     */
    public SimulatorWebSlave() {
        this.client = HttpFactory.getHttpClient();
    }

    /**
     * Run-Method to be startet with Therad.start() Will start downloading files
     * from server, checking for next files, starting loacal simulations and
     * uploading results
     */
    @Override
    public void run() {
        support.setLogToWindow(false);//stop window-logging. this is not saved in properties
        this.pathToTimeNet = support.getPathToTimeNet();//  pathToTimeNetTMP;
        clientID = String.valueOf(Math.random()) + Long.toString(Calendar.getInstance().getTimeInMillis());
        clientID = String.valueOf(Math.abs(clientID.hashCode()));
        //TODO set skill-string correctly!
        clientSkills = "";
        if (support.checkTimeNetPath()) {
            clientSkills += "_" + typedef.typeOfClientSkills.TIMENET.toString() + "_";
        }

        while (!shouldEnd) {
            //Request the server Api to get the Status Code and response body.
            // Getting the status code.
            client = HttpFactory.getHttpClient();
            httpGet = HttpFactory.getGetRequest(support.getReMoteAddress() + "/rest/api/downloads/ND?ID=" + clientID + "&SKILLS=" + clientSkills);
            HttpResponse response;
            String responseString;
            try {
                response = client.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                support.log("Response for " + httpGet.toString() + "is " + statusCode, typeOfLogLevel.INFO);
                if (statusCode == 200) {
                    responseString = new BasicResponseHandler().handleResponse(response);
                    FileWriter fileWriter;
                    String filename = response.getFirstHeader("filename").getValue();
                    simid = response.getFirstHeader("simid").getValue();
                    support.log("Downloaded simulation filename=======" + filename, typeOfLogLevel.INFO);
                    String exportFileName = support.getTmpPath() + File.separator + filename;
                    File newTextFile = new File(exportFileName);
                    fileWriter = new FileWriter(newTextFile);
                    fileWriter.write(responseString);
                    fileWriter.close();
                    support.log("File written. Try to start simulation.", typeOfLogLevel.INFO);
                    startLocalSimulation(exportFileName);

                }
                //HttpClient client = new DefaultHttpClient() ;
                //Verify response if any
                if (response != null) {
                    support.log("Responsecode of task-request: " + Integer.toString(response.getStatusLine().getStatusCode()), typeOfLogLevel.INFO);
                    EntityUtils.consume(response.getEntity());
                }
            } catch (Exception ex) {
                support.log("IOException during HTTP-Request for simulations. Error msg: " + ex.getLocalizedMessage(), typeOfLogLevel.ERROR);
            }
            try {
                Thread.sleep(support.DEFAULT_SLEEPING_TIME);

            } catch (InterruptedException ex) {
                support.log("Error while sleeping Thread of Slave Web simulator. Error msg: " + ex.getLocalizedMessage(), typeOfLogLevel.ERROR);
            }

            support.spinInLabel();
            support.setStatusText("Waiting for simulation tasks.");

            if (this.shouldEnd) {
                support.log("Slave Thread will end now.", typeOfLogLevel.INFO);
                support.setStatusText("");
            }
        }
        support.setLogToWindow(true);//start window-logging. this is not saved in properties
    }

    /**
     * @return the shouldEnd, if true, the Thread is going to end soon
     */
    public boolean isShouldEnd() {
        return shouldEnd;
    }

    /**
     * @param shouldEnd the shouldEnd to set, if true, the Thread should shut
     * down normally
     */
    public void setShouldEnd(boolean shouldEnd) {
        this.shouldEnd = shouldEnd;
    }

    /**
     * Start the simualtion using the local TimeNET installation
     *
     * @param exportFileName Name of SCPN to be simulated loacally
     */
    private void startLocalSimulation(String exportFileName) {
        try {
            //Calculate timeout, give Maxtime * 2
            int timeOut = Float.valueOf(support.getIntStringValueFromFileName(exportFileName, "MaxTime")).intValue() * 2;
            timeOut += Calendar.getInstance().getTimeInMillis() * 1000;//End time in Seconds
            // Execute command
            java.lang.ProcessBuilder processBuilder = new java.lang.ProcessBuilder("java", "-jar", "TimeNET.jar", exportFileName, "autostart=true", "autostop=true", "secmax=" + support.getIntStringValueFromFileName(exportFileName, "MaxTime"), "endtime=" + support.getIntStringValueFromFileName(exportFileName, "EndTime"), "seed=" + support.getIntStringValueFromFileName(exportFileName, "Seed"), "confidence=" + support.getIntStringValueFromFileName(exportFileName, "ConfidenceIntervall"), "epsmax=" + support.getValueFromFileName(exportFileName, "MaxRelError"));
            processBuilder.directory(new java.io.File(this.pathToTimeNet));
            support.log("Command is: " + processBuilder.command().toString(), typeOfLogLevel.INFO);

            // Start new process
            long timeStamp = Calendar.getInstance().getTimeInMillis();

            java.lang.Process p = processBuilder.start();

            java.util.Scanner s = new java.util.Scanner(p.getInputStream()).useDelimiter("\\Z");//Scans output of process
            support.log(s.next(), typeOfLogLevel.INFO);//prints output of process into log
            boolean isRunning = true;
            while (isRunning) {
                try {
                    if (p.exitValue() == 0) {
                        isRunning = false;
                    }
                } catch (IllegalThreadStateException e) {
                }
                if (Calendar.getInstance().getTimeInMillis() * 1000 >= timeOut) {
                    p.destroy();
                    isRunning = false;
                }
            }
            timeStamp = (Calendar.getInstance().getTimeInMillis() - timeStamp) / 1000;//Time for calculation in seconds

            //Copy results.log
            String sourceFile = support.removeExtention(exportFileName) + ".result" + File.separator + "results.log";
            String sinkFile = support.removeExtention(exportFileName) + "simTime_" + timeStamp + ".log";
            if (support.copyFile(sourceFile, sinkFile, false)) {
                support.log("Coppied log-file. Now delete the directory and original log-file.", typeOfLogLevel.INFO);
                File tmpFile = new File(sourceFile);
                tmpFile.delete();
                tmpFile = new File(support.removeExtention(exportFileName) + ".result");
                tmpFile.delete();
                support.log("Deleted original Log-file and directory. Try to upload result.", typeOfLogLevel.INFO);
                this.actualSimulationLogFile = sinkFile;
                File logFile = new File(sinkFile);
                executeMultiPartRequest(support.getReMoteAddress() + "/rest/log/upload", logFile, logFile.getName(), "File Uploaded :: WORDS", simid);
            }
            File file = new File(exportFileName);
            String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator)) + File.separator + nameOfTempDirectory;

            support.log("Delete Path to tmp files: " + path, typeOfLogLevel.INFO);
            support.del(new File(path));
            support.log("Delete local log-file.", typeOfLogLevel.INFO);
            support.del(new File(sinkFile));
            support.log("Delete local xml-file.", typeOfLogLevel.INFO);
            support.del(new File(exportFileName));
        } catch (IOException e) {
            support.log(e.getLocalizedMessage(), typeOfLogLevel.ERROR);
        } catch (Exception ex) {
            support.log(ex.getLocalizedMessage(), typeOfLogLevel.ERROR);
        }
    }

    /**
     * Execute reauest to upload result log-file to server
     *
     * @param urlString Address of server inkl. directory and simid
     * @param file file to upload to server
     * @param fileName name of file to upload to server
     * @param fileDescription Description of file to be uploaded
     * @param simid SIMID of simulations
     * @throws java.lang.Exception
     */
    public void executeMultiPartRequest(String urlString, File file, String fileName, String fileDescription, String simid) throws Exception {
        //HttpClient client = new DefaultHttpClient() ;
        postRequest = HttpFactory.getPostRequest(urlString);
        try {
            //Set various attributes 
            MultipartEntity multiPartEntity = new MultipartEntity();
            multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : ""));
            multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName()));
            multiPartEntity.addPart("simid", new StringBody(simid));

            //FileBody fileBody = new FileBody(file, "application/octect-stream") ;
            FileBody fileBody = new FileBody(file, "multipart/form-data");
            //Prepare payload
            multiPartEntity.addPart("attachment", fileBody);

            //Set to request body
            postRequest.setEntity(multiPartEntity);

            //Send request
            HttpResponse response = client.execute(postRequest);

            //Verify response if any
            if (response != null) {
                support.log("Responsecode of Upload-Request: " + Integer.toString(response.getStatusLine().getStatusCode()), typeOfLogLevel.INFO);
                EntityUtils.consume(response.getEntity());
            }

        } catch (Exception ex) {
            support.log("Exception while uploading logfile. msg: " + ex.getLocalizedMessage(), typeOfLogLevel.ERROR);
        }
    }
}
