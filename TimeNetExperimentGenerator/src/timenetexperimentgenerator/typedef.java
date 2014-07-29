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

public enum typeOfOptimization{HillClimbing, SimAnnealing, SimpleAnnealing, ChargedSystemSearch, Genetic, Seidel3};

public enum typeOfAnnealingParameterCalculation{Standard, Random};

}
