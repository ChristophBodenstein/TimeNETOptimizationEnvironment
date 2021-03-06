/*
 * Provides some typedefinitions
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe;

/**
 *
 * @author Christoph Bodenstein
 */
public class typedef {

    /**
     * Defines how to calculate the start value for a parameter druing
     * optimization
     */
    public enum typeOfStartValueEnum {

        /**
         * parameter is set to its start-value (from table)
         */
        start,
        /**
         * parameter is set to its end-value (from table)
         */
        end,
        /**
         * parameter is set to its middle-value (from table, abs((end-start)/2))
         */
        middle,
        /**
         * parameter is set to a random value between start and end value
         */
        random,
        /**
         * parameter is set to a given value (actual parameter value is not
         * changed)
         */
        preset
    };

    /**
     * Enum to set/ describe the kind of calculating the next paramater value
     * during optimization Most important to hill climbing and simulated
     * annealing
     */
    public enum typeOfNeighborhoodEnum {

        /**
         * Increase value of chosen parameter by stepsize (depending on
         * resolution / discretization)
         */
        StepForward,
        /**
         * Increase or decrease calue of chosen parameter by stepsize. Important
         * for Hill-Climbing, ignored else After #number of wrong solutions in
         * one direction parameter value is set to last good value and then
         * dircetion changes (inc/dec) (depending on resolution /
         * discretization)
         */
        StepForwardBackward,
        /**
         * Increase or decrease parametervalue by random (depending on
         * resolution / discretization)
         */
        StepForwardBackRandom,
        /**
         * Chose next parametervalue by random within the given neighborhood
         * size parametervalue are set to the next-best value matching the
         * discretization / resolution (depending on resolution /
         * discretization)
         */
        RandomStepInNeighborhood,
        /**
         * Chose next parametervalue by random within the whole design space!
         * parametervalue are set to the next-best value matching the
         * discretization / resolution (depending on resolution /
         * discretization) Maybe obsolete because neighborhood size could be set
         * to 100%
         */
        RandomStepInDesignspace,
        /**
         * Chose next parametervalue by random within the given neighborhood
         * size (NOT depending on resolution / discretization !)
         */
        RandomSteplessInNeighborhood
    };

    /**
     * Type of Annelaing in sim annealing optimization (cooling function) Mostly
     * taken from Book "Stochastic Discrete Event Systems" (A. Zimmermann, Page
     * 227)
     */
    public enum typeOfAnnealing {

        /**
         * Boltzmann-annealing
         */
        Boltzmann,
        /**
         * Fast Annelaing
         */
        FastAnnealing,
        /**
         * Very fast annealing
         */
        VeryFastAnnealing
    };

    /**
     * Tyoe of target for optimization (minimization maximization, target value)
     */
    public enum typeOfTarget {

        /**
         * try to find the minimal value of measure
         */
        min,
        /**
         * try to find the maximum value of measure
         */
        max,
        /**
         * try to find a measure value that is near to target value
         */
        value
    };

    /**
     * Type of optimization to use
     */
    public enum typeOfOptimization {

        /**
         * Standard hill climbing algorithm with abort after # wrong solutions
         * (given in Opti-Preferences)
         */
        HillClimbing,
        /**
         * Simulated Annealing, as given in Book "Stochastic Discrete Event
         * Systems" (A. Zimmermann, Page 227, ff.)
         */
        SimAnnealing,
        /**
         * Standard Two-Phase Alogithm. Use different configurations of sim
         * annealing
         */
        TwoPhase,
        /**
         * Charged System Search (Andy Seidel)
         */
        ChargedSystemSearch,
        /**
         * Genetic Algorithm (Andy Seidel)
         */
        Genetic,
        /**
         * ABC-Algorithm (Andy Seidel)
         */
        ABC,
        /**
         * MVMO-Algorithm (Andy Seidel)
         */
        MVMO,
        /**
         * Multiphase Algorithm. Use the same algo in every phase, change
         * accuracy parameter
         */
        MultiPhase
    };

