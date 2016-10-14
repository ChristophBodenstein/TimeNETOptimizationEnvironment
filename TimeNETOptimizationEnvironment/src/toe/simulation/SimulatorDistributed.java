/*
 * Web based simulator of SCPNs
 * build SCPNs, upload them, wait for result, download results, return results


 * To be modified by: Group studies 2014


 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import toe.HttpFactory;
import toe.Parser;
import toe.datamodel.MeasureType;
import toe.datamodel.SimulationType;
import toe.datamodel.parameter;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Christoph Bodenstein & ...
 */
public class SimulatorDistributed extends Thread implements Simulator {

    String logFileName = "";
    ArrayList< ArrayList<parameter>> listOfParameterSets;
    private HashMap<String, ArrayList<parameter>> listOfParametersetsByFileNameHashmap = new HashMap<>();
    ArrayList<SimulationType> listOfCompletedSimulationParsers;
    ArrayList<String> listOfUnproccessedFilesNames;
    String originalFilename;
    String tmpFilePath;
    String actualSimulationLogFile = "";//actual log-file for one local simulation
    String simid;
    private int status = 0; //Status of simulations, 0..100%
    boolean cancelSimulations = false;
    boolean log = true;
    boolean keepSimulationFiles = false;
    HttpClient client = null;
    HttpGet httpGet = null;

    /**
     * Constructor
     */
    public SimulatorDistributed() {
        super();
        logFileName = support.getTmpPath() + File.separator + "SimLog_" + getClass().getSimpleName() + "_" + Calendar.getInstance().getTimeInMillis() + ".csv";
        support.log("LogfileName:" + logFileName, typeOfLogLevel.INFO);
        listOfUnproccessedFilesNames = new ArrayList<>();
        simid = Long.toString(Calendar.getInstance().getTimeInMillis());
    }

