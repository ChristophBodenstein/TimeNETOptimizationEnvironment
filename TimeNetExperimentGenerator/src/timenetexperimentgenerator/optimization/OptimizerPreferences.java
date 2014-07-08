/*
 * Preferences For Hill Climbing, combines logic for load/save and GUI
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

public String pref_LogFileAddon="";
public int pref_WrongSimulationsUntilBreak=30;
public int pref_WrongSimulationsPerDirection=10;
public support.typeOfStartValueEnum pref_StartValue=support.typeOfStartValueEnum.start;

    /**
     * Creates new form OptimizerHillPreferences
     */
    public OptimizerPreferences() {
        initComponents();
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

        jLabel1.setText("Startvalue for parameters");

        jComboBoxTypeOfStartValue.setModel(new DefaultComboBoxModel(support.typeOfStartValueEnum.values()));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addGap(26, 26, 26)
                .addComponent(jComboBoxTypeOfStartValue, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxTypeOfStartValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(257, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OptimizerPreferences.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OptimizerPreferences.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OptimizerPreferences.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OptimizerPreferences.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OptimizerPreferences().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JComboBox jComboBoxTypeOfStartValue;
    private javax.swing.JLabel jLabel1;
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
	}
    
    this.pref_WrongSimulationsUntilBreak=support.loadIntFromProperties("pref_WrongSimulationsUntilBreak", pref_WrongSimulationsUntilBreak, auto);
    support.log("Loaded pref_WrongSimulationsUntilBreak is "+pref_WrongSimulationsUntilBreak);
    
    this.pref_WrongSimulationsPerDirection=support.loadIntFromProperties("pref_WrongSimulationsPerDirection", pref_WrongSimulationsPerDirection, auto);
    support.log("Loaded pref_WrongSimulationsPerDirection is "+pref_WrongSimulationsPerDirection);
    
    this.pref_StartValue=support.typeOfStartValueEnum.valueOf(auto.getProperty("pref_StartValue", support.typeOfStartValueEnum.start.toString()));
    support.log("Loaded StartValue is "+pref_StartValue);
    
    this.pref_LogFileAddon=auto.getProperty("pref_LogFileAddon", "");
    support.log("Loaded Optimizer_Logfile-Addon is "+pref_LogFileAddon);
    
    }
    
    /**
     * Save Preferences to defined file
     */
    public void savePreferences(){
    support.log("Saving Properties of HillClimbing");
        try{
        auto.setProperty("pref_WrongSimulationsUntilBreak", Integer.toString(pref_WrongSimulationsUntilBreak));
        auto.setProperty("pref_WrongSimulationsPerDirection", Integer.toString(pref_WrongSimulationsPerDirection));
        auto.setProperty("pref_StartValue", pref_StartValue.toString());
        auto.setProperty("pref_LogFileAddon", pref_LogFileAddon);
        
        File parserprops =  new File(propertyFile);
        auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
        }catch(IOException e){
        support.log("Problem Saving the properties.");
        }
    
    }

    
    
    
    
}
