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
    private static final BenchmarkFunction myBFSphere = new BFSphere();

    /**
     * Returns the chosen Benchmark-function instance TODO: Use Singleton
     *
     * @return Benchmarkfunction-object
     */
    public static BenchmarkFunction getBenchmarkFunction() {
        switch (support.getChosenBenchmarkFunction()) {
            case Matya:
                return myBFMatya;
            case Sphere:
                return myBFSphere;

            default:
                return myBFMatya;
        }
    }
}
