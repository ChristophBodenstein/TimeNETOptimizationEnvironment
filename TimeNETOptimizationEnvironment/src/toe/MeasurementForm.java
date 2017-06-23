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

    private ArrayList<MeasureType> listOfMeasureMents = new ArrayList<>();

    /**
     * Creates new form MeasurementForm
     */
    public MeasurementForm() {
        initComponents();
        this.setActivated(true);
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
        jComboBoxTargetType = new javax.swing.JComboBox();
        jSpinnerTargetValue = new javax.swing.JSpinner();

        jComboBoxMeasurementName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Measure available!" }));
        jComboBoxMeasurementName.setToolTipText("Chose Measurement to optimize");

        jComboBoxTargetType.setModel(new DefaultComboBoxModel(typeOfTarget.values()));
        jComboBoxTargetType.setToolTipText("Chose Optimization Target Value");
        jComboBoxTargetType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxTargetTypeItemStateChanged(evt);
            }
        });

        jSpinnerTargetValue.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 0.001d));
        jSpinnerTargetValue.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerTargetValue, "#.####"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBoxTargetType, 0, 82, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinnerTargetValue, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jComboBoxMeasurementName, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jComboBoxMeasurementName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerTargetValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxTargetType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTargetTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxTargetTypeItemStateChanged
        this.jSpinnerTargetValue.setEnabled(this.getTargetType().equals(typeOfTarget.value));
    }//GEN-LAST:event_jComboBoxTargetTypeItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxMeasurementName;
    private javax.swing.JComboBox jComboBoxTargetType;
    private javax.swing.JSpinner jSpinnerTargetValue;
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
        support.log("***Error: No Measurement chosen, will return null!", typeOfLogLevel.ERROR);
        return null;
    }

    /**
     * *
     * Selects the masurement with given name as optimization target
     *
     * @param nameOfMeasurement
     */
    public void setChosenMeasurement(String nameOfMeasurement) {
        int i = 0;
        for (i = 0; i < this.jComboBoxMeasurementName.getItemCount(); i++) {
            if (this.jComboBoxMeasurementName.getItemAt(i).equals(nameOfMeasurement)) {
                this.jComboBoxMeasurementName.setSelectedIndex(i);
            }
        }

    }

    /**
     * Return target value, given by user in JTextField
     *
     * @return target value for measurement
     */
    public double getCustomTargetValue() {
        double returnValue;
        returnValue = (double) jSpinnerTargetValue.getValue();
        return returnValue;
    }

    /**
     * *
     * Set the custom target value for measure
     *
     * @param targetValue Targetvalue for optimization
     */
    public void setTargetValue(double targetValue) {
        jSpinnerTargetValue.setValue(targetValue);
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
    public typeOfTarget getTargetType() {
        return (typeOfTarget.valueOf(this.jComboBoxTargetType.getSelectedItem().toString()));
    }

    /**
     * *
     * Sets the type of target for optimization
     *
     * @param targetType Type of target to be set
     */
    public void setTargetType(typeOfTarget targetType) {
        this.jComboBoxTargetType.setSelectedItem(targetType);
    }

    /**
     * Activates or deactivates this element. The chceckbox will be
     * selected/deselected
     *
     * @param b true -> activate, false -> deactivated
     */
    public void setActivated(boolean b) {
        this.jComboBoxMeasurementName.setEnabled(b);
        this.jComboBoxTargetType.setEnabled(b);
        this.jSpinnerTargetValue.setEnabled(this.getTargetType().equals(typeOfTarget.value));
    }

    /**
     * Return selection status of Active-Checkbox
     *
     * @return true if this measure is selected, els false
     */
    public boolean isActive() {
        //Multi-Target is not yet implemented completely
        return true;
    }

    /**
     * Override to enable/disable members
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.setActivated(enabled);
        this.jSpinnerTargetValue.setEnabled(enabled && this.getTargetType().equals(typeOfTarget.value));
    }

}
