/*
 * Main Frame for TimeNetExperimentGenerator
 * provides many additional features
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
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Christoph Bodenstein
 */
public class MainFrame extends javax.swing.JFrame implements TableModelListener{
private String propertyFile=System.getProperty("user.home")+File.separatorChar+ ".ExperimentGeneratorprops.prop";
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
private String pathToTimeNet="";
private SimulationCache mySimulationCache=new SimulationCache();
private String pathToLastSimulationCache="";


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
        
        jButtonPathToTimeNet.setBackground(Color.GRAY);
        jButtonPathToTimeNet.setText("Enter Path To TimeNet");
        
        pConfidenceIntervall.initWithValues("ConfidenceIntervall", "95", "95", "1"); 
        pSeed.initWithValues("Seed", "0", "0", "1");
        pEndTime.initWithValues("EndTime","0","0","1");
        pMaxTime.initWithValues("MaxTime","0","0","1");
        pMaxError.initWithValues("MaxError","5","5","1");

        this.jTextFieldSCPNFile.setText(auto.getProperty("file"));
        //this.jTextFieldPathToTimeNet.setText(auto.getProperty("timenetpath"));
        this.setPathToTimeNet(auto.getProperty("timenetpath"));
        //System.out.println("Read Path to TimeNet:"+auto.getProperty("timenetpath"));
        
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

        this.pathToLastSimulationCache=auto.getProperty("pathToLastSimulationCache", "");

        this.checkIfTimeNetPathIsCorrect();
        this.deactivateExportButtons();

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


