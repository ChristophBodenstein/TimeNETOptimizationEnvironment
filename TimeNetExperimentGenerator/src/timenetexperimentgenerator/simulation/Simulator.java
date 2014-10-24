/*
 * Interface for all Simulators

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.util.ArrayList;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.SimulationType;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Simulator {
public void initSimulator(ArrayList< ArrayList<parameter> > listOfParameterSetsTMP, int simulationCounterTMP, boolean log);
public int getStatus();
public int getSimulationCounter();
public ArrayList<SimulationType> getListOfCompletedSimulationParsers();
public SimulationType getCalculatedOptimum();
}
