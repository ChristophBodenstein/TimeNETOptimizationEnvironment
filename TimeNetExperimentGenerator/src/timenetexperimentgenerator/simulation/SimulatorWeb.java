/*
 * Web based simulator of SCPNs
 * build SCPNs, upload them, wait for result, download results, return results


* To be modified by: Group studies 2014


 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.parser;
import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein & ...
 */
public class SimulatorWeb implements Runnable, Simulator{
String logFileName="";

    /**
     * Constructor
     */
    public SimulatorWeb(){
    logFileName=support.getTmpPath()+File.separator+"SimLog_DistributedSimulation"+Calendar.getInstance().getTimeInMillis()+".csv";  
    }
    
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void initSimulator(ArrayList<parameter[]> listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getSimulationCounter() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ArrayList<parser> getListOfCompletedSimulationParsers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