    /**
     * Run-method of this thread, to be called by Thread.start()
     */
    @Override
    public void run() {
        client = HttpFactory.getHttpClient();
        boolean uploadSuccessful;//To handle upload errors
        ArrayList<parameter> actualParameterSet;

        support.log("Web-Simulation-Thread started to simulate " + listOfParameterSets.size() + " simulations.", typeOfLogLevel.INFO);
        if (support.isDistributedSimulationAvailable()) {
            try {
                support.log("Distributed Simulation available, starting distributed simulations.", typeOfLogLevel.INFO);

                support.log("Logfilename is:" + logFileName, typeOfLogLevel.INFO);
                support.log("simid: " + simid, typeOfLogLevel.INFO);
                //Open Logfile and write first line
                //FileWriter fw;
                if (listOfParameterSets.size() > 0) {
                    //Upload XML files
                    for (int i = 0; i < listOfParameterSets.size(); i++) {
                        support.setStatusText("Uploading " + i + "/" + listOfParameterSets.size());
                        if (cancelSimulations) {
                            return;
                        }
                        actualParameterSet = listOfParameterSets.get(i);//get actual parameterset

                        String actualParameterFileName = createLocalSimulationFile(actualParameterSet, support.getGlobalSimulationCounter());//create actual SCPN xml-file and save it in tmp-folder
                        File file = new File(actualParameterFileName);
                        int count = 0;
                        uploadSuccessful = false;
                        while (!uploadSuccessful) {
                            count++;
                            support.log("Trying to upload the same simulation " + count + " time. ThreadID:" + Thread.currentThread().toString(), typeOfLogLevel.INFO);
                            try {
                                //Upload the file
                                uploadSimulationFile(support.getReMoteAddress() + "/rest/file/upload", file, file.getName(), "File Uploaded :: WORDS");
                                uploadSuccessful = true;
                                support.log("Upload successful. Will wait for results. Try: " + count, typeOfLogLevel.INFO);
                            } catch (Exception ex) {
                                support.log("Upload error, will try again in " + support.DEFAULT_SLEEPING_TIME + " ms.", typeOfLogLevel.INFO);
                                support.waitSingleThreaded(support.DEFAULT_SLEEPING_TIME);
                                Thread.currentThread().join();
                            }

                        }
                        //add the file to the unprocessed files names 
                        listOfUnproccessedFilesNames.add(support.removeExtention(file.getName()));
                        listOfParametersetsByFileNameHashmap.put(support.removeExtention(file.getName()), actualParameterSet);
                        support.log("Used xml-filename in hashmap: " + support.removeExtention(file.getName()), typeOfLogLevel.VERBOSE);
                        support.del(file);//delete tmp xml-file
                        support.incGlobalSimulationCounter();
                        if (support.isCancelEverything()) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                support.log("Error while uploading simulations to server.", typeOfLogLevel.ERROR);
                support.log(e.getMessage(), typeOfLogLevel.ERROR);
            }
            //end of the first phase which is uploading the XML files

            //Start of Downloading phase
            int i = 0;
            //Start asking the server for log files
            while ((i < listOfParameterSets.size()) && (!support.isCancelEverything())) {
                support.setStatusText("Waiting for results.(" + i + "/" + listOfParameterSets.size() + ")");
                support.spinInLabel();
                HttpResponse response = null;
                String responseString;

                httpGet = HttpFactory.getGetRequest(support.getReMoteAddress() + "/rest/api/downloads/log/" + simid);
                //support.log("asking for results with address:" + support.getReMoteAddress() + "/rest/api/downloads/log/" + simid);
                client = HttpFactory.getHttpClient();
                try {

                    response = client.execute(httpGet);

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        responseString = new BasicResponseHandler().handleResponse(response);
                        String filename = response.getFirstHeader("filename").getValue();
                        String[] tmpFilenameArray = filename.split("simTime");
                        String filenameWithoutExtension = tmpFilenameArray[0];
                        //Check wether we already got the same file before if yes discard it else process it 
                        if (listOfUnproccessedFilesNames.contains(filenameWithoutExtension)) {

                            FileWriter fileWriter;

                            support.log("Downloading filename=======" + filename, typeOfLogLevel.INFO);
                            String exportFileName = tmpFilePath + File.separator + filename;
                            File newTextFile = new File(exportFileName);
                            fileWriter = new FileWriter(newTextFile);
                            fileWriter.write(responseString);
                            fileWriter.close();
                            actualSimulationLogFile = exportFileName;

                            String[] tmpArrayToGetSimulationCounter=filenameWithoutExtension.split("n_");
                            tmpArrayToGetSimulationCounter = tmpArrayToGetSimulationCounter[1].split("_Max");
                            
                            
                            //create the xml-file again for parsing, tmp solution
                            support.log("Used xml-filename on network: " + filenameWithoutExtension, typeOfLogLevel.VERBOSE);
                            actualParameterSet = listOfParametersetsByFileNameHashmap.get(filenameWithoutExtension);
                            createLocalSimulationFile(actualParameterSet, new Integer(tmpArrayToGetSimulationCounter[0]));

                            Parser myParser = new Parser();
                            SimulationType myResults = myParser.parse(actualSimulationLogFile);//parse Log-file and xml-file

                            if (myParser.isParsingSuccessfullFinished()) {
                                i++;
                                listOfUnproccessedFilesNames.remove(filenameWithoutExtension);
                                support.log("Parsing successful.", typeOfLogLevel.INFO);
                                myResults.setIsFromDistributedSimulation(true);
                                if (this.log) {
                                    support.addLinesToLogFile(myResults, logFileName);
                                }

                                this.listOfCompletedSimulationParsers.add(myResults);//add parser to local list of completed simulations

                                if (!keepSimulationFiles) {
                                    support.log("Will delete XML-File and log-File.", typeOfLogLevel.INFO);
                                    tmpFilenameArray = actualSimulationLogFile.split("simTime");
                                    String actualParameterFileName = tmpFilenameArray[0] + ".xml";
                                    support.del(new File(actualParameterFileName));
                                    support.del(new File(actualSimulationLogFile));
                                }
                                //Update status
                                this.status = 100 * i / listOfParameterSets.size();
                                support.log("Status of WebSimulator: " + this.status, typeOfLogLevel.INFO);
                                this.deleteSimulationOnServer(filenameWithoutExtension);
                            } else {
                                //Trigger simulation again (send request to server)
                                support.log("The received file has been ignored because of problems parsing the result logfile " + filenameWithoutExtension, typeOfLogLevel.INFO);
                                this.resetSimulation(filenameWithoutExtension);
                            }
                        }

                    } else {
                        //support.log("Response from server was negative. Will wait "+support.DEFAULT_SLEEPING_TIME+" ms.");
                        //Wait with full force
                        support.waitSingleThreaded(support.DEFAULT_SLEEPING_TIME);
                        //Thread.sleep(support.DEFAULT_SLEEPING_TIME);
                    }

                } catch (Exception ex) {
                    support.log("Problem connecting to server (asking for results). please check your network preferences.", typeOfLogLevel.ERROR);
                    support.log(ex.getMessage(), typeOfLogLevel.ERROR);
                } finally {
                    //support.log("Trying to consume Response from upload.");
                    try {
                        if (response != null) {
                            try {
                                EntityUtils.consume(response.getEntity());
                            } catch (final ConnectionClosedException ignore) {
                                support.log("Connection-Closed-Exception while consuming http-response!", typeOfLogLevel.ERROR);
                                HttpFactory.resetConnections();
                            }
                        }

                    } catch (Exception ex) {
                        support.log("Error consuming the http-response while asking for results.", typeOfLogLevel.ERROR);
                        support.log(ex.getMessage(), typeOfLogLevel.ERROR);
                    }
                    //support.log("Try to cleanup after download.");
                    httpGet.releaseConnection();
                }

            }
            if (support.isCancelEverything()) {
                support.log("Distributed Simulation canceled. Will send command to delete all simulations on server.", typeOfLogLevel.INFO);
                deleteAllSimulationsFromServer();
            } else {
                support.log("All Simulation results collected from server. Simulator will end.", typeOfLogLevel.INFO);
            }

            /*try {
             Thread.currentThread().join();
             } catch (InterruptedException ex) {
             support.log("Error joining this distributed-simulation thread.");
             }*/
        } else {
            support.log("TimeNET-Path NOT ok!", typeOfLogLevel.INFO);
        }
        support.log("End of Thread reached, should end now.", typeOfLogLevel.INFO);
        synchronized (this) {
            notify();
        }
    }

    /**
     * Reset the simulation. Send reset-request to server. This will delete the
     * log-file and cause a new simulation
     *
     * @param filename Filename of simulation to reset
     */
    public void resetSimulation(String filename) {
        client = HttpFactory.getHttpClient();
        HttpPost postRequest = HttpFactory.getPostRequest(support.getReMoteAddress() + "/resetSimulation");
        try {
            support.log("Try to connect " + postRequest.getURI().toString(), typeOfLogLevel.INFO);

            //Set various attributes 
            MultipartEntity multiPartEntity = new MultipartEntity();
            multiPartEntity.addPart("prefix", new StringBody(filename));
            multiPartEntity.addPart("simid", new StringBody(simid));
            //Set to request body
            postRequest.setEntity(multiPartEntity);

            BasicResponseHandler myTmpResponseHandler = new BasicResponseHandler();

            //Send request
            String result = client.execute(postRequest, myTmpResponseHandler);
            support.log("Response of Upload was:" + result, typeOfLogLevel.INFO);
            if (result.contains("false")) {
                throw new Exception("Simulation-Reset not successful. Server returned false!");
            }
            if (result.contains("true")) {
                support.log("Deletion of file " + filename + " successful.", typeOfLogLevel.INFO);
            }
            multiPartEntity.consumeContent();
            //EntityUtils.consume(response.getEntity());
            postRequest.releaseConnection();
            postRequest.reset();

        } catch (Exception ex) {
            support.log("Error resetting simulation: " + filename, typeOfLogLevel.ERROR);
            support.log(ex.getLocalizedMessage(), typeOfLogLevel.ERROR);
        }
    }

    /**
     * Delete all related files for this simulation (log, xml)
     *
     * @param filename Name of simulation file to be deleted from server
     */
    public void deleteSimulationOnServer(String filename) {
        client = HttpFactory.getHttpClient();
        HttpPost postRequest = HttpFactory.getPostRequest(support.getReMoteAddress() + "/deleteSimulation");
        try {
            support.log("Try to connect " + postRequest.getURI().toString(), typeOfLogLevel.INFO);

            //Set various attributes 
            MultipartEntity multiPartEntity = new MultipartEntity();
            multiPartEntity.addPart("prefix", new StringBody(filename));
            multiPartEntity.addPart("simid", new StringBody(simid));
            multiPartEntity.addPart("serversecret", new StringBody(support.getServerSecret()));
            //Set to request body
            postRequest.setEntity(multiPartEntity);

            BasicResponseHandler myTmpResponseHandler = new BasicResponseHandler();

            //Send request
            String result = client.execute(postRequest, myTmpResponseHandler);
            //support.log("Response of delete-request was:" + result);
            if (result.contains("false")) {
                throw new Exception("Deletion of file " + filename + " not successful. Server returned false!");
            }
            if (result.contains("true")) {
                //support.log("Deletion of file " + filename + " successful.");
            }
            multiPartEntity.consumeContent();
            //EntityUtils.consume(response.getEntity());
            postRequest.releaseConnection();
            postRequest.reset();

        } catch (Exception ex) {
            support.log("Error deleting simulation on server: " + filename, typeOfLogLevel.ERROR);
            support.log(ex.getLocalizedMessage(), typeOfLogLevel.ERROR);
        }
    }

    /**
     * Will delete ALL Simulations, uploaded by this client! Useful when
     * simulation runs are canceled
     */
    public void deleteAllSimulationsFromServer() {
        client = HttpFactory.getHttpClient();
        HttpPost postRequest = HttpFactory.getPostRequest(support.getReMoteAddress() + "/deleteAllSimulations");
        try {
            support.log("Try to connect " + postRequest.getURI().toString(), typeOfLogLevel.INFO);

            //Set various attributes 
            MultipartEntity multiPartEntity = new MultipartEntity();
            multiPartEntity.addPart("simid", new StringBody(this.simid));
            multiPartEntity.addPart("serversecret", new StringBody(support.getServerSecret()));
            //Set to request body
            postRequest.setEntity(multiPartEntity);
            BasicResponseHandler myTmpResponseHandler = new BasicResponseHandler();

            //Send request
            String result = client.execute(postRequest, myTmpResponseHandler);
            support.log("Response of Upload was:" + result, typeOfLogLevel.INFO);
            if (result.contains("false")) {
                throw new Exception("Deletion of all Simulations not successful. Server returned false!");
            }
            if (result.contains("true")) {
                support.log("Deletion of all Simulations successful.", typeOfLogLevel.INFO);
            }
            multiPartEntity.consumeContent();
            //EntityUtils.consume(response.getEntity());
            postRequest.releaseConnection();
            postRequest.reset();

        } catch (Exception ex) {
            support.log("Error deleting all simulations on server.", typeOfLogLevel.ERROR);
            support.log(ex.getLocalizedMessage(), typeOfLogLevel.ERROR);
        }
    }

    /**
     * Iits the simulator with all necessary varibales and starts the Thread So
     * the simulator-thread don`t need to be started external!
     *
     * @param listOfParameterSetsTMP ArrayList of parametersets (ArrayList) to
     * be simulated
     * @param log boolean value whether to write results to a separate log file
     * or not
     */
    @Override
    public void initSimulator(ArrayList< ArrayList<parameter>> listOfParameterSetsTMP, boolean log) {
        this.status = 0;
        this.listOfCompletedSimulationParsers = new ArrayList<>();

        this.listOfParameterSets = support.getCopyOfArrayListOfParametersets(listOfParameterSetsTMP);
        support.log("Given " + listOfParameterSetsTMP.size() + " parametersets to be simulated via web.", typeOfLogLevel.INFO);
        this.log = log;
        this.originalFilename = support.getOriginalFilename();//  originalFilenameTMP;
        this.tmpFilePath = support.getTmpPath();// tmpFilePathTMP;

        //Start this thread
        new Thread(this).start();
    }

    /**
     * Creates a local xml-file for SCPNs, based on given parameterset
     *
     * @param p parameterset to be used for building the local xml-simulation
     * file
     * @param simulationNumber number of acutal simulation count to be used in
     * filename
     */
    private String createLocalSimulationFile(ArrayList<parameter> p, int simulationNumber) {
        String fileNameOfLocalSimulationFile = "";
        File f = new File(this.originalFilename);
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(this.originalFilename);
            NodeList parameterList = doc.getElementsByTagName("parameter");
            String ConfidenceIntervall = "90", Seed = "0", EndTime = "0", MaxTime = "0", MaxRelError = "5";
            for (int parameterNumber = 0; parameterNumber < p.size(); parameterNumber++) {
                if (!p.get(parameterNumber).isExternalParameter()) {
                    for (int i = 0; i < parameterList.getLength(); i++) {
                        if (parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(p.get(parameterNumber).getName())) {
                            parameterList.item(i).getAttributes().getNamedItem("defaultValue").setNodeValue(p.get(parameterNumber).getStringValue());
                        }
                        //support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    }
                } else {
                    if (p.get(parameterNumber).getName().equals("MaxTime")) {
                        MaxTime = p.get(parameterNumber).getStringValue();
                    }
                    if (p.get(parameterNumber).getName().equals("EndTime")) {
                        EndTime = p.get(parameterNumber).getStringValue();
                    }
                    if (p.get(parameterNumber).getName().equals("Seed")) {
                        Seed = p.get(parameterNumber).getStringValue();
                    }
                    if (p.get(parameterNumber).getName().equals("ConfidenceIntervall")) {
                        ConfidenceIntervall = p.get(parameterNumber).getStringValue();
                    }
                    if (p.get(parameterNumber).getName().equals("MaxRelError")) {
                        MaxRelError = p.get(parameterNumber).getStringValue();
                    }

                }
            }

            //build filename
            String exportFileName = this.tmpFilePath + File.separator + support.removeExtention(f.getName()) + "_n_" + simulationNumber + "_MaxTime_" + MaxTime + "_EndTime_" + EndTime + "_Seed_" + Seed + "_ConfidenceIntervall_" + ConfidenceIntervall + "_MaxRelError_" + MaxRelError + "_.xml";

            //Export/write file to filesystem
            support.log("File to export: " + exportFileName, typeOfLogLevel.INFO);
            TransformerFactory tFactory
                    = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(exportFileName));
            transformer.transform(source, result);

            return exportFileName;

        } catch (Exception e) {
            return fileNameOfLocalSimulationFile;
        } finally {
        }

    }

