/*
 * Factory: Returns Simulator or Optimizer

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimOptiFactory {

    public static Simulator getSimulator(){
        if(support.isCachedSimulationEnabled()&&(support.getMySimulationCache()!=null)){
        SimulatorCached tmpSimulator=new SimulatorCached();
        tmpSimulator.setMySimulationCache(support.getMySimulationCache());
        return tmpSimulator;
        }
    return new SimulatorLocal();
    }

    public static Optimizer getOptimizer(){
    return new OptimizerGreedy();
    }

}
