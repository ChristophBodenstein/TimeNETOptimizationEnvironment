/*
 * Optimizer using the simulated annealing algorithm
 * It´s a child of OptimizerHill
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.optimization;

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
public class OptimizerSimAnnealing extends OptimizerHill implements Runnable, Optimizer {

    private int accepted = 0, generated = 0;
    private double D;
    private double c;
    double actualTempParameter = 1;
    double actualTempCost = 1;
    String nameOfdummyLogfile;
    int phase = 0; //Phase of Simulated Annealing in Two-Phase-Mode (0..1)

    /**
     * Constructor
     *
     */
    public OptimizerSimAnnealing() {
        super();
        D = support.getListOfChangableParameters(support.getMainFrame().getParameterBase()).size();//Number of changeable parameters
        c = -Math.log(support.getOptimizerPreferences().getPref_TRatioScale(phase));
        c = c * Math.exp(-Math.log(support.getOptimizerPreferences().getPref_TAnnealScale(phase) / this.D));
        actualTempCost = support.getOptimizerPreferences().getPref_MaxTempCost(phase);
        actualTempParameter = support.getOptimizerPreferences().getPref_MaxTempParameter(phase);
        nameOfdummyLogfile = this.logFileName;
        nameOfdummyLogfile = support.removeExtention(nameOfdummyLogfile) + "_SA_Temperatures.csv";
        support.addLinesToLogFileFromListOfParser(null, nameOfdummyLogfile);
        this.optimized = false;

    }

    /**
     * Check, if next Solution is better. Calculate if Optimization is done or
     * not Reset wrongSolutionCounter if solution is better Reset
     * wrongSolutionPerDirectionCounter if solution is better Return true if
     * wrongSolutionCounter is down
     *
     * @return true if optimization result is found, else false
     */
    @Override
    protected boolean isOptimized(double actualDistance, double nextDistance) {
        //If next Solution is better then take it as actual best solution

        //Inc Simulationcounter
        generated++;

        //Calculate the new Temperatures
        switch (support.getOptimizerPreferences().getPref_Cooling(phase)) {
            default:
            case Boltzmann:
                double denominator = (Math.log((double) generated));
                if (denominator <= 0.0) {
                    denominator = 0.1;
                }
                actualTempParameter = (1 / denominator) * support.getOptimizerPreferences().getPref_MaxTempParameter(phase);
                if (actualTempParameter > support.getOptimizerPreferences().getPref_MaxTempParameter(phase)) {
                    actualTempParameter = support.getOptimizerPreferences().getPref_MaxTempParameter(phase);
                }

                actualTempCost = (1 / denominator) * support.getOptimizerPreferences().getPref_MaxTempCost(phase);
                if (actualTempCost > support.getOptimizerPreferences().getPref_MaxTempCost(phase)) {
                    actualTempCost = support.getOptimizerPreferences().getPref_MaxTempCost(phase);
                }

                break;

            case FastAnnealing:
                actualTempParameter = (1 / (double) generated) * support.getOptimizerPreferences().getPref_MaxTempParameter(phase);
                actualTempCost = (1 / (double) generated) * support.getOptimizerPreferences().getPref_MaxTempCost(phase);
                break;

            case VeryFastAnnealing:
                actualTempParameter = Math.exp(-c * Math.pow((double) generated, 1 / D)) * support.getOptimizerPreferences().getPref_MaxTempParameter(phase);
                actualTempCost = Math.exp(-c * Math.pow((double) accepted, 1 / D)) * support.getOptimizerPreferences().getPref_MaxTempCost(phase);

                break;
        }

        support.log("Actual Temp for Parameters: " + actualTempParameter, typeOfLogLevel.INFO);
        support.log("Actual Temp for Cost: " + actualTempCost, typeOfLogLevel.INFO);
        //Eject if Temperature is lower then Epsilon
        if (support.round(actualTempCost, 3) <= support.getOptimizerPreferences().getPref_Epsilon(phase) || support.round(actualTempParameter, 3) <= support.getOptimizerPreferences().getPref_Epsilon(phase)) {
            //Set currentsolution=bestsolution so it will be printed as optimum
            currentSolution = bestSolution;
            this.optimized = true;
            return true;
        }

        //If new cost is lower then repvious then break and accept new solution
        if (getActualDistance(nextSolution) < getActualDistance(bestSolution)) {
            bestSolution = nextSolution;

            double delta = getActualDistance(nextSolution) - getActualDistance(currentSolution);

            if ((delta < 0) || (Math.random() < (Math.exp(-delta / actualTempCost)))) {
                currentSolution = nextSolution;
                this.accepted++;
            }
        }
        return false;//Go back and loop again
    }

    /**
     * Returns the next parameterset You should overload this method in your
     * child-classes
     *
     * @param actualParameterset actual parameterset, if null, then first
     * parameterset is calculated
     * @return next parameterset to be simulated
     */
    @Override
    protected ArrayList<parameter> getNextParameterset(ArrayList<parameter> actualParameterset) {
        ArrayList<parameter> newParameterset = support.getCopyOfParameterSet(parameterBase);
        ArrayList<parameter> listOfChangableParameters = support.getListOfChangableParameters(newParameterset);

        support.log("TempParameter:" + actualTempParameter + " and TempCost:" + actualTempCost, typeOfLogLevel.INFO);
        for (int i = 0; i < listOfChangableParameters.size(); i++) {

            parameter p = listOfChangableParameters.get(i);
            double sign;
            double distanceMax = p.getEndValue() - p.getStartValue();
            double r;

            double nextValue = p.getEndValue() + 1;
            double simpleValue;

            while ((nextValue < p.getStartValue() || nextValue > p.getEndValue()) && (!support.isCancelEverything())) {
                //while(r<1){
                r = Math.random();
                r = 1 - (r * 2);
                r = r + 0.01;

                sign = Math.signum(r);

                //Calculation of Standard nextValue
                //nextValue = p.getValue() + sign * actualTempParameter *(Math.pow(1+(1/actualTempParameter),Math.abs(2*r-1) )) * distanceMax;
                nextValue = p.getValue() + sign * actualTempParameter * (Math.pow(1 + (1 / actualTempParameter), Math.abs(2 * r) - 1)) * distanceMax;
                support.log("Min:" + p.getStartValue() + " Max:" + p.getEndValue() + " NextValue:" + nextValue, typeOfLogLevel.INFO);
                /*
                 support.log("xCurrent:"+p.getValue());
                 support.log("sign:"+sign);
                 support.log("r:"+r);
                 support.log("Tpar:"+actualTempParameter);
                 support.log("Pow_Down:"+(1+(1/actualTempParameter)));
                 support.log("Pow_Up:"+(Math.abs(2*r)-1));
                 support.log("d:"+distanceMax);
                 support.log("NextValue:"+nextValue);
                 */
 /*try{
                 Thread.sleep(80);
                 }catch(Exception e){}
                 */

                //Calculation of simple nextValue
                double range = (p.getEndValue() - p.getStartValue());
                simpleValue = Math.round(Math.random() * range * actualTempParameter);

                switch (support.getOptimizerPreferences().getPref_CalculationOfNextParameterset(phase)) {
                    default:
                        //Do nothing
                        break;
                    case Stepwise:
                        nextValue = Math.round(nextValue / p.getStepping()) * p.getStepping();
                        break;
                    case Standard:
                        //Do nothing
                        break;
                    case Simple:
                        support.log("SimpleValue to add is " + simpleValue, typeOfLogLevel.INFO);
                        if (Math.random() >= 0.5) {
                            nextValue = p.getValue() + simpleValue;
                        } else {
                            nextValue = p.getValue() - simpleValue;
                        }
                        break;
                    case SimpleStepwise:
                        support.log("SimpleValue to add is " + simpleValue, typeOfLogLevel.INFO);

                        if (Math.random() >= 0.5) {
                            nextValue = p.getValue() + Math.round(simpleValue / p.getStepping()) * p.getStepping();
                        } else {
                            nextValue = p.getValue() - Math.round(simpleValue / p.getStepping()) * p.getStepping();
                        }
                        break;

                }

                //Normalize between min and max value
                nextValue = Math.min(nextValue, p.getEndValue());
                nextValue = Math.max(nextValue, p.getStartValue());

                support.log("Try to set value to: " + nextValue, typeOfLogLevel.INFO);
            }

            p.setValue(nextValue);
            support.log("Setting Parameter " + p.getName() + " to Value " + p.getValue() + ".", typeOfLogLevel.INFO);

        }

        //Log the Temperature-Data to a seperate file
        parameter parameterTempParameter = new parameter();
        parameterTempParameter.setName(typedef.listOfParametersToIgnore[0]);
        parameterTempParameter.setValue(actualTempParameter);

        parameter parameterCostParameter = new parameter();
        parameterCostParameter.setName(typedef.listOfParametersToIgnore[1]);
        parameterCostParameter.setValue(actualTempCost);

        ArrayList<parameter> dummyParameterset = new ArrayList<>();

        dummyParameterset.add(parameterCostParameter);
        dummyParameterset.add(parameterTempParameter);

        MeasureType dummyMeasure = new MeasureType();
        dummyMeasure.setMeasureName("SimAnnealingTemperature");
        ArrayList<MeasureType> dummyMeasureList = new ArrayList<>();

        dummyMeasureList.add(dummyMeasure);

        SimulationType dummySim = new SimulationType();
        dummySim.setListOfParameters(dummyParameterset);
        dummySim.setMeasures(dummyMeasureList);

        ArrayList<SimulationType> dummySimulationTypeList = new ArrayList<>();
        dummySimulationTypeList.add(dummySim);

        support.addLinesToLogFileFromListOfParser(dummySimulationTypeList, nameOfdummyLogfile);

        //End of logging the temperatures
        if (support.isCancelEverything()) {
            return null;
        }

        return newParameterset;
    }

}
