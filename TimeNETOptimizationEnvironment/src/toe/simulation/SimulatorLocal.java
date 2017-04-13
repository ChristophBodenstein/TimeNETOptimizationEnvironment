/*
 * Starts simulations local
 * Needs: Path to timenet, Set of Parameters, Path to original-File
 * Returns Set of measurements, corresponding to given Set of Parameters

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import toe.Parser;
import toe.datamodel.MeasureType;
import toe.datamodel.SimulationType;
import toe.datamodel.parameter;
import toe.helper.nativeProcess;
import toe.helper.nativeProcessCallbacks;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimulatorLocal extends Thread implements Simulator, nativeProcessCallbacks {

    ArrayList< ArrayList<parameter>> listOfParameterSets;
    ArrayList<SimulationType> listOfCompletedSimulationParsers;
    String originalFilename;
    String pathToTimeNet;
    String tmpFilePath;
    private int status = 0; //Status of simulations, 0..100%
    String logFileName;
    String actualSimulationLogFile = "";//actual log-file for one local simulation
    private final String nameOfTempDirectory = "14623786483530251523506521233052";
    boolean log = true;
    boolean keepSimulationFiles = false;
    long timeStamp = 0;//TimeStamp for measuring the runtime of one simulation
    Integer maxTime = 600;

    /**
     * Constructor
     */
    public SimulatorLocal() {
        logFileName = support.getTmpPath() + File.separator + "SimLog_" + getClass().getSimpleName() + "_" + Calendar.getInstance().getTimeInMillis() + ".csv";
        support.log("LogfileName:" + logFileName, typeOfLogLevel.INFO);
    }

    /**
     * inits and starts the simulator If simulationCounter is set to less then
     * 0, the old value wil be used and continouusly increased
     *
     * @param listOfParameterSetsTMP List of Parameter-sets to be simulated
     */
    @Override
    public void initSimulator(ArrayList< ArrayList<parameter>> listOfParameterSetsTMP, boolean log) {
        this.status = 0;
        this.listOfParameterSets = listOfParameterSetsTMP;
        this.log = log;
        this.originalFilename = support.getOriginalFilename();//  originalFilenameTMP;
        this.pathToTimeNet = support.getPathToTimeNet();//  pathToTimeNetTMP;
        this.tmpFilePath = support.getTmpPath();// tmpFilePathTMP;

        //Start this thread
        new Thread(this).start();

    }

    /**
     * Run Method to start simulations and collect the data simulats the SCPNs,
     * main routine
     */
    @Override
    public void run() {
        this.status = 0;
        this.listOfCompletedSimulationParsers = new ArrayList<>();
        int numberOfSimulations = 0;
        if (support.checkTimeNetPath()) {
            try {
                support.log("TimeNET-Path ok, starting local simulations.", typeOfLogLevel.INFO);

                support.log("Logfilename is:" + logFileName, typeOfLogLevel.INFO);

                int simulationAttemptCounter;

                if (listOfParameterSets.size() > 0) {
                    support.log("Supposed to simulate " + listOfParameterSets.size() + " parametersets.", typeOfLogLevel.INFO);
                    for (int i = 0; i < listOfParameterSets.size(); i++) {

                        support.setStatusText("Waiting to start sim " + (i + 1) + "/" + listOfParameterSets.size());
                        //Wait for some Time. Maybe this is needed on some Systems
                        support.waitSingleThreaded(support.DEFAULT_TIME_BETWEEN_LOCAL_SIMULATIONS);//Wait to be (quite)sure system ressources are free

                        //Try every simulation several times if TimeNet crashs
                        //Reset Simulation-Attempt-Counter for next Parameterset
                        simulationAttemptCounter = support.DEFAULT_LOCAL_SIMULATION_ATTEMPTS;

                        while (simulationAttemptCounter > 0) {

                            support.setStatusText("Sim " + (i + 1) + "/" + listOfParameterSets.size() + " attempt " + (support.DEFAULT_LOCAL_SIMULATION_ATTEMPTS - simulationAttemptCounter + 1));

                            if (support.isCancelEverything()) {
                                support.log("Local Simulation canceld by user.", typeOfLogLevel.INFO);
                                support.setStatusText("Operations canceled.");
                                return;
                            }
                            ArrayList<parameter> actualParameterSet = listOfParameterSets.get(i);//get actual parameterset
                            String actualParameterFileName = createLocalSimulationFile(actualParameterSet, support.getGlobalSimulationCounter());//create actual SCPN xml-file and save it in tmp-folder
                            support.log("Simulating file:" + actualParameterFileName, typeOfLogLevel.INFO);
                            startLocalSimulation(actualParameterFileName);//Returns, when Simulation has ended
                            //SimulationType myResults=new SimulationType();//create new SimulationResults
                            //here the SimType has to get Data From Parser;
                            Parser myParser = new Parser();
                            SimulationType myResults = myParser.parse(actualSimulationLogFile);//parse Log-file and xml-file

                            if (myParser.isParsingSuccessfullFinished()) {
                                support.log("Parsing successful.", typeOfLogLevel.INFO);
                                //listOfCompletedSimulationParsers.add(myResults);
                                if (this.log) {
                                    support.addLinesToLogFile(myResults, logFileName);
                                }

                                this.listOfCompletedSimulationParsers.add(myResults);//add parser to local list of completed simulations

                                if (!keepSimulationFiles) {
                                    support.log("Will delete XML-File and log-File.", typeOfLogLevel.INFO);
                                    support.del(new File(actualParameterFileName));
                                    support.del(new File(actualSimulationLogFile));
                                }
                                simulationAttemptCounter = 0;
                            } else {
                                simulationAttemptCounter--;
                                support.log("Error Parsing the Simulation results. Maybe Simulation failure?", typeOfLogLevel.ERROR);
                                if (simulationAttemptCounter == 0) {
                                    support.log("Will end local simulation because of " + support.DEFAULT_LOCAL_SIMULATION_ATTEMPTS + " failed simulation attempts", typeOfLogLevel.INFO);
                                }
                            }
                        }//End of whileloop for several simulatio attempts

                        numberOfSimulations++;//increment local simulation counter
                        support.incGlobalSimulationCounter();

                        this.status = numberOfSimulations * 100 / listOfParameterSets.size(); //update status of local simulations (in %)
                        support.incGlobalSimulationCounter();

                    }

                }

            } catch (Exception e) {
                support.log("Error while creating local simulation file or log-file.", typeOfLogLevel.ERROR);
            }

        } else {
            support.log("Timenet-Path NOT ok!", typeOfLogLevel.INFO);
        }
        support.setStatusText("Local simulation finished.");
        synchronized (this) {
            notify();
        }
        //Simple ending Callback to reactivate uer-interface
        support.simOptiOperationSuccessfull("The End");
    }

    /**
     * Creates the local file for simulation, including all information in file
     * and filename
     *
     * @param p parameterset to be simulated
     * @param simulationNumber number of simulation
     * @return Name of simulation file incl. path
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
            for (parameter p1 : p) {
                if (!p1.isExternalParameter()) {
                    for (int i = 0; i < parameterList.getLength(); i++) {
                        if (parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(p1.getName())) {
                            parameterList.item(i).getAttributes().getNamedItem("defaultValue").setNodeValue(p1.getStringValue());
                        }
                        //support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    }
                } else {
                    if (p1.getName().equals("MaxTime")) {
                        MaxTime = p1.getStringValue();
                    }
                    if (p1.getName().equals("EndTime")) {
                        EndTime = p1.getStringValue();
                    }
                    if (p1.getName().equals("Seed")) {
                        Seed = p1.getStringValue();
                    }
                    if (p1.getName().equals("ConfidenceIntervall")) {
                        ConfidenceIntervall = p1.getStringValue();
                    }
                    if (p1.getName().equals("MaxRelError")) {
                        MaxRelError = p1.getStringValue();
                    }
                }
            }

            //Dateiname bilden
            String exportFileName = this.tmpFilePath + File.separator + support.removeExtention(f.getName()) + "_n_" + simulationNumber + "_MaxTime_" + MaxTime + "_EndTime_" + EndTime + "_Seed_" + Seed + "_ConfidenceIntervall_" + ConfidenceIntervall + "_MaxRelError_" + MaxRelError + "_.xml";
            //Exportieren
            support.log("File to export: " + exportFileName, typeOfLogLevel.INFO);

            TransformerFactory tFactory
                    = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(exportFileName));
            transformer.transform(source, result);

            return exportFileName;

        } catch (Exception e) {
            support.log("Error creating local simulation file. Msg: " + e.getLocalizedMessage(), typeOfLogLevel.ERROR);
            return fileNameOfLocalSimulationFile;
        } finally {
        }

    }

    /**
     * starts the local simulation run, returns, when simulation has ended
     *
     * @param exportFileName Filename of SCPN to be simulated with TimeNet
     */
    private void startLocalSimulation(String exportFileName) {
        try {
            File file = new File(exportFileName);
            String tmpPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator)) + File.separator + nameOfTempDirectory;

            if (support.isDeleteTmpSimulationFiles()) {
                //Clean up before simulation, in case an old sim used the same dir
                support.log("Delete Path to tmp files: " + tmpPath, typeOfLogLevel.INFO);
                support.del(new File(tmpPath));
            }
            // Execute command
            maxTime = new Integer(support.getIntStringValueFromFileName(exportFileName, "MaxTime"));
            java.lang.ProcessBuilder processBuilder = new java.lang.ProcessBuilder("java", "-jar", "TimeNET.jar", exportFileName, "autostart=true", "autostop=true", "secmax=" + maxTime.toString(), "endtime=" + support.getIntStringValueFromFileName(exportFileName, "EndTime"), "seed=" + support.getIntStringValueFromFileName(exportFileName, "Seed"), "confidence=" + support.getIntStringValueFromFileName(exportFileName, "ConfidenceIntervall"), "epsmax=" + support.getValueFromFileName(exportFileName, "MaxRelError"));
            processBuilder.directory(new java.io.File(this.pathToTimeNet));
            support.log("Command is: " + processBuilder.command().toString(), typeOfLogLevel.INFO);

            // Start new process
            timeStamp = Calendar.getInstance().getTimeInMillis();
            nativeProcess myNativeProcess = new nativeProcess(processBuilder, this);
            synchronized (myNativeProcess) {
                try {
                    myNativeProcess.wait(maxTime * 1000);
                } catch (Exception e) {
                    support.log("Simulation may took longer than given MaxTime! Will be aborted! " + " MaxTime was:" + maxTime.toString(), typeOfLogLevel.ERROR);
                    myNativeProcess.setKillProcess(true);
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
                support.log("Deleted original Log-file and directory.", typeOfLogLevel.INFO);
                this.actualSimulationLogFile = sinkFile;
            }

            if (support.isDeleteTmpSimulationFiles()) {
                //Clean up after simulation
                support.log("Delete Path to tmp files: " + tmpPath, typeOfLogLevel.INFO);
                support.del(new File(tmpPath));
            }
        } catch (Exception e) {
            support.log("Problem simulating local.", typeOfLogLevel.ERROR);
        }
    }

    /**
     * Returns List of completed simulation parsers.
     *
     * @return List of completed simulation parsers
     */
    @Override
    public ArrayList<SimulationType> getListOfCompletedSimulations() {
        return this.listOfCompletedSimulationParsers;
    }

    /**
     * Delete content of tmp-folder, all xml-files, log-files and generated
     * source-code
     */
    public void deleteTmpFiles() {
        File[] listOfFile = new File(this.tmpFilePath).listFiles();
        for (File listOfFile1 : listOfFile) {
            support.del(listOfFile1);
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

    @Override
    public void processEnded() {
        support.log("Local Simulation ended.", typeOfLogLevel.INFO);
    }

    @Override
    public void errorOccured(String message) {
        support.log("Error while local simulation.", typeOfLogLevel.INFO);
    }

    /**
     * Returns the calulated optimimum For Benchmark-Functions this can be
     * caluclated. For other simulators, this must be given by user.
     */
    @Override
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {
        support.log("No calculated optimum available in local simulation. Will return null.", typeOfLogLevel.INFO);
        return null;
    }

    @Override
    public int cancelAllSimulations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String getLogfileName() {
        return this.logFileName;
    }
}
