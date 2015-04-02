/*
 * Optimization-Algorithm implemented bei Andy Seidel during Diploma Thesis 2014
 *
 * Mean-Variance-Mapping-Optimization
 * Original Paper: Erlich, Ganesh, Worawat ; 2010:
 * A Mean-Variance Optimization Algorithm
 */
package toe.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import toe.SimOptiFactory;
import toe.datamodel.SimulationType;
import toe.datamodel.parameter;
import toe.simulation.Simulator;
import toe.support;
import toe.typedef.typeOfMVMOMutationSelection;
import toe.typedef.typeOfMVMOParentSelection;

/**
 *
 * @author A. Seidel
 */
public class OptimizerMVMO extends OptimizerPopulationBased implements Runnable, Optimizer {

    private final int startPopulationSize = support.getOptimizerPreferences().getPref_MVMO_StartingPop(); //size of population after initialization
    private final int maxPopulationSize = support.getOptimizerPreferences().getPref_MVMO_MaxPop(); //maximum Population size

    private ArrayList<Double> parameterMeanValues;
    private ArrayList<Double> parameterVarianceValues;

    private final double scalingFactor = 10.0;
    private final double asymmetryFactor = 3.0;

    private double sd = 75.0;

    /**
     * return maximum number of optimization cycles before breaking up
     *
     * @return the current maximum number of optimization cycles
     */
    public int getMaxNumberOfOptiCycles() {
        return this.maxNumberOfOptiCycles;
    }

    /**
     * sets maximum number of optimization cycles. Has to be at least 1,
     * otherwise it is ignored.
     *
     * @param newMaxNumberOfOtpiCycles the new maximum number of optimization
     * cycles
     */
    public void setMaxNumberOfOptiCycles(int newMaxNumberOfOtpiCycles) {
        if (newMaxNumberOfOtpiCycles > 0) {
            this.maxNumberOfOptiCycles = newMaxNumberOfOtpiCycles;
        }
    }

    /**
     * gets maximum number of optimization cycles without improvement, before
     * breaking optimization loop.
     *
     * @return the maximum number of optimization cycles without improvemet
     */
    public int getMaxNumberOfOptiCyclesWithoutImprovement() {
        return this.maxNumberOfOptiCyclesWithoutImprovement;
    }

    /**
     * sets maximum number of optimization cycles without improvement. Has to be
     * at least 1, otherwise it is ignored.
     *
     * @param newMaxNumberOfOptiCyclesWithoutImprovement the new maximum number
     * of optimization cycles without improvement
     */
    public void setMaxNumberOfOptiCyclesWithoutImprovement(int newMaxNumberOfOptiCyclesWithoutImprovement) {
        if (newMaxNumberOfOptiCyclesWithoutImprovement > 0) {
            this.maxNumberOfOptiCyclesWithoutImprovement = newMaxNumberOfOptiCyclesWithoutImprovement;
        }
    }
    
    /**
     * Contructor
     */
    public OptimizerMVMO() {
        logFileName = support.getTmpPath() + File.separator + "Optimizing_with_Genetic_Algorithm_" + Calendar.getInstance().getTimeInMillis() + "_ALL" + ".csv";
    }

    @Override
    public void run() {
        int optiCycleCounter = 0;
        population = createRandomPopulation(startPopulationSize, false);

        Simulator mySimulator = SimOptiFactory.getSimulator();
        mySimulator.initSimulator(getNextParameterSetAsArrayList(), optiCycleCounter, false);
        support.waitForEndOfSimulator(mySimulator, optiCycleCounter, support.DEFAULT_TIMEOUT);

        ArrayList<SimulationType> simulationResults = mySimulator.getListOfCompletedSimulationParsers();
        population = getPopulationFromSimulationResults(simulationResults);
        population = normalize(population);

        int simulationCounter = 0;
        int lastGenSelected = 0;
        int numGenesToSelect = 1;
        while (optiCycleCounter < this.maxNumberOfOptiCycles) {
            if (currentNumberOfOptiCyclesWithoutImprovement >= maxNumberOfOptiCyclesWithoutImprovement) {
                support.log("Too many optimization cycles without improvement. Ending optimization.");
                break;
            }

            //modification phase -----------------------------------------------------------------------
            population = sortPopulation(population); //sort them, so the best one is number one
            parameterMeanValues = getMeanValues(population);
            parameterVarianceValues = getVarianceValues(population);
            SimulationType candidate = createCandidate(
                    population,
                    parameterMeanValues,
                    parameterVarianceValues,
                    typeOfMVMOParentSelection.Best,
                    typeOfMVMOMutationSelection.Random,
                    lastGenSelected, numGenesToSelect);

            //simulation phase -----------------------------------------------------------------------
            candidate = denormalize(candidate);
            ArrayList< ArrayList<parameter>> parameterList = getNextParameterSetAsArrayList(candidate);
            for (ArrayList<parameter> pArray : parameterList) {
                pArray = roundToStepping(pArray);
            }

            mySimulator.initSimulator(parameterList, simulationCounter, false);
            support.waitForEndOfSimulator(mySimulator, simulationCounter, support.DEFAULT_TIMEOUT);
            simulationCounter = mySimulator.getSimulationCounter();

            simulationResults = mySimulator.getListOfCompletedSimulationParsers();
            support.addLinesToLogFileFromListOfParser(simulationResults, logFileName);
            population = tryAddCandidate(population, simulationResults);

            if (population.size() == maxPopulationSize) {
                updateTopMeasure(); //TODO: not optimal for mvmo, because the first one is alway the best
                printPopulationDistances();
            }

            ++optiCycleCounter;
        }
        this.optimized = true;

    }

