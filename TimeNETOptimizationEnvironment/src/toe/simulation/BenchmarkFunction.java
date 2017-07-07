/*
 * Interface for all Benchmark Function

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.util.ArrayList;
import toe.datamodel.MeasureType;
import toe.datamodel.SimulationType;
import toe.datamodel.parameter;
import toe.typedef;

/**
 *
 * @author Christoph Bodenstein
 */
public interface BenchmarkFunction {

    /**
     * Returns the optimum simulation object All target measures will have the
     * same values in benchmark functions Thus giving a target measure in
     * function call is not needed
     *
     * @param targetMeasure Target Measure with Type of optimum and value
     * @return SimulationType-object of optimal target value
     */
    public SimulationType getOptimumSimulation(MeasureType targetMeasure);

    /**
     * Returns calculated simulation object, containing the value of measures
     * for given parameterlist
     *
     * all Measures have to contain simTime, CPUTime, Min/Max-Value
     *
     * @param parameterList definition space coordinates to calculate the result
     * values
     * @return SimulationTpye-object containing calculated measure values
     */
    public SimulationType getSimulationResult(ArrayList<parameter> parameterList);

    /**
     * Returns the minimal value a measure can have with this function
     *
     * @return minimum measure value
     */
    public double getMinValue();

    /**
     * Returns the maximum value a measure can have with this function
     *
     * @return maximum measure value
     */
    public double getMaxValue();

    /**
     * Return type of this benchmark function. (typeOfBenchmarkFunction) This
     * makes it easier to check and compare in other parts of program
     *
     * @return Type of this benchmarkfunction
     */
    public typedef.typeOfBenchmarkFunction getTypeOfBenchmarkFunction();
}
