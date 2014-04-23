/*
 * Returns Simulator or Optimizer
 */

package timenetexperimentgenerator;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimOptiFactory {

    public static Simulator getSimulator(){
        if(support.isCachedSimulationEnabled()&&(support.getMySimulationCache()!=null)){
        LocalSimulator tmpSimulator=new LocalSimulator();
        tmpSimulator.setMySimulationCache(support.getMySimulationCache());
        return tmpSimulator;
        }
    return new simpleLocalSimulator();
    }

    public static Optimizer getOptimizer(){
    return new SimpleGreedyOptimizer();
    }

}
