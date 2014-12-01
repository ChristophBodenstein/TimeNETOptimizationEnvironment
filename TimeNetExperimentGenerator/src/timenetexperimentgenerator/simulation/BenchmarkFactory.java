/*
 * Factory that returns the chosen Benchmark-Function
 */
package timenetexperimentgenerator.simulation;

import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein
 */
public class BenchmarkFactory {

    private static final BenchmarkFunction myBFMatya = new BFMatya();

    /**
     * Returns the chosen Benchmark-function instance TODO: Use Singleton
     *
     * @return Benchmarkfunction-object
     */
    public static BenchmarkFunction getBenchmarkFunction() {
        switch (support.getChosenBenchmarkFunction()) {
            case Matya:
                return myBFMatya;

            default:
                return myBFMatya;
        }
    }
}
