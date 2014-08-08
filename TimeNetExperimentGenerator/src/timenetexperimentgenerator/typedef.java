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
public enum typeOfStartValueEnum{start, end, middle, random};

public enum typeOfNeighborhoodEnum{StepForward,StepForwardBackward,StepForwardBackRandom,RandomStepInNeighborhood,RandomStepInDesignspace,RandomSteplessInNeighborhood};

public enum typeOfAnnealing{Boltzmann, FastAnnealing, VeryFastAnnealing};

public enum typeOfOptimization{HillClimbing, SimAnnealing, TwoPhase, ChargedSystemSearch, Genetic, ABC, MultiPhase};

public enum typeOfSimulator{Local, Cache_Only, Cached_Local, Distributed};

public enum typeOfAnnealingParameterCalculation{Standard, Stepwise, Simple, SimpleStepwise};

public static final String[] listOfParametersToIgnore={"TempPara","TempCost"};

}
