/**
 * Simulator which uses log data from already done simulations and returns them.
 * If not all parametersets can be found in cache, the rest is simulated
 * via Benchmark-function
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
 */
package toe.simulation;

import toe.datamodel.MeasureType;
import toe.datamodel.SimulationType;
import toe.helper.SimOptiCallback;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimulatorCachedBenchmark extends SimulatorCachedLocal {

    /**
     * Returns new Simulator-object to be used, if parametersets are not in
     * cache
     *
     * @return Simulator object (benchmark)
     */
    @Override
    public Simulator getNoCacheSimulator() {
        return new SimulatorBenchmark();
    }

    /**
     * Returns the calculated optimimum For Benchmark-Functions this can be
     * calculated. For other simulators, this must be given by user.
     *
     * @param targetMeasure Measure to be optimized.
     * @return caluclated optimum. Not possible in Web-Simulator so returns null
     */
    @Override
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {
        return getNoCacheSimulator().getCalculatedOptimum(targetMeasure);
    }

    @Override
    public boolean isOptimumCalculated() {
        return getNoCacheSimulator().isOptimumCalculated();
    }

    @Override
    public void startCalculatingOptimum(SimOptiCallback listener) {
        getNoCacheSimulator().startCalculatingOptimum(listener);
    }

    @Override
    public void stopCalculatingOptimum(SimOptiCallback listener) {
        getNoCacheSimulator().stopCalculatingOptimum(listener);
    }

    @Override
    public void discardCalculatedOptimum(SimOptiCallback listener) {
        getNoCacheSimulator().discardCalculatedOptimum(listener);
    }
}