    /**
     * Type of Simulator to be used in combination with optimization or batch
     * simulation
     */
    public enum typeOfSimulator {

        /**
         * Simulate the sCPN on local TimeNET installation
         */
        Local,
        /**
         * Take values from loaded cahce-file. If parametr set is not found,
         * take the next matching parameter set
         */
        Cache_Only,
        /**
         * Take values from loaded cahce-file. If parametr set is not found,
         * simulate SCPN on local TimeNET installation
         */
        Cached_Local,
        /**
         * Simulate SCPN distributed, upload it to simulation server and wait
         * for results
         */
        Distributed,
        /**
         * Take values from loaded cahce-file. If parametr set is not found,
         * simulate SCPN with distributed simulator. If distributed simulation
         * is not possible, use local TimeNET installation
         */
        Cached_Distributed,
        /**
         * Use chosen benchmark function to "simulate" parameter set
         */
        Benchmark,
        /**
         * To test caching effects with benchmar functions
         */
        Cached_Benchmark

    };

    /**
     * Type of calculating next parameter set in simulated annealing
     */
    public enum typeOfAnnealingParameterCalculation {

        /**
         * Standard-Version as shown in Book "Stochastic Discrete Event Systems"
         * (A. Zimmermann, Page 227)
         */
        Standard,
        /**
         * Modified Standard-Version as shown in Book "Stochastic Discrete Event
         * Systems" (A. Zimmermann, Page 227) Calculate parameter as standard,
         * than take the next matching parameter set accoring to resolution /
         * discretization
         *
         */
        Stepwise,
        /**
         * Calulcate the parameterset simple depending on actual temperaturs and
         * design space
         */
        Simple,
        /**
         * Calulcate the parameterset simple depending on actual temperaturs and
         * design space Than take the next matching parameter set accoring to
         * resolution / discretization
         */
        SimpleStepwise
    };

    /**
     * Type of Benchmark functions to "simulate" parameter sets Some are only
     * define for TWO iteratable parameter
     */
    public enum typeOfBenchmarkFunction {

        /**
         * Sphere function
         */
        Sphere,
        /**
         * Ackley function
         */
        Ackley,
        /**
         * Matyas function
         */
        Matya,
        /**
         * Schwefel function
         */
        Schwefel,
        //        /**
        //         * Rastrigins function
        //         */
        //        Rastrigin
    };

    /**
     * Type of Mutation selection for MVMO Algorithm (Andy Seidel)
     */
    public enum typeOfMVMOMutationSelection {

        /**
         *
         */
        Random,
        /**
         *
         */
        RandomWithMovingSingle,
        /**
         *
         */
        MovingGroupSingleStep,
        /**
         *
         */
        MovingGroupMultiStep
    };

    /**
     * Type of parent selection for MVMO-Algorithm (Andy Seidel)
     */
    public enum typeOfMVMOParentSelection {

        /**
         *
         */
        Best,
        /**
         *
         */
        Random,
        /**
         *
         */
        Weighted
    };

    /**
     * Tpye of crossover-operation for genetic-algorithm (Andy Seidel)
     */
    public enum typeOfGeneticCrossover {

        /**
         *
         */
        OnePoint,
        /**
         *
         */
        SBX,
        /**
         *
         */
        MPC

    };

    /**
     * List of parameters to be ignored for some reasons when reading it in
     * cache or searching for paremetrsets in cache
     */
    public static final String[] listOfParametersToIgnore = {"TempPara", "TempCost", "UsedCPUTIME"};

    /**
     * UI-States to set during runtime
     */
    public enum uiState {

        /**
         * opti-Buttons and other are deactivated until net is loaded.
         * Start-State (default)
         */
        defaultState,
        /**
         * App is in client mode. Everything except Client-Checkbox is
         * deactiviated
         */
        clientState,
        /**
         * Everything except cancel-Button is deactivated Useful while waiting
         * for long running tasks (creation of large ds or optimization)
         */
        processRunning
    };

