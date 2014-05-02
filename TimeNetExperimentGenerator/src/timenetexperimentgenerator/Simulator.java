/*
 * Interface for all Simulators

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import java.util.ArrayList;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Simulator {
public void initSimulator(ArrayList<parameter[]> listOfParameterSetsTMP, int simulationCounterTMP);
public int getStatus();
public int getSimulationCounter();
public ArrayList<parser> getListOfCompletedSimulationParsers();
}
