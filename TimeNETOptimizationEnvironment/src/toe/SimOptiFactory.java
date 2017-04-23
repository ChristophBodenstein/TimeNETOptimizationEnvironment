/*
 * Factory: Returns Simulator or Optimizer

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe;

import toe.simulation.SimulatorCachedLocal;
import toe.simulation.SimulatorCachedDistributed;
import toe.simulation.SimulatorLocal;
import toe.simulation.SimulatorDistributed;
import toe.simulation.Simulator;
import toe.simulation.SimulatorBenchmark;
import toe.simulation.SimulatorCached;
import toe.optimization.OptimizerABC;
import toe.optimization.OptimizerGenetic;
import toe.optimization.OptimizerMultiPhase;
import toe.optimization.Optimizer;
import toe.optimization.OptimizerChargedSystemSearch;
import toe.optimization.OptimizerMVMO;
import toe.optimization.OptimizerSimAnnealing;
import toe.optimization.OptimizerHill;
import java.util.ArrayList;
import toe.optimization.OptimizerTwoPhase;
import toe.simulation.SimulatorCachedBenchmark;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimOptiFactory {
//private static SimulationCache singleTonSimulationCache=new SimulationCache();

    private static ArrayList<Simulator> listOfCreatedSimulators = new ArrayList<Simulator>();//Lis of Simulators to end on program exit. First we use only remote simulators

    public static Simulator getSimulator() {

        switch (support.getChosenSimulatorType()) {
            case Local:
                //Return local simulator
                return new SimulatorLocal();
            //no break;
            case Cache_Only:
                //Return Cache-Only-Simulator
                if (support.isCachedSimulationAvailable() && (support.getMySimulationCache() != null)) {
                    SimulatorCached returnSimulator = new SimulatorCached();
                    //returnSimulator.setMySimulationCache(support.getMySimulationCache());
                    return returnSimulator;
                } else {
                    return new SimulatorLocal();
                }
            //no break;
            case Cached_Local:
                //Return Cache&Local Simulator
                SimulatorCachedLocal returnSimulator = new SimulatorCachedLocal();
                //returnSimulator.setMySimulationCache(support.getMySimulationCache());
                return returnSimulator;
            //no break;
            case Distributed:
                //Return distributed simulator
                Simulator resultSim = new SimulatorDistributed();
                listOfCreatedSimulators.add(resultSim);
                return resultSim;
            case Cached_Distributed:
                //Return Cache&Distributed Simulator
                SimulatorCachedDistributed returnSimulatorDistributed = new SimulatorCachedDistributed();
                //returnSimulatorDistributed.setMySimulationCache(support.getMySimulationCache());
                listOfCreatedSimulators.add(returnSimulatorDistributed);
                return returnSimulatorDistributed;

            case Benchmark:
                //Return Simulator for Benchmark-Functions
                return new SimulatorBenchmark();
            case Cached_Benchmark:
                //Return the Benchmark-Simulator with cache-support
                SimulatorCachedBenchmark returnSimulatorCachedBenchmark = new SimulatorCachedBenchmark();
                //returnSimulatorCachedBenchmark.setMySimulationCache(support.getMySimulationCache());
                return returnSimulatorCachedBenchmark;
            default:
                return new SimulatorLocal();
        }
    }

    /**
     * End all remote Simulations if possible
     */
    public static int endAllRemoteSimulations() {
        for (Simulator CreatedSimulator : listOfCreatedSimulators) {
            if (CreatedSimulator != null) {
                CreatedSimulator.cancelAllSimulations();
            }
        }
        return 0;
    }

    /**
     * Returns a new Optimizer, based on chosen Optimizertype in MainFrame
     * (support.getChosenOptimization())
     *
     * @return new Optimizer
     * @see typedef
     */
    public static Optimizer getOptimizer() {
        return getOptimizer(support.getChosenOptimizerType());
    }

    /**
     * Returns a new Optimizer, based on the given type
     *
     * @param type Type of Optimizer, Enum typedef.typeOfOptimization
     * @return new Optimizer
     * @see typedef
     */
    public static Optimizer getOptimizer(typedef.typeOfOptimization type) {
        switch (type) {
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
            case MVMO:
                return new OptimizerMVMO();
            case TwoPhase:
                return new OptimizerTwoPhase();
            case MultiPhase:
                return new OptimizerMultiPhase();
            default:
                return new OptimizerHill();
        }

    }

    /**
     * Returns the singleton SimulationCache. This can be used to store every
     * Simulation the Program triggers at runtime Besides this you can choose to
     * use your own SimulationCache
     *
     * @return SimulationCache for all simulations
     */
    //public static SimulationCache getSimulationCache(){
    //return singleTonSimulationCache;
    //}
}
