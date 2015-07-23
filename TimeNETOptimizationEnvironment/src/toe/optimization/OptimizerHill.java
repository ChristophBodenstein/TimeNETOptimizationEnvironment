/*
 * Optimizer using the hill climbing algorithm, its a modified and configured version of simmulated annealing
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.optimization;

import toe.helper.StatisticAggregator;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import toe.MainFrame;
import toe.SimOptiFactory;
import toe.datamodel.MeasureType;
import toe.datamodel.parameter;
import toe.datamodel.SimulationType;
import toe.simulation.Simulator;
import toe.support;
import toe.typedef;

/**
 *
 * @author Christoph Bodenstein
 */
public class OptimizerHill implements Runnable, Optimizer {

    private final double sizeOfNeighborhood;
    private static final OptimizerPreferences myPreferences = support.getOptimizerPreferences();

    private int simulationCounter = 0;
    SimulationType currentSolution;
    SimulationType nextSolution;
    SimulationType bestSolution;
    String tmpPath = "";
    String filename = "";//Original filename
    String pathToTimeNet = "";
    MainFrame parent = null;
    JTabbedPane MeasureFormPane;
    ArrayList<MeasureType> listOfMeasures = new ArrayList<>();//Get List of all measures from MainFrame //Empty here
    ArrayList<SimulationType> historyOfParsers = new ArrayList<>();//History of all simulation runs
    ArrayList<parameter> parameterBase;//Base set of parameters, start/end-value, stepping, etc.
    boolean optimized = false;//False until Optimization is ended
    JLabel infoLabel;
    double simulationTimeSum = 0;
    double cpuTimeSum = 0;
    String logFileName;
    int wrongSolutionCounter = myPreferences.getPref_WrongSimulationsUntilBreak();
    int wrongSolutionPerDirectionCounter = myPreferences.getPref_WrongSimulationsPerDirection();
    boolean directionOfOptimization = true;//true->increment parameters, false->decrement parameters
    boolean directionOfOptimizationChanged = false;//True->direction already changed, False->you can change it one time
    int stuckInCacheCounter = support.DEFAULT_CACHE_STUCK;

    /**
     * Constructor
     *
     */
    public OptimizerHill() {
        this.sizeOfNeighborhood = myPreferences.getPref_SizeOfNeighborhood();
        logFileName = support.getTmpPath() + File.separator + this.getClass().getSimpleName() + "_" + Calendar.getInstance().getTimeInMillis() + myPreferences.getPref_LogFileAddon() + ".csv";
        support.log("LogfileName:" + logFileName);
        this.wrongSolutionCounter = myPreferences.getPref_WrongSimulationsUntilBreak();
        myPreferences.setVisible(false);
    }

    /**
     * Init the optimization, loads the default values and targets from
     * support-class and starts optimization
     *
     */
    @Override
    public void initOptimizer() {
        this.pathToTimeNet = support.getPathToTimeNet();// pathToTimeNetTMP;
        this.MeasureFormPane = support.getMeasureFormPane();//MeasureFormPaneTMP;
        this.parent = support.getMainFrame();// parentTMP;
        this.parameterBase = support.getParameterBase();
        support.setParameterBase(parameterBase);
        this.optimized = false;
        this.listOfMeasures = parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
        support.log("# of Measures to be optimized: " + this.listOfMeasures.size());

        this.filename = support.getOriginalFilename();// originalFilename;

        this.tmpPath = support.getTmpPath();
        //Start this Thread
        new Thread(this).start();
    }

