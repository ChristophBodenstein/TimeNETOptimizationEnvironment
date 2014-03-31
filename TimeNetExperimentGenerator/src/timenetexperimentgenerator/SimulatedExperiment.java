/*
 * A Simulated Experiment. Consists of List of Parameters + List of MeasureTypes
 */

package timenetexperimentgenerator;

import java.util.ArrayList;

/**
 *
 * @author Christoph Bodenstein
 */
public class SimulatedExperiment {
private ArrayList listOfParameters;
private ArrayList listOfMeasures;

    /**
     * @return the listOfParameters
     */
    public ArrayList getListOfParameters() {
        return listOfParameters;
    }

    /**
     * @param listOfParameters the listOfParameters to set
     */
    public void setListOfParameters(ArrayList listOfParameters) {
        this.listOfParameters = listOfParameters;
    }

    /**
     * @return the listOfMeasures
     */
    public ArrayList getListOfMeasures() {
        return listOfMeasures;
    }

    /**
     * @param listOfMeasures the listOfMeasures to set
     */
    public void setListOfMeasures(ArrayList listOfMeasures) {
        this.listOfMeasures = listOfMeasures;
    }



}
