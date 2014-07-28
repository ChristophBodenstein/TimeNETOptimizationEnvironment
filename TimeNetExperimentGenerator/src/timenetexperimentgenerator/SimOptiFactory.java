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

        if(support.getChosenSimulatorType().equals(new Integer(0))){
        //Return local simulator
        return new SimulatorLocal();
        }


        if(support.getChosenSimulatorType().equals(new Integer(1))&&support.isCachedSimulationAvailable()&&(support.getMySimulationCache()!=null)){
        //Return Cached simulator
        SimulatorCached tmpSimulator=new SimulatorCached();
        tmpSimulator.setMySimulationCache(support.getMySimulationCache());
        return tmpSimulator;
        }

        
        if(support.getChosenSimulatorType().equals(new Integer(2))){
        //Return Cached/Local simulator, All new Parsers will be stored in local cache
        SimulatorCachedLocal mySimulator=new SimulatorCachedLocal();
        mySimulator.setMySimulationCache(singleTonSimulationCache);
        return mySimulator;
        }
        
        if(support.getChosenSimulatorType().equals(new Integer(3))){
        //Return distributed simulator
        return new SimulatorWeb();
        }


    return new SimulatorLocal();//Default
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