    /**
     * Main Routine for Thread. The Optimization runs here
     */
    @Override
    public void run() {
        ArrayList<parameter> lastParameterset;
        ArrayList<ArrayList<parameter>> newParameterset;
        //Simulator init with initial parameterset
        Simulator mySimulator = SimOptiFactory.getSimulator();

        mySimulator.initSimulator(getParametersetAsArrayList(getFirstParameterset()), getSimulationCounter(), false);
        //Wait until Simulator has ended
        support.waitForEndOfSimulator(mySimulator, getSimulationCounter(), support.DEFAULT_TIMEOUT);
        support.addLinesToLogFileFromListOfParser(mySimulator.getListOfCompletedSimulationParsers(), logFileName);
        this.historyOfParsers = support.appendListOfParsers(historyOfParsers, mySimulator.getListOfCompletedSimulationParsers());
        currentSolution = mySimulator.getListOfCompletedSimulationParsers().get(0);
        nextSolution = currentSolution;
        bestSolution = currentSolution;
        ArrayList<SimulationType> listOfCompletedSimulations;

        lastParameterset = currentSolution.getListOfParametersFittedToBaseParameterset();

        support.log("Start of Optimization-loop");
        while (!optimized && !support.isCancelEverything()) {
            support.spinInLabel();

            newParameterset = getNextParametersetAsArrayList(lastParameterset);

            stuckInCacheCounter = support.DEFAULT_CACHE_STUCK;//Reset Stuck-Counter
            mySimulator.initSimulator(newParameterset, getSimulationCounter(), false);
            support.waitForEndOfSimulator(mySimulator, getSimulationCounter(), support.DEFAULT_TIMEOUT);
            this.setSimulationCounter(mySimulator.getSimulationCounter());
            listOfCompletedSimulations = mySimulator.getListOfCompletedSimulationParsers();
            support.log("List of Simulation results is: " + listOfCompletedSimulations.size() + " elements big.");
            //Shrink to first element of List
            listOfCompletedSimulations = support.shrinkArrayListToFirstMember(listOfCompletedSimulations);

            //Fit all resulting Simulation-Parameterlists
            for (SimulationType listOfCompletedSimulation : listOfCompletedSimulations) {
                listOfCompletedSimulation.setListOfParameters(listOfCompletedSimulation.getListOfParametersFittedToBaseParameterset());
            }

            if (listOfCompletedSimulations == null) {
                support.log("Error. List of completed Simulations is NULL!");
                return;
            }

            if (listOfCompletedSimulations.size() < 1) {
                support.log("Error. List of completed Simulations is 0. Will use last solution to provoke same simulation attempt.");
                nextSolution = currentSolution;
                listOfCompletedSimulations.add(currentSolution);
            } else {

                //TODO Only last Simulation is used. For future use we should use all Simulations to paralelise to simulation
                nextSolution = listOfCompletedSimulations.get(0);
            }

            support.addLinesToLogFileFromListOfParser(listOfCompletedSimulations, logFileName);
            this.historyOfParsers = support.appendListOfParsers(historyOfParsers, listOfCompletedSimulations);

            //Set the LastParameterset to be compared in next opti-loop
            lastParameterset = nextSolution.getListOfParametersFittedToBaseParameterset();

            if (stuckInCacheCounter >= 1) {
                //Check, if Optimization has ended!
                optimized = isOptimized(getActualDistance(currentSolution), getActualDistance(nextSolution));
            } else {
                //End Optimization because we are stuck in cache in the last parameterset
                optimized = true;
                currentSolution = bestSolution;
                support.log("End because we are stuck in temporary simulation cache.");
            }

        }
        support.log(this.getClass().getSimpleName() + " has ended, printing optimal value:");
        support.addLinesToLogFile(currentSolution, logFileName);
        support.setStatusText("Optimization ended. See Log.");
        support.printOptimizedMeasures(currentSolution, this.listOfMeasures);
        StatisticAggregator.printStatistic(this.logFileName);

        if (support.isCancelEverything()) {
            support.log("Optimization was canceled! Optimum might not found!");
        }

    }

