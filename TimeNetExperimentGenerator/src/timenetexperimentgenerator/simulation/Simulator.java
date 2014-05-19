/*
 * Interface for all Simulators

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import java.util.ArrayList;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.parser;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Simulator {
public void initSimulator(ArrayList<parameter[]> listOfParameterSetsTMP, int simulationCounterTMP, boolean log);
public int getStatus();
public int getSimulationCounter();
public ArrayList<parser> getListOfCompletedSimulationParsers();
}
