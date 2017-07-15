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
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Christoph Bodenstein
 */
public class BFSchwefel implements BenchmarkFunction {

    double limitUpper = support.DEFAULT_Schwefel_limitupper;
    double limitLower = support.DEFAULT_Schwefel_limitLower;

    @Override
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

        //source of this part is from Le Minh Nghia, NTU-Singapore
        //Schwefel's problem 1.2 - Unimodal
        //Global optimum: f = 0 at x[] = 0
        sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i] * Math.sin(Math.pow(Math.abs(x[i]), 0.5));
        }
        sum = 418.9829 * x.length - sum;

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

    @Override
    public SimulationType getOptimumSimulation(MeasureType targetMeasure) {
        ArrayList<parameter> optimumParameterlist = support.getCopyOfParameterSet(support.getOriginalParameterBase());
        ArrayList<parameter> optimumChangableParameterset = support.getListOfChangableParameters(optimumParameterlist);

        //Optimum is in the middle of each parameter, its exact at 0,0
        for (parameter p : optimumChangableParameterset) {
            p.setValue(p.getEndValue() - ((p.getEndValue() - p.getStartValue()) / 2));
            support.log("P-Value for optimal solution (" + p.getName() + "): " + p.getValue(), typeOfLogLevel.INFO);
        }
        SimulationType tmpSimulation = this.getSimulationResult(optimumParameterlist);
        support.log("Measurement-Values of optimal solution are:", typeOfLogLevel.INFO);

        for (MeasureType m : tmpSimulation.getMeasures()) {
            support.log(m.getMeasureName() + " has value: " + m.getMeanValue() + " with max: " + m.getMaxValue() + " and min: " + m.getMinValue(), typeOfLogLevel.INFO);
        }

        return tmpSimulation;

    }

    @Override
    public double getMinValue() {
        return 0.0;
    }

    @Override
    public double getMaxValue() {
        return 418.9829 * support.getNumberOfChangeableParameters() * 2;
    }

    @Override
    public typedef.typeOfBenchmarkFunction getTypeOfBenchmarkFunction() {
        return typedef.typeOfBenchmarkFunction.Schwefel;
    }
}
