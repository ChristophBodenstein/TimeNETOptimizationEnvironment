/*
 * Parser, to read information from log-files (from stationary SCPN-Simulation)
 * to use in csv files or for optimization.
 * XML-Source for Simulaiton is needed to parse log file
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package timenetexperimentgenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;

/**
 *
 * @author Christoph Bodenstein, Andy Seidel
 */
public class Parser {

    private String logName;
    private String SimulationType;
    private ArrayList<String> tmpStrings = new ArrayList();
    private int parseStatus = 0;
    private double CPUTime = 0;
    private String xmlFileName = "";
    private boolean isFromCache = true;//is true, if from cache and false if logfile is parsed
    private boolean isFromDistributedSimulation = false;//Is False, if local simulated, true if simulated via Web
    private boolean parsingSuccessfullFinished = false;

    /**
     * Constructor
     */
    public Parser() {
        this.logName = "";
        this.SimulationType = "";
        this.tmpStrings = new ArrayList();
        this.parseStatus = 0;
        this.CPUTime = 0;
        this.xmlFileName = "";
        this.isFromCache = true;
        this.isFromDistributedSimulation = false;
    }

    /**
     * Special Constructor. Copies all nested data from old parser to new one
     * (deep copy)
     *
     * @param originalParser Original parser to be copied
     */
    public Parser(Parser originalParser) {
        this.logName = originalParser.logName;
        this.SimulationType = originalParser.SimulationType;
        this.tmpStrings = originalParser.tmpStrings;
        this.parseStatus = originalParser.parseStatus;
        this.CPUTime = originalParser.CPUTime;
        this.xmlFileName = originalParser.xmlFileName;
        this.isFromCache = originalParser.isFromCache;
        this.isFromDistributedSimulation = originalParser.isFromDistributedSimulation;
    }

    /**
     * parses the log-file
     *
     * @param filename Name of logfile to parse
     * @return SimulationType incl. all information from logfile (measurement
     * values etc.)
     */
    public SimulationType parse(String filename) {
        String xmlFilename;
        ArrayList<parameter> tmpParameterList = new ArrayList<parameter>();
        String[] segs;
        SimulationType results = new SimulationType();
        parsingSuccessfullFinished = false;

        if (this.xmlFileName.equals("")) {
            support.log("Searching corresponding xml-file for: " + filename);
            String[] tmpFilenameArray = filename.split("simTime");
            xmlFilename = tmpFilenameArray[0] + ".xml";
            support.log("XML-Filename is:" + xmlFilename);
        } else {
            support.log("XML-Filename given: " + this.xmlFileName);
            xmlFilename = xmlFileName;
        }

        File xmlFile = new File(xmlFilename);
        if (!xmlFile.exists()) {
            support.log("XML-File not found, eject.");
            return null;
        }
        support.log("Parsing XML-File: " + xmlFilename);
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFilename);
            NodeList parameterList = doc.getElementsByTagName("parameter");
            support.log("***Start List of Available parameters in xml-file***");
            for (int i = 0; i < parameterList.getLength(); i++) {
                parameter tmpParameter = new parameter();

                support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                tmpParameter.setName(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                tmpParameter.setValue(support.getDouble(parameterList.item(i).getAttributes().getNamedItem("defaultValue").getNodeValue()));
                tmpParameterList.add(tmpParameter);
            }
            support.log("***End of List of Available parameters in xml-file***");
        } catch (Exception e) {
            support.log("Error while parsing xml-file " + xmlFilename);
            support.log("ErrorMsg: " + e.getLocalizedMessage());
        }

