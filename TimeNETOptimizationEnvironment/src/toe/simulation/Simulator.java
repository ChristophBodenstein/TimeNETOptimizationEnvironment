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
import toe.helper.SimOptiCallback;

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
     * Check if optimum is calculated / coordinates in current design space are
     * calculated
     *
     * @return true if optimum within given design space is calculated
     */
    public boolean isOptimumCalculated();

    /**
     * Start calculating the optimum solution, this can take time for big design
     * spaces.
     */
    public void startCalculatingOptimum(SimOptiCallback listener);

    /**
     * Trigger to stop the calculation of optimum solution
     * TODO: Check if necessary, maybe CancelAll works
     */
    public void stopCalculatingOptimum(SimOptiCallback listener);

    /**
     * Discard the calculated optimum solution. This should be called after
     * design space has changed
     */
    public void discardCalculatedOptimum();

    /**
     * Cancel all running simulations (called on program exit)
     *
     * @return info-value about success of cancelation
     */
    public int cancelAllSimulations();

    /**
     * Returns name of local logfile, used by this simulator. The logfilename is
     * chosen at init of simulator
     *
     * @return
     */
    public String getLogfileName();
}
