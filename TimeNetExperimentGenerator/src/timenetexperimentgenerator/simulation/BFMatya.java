/*
 * Matyas Benchmark function
 */
package timenetexperimentgenerator.simulation;

import java.util.ArrayList;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.typedef;

/**
 *
 * @author Christoph Bodenstein
 */
public class BFMatya implements BenchmarkFunction {
    
    double limitUpper = support.DEFAULT_Matya_limitLower;
    double limitLower = support.DEFAULT_Matya_limitupper;
    
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
        if (dimension != 2) {
            support.log("Matya is only defined for 2 dimensions");
            return null;
        }
        double x0 = x[0];
        double x1 = x[1];
        sum = 0.26 * (x0 * x0 + x1 * x1) - 0.48 * x0 * x1;
        
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
        return 0.26 * (support.DEFAULT_Matya_limitLower * support.DEFAULT_Matya_limitLower + support.DEFAULT_Matya_limitupper * support.DEFAULT_Matya_limitupper) - 0.48 * support.DEFAULT_Matya_limitLower * support.DEFAULT_Matya_limitupper;
    }
    
    public typedef.typeOfBenchmarkFunction getTypeOfBenchmarkFunction() {
        return typedef.typeOfBenchmarkFunction.Matya;
    }
}
