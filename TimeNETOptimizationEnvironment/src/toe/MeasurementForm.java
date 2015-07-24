/*
 * Form for Optimization-targets, its used within a tabbed pane to choose the target-measurements
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe;

import toe.datamodel.MeasureType;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import toe.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public final class MeasurementForm extends javax.swing.JPanel {

    private ArrayList<MeasureType> listOfMeasureMents = new ArrayList<MeasureType>();

    /**
     * Creates new form MeasurementForm
     */
    public MeasurementForm() {
        initComponents();
        this.setActivated(true);
        //Remove next two lines if multiple Measurements should be enabled again.
        //Meanwhile this feature is disabled
        this.jCheckBoxEnableOptimizationForThisMeasurement.setSelected(true);
        this.jCheckBoxEnableOptimizationForThisMeasurement.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBoxMeasurementName = new javax.swing.JComboBox();
        jComboBoxOptimizationTarget = new javax.swing.JComboBox();
        jTextFieldCustomTargetValue = new javax.swing.JTextField();
        jCheckBoxEnableOptimizationForThisMeasurement = new javax.swing.JCheckBox();

        jComboBoxMeasurementName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Measure available!" }));
        jComboBoxMeasurementName.setToolTipText("Chose Measurement to optimize");

        jComboBoxOptimizationTarget.setModel(new DefaultComboBoxModel(typeOfTarget.values()));
        jComboBoxOptimizationTarget.setToolTipText("Chose Optimization Target Value");
        jComboBoxOptimizationTarget.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxOptimizationTargetItemStateChanged(evt);
            }
        });

        jTextFieldCustomTargetValue.setText("0");
        jTextFieldCustomTargetValue.setToolTipText("Enter Custom Target Value for Measurement");

        jCheckBoxEnableOptimizationForThisMeasurement.setToolTipText("Enable to optimize for this Measurement");
        jCheckBoxEnableOptimizationForThisMeasurement.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxEnableOptimizationForThisMeasurementItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBoxOptimizationTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldCustomTargetValue, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                    .addComponent(jComboBoxMeasurementName, 0, 204, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxEnableOptimizationForThisMeasurement)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jComboBoxMeasurementName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldCustomTargetValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxOptimizationTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBoxEnableOptimizationForThisMeasurement)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxEnableOptimizationForThisMeasurementItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxEnableOptimizationForThisMeasurementItemStateChanged
        this.setActivated(this.jCheckBoxEnableOptimizationForThisMeasurement.isSelected());
    }//GEN-LAST:event_jCheckBoxEnableOptimizationForThisMeasurementItemStateChanged

    private void jComboBoxOptimizationTargetItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxOptimizationTargetItemStateChanged
        this.jTextFieldCustomTargetValue.setEnabled(this.getOptimizationTarget().equals("value"));
    }//GEN-LAST:event_jComboBoxOptimizationTargetItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxEnableOptimizationForThisMeasurement;
    private javax.swing.JComboBox jComboBoxMeasurementName;
    private javax.swing.JComboBox jComboBoxOptimizationTarget;
    private javax.swing.JTextField jTextFieldCustomTargetValue;
    // End of variables declaration//GEN-END:variables

    /**
     * Return Name of the chosen measurement for optimization
     *
     * @return Name of Measurement which was chosen for optimization
     */
    public String getNameOfChosenMeasurement() {
        return (String) this.jComboBoxMeasurementName.getSelectedItem();
    }

    /**
     * Return chosen Measurement to be optimized
     *
     * @return measurement to be optimized
     */
    public MeasureType getChosenMeasurement() {
        //ArrayList<MeasureType> myExportMeasureList=new ArrayList<MeasureType>();
        for (MeasureType listOfMeasureMent : this.listOfMeasureMents) {
            if (this.getNameOfChosenMeasurement().equals(listOfMeasureMent.getMeasureName())) {
                return listOfMeasureMent;
            }
        }
        support.log("***Error: No MeasureMent chosen, wil return null!");
        return null;
    }

    /**
     * Return target value, given by user in JTextField
     *
     * @return target value for measurement
     */
    public double getCustomTargetValue() {
        double returnValue;
        returnValue = support.getDouble(this.jTextFieldCustomTargetValue.getText());
        return returnValue;
    }

    /**
     * Set the list of measurementnames. Their names will appear in JCombobox to
     * be chosen
     *
     * @param l list of measurements. Names will be displayed in JCombobox
     */
    public void setMeasurements(ArrayList<MeasureType> l) {
        this.listOfMeasureMents = l;
        this.jComboBoxMeasurementName.removeAllItems();
        for (MeasureType l1 : l) {
            this.jComboBoxMeasurementName.addItem((l1).getMeasureName());
        }
    }

    /**
     * Returns List of all possible Measurements for this form, which can be
     * chosen to optimize
     *
     * @return List of possbibleMeasurements to chose from fro optimization
     */
    public ArrayList<MeasureType> getMeasurements() {
        return listOfMeasureMents;
    }

    /**
     * Returns type of target as selected by user with combobox
     *
     * @return Type of target
     */
    public typeOfTarget getOptimizationTarget() {
        return (typeOfTarget.valueOf(this.jComboBoxOptimizationTarget.getSelectedItem().toString()));
    }

    /**
     * Activates or deactivates this element. The chceckbox will be
     * selected/deselected
     *
     * @param b true -> activate, false -> deactivated
     */
    public void setActivated(boolean b) {
        this.jComboBoxMeasurementName.setEnabled(b);
        this.jComboBoxOptimizationTarget.setEnabled(b);
        this.jTextFieldCustomTargetValue.setEnabled(this.getOptimizationTarget().equals(typeOfTarget.value));
    }

    /**
     * Return selection status of Active-Checkbox
     *
     * @return true if this measure is selected, els false
     */
    public boolean isActive() {
        return this.jCheckBoxEnableOptimizationForThisMeasurement.isSelected();
    }

}
