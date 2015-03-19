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

/**
 * Class to simulate real SCPN-Simulation. It uses the SimulationCache with read
 * log-data
 *
 * @author Christoph Bodenstein
 */
public class SimulatorBenchmark implements Simulator, Runnable {

    private SimulationCache mySimulationCache = null;
    private ArrayList<SimulationType> myListOfSimulations = null;
//private int simulationCounter=0;
    private String logFileName;
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
        support.log("LogfileName:" + logFileName);
    }

    /**
     * inits and starts the simulation, this is neccessary and must be
     * implemented In Benchmark we don`t use a local cache Ackley, Rosenbrock,
     * Schwefel, Rastrigin: source from Le Minh Nghia, NTU-Singapore Parts of
     * other functions are isp. by http://fossies.org/dox/cilib-0.7.6
     *
     * @param listOfParameterSetsTMP List of Parametersets to be simulated
     * @param simulationCounterTMP actual Number of simulation, will be
     * increased with every simulation-run
     * @param log write special log file for this simulator. true: write log
     * file, false: dont write log file
     */
    public void initSimulator(ArrayList<ArrayList<parameter>> listOfParameterSetsTMP, int simulationCounterTMP, boolean log) {
        this.log = log;
        //this.simulationCounter=simulationCounterTMP;
        this.listOfParameterSetsTMP = listOfParameterSetsTMP;
        this.status = 0;
        new Thread(this).start();
    }

    /**
     * Returns the actual status of all simulations
     *
     * @return % of simulatiions that are finished
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * Returns the actual simulation Counter
     *
     * @return actual simulation counter
     */
    public int getSimulationCounter() {
        return support.getGlobalSimulationCounter();
    }

    /**
     * Gets the list of completed simulations, should be used only if
     * getStatus() returns 100
     *
     * @return list of completed simulations (parsers) which contain all data
     * from the log-files
     */
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
        double cc = confidenceIntervall - 84;//norm to 1..15
        double me = 15 / maxRelError;//norm to 1..15
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
    public void run() {
        myListOfSimulations = new ArrayList<SimulationType>();
        support.log("Number of Benchmark-Simulations to do: " + listOfParameterSetsTMP.size());
        for (int i = 0; i < this.listOfParameterSetsTMP.size(); i++) {
            support.setStatusText("Simulating: " + (i + 1) + "/" + listOfParameterSetsTMP.size());
            support.incGlobalSimulationCounter();
            this.status = (100 / listOfParameterSetsTMP.size()) * i;
            myListOfSimulations.add(BenchmarkFactory.getBenchmarkFunction().getSimulationResult(listOfParameterSetsTMP.get(i)));
        }
        support.log("Number of done simulations: " + myListOfSimulations.size());
        if (log) {
            //Print out a log file
            support.addLinesToLogFileFromListOfParser(myListOfSimulations, logFileName);
        }
        this.status = 100;

//        /*
//        double limitLower = -5.0, limitUpper = 5.0;//The limits of the used benchmark function
//
//        switch (benchmarkFunction) {
//            case Ackley:
//                limitLower = support.DEFAULT_ACKLEY_limitLower;
//                limitUpper = support.DEFAULT_ACKLEY_limitupper;
//                break;
//            /*case Rosenbrock:
//             limitLower=-5.0;
//             limitUpper=5.0;
//             break;*/
//            case Sphere:
//                limitLower = support.DEFAULT_Sphere_limitLower;
//                limitUpper = support.DEFAULT_Sphere_limitupper;
//                break;
//            case Matya:
//                limitUpper = support.DEFAULT_Matya_limitLower;
//                limitLower = support.DEFAULT_Matya_limitupper;
//                break;
//            /*case Easom:
//             limitUpper=100.0;
//             limitLower=-100.0;
//             break;*/
//            case Schwefel:
//                limitUpper = support.DEFAULT_Schwefel_limitLower;
//                limitLower = support.DEFAULT_Schwefel_limitupper;
//                break;
//            case Rastrigin:
//                limitLower = support.DEFAULT_Rastrigin_limitLower;
//                limitUpper = support.DEFAULT_Rastrigin_limitupper;
//                break;
//
//            default:
//                break;
//        }
//
//        myListOfSimulations = new ArrayList<SimulationType>();
//
//        for (int i = 0; i < listOfParameterSetsTMP.size(); i++) {
//            SimulationType tmpSimulation = new SimulationType();
//            ArrayList<parameter> tmpParameterList = listOfParameterSetsTMP.get(i);
//            ArrayList<parameter> tmpListOfChangableParameter = support.getListOfChangableParameters(tmpParameterList);
//
//            //set indicator
//            support.setStatusText("Simulating: " + (i + 1) + "/" + listOfParameterSetsTMP.size());
//
//            double sum;
//            int dimension = tmpListOfChangableParameter.size();
//            double xNew;
//            double x[] = new double[dimension];
//            double value;
//            for (int c = 0; c < dimension; c++) {
//                parameter p = tmpListOfChangableParameter.get(c);
//                value = p.getValue();
//                p = support.getParameterByName(support.getOriginalParameterBase(), p.getName());
//                //Check Range and align the value to map constraints
//                //p.printInfo();
//                xNew = (value - p.getStartValue()) / (p.getEndValue() - p.getStartValue());
//                x[c] = xNew * (limitUpper - limitLower) + limitLower;
//            }
//
//            //support.log(dimension+" parameter out of "+tmpParameterList.size()+" are changable.");
//        /*if(dimension<2){
//             support.log("New Dimension is smaller, check details of Paramaters.");
//             for(int ci=0;ci<tmpParameterList.size();ci++){
//             parameter p=tmpParameterList.get(ci);
//             p.printInfo();
//             }
//             }*/
//            switch (benchmarkFunction) {
//                case Ackley:
//                    //source of this part is from Le Minh Nghia, NTU-Singapore
//                    double sum1 = 0.0;
//                    double sum2 = 0.0;
//                    for (int d = 0; d < x.length; d++) {
//                        sum1 += (x[d] * x[d]);
//                        sum2 += (Math.cos(2 * Math.PI * x[d]));
//                    }//end of for-d-loop
//                    sum = (-20.0 * Math.exp(-0.2 * Math.sqrt(sum1 / ((double) x.length))) - Math.exp(sum2 / ((double) x.length)) + 20.0 + Math.E);
//                    break;
//                /*case Rosenbrock:
//                 //source of this part is from Le Minh Nghia, NTU-Singapore
//                 double [] v = new double[x.length];
//                 for (int i1 = 0; i1 < x.length; i1++) v[i1] = x[i1] + 1;
//                 for (int i1 = 0 ; i1 < (x.length-1) ; i1 ++) {
//                 double temp1 = (v[i1] * v[i1]) - v[i1+1];
//                 double temp2 = v[i1] - 1.0;
//                 sum += (100.0 * temp1 * temp1) + (temp2 * temp2);
//                 }
//                 break;*/
//                case Sphere:
//                    sum = 0.0;
//                    for (int c = 0; c < dimension; c++) {
//                        parameter p = tmpListOfChangableParameter.get(c);
//                        value = p.getValue();
//                        p = support.getParameterByName(support.getOriginalParameterBase(), p.getName());
//                        //Check Range and align the value to map constraints
//                        xNew = (value - p.getStartValue()) / (p.getEndValue() - p.getStartValue());
//                        xNew = xNew * (limitUpper - limitLower) + limitLower;
//                        sum += (xNew * xNew);
//                    }//End of for-c-loop
//                    break;
//
//                case Matya:
//                    if (dimension != 2) {
//                        support.log("Matya is only defined for 2 dimensions");
//                        return;
//                    }
//                    double x0 = x[0];
//                    double x1 = x[1];
//                    sum = 0.26 * (x0 * x0 + x1 * x1) - 0.48 * x0 * x1;
//                    break;
//
//                case Schwefel:
//                    //source of this part is from Le Minh Nghia, NTU-Singapore
//                    //Schwefel's problem 1.2 - Unimodal
//                    //Global optimum: f = 0 at x[] = 0
//                    double prev_sum,
//                     curr_sum,
//                     outer_sum;
//                    curr_sum = x[0];
//                    outer_sum = (curr_sum * curr_sum);
//                    for (int i1 = 1; i1 < x.length; i1++) {
//                        prev_sum = curr_sum;
//                        curr_sum = prev_sum + x[i1];
//                        outer_sum += (curr_sum * curr_sum);
//                    }
//                    sum = outer_sum;
//                    break;
//
//                case Rastrigin:
//                    //source of this part is from Le Minh Nghia, NTU-Singapore
//                    //Multimodal - x [-5,5], global - 0 at x[] = 0
//                    double res = 10 * x.length;
//                    for (int i1 = 0; i1 < x.length; i1++) {
//                        res += x[i1] * x[i1]
//                                - 10 * Math.cos(2 * Math.PI * x[i1]);
//                    }
//                    sum = res;
//                    break;
//
//                /*case Easom:
//                 if(dimension!=2){
//                 support.log("Easom is only defined for 2 dimensions");
//                 return;
//                 }
//                 double powerTerm1 = -((x[0]-Math.PI)*(x[0]-Math.PI));
//                 double powerTerm2 = -((x[1]-Math.PI)*(x[1]-Math.PI));
//                 double power = powerTerm1 + powerTerm2;
//                 sum = -Math.cos(x[0]) * Math.cos(x[1]) * Math.exp(power);
//                 break;
//                 */
//                default:
//                    sum = 0.0;
//                    break;
//            }
//
//            ArrayList<MeasureType> tmpListOfMeasurements = ((MeasurementForm) support.getMeasureFormPane().getComponentAt(0)).getMeasurements();
//            //All Measure will have the same result value
//
//            ArrayList<MeasureType> newListOfMeasurements = new ArrayList<MeasureType>();
//
//            for (int d = 0; d < tmpListOfMeasurements.size(); d++) {
//                //make deep copy of old Measurement
//                MeasureType tmpMeasurement = new MeasureType(tmpListOfMeasurements.get(d));
//                tmpMeasurement.setAccuraryReached(true);
//                tmpMeasurement.setMeanValue(sum);
//                //TODO fill out all other imformation
//                newListOfMeasurements.add(tmpMeasurement);
//                tmpMeasurement.setCPUTime(2.22);
//
//            }//end of for-d-loop
//
//            tmpSimulation.setListOfParameters(tmpParameterList);
//            tmpSimulation.setMeasures(newListOfMeasurements);
//            myListOfSimulations.add(tmpSimulation);
//            support.incGlobalSimulationCounter();
//
//            this.status = (100 / listOfParameterSetsTMP.size()) * i;
//        }//End of for-i-loop
//        this.status = 100;
//        
    }

    /**
     * Returns the calulated optimimum For Benchmark-Functions this can be
     * caluclated. For other simulators, this must be given by user.
     *
     * @param targetMeasure Measure that should be optimized
     * @return calculated optimum (for given measure)
     */
    public SimulationType getCalculatedOptimum(MeasureType targetMeasure) {

        return BenchmarkFactory.getBenchmarkFunction().getOptimumSimulation();

//        support.log("SimulatorBenchmark: Getting absolute optimum simulation from Benchmark-Function.");
//        ArrayList<parameter> optimumParameterlist = support.getCopyOfParameterSet(support.getOriginalParameterBase());
//        ArrayList<parameter> optimumChangableParameterset = support.getListOfChangableParameters(optimumParameterlist);
//
//        //Create dummy-Simulation with optimum-Measure
//        SimulationType myOptimumSimulation = new SimulationType();
//
//        ArrayList<MeasureType> tmpListOfMeasurements = support.getMainFrame().getListOfActiveMeasureMentsToOptimize();
//        //All Measure will have the same result value
//
//        ArrayList<MeasureType> newListOfMeasurements = new ArrayList<MeasureType>();
//        for (int d = 0; d < tmpListOfMeasurements.size(); d++) {
//            //make deep copy of old Measurement
//            MeasureType tmpMeasurement = new MeasureType(tmpListOfMeasurements.get(d));
//            tmpMeasurement.setAccuraryReached(true);
//            //Meanvalue is given by original Measure
//            tmpMeasurement.setCPUTime(0);
//            newListOfMeasurements.add(tmpMeasurement);
//        }//end of for-d-loop
//        switch (benchmarkFunction) {
//            case Ackley:
//                //Optimum is in the middle of each parameter, its exact at 0,0
//                for (int i = 0; i < optimumChangableParameterset.size(); i++) {
//                    parameter p = optimumChangableParameterset.get(i);
//                    p.setValue(p.getEndValue() - ((p.getEndValue() - p.getStartValue()) / 2));
//                }
//
//                break;
//            /*case Rosenbrock:
//             //TODO remove this function
//             support.log("No Optimum is calculated for Rosenbrock!");
//             break;
//             */
//            case Sphere:
//                //Optimum is in the middle of each parameter, its exact at 0,0
//                for (int i = 0; i < optimumChangableParameterset.size(); i++) {
//                    parameter p = optimumChangableParameterset.get(i);
//                    p.setValue(p.getEndValue() - ((p.getEndValue() - p.getStartValue()) / 2));
//                }
//                break;
//
//            case Matya:
//                //Optimum is in the middle of each parameter, its exact at 0,0
//                for (int i = 0; i < optimumChangableParameterset.size(); i++) {
//                    parameter p = optimumChangableParameterset.get(i);
//                    p.setValue(p.getEndValue() - ((p.getEndValue() - p.getStartValue()) / 2));
//                }
//                break;
//
//            case Schwefel:
//                //Optimum is in the middle of each parameter, its exact at 0,0
//                for (int i = 0; i < optimumChangableParameterset.size(); i++) {
//                    parameter p = optimumChangableParameterset.get(i);
//                    p.setValue(p.getEndValue() - ((p.getEndValue() - p.getStartValue()) / 2));
//                }
//                break;
//
//            case Rastrigin:
//                //Optimum is in the middle of each parameter, its exact at 0,0
//                for (int i = 0; i < optimumChangableParameterset.size(); i++) {
//                    parameter p = optimumChangableParameterset.get(i);
//                    p.setValue(p.getEndValue() - ((p.getEndValue() - p.getStartValue()) / 2));
//                }
//                break;
//
//            /*case Easom:
//             //TODO remove this function
//             support.log("No Optimum is calculated for Easom!");
//             break;
//             */
//            default:
//
//                break;
//        }
//
//        myOptimumSimulation.setListOfParameters(optimumParameterlist);
//        myOptimumSimulation.setMeasures(newListOfMeasurements);
//
//        return myOptimumSimulation;
    }

    @Override
    public int cancelAllSimulations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}