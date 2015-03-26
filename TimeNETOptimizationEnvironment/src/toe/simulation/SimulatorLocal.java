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

/**
 *
 * @author Christoph Bodenstein
 */
public class SimulatorLocal implements Runnable, Simulator, nativeProcessCallbacks {

    ArrayList< ArrayList<parameter>> listOfParameterSets;
    ArrayList<SimulationType> listOfCompletedSimulationParsers;
    String originalFilename;
    String pathToTimeNet;
    String tmpFilePath;
    private int status = 0; //Status of simulations, 0..100%
    private int simulationCounter = 0;//Startvalue for count of simulations, will be in the filename of sim and log
    String logFileName;
    String actualSimulationLogFile = "";//actual log-file for one local simulation
    private final String nameOfTempDirectory = "14623786483530251523506521233052";
    boolean log = true;
    boolean keepSimulationFiles = false;
    long timeStamp = 0;//TimeStamp for measuring the runtime of one simulation

    /**
     * Constructor
     */
    public SimulatorLocal() {
        logFileName = support.getTmpPath() + File.separator + "SimLog_LocalSimulation_without_Cache" + Calendar.getInstance().getTimeInMillis() + ".csv";
    }

    /**
     * inits and starts the simulator If simulationCounter is set to less then
     * 0, the old value wil be used and continouusly increased
     *
     * @param listOfParameterSetsTMP List of Parameter-sets to be simulated
     * @param simulationCounterTMP start value of simulation counter
     */
    public void initSimulator(ArrayList< ArrayList<parameter>> listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
        this.status = 0;
        this.listOfParameterSets = listOfParameterSetsTMP;
        this.log = log;
        this.originalFilename = support.getOriginalFilename();//  originalFilenameTMP;
        this.pathToTimeNet = support.getPathToTimeNet();//  pathToTimeNetTMP;
        this.tmpFilePath = support.getTmpPath();// tmpFilePathTMP;
        if (simulationCounterTMP >= 0) {
            this.simulationCounter = simulationCounterTMP;
        }

        //Start this thread
        new Thread(this).start();

    }

    /**
     * Run Method to start simulations and collect the data simulats the SCPNs,
     * main routine
     */
    public void run() {
        this.status = 0;
        this.listOfCompletedSimulationParsers = new ArrayList<SimulationType>();
        int numberOfSimulations = 0;
        if (support.checkTimeNetPath()) {
            try {
                support.log("Timenet-Path ok, starting local simulations.");

                support.log("Logfilename is:" + logFileName);

                int simulationAttemptCounter;

                if (listOfParameterSets.size() > 0) {
                    support.log("Supposed to simulate " + listOfParameterSets.size() + " parametersets.");
                    for (int i = 0; i < listOfParameterSets.size(); i++) {

                        support.setStatusText("wating to start sim " + (i + 1) + "/" + listOfParameterSets.size());
                        //Wait for some Time. Maybe this is needed on some Systems
                        Thread.sleep(support.DEFAULT_TIME_BETWEEN_LOCAL_SIMULATIONS);

                        //Try every simulation several times if TimeNet crashs
                        //Reset Simulation-Attempt-Counter for next Parameterset
                        simulationAttemptCounter = support.DEFAULT_LOCAL_SIMULATION_ATTEMPTS;

                        while (simulationAttemptCounter > 0) {

                            support.setStatusText("Sim " + (i + 1) + "/" + listOfParameterSets.size() + " attempt " + (support.DEFAULT_LOCAL_SIMULATION_ATTEMPTS - simulationAttemptCounter + 1));

                            if (support.isCancelEverything()) {
                                support.log("Local Simulation canceld by user.");
                                support.setStatusText("Operations canceled.");
                                return;
                            }
                            ArrayList<parameter> actualParameterSet = listOfParameterSets.get(i);//get actual parameterset
                            String actualParameterFileName = createLocalSimulationFile(actualParameterSet, support.getGlobalSimulationCounter());//create actual SCPN xml-file and save it in tmp-folder
                            support.log("Simulating file:" + actualParameterFileName);
                            startLocalSimulation(actualParameterFileName);//Returns, when Simulation has ended
                            //SimulationType myResults=new SimulationType();//create new SimulationResults
                            //here the SimType has to get Data From Parser;
                            Parser myParser = new Parser();
                            SimulationType myResults = myParser.parse(actualSimulationLogFile);//parse Log-file and xml-file

                            if (myParser.isParsingSuccessfullFinished()) {
                                support.log("Parsing successful.");
                                //listOfCompletedSimulationParsers.add(myResults);
                                if (this.log) {
                                    support.addLinesToLogFile(myResults, logFileName);
                                }

                                this.listOfCompletedSimulationParsers.add(myResults);//add parser to local list of completed simulations

                                if (!keepSimulationFiles) {
                                    support.log("Will delete XML-File and log-File.");
                                    support.del(new File(actualParameterFileName));
                                    support.del(new File(actualSimulationLogFile));
                                }
                                simulationAttemptCounter = 0;
                            } else {
                                simulationAttemptCounter--;
                                support.log("Error Parsing the Simulation results. Maybe Simulation failure?");
                                if (simulationAttemptCounter == 0) {
                                    support.log("Will end local simulation because of " + support.DEFAULT_LOCAL_SIMULATION_ATTEMPTS + " failed simulation attempts");
                                }
                            }
                        }//End of whileloop for several simulatio attempts

                        numberOfSimulations++;//increment local simulation counter
                        support.incGlobalSimulationCounter();

                        this.status = numberOfSimulations * 100 / listOfParameterSets.size(); //update status of local simulations (in %)
                        this.simulationCounter++;//increment given global simulation counter, TODO : remove this

                    }

                }

            } catch (Exception e) {
                support.log("Error while creating local simulation file or log-file.");
            }

        } else {
            support.log("Timenet-Path NOT ok!");
        }
        support.setStatusText("Local simulation finished.");
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
            support.log("File to export: " + exportFileName);

            TransformerFactory tFactory
                    = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(exportFileName));
            transformer.transform(source, result);

