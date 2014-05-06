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
        //Return distributed simulator
        return new SimulatorWeb();
        }


    return new SimulatorLocal();//Default
    }

    public static Optimizer getOptimizer(){
        switch (support.getChosenOptimizerType().intValue()){
            case 0:
                    return new OptimizerGreedy();
            case 1:
                    return new OptimizerSimAnnealing();
            case 2:
                    return new OptimizerASeidel1();
            case 3:
                    return new OptimizerASeidel2();
            case 4:
                    return new OptimizerASeidel3();

            default: 
                    return new OptimizerGreedy();
        }
    }

}
