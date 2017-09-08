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
import toe.helper.SimOptiCallback;
import toe.support;
import toe.typedef;
import toe.typedef.typeOfBenchmarkFunction;
import toe.typedef.typeOfLogLevel;

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read
 * log-data
 *
 * @author Christoph Bodenstein
 */
public class SimulatorBenchmark extends Thread implements Simulator, SimOptiCallback {

    private SimulationCache mySimulationCache = null;
    private ArrayList<SimulationType> myListOfSimulations = null;
    private final String logFileName;
    private typeOfBenchmarkFunction benchmarkFunction = typeOfBenchmarkFunction.Sphere;
    int status = 0;
    boolean log = true;
    ArrayList<ArrayList<parameter>> listOfParameterSetsTMP;
    //boolean isOptimumCalculated = false; // by default optimum coordinates are not calculated
    boolean cancelAllSimulations = false;//Flag to cancel all simulations
    private SimOptiCallback listener = null;
    private SimulationType calculatedOptimum = null;

    /**
     * Constructor
     */
    public SimulatorBenchmark() {
        this.benchmarkFunction = support.getChosenBenchmarkFunction();
        logFileName = support.getTmpPath() + File.separator + "SimLog_Benchmark_" + benchmarkFunction.toString() + "_" + Calendar.getInstance().getTimeInMillis() + ".csv";
        support.log("LogfileName:" + logFileName, typeOfLogLevel.INFO);
    }

    /**
     * Inits and starts the simulation, this is neccessary and must be
     * implemented. In Benchmark we don`t use a local cache. Ackley, Schwefel:
     * source from Le Minh Nghia, NTU-Singapore. Parts of other functions are
     * inspired by http://fossies.org/dox/cilib-0.7.6
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
    public ArrayList<SimulationType> getListOfCompletedSimulations() {
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

            if (cancelAllSimulations) {
                support.log("Will abort all benchmark simulations and return null.", typeOfLogLevel.INFO);
                status = 100;
                myListOfSimulations = null;
                cancelAllSimulations = false;
                break;
            }
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
     * calculated. For other simulators, this must be given by user.
     *
     * @param targetMeasure Measure that should be optimized
     * @return calculated optimum (for given measure)
     */
    @Override
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {
        if (calculatedOptimum != null) {
            return this.calculatedOptimum;
        } else {
            return BenchmarkFactory.getBenchmarkFunction().getOptimumSimulation(targetMeasure);
        }
    }

    @Override
    public int cancelAllSimulations() {
        cancelAllSimulations = true;
        return 0;
    }

    @Override
    public String getLogfileName() {
        return this.logFileName;
    }

    @Override
    public boolean isOptimumCalculated() {
        return (this.calculatedOptimum != null);
    }

    @Override
    public void startCalculatingOptimum(SimOptiCallback listener) {
        this.listener = listener;
        //Check if all parametersets are generated (ListOfParameterSetsToBeWritten)
        if (support.getMainFrame().getListOfParameterSetsToBeWritten().size() <= support.DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION) {
            support.setStatusText("Not enaugh Parametersets to simulate for target check.");
            support.log("No Parametersets to simulate for target check.", typeOfLogLevel.INFO);
            listener.operationFeedback("Generate DS first!", typedef.typeOfProcessFeedback.TargetCheckFailed);
            return;
        } else {
            //If not, ask to generate it (not necessary, button is disabled in this case)
        }
        support.setParameterBase(support.getMainFrame().getParameterBase());
        support.setOriginalParameterBase(support.getMainFrame().getParameterBase());
        this.initSimulator(support.getMainFrame().getListOfParameterSetsToBeWritten(), false);
        support.waitForSimulatorAsynchronous(this, this);
    }

    @Override
    public void stopCalculatingOptimum(SimOptiCallback listener) {
        listener.operationFeedback("Targetcheck canceled.", typedef.typeOfProcessFeedback.TargetDiscarded);
        this.calculatedOptimum = null;
    }