    /**
     * Type of Process feedback for callback method in main program or elsewhere
     */
    public enum typeOfProcessFeedback {

        /**
         * Optimization has ended successful, maybe show results
         */
        /**
         * Optimization has ended successful, maybe show results
         */
        OptimizationSuccessful,
        /**
         * Optimization was canceled
         */
        OptimizationCanceled,
        /**
         * Optimization has ended but was not succesful
         */
        OptimizationNotSuccessful,
        /**
         * Simulation was succesful
         */
        SimulationSuccessful,
        /**
         * Simulation was canceled
         */
        SimulationCanceled,
        /**
         * Simulation has ended but was not succesful
         */
        SimulationNotSuccessful,
        /**
         * Generation of design space was successful
         */
        GenerationSuccessful,
        /**
         * Generation of design space was canceled
         */
        GenerationCanceled,
        /**
         * Generation of design space has ended but was not succesful
         */
        GenerationNotSuccessful,
        /**
         * Something was canceled (generic cancel handler)
         */
        SomethingCanceled,
        /**
         * Something was successful (generic success handler)
         */
        SomethingSuccessful,
        /**
         * Check of selected target value was successful, Optimization can be
         * analyzed completely
         */
        TargetCheckSuccessful,
        /**
         * *
         * Targetvalue is not unique, Optimization statistics will be wrong
         */
        TargetValueNotUnique,
        /**
         * Check of selected target value has failed. Optimization should not be
         * started, might lead to wrong results.
         */
        TargetCheckFailed,
        /**
         * Target was discarded because something changed (parameter, SCPN,
         * benchmark-function, etc.)
         */
        TargetDiscarded
    };

    /**
     * Type of possible plots (no plot possible, 2D-Plot, 3D-Plot)
     */
    public enum typeOfPossiblePlot {

        /**
         * No Plot is possible, may no Axes are configured
         */
        NoPlot,
        /**
         * 2D-Plot is possble and colorchooser is active
         */
        Plot2D,
        /**
         * 3D-Plot is possible, also the interactive plot
         */
        Plot3D
    };

    /**
     * Type of 3D-Plot to export Script
     */
    public enum typeOf3DPlot {

        /**
         * Standard Scatterplot3D with lib plot3D
         */
        ScatterPlot,
        /**
         * interactive opengl with lib rgl
         */
        Perspective,
        /**
         * Draws a 2D-Heatmap
         */
        Heatmap
    };

    /**
     * Type of distance calculation in definition space for found optima
     */
    public enum typeOfRelativeDistanceCalculation {

        /**
         * Use standard relative distance
         */
        STANDARD,
        /**
         * use EULID Distance and calc relative value
         */
        EUKLID
    };

    /**
     * Skills of Client to be mentioned for distributed simulation TimeNET,
     * MLDesigner, Matlab ???
     */
    public enum typeOfClientSkills {

        /**
         * client can simulate SCPNs (with TimeNET)
         */
        TIMENET,
        /**
         * client can simulate MLD-Models
         */
        MLDESIGNER,
        /**
         * client can simulate Matlab models
         */
        MATLAB
    };

    /**
     * Types of log (Verbose, Info, Error, Result)
     */
    public enum typeOfLogLevel {

        /**
         * Everything to be mentioned, which is less interesting
         */
        VERBOSE,
        /**
         * Everything which might be interesting
         */
        INFO,
        /**
         * Errors during runtime
         */
        ERROR,
        /**
         * Results of Optimization/Simulation, etc.
         */
        RESULT
    };

    /**
     * Possible results of calculating the optimum solution
     */
    public enum typeOfOptimumCalculationResult {

        /**
         * No problems, the optimum could be found in Design space, its
         * coordinates are unique.
         */
        SUCCESSFUL,
        /**
         * Optimum was calculated successful but problems occured. Maybe
         * coordinates are not unique.
         */
        PROBLEMATIC,
        /**
         * Optimum could not be calculated.
         */
        ERROR
    };

}
