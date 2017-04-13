/*
 * Interface for all Simulators

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.util.ArrayList;
import toe.datamodel.MeasureType;
import toe.datamodel.parameter;
import toe.datamodel.SimulationType;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Simulator {

    /**
     * Inits the simulator with all necessary varibales and starts the Thread So
     * the simulator-thread don`t need to be started external!
     *
     * @param listOfParameterSetsTMP ArrayList of parametersets (ArrayList) to
     * be simulated
     * @param log boolean value whether to write results to a separate log file
     * or not
     */
    public void initSimulator(ArrayList< ArrayList<parameter>> listOfParameterSetsTMP, boolean log);

    /**
     *
     * @return status of simulator (0..100) in % if 100 then simulation has
     * ended
     */
    public int getStatus();


    /**
     *
     * @return List of completed simulations as parser-objects to be used for
     * optimization or export
     */
    public ArrayList<SimulationType> getListOfCompletedSimulations();

    /**
     *
     * @param targetMeasure Measure to be optimized (Measure incl. target value
     * etc.)
     * @return Simulation which is the best for given Measure and target, if it
     * can be calculated (only useful for benchmark, Cache-* simulations)
     */
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure);
    
    /**
     * Cancel all running simulations (called on program exit)
     * @return info-value about success of cancelation
     */
    public int cancelAllSimulations();
    
    /**
     * Returns name of local logfile, used by this simulator. The logfilename is chosen at init of simulator
     * @return 
     */
    public String getLogfileName();
}
