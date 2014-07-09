/*
 * Preferences for all optimization-algorithms, combines logic for load/save and GUI
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.optimization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein
 */
public class OptimizerPreferences extends javax.swing.JFrame {
private String propertyFile=System.getProperty("user.home")+File.separatorChar+ ".OptimizerProperties.prop";
private Properties auto=new Properties();

private String pref_LogFileAddon="";
private int pref_WrongSimulationsUntilBreak;
private int pref_WrongSimulationsPerDirection;
private support.typeOfStartValueEnum pref_StartValue=support.typeOfStartValueEnum.start;

    /**
     * Creates new form OptimizerHillPreferences
     */
    public OptimizerPreferences() {
        initComponents();
        this.setPref_WrongSimulationsUntilBreak(support.DEFAULT_WRONG_SOLUTIONS_IN_A_ROW);
        this.setPref_WrongSimulationsPerDirection(support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION);

        this.loadPreferences();

        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBoxTypeOfStartValue = new javax.swing.JComboBox();
        jSpinnerWrongSolutionsUntilBreak = new javax.swing.JSpinner();
        jSpinnerWrongSolutionsPerDirectionUntilBreak = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldLogFileAddon = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jCheckBoxAddPrefsToLogfilename = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();

        jLabel1.setText("Startvalue for parameters");

        jComboBoxTypeOfStartValue.setModel(new DefaultComboBoxModel(support.typeOfStartValueEnum.values()));
        jComboBoxTypeOfStartValue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxTypeOfStartValueItemStateChanged(evt);
            }
        });
        jComboBoxTypeOfStartValue.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                jComboBoxTypeOfStartValueVetoableChange(evt);
            }
        });

        jSpinnerWrongSolutionsUntilBreak.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerWrongSolutionsUntilBreakStateChanged(evt);
            }
        });
        jSpinnerWrongSolutionsUntilBreak.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                jSpinnerWrongSolutionsUntilBreakVetoableChange(evt);
            }
        });

        jSpinnerWrongSolutionsPerDirectionUntilBreak.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerWrongSolutionsPerDirectionUntilBreakStateChanged(evt);
            }
        });
        jSpinnerWrongSolutionsPerDirectionUntilBreak.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                jSpinnerWrongSolutionsPerDirectionUntilBreakVetoableChange(evt);
            }
        });

        jLabel2.setText("Wrong Solutions until break");

        jLabel3.setText("Wrong Solutions per direction until break");

        jTextFieldLogFileAddon.setToolTipText("Addon-Text for Logfilename");
        jTextFieldLogFileAddon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLogFileAddonActionPerformed(evt);
            }
        });
        jTextFieldLogFileAddon.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTextFieldLogFileAddonInputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });
        jTextFieldLogFileAddon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldLogFileAddonKeyReleased(evt);
            }
        });
        jTextFieldLogFileAddon.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                jTextFieldLogFileAddonVetoableChange(evt);
            }
        });

        jLabel4.setText("Addon-Text for Logfile");

        jCheckBoxAddPrefsToLogfilename.setSelected(true);
        jCheckBoxAddPrefsToLogfilename.setText("Add Prefs to Logfilename");

        jButton1.setText("Save");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                        .addGap(26, 26, 26))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jComboBoxTypeOfStartValue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSpinnerWrongSolutionsUntilBreak, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                                    .addComponent(jSpinnerWrongSolutionsPerDirectionUntilBreak, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                                    .addComponent(jTextFieldLogFileAddon, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))))
                        .addGap(18, 18, 18)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jCheckBoxAddPrefsToLogfilename)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxTypeOfStartValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerWrongSolutionsUntilBreak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerWrongSolutionsPerDirectionUntilBreak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                .addComponent(jCheckBoxAddPrefsToLogfilename)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldLogFileAddon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldLogFileAddonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLogFileAddonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldLogFileAddonActionPerformed

    private void jSpinnerWrongSolutionsPerDirectionUntilBreakVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jSpinnerWrongSolutionsPerDirectionUntilBreakVetoableChange
    
    }//GEN-LAST:event_jSpinnerWrongSolutionsPerDirectionUntilBreakVetoableChange

    private void jSpinnerWrongSolutionsUntilBreakVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jSpinnerWrongSolutionsUntilBreakVetoableChange
    
    }//GEN-LAST:event_jSpinnerWrongSolutionsUntilBreakVetoableChange

    private void jComboBoxTypeOfStartValueVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jComboBoxTypeOfStartValueVetoableChange
    
    }//GEN-LAST:event_jComboBoxTypeOfStartValueVetoableChange

    private void jTextFieldLogFileAddonVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jTextFieldLogFileAddonVetoableChange
    
    }//GEN-LAST:event_jTextFieldLogFileAddonVetoableChange

    private void jComboBoxTypeOfStartValueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxTypeOfStartValueItemStateChanged
    
    }//GEN-LAST:event_jComboBoxTypeOfStartValueItemStateChanged

    private void jSpinnerWrongSolutionsUntilBreakStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerWrongSolutionsUntilBreakStateChanged
    
    }//GEN-LAST:event_jSpinnerWrongSolutionsUntilBreakStateChanged

    private void jSpinnerWrongSolutionsPerDirectionUntilBreakStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerWrongSolutionsPerDirectionUntilBreakStateChanged
    
    }//GEN-LAST:event_jSpinnerWrongSolutionsPerDirectionUntilBreakStateChanged

    private void jTextFieldLogFileAddonInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTextFieldLogFileAddonInputMethodTextChanged
    
    }//GEN-LAST:event_jTextFieldLogFileAddonInputMethodTextChanged

    private void jTextFieldLogFileAddonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLogFileAddonKeyReleased
    
    }//GEN-LAST:event_jTextFieldLogFileAddonKeyReleased

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    this.savePreferences();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBoxAddPrefsToLogfilename;
    public javax.swing.JComboBox jComboBoxTypeOfStartValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSpinner jSpinnerWrongSolutionsPerDirectionUntilBreak;
    private javax.swing.JSpinner jSpinnerWrongSolutionsUntilBreak;
    private javax.swing.JTextField jTextFieldLogFileAddon;
    // End of variables declaration//GEN-END:variables


    /**
     * Load Preferences from defined file
     */
    public void loadPreferences(){
    try {
                FileInputStream in=new FileInputStream(propertyFile);
		auto.load(in);
                in.close();
	} catch (IOException e) {
		// Exception bearbeiten
            support.log("Error while loading Optimizer-Properties.");
	}
    
    this.setPref_WrongSimulationsUntilBreak(support.loadIntFromProperties("pref_WrongSimulationsUntilBreak", getPref_WrongSimulationsUntilBreak(), auto));
    support.log(Integer.toString(support.loadIntFromProperties("pref_WrongSimulationsUntilBreak", getPref_WrongSimulationsUntilBreak(), auto)));
    
    support.log("Loaded pref_WrongSimulationsUntilBreak is "+getPref_WrongSimulationsUntilBreak());
    
        this.setPref_WrongSimulationsPerDirection(support.loadIntFromProperties("pref_WrongSimulationsPerDirection", getPref_WrongSimulationsPerDirection(), auto));
    support.log("Loaded pref_WrongSimulationsPerDirection is "+getPref_WrongSimulationsPerDirection());
    
        this.setPref_StartValue(support.typeOfStartValueEnum.valueOf(auto.getProperty("pref_StartValue", support.typeOfStartValueEnum.start.toString())));
    support.log("Loaded StartValue is "+getPref_StartValue());
    
        this.setPref_LogFileAddon(auto.getProperty("pref_LogFileAddon", ""));
    support.log("Loaded Optimizer_Logfile-Addon is "+this.jTextFieldLogFileAddon.getText());
    
    }
    
    /**
     * Save Preferences to defined file
     */
    public void savePreferences(){
    support.log("Saving Properties of Optimization");
        try{
        auto.setProperty("pref_WrongSimulationsUntilBreak", Integer.toString(getPref_WrongSimulationsUntilBreak()));
        auto.setProperty("pref_WrongSimulationsPerDirection", Integer.toString(getPref_WrongSimulationsPerDirection()));
        auto.setProperty("pref_StartValue", getPref_StartValue().toString());
        auto.setProperty("pref_LogFileAddon", this.jTextFieldLogFileAddon.getText());
        
        File parserprops =  new File(propertyFile);
        auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
        }catch(IOException e){
        support.log("Problem Saving the properties.");
        }
    
    }

    /**
     * @return the pref_WrongSimulationsUntilBreak
     */
    public int getPref_WrongSimulationsUntilBreak() {
        pref_WrongSimulationsUntilBreak=(Integer)this.jSpinnerWrongSolutionsUntilBreak.getValue();
        return pref_WrongSimulationsUntilBreak;
    }

    /**
     * @param pref_WrongSimulationsUntilBreak the pref_WrongSimulationsUntilBreak to set
     */
    public void setPref_WrongSimulationsUntilBreak(int pref_WrongSimulationsUntilBreak) {
        this.pref_WrongSimulationsUntilBreak = pref_WrongSimulationsUntilBreak;
        this.jSpinnerWrongSolutionsUntilBreak.setValue(pref_WrongSimulationsUntilBreak);
    }

    /**
     * @return the pref_WrongSimulationsPerDirection
     */
    public int getPref_WrongSimulationsPerDirection() {
        pref_WrongSimulationsPerDirection=(Integer)this.jSpinnerWrongSolutionsPerDirectionUntilBreak.getValue();
        return pref_WrongSimulationsPerDirection;
    }

    /**
     * @param pref_WrongSimulationsPerDirection the pref_WrongSimulationsPerDirection to set
     */
    public void setPref_WrongSimulationsPerDirection(int pref_WrongSimulationsPerDirection) {
        this.pref_WrongSimulationsPerDirection = pref_WrongSimulationsPerDirection;
        this.jSpinnerWrongSolutionsPerDirectionUntilBreak.setValue(pref_WrongSimulationsPerDirection);
    }

    /**
     * @return the pref_LogFileAddon
     */
    public String getPref_LogFileAddon() {
    this.pref_LogFileAddon=this.jTextFieldLogFileAddon.getText();
    String addonString=pref_LogFileAddon;

        if(this.jCheckBoxAddPrefsToLogfilename.isSelected()){
        addonString+="_WSIMPERDIR_"+this.getPref_WrongSimulationsPerDirection()+"_WSIM_"+this.getPref_WrongSimulationsUntilBreak()+"_StartAt_"+this.jComboBoxTypeOfStartValue.getSelectedItem();
        }
        return addonString;
    }

    /**
     * @param pref_LogFileAddon the pref_LogFileAddon to set
     */
    public void setPref_LogFileAddon(String pref_LogFileAddon) {
        this.jTextFieldLogFileAddon.setText(pref_LogFileAddon);
        this.pref_LogFileAddon = pref_LogFileAddon;
    }

    /**
     * @return the pref_StartValue
     */
    public support.typeOfStartValueEnum getPref_StartValue() {
        pref_StartValue=(support.typeOfStartValueEnum)this.jComboBoxTypeOfStartValue.getSelectedItem();
        return pref_StartValue;
    }

    /**
     * @param pref_StartValue the pref_StartValue to set
     */
    public void setPref_StartValue(support.typeOfStartValueEnum pref_StartValue) {
        this.jComboBoxTypeOfStartValue.setSelectedItem(pref_StartValue);
        this.pref_StartValue = pref_StartValue;
    }
    
}