    @Override
    public void discardCalculatedOptimum() {
        support.log("TBD Implement: discardCalculatedOptimum()", typeOfLogLevel.ERROR);
        this.calculatedOptimum = null;
    }

    @Override
    public void operationFeedback(String message, typedef.typeOfProcessFeedback feedback) {
        try {
            switch (feedback) {
                case SimulationSuccessful:
                    ArrayList<SimulationType> simulationResults = this.getListOfCompletedSimulations();
                    double oldDistance = simulationResults.get(0).getDistanceToTargetValue();
                    ArrayList<SimulationType> foundOptima = new ArrayList<>();
                    foundOptima.add(simulationResults.get(0));

                    for (int i = 1; i < simulationResults.size(); i++) {
                        support.spinInLabel();
                        support.log(String.valueOf(simulationResults.get(i).getDistanceToTargetValue()), typeOfLogLevel.VERBOSE);
                        if (oldDistance > simulationResults.get(i).getDistanceToTargetValue()) {
                            foundOptima.clear();
                            foundOptima.add(simulationResults.get(i));
                            oldDistance = simulationResults.get(i).getDistanceToTargetValue();
                        } else if (Math.abs(oldDistance - simulationResults.get(i).getDistanceToTargetValue()) < support.DEFAULT_TARGET_STEPPING) {
                            foundOptima.add(simulationResults.get(i));
                        }
                    }

                    if (foundOptima.size() < 1) {
                        //Error checking the target
                        listener.operationFeedback("Opticheck failed.", typedef.typeOfProcessFeedback.TargetCheckFailed);
                    } else if (foundOptima.size() > 1) {
                        //Not unique
                        listener.operationFeedback("Selected Target is not unique There are " + foundOptima.size() + " same targets.", typedef.typeOfProcessFeedback.TargetValueNotUnique);
                        support.log("The distance to target is: " + oldDistance, typeOfLogLevel.INFO);
                    } else if (foundOptima.size() == 1) {
                        //Exactly one optimum with selected target value was found
                        if (oldDistance > 0.0) {
                            //distance not zero --> will adapt selected optimum!
                            listener.operationFeedback("Target is unique, will change target value to match distance of 0.0.", typedef.typeOfProcessFeedback.TargetCheckSuccessful);
                            support.log("Old distance to target is: " + oldDistance, typeOfLogLevel.INFO);

                        } else {
                            listener.operationFeedback("Target is unique and distance is 0.0!", typedef.typeOfProcessFeedback.TargetCheckSuccessful);
                        }
                    }
                    support.log("Target value(s) found at: ", typeOfLogLevel.RESULT);
                    for (int i = 0; i < foundOptima.size(); i++) {
                        support.log("Parameterset: " + i, typeOfLogLevel.RESULT);
                        for (int c = 0; c < foundOptima.get(i).getListOfParameters().size(); c++) {
                            if (foundOptima.get(i).getListOfParameters().get(c).isIteratable()) {
                                foundOptima.get(i).getListOfParameters().get(c).printInfo(typeOfLogLevel.INFO);
                            }
                        }
                    }
                    support.log("Targetvalue #0 will be used for optimization and statistics.", typeOfLogLevel.RESULT);
                    calculatedOptimum = foundOptima.get(0);
                    break;
                case SimulationCanceled:
                    support.log("Simulation aborted during targetcheck.", typeOfLogLevel.INFO);
                    listener.operationFeedback("Targetecheck aborted.", typedef.typeOfProcessFeedback.TargetCheckFailed);
                    break;
                default:
                    support.log("Unexpected feedback from simulator during targetcheck.", typeOfLogLevel.ERROR);
                    listener.operationFeedback("Unexpected feedback.", typedef.typeOfProcessFeedback.TargetCheckFailed);
            }
        } catch (Exception e) {
            support.log("General error during targetcheck. Maybe Reference to simulator missing?", typeOfLogLevel.ERROR);
            listener.operationFeedback("Error during targetcheck.", typedef.typeOfProcessFeedback.TargetCheckFailed);
            support.log(e.getMessage(), typeOfLogLevel.ERROR);
        }
    }
}
