/*
 * Factory: Returns Simulator or Optimizer

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import timenetexperimentgenerator.optimization.*;
import timenetexperimentgenerator.simulation.*;
import timenetexperimentgenerator.optimization.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimOptiFactory {
private static SimulationCache singleTonSimulationCache=new SimulationCache();

    public static Simulator getSimulator(){    

        switch(support.getChosenSimulatorType()){
            case Local:
                //Return local simulator
                return new SimulatorLocal();
                //no break;
            case Cache_Only:
                //Return Cache-Only-Simulator
                if (support.isCachedSimulationAvailable()&&(support.getMySimulationCache()!=null)){
                SimulatorCached returnSimulator = new SimulatorCached();
                returnSimulator.setMySimulationCache(support.getMySimulationCache());
                return returnSimulator;
                }else {
                return new SimulatorLocal();
                }
                //no break;
            case Cached_Local:
                //Return Cache&Local Simulator
                SimulatorCachedLocal returnSimulator = new SimulatorCachedLocal();
                returnSimulator.setMySimulationCache(singleTonSimulationCache);
                return returnSimulator;
                //no break;
            case Distributed:
                //Return distributed simulator
                return new SimulatorWeb();
            default:
                return new SimulatorLocal();
        }
    }


    
    /**
     * Returns a new Optimizer, based on chosen Optimizertype in MainFrame (support.getChosenOptimization())
     * @return new Optimizer
     * @see typedef
     */
    public static Optimizer getOptimizer(){
        return getOptimizer(support.getChosenOptimizerType());
    }


    /**
     * Returns a new Optimizer, based on the given type
     * @param type Type of Optimizer, Enum typedef.typeOfOptimization
     * @return new Optimizer
     * @see typedef
     */
    public static Optimizer getOptimizer(typedef.typeOfOptimization type){
        switch (type){
            case HillClimbing:
                    return new OptimizerHill();
            case SimAnnealing:
                    return new OptimizerSimAnnealing();
            case ChargedSystemSearch:
                    return new OptimizerChargedSystemSearch();
            case Genetic:
                    return new OptimizerGenetic();
            case ABC:
                    return new OptimizerABC();

            case MultiPhase:
                    return new OptimizerMultiPhase();
            default:
                    return new OptimizerHill();
        }

    }


    /**
     * Returns the singleton SimulationCache. This can be used to store every Simulation the Program triggers at runtime
     * Besides this you can choose to use your own SimulationCache 
     * @return SimulationCache for all simulations
     */
    public static SimulationCache getSimulationCache(){
    return singleTonSimulationCache;
    }
}
