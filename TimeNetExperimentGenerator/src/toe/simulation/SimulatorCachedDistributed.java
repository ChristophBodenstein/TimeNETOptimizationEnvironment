/**
 * Simulator which uses log data from already done simulations and returns them.
 * If not all parametersets can be found in cache, the rest is simulated
 * distributed
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
 */
package toe.simulation;

/**
 *
 * @author chbo1in
 */
public class SimulatorCachedDistributed extends SimulatorCachedLocal {

    /**
     * Returns new Simulator-object to be used, if parametersets are not in
     * cache
     *
     * @return Simulator object (distributed)
     */
    @Override
    public Simulator getNoCacheSimulator() {
        return new SimulatorDistributed();
    }
}
