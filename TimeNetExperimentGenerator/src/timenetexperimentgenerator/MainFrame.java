/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainFrame.java
 *
 * Created on 08.08.2013, 14:56:21
 */

package timenetexperimentgenerator;


import java.awt.Color;
import java.awt.Component;
import java.io.*;
import javax.swing.JFileChooser;
import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 *
 * @author sse
 */
public class MainFrame extends javax.swing.JFrame implements TableModelListener{
private String propertyFile="ExperimentGeneratorprops.prop";
Properties auto = new Properties();
private String fileName="";
public boolean cancelOperation=false;
ArrayList <parameter[]>ListOfParameterSetsToBeWritten=new ArrayList<parameter[]>();//Name, Value
generator myGenerator;
public parameter pConfidenceIntervall=new parameter();
public parameter pSeed=new parameter();
public parameter pEndTime=new parameter();
public parameter pMaxTime=new parameter();
public parameter pMaxError=new parameter();
private int colorClm = -1, colorRow = -1;
ArrayList <Long>ListOfParameterSetIds=new ArrayList<Long>();
private int sizeOfDesignSpace;



    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();
        try {
                FileInputStream in=new FileInputStream(propertyFile);
		auto.load(in);
                in.close();
	} catch (IOException e) {
		// Exception bearbeiten
	}
        pConfidenceIntervall.initWithValues("ConfidenceIntervall", "95", "95", "1"); 
        pSeed.initWithValues("Seed", "0", "0", "1");
        pEndTime.initWithValues("EndTime","0","0","1");
        pMaxTime.initWithValues("MaxTime","0","0","1");
        pMaxError.initWithValues("MaxError","5","5","1");

        this.jTextFieldSCPNFile.setText(auto.getProperty("file"));
        this.jTextFieldPathToTimeNet.setText(auto.getProperty("timenetpath"));
        this.pConfidenceIntervall.setStartValue(checkIfStringIsNull(auto.getProperty("ConfidenceIntervallStart"),pConfidenceIntervall.getStartValue()));
        this.pConfidenceIntervall.setEndValue(checkIfStringIsNull(auto.getProperty("ConfidenceIntervallEnd"),pConfidenceIntervall.getEndValue()));
        this.pConfidenceIntervall.setStepping(checkIfStringIsNull(auto.getProperty("ConfidenceIntervallStepping"),pConfidenceIntervall.getStepping()));

        this.pEndTime.setStartValue(checkIfStringIsNull(auto.getProperty("EndTimeStart"),pEndTime.getStartValue()));
        this.pEndTime.setEndValue(checkIfStringIsNull(auto.getProperty("EndTimeEnd"),pEndTime.getEndValue()));
        this.pEndTime.setStepping(checkIfStringIsNull(auto.getProperty("EndTimeStepping"),pEndTime.getStepping()));

        this.pMaxTime.setStartValue(checkIfStringIsNull(auto.getProperty("MaxTimeStart"),pMaxTime.getStartValue()));
        this.pMaxTime.setEndValue(checkIfStringIsNull(auto.getProperty("MaxTimeEnd"),pMaxTime.getEndValue()));
        this.pMaxTime.setStepping(checkIfStringIsNull(auto.getProperty("MaxTimeStepping"),pMaxTime.getStepping()));

        this.pSeed.setStartValue(checkIfStringIsNull(auto.getProperty("SeedStart"),pSeed.getStartValue()));
        this.pSeed.setEndValue(checkIfStringIsNull(auto.getProperty("SeedEnd"),pSeed.getEndValue()));
        this.pSeed.setStepping(checkIfStringIsNull(auto.getProperty("SeedStepping"),pSeed.getStepping()));

        this.pMaxError.setStartValue(checkIfStringIsNull(auto.getProperty("MaxErrorStart"),pMaxError.getStartValue()));
        this.pMaxError.setEndValue(checkIfStringIsNull(auto.getProperty("MaxErrorEnd"),pMaxError.getEndValue()));
        this.pMaxError.setStepping(checkIfStringIsNull(auto.getProperty("MaxErrorStepping"),pMaxError.getStepping()));


        this.checkIfTimeNetPathIsCorrect();
        this.deactivateExportButtons();
        this.activateReloadButtons();


