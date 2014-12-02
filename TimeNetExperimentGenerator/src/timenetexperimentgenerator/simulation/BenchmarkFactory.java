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
    private static final BenchmarkFunction myBFAckley = new BFAckley();
    private static final BenchmarkFunction myBFSchwefel = new BFSchwefel();

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
            case Ackley:
                return myBFAckley;
            case Schwefel:
                return myBFSchwefel;

            default:
                return myBFMatya;
        }
    }
}