            return exportFileName;

        } catch (Exception e) {
            support.log("Error creating local simulation file. Msg: " + e.getLocalizedMessage());
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
                support.log("Delete Path to tmp files: " + tmpPath);
                support.del(new File(tmpPath));
            }
            // Execute command
            java.lang.ProcessBuilder processBuilder = new java.lang.ProcessBuilder("java", "-jar", "TimeNET.jar", exportFileName, "autostart=true", "autostop=true", "secmax=" + support.getIntStringValueFromFileName(exportFileName, "MaxTime"), "endtime=" + support.getIntStringValueFromFileName(exportFileName, "EndTime"), "seed=" + support.getIntStringValueFromFileName(exportFileName, "Seed"), "confidence=" + support.getIntStringValueFromFileName(exportFileName, "ConfidenceIntervall"), "epsmax=" + support.getValueFromFileName(exportFileName, "MaxRelError"));
            processBuilder.directory(new java.io.File(this.pathToTimeNet));
            support.log("Command is: " + processBuilder.command().toString());

            // Start new process
            timeStamp = Calendar.getInstance().getTimeInMillis();

            nativeProcess myNativeProcess = new nativeProcess(processBuilder, this);

            //We wait until the process is ended or aborted
            while (myNativeProcess.isRunning()) {
                Thread.sleep(1000);
            }

            timeStamp = (Calendar.getInstance().getTimeInMillis() - timeStamp) / 1000;//Time for calculation in seconds

            //Copy results.log
            String sourceFile = support.removeExtention(exportFileName) + ".result" + File.separator + "results.log";
            String sinkFile = support.removeExtention(exportFileName) + "simTime_" + timeStamp + ".log";
            if (support.copyFile(sourceFile, sinkFile, false)) {
                support.log("Coppied log-file. Now delete the directory and original log-file.");
                File tmpFile = new File(sourceFile);
                tmpFile.delete();
                tmpFile = new File(support.removeExtention(exportFileName) + ".result");
                tmpFile.delete();
                support.log("Deleted original Log-file and directory.");
                this.actualSimulationLogFile = sinkFile;
            }

            if (support.isDeleteTmpSimulationFiles()) {
                //Clean up after simulation
                support.log("Delete Path to tmp files: " + tmpPath);
                support.del(new File(tmpPath));
            }
        } catch (Exception e) {
            support.log("Problem simulating local.");
        }
    }

    /**
     * Returns List of completed simulation parsers.
     *
     * @return List of completed simulation parsers
     */
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
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
     * Returns actual number of simulation
     *
     * @return number of actual simulation
     */
    public int getSimulationCounter() {
        return this.simulationCounter;
    }

    /**
     * sets the number of simulation to be started with
     *
     * @param i number of simulation for simulation counter
     */
    public void setSimulationCounter(int i) {
        this.simulationCounter = i;
    }

    /**
     * Returns the status of simulations
     *
     * @return % of simulations that are finished
     */
    public int getStatus() {
        return this.status;
    }

    public void processEnded() {
        support.log("Local Simulation ended.");
    }

    public void errorOccured(String message) {
        support.log("Error while local simulation.");
    }

    /**
     * Returns the calulated optimimum For Benchmark-Functions this can be
     * caluclated. For other simulators, this must be given by user.
     */
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {
        support.log("SimulatorLocal: Getting absolute optimum simulation from Cache. Will return null.");
        return null;
    }

    @Override
    public int cancelAllSimulations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