    private ArrayList< ArrayList<SimulationType>> tryAddCandidate(ArrayList< ArrayList<SimulationType>> population, ArrayList<SimulationType> candidate) {
        candidate.set(0, normalize(candidate.get(0)));
        if (population.size() < maxPopulationSize) {
            population.add(candidate);
            return population;
        }
        if (population.get(population.size() - 1).get(0).getDistanceToTargetValue() >= candidate.get(0).getDistanceToTargetValue()) {
            support.log("Added new one to population");
            population.set(population.size() - 1, candidate);
            population = sortPopulation(population);
        }
        return population;
    }

    private ArrayList<Double> getMeanValues(ArrayList<ArrayList<SimulationType>> population) {
        ArrayList<Double> meanValues = new ArrayList<>();
        int numParameters = population.get(0).get(0).getListOfParameters().size();

        for (int parameterIndex = 0; parameterIndex < numParameters; ++parameterIndex) {
            double sum = 0;
            for (int populationIndex = 0; populationIndex < population.size(); ++populationIndex) {
                try //TODO: BUG after simulation sometimes there are more parameters than before
                {
                    sum += population.get(populationIndex).get(0).getListOfParameters().get(parameterIndex).getValue();
                } catch (IndexOutOfBoundsException e) {
                    support.log(e.getMessage());
                }
            }
            meanValues.add(sum / population.size());
        }
        return meanValues;
    }

    private ArrayList<Double> getVarianceValues(ArrayList<ArrayList<SimulationType>> population) {
        ArrayList<Double> varianceValues = new ArrayList<>();
        int numParameters = population.get(0).get(0).getListOfParameters().size();
        for (int parameterIndex = 0; parameterIndex < numParameters; ++parameterIndex) {
            double sum = 0;
            for (int populationIndex = 0; populationIndex < population.size(); ++populationIndex) {
                try //TODO: BUG after simulation sometimes there are more parameters than before
                {
                    double value = population.get(populationIndex).get(0).getListOfParameters().get(parameterIndex).getValue();
                    sum += (value - parameterMeanValues.get(parameterIndex)) * (value - parameterMeanValues.get(parameterIndex));
                } catch (IndexOutOfBoundsException e) {
                    support.log(e.getMessage());
                }

            }
            varianceValues.add(sum / population.size());
        }
        return varianceValues;
    }