    /**
     * Check, if next Solution is better. Calculate if Optimization is done or
     * not Reset wrongSolutionCounter if solution is better Reset
     * wrongSolutionPerDirectionCounter if solution is better Return true if
     * wrongSolutionCounter is down
     *
     * @param actualDistance actual (last) distance of measure
     * @param nextDistance new distance of measure (given by actual simulation)
     * @return true if optimized, else false
     */
    protected boolean isOptimized(double actualDistance, double nextDistance) {
        //If next Solution is better then take it as actual best solution
        if ((nextDistance < actualDistance)) {
            support.log("Choosing next solution for " + this.getClass().getSimpleName());
            currentSolution = nextSolution;//Set Global Solution Value
            bestSolution = currentSolution;//Set global best Solution Value
            //Reset wrong-solution-counter
            wrongSolutionCounter = myPreferences.getPref_WrongSimulationsUntilBreak();
            wrongSolutionPerDirectionCounter = myPreferences.getPref_WrongSimulationsPerDirection();
            return false;
        } else {
            nextSolution = null;
            //Count up the Solutions which are not taken
            //After X wrong solutions exit
            support.log("Distance was higher, Solution not chosen. Counting up wrong-solution-counter.");
            wrongSolutionCounter--;
            if (wrongSolutionCounter <= 1) {
                support.log("There were " + myPreferences.getPref_WrongSimulationsUntilBreak() + " wrong solutions. Assume optimum is already found.");
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     *
     * Returns the first Parameterset with respect to strategy of choosing
     *
     * @return ArrayList of parameter for start of optimization
     */
    private ArrayList<parameter> getFirstParameterset() {
        ArrayList<parameter> newParameterset = support.getCopyOfParameterSet(parameterBase);
        //calculate the first parameterset
        switch (myPreferences.getPref_StartValue()) {
            case start:
                support.log("Taking Min-Values as Start for every Parameter.");
                //Calculate first parameterset, set every parameter to start-value
                //For this choosing strategy, the first element must be minimum
                for (parameter p : newParameterset) {
                    if (p.isIteratableAndIntern()) {
                        p.setValue(p.getStartValue());
                    }
                }
                break;
            case middle:
                support.log("Taking Middle-Values as Start for every Parameter.");
                //Calulate first parameterset, the mean value of all parameters, with respect to stepping
                for (parameter p : newParameterset) {
                    if (p.isIteratableAndIntern()) {
                        double distance = p.getEndValue() - p.getStartValue();
                        distance = Math.round(0.5 * distance / p.getStepping()) * p.getStepping() + p.getStartValue();
                        p.setValue(distance);
                    }
                }
                break;
            case end:
                support.log("Taking Max-Values as Start for every Parameter.");
                //Calculate first parameterset, set every parameter to end-value
                //For this choosing strategy, the first element must be minimum
                for (parameter p : newParameterset) {
                    if (p.isIteratableAndIntern()) {
                        p.setValue(p.getEndValue());
                    }
                }
                break;
            case random:
                support.log("Taking Random-Values as Start for every Parameter.");
                //Calulate first parameterset, the random value of all parameters, with respect to stepping
                for (parameter p : newParameterset) {
                    if (p.isIteratableAndIntern()) {
                        double distance = p.getEndValue() - p.getStartValue();
                        double rnd = Math.random();
                        distance = Math.round(rnd * distance / p.getStepping()) * p.getStepping() + p.getStartValue();
                        p.setValue(distance);
                    }
                }
                break;
            case preset:
                //Nothing to to, Value is already set to the preferred start-Value
                //But let`s make sure, set it to middle if something is wrong
                for (parameter p : newParameterset) {
                    if (p.isIteratableAndIntern()) {
                        if ((p.getValue() < p.getStartValue()) || (p.getValue() > p.getEndValue())) {
                            double distance = p.getEndValue() - p.getStartValue();
                            distance = Math.round(0.5 * distance / p.getStepping()) * p.getStepping() + p.getStartValue();
                            p.setValue(distance);
                        }
                    }
                }

                break;

        }
        return newParameterset;
    }

    /**
     * Returns the next parameterset in neighborhood Next parameterset is chosen
     * based on the used strategy You should overload this method in your
     * child-classes
     *
     * @param actualParameterset actual parameterset, if null, then first
     * parameterset is calculated
     * @return next parameterset to be simulated
     */
    protected ArrayList<parameter> getNextParameterset(ArrayList<parameter> actualParameterset) {
        ArrayList<parameter> newParameterset = support.getCopyOfParameterSet(parameterBase);
        ArrayList<parameter> listOfChangableParameters = support.getListOfChangableParameters(newParameterset);
        //Count the number of changable parameters
        //this.numberOfChangableParameters=listOfChangableParameters.size();

        if (actualParameterset == null) {
            //Return the inital parameterset for optimization, based on startvalue-strategy
            return getFirstParameterset();
        } else {
            //First parameterset exists, calculate the next
            newParameterset = support.getCopyOfParameterSet(actualParameterset);

            //Eject, if no parameter can be changed (important in Multiphase-Opti)
            if (listOfChangableParameters.size() <= 0) {
                return actualParameterset;
            }

            int numberOfParameterToBeChanged = 0;//Default, change the first changeable parameter
            int numberOfLastParameter = -1;//Number of last parameter that was changed(in an Array of changable parameters)
            //Check, which parameters can be changed
            if (listOfChangableParameters.size() > 1) {
                SimulationType lastParser = currentSolution;

                ArrayList<parameter> lastParameterList = lastParser.getListOfParameters();

                //Don't Sort the parameterlist!
                //Collections.sort(lastParameterList);
                support.log("Number of Parameters in List: " + lastParameterList.size());

                //For every Parameter check if it is iteratable and if it was changed last time
                int i;
                numberOfLastParameter = -1;//Number of last parameter that was changed(in an Array of changable parameters)
                for (i = 0; i < lastParameterList.size(); i++) {
                    if (lastParameterList.get(i).isIteratableAndIntern()) {
                        numberOfLastParameter++;
                        support.log("Iteratable Parameter with number " + numberOfLastParameter + " found.");
                        /*if it was changed, then break, and numberOfLastParameter contains the number of last changed parameter in array of changable parameters*/
                        if (lastParameterList.get(i) != actualParameterset.get(i)) {
                            break;
                        }
                    }

                }
                //It numberOfLastParameter is -1 we set it to 0, this is in multiphase-opti needed
                numberOfLastParameter = Math.max(numberOfLastParameter, 0);
                // At this point, numberOfLastParameter contains the number of last changed parameter in an array of all changeable parameters
                support.log("Number of Last changed Parameter is " + numberOfLastParameter);

                if (nextSolution == null) {
                    support.log("Last Solution was not better then overlast solution. Counting up Wrong Solutions in one direction.");
                    wrongSolutionPerDirectionCounter--;
                }

                if (wrongSolutionPerDirectionCounter <= 0) {
                    //Number of maximum wrong solutions per direction/parameter reached
                    //-->first change the direction, if already changed, choose next parameter
                    wrongSolutionPerDirectionCounter = myPreferences.getPref_WrongSimulationsPerDirection();

                    support.log(myPreferences.getPref_WrongSimulationsPerDirection() + " wrong solutions in one direction.");
                    if (this.directionOfOptimization && myPreferences.getPref_NeighborhoodType() == typedef.typeOfNeighborhoodEnum.StepForwardBackRandom) {
                        //Switch direction of Optimization but change the same old parameter
                        //This only applies if StepForwardBackward

                        support.log("Changing direction of Optimization to false(backwards).");
                        this.directionOfOptimization = false;
                        numberOfParameterToBeChanged = numberOfLastParameter;
                        //Exchange whole parameterset by the last best knwon solution
                        newParameterset = support.getCopyOfParameterSet(this.bestSolution.getListOfParameters());
                    } else {
                        support.log("Changing direction of Optimization back to true(forward). Taking next parameter to change.");
                        this.directionOfOptimization = true;
                        //newParameterset=currentSolution.getListOfParameters();
                        //Exchange whole parameterset by the last best knwon solution
                        newParameterset = support.getCopyOfParameterSet(this.bestSolution.getListOfParameters());

                        //Select next Parameter to be changed with round-robin
                        numberOfParameterToBeChanged = numberOfLastParameter + 1;
                        if (numberOfParameterToBeChanged >= listOfChangableParameters.size()) {
                            numberOfParameterToBeChanged = 0;

                            //Reset the wrong solution-counter
                            this.wrongSolutionPerDirectionCounter = myPreferences.getPref_WrongSimulationsPerDirection();

                        }
                        support.log("Last changed Parameter was: " + numberOfLastParameter + ", next Parameter to be changed is " + numberOfParameterToBeChanged);
                        support.log("There are " + listOfChangableParameters.size() + " parameters in list to be changed.");
                    }
                } else {
                    //Select old parameter to be changed again
                    support.log("Changing parameter " + numberOfLastParameter);
                    numberOfParameterToBeChanged = numberOfLastParameter;
                }

            } else {
                numberOfParameterToBeChanged = 0;
            }

            //Select new Parameter to be changed and change it!
            //Get Parameter by name
            String nameOfParameterToBeChanged = listOfChangableParameters.get(numberOfParameterToBeChanged).getName();
            boolean incResult = false;
            support.log("Number of Parameter to be changed " + numberOfParameterToBeChanged);
            support.log("Name of Parameter to be changed: " + nameOfParameterToBeChanged);
            switch (myPreferences.getPref_NeighborhoodType()) {
                case StepForward://0 choose the next neighbor based on stepping forward
                    //Inc this parameter by standard-increment
                    incResult = support.getParameterByName(newParameterset, nameOfParameterToBeChanged).incValue();
                    break;

                case StepForwardBackward://0 choose the next neighbor based on stepping forward or backward
                    //Inc or Dec this parameter by standard-increment
                    incResult = support.getParameterByName(newParameterset, nameOfParameterToBeChanged).incDecValue(this.directionOfOptimization);
                    break;

                case StepForwardBackRandom: //Step back and forward randomly based on stepping
                    for (parameter p : newParameterset) {
                        if (p.isIteratableAndIntern()) {
                            double nextValue = 0.0;
                            if (Math.random() >= 0.5) {
                                incResult = support.getParameterByName(newParameterset, nameOfParameterToBeChanged).incValue();
                            } else {
                                incResult = support.getParameterByName(newParameterset, nameOfParameterToBeChanged).decValue();
                            }
                            //p.setValue(nextValue);
                        }
                    }
                    break;
                case RandomStepInNeighborhood: //Calculate neighborhood and choose next value randomly
                    for (parameter p : newParameterset) {
                        if (p.isIteratableAndIntern()) {
                            double stepCount = (p.getEndValue() - p.getStartValue()) / p.getStepping();
                            double nextValue = p.getStepping() * Math.round(Math.random() * stepCount * this.sizeOfNeighborhood / 100);
                            if (Math.random() >= 0.5) {
                                nextValue = Math.min(p.getValue() + nextValue, p.getEndValue());
                            } else {
                                nextValue = Math.max(p.getValue() - nextValue, p.getStartValue());
                            }
                            p.setValue(nextValue);
                        }
                    }
                    incResult = true;
                    break;
                case RandomStepInDesignspace: //Choose Value randomly out of complete designspace
                    for (parameter p : newParameterset) {
                        if (p.isIteratableAndIntern()) {
                            double stepCount = (p.getEndValue() - p.getStartValue()) / p.getStepping();
                            double nextValue = p.getStartValue() + Math.round(Math.random() * stepCount);
                            p.setValue(nextValue);
                        }
                    }
                    incResult = true;
                    break;
                case RandomSteplessInNeighborhood: //Calculate neighborhood and choose next value randomly, Ignore Stepping!
                    for (parameter p : newParameterset) {
                        if (p.isIteratableAndIntern()) {
                            double range = (p.getEndValue() - p.getStartValue());
                            double nextValue = Math.round(Math.random() * range * this.sizeOfNeighborhood / 100);
                            if (Math.random() >= 0.5) {
                                nextValue = Math.min(p.getValue() + nextValue, p.getEndValue());
                            } else {
                                nextValue = Math.max(p.getValue() - nextValue, p.getStartValue());
                            }
                            p.setValue(nextValue);
                        }
                    }
                    incResult = true;
                    break;
                default:
                    //Dont change the parameterset
                    break;

            }

            switch (myPreferences.getPref_NeighborhoodType()) {
                case StepForward:
                case StepForwardBackward:
                    parameter p = support.getParameterByName(newParameterset, nameOfParameterToBeChanged);
                    if (incResult) {
                        support.log("Parameter " + p.getName() + " could be incremented.(or decremented)");
                    } else {
                        support.log("Parameter " + p.getName() + " could NOT be incremented.(or decremented). Set WrongSolutionsPerDirectionCounter to 0 to change the direction next time.");
                        wrongSolutionPerDirectionCounter = 0;
                    }
                    break;
                default:
                    break;

            }

            return newParameterset;
        }

    }

    /**
     * Wrapper, returns next Parameterset as ArrayList with one member
     *
     * @param actualParameterset Base Parameterset to calculate the next one
     * @return ArrayList of Parametersets
     */
    private ArrayList< ArrayList<parameter>> getNextParametersetAsArrayList(ArrayList<parameter> actualParameterset) {
        ArrayList< ArrayList<parameter>> myParametersetList = new ArrayList<>();
        myParametersetList.add(getNextParameterset(actualParameterset));
        return myParametersetList;
    }

    /**
     * Wrapper, returns ArrayList of ArrayList of Parameters
     */
    private ArrayList< ArrayList<parameter>> getParametersetAsArrayList(ArrayList<parameter> actualParameterset) {
        ArrayList< ArrayList<parameter>> myParametersetList = new ArrayList<>();
        myParametersetList.add(actualParameterset);
        return myParametersetList;
    }

    /**
     * Returns the fitness value of actual Paremeterset/Measure Sums up all
     * distances from Measures
     *
     * @param p actual simulation incl. results to calculate the distance of
     * measures
     * @return distance (Fx)
     */
    protected double getActualDistance(SimulationType p) {
        double distance = 0;
        for (MeasureType listOfMeasure : listOfMeasures) {
            MeasureType activeMeasure = p.getMeasureByName(listOfMeasure.getMeasureName());
            MeasureType activeMeasureFromInterface = listOfMeasure; //Contains Optimization targets
            activeMeasure.setTargetValue(activeMeasureFromInterface.getTargetValue(), activeMeasureFromInterface.getTargetTypeOf());
            if (activeMeasure.getTargetTypeOf().equals(typedef.typeOfTarget.value)) {
                distance = activeMeasure.getDistanceFromTarget();
            } else {
                if (activeMeasure.getTargetTypeOf().equals(typedef.typeOfTarget.min)) {
                    distance = activeMeasure.getMeanValue();
                } else {
                    if (activeMeasure.getTargetTypeOf().equals(typedef.typeOfTarget.max)) {
                        distance = 0 - activeMeasure.getMeanValue();
                    }
                }
            }
        }
        return distance;
    }

    /**
     * @return the simulationCounter
     */
    public int getSimulationCounter() {
        return support.getGlobalSimulationCounter();// simulationCounter;
    }

    /**
     * @param simulationCounter the simulationCounter to set
     */
    public void setSimulationCounter(int simulationCounter) {
        this.simulationCounter = simulationCounter;
    }

    /**
     * Returns the found optmium (SimulationType) If optimum is not found or
     * optimizer is still running it returns null
     *
     * @return null if optimization not yet ended, else Optimum
     */
    @Override
    public SimulationType getOptimum() {
        if (this.optimized) {
            support.log("Its optimized, so returning best solution.");
            return this.bestSolution;
        } else {
            //support.log("Its NOT optimized, returning null.");
            return null;
        }
    }

    /**
     * Set the logfilename this is useful for multi-optimization or if you like
     * specific names for your logfiles
     *
     * @param name Name (path) of logfile
     */
    @Override
    public void setLogFileName(String name) {
        this.logFileName = name;
    }

    /**
     * Returns the used logfileName
     *
     * @return name of logfile
     */
    @Override
    public String getLogFileName() {
        return this.logFileName;
    }
}
