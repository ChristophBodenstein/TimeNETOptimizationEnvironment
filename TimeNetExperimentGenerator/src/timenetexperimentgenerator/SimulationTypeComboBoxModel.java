/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator;

import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author sse
 */
public class SimulationTypeComboBoxModel extends DefaultComboBoxModel{
   @Override
    public void setSelectedItem(Object anObject) {

        if (anObject != null) {

            
            if (anObject.toString().equals(support.SIMTYPES[0])) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType(0);
            }
            
            if ((anObject.toString().equals(support.SIMTYPES[1]))&&(support.isCachedSimulationAvailable())) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType(1);
            }
            
            if ((anObject.toString().equals(support.SIMTYPES[2]))&&(support.isDistributedSimulationAvailable() )) {
                super.setSelectedItem(anObject);
                support.setChosenSimulatorType(2);
            }
            

        } else {

            //super.setSelectedItem(anObject);

        }

    } 
}