    private SimulationType createCandidate(
            ArrayList<ArrayList<SimulationType>> population,
            ArrayList<Double> parameterMeanValues,
            ArrayList<Double> parameterVarianceValues,
            typeOfMVMOParentSelection parentSelection,
            typeOfMVMOMutationSelection mutationSelection,
            int startingIndex,
            int numGenesToMutate) {
        SimulationType candidate = new SimulationType();
        //parent selection
        if (parentSelection == typeOfMVMOParentSelection.Best) {
            candidate = new SimulationType(population.get(0).get(0));
        }

        //select genes to mutate
        ArrayList<Integer> genesToMutate = new ArrayList<>();
        if (mutationSelection == typeOfMVMOMutationSelection.Random) {
            genesToMutate = fillWithRandomIndices(genesToMutate, numGenesToMutate, candidate.getListOfParameters());
        } else if (mutationSelection == typeOfMVMOMutationSelection.RandomWithMovingSingle) {
            genesToMutate.add(startingIndex);
            genesToMutate = fillWithRandomIndices(genesToMutate, numGenesToMutate, candidate.getListOfParameters());
        } else if (mutationSelection == typeOfMVMOMutationSelection.MovingGroupSingleStep
                || mutationSelection == typeOfMVMOMutationSelection.MovingGroupMultiStep) {
            for (int i = 0; i < numGenesToMutate; ++i) {
                genesToMutate.add((startingIndex + i) % population.size());
            }
        }

        //mutation
        double si;
        double si1;
        double si2;
        double mean;
        double variance;
        double kd = 0.0505 / candidate.getListOfParameters().size() + 1;
        for (int i = 0; i < genesToMutate.size(); ++i) {
            //support.log("Try to mutate gen nr: " + genesToMutate.get(i));
            parameter p = candidate.getListOfParameters().get(genesToMutate.get(i));
            if (p.isIteratableAndIntern()) {
                mean = parameterMeanValues.get(genesToMutate.get(i));
                variance = parameterVarianceValues.get(genesToMutate.get(i));
                si = -1.0 * Math.log(variance) * scalingFactor;
                if (variance == 0) {
                    if (si < sd) {
                        sd = sd * kd;
                    } else if (si > sd) {
                        sd = sd / kd;
                    }
                    si = sd;
                }

                if (p.getValue() < mean) {
                    si1 = si;
                    si2 = si * asymmetryFactor;
                } else if (p.getValue() > mean) {
                    si1 = si * asymmetryFactor;
                    si2 = si;
                } else {
                    si1 = si;
                    si2 = si;
                }

                double xTemp = randomGenerator.nextDouble();
                //support.log("Temp x: " + xTemp);
                //support.log("Mean: " + mean);
                //support.log("Variance " + variance);
                //support.log("Si: " + si);
                double x = transform(mean, si1, si2, xTemp)
                        + (1 - transform(mean, si1, si2, 1) + transform(mean, si1, si2, 0)) * xTemp
                        - transform(mean, si1, si2, 0);
                if (x < 0) {
                    x = 0;
                    support.log("Transformation value too low, forced to 0");
                } else if (x > 1) {
                    x = 1;
                    support.log("Transformation value too high, forced to 1");
                }
                //support.log("x: " + x);
                p.setValue(x);
            }
        }
        return candidate;
    }

    private double transform(double x_mean, double si1, double si2, double ui) {
        double r;
        r = x_mean * (1 - Math.exp(-1.0 * si1 * ui)) + (1 - x_mean) * Math.exp((1 - ui) * si2 * (-1));
        return r;
    }

    private ArrayList<Integer> fillWithRandomIndices(
            ArrayList<Integer> list, int targetSize, ArrayList<parameter> parameters) {
        ArrayList<Integer> possibleGens = new ArrayList<>();
        for (int i = 0; i < parameters.size(); ++i) {
            if (parameters.get(i).isIteratableAndIntern() && !list.contains(i)) {
                possibleGens.add(i);
            }
        }
        while (list.size() < targetSize) {
            if (possibleGens.isEmpty()) {
                break;
            }
            int randomSelection = randomGenerator.nextInt(possibleGens.size());
            list.add(possibleGens.get(randomSelection));
            possibleGens.remove(randomSelection);

        }
        return list;
    }

    private ArrayList< ArrayList<SimulationType>> normalize(ArrayList< ArrayList<SimulationType>> list) {
        for (int i = 0; i < list.size(); ++i) {
            normalize(list.get(i).get(0));
        }
        return list;
    }

    private SimulationType normalize(SimulationType simulation) {
        ArrayList<parameter> paraList = simulation.getListOfParameters();
        for (int paraNum = 0; paraNum < paraList.size(); ++paraNum) {
            parameter p = paraList.get(paraNum);
            //support.log("Normalizing: " + p.getValue() + "\tStart: " + p.getStartValue() + "\tEnd: " + p.getEndValue());
            double newValue = (p.getValue() - p.getStartValue()) / (p.getEndValue() - p.getStartValue());
            //support.log("Normalised to " +  newValue);
            p.setValue(newValue);
        }
        return simulation;
    }

    private ArrayList< ArrayList<SimulationType>> denormalize(ArrayList< ArrayList<SimulationType>> list) {
        for (int i = 0; i < list.size(); ++i) {
            //SimulationType simulation = list.get(i).get(0);
            //simulation = denormalize(simulation);
            denormalize(list.get(i).get(0));
        }
        return list;
    }

    private SimulationType denormalize(SimulationType simulation) {
        ArrayList<parameter> paraList = simulation.getListOfParameters();
        for (int paraNum = 0; paraNum < paraList.size(); ++paraNum) {
            parameter p = paraList.get(paraNum);
            //support.log("Denormalizing: " + p.getValue() + "\tStart: " + p.getStartValue() + "\tEnd: " + p.getEndValue());
            double newValue = p.getValue() * (p.getEndValue() - p.getStartValue()) + p.getStartValue();
            //support.log("Denormalised to " +  newValue);
            p.setValue(newValue);
        }
        return simulation;
    }

}
