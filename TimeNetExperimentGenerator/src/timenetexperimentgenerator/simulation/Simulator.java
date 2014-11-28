/*
 * Interface for all Simulators

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package timenetexperimentgenerator.simulation;

import java.util.ArrayList;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.SimulationType;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Simulator {

    /**
     * Iits the simulator with all necessary varibales and starts the Thread So
     * the simulator-thread don`t need to be started external!
     *
     * @param listOfParameterSetsTMP ArrayList of parametersets (ArrayList) to
     * be simulated
     * @param simulationCounterTMP actual simualtion counter, to be increased by
     * this simulator (deprecated)
     * @param log boolean value whether to write results to a separate log file
     * or not
     */
    public void initSimulator(ArrayList< ArrayList<parameter>> listOfParameterSetsTMP, int simulationCounterTMP, boolean log);

    /**
     *
     * @return status of simulator (0..100) in % if 100 then simulation has
     * ended
     */
    public int getStatus();

    /**
     *
     * @return actual simulation counter (deprecated)
     */
    public int getSimulationCounter();

    /**
     *
     * @return List of completed simulations as parser-objects to be used for
     * optimization or export
     */
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers();

    /**
     *
     * @param targetMeasure Measure to be optimized (Measure incl. target value
     * etc.)
     * @return Simulation which is the best for given Measure and target, if it
     * can be calculated (only useful for benchmark, Cache-* simulations)
     */
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure);
}
