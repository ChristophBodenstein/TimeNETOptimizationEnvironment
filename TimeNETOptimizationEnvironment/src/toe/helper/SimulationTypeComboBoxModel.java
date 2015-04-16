/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toe.helper;

import javax.swing.DefaultComboBoxModel;
import toe.support;
import toe.typedef.typeOfSimulator;

/**
 *
 * @author sse
 */
public class SimulationTypeComboBoxModel extends DefaultComboBoxModel {

    /**
     * Constructor
     *
     * @param values Array of Values to be used in combobox
     */
    public SimulationTypeComboBoxModel(typeOfSimulator[] values) {
        super(values);
    }

    /**
     * Sets the selected object. Is called when user changes the value of
     * combobox. Here cheacks are made if the chosen object can really be chosen
     * (e.g. if cache is available)
     *
     * @param anObject Selected object. Will be checked if possible to select
     */
    @Override
    public void setSelectedItem(Object anObject) {
        //Here is defined, what kind of simulation is possible
        if (anObject != null) {

            if ((anObject).equals(typeOfSimulator.Local)) {//  toString().equals(support.SIMTYPES[0])) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator) anObject);
            }

            if (((anObject).equals(typeOfSimulator.Cache_Only)) && (support.isCachedSimulationAvailable())) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator) anObject);
            }

            if ((anObject).equals(typeOfSimulator.Cached_Local)) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator) anObject);
            }
            if (((anObject).equals(typeOfSimulator.Distributed)) && (support.isDistributedSimulationAvailable())) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator) anObject);
            }
            if (((anObject).equals(typeOfSimulator.Cached_Distributed)) && (support.isDistributedSimulationAvailable())) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator) anObject);
            }

            if ((anObject).equals(typeOfSimulator.Benchmark)) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator) anObject);
            }
            if ((anObject).equals(typeOfSimulator.Cached_Benchmark)) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator) anObject);
            }

        } else {

            //super.setSelectedItem(anObject);
        }

    }
}
