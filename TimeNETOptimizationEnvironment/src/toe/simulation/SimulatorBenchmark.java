/**
 * Simulator which uses log data from already done simulations and returns them.
 *
 * Christoph Bodenstein TU-Ilmenau, FG SSE
 */
package toe.simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import toe.datamodel.parameter;
import toe.datamodel.SimulationType;
import toe.datamodel.MeasureType;
import toe.support;
import toe.typedef.typeOfBenchmarkFunction;
import toe.typedef.typeOfLogLevel;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read
 * log-data
 *
 * @author Christoph Bodenstein
 */
public class SimulatorBenchmark extends Thread implements Simulator {

    private SimulationCache mySimulationCache = null;
    private ArrayList<SimulationType> myListOfSimulations = null;
    private final String logFileName;
    private typeOfBenchmarkFunction benchmarkFunction = typeOfBenchmarkFunction.Sphere;
    int status = 0;
    boolean log = true;
    ArrayList<ArrayList<parameter>> listOfParameterSetsTMP;

    /**
     * Constructor
     */
    public SimulatorBenchmark() {
        this.benchmarkFunction = support.getChosenBenchmarkFunction();
        logFileName = support.getTmpPath() + File.separator + "SimLog_Benchmark_" + benchmarkFunction.toString() + "_" + Calendar.getInstance().getTimeInMillis() + ".csv";
        support.log("LogfileName:" + logFileName, typeOfLogLevel.INFO);
    }

    /**
     * inits and starts the simulation, this is neccessary and must be
     * implemented In Benchmark we don`t use a local cache Ackley, Rosenbrock,
     * Schwefel, Rastrigin: source from Le Minh Nghia, NTU-Singapore Parts of
     * other functions are isp. by http://fossies.org/dox/cilib-0.7.6
     *
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param log write special log file for this simulator. true: write log
     * file, false: dont write log file
     */
    @Override
    public void initSimulator(ArrayList<ArrayList<parameter>> listOfParameterSetsTMP, boolean log) {
        this.log = log;
        this.listOfParameterSetsTMP = listOfParameterSetsTMP;
        this.status = 0;
        new Thread(this).start();
    }

    /**
     * Returns the actual status of all simulations
     *
     * @return % of simulatiions that are finished
     */
    @Override
    public int getStatus() {
        return this.status;
    }

    /**
     * Gets the list of completed simulations, should be used only if
     * getStatus() returns 100
     *
     * @return list of completed simulations (parsers) which contain all data
     * from the log-files
     */
    @Override
    public ArrayList<SimulationType> getListOfCompletedSimulationParsers() {
        return this.myListOfSimulations;
    }

    /**
     * Returns the data-source for simulated simulation-runs
     *
     * @return the mySimulationCache
     */
    public SimulationCache getMySimulationCache() {
        return mySimulationCache;
    }

    /**
     * Sets the data-source for simulated simulation-runs
     *
     * @param mySimulationCache the mySimulationCache to set
     */
    public void setMySimulationCache(SimulationCache mySimulationCache) {
        this.mySimulationCache = mySimulationCache;
    }

    /**
     * Calculated the approx number of simulation steps for one simulation
     * Dummy-Function to immitate the behavoir of real simulations
     *
     * @return approx number of simulation steps
     * @param confidenceIntervall given ConfidenceIntervall
     * @param maxRelError given Maximum relative Error
     */
    public static double getSimulationSteps(double confidenceIntervall, double maxRelError) {
        return 10;
    }

    /**
     * Calculate the approx CPU Time for one Simulation run Is useful to
     * simulate accuracy adaptive optimization Dummy-Function to immitate the
     * behavior of real simulations
     *
     * @return approx CPU Time for one simulation run
     * @param confidenceIntervall given ConfidenceIntervall
     * @param maxRelError given Maximum relative Error
     */
    public static double getCPUTime(double confidenceIntervall, double maxRelError) {
        double cc = Math.min(Math.max(confidenceIntervall - 84, 1), 15);//norm to 1..15
        double me = Math.min(Math.max(15 / maxRelError, 1), 15);//norm to 1..15
        //double constX2 = 4.1765277575;
        //double constX3 = -0.0019265913;
        //double constX4 = 89.0756210425;
        double a = 49.74;
        double b = 23.91;
        double c = 154.29;

        //double result=Math.pow(max + conf, 2) * constX2 + Math.pow(max + conf, 3) * constX3 + constX4;
        //support.log("ConfInterval: "+confidenceIntervall + " MaxRelError: "+maxRelError + "CPUTime: "+result);
        //=POTENZ((A2/84+15/B2);2)*$M$12+POTENZ((A2/84+15/B2);3)*$N$12+$O$12
        double result = Math.pow((cc * me / a), 3) * b + c;
        return result;

    }

    /**
     * Main Method for thread, called with Thread.start()
     */
    @Override
    public void run() {
        myListOfSimulations = new ArrayList<>();
        support.log("Number of Benchmark-Simulations to do: " + listOfParameterSetsTMP.size(), typeOfLogLevel.INFO);
        for (int i = 0; i < this.listOfParameterSetsTMP.size(); i++) {
            support.setStatusText("Simulating: " + (i + 1) + "/" + listOfParameterSetsTMP.size());
            support.incGlobalSimulationCounter();
            this.status = (100 / listOfParameterSetsTMP.size()) * i;
            myListOfSimulations.add(BenchmarkFactory.getBenchmarkFunction().getSimulationResult(listOfParameterSetsTMP.get(i)));
        }
        support.log("Number of done simulations: " + myListOfSimulations.size(), typeOfLogLevel.INFO);
        if (log) {
            //Print out a log file
            support.addLinesToLogFileFromListOfSimulations(myListOfSimulations, logFileName);
        }
        this.status = 100;
        synchronized (this) {
            notify();
        }
    }

    /**
     * Returns the calulated optimimum For Benchmark-Functions this can be
     * caluclated. For other simulators, this must be given by user.
     *
     * @param targetMeasure Measure that should be optimized
     * @return calculated optimum (for given measure)
     */
    @Override
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {

        return BenchmarkFactory.getBenchmarkFunction().getOptimumSimulation();
    }

    @Override
    public int cancelAllSimulations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLogfileName() {
        return this.logFileName;
    }
}