    /**
     * Returns the status of simulations
     *
     * @return % of simulations that are finished
     */
    @Override
    public int getStatus() {
        return this.status;

    }

    /**
     * Returns List of completed simulation parsers.
     *
     * @return List of completed simulation parsers
     */
    @Override
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
        return this.listOfCompletedSimulationParsers;
    }

    /**
     * Execute Multipart rquest to http-server for uploading files and text data
     *
     * @param urlString complete Adress+dir for uploading file and addition data
     * (http://example.com/dir)
     * @param file File to be uploaded
     * @param fileName Filename to be used in multipart-text-field fileName
     * @param fileDescription fileDescription to be used in multipart-text-field
     * fileDescription
     * @throws java.lang.Exception
     */
    public void uploadSimulationFile(String urlString, File file, String fileName, String fileDescription) throws Exception {
        client = HttpFactory.getHttpClient();
        HttpPost postRequest = HttpFactory.getPostRequest(urlString);
        try {
            support.log("Try to connect " + urlString, typeOfLogLevel.INFO);

            //Set various attributes 
            MultipartEntity multiPartEntity = new MultipartEntity();
            multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : ""));
            multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName()));
            multiPartEntity.addPart("simid", new StringBody(this.simid));
            multiPartEntity.addPart("serversecret", new StringBody(support.getServerSecret()));

            //FileBody fileBody = new FileBody(file, "application/octect-stream") ;
            FileBody fileBody = new FileBody(file, "multipart/form-data");
            //Prepare payload
            multiPartEntity.addPart("attachment", fileBody);

            //Set to request body
            postRequest.setEntity(multiPartEntity);

            BasicResponseHandler myTmpResponseHandler = new BasicResponseHandler();

            //Send request
            //HttpResponse response = client.execute(postRequest, myTmpResponseHandler) ;
            String result = client.execute(postRequest, myTmpResponseHandler);
            //support.waitSingleThreaded(support.DEFAULT_TIMEOUT);
            support.log("Response of Upload was:" + result, typeOfLogLevel.INFO);
            if (result.contains("false")) {
                throw new Exception("Upload not successful. Server returned false!");
            }
            if (result.contains("true")) {
                support.log("Upload of file " + fileName + " successful.", typeOfLogLevel.INFO);
            }
            multiPartEntity.consumeContent();
            //EntityUtils.consume(response.getEntity());
            postRequest.releaseConnection();
            postRequest.reset();

        } catch (Exception ex) {
            support.log("Error uploading simulation file: " + fileName, typeOfLogLevel.ERROR);
            support.log(ex.getLocalizedMessage(), typeOfLogLevel.ERROR);
            throw (ex);
        }
    }

    /**
     * Returns the calculated optimimum For Benchmark-Functions this can be
     * calculated. For other simulators, this must be given by user.
     *
     * @param targetMeasure Measure to be optimized.
     * @return caluclated optimum. Not possible in Web-Simulator so returns null
     */
    @Override
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {
        support.log("No calculated optimum available in distributed simulation. Will return null.", typeOfLogLevel.ERROR);
        return null;
    }

    @Override
    public int cancelAllSimulations() {
        support.log("Will cancel all remote simulations (delete from server).", typeOfLogLevel.INFO);
        this.deleteAllSimulationsFromServer();
        return 0;
    }

}
