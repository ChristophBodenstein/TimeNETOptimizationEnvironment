/*
 * Interface for all Optimizers
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package toe.optimization;

import toe.datamodel.SimulationType;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Optimizer {
public void initOptimizer();
public SimulationType getOptimum();//Returns null until Simulation has ended
public void setLogFileName(String name);
public String getLogFileName();
}
