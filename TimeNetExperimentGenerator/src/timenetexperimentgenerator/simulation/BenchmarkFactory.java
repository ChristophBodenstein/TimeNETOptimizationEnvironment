/*
 * Factory that returns the chosen Benchmark-Function
 */
package timenetexperimentgenerator.simulation;

/**
 *
 * @author Christoph Bodenstein
 */
public class BenchmarkFactory {

    /**
     * Returns the chosen Benchmark-function instance TODO: Use Singleton
     * @return Benchmarkfunction-object
     */
    public static BenchmarkFunction getBenchmarkFunction() {
        return new BFMatya();
    }
}
