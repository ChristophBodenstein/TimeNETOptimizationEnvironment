/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator.helper;

import javax.swing.DefaultComboBoxModel;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.typedef.typeOfSimulator;

/**
 *
 * @author sse
 */
public class SimulationTypeComboBoxModel extends DefaultComboBoxModel{

    public SimulationTypeComboBoxModel(typeOfSimulator[] values) {
        super(values);
    }
   @Override
    public void setSelectedItem(Object anObject) {
        //Here is defined, what kind of simulation is possible
        if (anObject != null) {

            
            if ((anObject).equals(typeOfSimulator.Local.toString())){//  toString().equals(support.SIMTYPES[0])) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator)anObject);
            }
            
            if ( ((anObject).equals(typeOfSimulator.Cache_Only.toString()) )&&(support.isCachedSimulationAvailable())) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator)anObject);
            }
            
            if ((anObject).equals(typeOfSimulator.Cached_Local.toString())) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator)anObject);
            }
            if ( ((anObject).equals(typeOfSimulator.Distributed.toString()) )&&(support.isDistributedSimulationAvailable() )) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType((typeOfSimulator)anObject);
            }
            

        } else {

            //super.setSelectedItem(anObject);

        }

    } 
}
