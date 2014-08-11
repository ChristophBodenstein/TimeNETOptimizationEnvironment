/*
 * Interface for all Optimizers
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.optimization;

import timenetexperimentgenerator.datamodel.SimulationType;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Optimizer {
public void initOptimizer();
public SimulationType getOptimum();//Returns null until Simulation has ended
}
