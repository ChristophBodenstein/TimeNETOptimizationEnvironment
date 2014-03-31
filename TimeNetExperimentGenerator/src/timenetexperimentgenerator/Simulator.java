/*
 * Interface for all Simulators
 */

package timenetexperimentgenerator;

import java.util.ArrayList;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Simulator {
public void initSimulator(ArrayList<parameter[]> listOfParameterSetsTMP, String originalFilenameTMP, String pathToTimeNetTMP, String tmpFilePathTMP,boolean remoteTMP, int simulationCounterTMP);
public int getStatus();
public int getSimulationCounter();
public ArrayList<parser> getListOfCompletedSimulationParsers();
}
