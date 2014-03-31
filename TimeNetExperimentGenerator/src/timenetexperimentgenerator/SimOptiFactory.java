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
    return new simpleLocalSimulator();
    }

    public static Optimizer getOptimizer(){
    return new SimpleGreedyOptimizer();
    }

}
