/*
 * Provides some typedefinitions
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package timenetexperimentgenerator;

/**
 *
 * @author Christoph Bodenstein
 */
public class typedef {

    
/**
 * Some TypeDefinitions
 */
public enum typeOfStartValueEnum{start, end, middle, random, preset};

public enum typeOfNeighborhoodEnum{StepForward,StepForwardBackward,StepForwardBackRandom,RandomStepInNeighborhood,RandomStepInDesignspace,RandomSteplessInNeighborhood};

public enum typeOfAnnealing{Boltzmann, FastAnnealing, VeryFastAnnealing};

public enum typeOfOptimization{HillClimbing, SimAnnealing, TwoPhase, ChargedSystemSearch, Genetic, ABC, MultiPhase, MVMO};

public enum typeOfSimulator{Local, Cache_Only, Cached_Local, Distributed, Benchmark};

public enum typeOfAnnealingParameterCalculation{Standard, Stepwise, Simple, SimpleStepwise};

public enum typeOfBenchmarkFunction{Sphere, Ackley, Rosenbrock, Matya, Easom, Schwefel, Rastrigin};

public enum typeOfMVMOMutationSelection{Random, RandomWithMovingSingle, MovingGroupSingleStep, MovingGroupMultiStep};

public enum typeOfMVMOParentSelection{Best, Random, Weighted}

public static final String[] listOfParametersToIgnore={"TempPara","TempCost","UsedCPUTIME"};

public enum uiState{defaultState, clientState, processRunning};
}
