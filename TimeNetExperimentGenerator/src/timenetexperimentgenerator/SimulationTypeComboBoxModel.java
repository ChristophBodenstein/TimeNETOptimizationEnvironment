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

            if (anObject.toString().equals("Local Sim.")) {

                super.setSelectedItem(anObject);

            }
            
            if ((anObject.toString().equals("Cached Sim."))&&(support.isCachedSimulationEnabled())) {
                super.setSelectedItem(anObject);
            }

        } else {

            //super.setSelectedItem(anObject);

        }

    } 
}