        jTableParameterList.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                        try{
                            String rowName=(String)table.getValueAt(row, 0);
                            if (rowName.equals("Seed")||rowName.equals("MaxTime")||rowName.equals("EndTime")||rowName.equals("ConfidenceIntervall")||rowName.equals("MaxRelError")){
                            //if (row == colorRow && column == colorClm) {
                                setBackground(Color.LIGHT_GRAY);
                                setForeground(Color.BLACK);
                            } else {
                                setBackground(Color.WHITE);
                                setForeground(Color.BLUE);
                            }
                        }catch(Exception e){
                         }
                return this;
            }
        });


        
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonOpenSCPN = new javax.swing.JButton();
        jTextFieldSCPNFile = new javax.swing.JTextField();
        jButtonReload = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableParameterList = new javax.swing.JTable();
        jButtonExport = new javax.swing.JButton();
        jLabelExportStatus = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextFieldPathToTimeNet = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jButtonStartBatchSimulation = new javax.swing.JButton();
        jLabelCheckPathToTimeNet = new javax.swing.JLabel();
        jButtonGenerateListOfExperiments = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jButtonLoadSampleLogFile = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        measurementForm1 = new timenetexperimentgenerator.MeasurementForm();
        measurementForm2 = new timenetexperimentgenerator.MeasurementForm();
        jButtonStartOptimization = new javax.swing.JButton();
        jLabelSimulationCount = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButtonOpenSCPN.setText("Open SCPN");
        jButtonOpenSCPN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenSCPNActionPerformed(evt);
            }
        });

        jButtonReload.setText("Reload");
        jButtonReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReloadActionPerformed(evt);
            }
        });

        jTableParameterList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Name", "StartValue", "EndValue", "Stepping"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTableParameterList.setAutoCreateRowSorter(true);
        jTableParameterList.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane1.setViewportView(jTableParameterList);

        jButtonExport.setText("Export Experiments");
        jButtonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportActionPerformed(evt);
            }
        });

        jLabelExportStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextFieldPathToTimeNet.setText("Absolute Path to TimeNet");
        jTextFieldPathToTimeNet.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldPathToTimeNetKeyReleased(evt);
            }
        });

        jButtonStartBatchSimulation.setText("Start batch simulation");
        jButtonStartBatchSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartBatchSimulationActionPerformed(evt);
            }
        });

        jLabelCheckPathToTimeNet.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabelCheckPathToTimeNet.setForeground(new java.awt.Color(255, 0, 0));
        jLabelCheckPathToTimeNet.setText("Check Path to TimeNET.jar!");

        jButtonGenerateListOfExperiments.setText("Generate Design Space");
        jButtonGenerateListOfExperiments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateListOfExperimentsActionPerformed(evt);
            }
        });

        jButtonLoadSampleLogFile.setText("Load Sample Log-File for Opti");
        jButtonLoadSampleLogFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadSampleLogFileActionPerformed(evt);
            }
        });

        jTabbedPane1.addTab("T1", measurementForm1);
        jTabbedPane1.addTab("T2", measurementForm2);

        jButtonStartOptimization.setText("Start Optimization");
        jButtonStartOptimization.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartOptimizationActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 752, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(jLabelCheckPathToTimeNet, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(182, 182, 182))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldPathToTimeNet)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldSCPNFile)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .add(28, 28, 28)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabelSimulationCount, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTabbedPane1)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                        .add(jButtonOpenSCPN, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(59, 59, 59)
                                        .add(jButtonReload))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator2)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                        .add(jButtonExport)
                                        .add(18, 18, 18)
                                        .add(jButton1))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonGenerateListOfExperiments, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonStartBatchSimulation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator3)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonLoadSampleLogFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                        .add(38, 38, 38)
                                        .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonStartOptimization, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(66, 66, 66)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldSCPNFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonOpenSCPN)
                    .add(jButtonReload))
                .add(2, 2, 2)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButtonExport)
                            .add(jButton1))
                        .add(18, 18, 18)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(jButtonGenerateListOfExperiments)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonStartBatchSimulation)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButtonLoadSampleLogFile)
                        .add(18, 18, 18)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(44, 44, 44)
                        .add(jButtonStartOptimization))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                .add(29, 29, 29)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jTextFieldPathToTimeNet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(jLabelCheckPathToTimeNet)
                        .add(8, 8, 8))
                    .add(jLabelSimulationCount, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOpenSCPNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenSCPNActionPerformed

      javax.swing.filechooser.FileFilter myFilter=new javax.swing.filechooser.FileNameExtensionFilter("xml file", "xml");
      JFileChooser fileChooser = new JFileChooser(this.jTextFieldSCPNFile.getText());
      fileChooser.setCurrentDirectory(new java.io.File(this.jTextFieldSCPNFile.getText()+"/.."));
      fileChooser.setFileFilter(myFilter);
      fileChooser.setDialogTitle("Select SCPN-Net");



      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        System.out.println("getCurrentDirectory(): "
         +  fileChooser.getCurrentDirectory());
        System.out.println("getSelectedFile() : "
         +  fileChooser.getSelectedFile());
        this.jTextFieldSCPNFile.setText(fileChooser.getSelectedFile().toString());
        this.readSCPNFile(fileChooser.getSelectedFile().toString());
        this.saveProperties();
        /*auto.setProperty("file", fileChooser.getSelectedFile().toString());
        File parserprops =  new File(propertyFile);
	try {
		auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
	} catch (IOException e) {
		// Exception bearbeiten
	}*/
      }
    else {
      System.out.println("No Selection ");
      }
    }//GEN-LAST:event_jButtonOpenSCPNActionPerformed

    private void jButtonReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReloadActionPerformed
    this.readSCPNFile(jTextFieldSCPNFile.getText());
    }//GEN-LAST:event_jButtonReloadActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
    //this.cancelOperation=false;
    //ArrayList <parameter[]>ListOfParameterSetsToBeWritten=new ArrayList<parameter[]>();//Name, Value

    
    //this.restartGenerator();
    //this.waitForGenerator();

        if(ListOfParameterSetsToBeWritten!=null){
        System.out.println("Length of ParameterSet-List: "+ListOfParameterSetsToBeWritten.size());
        exporter tmpExporter=new exporter(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this);
        }else{
        System.out.println("Export-Operation cancled.");
        }
    this.cancelOperation=false;

    //exporter tmpExporter=new exporter(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this);
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

    this.cancelOperation=true;
    
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextFieldPathToTimeNetKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldPathToTimeNetKeyReleased
    this.saveProperties();
        /*auto.setProperty("timenetpath", this.jTextFieldPathToTimeNet.getText());
        File parserprops =  new File(propertyFile);
	try {
		auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
	} catch (IOException e) {
		// Exception bearbeiten
	}
    */
     this.checkIfTimeNetPathIsCorrect();
    }//GEN-LAST:event_jTextFieldPathToTimeNetKeyReleased

    public String getPathToTimeNet(){
    return this.jTextFieldPathToTimeNet.getText();
    }

    /**
     * Start generation of experiments and start of batch simulation
     */
    private void jButtonStartBatchSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartBatchSimulationActionPerformed
    //ArrayList <parameter[]>ListOfParameterSetsToBeWritten=new ArrayList<parameter[]>();//Name, Value
    //Thread is started automatically
    //generator myGenerator=new generator(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this, jTableParameterList);
    //this.restartGenerator();
    //this.waitForGenerator();
    //Thread is started automatically
    batchSimulator mySimulator=new batchSimulator(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this);
    
    }//GEN-LAST:event_jButtonStartBatchSimulationActionPerformed

    private void jButtonGenerateListOfExperimentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateListOfExperimentsActionPerformed
    this.restartGenerator();
    }//GEN-LAST:event_jButtonGenerateListOfExperimentsActionPerformed

    private void jButtonLoadSampleLogFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadSampleLogFileActionPerformed
        /*
         show Filechoser
         * load log-file
         * parse log-file
         * get list of measurements
         * Set Names of Measurements in Tabs
         * 
         */
      javax.swing.filechooser.FileFilter myFilter=new javax.swing.filechooser.FileNameExtensionFilter("log-file", "log");
      JFileChooser fileChooser = new JFileChooser(this.jTextFieldSCPNFile.getText());
      fileChooser.setCurrentDirectory(new java.io.File(this.jTextFieldSCPNFile.getText()+"/.."));
      fileChooser.setFileFilter(myFilter);
      fileChooser.setDialogTitle("Select Log-File");

      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        System.out.println("getCurrentDirectory(): "
         +  fileChooser.getCurrentDirectory());
        System.out.println("getSelectedFile() : "
         +  fileChooser.getSelectedFile());
      parser myParser=new parser();
      myParser.parse(fileChooser.getSelectedFile().toString(),this.jTextFieldSCPNFile.getText());

      
       for(int i=0;i<this.jTabbedPane1.getComponentCount();i++){
       ((MeasurementForm)this.jTabbedPane1.getComponent(i)).setMeasurements(myParser.getMeasures());
       }


        //this.jTextFieldSCPNFile.setText(fileChooser.getSelectedFile().toString());
        //this.readSCPNFile(fileChooser.getSelectedFile().toString());
        this.saveProperties();

      }
    else {
      System.out.println("No Selection ");
      }


    }//GEN-LAST:event_jButtonLoadSampleLogFileActionPerformed

    private void jButtonStartOptimizationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartOptimizationActionPerformed
       
        genericOptimizer myOptimizer=new genericOptimizer(this.jTextFieldSCPNFile.getText(), this, jTabbedPane1, this.jTextFieldPathToTimeNet.getText(), this.jLabelExportStatus);
    }//GEN-LAST:event_jButtonStartOptimizationActionPerformed

    public void calculateDesignSpace(){
        myGenerator=new generator(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this, jTableParameterList);
        this.sizeOfDesignSpace=myGenerator.getSizeOfDesignspace();
        this.jLabelExportStatus.setText("Designspace-Size:"+ sizeOfDesignSpace);
    }

    public void buildListOfParameterSetsToExport(ArrayList ListOfParameterSetsToBeWritten, ArrayList ListOfParameterAsFromTable, parameter[] lastParameterSet, JLabel infoLabel){
    //erstelle Liste der Länge von ListOfParameterAsFromTable, packe alle Parameter rein
    //Beginne erste Schleife, kopiere deep die ganze Liste
    boolean isAlreadyInExportList=false;
    
    if(cancelOperation){
    this.activateReloadButtons();
        return;
    }
        if(ListOfParameterAsFromTable.size()>0){
        parameter loopParameter=(parameter)ListOfParameterAsFromTable.get(ListOfParameterAsFromTable.size()-1);
        //ListOfParameterAsFromTable.remove(0);

        ListOfParameterAsFromTable.remove(loopParameter);

        String loopName=loopParameter.getName();
        //System.out.println("Iterating through "+loopName +" with ListSize "+ListOfParameterAsFromTable.size());
        boolean canIterate=true;

        float start=1, end=1, step=1;
            try{
            start=Float.parseFloat(loopParameter.getStartValue());
            end=Float.parseFloat(loopParameter.getEndValue());
            step=Float.parseFloat(loopParameter.getStepping());
            canIterate=true;
            /*    if((end-start)>0){
                    if(((end-start)/step)>0){ //Iteration möglich
                    canIterate=true;
                    }
                }
            */
            }catch(Exception e){
            //canIterate=false;
            System.out.println("Could not convert into float, maybe String is used. Will not iterate through parameter "+loopParameter.getName());
            return;
            }

            if(canIterate){
            float usedValue=start;
            int endCounter=1;
                    if((end-start)>0){
                        endCounter=(int)Math.ceil((end-start)/step) +1 ;
                        //System.out.println(endCounter +" steps to be made for "+loopName);
                    }
                //if(start!=end){endCounter++;}
            
                for(int i=0;i<endCounter;i++){
                usedValue=start+(float)i*step;
                String usedValueString=String.valueOf(usedValue);
                //System.out.println("Setting Value to "+usedValueString+"for "+loopName);
                
                
                parameter[] nextParameterSet=new parameter[lastParameterSet.length];
                    //Kopie des Parametersets anlegen
                    for(int c=0;c<lastParameterSet.length;c++){
                        try{nextParameterSet[c]=(parameter) lastParameterSet[c].clone();}
                        catch(Exception e){e.printStackTrace();}

                    }

                    for(int c=0;c<nextParameterSet.length;c++){
                        if(nextParameterSet[c].getName().equals(loopName)){
                        //modifizierten Parameter setzen
                        //System.out.println("Alte ID: "+getIDOfParameterSet(nextParameterSet));
                        nextParameterSet[c].setValue(usedValueString);
                        //System.out.println("Setze "+loopName+" auf "+usedValueString);
                        //System.out.println("Neue ID: "+getIDOfParameterSet(nextParameterSet));
                        }
                    }

                /*isAlreadyInExportList=false;
                if(ListOfParameterSetsToBeWritten.size()>0){
                    for(int d=0; d<ListOfParameterSetsToBeWritten.size();d++){
                        long id0,id1;
                        id0=getIDOfParameterSet((parameter[])ListOfParameterSetsToBeWritten.get(d));
                        id1=getIDOfParameterSet(nextParameterSet);
                        //System.out.println("Old ID0:"+id0);
                        //System.out.println("New ID1:"+id1);
                        if(id0==id1){
                        System.out.println("Ids are equal!");
                        //isAlreadyInExportList=true;
                        }
                    }
                }
                */
                //modifiziertes Parameterset hinzufügen
                //if(!isAlreadyInExportList){ListOfParameterSetsToBeWritten.add(nextParameterSet);}
                if(ListOfParameterAsFromTable.size()==0){
                addToListOfParameterSetsToBeWritten(nextParameterSet);}
                //Aufruf mit der aktuellen, reduzierten Parameterliste
                buildListOfParameterSetsToExport(ListOfParameterSetsToBeWritten, ListOfParameterAsFromTable, nextParameterSet, infoLabel);
                }
            }else{
                //Prüfen, ob dieser Eintrag bereits vorhanden ist
                isAlreadyInExportList=false;
                /*if(ListOfParameterSetsToBeWritten.size()>0){
                    for(int i=0; i<ListOfParameterSetsToBeWritten.size();i++){
                        long id0,id1;
                        id0=getIDOfParameterSet((parameter[])ListOfParameterSetsToBeWritten.get(i));
                        id1=getIDOfParameterSet(lastParameterSet);
                        if(id0==id1){
                        isAlreadyInExportList=true;
                        }
                    }


                }*/
                //Zur Liste hinzufügen
                //if(!isAlreadyInExportList){ListOfParameterSetsToBeWritten.add(lastParameterSet);}
                //addToListOfParameterSetsToBeWritten(lastParameterSet);
                //Aufruf mit der aktuellen, reduzierten Parameterliste
                //buildListOfParameterSetsToExport(ListOfParameterSetsToBeWritten, ListOfParameterAsFromTable, lastParameterSet, infoLabel);
                
            }
            //ListOfParameterAsFromTable.remove(ListOfParameterAsFromTable.size()-1);
        ListOfParameterAsFromTable.add(loopParameter);
        
        }else{
        //Schleifenabbruch, popup
        }

        //In Schleife tue dies wieder, rufe Schleife mit Arraylist - Letztem Element auf
    }


    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonGenerateListOfExperiments;
    private javax.swing.JButton jButtonLoadSampleLogFile;
    private javax.swing.JButton jButtonOpenSCPN;
    private javax.swing.JButton jButtonReload;
    private javax.swing.JButton jButtonStartBatchSimulation;
    private javax.swing.JButton jButtonStartOptimization;
    private javax.swing.JLabel jLabelCheckPathToTimeNet;
    private javax.swing.JLabel jLabelExportStatus;
    private javax.swing.JLabel jLabelSimulationCount;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableParameterList;
    private javax.swing.JTextField jTextFieldPathToTimeNet;
    private javax.swing.JTextField jTextFieldSCPNFile;
    private timenetexperimentgenerator.MeasurementForm measurementForm1;
    private timenetexperimentgenerator.MeasurementForm measurementForm2;
    // End of variables declaration//GEN-END:variables


    /**
     * Reads the xml-file of SCPN and sets the User Interface
     */
    private void readSCPNFile(String filename){
        deactivateExportButtons();
        try{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	Document doc = docBuilder.parse(filename);
        NodeList parameterList=doc.getElementsByTagName("parameter");

            for(int i=0;i<parameterList.getLength();i++){
            System.out.println(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            
            }
        jTableParameterList.setModel(new parameterTableModel(parameterList, this));
        jTableParameterList.getModel().addTableModelListener(this);

        this.fileName=filename;//nach Erfolg, globalen filename setzen
        activateGenerateButtons();
        }catch(Exception e){
        e.printStackTrace();
        }
    }

    public String removeExtention(String filePath) {
    // These first few lines the same as Justin's
    File f = new File(filePath);

        // if it's a directory, don't remove the extention
        if (f.isDirectory()){ return filePath;}

        String name = f.getName();

        // Now we know it's a file - don't need to do any special hidden
        // checking or contains() checking because of:
        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0)
        {
            // No period after first character - return name as it was passed in
            return filePath;
        }
        else
        {
            // Remove the last period and everything after it
            File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
            return renamed.getPath();
        }
    }

    public ArrayList removeDuplicates(ArrayList ListOfParameterSetsToBeWritten, JLabel infoLabel){
    ArrayList<parameter[]> tmpList=new ArrayList();
    boolean existsInOutPutList=false;
        for(int i=0; i<ListOfParameterSetsToBeWritten.size();i++){
        infoLabel.setText("Checking "+i+"/"+ListOfParameterSetsToBeWritten.size());
        if(cancelOperation){
        infoLabel.setText("Operation canceled");
        this.activateReloadButtons();
        return null;
        }
        parameter[] tmpParameterSet=(parameter[])ListOfParameterSetsToBeWritten.get(i);
        existsInOutPutList=false;
            if(tmpList.size()>0){
                for(int c=0;c<tmpList.size();c++){
                    parameter[] tmpListParameter=(parameter[])tmpList.get(c);
                    long tmpParameterSetID=getIDOfParameterSet(tmpParameterSet);
                    long tmpListParameterID=getIDOfParameterSet(tmpListParameter);
                    /*
                    System.out.println("P1: "+tmpParemeterSetID);
                    System.out.println("P2: "+tmpListParameterID);
                    * */

                    if(tmpListParameterID==tmpParameterSetID){
                    existsInOutPutList=true;
                    System.out.println("These Parameters are equal:");
                    System.out.println(tmpParameterSetID + " and " + tmpListParameterID);
                    printParameterSetCompare(tmpParameterSet, tmpListParameter);
                    
                    }
                }
            }
            if(!existsInOutPutList){
            tmpList.add(tmpParameterSet);
            }
        }
        /*
        System.out.println("Size of List without duplicates: "+tmpList.size());
        for(int i=0;i<tmpList.size();i++){
        System.out.println("P-ID: "+ getIDOfParameterSet(tmpList.get(i)) );
        }
         */
    return tmpList;
    }

    public long getIDOfParameterSet(parameter[] parameterset){
    long id=0;
    String tmpString="";
    //Arrays.sort(parameterset);
        for(int i=0;i<parameterset.length;i++){
        id=id+parameterset[i].getID();
        //System.out.println("ID of:"+ parameterset[i].getName()+" is " +parameterset[i].getID());
        tmpString=tmpString+String.valueOf(parameterset[i].getID());
        }
    //return id;
    return (long)tmpString.hashCode();
    }

    public String getNextSpinningChar(String oldChar){
    if(oldChar.equals("-")){return "\\";}
    if(oldChar.equals("\\")){return "|";}
    if(oldChar.equals("|")){return "/";}
    if(oldChar.equals("/")){return "-";}
    return "-";
    }

    /**
     * Starts and restarts the generator Thread
     */
    public void restartGenerator(){
    this.cancelOperation=false;
    myGenerator=null;
    ListOfParameterSetIds=new ArrayList<Long>();
    ListOfParameterSetsToBeWritten=new ArrayList<parameter[]>();
    myGenerator=new generator(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this, jTableParameterList);
    myGenerator.start();
    this.waitForGenerator();
    jButtonStartBatchSimulation.setEnabled(true);
    }


    /**
     * Waits for End of Generator
     */
    public void waitForGenerator(){
        try{
            wait(100);
            if (this.myGenerator.isAlive())return;
        }catch (Exception e){
        }
    }

    /**
     * restarts the generator after Table has changed
     */
    public void tableChanged(TableModelEvent e) {
    //System.out.println("Editing of Cell stopped, restarting generator.");
    //this.restartGenerator();
    jButtonStartBatchSimulation.setEnabled(false);
    readStaticParametersFromTable();
    saveProperties();
    calculateDesignSpace();
    }

    private void readStaticParametersFromTable(){
    //TODO: implement this
    this.pConfidenceIntervall.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("ConfidenceIntervall", "StartValue")) ;
    this.pConfidenceIntervall.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("ConfidenceIntervall", "EndValue")) ;
    this.pConfidenceIntervall.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("ConfidenceIntervall", "Stepping")) ;

    this.pEndTime.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("EndTime", "StartValue")) ;
    this.pEndTime.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("EndTime", "EndValue")) ;
    this.pEndTime.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("EndTime", "Stepping")) ;

    this.pMaxTime.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("MaxTime", "StartValue")) ;
    this.pMaxTime.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("MaxTime", "EndValue")) ;
    this.pMaxTime.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("MaxTime", "Stepping")) ;

    this.pSeed.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("Seed", "StartValue")) ;
    this.pSeed.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("Seed", "EndValue")) ;
    this.pSeed.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("Seed", "Stepping")) ;

    this.pMaxError.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("MaxRelError", "StartValue")) ;
    this.pMaxError.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("MaxRelError", "EndValue")) ;
    this.pMaxError.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getValueByName("MaxRelError", "Stepping")) ;

    }

    public void deactivateExportButtons(){
    this.jButtonExport.setEnabled(false);
    this.jButtonStartBatchSimulation.setEnabled(false);
    //this.jButtonOpenSCPN.setEnabled(false);
    this.jButtonReload.setEnabled(false);
    this.jButtonStartOptimization.setEnabled(false);
    this.jButtonGenerateListOfExperiments.setEnabled(false);
    }
    public void activateExportButtons(){
    this.jButtonExport.setEnabled(true);
    //this.jButtonOpenSCPN.setEnabled(true);
    this.jButtonReload.setEnabled(true);
    this.jButtonStartOptimization.setEnabled(true);
    this.jButtonGenerateListOfExperiments.setEnabled(true);
    this.checkIfTimeNetPathIsCorrect();
    }
    public void activateReloadButtons(){
    this.jButtonOpenSCPN.setEnabled(true);
    this.jButtonReload.setEnabled(true);
    }
    public void activateGenerateButtons(){
    this.jButtonStartOptimization.setEnabled(true);
    this.jButtonGenerateListOfExperiments.setEnabled(true);
    //this.jButtonStartBatchSimulation.setEnabled(true);
    }

    
    /*
    * Checks, if given Path to TimeNet is correct
    * If correct, then install new "RemoteSystem Client.config"
    */
    private void checkIfTimeNetPathIsCorrect(){
    String path=jTextFieldPathToTimeNet.getText();
    File tmpFile=new File(path+File.separator+"TimeNET.jar");
        if(tmpFile.exists()){
        this.jButtonStartBatchSimulation.setEnabled(true);
        this.jLabelCheckPathToTimeNet.setVisible(false);
            //Try to install "RemoteSystem Client.config"
            try{
            InputStream ddlStream = this.getClass().getClassLoader().getResourceAsStream("timenetexperimentgenerator/RemoteSystem Client.config");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(path+File.separator+"RemoteSystem Client.config");
                    byte[] buf = new byte[2048];
                    int r = ddlStream.read(buf);
                    while(r != -1) {
                        fos.write(buf, 0, r);
                        r = ddlStream.read(buf);
                    }
                } finally {
                    if(fos != null) {
                        fos.close();
                    }
                }
            }catch(Exception e){
            System.out.println("Failed to install RemoteSystem Clent.config");
            e.printStackTrace();
            }
            
        
        
        }else{
        this.jButtonStartBatchSimulation.setEnabled(false);
        this.jLabelCheckPathToTimeNet.setVisible(true);
        }
    }


    private void saveProperties(){
    System.out.println("Saving Properties.");
        try{
    auto.setProperty("timenetpath", this.jTextFieldPathToTimeNet.getText());
    auto.setProperty("file", this.jTextFieldSCPNFile.getText().toString());
    
    auto.setProperty("ConfidenceIntervallStart",this.pConfidenceIntervall.getStartValue());
    auto.setProperty("ConfidenceIntervallEnd",this.pConfidenceIntervall.getEndValue());
    auto.setProperty("ConfidenceIntervallStepping",this.pConfidenceIntervall.getStepping());

    auto.setProperty("EndTimeStart",this.pEndTime.getStartValue());
    auto.setProperty("EndTimeEnd",this.pEndTime.getEndValue());
    auto.setProperty("EndTimeStepping",this.pEndTime.getStepping());
        
    auto.setProperty("MaxTimeStart",this.pMaxTime.getStartValue());
    auto.setProperty("MaxTimeEnd",this.pMaxTime.getEndValue());
    auto.setProperty("MaxTimeStepping",this.pMaxTime.getStepping());
    auto.setProperty("SeedStart",this.pSeed.getStartValue());
    auto.setProperty("SeedEnd",this.pSeed.getEndValue());
    auto.setProperty("SeedStepping",this.pSeed.getStepping());
        
    auto.setProperty("MaxErrorStart",this.pMaxError.getStartValue());
    auto.setProperty("MaxErrorEnd",this.pMaxError.getEndValue());
    auto.setProperty("MaxErrorStepping",this.pMaxError.getStepping());
    
    File parserprops =  new File(propertyFile);
    auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
        }catch(Exception e){
        System.out.println("Problem Saving the properties.");
        e.printStackTrace();
        }

    }

    public String checkIfStringIsNull(String loadedValue, String defaultValue){
        if(loadedValue!=null){
        return loadedValue;
        }else{
            if(defaultValue!=null){
            return defaultValue;
            }else{
            return "0";
            }
        }

    }

    private void printParameterSetCompare(parameter[] p, parameter[] p1){
        System.out.println("Printing P-Set:");
        for(int i=0; i<p.length; i++){
        System.out.println( ((parameter)p[i]).getName());
        System.out.println( ((parameter)p[i]).getValue() + " vs "+((parameter)p1[i]).getValue());

        }
    }

    public void addToListOfParameterSetsToBeWritten(parameter[] p){

    //Long tmpId=getIDOfParameterSet(p);
    //System.out.println("ID to Add: "+tmpId);
        //for(int i=0;i<ListOfParameterSetIds.size();i++){
        //    if(ListOfParameterSetIds.get(i).compareTo(tmpId)==0 ){
            //printParameterSetCompare(ListOfParameterSetsToBeWritten.get(i), p);
        //    }
        //}
    
    ListOfParameterSetsToBeWritten.add(p);
    //ListOfParameterSetIds.add(tmpId);

    this.jLabelExportStatus.setText("Building Parametersets:"+ListOfParameterSetsToBeWritten.size()*100/this.sizeOfDesignSpace +"%");

    }

        /**
     calculates list of parameters from table
     * this is the base list with start/end/stepping values
     **/
    public parameter[] getParameterBase(){
    int parameterCount=this.jTableParameterList.getModel().getRowCount();
    parameterTableModel tModel=(parameterTableModel) this.jTableParameterList.getModel();
    //String [][] parameterArray=tModel.getParameterArray();
    parameter[] parameterArray=new parameter[tModel.getRowCount()];

    //ArrayListe aufbauen und Funktion mit dieser Liste aufrufen
    //ArrayList <parameter>ListOfParameterAsFromTable=new ArrayList();//wird in rekursiver Funktion verkleinert
        for (int i=0; i<tModel.getRowCount();i++){
        parameter tmpParameter=new parameter();
        tmpParameter.setName(tModel.getValueAt(i, 0).toString());
        tmpParameter.setValue(tModel.getValueAt(i, 1).toString());
        tmpParameter.setStartValue(tModel.getValueAt(i, 1).toString());//=StartValue
        tmpParameter.setEndValue(tModel.getValueAt(i, 2).toString());
        tmpParameter.setStepping(tModel.getValueAt(i, 3).toString());
        //ListOfParameterAsFromTable.add(tmpParameter);
        parameterArray[i]=tmpParameter;
        }
    return parameterArray;
    }

    /*
     * To display the actual Simulation Count
     */
    public void setSimulationCounter(int i){
        if(i>0){
        this.jLabelSimulationCount.setText(String.valueOf(i));
        }else{
        this.jLabelSimulationCount.setText("");
        }
    }
    
    
    /**
     * Returns List of MeasureTypes, given by Tabbed-Pane, to which it should be optimized
     */
    public ArrayList<MeasureType> getListOfActiveMeasureMentsToOptimize(){
    ArrayList<MeasureType> myTmpList=new ArrayList<MeasureType>();//((MeasurementForm)this.jTabbedPane1.getComponent(0)).getListOfMeasurements();

        for(int i=0; i<this.jTabbedPane1.getComponentCount();i++){
            if(((MeasurementForm)this.jTabbedPane1.getComponent(i)).isActive()){
            myTmpList.add(((MeasurementForm)this.jTabbedPane1.getComponent(i)).getChosenMeasurement());
            }
        }
    return myTmpList;
    }
    

}
