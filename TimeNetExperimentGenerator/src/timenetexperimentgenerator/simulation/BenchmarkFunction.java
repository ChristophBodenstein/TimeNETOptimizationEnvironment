/*
 * Interface for all Benchmark Function

 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package timenetexperimentgenerator.simulation;

import java.util.ArrayList;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;

/**
 *
 * @author Christoph Bodenstein
 */
public interface BenchmarkFunction {
/**
 * Returns the optimal Measure incl. Value, Min/Max
 * @param targetMeasure Measure, which should be calculated
 * @return MeasureType containing the value and min/max values
 */
public MeasureType getOptimumMeasure(MeasureType targetMeasure);

/**
 * 
 * @param parameterList
 * @return 
 */
public MeasureType getMeasure(ArrayList<parameter> parameterList);
public SimulationType getOptimumSimulation(MeasureType targetMeasure, ArrayList<parameter> parameterList);

    
}
