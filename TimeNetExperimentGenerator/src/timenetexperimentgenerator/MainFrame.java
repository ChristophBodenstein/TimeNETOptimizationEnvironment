/*
 * Main Frame for TimeNetExperimentGenerator
 * provides many additional features
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;


import timenetexperimentgenerator.helper.*;
import timenetexperimentgenerator.datamodel.*;
import timenetexperimentgenerator.simulation.*;
import timenetexperimentgenerator.optimization.*;
import java.awt.Color;
import java.awt.Component;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
ArrayList < ArrayList<parameter> >ListOfParameterSetsToBeWritten=new ArrayList< ArrayList<parameter> >();//Name, Value
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
private SimulationCache mySimulationCache=null;
private String pathToLastSimulationCache="";
private SimulationTypeComboBoxModel mySimulationTypeModel=new SimulationTypeComboBoxModel();
private DefaultComboBoxModel myOptiTypeModel=new DefaultComboBoxModel();
SimulatorWebSlave mySlave=new SimulatorWebSlave();


    /** Creates new form MainFrame */
    public MainFrame() {
    support.setMainFrame(this);
    
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
        
        pConfidenceIntervall.initWithValues("ConfidenceIntervall", 95, 95, 1); 
        pSeed.initWithValues("Seed", 0, 0, 1);
        pEndTime.initWithValues("EndTime",0,0,1);
        pMaxTime.initWithValues("MaxTime",0,0,1);
        pMaxError.initWithValues("MaxError",5,5,1);

        this.jTextFieldSCPNFile.setText(auto.getProperty("file"));
        //this.jTextFieldPathToTimeNet.setText(auto.getProperty("timenetpath"));
        this.setPathToTimeNet(auto.getProperty("timenetpath"));
        //support.log("Read Path to TimeNet:"+auto.getProperty("timenetpath"));
        
        this.pConfidenceIntervall.setStartValue(this.loadDouble("ConfidenceIntervallStart",pConfidenceIntervall.getStartValue()));
        this.pConfidenceIntervall.setEndValue(this.loadDouble("ConfidenceIntervallEnd",pConfidenceIntervall.getEndValue()));
        this.pConfidenceIntervall.setStepping(this.loadDouble("ConfidenceIntervallStepping",pConfidenceIntervall.getStepping()));

        this.pEndTime.setStartValue(this.loadDouble("EndTimeStart",pEndTime.getStartValue()));
        this.pEndTime.setEndValue(this.loadDouble("EndTimeEnd",pEndTime.getEndValue()));
        this.pEndTime.setStepping(this.loadDouble("EndTimeStepping",pEndTime.getStepping()));

        this.pMaxTime.setStartValue(this.loadDouble("MaxTimeStart",pMaxTime.getStartValue()));
        this.pMaxTime.setEndValue(this.loadDouble("MaxTimeEnd",pMaxTime.getEndValue()));
        this.pMaxTime.setStepping(this.loadDouble("MaxTimeStepping",pMaxTime.getStepping()));

        this.pSeed.setStartValue(this.loadDouble("SeedStart",pSeed.getStartValue()));
        this.pSeed.setEndValue(this.loadDouble("SeedEnd",pSeed.getEndValue()));
        this.pSeed.setStepping(this.loadDouble("SeedStepping",pSeed.getStepping()));

        this.pMaxError.setStartValue(this.loadDouble("MaxErrorStart",pMaxError.getStartValue()));
        this.pMaxError.setEndValue(this.loadDouble("MaxErrorEnd",pMaxError.getEndValue()));
        this.pMaxError.setStepping(this.loadDouble("MaxErrorStepping",pMaxError.getStepping()));

        this.pathToLastSimulationCache=auto.getProperty("pathToLastSimulationCache", "");

        support.setIsRunningAsSlave(Boolean.parseBoolean(auto.getProperty("isRunningAsSlave")));
        
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
                            //TODO: get List of external Parameters from Support-Class!!!
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
        
        this.checkIfCachedSimulationIsPossible();
        
        
        for (String SIMTYPES : support.SIMTYPES) {
        mySimulationTypeModel.addElement(SIMTYPES);
        }

        for (String OPTITYPES : support.OPTITYPES){
        myOptiTypeModel.addElement(OPTITYPES);
        }
        this.jComboBoxOptimizationType.setModel(myOptiTypeModel);


        this.updateComboBoxSimulationType();
        
        if(support.isIsRunningAsSlave()){
        new Thread(this.mySlave).start();
        }
        
    }


    /**
     * Updates the combobox for selection of Simulation type
     * if cache is available --> Selection is possible
     * if server is available --> Selection of Distr. is available
     */
    private void updateComboBoxSimulationType(){
    DefaultListSelectionModel model = new DefaultListSelectionModel();
    model.addSelectionInterval(0, 0);
    model.addSelectionInterval(2, 2);
    if(support.isCachedSimulationAvailable()){
    model.addSelectionInterval(1, 1);
    }
    
    this.jComboBoxSimulationType.setRenderer(new EnabledJComboBoxRenderer(model));
    
    this.jComboBoxSimulationType.setModel(mySimulationTypeModel);
    }


    /**
     * Check, if cached simulation is possible
     * if cached simulation is possible, then set some switches etc...
     */
    private void checkIfCachedSimulationIsPossible(){
    
        if(mySimulationCache!=null){
            if(mySimulationCache.checkIfAllParameterMatchTable((parameterTableModel)this.jTableParameterList.getModel())){
            support.setMySimulationCache(mySimulationCache);
            support.setCachedSimulationEnabled(true);
            }
        }
    this.updateComboBoxSimulationType();
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
        jComboBoxSimulationType = new javax.swing.JComboBox();
        jComboBoxOptimizationType = new javax.swing.JComboBox();
        jButtonEnterURLToSimServer = new javax.swing.JButton();
        jCheckBoxSlaveSimulator = new javax.swing.JCheckBox();
        jComboBoxTypeOfStartValue = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(675, 600));
        setMinimumSize(new java.awt.Dimension(675, 600));
        setResizable(false);

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

        jScrollPane1.setMinimumSize(new java.awt.Dimension(469, 404));

        jTableParameterList.setAutoCreateRowSorter(true);
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
        jTableParameterList.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTableParameterList.setMinimumSize(new java.awt.Dimension(20, 24));
        jTableParameterList.setPreferredSize(new java.awt.Dimension(469, 404));
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

        jLabelExportStatus.setBackground(new java.awt.Color(255, 255, 204));
        jLabelExportStatus.setToolTipText("Program Status");
        jLabelExportStatus.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabelExportStatus.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabelExportStatus.setMinimumSize(new java.awt.Dimension(50, 20));
        jLabelExportStatus.setPreferredSize(new java.awt.Dimension(50, 20));

        jButtonLoadCacheFile.setText("Load Cached Simulation Results");
        jButtonLoadCacheFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadCacheFileActionPerformed(evt);
            }
        });

        jComboBoxSimulationType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sim" }));
        jComboBoxSimulationType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxSimulationTypeItemStateChanged(evt);
            }
        });

        jComboBoxOptimizationType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Greedy", "Sim. Annealing", " " }));
        jComboBoxOptimizationType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxOptimizationTypeItemStateChanged(evt);
            }
        });
        jComboBoxOptimizationType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxOptimizationTypeActionPerformed(evt);
            }
        });

        jButtonEnterURLToSimServer.setText("Enter URL of Sim.-Server");
        jButtonEnterURLToSimServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEnterURLToSimServerActionPerformed(evt);
            }
        });

        jCheckBoxSlaveSimulator.setText("be a Slave");
        jCheckBoxSlaveSimulator.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxSlaveSimulatorItemStateChanged(evt);
            }
        });

        jComboBoxTypeOfStartValue.setModel(new DefaultComboBoxModel(support.typeOfStartValueEnum.values()));

        jLabel1.setText("Start at");

        jMenu1.setText("File");
        jMenu1.add(jSeparator4);
        jMenu1.add(jSeparator5);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        jMenuItem1.setText("Quit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jMenu3.setText("Log");

        jMenuItem3.setText("Clear Log");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuItem4.setText("Print all Statistics in Log");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem4);

        jMenuItem2.setText("Open Log-Window");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jSeparator1))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jButtonPathToTimeNet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jButtonEnterURLToSimServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jCheckBoxSlaveSimulator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 198, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(5, 5, 5))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 373, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 26, Short.MAX_VALUE))
                                .add(layout.createSequentialGroup()
                                    .add(jTextFieldSCPNFile)
                                    .add(5, 5, 5))))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabelSimulationCount, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(jButtonOpenSCPN, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(5, 5, 5)
                                .add(jButtonReload, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(jSeparator2)
                            .add(layout.createSequentialGroup()
                                .add(jButtonExport, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                .add(5, 5, 5)
                                .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(jButtonGenerateListOfExperiments, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jButtonStartBatchSimulation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jSeparator3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonLoadCacheFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(20, 20, 20)
                                .add(jButtonStartOptimization, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jComboBoxSimulationType, 0, 135, Short.MAX_VALUE)
                                    .add(layout.createSequentialGroup()
                                        .add(6, 6, 6)
                                        .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .add(5, 5, 5)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(jComboBoxTypeOfStartValue, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(jComboBoxOptimizationType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 121, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(5, 5, 5))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabelExportStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 265, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(5, 5, 5))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldSCPNFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonOpenSCPN)
                    .add(jButtonReload))
                .add(5, 5, 5)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButtonExport)
                            .add(jButton1))
                        .add(5, 5, 5)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jButtonGenerateListOfExperiments)
                        .add(5, 5, 5)
                        .add(jButtonStartBatchSimulation)
                        .add(5, 5, 5)
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jButtonLoadCacheFile)
                        .add(5, 5, 5)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jComboBoxSimulationType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jComboBoxOptimizationType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jComboBoxTypeOfStartValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 6, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jButtonPathToTimeNet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonStartOptimization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jButtonEnterURLToSimServer)
                        .add(5, 5, 5)
                        .add(jCheckBoxSlaveSimulator))
                    .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
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
        support.log("getCurrentDirectory(): "
         +  fileChooser.getCurrentDirectory());
        support.log("getSelectedFile() : "
         +  fileChooser.getSelectedFile());
        this.jTextFieldSCPNFile.setText(fileChooser.getSelectedFile().toString());
        this.readSCPNFile(fileChooser.getSelectedFile().toString());
        this.saveProperties();
      }
    else {
      support.log("No Selection ");
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
        support.log("Length of ParameterSet-List: "+ListOfParameterSetsToBeWritten.size());
        exporter tmpExporter=new exporter(ListOfParameterSetsToBeWritten);
        }else{
        support.log("Export-Operation cancled.");
        }
    this.cancelOperation=false;
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    this.cancelOperation=true;    
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Start of batch simulation
     */
    private void jButtonStartBatchSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartBatchSimulationActionPerformed
        //Ask for Tmp-Path
        support.setTmpPath(support.getPathToDirByDialog("Dir for export TMP-Files and log.\n "+"Go INTO the dir to choose it!", new File(support.getOriginalFilename()).getPath() ));
        support.setPathToTimeNet(pathToTimeNet);
        support.setMainFrame(this);
        support.setOriginalFilename(fileName);
        support.setStatusLabel(jLabelExportStatus);
        support.setMeasureFormPane(jTabbedPane1);
    
        //LocalBatchSimulatorEngine mySimulator=new LocalBatchSimulatorEngine(ListOfParameterSetsToBeWritten);
        //SimulatorLocal mySimulator=new SimulatorLocal();
        Simulator mySimulator=SimOptiFactory.getSimulator();
        mySimulator.initSimulator(ListOfParameterSetsToBeWritten, 0, true);
        
    
    }//GEN-LAST:event_jButtonStartBatchSimulationActionPerformed

    private void jButtonGenerateListOfExperimentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateListOfExperimentsActionPerformed
    this.restartGenerator();
    }//GEN-LAST:event_jButtonGenerateListOfExperimentsActionPerformed

    private void jButtonStartOptimizationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartOptimizationActionPerformed
        if(this.sizeOfDesignSpace<=10){
        //TODO check , if opti is possible (target chosen etc.)
        support.log("Design space to small, no Optimization posible.");
        }else{
                if(this.getListOfActiveMeasureMentsToOptimize().size()>=1){
                //Ask for Tmp-Path
                support.setTmpPath(support.getPathToDirByDialog("Dir for export TMP-Files and log.\n "+"Go INTO the dir to choose it!",  new File(support.getOriginalFilename()).getPath()) );
                //TODO if tmpPath is empty or null --> return
                    if(support.getTmpPath()!=null){
                    support.setPathToTimeNet(pathToTimeNet);
                    support.setMainFrame(this);
                    support.setOriginalFilename(fileName);
                    support.setStatusLabel(jLabelExportStatus);
                    support.setMeasureFormPane(jTabbedPane1);
                    support.setTypeOfStartValue((support.typeOfStartValueEnum)this.jComboBoxTypeOfStartValue.getSelectedItem());
                    Optimizer myOptimizer=SimOptiFactory.getOptimizer();
                    myOptimizer.initOptimizer();
                    }else{
                    support.log("No Tmp-Path given, Optimization not possible.");
                    }
                    
                    
                }else{
                support.log("No Measurements to optimize for are chosen.");
                }
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
        support.log("choosen outputdir: "+outputDir);
        this.setPathToTimeNet(outputDir);
        this.checkIfTimeNetPathIsCorrect();
      }else{
      support.log("No Path to TimeNet chosen.");
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
            support.log("No input file chosen!");
            return;
        }else{
            inputFile=fileChooser.getSelectedFile().toString();
        }
        support.log("choosen input file with cached simulation results: "+inputFile);
      }else{
      support.log("No input file chosen!");
      return;
      }  
      this.mySimulationCache=SimOptiFactory.getSimulationCache();
        if(!mySimulationCache.parseSimulationCacheFile(inputFile,((MeasurementForm)this.jTabbedPane1.getComponent(0)).getListOfMeasurements(), (parameterTableModel)this.jTableParameterList.getModel(),this )){
            support.log("Wrong Simulation cache file for this SCPN!");
        }else{
        this.pathToLastSimulationCache=fileChooser.getSelectedFile().getPath();
        this.saveProperties();
        }
    this.checkIfCachedSimulationIsPossible();
    }//GEN-LAST:event_jButtonLoadCacheFileActionPerformed

    private void jComboBoxOptimizationTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOptimizationTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxOptimizationTypeActionPerformed

    private void jComboBoxSimulationTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxSimulationTypeItemStateChanged
        support.setChosenSimulatorType(this.jComboBoxSimulationType.getSelectedIndex());
    }//GEN-LAST:event_jComboBoxSimulationTypeItemStateChanged

    private void jComboBoxOptimizationTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxOptimizationTypeItemStateChanged
        support.setChosenOptimizerType(this.jComboBoxOptimizationType.getSelectedIndex());
    }//GEN-LAST:event_jComboBoxOptimizationTypeItemStateChanged

    private void jButtonEnterURLToSimServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEnterURLToSimServerActionPerformed
        
        String s = (String)JOptionPane.showInputDialog(
                    this,
                    "Enter URL of Simulation Server:\n",
                    "URL of Simulation Server",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    support.getReMoteAddress());

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
        support.log("URL of Simulation-Server as given from user is " + s + "!");
        support.setRemoteAddress(s);
        this.saveProperties();
        }else{
        support.log("URL of Simulation-Server was not entered!");    
        }
    }//GEN-LAST:event_jButtonEnterURLToSimServerActionPerformed

    private void jCheckBoxSlaveSimulatorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxSlaveSimulatorItemStateChanged
        //set Property for startup
        //start the Slave-Thread
        support.setIsRunningAsSlave(this.jCheckBoxSlaveSimulator.isSelected());
        this.saveProperties();
        if(support.isIsRunningAsSlave()){
        
        new Thread(this.mySlave).start();
        }else{
        this.mySlave.setShouldEnd(true);
        }
        
    }//GEN-LAST:event_jCheckBoxSlaveSimulatorItemStateChanged

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
    StatisticAggregator.printAllStatistics();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
    support.getMyLogFrame().setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
    support.getMyLogFrame().clearText();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    /**
     * Calculates the design space, number of all permutations of parameters
     * with respect to the stepping sizes
     */
    public void calculateDesignSpace(){
        myGenerator=new generator(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this, jTableParameterList);
        this.sizeOfDesignSpace=myGenerator.getSizeOfDesignspace();
        this.jLabelExportStatus.setText("Designspace-Size:"+ sizeOfDesignSpace);
    }

    
    /**
     * Builds list of parametersets based on parameters and steppings from table
     * is used recursive
     * @param ListOfParameterAsFromTable List of parameters from table
     * @param ListOfParameterSetsToBeWritten List of parameterset to be simulated, this is the designspace
     * @param lastParameterSet the last parameterset while generating recursive
     * @param infoLabel Label to display some information while generating
     */
    public void buildListOfParameterSetsToExport(ArrayList ListOfParameterSetsToBeWritten, ArrayList ListOfParameterAsFromTable, ArrayList<parameter> lastParameterSet, JLabel infoLabel){
    boolean isAlreadyInExportList=false;
    
    if(cancelOperation){
    this.activateReloadButtons();
        return;
    }
        if(ListOfParameterAsFromTable.size()>0){
        parameter loopParameter=(parameter)ListOfParameterAsFromTable.get(ListOfParameterAsFromTable.size()-1);

        ListOfParameterAsFromTable.remove(loopParameter);

        String loopName=loopParameter.getName();
        boolean canIterate=true;

        double start=1, end=1, step=1;
            try{
            start=support.getDouble(loopParameter.getStartValue());
            end=support.getDouble(loopParameter.getEndValue());
            step=support.getDouble(loopParameter.getStepping());
            canIterate=true;
            }catch(NumberFormatException e){
            support.log("Could not convert into double, maybe String is used. Will not iterate through parameter "+loopParameter.getName());
            return;
            }

            if(canIterate){
            double usedValue;
            int endCounter=1;
                    if((end-start)>0){
                        endCounter=(int)Math.ceil((end-start)/step) +1 ;
                    }
            
                for(int i=0;i<endCounter;i++){
                usedValue=start+(double)i*step;
                usedValue=support.round(usedValue);
                ArrayList<parameter> nextParameterSet = new ArrayList<parameter>();
                    //Get copy of paremeterset
                    for(int c=0;c<lastParameterSet.size();c++){
                        try{nextParameterSet.add((parameter) lastParameterSet.get(c).clone());}
                        catch(CloneNotSupportedException e){
                        support.log("Clone is not Supported:"+e.toString());
                        }

                    }

                    for(int c=0;c<nextParameterSet.size();c++){
                        if(nextParameterSet.get(i).getName().equals(loopName)){
                        //set modified parameterset
                        nextParameterSet.get(i).setValue(usedValue);
                        }
                    }
                if(ListOfParameterAsFromTable.size()==0){
                addToListOfParameterSetsToBeWritten(nextParameterSet);}
                //call this method again with reduced parameterset
                buildListOfParameterSetsToExport(ListOfParameterSetsToBeWritten, ListOfParameterAsFromTable, nextParameterSet, infoLabel);
                }
            }else{
                //check if entry ia already in list
                //no more used...
                isAlreadyInExportList=false; 
            }
        ListOfParameterAsFromTable.add(loopParameter);
        
        }else{
        //Exit the loop, popup
        }
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
    private javax.swing.JButton jButtonEnterURLToSimServer;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonGenerateListOfExperiments;
    private javax.swing.JButton jButtonLoadCacheFile;
    private javax.swing.JButton jButtonOpenSCPN;
    private javax.swing.JButton jButtonPathToTimeNet;
    private javax.swing.JButton jButtonReload;
    private javax.swing.JButton jButtonStartBatchSimulation;
    private javax.swing.JButton jButtonStartOptimization;
    private javax.swing.JCheckBox jCheckBoxSlaveSimulator;
    private javax.swing.JComboBox jComboBoxOptimizationType;
    private javax.swing.JComboBox jComboBoxSimulationType;
    private javax.swing.JComboBox jComboBoxTypeOfStartValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelExportStatus;
    private javax.swing.JLabel jLabelSimulationCount;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableParameterList;
    private javax.swing.JTextField jTextFieldSCPNFile;
    private timenetexperimentgenerator.MeasurementForm measurementForm1;
    private timenetexperimentgenerator.MeasurementForm measurementForm2;
    // End of variables declaration//GEN-END:variables


    /**
     * Reads the xml-file of SCPN and sets the User Interface
     * @param filename String name of the SCPN-File to read
     */
    private void readSCPNFile(String filename){
        deactivateExportButtons();
        try{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	Document doc = docBuilder.parse(filename);
        NodeList parameterList=doc.getElementsByTagName("parameter");

            for(int i=0;i<parameterList.getLength();i++){
            support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            }
        jTableParameterList.setModel(new parameterTableModel(parameterList, this));
        jTableParameterList.getModel().addTableModelListener(this);

        
        //Measures auslesen
        NodeList MeasurenameList=doc.getElementsByTagName("measure");
        if(MeasurenameList.getLength()>=1){
        ArrayList<MeasureType> Measures=new ArrayList();
        support.log("****** Measure-Names ******");
            for(int i=0;i<MeasurenameList.getLength();i++){
            support.log(MeasurenameList.item(i).getAttributes().getNamedItem("name").getNodeValue());
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
    
    /**
     * removed duplicate parametersets, not used anymore?
     * @param ListOfParameterSetsToBeWritten List of parametersets to be checked for duplicates
     * @param infoLabel label to print out some information while checking
     * @return List of parametersets without duplicates
     */
    public ArrayList<parameter[]> removeDuplicates(ArrayList<parameter[]> ListOfParameterSetsToBeWritten, JLabel infoLabel){
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
                    support.log("P1: "+tmpParemeterSetID);
                    support.log("P2: "+tmpListParameterID);
                    * */

                    if(tmpListParameterID==tmpParameterSetID){
                    existsInOutPutList=true;
                    support.log("These Parameters are equal:");
                    support.log(tmpParameterSetID + " and " + tmpListParameterID);
                    printParameterSetCompare(tmpParameterSet, tmpListParameter);
                    
                    }
                }
            }
            if(!existsInOutPutList){
            tmpList.add(tmpParameterSet);
            }
        }
        /*
        support.log("Size of List without duplicates: "+tmpList.size());
        for(int i=0;i<tmpList.size();i++){
        support.log("P-ID: "+ getIDOfParameterSet(tmpList.get(i)) );
        }
         */
    return tmpList;
    }

    /**
     * Returns id of a parameterset to compare with other parametersets
     * @param parameterset Set of parameters to calculate the id from
     * @return ID of whole parameterset
     */
    public long getIDOfParameterSet(parameter[] parameterset){
    long id=0;
    String tmpString="";
    //Arrays.sort(parameterset);
        for(int i=0;i<parameterset.length;i++){
        id=id+parameterset[i].getID();
        //support.log("ID of:"+ parameterset[i].getName()+" is " +parameterset[i].getID());
        tmpString=tmpString+String.valueOf(parameterset[i].getID());
        }
    //return id;
    return (long)tmpString.hashCode();
    }

    
    /**
     * Returns next spinning char for displaying a spinning wheel in a label
     * @param oldChar actual char in label to calculate the next
     * @return next char to be displayed in label
     */
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
    ListOfParameterSetsToBeWritten=new ArrayList<ArrayList<parameter>>();
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
            while(this.myGenerator.isAlive()){
            Thread.sleep(500);
            }
        }catch (InterruptedException e){
            support.log("Error while waiting for Generator.");
        }
    }

    /**
     * Restarts the generator after Table has changed
     * @param e
     */
    public void tableChanged(TableModelEvent e) {
    //support.log("Editing of Cell stopped, restarting generator.");
    //this.restartGenerator();
    jButtonStartBatchSimulation.setEnabled(false);
    readStaticParametersFromTable();
    saveProperties();
    calculateDesignSpace();
    checkIfCachedSimulationIsPossible();
    }

    /**
     * Reads the simulation-parameters from table (not SCPN-Specific parameters)
     * and stores these parameters in local fields
     */
    private void readStaticParametersFromTable(){
    
    this.pConfidenceIntervall.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("ConfidenceIntervall", "StartValue")) ;
    this.pConfidenceIntervall.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("ConfidenceIntervall", "EndValue")) ;
    this.pConfidenceIntervall.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("ConfidenceIntervall", "Stepping")) ;

    this.pEndTime.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("EndTime", "StartValue")) ;
    this.pEndTime.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("EndTime", "EndValue")) ;
    this.pEndTime.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("EndTime", "Stepping")) ;

    this.pMaxTime.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("MaxTime", "StartValue")) ;
    this.pMaxTime.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("MaxTime", "EndValue")) ;
    this.pMaxTime.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("MaxTime", "Stepping")) ;

    this.pSeed.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("Seed", "StartValue")) ;
    this.pSeed.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("Seed", "EndValue")) ;
    this.pSeed.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("Seed", "Stepping")) ;

    this.pMaxError.setStartValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("MaxRelError", "StartValue")) ;
    this.pMaxError.setEndValue( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("MaxRelError", "EndValue")) ;
    this.pMaxError.setStepping( ((parameterTableModel)this.jTableParameterList.getModel()).getDoubleValueByName("MaxRelError", "Stepping")) ;

    }

    /**
     * Sets the Export-Button for Experiments/Simulations De-active
     */
    protected final void deactivateExportButtons(){
    this.jButtonExport.setEnabled(false);
    this.jButtonStartBatchSimulation.setEnabled(false);
    //this.jButtonOpenSCPN.setEnabled(false);
    this.jButtonReload.setEnabled(false);
    this.jButtonStartOptimization.setEnabled(false);
    this.jButtonGenerateListOfExperiments.setEnabled(false);
    }
    /**
     * Sets the Export-Button for Experiments/Simulations active
     */
    public void activateExportButtons(){
    this.jButtonExport.setEnabled(true);
    //this.jButtonOpenSCPN.setEnabled(true);
    this.jButtonReload.setEnabled(true);
    this.jButtonStartOptimization.setEnabled(true);
    this.jButtonGenerateListOfExperiments.setEnabled(true);
    this.checkIfTimeNetPathIsCorrect();
    }
    /**
     * Sets the Reload-SCPN-Button and open-SCPN-Button active
     */
    public void activateReloadButtons(){
    this.jButtonOpenSCPN.setEnabled(true);
    this.jButtonReload.setEnabled(true);
    }
    /**
     * Sets the Generate- and OptimizeButtons active
     */
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
    support.log("TimeNet should be here: "+tmpFile.getAbsolutePath());
        if(tmpFile.exists()){
        this.jButtonStartBatchSimulation.setEnabled(true);
        //this.jLabelCheckPathToTimeNet.setVisible(false);
        jButtonPathToTimeNet.setBackground(Color.GREEN);
        jButtonPathToTimeNet.setOpaque(true);
        jButtonPathToTimeNet.setBorderPainted(false);
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
            support.log("Failed to install RemoteSystem Clent.config");
            }
        }else{
        
        this.jButtonStartBatchSimulation.setEnabled(false);
        //this.jLabelCheckPathToTimeNet.setVisible(true);
        jButtonPathToTimeNet.setBackground(Color.RED);
        jButtonPathToTimeNet.setOpaque(true);
        jButtonPathToTimeNet.setBorderPainted(true);
        jButtonPathToTimeNet.setText("Enter Path To TimeNet");
        jButtonStartOptimization.setEnabled(false);
        
        
        }
    }

    /**
     * Saves program-properties to a local file in home-dir
     */
    private void saveProperties(){
    support.log("Saving Properties.");
        try{
    auto.setProperty("timenetpath", this.getPathToTimeNet());
    auto.setProperty("file", this.jTextFieldSCPNFile.getText().toString());
    
    auto.setProperty("ConfidenceIntervallStart",support.getString(this.pConfidenceIntervall.getStartValue()));
    auto.setProperty("ConfidenceIntervallEnd",support.getString(this.pConfidenceIntervall.getEndValue()));
    auto.setProperty("ConfidenceIntervallStepping",support.getString(this.pConfidenceIntervall.getStepping()));

    auto.setProperty("EndTimeStart",support.getString(this.pEndTime.getStartValue()));
    auto.setProperty("EndTimeEnd",support.getString(this.pEndTime.getEndValue()));
    auto.setProperty("EndTimeStepping",support.getString(this.pEndTime.getStepping()));
        
    auto.setProperty("MaxTimeStart",support.getString(this.pMaxTime.getStartValue()));
    auto.setProperty("MaxTimeEnd",support.getString(this.pMaxTime.getEndValue()));
    auto.setProperty("MaxTimeStepping",support.getString(this.pMaxTime.getStepping()));
    auto.setProperty("SeedStart",support.getString(this.pSeed.getStartValue()));
    auto.setProperty("SeedEnd",support.getString(this.pSeed.getEndValue()));
    auto.setProperty("SeedStepping",support.getString(this.pSeed.getStepping()));
        
    auto.setProperty("MaxErrorStart",support.getString(this.pMaxError.getStartValue()));
    auto.setProperty("MaxErrorEnd",support.getString(this.pMaxError.getEndValue()));
    auto.setProperty("MaxErrorStepping",support.getString(this.pMaxError.getStepping()));
    
    auto.setProperty("pathToLastSimulationCache", this.pathToLastSimulationCache);
    
    auto.setProperty("OptimizationType", support.getChosenOptimizerType().toString());
    auto.setProperty("SimulationType", support.getChosenSimulatorType().toString());
    
    auto.setProperty("isRunningAsSlave", Boolean.toString(support.isIsRunningAsSlave()));
    
    File parserprops =  new File(propertyFile);
    auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
        }catch(IOException e){
        support.log("Problem Saving the properties.");
        }

    }

    /**
     * Returns a String, if String is != null, else defaultvalue.
     * If defaultvalue is null, then returns "0"
     * @param loadedValue String to be checked and returned
     * @param defaultValue Defaultvalue to be returned if String is null
     * @returns loadedValue if != null, else defaultValue or "0"
     */
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

    /**
     * Return double value of loaded property
     * If any error occurs, the given default value is returned
     * @param name Name of the property to be loaded
     * @param defaultValue The default to be returned, if error occurs
     * @return double value of property
     */
    private double loadDouble(String name, double defaultValue){
        try{
        return Double.valueOf(auto.getProperty(name));
        }catch(Exception e){
        support.log("Error loading property: "+name);
        return defaultValue;
        }
    }

    /**
     * Prints 2 parametersets and it`s values to see the difference
     * Just used for debug reasons
     * @param p parameter to be compared with p1
     * @param p1 parameter to be compared with p
     */
    private void printParameterSetCompare(parameter[] p, parameter[] p1){
        support.log("Printing P-Set:");
        for(int i=0; i<p.length; i++){
        support.log( ((parameter)p[i]).getName());
        support.log( ((parameter)p[i]).getValue() + " vs "+((parameter)p1[i]).getValue());

        }
    }

    /**
     * Adds a parameterset to the list of parametersets
     * @param p parameterset to be added
     */
    public void addToListOfParameterSetsToBeWritten(ArrayList<parameter> p){
    ListOfParameterSetsToBeWritten.add(p);
    this.jLabelExportStatus.setText("Building Parametersets:"+ListOfParameterSetsToBeWritten.size()*100/this.sizeOfDesignSpace +"%");

    }

     /**
     * calculates list of parameters from table
     * this is the base list with start/end/stepping values
     * @return List of Parameters from Table (Base of Parameter Iterations)
     **/
    public ArrayList<parameter> getParameterBase(){
    //int parameterCount=this.jTableParameterList.getModel().getRowCount();
    parameterTableModel tModel=(parameterTableModel) this.jTableParameterList.getModel();
    //String [][] parameterArray=tModel.getParameterArray();
    ArrayList<parameter> parameterArray = new ArrayList<parameter>();

    //ArrayListe aufbauen und Funktion mit dieser Liste aufrufen
    
        for (int i=0; i<tModel.getRowCount();i++){
        parameter tmpParameter=new parameter();
        tmpParameter.setName(tModel.getValueAt(i, 0).toString());
        tmpParameter.setStartValue(tModel.getDoubleValueAt(i, 1));//=StartValue
        tmpParameter.setEndValue(tModel.getDoubleValueAt(i, 2));
        tmpParameter.setValue(tModel.getDoubleValueAt(i, 1));
        
        tmpParameter.setStepping(tModel.getDoubleValueAt(i, 3));
        //ListOfParameterAsFromTable.add(tmpParameter);
        parameterArray.add(tmpParameter);
        }
    return parameterArray;
    }

    /**
     * To display the actual Simulation Count
     * @param i value of simulationcounter to display
     */
    public void updateSimulationCounterLabel(int i){
        if(i>0){
        this.jLabelSimulationCount.setText(String.valueOf(i));
        }else{
        this.jLabelSimulationCount.setText("");
        }
    }
    
    
    /**
     * Returns list of MeasureTypes to be optimized
     * @return List of MeasureTypes, given by Tabbed-Pane, to which it should be optimized
     */
    public ArrayList<MeasureType> getListOfActiveMeasureMentsToOptimize(){
    ArrayList<MeasureType> myTmpList=new ArrayList<MeasureType>();//((MeasurementForm)this.jTabbedPane1.getComponent(0)).getListOfMeasurements();

        for(int i=0; i<this.jTabbedPane1.getComponentCount();i++){
            MeasurementForm tmpMeasurementForm=(MeasurementForm)this.jTabbedPane1.getComponent(i);
            if(tmpMeasurementForm.isActive()){
            MeasureType tmpMeasure=tmpMeasurementForm.getChosenMeasurement();
            float targetValue=tmpMeasurementForm.getCustomTargetValue();
            String targetKind=tmpMeasurementForm.getOptimizationTarget();
            tmpMeasure.setTargetValue(targetValue, targetKind);
            myTmpList.add(tmpMeasure);
            }
        }
    return myTmpList;
    }

    /**
     * Set local variable of path to Timenet
     * @param pathToTimeNet the pathToTimeNet to set
     */
    private void setPathToTimeNet(String pathToTimeNet) {
        this.pathToTimeNet = pathToTimeNet;   
    }
    
    /**
     * returns local value of path to TimeNet
     */
    private String getPathToTimeNet(){
    return this.pathToTimeNet;
    }
    
    
    
}
