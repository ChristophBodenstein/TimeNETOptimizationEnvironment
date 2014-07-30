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
//TODO, add support for typeOfSimulator !!!

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
                SimulatorCached returnSimulator = new SimulatorCached();
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
    

    public static Optimizer getOptimizer(){
        switch (support.getChosenOptimizerType()){
            case HillClimbing:
                    return new OptimizerHill();
            case SimAnnealing:
                    return new OptimizerSimAnnealing();
            case ChargedSystemSearch:
                    return new OptimizerChargedSystemSearch();
            case Genetic:
                    return new OptimizerGenetic();
            case Seidel3:
                    return new OptimizerASeidel3();
            case SimpleAnnealing:
                    return new OptimizerSimAnnealing();

            default: 
                    return new OptimizerHill();
        }
    }

    public static SimulationCache getSimulationCache(){
    return singleTonSimulationCache;
    }
}
