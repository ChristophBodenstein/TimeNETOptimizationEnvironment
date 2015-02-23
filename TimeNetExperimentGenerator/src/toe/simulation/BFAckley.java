/*
 * Sphere Benchmark function
 */
package toe.simulation;

import java.util.ArrayList;
import toe.datamodel.MeasureType;
import toe.datamodel.SimulationType;
import toe.datamodel.parameter;
import toe.support;
import toe.typedef;

/**
 *
 * @author Christoph Bodenstein
 */
public class BFAckley implements BenchmarkFunction {

    double limitUpper = support.DEFAULT_ACKLEY_limitupper;
    double limitLower = support.DEFAULT_ACKLEY_limitLower;

    public SimulationType getSimulationResult(ArrayList<parameter> parameterList) {
        ArrayList<parameter> tmpParameterList = (parameterList);
        ArrayList<parameter> tmpListOfChangableParameter = support.getListOfChangableParameters(tmpParameterList);
        SimulationType tmpSimulation = new SimulationType();

        double sum;
        int dimension = tmpListOfChangableParameter.size();
        double xNew;
        double x[] = new double[dimension];
        double value;

        double confInterval = support.getParameterByName(parameterList, "ConfidenceIntervall").getValue();
        double maxError = support.getParameterByName(parameterList, "MaxRelError").getValue();

        for (int c = 0; c < dimension; c++) {
            parameter p = tmpListOfChangableParameter.get(c);
            value = p.getValue();
            p = support.getParameterByName(support.getOriginalParameterBase(), p.getName());
            //Check Range and align the value to map constraints
            //p.printInfo();
            xNew = (value - p.getStartValue()) / (p.getEndValue() - p.getStartValue());
            x[c] = xNew * (limitUpper - limitLower) + limitLower;
        }

        double sum1 = 0.0;
        double sum2 = 0.0;
        for (int d = 0; d < x.length; d++) {
            sum1 += (x[d] * x[d]);
            sum2 += (Math.cos(2 * Math.PI * x[d]));
            }//end of for-d-loop
        sum = (-20.0 * Math.exp(-0.2 * Math.sqrt(sum1 / ((double) x.length))) - Math.exp(sum2 / ((double) x.length)) + 20.0 + Math.E);
        
        ArrayList<MeasureType> tmpListOfMeasurements = support.getMeasures();
        //All Measure will have the same result value

        ArrayList<MeasureType> newListOfMeasurements = new ArrayList<MeasureType>();

        for (MeasureType tmpListOfMeasurement : tmpListOfMeasurements) {
            //make deep copy of old Measurement
            MeasureType tmpMeasurement = new MeasureType(tmpListOfMeasurement);
            tmpMeasurement.setAccuraryReached(true);
            tmpMeasurement.setMeanValue(sum);
            tmpMeasurement.setCPUTime(SimulatorBenchmark.getCPUTime(confInterval, maxError));
            tmpMeasurement.setSimulationTime(SimulatorBenchmark.getSimulationSteps(confInterval, maxError));
            tmpMeasurement.setMaxValue(this.getMaxValue());
            tmpMeasurement.setMinValue(this.getMinValue());
            newListOfMeasurements.add(tmpMeasurement);
        }

        tmpSimulation.setListOfParameters(tmpParameterList);
        tmpSimulation.setMeasures(newListOfMeasurements);
        return tmpSimulation;
    }

    public SimulationType getOptimumSimulation() {
        ArrayList<parameter> optimumParameterlist = support.getCopyOfParameterSet(support.getOriginalParameterBase());
        ArrayList<parameter> optimumChangableParameterset = support.getListOfChangableParameters(optimumParameterlist);

        //Optimum is in the middle of each parameter, its exact at 0,0
        for (parameter p : optimumChangableParameterset) {
            p.setValue(p.getEndValue() - ((p.getEndValue() - p.getStartValue()) / 2));
            support.log("P-Value for optimal solution (" + p.getName() + "): " + p.getValue());
        }
        SimulationType tmpSimulation = this.getSimulationResult(optimumParameterlist);
        support.log("Measurement-Values of optimal solution are:");

        for (MeasureType m : tmpSimulation.getMeasures()) {
            support.log(m.getMeasureName() + " has value: " + m.getMeanValue() + " with max: " + m.getMaxValue() + " and min: " + m.getMinValue());
        }

        return tmpSimulation;

    }

    public double getMinValue() {
        return 0.0;
    }

    public double getMaxValue() {
        return 20 + Math.E - Math.pow(Math.E,(-0.2*Math.max(Math.abs(limitLower),Math.abs(limitUpper))));
    }

    public typedef.typeOfBenchmarkFunction getTypeOfBenchmarkFunction() {
        return typedef.typeOfBenchmarkFunction.Ackley;
    }
}