        //Reload the last File
        this.readSCPNFile(jTextFieldSCPNFile.getText());
        support.setStatusLabel(jLabelExportStatus);
        support.setMainFrame(this);
        support.setMeasureFormPane(jTabbedPane1);
        support.setPathToTimeNet(pathToTimeNet);
        
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
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jButtonStartBatchSimulation = new javax.swing.JButton();
        jButtonGenerateListOfExperiments = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        measurementForm1 = new timenetexperimentgenerator.MeasurementForm();
        measurementForm2 = new timenetexperimentgenerator.MeasurementForm();
        jButtonStartOptimization = new javax.swing.JButton();
        jLabelSimulationCount = new javax.swing.JLabel();
        jButtonPathToTimeNet = new javax.swing.JButton();
        jLabelExportStatus = new javax.swing.JLabel();
        jButtonLoadCacheFile = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

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

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonStartBatchSimulation.setText("Start batch simulation");
        jButtonStartBatchSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartBatchSimulationActionPerformed(evt);
            }
        });

        jButtonGenerateListOfExperiments.setText("Generate Design Space");
        jButtonGenerateListOfExperiments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateListOfExperimentsActionPerformed(evt);
            }
        });

        jTabbedPane1.addTab("Target 1", measurementForm1);
        jTabbedPane1.addTab("Target 2", measurementForm2);

        jButtonStartOptimization.setText("Start Optimization");
        jButtonStartOptimization.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartOptimizationActionPerformed(evt);
            }
        });

        jButtonPathToTimeNet.setText("Enter Path to TimeNet");
        jButtonPathToTimeNet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPathToTimeNetActionPerformed(evt);
            }
        });

        jButtonLoadCacheFile.setText("Load Cached Simulation Results");
        jButtonLoadCacheFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadCacheFileActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Cached Optimization");

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

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
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldSCPNFile)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(jButtonPathToTimeNet)
                                .add(0, 0, Short.MAX_VALUE)))
                        .add(28, 28, 28)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabelSimulationCount, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jCheckBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 265, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, jTabbedPane1)
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
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, jButtonStartOptimization, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(jButtonLoadCacheFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .add(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldSCPNFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonOpenSCPN)
                    .add(jButtonReload))
                .add(2, 2, 2)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .add(30, 30, 30))
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
                        .add(59, 59, 59)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonLoadCacheFile)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jCheckBox1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonStartOptimization)
                        .add(0, 19, Short.MAX_VALUE)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jButtonPathToTimeNet, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelSimulationCount, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
      }
    else {
      System.out.println("No Selection ");
      }
    }//GEN-LAST:event_jButtonOpenSCPNActionPerformed

    private void jButtonReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReloadActionPerformed
    this.readSCPNFile(jTextFieldSCPNFile.getText());
    }//GEN-LAST:event_jButtonReloadActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
        
        //Ask for Export-Path
        support.setTmpPath(support.getPathToDirByDialog("Dir for export of xml-Files.\n "+"Go INTO the dir to choose it!", null));
        support.setPathToTimeNet(pathToTimeNet);
        support.setMainFrame(this);
        support.setOriginalFilename(fileName);
        support.setStatusLabel(jLabelExportStatus);
        support.setMeasureFormPane(jTabbedPane1);
        
        if(ListOfParameterSetsToBeWritten!=null){
        System.out.println("Length of ParameterSet-List: "+ListOfParameterSetsToBeWritten.size());
        exporter tmpExporter=new exporter(ListOfParameterSetsToBeWritten);
        }else{
        System.out.println("Export-Operation cancled.");
        }
    this.cancelOperation=false;
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    this.cancelOperation=true;    
    }//GEN-LAST:event_jButton1ActionPerformed

    public String getPathToTimeNet(){
    return this.pathToTimeNet;
    }

    /**
     * Start of batch simulation
     */
    private void jButtonStartBatchSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartBatchSimulationActionPerformed
        //Ask for Tmp-Path
        support.setTmpPath(support.getPathToDirByDialog("Dir for export TMP-Files and log.\n "+"Go INTO the dir to choose it!", null));
        support.setPathToTimeNet(pathToTimeNet);
        support.setMainFrame(this);
        support.setOriginalFilename(fileName);
        support.setStatusLabel(jLabelExportStatus);
        support.setMeasureFormPane(jTabbedPane1);
    
        BatchSimulator mySimulator=new BatchSimulator(ListOfParameterSetsToBeWritten);
    
    }//GEN-LAST:event_jButtonStartBatchSimulationActionPerformed

    private void jButtonGenerateListOfExperimentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateListOfExperimentsActionPerformed
    this.restartGenerator();
    }//GEN-LAST:event_jButtonGenerateListOfExperimentsActionPerformed

    private void jButtonStartOptimizationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartOptimizationActionPerformed
        if(this.sizeOfDesignSpace<=10){
        
        }else   {
                //Ask for Tmp-Path
                support.setTmpPath(support.getPathToDirByDialog("Dir for export TMP-Files and log.\n "+"Go INTO the dir to choose it!", null));
                support.setPathToTimeNet(pathToTimeNet);
                support.setMainFrame(this);
                support.setOriginalFilename(fileName);
                support.setStatusLabel(jLabelExportStatus);
                support.setMeasureFormPane(jTabbedPane1);
                Optimizer myOptimizer=SimOptiFactory.getOptimizer();
                myOptimizer.initOptimizer();
                }
    }//GEN-LAST:event_jButtonStartOptimizationActionPerformed

    private void jButtonPathToTimeNetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPathToTimeNetActionPerformed
    JFileChooser fileChooser = new JFileChooser(this.getPathToTimeNet());
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    fileChooser.setControlButtonsAreShown(true);
    fileChooser.setDialogTitle(" Choose Dir of TimeNet ");
    String outputDir;


      if (fileChooser.showDialog(this, "Choose this") == JFileChooser.APPROVE_OPTION) {
        if(fileChooser.getSelectedFile().isDirectory() ){
            outputDir=fileChooser.getSelectedFile().toString();
        }else{
            outputDir=fileChooser.getCurrentDirectory().toString();
        }
        System.out.println("choosen outputdir: "+outputDir);
        this.setPathToTimeNet(outputDir);
        this.checkIfTimeNetPathIsCorrect();
      }else{
      System.out.println("No Path to TimeNet chosen.");
      }  
        
        
    }//GEN-LAST:event_jButtonPathToTimeNetActionPerformed

    private void jButtonLoadCacheFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadCacheFileActionPerformed
    //JFileChooser fileChooser = new JFileChooser(this.getPathToTimeNet());
    javax.swing.filechooser.FileFilter myFilter=new javax.swing.filechooser.FileNameExtensionFilter("csv file", "csv");
    JFileChooser fileChooser = new JFileChooser(this.pathToLastSimulationCache);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    fileChooser.setControlButtonsAreShown(true);
    fileChooser.setDialogTitle(" Choose File with cached simulation files ");
    fileChooser.setFileFilter(myFilter);
    String inputFile;


      if (fileChooser.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
        if(fileChooser.getSelectedFile().isDirectory() ){
            System.out.println("No input file chosen!");
            return;
        }else{
            inputFile=fileChooser.getSelectedFile().toString();
        }
        System.out.println("choosen input file with cached simulation results: "+inputFile);
      }else{
      System.out.println("No input file chosen!");
      return;
      }  
      
        if(!mySimulationCache.parseSimulationCacheFile(inputFile,((MeasurementForm)this.jTabbedPane1.getComponent(0)).getListOfMeasurements(), (parameterTableModel)this.jTableParameterList.getModel(),this )){
            System.out.println("Wrong Simulation cache file for this SCPN!");
        }else{
        this.pathToLastSimulationCache=fileChooser.getSelectedFile().getPath();
        this.saveProperties();
        }
        
    }//GEN-LAST:event_jButtonLoadCacheFileActionPerformed

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
            start=support.getFloatFromString(loopParameter.getStartValue());
            end=support.getFloatFromString(loopParameter.getEndValue());
            step=support.getFloatFromString(loopParameter.getStepping());
            canIterate=true;
            /*    if((end-start)>0){
                    if(((end-start)/step)>0){ //Iteration möglich
                    canIterate=true;
                    }
                }
            */
            }catch(NumberFormatException e){
            //canIterate=false;
            System.out.println("Could not convert into float, maybe String is used. Will not iterate through parameter "+loopParameter.getName());
            return;
            }

            if(canIterate){
            float usedValue;
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
                        catch(CloneNotSupportedException e){
                        System.out.println("Clone is not Supported:"+e.toString());
                        }

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
    private javax.swing.JButton jButtonLoadCacheFile;
    private javax.swing.JButton jButtonOpenSCPN;
    private javax.swing.JButton jButtonPathToTimeNet;
    private javax.swing.JButton jButtonReload;
    private javax.swing.JButton jButtonStartBatchSimulation;
    private javax.swing.JButton jButtonStartOptimization;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabelExportStatus;
    private javax.swing.JLabel jLabelSimulationCount;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableParameterList;
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

        
        //Measures auslesen
        NodeList MeasurenameList=doc.getElementsByTagName("measure");
        if(MeasurenameList.getLength()>=1){
        ArrayList<MeasureType> Measures=new ArrayList();
        System.out.println("****** Measure-Names ******");
            for(int i=0;i<MeasurenameList.getLength();i++){
            System.out.println(MeasurenameList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            MeasureType tmpMeasure=new MeasureType();
            tmpMeasure.setMeasureName(MeasurenameList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            Measures.add(tmpMeasure);
            }
        
            for(int i=0;i<this.jTabbedPane1.getComponentCount();i++){
            ((MeasurementForm)this.jTabbedPane1.getComponent(i)).setMeasurements(Measures);
            }
        
        }
        
        
        this.fileName=filename;//nach Erfolg, globalen filename setzen
        support.setOriginalFilename(filename);
        activateGenerateButtons();
        activateReloadButtons();
        }catch(ParserConfigurationException e){
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (DOMException e) {
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
     * @param e
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

    protected final void deactivateExportButtons(){
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
    String path=this.getPathToTimeNet();//jTextFieldPathToTimeNet.getText();
    File tmpFile=new File(path+File.separator+"TimeNET.jar");
    System.out.println("TimeNet should be here: "+tmpFile.getAbsolutePath());
        if(tmpFile.exists()){
        this.jButtonStartBatchSimulation.setEnabled(true);
        //this.jLabelCheckPathToTimeNet.setVisible(false);
        jButtonPathToTimeNet.setBackground(Color.GREEN);
        jButtonPathToTimeNet.setText("Reset Path To TimeNet");
        jButtonStartOptimization.setEnabled(true);
        support.setPathToTimeNet(path);
        this.pathToTimeNet=path;
        this.saveProperties();
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
            }catch(IOException e){
            System.out.println("Failed to install RemoteSystem Clent.config");
            }
        }else{
        //TODO Buttons zur Auswahl des TimeNet-Verzeichnisses ausgrauen
        this.jButtonStartBatchSimulation.setEnabled(false);
        //this.jLabelCheckPathToTimeNet.setVisible(true);
        jButtonPathToTimeNet.setBackground(Color.GRAY);
        jButtonPathToTimeNet.setText("Enter Path To TimeNet");
        jButtonStartOptimization.setEnabled(false);
        
        
        }
    }


    private void saveProperties(){
    System.out.println("Saving Properties.");
        try{
    auto.setProperty("timenetpath", this.getPathToTimeNet());
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
    
    auto.setProperty("pathToLastSimulationCache", this.pathToLastSimulationCache);
    
    File parserprops =  new File(propertyFile);
    auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
        }catch(IOException e){
        System.out.println("Problem Saving the properties.");
        }

    }

    private String checkIfStringIsNull(String loadedValue, String defaultValue){
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
     * calculates list of parameters from table
     * this is the base list with start/end/stepping values
     * @return List of Parameters from Table (Base of Parameter Iterations)
     **/
    public parameter[] getParameterBase(){
    //int parameterCount=this.jTableParameterList.getModel().getRowCount();
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
     * @return List of MeasureTypes, given by Tabbed-Pane, to which it should be optimized
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

    /**
     * @param pathToTimeNet the pathToTimeNet to set
     */
    private void setPathToTimeNet(String pathToTimeNet) {
        this.pathToTimeNet = pathToTimeNet;
        
    }
    
    
    
    
}