        this.logName = filename;
        File logFile = new File(this.logName);
        if (!logFile.exists()) {
            support.log("Log-File not found, eject.");
            return null;
        }

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = in.readLine()) != null) {
                tmpStrings.add(line);
            }
            in.close();
        } catch (IOException e) {
            //e.printStackTrace();
            support.log("Error while parsing log-file " + filename);
            return null;
        }

        try {
            segs = tmpStrings.get(0).split(" ");
            SimulationType = segs[2];
            segs = tmpStrings.get(1).split(" ");
            double localSimulationTime = (Float.valueOf(segs[2]));
            double localCPUTime = 0;

            //support.log("SimulationType: "+SimulationType);
            //support.log("SimulationTime: "+getSimulationTime().toString()+" seconds.");
            //Add all Parameters from logfile-name
            segs = logName.split("_");
            for (int i = 0; i < segs.length; i++) {
                if (segs[i].equals("MaxTime")) {
                    parameter tmpP = new parameter();
                    tmpP.setName("MaxTime");
                    tmpP.setValue(support.getDouble(segs[i + 1]));
                    tmpParameterList.add(tmpP);
                }
                if (segs[i].equals("EndTime")) {
                    parameter tmpP = new parameter();
                    tmpP.setName("EndTime");
                    tmpP.setValue(support.getDouble(segs[i + 1]));
                    tmpParameterList.add(tmpP);
                }
                if (segs[i].equals("Seed")) {
                    parameter tmpP = new parameter();
                    tmpP.setName("Seed");
                    tmpP.setValue(support.getDouble(segs[i + 1]));
                    tmpParameterList.add(tmpP);
                }
                if (segs[i].equals("ConfidenceIntervall")) {
                    parameter tmpP = new parameter();
                    tmpP.setName("Configured-ConfidenceIntervall");
                    tmpP.setValue(support.getDouble(segs[i + 1]));
                    tmpParameterList.add(tmpP);
                }
                if (segs[i].equals("MaxRelError")) {
                    parameter tmpP = new parameter();
                    tmpP.setName("MaxRelError");
                    tmpP.setValue(support.getDouble(segs[i + 1]));
                    tmpParameterList.add(tmpP);
                }
                if (segs[i].equals("simTime")) {
                    //CPU-Time is called simTime in logfile-Name!
                    parameter tmpP = new parameter();
                    tmpP.setName("UsedCPUTIME");
                    //String[] tmpSegs=segs[i+1].split(".");
                    tmpP.setValue(support.getDouble(segs[i + 1].substring(0, segs[i + 1].indexOf("."))));
                    tmpParameterList.add(tmpP);
                    localCPUTime = (support.getDouble(segs[i + 1].substring(0, segs[i + 1].indexOf("."))));
                }

            }
            results.setListOfParameters(tmpParameterList);

            //Begin parsing rest of file
            MeasureType tmpMeasure = new MeasureType();
            tmpMeasure.setSimulationTime(localSimulationTime);
            //tmpMeasure.setParameterList(tmpParameterList);
            tmpMeasure.setCPUTime(localCPUTime);
            String tmpConfidence;
            for (int i = 0; i < tmpStrings.size(); i++) {
                switch (parseStatus) {
                    case 0:
                        if (tmpStrings.get(i).split(" ")[0].equalsIgnoreCase("Measure:")) {
                            tmpMeasure = new MeasureType();
                            //tmpMeasure.setParameterList(tmpParameterList);
                            tmpMeasure.setCPUTime(localCPUTime);
                            tmpMeasure.setSimulationTime(localSimulationTime);
                            tmpMeasure.setMeasureName(tmpStrings.get(i).split(" ")[1]);
                            parseStatus = 1;//Measures found
                            support.log("Measures found");
                        }
                        break;
                    case 1://Measures found, name exists
                        if (tmpStrings.get(i).contains("Mean Value")) {
                            String input = tmpStrings.get(i + 2);
                            Scanner s = new Scanner(input).useDelimiter("\\s+");
                            //segs=tmpStrings.get(i+2).split( "\\s*" );
                            tmpMeasure.setMeanValue(support.getDouble(s.next()));
                            tmpMeasure.setVariance(support.getDouble(s.next()));
                            tmpConfidence = s.next();
                            tmpConfidence = tmpConfidence.replaceAll("\\[|\\]", "");
                            segs = tmpConfidence.split(Pattern.quote(";"));
                            //float[] tmpConf={ (segs[0]),Float.valueOf(segs[1])};
                            double[] tmpConf = {support.getDouble(segs[0]), support.getDouble(segs[1])};
                            tmpMeasure.setConfidenceInterval(tmpConf);
                            tmpMeasure.setEpsilon(support.getDouble(s.next()));
                            //tmpMeasure.setParameterList(tmpParameterList);
                            results.getMeasures().add(tmpMeasure);
                            parseStatus = 0;
                            support.log("Measures " + tmpMeasure.getMeasureName() + " has Epsilon of " + tmpMeasure.getEpsilon() + " and Mean of " + tmpMeasure.getMeanValue());
                        }

                        if (tmpStrings.get(i).contains("WARNING:")) {
                            if (tmpStrings.get(i + 1).contains("Has not reached predifined accuracy!")) {
                                tmpMeasure.setAccuraryReached(false);
                            }
                        }

                        break;
                    default:
                        break;
                }
            }

            this.parsingSuccessfullFinished = true;
            return results;
        } catch (Exception e) {
            support.log("Error while parsing log-file, but after reading from file-system.");
            return null;
        }
    }

    /**
     * Returns success of parsing the log file. Returns false if any error
     * occured or parsing was not yet started
     *
     * @return true if parsing was succesful. false if any error occured
     */
    public boolean isParsingSuccessfullFinished() {
        return this.parsingSuccessfullFinished;
    }

    /**
     * Returns true if logfile was returned from sim server / simulation was
     * executed distributed
     *
     * @param isFromDistributedSimulation the isFromDistributedSimulation to set
     */
    public void setIsFromDistributedSimulation(boolean isFromDistributedSimulation) {
        this.isFromDistributedSimulation = isFromDistributedSimulation;
    }

}
