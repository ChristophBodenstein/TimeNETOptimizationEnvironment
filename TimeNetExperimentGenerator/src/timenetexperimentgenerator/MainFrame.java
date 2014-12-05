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
import timenetexperimentgenerator.plot.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
import timenetexperimentgenerator.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public final class MainFrame extends javax.swing.JFrame implements TableModelListener, SimOptiCallback {

    Properties auto = new Properties();
    private String fileName = "";
    ArrayList< ArrayList<parameter>> ListOfParameterSetsToBeWritten = new ArrayList< ArrayList<parameter>>();//Name, Value
    generator myGenerator;
    private parameter pConfidenceIntervall = new parameter();
    private parameter pSeed = new parameter();
    private parameter pEndTime = new parameter();
    private parameter pMaxTime = new parameter();
    private parameter pMaxError = new parameter();
    ArrayList<Long> ListOfParameterSetIds = new ArrayList<Long>();
    private int sizeOfDesignSpace;
    private String pathToTimeNet = "";
    private SimulationCache mySimulationCache = null;
    private String pathToLastSimulationCache = "";
    private SimulationTypeComboBoxModel mySimulationTypeModel = new SimulationTypeComboBoxModel(typeOfSimulator.values());
    SimulatorWebSlave mySlave = new SimulatorWebSlave();

    private RPlugin rplugin;
    private String pathToR = "";
    private JDialog aboutDialog;

    private boolean savePropertiesEnabled = false;

    private String logFileNameOfOptimizer = null;

    private ArrayList<Component> listOfUIComponents = new ArrayList<Component>();//List of all Components
    private ArrayList<Boolean> listOfUIStates = new ArrayList<Boolean>();
    private ArrayList<Boolean> listOfUIStatesPushed;

    /**
     * Creates new form MainFrame
     *
     * @throws java.io.IOException
     */
    public MainFrame() throws IOException {

        support.setMainFrame(this);

        initComponents();

        aboutDialog = new JDialog(MainFrame.getWindows()[0], ModalityType.DOCUMENT_MODAL);
        aboutDialog.setContentPane(new AboutPanel());
        aboutDialog.pack();
        aboutDialog.setVisible(false);

        try {
            FileInputStream in = new FileInputStream(support.NAME_OF_PREFERENCES_FILE);
            auto.load(in);
            in.close();
        } catch (IOException e) {
            // Exception bearbeiten
        }

        jButtonPathToTimeNet.setBackground(Color.GRAY);
        jButtonPathToTimeNet.setText("Enter Path To TimeNet");

        jButtonPathToR.setBackground(Color.GRAY);
        jButtonPathToR.setText("Enter Path To R");

        pConfidenceIntervall.initWithValues("ConfidenceIntervall", 95, 95, 1);
        pSeed.initWithValues("Seed", 0, 0, 1);
        pEndTime.initWithValues("EndTime", 0, 0, 1);
        pMaxTime.initWithValues("MaxTime", 0, 0, 1);
        pMaxError.initWithValues("MaxError", 5, 5, 1);

        this.jTextFieldSCPNFile.setText(auto.getProperty("file"));
        //this.jTextFieldPathToTimeNet.setText(auto.getProperty("timenetpath"));
        this.setPathToTimeNet(auto.getProperty("timenetpath"));
        //support.log("Read Path to TimeNet:"+auto.getProperty("timenetpath"));
        this.setPathToR(auto.getProperty("rpath"));
        //Read tmp path from properties, needed for client-mode-start
        support.setTmpPath(auto.getProperty("tmppath"));

        //set null strings to empty strings, avoids a crash when saving the configuration
        if (this.getPathToR() == null) {
            this.setPathToR("");
        }
        if (this.getPathToTimeNet() == null) {
            this.setPathToTimeNet("");
        }

        //init r plugin
        rplugin = new RPlugin();

        this.pConfidenceIntervall.setStartValue(support.loadDoubleFromProperties("ConfidenceIntervallStart", pConfidenceIntervall.getStartValue(), auto));
        this.pConfidenceIntervall.setEndValue(support.loadDoubleFromProperties("ConfidenceIntervallEnd", pConfidenceIntervall.getEndValue(), auto));
        this.pConfidenceIntervall.setStepping(support.loadDoubleFromProperties("ConfidenceIntervallStepping", pConfidenceIntervall.getStepping(), auto));

        this.pEndTime.setStartValue(support.loadDoubleFromProperties("EndTimeStart", pEndTime.getStartValue(), auto));
        this.pEndTime.setEndValue(support.loadDoubleFromProperties("EndTimeEnd", pEndTime.getEndValue(), auto));
        this.pEndTime.setStepping(support.loadDoubleFromProperties("EndTimeStepping", pEndTime.getStepping(), auto));

        this.pMaxTime.setStartValue(support.loadDoubleFromProperties("MaxTimeStart", pMaxTime.getStartValue(), auto));
        this.pMaxTime.setEndValue(support.loadDoubleFromProperties("MaxTimeEnd", pMaxTime.getEndValue(), auto));
        this.pMaxTime.setStepping(support.loadDoubleFromProperties("MaxTimeStepping", pMaxTime.getStepping(), auto));

        this.pSeed.setStartValue(support.loadDoubleFromProperties("SeedStart", pSeed.getStartValue(), auto));
        this.pSeed.setEndValue(support.loadDoubleFromProperties("SeedEnd", pSeed.getEndValue(), auto));
        this.pSeed.setStepping(support.loadDoubleFromProperties("SeedStepping", pSeed.getStepping(), auto));

        this.pMaxError.setStartValue(support.loadDoubleFromProperties("MaxErrorStart", pMaxError.getStartValue(), auto));
        this.pMaxError.setEndValue(support.loadDoubleFromProperties("MaxErrorEnd", pMaxError.getEndValue(), auto));
        this.pMaxError.setStepping(support.loadDoubleFromProperties("MaxErrorStepping", pMaxError.getStepping(), auto));

        this.pathToLastSimulationCache = auto.getProperty("pathToLastSimulationCache", "");

        try {
            support.setChosenBenchmarkFunction(typeOfBenchmarkFunction.valueOf(auto.getProperty("BenchmarkType", support.DEFAULT_TYPE_OF_BENCHMARKFUNCTION.toString())));
        } catch (Exception e) {
            support.log("Error loading Benchmark-Type. Maybe recently used benchmark is not longer available. Using Default.");
            support.setChosenBenchmarkFunction(support.DEFAULT_TYPE_OF_BENCHMARKFUNCTION);
        }
        this.jComboBoxBenchmarkFunction.setSelectedItem(support.getChosenBenchmarkFunction());

        support.setIsRunningAsSlave(Boolean.parseBoolean(auto.getProperty("isRunningAsSlave", "false")));

        support.setRemoteAddress(auto.getProperty("RemoteAddress", ""));

        this.checkIfTimeNetPathIsCorrect();
        this.checkIfRPathIsCorrect();
        this.checkIfURLIsCorrect();
        this.deactivateExportButtons();

        jTableParameterList.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                try {
                    String rowName = (String) table.getValueAt(row, 0);
                    //TODO: get List of external Parameters from Support-Class!!!
                    if (rowName.equals("Seed") || rowName.equals("MaxTime") || rowName.equals("EndTime") || rowName.equals("ConfidenceIntervall") || rowName.equals("MaxRelError")) {
                        //if (row == colorRow && column == colorClm) {
                        setBackground(Color.LIGHT_GRAY);
                        setForeground(Color.BLACK);
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(Color.BLUE);
                    }
                } catch (Exception e) {
                }
                return this;
            }
        });

        support.setStatusLabel(jLabelExportStatus);
        support.setMeasureFormPane(jTabbedPaneOptiTargets);
        support.setPathToTimeNet(pathToTimeNet);
        support.setPathToR(pathToR);

        this.checkIfCachedSimulationIsPossible();

        this.updateComboBoxSimulationType();

        if (support.isIsRunningAsSlave()) {
            this.jCheckBoxSlaveSimulator.setSelected(true);
            new Thread(this.mySlave).start();
        } else {
            this.jCheckBoxSlaveSimulator.setSelected(false);
        }

        support.log(auto.getProperty("SimulationType"));

        this.jComboBoxSimulationType.setSelectedItem(typeOfSimulator.valueOf(auto.getProperty("SimulationType", support.DEFAULT_TYPE_OF_SIMULATOR.toString())));

        this.jComboBoxOptimizationType.setSelectedItem(typeOfOptimization.valueOf(auto.getProperty("OptimizationType", support.DEFAULT_TYPE_OF_OPTIMIZER.toString())));

        savePropertiesEnabled = true;//Enable property saving after init of all components

        //Add all Components to ListOfUIComponents
        listOfUIComponents.add(this.jTextFieldSCPNFile);
        listOfUIComponents.add(this.jButtonReload);
        listOfUIComponents.add(this.jButtonExport);
        listOfUIComponents.add(this.jButtonCancel);
        listOfUIComponents.add(this.jButtonGenerateListOfExperiments);
        listOfUIComponents.add(this.jButtonStartBatchSimulation);
        listOfUIComponents.add(this.jButtonStartOptimization);
        listOfUIComponents.add(this.jButtonLoadCacheFile);
        listOfUIComponents.add(this.jButtonOptiOptions);
        listOfUIComponents.add(this.jButtonPathToTimeNet);
        listOfUIComponents.add(this.jButtonPathToR);
        listOfUIComponents.add(this.jButtonEnterURLToSimServer);
        listOfUIComponents.add(this.jButtonPlotR);
        listOfUIComponents.add(this.jTableParameterList);
        listOfUIComponents.add(this.jTabbedPaneOptiTargets);
        listOfUIComponents.add(this.jCheckBoxSlaveSimulator);
        listOfUIComponents.add(this.jComboBoxBenchmarkFunction);
        listOfUIComponents.add(this.jComboBoxSimulationType);
        listOfUIComponents.add(this.jComboBoxOptimizationType);
        listOfUIComponents.add(this.jButtonOpenSCPN);
        listOfUIComponents.add(this.jSpinnerNumberOfOptimizationRuns);

        //Reload the last File
        this.readSCPNFile(jTextFieldSCPNFile.getText());

        this.switchUIState(uiState.defaultState);
        if (support.isIsRunningAsSlave()) {
            this.switchUIState(uiState.clientState);
        }
    }

    /**
     * Updates the combobox for selection of Simulation type if cache is
     * available --> Selection is possible if server is available --> Selection
     * of Distr. is available
     */
    public void updateComboBoxSimulationType() {
        DefaultListSelectionModel model = new DefaultListSelectionModel();
        model.addSelectionInterval(0, 0);
        model.addSelectionInterval(2, 2);
        model.addSelectionInterval(5, 5);
        if (support.isCachedSimulationAvailable()) {
            model.addSelectionInterval(1, 1);
        }
        if (support.isDistributedSimulationAvailable()) {
            model.addSelectionInterval(3, 3);
            model.addSelectionInterval(4, 4);
        }

        this.jComboBoxSimulationType.setRenderer(new EnabledJComboBoxRenderer(model));

        this.jComboBoxSimulationType.setModel(mySimulationTypeModel);
    }

    /**
     * Check, if cached simulation is possible if cached simulation is possible,
     * then set some switches etc...
     *
     * @return true if CachedSimulation is possible, else false
     */
    private boolean checkIfCachedSimulationIsPossible() {

        if (mySimulationCache != null) {
            if (mySimulationCache.checkIfAllParameterMatchTable((parameterTableModel) this.jTableParameterList.getModel())) {
                support.log("Cached Simulation available, all Parameter match.");
                support.setMySimulationCache(mySimulationCache);
                support.setCachedSimulationEnabled(true);
            } else {
                support.log("Cached Simulation not available, but all Parameter match. Maybe Stepping or Range is wrong.");
                support.setCachedSimulationEnabled(false);
            }
        } else {
            support.log("Cached Simulation not available, no simulation cache given.");
            support.setCachedSimulationEnabled(false);
        }
        this.updateComboBoxSimulationType();
        return support.isCachedSimulationAvailable();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        jButtonCancel = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jButtonStartBatchSimulation = new javax.swing.JButton();
        jButtonGenerateListOfExperiments = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jTabbedPaneOptiTargets = new javax.swing.JTabbedPane();
        measurementForm1 = new timenetexperimentgenerator.MeasurementForm();
        jButtonStartOptimization = new javax.swing.JButton();
        jButtonPathToTimeNet = new javax.swing.JButton();
        jLabelExportStatus = new javax.swing.JLabel();
        jButtonLoadCacheFile = new javax.swing.JButton();
        jComboBoxSimulationType = new javax.swing.JComboBox();
        jComboBoxOptimizationType = new javax.swing.JComboBox();
        jButtonEnterURLToSimServer = new javax.swing.JButton();
        jCheckBoxSlaveSimulator = new javax.swing.JCheckBox();
        jButtonOptiOptions = new javax.swing.JButton();
        jButtonPathToR = new javax.swing.JButton();
        jButtonPlotR = new javax.swing.JButton();
        jComboBoxBenchmarkFunction = new javax.swing.JComboBox();
        jProgressBarMemoryUsage = new javax.swing.JProgressBar();
        jLabelMemoryUsage = new javax.swing.JLabel();
        jLabelSpinning = new javax.swing.JLabel();
        jSpinnerNumberOfOptimizationRuns = new javax.swing.JSpinner();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jCheckBoxMenuItemLogToFile = new javax.swing.JCheckBoxMenuItem();
        jMenuItemClearLogFile = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jCheckBoxMenuItemLogToWindow = new javax.swing.JCheckBoxMenuItem();
        jMenuItemClearLogWindow = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonStartBatchSimulation.setText("Start batch sim");
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

        jTabbedPaneOptiTargets.addTab("Target", measurementForm1);

        jButtonStartOptimization.setText("Start Optimization");
        jButtonStartOptimization.setEnabled(false);
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

        jComboBoxSimulationType.setModel(new DefaultComboBoxModel(typeOfSimulator.values()));
        jComboBoxSimulationType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxSimulationTypeItemStateChanged(evt);
            }
        });

        jComboBoxOptimizationType.setModel(new DefaultComboBoxModel(typeOfOptimization.values()));
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
        jCheckBoxSlaveSimulator.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBoxSlaveSimulatorMouseClicked(evt);
            }
        });
        jCheckBoxSlaveSimulator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSlaveSimulatorActionPerformed(evt);
            }
        });

        jButtonOptiOptions.setText("Options");
        jButtonOptiOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOptiOptionsActionPerformed(evt);
            }
        });

        jButtonPathToR.setText("Enter Path to R");
        jButtonPathToR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPathToRActionPerformed(evt);
            }
        });

        jButtonPlotR.setText("R Plot");
        jButtonPlotR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlotRActionPerformed(evt);
            }
        });

        jComboBoxBenchmarkFunction.setModel(new DefaultComboBoxModel(typeOfBenchmarkFunction.values()));
        jComboBoxBenchmarkFunction.setEnabled(false);
        jComboBoxBenchmarkFunction.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxBenchmarkFunctionItemStateChanged(evt);
            }
        });

        jProgressBarMemoryUsage.setToolTipText("Memory-Usage. Click to print Values to log.");
        jProgressBarMemoryUsage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jProgressBarMemoryUsageMouseClicked(evt);
            }
        });

        jLabelMemoryUsage.setText("Memory Usage");

        jLabelSpinning.setText("..");

        jSpinnerNumberOfOptimizationRuns.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jMenu1.setText("File");
        jMenu1.add(jSeparator4);
        jMenu1.add(jSeparator5);

        jMenuItem5.setText("About");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);
        jMenu1.add(jSeparator6);

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

        jCheckBoxMenuItemLogToFile.setText("Log to file");
        jCheckBoxMenuItemLogToFile.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxMenuItemLogToFileItemStateChanged(evt);
            }
        });
        jMenu3.add(jCheckBoxMenuItemLogToFile);

        jMenuItemClearLogFile.setText("Clear Log File");
        jMenuItemClearLogFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearLogFileActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemClearLogFile);
        jMenu3.add(jSeparator7);

        jCheckBoxMenuItemLogToWindow.setSelected(true);
        jCheckBoxMenuItemLogToWindow.setText("Log to window");
        jCheckBoxMenuItemLogToWindow.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxMenuItemLogToWindowItemStateChanged(evt);
            }
        });
        jMenu3.add(jCheckBoxMenuItemLogToWindow);

        jMenuItemClearLogWindow.setText("Clear Log window");
        jMenuItemClearLogWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearLogWindowActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemClearLogWindow);
        jMenu3.add(jSeparator8);

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
                        .add(10, 10, 10)
                        .add(jSeparator1))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(11, 11, 11)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jTextFieldSCPNFile)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 373, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jButtonPathToTimeNet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jCheckBoxSlaveSimulator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 198, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(0, 0, Short.MAX_VALUE))))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jButtonPathToR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonPlotR)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTabbedPaneOptiTargets, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(jButtonOpenSCPN, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(5, 5, 5)
                                .add(jButtonReload, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(jSeparator2)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(jButtonExport, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jButtonCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 137, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jButtonGenerateListOfExperiments, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jSeparator3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jButtonLoadCacheFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jButtonStartBatchSimulation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 265, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabelMemoryUsage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jProgressBarMemoryUsage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jLabelSpinning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(23, 23, 23))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(layout.createSequentialGroup()
                                        .add(jButtonStartOptimization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 219, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(jSpinnerNumberOfOptimizationRuns, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(layout.createSequentialGroup()
                                                .add(jComboBoxSimulationType, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(5, 5, 5))
                                            .add(layout.createSequentialGroup()
                                                .add(jComboBoxBenchmarkFunction, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(jButtonOptiOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(jComboBoxOptimizationType, 0, 121, Short.MAX_VALUE))))
                                .add(5, 5, 5)))))
                .add(20, 20, 20))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jButtonEnterURLToSimServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
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
                            .add(jButtonCancel))
                        .add(5, 5, 5)
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jButtonGenerateListOfExperiments)
                        .add(5, 5, 5)
                        .add(jButtonStartBatchSimulation)
                        .add(5, 5, 5)
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jTabbedPaneOptiTargets, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jButtonLoadCacheFile)
                        .add(5, 5, 5)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jComboBoxSimulationType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jComboBoxOptimizationType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButtonOptiOptions)
                            .add(jComboBoxBenchmarkFunction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jButtonPathToTimeNet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jButtonStartOptimization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jSpinnerNumberOfOptimizationRuns, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jButtonPathToR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jButtonPlotR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(2, 2, 2)
                .add(jButtonEnterURLToSimServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxSlaveSimulator)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabelMemoryUsage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jProgressBarMemoryUsage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jLabelSpinning))
                .add(4, 4, 4))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOpenSCPNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenSCPNActionPerformed
        support.setCancelEverything(false);

        javax.swing.filechooser.FileFilter myFilter = new javax.swing.filechooser.FileNameExtensionFilter("xml file", "xml");
        JFileChooser fileChooser = new JFileChooser(this.jTextFieldSCPNFile.getText());
        fileChooser.setCurrentDirectory(new java.io.File(this.jTextFieldSCPNFile.getText() + "/.."));
        fileChooser.setFileFilter(myFilter);
        fileChooser.setDialogTitle("Select SCPN-Net");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            support.log("getCurrentDirectory(): "
                    + fileChooser.getCurrentDirectory());
            support.log("getSelectedFile() : "
                    + fileChooser.getSelectedFile());
            this.jTextFieldSCPNFile.setText(fileChooser.getSelectedFile().toString());
            this.readSCPNFile(fileChooser.getSelectedFile().toString());
            this.saveProperties();
        } else {
            support.log("No Selection ");
        }
    }//GEN-LAST:event_jButtonOpenSCPNActionPerformed

    private void jButtonReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReloadActionPerformed
        this.readSCPNFile(jTextFieldSCPNFile.getText());
    }//GEN-LAST:event_jButtonReloadActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
        support.setCancelEverything(false);

        //Ask for Export-Path
        //support.setTmpPath(support.getPathToDirByDialog("Dir for export of xml-Files.\n "+"Go INTO the dir to choose it!", null));
        support.setPathToTimeNet(pathToTimeNet);
        support.setMainFrame(this);
        support.setOriginalFilename(fileName);
        support.setStatusLabel(jLabelExportStatus);
        support.setMeasureFormPane(jTabbedPaneOptiTargets);

        if (ListOfParameterSetsToBeWritten != null) {
            support.log("Length of ParameterSet-List: " + ListOfParameterSetsToBeWritten.size());
            exporter tmpExporter = new exporter(ListOfParameterSetsToBeWritten);
        } else {
            support.log("Export-Operation cancled.");
        }
        support.setCancelEverything(false);
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        support.setCancelEverything(true);
        support.log("Try to cancel everything.");
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Start of batch simulation
     */
    private void jButtonStartBatchSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartBatchSimulationActionPerformed
        support.setCancelEverything(false);
        support.resetGlobalSimulationCounter();
        this.pushUIState();
        this.switchUIState(uiState.processRunning);

        //Ask for Tmp-Path
        String tmpPath = support.getPathToDirByDialog("Dir for export TMP-Files and log.\n ", support.getTmpPath());

        if (tmpPath != null) {
            support.setTmpPath(tmpPath);
            support.setPathToTimeNet(pathToTimeNet);
            support.setMainFrame(this);
            support.setOriginalFilename(fileName);
            support.setStatusLabel(jLabelExportStatus);
            support.setMeasureFormPane(jTabbedPaneOptiTargets);

            //LocalBatchSimulatorEngine mySimulator=new LocalBatchSimulatorEngine(ListOfParameterSetsToBeWritten);
            //SimulatorLocal mySimulator=new SimulatorLocal();
            //Set base parameterset and orignal base parameterset in support
            support.setOriginalParameterBase(((parameterTableModel) jTableParameterList.getModel()).getListOfParameter());
            support.setParameterBase(((parameterTableModel) jTableParameterList.getModel()).getListOfParameter());

            //If ListOfParameterSetsToBeWritten is null -->eject
            if (ListOfParameterSetsToBeWritten == null) {
                support.setStatusText("No Parametersets to simulate.");
                support.log("No Parametersets to simulate.");
                this.popUIState();
                return;
            }
            //If Parameterbase is null -->eject (This is needed for benchmark-simulations)
            if (support.getParameterBase() == null) {
                support.setStatusText("No Paramaterbase set.");
                support.log("No Paramaterbase set. No Simulation possible.");
                this.popUIState();
                return;
            }

            Simulator mySimulator = SimOptiFactory.getSimulator();
            mySimulator.initSimulator(ListOfParameterSetsToBeWritten, 0, true);
            support.waitForSimulatorAsynchronous(mySimulator, this);
        } else {
            this.popUIState();
        }
    }//GEN-LAST:event_jButtonStartBatchSimulationActionPerformed

    private void jButtonGenerateListOfExperimentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateListOfExperimentsActionPerformed
        this.switchUIState(uiState.processRunning);
        int i = JOptionPane.showConfirmDialog(this, "Really generate Designspace? This could take some time.", "Generate Design space?", JOptionPane.YES_NO_OPTION);
        if (i == 0) {
            support.setCancelEverything(false);
            this.restartGenerator();
        }
    }//GEN-LAST:event_jButtonGenerateListOfExperimentsActionPerformed

    private void jButtonStartOptimizationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartOptimizationActionPerformed
        support.setCancelEverything(false);
        support.resetGlobalSimulationCounter();

        //Set base parameterset and orignal base parameterset in support
        support.setOriginalParameterBase(((parameterTableModel) jTableParameterList.getModel()).getListOfParameter());
        support.setParameterBase(((parameterTableModel) jTableParameterList.getModel()).getListOfParameter());

        //Send chosen Optimizertype to support-class
        support.setChosenOptimizerType((typeOfOptimization) this.jComboBoxOptimizationType.getSelectedItem());
        if (this.sizeOfDesignSpace <= support.DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION) {
            support.log("Design space to small, no Optimization posible.");
            support.setStatusText("Designspace to small for Opti.");
        } else {
            if (this.getListOfActiveMeasureMentsToOptimize().size() >= 1) {
                this.switchUIState(uiState.processRunning);
                //Ask for Tmp-Path
                String tPath = (support.getPathToDirByDialog("Dir for export TMP-Files and log.\n ", support.getTmpPath()));
                //if tmpPath is empty or null --> return
                if (tPath != null) {
                    support.setTmpPath(tPath);
                    this.saveProperties();
                    support.setPathToTimeNet(pathToTimeNet);
                    support.setMainFrame(this);
                    support.setOriginalFilename(fileName);
                    support.setStatusLabel(jLabelExportStatus);
                    support.setMeasureFormPane(jTabbedPaneOptiTargets);
                    //support.setTypeOfStartValue((typeOfStartValueEnum)support.getOptimizerPreferences().jComboBoxTypeOfStartValue.getSelectedItem());

                    //If Parameterbase is null -->eject
                    if (support.getParameterBase() == null) {
                        support.setStatusText("No Paramaterbase set.");
                        support.log("No Paramaterbase set. No Simulation possible.");
                        this.popUIState();
                        return;
                    }
                    //Remove all old Optimizationstatistics
                    StatisticAggregator.removeOldOptimizationsFromList();

                    //Save original Parameterset, for stepping and designspace borders
                    support.setOriginalParameterBase(support.getCopyOfParameterSet(support.getParameterBase()));
                    //start Optimization via extra method, set number of multiple optimizations before
                    support.setNumberOfOptiRunsToGo((Integer) this.jSpinnerNumberOfOptimizationRuns.getValue());
                    startOptimizationAgain();
                    /*
                     Optimizer myOptimizer=SimOptiFactory.getOptimizer();
                     logFileNameOfOptimizer=support.getTmpPath()+File.separator+this.getClass().getSimpleName()+"_"+Calendar.getInstance().getTimeInMillis()+support.getOptimizerPreferences().getPref_LogFileAddon()+".csv";
                     myOptimizer.setLogFileName(logFileNameOfOptimizer);
                     myOptimizer.initOptimizer();
                     //Wait for end of Optimizer
                     support.waitForOptimizerAsynchronous(myOptimizer, this);
                     */

                    /*    while(myOptimizer.getOptimum()==null){
                     try {
                     Thread.sleep(500);
                     } catch (InterruptedException ex) {
                     support.log("Problem while waiting for Optimizer.");
                     }
                     }
                     */
                    //support.log("Optimum found, activating the UIComponents");
                } else {
                    support.log("No Tmp-Path given, Optimization not possible.");
                    this.popUIState();
                }

            } else {
                support.log("No Measurements to optimize for are chosen.");
                support.setStatusText("No Measurements chosen. No Opti possible.");
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
            if (fileChooser.getSelectedFile().isDirectory()) {
                outputDir = fileChooser.getSelectedFile().toString();
            } else {
                outputDir = fileChooser.getCurrentDirectory().toString();
            }
            support.log("choosen outputdir: " + outputDir);
            this.setPathToTimeNet(outputDir);
            this.checkIfTimeNetPathIsCorrect();
        } else {
            support.log("No Path to TimeNet chosen.");
        }


    }//GEN-LAST:event_jButtonPathToTimeNetActionPerformed

    private void jButtonLoadCacheFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadCacheFileActionPerformed
        //JFileChooser fileChooser = new JFileChooser(this.getPathToTimeNet());
        javax.swing.filechooser.FileFilter myFilter = new javax.swing.filechooser.FileNameExtensionFilter("csv file", "csv");
        JFileChooser fileChooser = new JFileChooser(this.pathToLastSimulationCache);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setDialogTitle(" Choose File with cached simulation files ");
        fileChooser.setFileFilter(myFilter);
        String inputFile;

        if (fileChooser.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().isDirectory()) {
                support.log("No input file chosen!");
                return;
            } else {
                inputFile = fileChooser.getSelectedFile().toString();
            }
            support.log("choosen input file with cached simulation results: " + inputFile);
        } else {
            support.log("No input file chosen!");
            return;
        }
        //this.mySimulationCache=SimOptiFactory.getSimulationCache();
        // Should we empty the cache each time, or only at user-wish?
        support.emptyCache();
        this.mySimulationCache = support.getMySimulationCache();
        if (!mySimulationCache.parseSimulationCacheFile(inputFile, ((MeasurementForm) this.jTabbedPaneOptiTargets.getComponent(0)).getMeasurements(), (parameterTableModel) this.jTableParameterList.getModel(), this)) {
            support.log("Wrong Simulation cache file for this SCPN!");
            support.setStatusText("Error loading cache-file!");
            return;
        } else {
            this.pathToLastSimulationCache = fileChooser.getSelectedFile().getPath();
            this.saveProperties();
        }
        //If cached simulation is available activate cache as Cache/local simulation
        if (this.checkIfCachedSimulationIsPossible()) {
            this.jComboBoxSimulationType.setSelectedItem(typeOfSimulator.Cache_Only);
            support.setChosenSimulatorType(typeOfSimulator.Cache_Only);
        }
    }//GEN-LAST:event_jButtonLoadCacheFileActionPerformed

    private void jComboBoxOptimizationTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOptimizationTypeActionPerformed

    }//GEN-LAST:event_jComboBoxOptimizationTypeActionPerformed

    private void jComboBoxSimulationTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxSimulationTypeItemStateChanged
        support.setChosenSimulatorType((typedef.typeOfSimulator) this.jComboBoxSimulationType.getSelectedItem());
        this.saveProperties();
    }//GEN-LAST:event_jComboBoxSimulationTypeItemStateChanged

    private void jComboBoxOptimizationTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxOptimizationTypeItemStateChanged
        support.setChosenOptimizerType((typedef.typeOfOptimization) this.jComboBoxOptimizationType.getSelectedItem());
        this.saveProperties();
    }//GEN-LAST:event_jComboBoxOptimizationTypeItemStateChanged

    private void jButtonEnterURLToSimServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEnterURLToSimServerActionPerformed

        String s = (String) JOptionPane.showInputDialog(
                this,
                "Enter URL of Simulation Server:\n",
                "URL of Simulation Server",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                support.getReMoteAddress());

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            try {
                support.log("URL of Simulation-Server as given from user is " + s + "!");
                support.setRemoteAddress(s);
                this.checkIfURLIsCorrect();
            } catch (IOException ex) {
                support.log("Problem setting the url to distributed simulation server.");
            }
        } else {
            support.log("URL of Simulation-Server was not entered!");
        }
    }//GEN-LAST:event_jButtonEnterURLToSimServerActionPerformed

    /**
     * Checks if URL to simulation server is correct. If it` correct, button
     * will be green, else red
     */
    private void checkIfURLIsCorrect() {
        String tmpURL = support.getReMoteAddress();
        boolean checksuccessful = false;
        support.log("Will try to check URL.");
        try {
            checksuccessful = support.checkRemoteAddress(tmpURL);
        } catch (IOException ex) {
            support.log("Problem checking the URL to disctributed simulation.");
        }

        support.log("Checking URL of distributed simulation server.");
        support.setDistributedSimulationAvailable(checksuccessful);
        updateComboBoxSimulationType();
        if (checksuccessful) {
            jButtonEnterURLToSimServer.setBackground(Color.GREEN);
            jButtonEnterURLToSimServer.setOpaque(true);
            jButtonEnterURLToSimServer.setBorderPainted(false);
            jButtonEnterURLToSimServer.setText("RESET URL of Sim.-Server");
            this.saveProperties();
            jButtonEnterURLToSimServer.setEnabled(true);
        } else {
            jButtonEnterURLToSimServer.setBackground(Color.RED);
            jButtonEnterURLToSimServer.setOpaque(true);
            jButtonEnterURLToSimServer.setBorderPainted(false);
            jButtonEnterURLToSimServer.setText("Enter URL of Sim.-Server");
            jButtonEnterURLToSimServer.setEnabled(true);
        }
    }

    private void jCheckBoxSlaveSimulatorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxSlaveSimulatorItemStateChanged
        //Removed functionality

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

    private void jMenuItemClearLogWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearLogWindowActionPerformed
        support.getMyLogFrame().clearText();
    }//GEN-LAST:event_jMenuItemClearLogWindowActionPerformed

    private void jButtonOptiOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOptiOptionsActionPerformed
        support.getOptimizerPreferences().setVisible(true);


    }//GEN-LAST:event_jButtonOptiOptionsActionPerformed

    private void jButtonPathToRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPathToRActionPerformed
        JFileChooser fileChooser = new JFileChooser(this.getPathToR());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setDialogTitle(" Choose Dir of R ");
        String outputDir;

        if (fileChooser.showDialog(this, "Choose this") == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().isDirectory()) {
                outputDir = fileChooser.getSelectedFile().toString();
            } else {
                outputDir = fileChooser.getCurrentDirectory().toString();
            }
            support.log("chosen outputdir: " + outputDir);
            this.setPathToR(outputDir);
            this.checkIfRPathIsCorrect();
        } else {
            support.log("No Path to R chosen.");
        }
    }//GEN-LAST:event_jButtonPathToRActionPerformed
    /**
     * Open R-Plot-Frame (R-Plot-Plugin)
     *
     * @param evt ActionEvent
     */
    private void jButtonPlotRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlotRActionPerformed
        rplugin.openPlotGui();
    }//GEN-LAST:event_jButtonPlotRActionPerformed

    /**
     * Show about-dialog of application
     *
     * @param evt ActionEvent
     */
    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    /**
     * If user turns on/off logging to window, this will be set in support-class
     * It will not be saved in properties because standard is to log to a window
     * To increase application speed or decrease system resource usage it can be
     * deactivated
     *
     * @param evt ItemEvent
     */
    private void jCheckBoxMenuItemLogToWindowItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemLogToWindowItemStateChanged
        support.setLogToWindow(this.jCheckBoxMenuItemLogToWindow.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItemLogToWindowItemStateChanged

    /**
     * If user turns on/off logging to file, this will be set in support-class
     * It will not be saved in properties because standard is not to log to a
     * file
     */
    private void jCheckBoxMenuItemLogToFileItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemLogToFileItemStateChanged
        support.setLogToFile(this.jCheckBoxMenuItemLogToFile.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItemLogToFileItemStateChanged

    /**
     * Clear logfile/delete log file User will be asked in a dialog if he really
     * wants to delete log file
     *
     * @param evt ActionEvent
     */
    private void jMenuItemClearLogFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearLogFileActionPerformed
        //Show dialog, if yes, delete file
        JOptionPane pane = new JOptionPane(
                "To be or not to be ?\nThat is the question.");
        Object[] options = new String[]{"Yes, Delete log file!", "No / Cancel"};
        pane.setOptions(options);
        JDialog dialog = pane.createDialog(new JFrame(), "Delete Log file?");
        dialog.setVisible(true);
        Object obj = pane.getValue();
        int result = -1;
        for (int k = 0; k < options.length; k++) {
            if (options[k].equals(obj)) {
                result = k;
            }
        }

        if (result == 0) {
            support.deleteLogFile();
        }

    }//GEN-LAST:event_jMenuItemClearLogFileActionPerformed

    /**
     * Everytime the Benchmark-function is changed by user, this will be saved
     * to properties
     *
     * @param evt ItemChangedEvent
     */
    private void jComboBoxBenchmarkFunctionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxBenchmarkFunctionItemStateChanged
        support.setChosenBenchmarkFunction((typeOfBenchmarkFunction) this.jComboBoxBenchmarkFunction.getSelectedItem());
        this.saveProperties();
    }//GEN-LAST:event_jComboBoxBenchmarkFunctionItemStateChanged

    /**
     * A click on the memorybar will cause a print of Memory statistics to log
     */
    private void jProgressBarMemoryUsageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jProgressBarMemoryUsageMouseClicked
        support.printMemoryStats();
    }//GEN-LAST:event_jProgressBarMemoryUsageMouseClicked
    /**
     * This method is called when checkbox for slave-simulation is clicked If
     * slave-mode was activated, it will be deactivated and slave thread will
     * try to end If slave mode was deactivated, program will ask for tmp-path
     * and if successful, start the client thread while program is in cleint
     * mode, all buttons will be deactivated
     *
     * @param evt Event from mouseclick
     */
    private void jCheckBoxSlaveSimulatorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBoxSlaveSimulatorMouseClicked
         //set Property for startup
        //start the Slave-Thread

        //If is selected and will be unselected then stop thread
        if (!this.jCheckBoxSlaveSimulator.isSelected()) {
            this.mySlave.setShouldEnd(true);
            if (support.isIsRunningAsSlave()) {
                this.popUIState();
            }
            support.setIsRunningAsSlave(false);
            this.saveProperties();
        } else {
            String tmpPath = support.getPathToDirByDialog("Dir for export TMP-Files and log.\n ", support.getTmpPath());

            if (tmpPath != null) {
                support.setTmpPath(tmpPath);
                if (support.checkTimeNetPath()) {
                    //TimeNet-Path ok, we can start
                    support.setIsRunningAsSlave(true);
                    if (support.isIsRunningAsSlave()) {
                        support.log("Tmp Path ok and timenetpath ok, try to start slave-thread.");
                        this.mySlave.setShouldEnd(false);
                        this.saveProperties();
                        new Thread(this.mySlave).start();
                        this.jCheckBoxSlaveSimulator.setSelected(true);
                        this.switchUIState(uiState.clientState);
                    }
                }
            } else {
                //No tmp path selected -->eject
                support.log("No Tmp Path selected for slave mode.");
                this.jCheckBoxSlaveSimulator.setSelected(false);
                support.setIsRunningAsSlave(false);
                this.saveProperties();
            }
        }
    }//GEN-LAST:event_jCheckBoxSlaveSimulatorMouseClicked

    private void jCheckBoxSlaveSimulatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSlaveSimulatorActionPerformed

    }//GEN-LAST:event_jCheckBoxSlaveSimulatorActionPerformed

    /**
     * Calculates the design space, number of all permutations of parameters
     * with respect to the stepping sizes
     */
    public void calculateDesignSpace() {
        myGenerator = new generator(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this, jTableParameterList);
        this.sizeOfDesignSpace = myGenerator.getSizeOfDesignspace();
        support.setStatusText("Designspace-Size:" + sizeOfDesignSpace);

        if (sizeOfDesignSpace > support.DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION) {
            this.jButtonStartOptimization.setEnabled(true);
        } else {
            this.jButtonStartOptimization.setEnabled(false);
            support.log("Design space smaller then " + support.DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION + ". Optimization not possible!");
            support.setStatusText(jLabelExportStatus.getText() + ". DS to small for Optimization.");
        }
    }

    /**
     * Builds list of parametersets based on parameters and steppings from table
     * is used recursive
     *
     * @param ListOfParameterAsFromTable List of parameters from table
     * @param ListOfParameterSetsToBeWritten List of parameterset to be
     * simulated, this is the designspace
     * @param lastParameterSet the last parameterset while generating recursive
     * @param infoLabel Label to display some information while generating
     */
    public void buildListOfParameterSetsToExport(ArrayList ListOfParameterSetsToBeWritten, ArrayList ListOfParameterAsFromTable, ArrayList<parameter> lastParameterSet, JLabel infoLabel) {
        boolean isAlreadyInExportList = false;

        if (support.isCancelEverything()) {
            this.activateReloadButtons();
            return;
        }
        if (ListOfParameterAsFromTable.size() > 0) {
            parameter loopParameter = (parameter) ListOfParameterAsFromTable.get(ListOfParameterAsFromTable.size() - 1);

            ListOfParameterAsFromTable.remove(loopParameter);

            String loopName = loopParameter.getName();
            boolean canIterate;

            double start, end, step;
            try {
                start = support.getDouble(loopParameter.getStartValue());
                end = support.getDouble(loopParameter.getEndValue());
                step = support.getDouble(loopParameter.getStepping());
                canIterate = true;
            } catch (NumberFormatException e) {
                support.log("Could not convert into double, maybe String is used. Will not iterate through parameter " + loopParameter.getName());
                return;
            }

            if (canIterate) {
                double usedValue;
                int endCounter = 1;
                if ((end - start) > 0) {
                    endCounter = (int) Math.ceil((end - start) / step) + 1;
                }

                for (int i = 0; i < endCounter; i++) {
                    usedValue = start + (double) i * step;
                    usedValue = support.round(usedValue);
                    ArrayList<parameter> nextParameterSet = new ArrayList<parameter>();
                    //Get copy of parameterset
                    for (parameter lastParameterSet1 : lastParameterSet) {
                        try {
                            nextParameterSet.add((parameter) lastParameterSet1.clone());
                        } catch (CloneNotSupportedException e) {
                            support.log("Clone is not Supported:" + e.toString());
                        }
                    }

                    for (parameter nextParameterSet1 : nextParameterSet) {
                        if (nextParameterSet1.getName().equals(loopName)) {
                            //set modified parameterset
                            nextParameterSet1.setValue(usedValue);
                        }
                    }
                    if (ListOfParameterAsFromTable.isEmpty()) {
                        addToListOfParameterSetsToBeWritten(nextParameterSet);
                    }
                    //call this method again with reduced parameterset
                    buildListOfParameterSetsToExport(ListOfParameterSetsToBeWritten, ListOfParameterAsFromTable, nextParameterSet, infoLabel);
                }
            } else {
                //check if entry ia already in list
                //no more used...
                isAlreadyInExportList = false;
            }
            ListOfParameterAsFromTable.add(loopParameter);

        } else {
            //Exit the loop, popup
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonEnterURLToSimServer;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonGenerateListOfExperiments;
    private javax.swing.JButton jButtonLoadCacheFile;
    private javax.swing.JButton jButtonOpenSCPN;
    private javax.swing.JButton jButtonOptiOptions;
    private javax.swing.JButton jButtonPathToR;
    private javax.swing.JButton jButtonPathToTimeNet;
    private javax.swing.JButton jButtonPlotR;
    private javax.swing.JButton jButtonReload;
    private javax.swing.JButton jButtonStartBatchSimulation;
    private javax.swing.JButton jButtonStartOptimization;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemLogToFile;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemLogToWindow;
    private javax.swing.JCheckBox jCheckBoxSlaveSimulator;
    private javax.swing.JComboBox jComboBoxBenchmarkFunction;
    private javax.swing.JComboBox jComboBoxOptimizationType;
    private javax.swing.JComboBox jComboBoxSimulationType;
    private javax.swing.JLabel jLabelExportStatus;
    private javax.swing.JLabel jLabelMemoryUsage;
    protected javax.swing.JLabel jLabelSpinning;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItemClearLogFile;
    private javax.swing.JMenuItem jMenuItemClearLogWindow;
    private javax.swing.JProgressBar jProgressBarMemoryUsage;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JSpinner jSpinnerNumberOfOptimizationRuns;
    private javax.swing.JTabbedPane jTabbedPaneOptiTargets;
    private javax.swing.JTable jTableParameterList;
    private javax.swing.JTextField jTextFieldSCPNFile;
    private timenetexperimentgenerator.MeasurementForm measurementForm1;
    // End of variables declaration//GEN-END:variables

    /**
     * Reads the xml-file of SCPN and sets the User Interface
     *
     * @param filename String name of the SCPN-File to read
     */
    private void readSCPNFile(String filename) {

        if (filename == null) {
            return;
        }
        if (filename.equals("")) {
            return;
        }

        deactivateExportButtons();
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filename);
            NodeList parameterList = doc.getElementsByTagName("parameter");

            for (int i = 0; i < parameterList.getLength(); i++) {
                support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue());
            }
            jTableParameterList.setModel(new parameterTableModel(parameterList, this));
            jTableParameterList.getModel().addTableModelListener(this);

            //Measures auslesen
            NodeList MeasurenameList = doc.getElementsByTagName("measure");
            if (MeasurenameList.getLength() >= 1) {
                ArrayList<MeasureType> Measures = new ArrayList();
                support.log("****** Measure-Names ******");
                for (int i = 0; i < MeasurenameList.getLength(); i++) {
                    support.log(MeasurenameList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    MeasureType tmpMeasure = new MeasureType();
                    tmpMeasure.setMeasureName(MeasurenameList.item(i).getAttributes().getNamedItem("name").getNodeValue());
                    Measures.add(tmpMeasure);
                }

                for (int i = 0; i < this.jTabbedPaneOptiTargets.getComponentCount(); i++) {
                    ((MeasurementForm) this.jTabbedPaneOptiTargets.getComponent(i)).setMeasurements(Measures);
                }

                //Set List of all Measurements to support
                support.setMeasures(Measures);

            }

            this.fileName = filename;//nach Erfolg, globalen filename setzen
            support.setOriginalFilename(filename);
            activateGenerateButtons();
            activateReloadButtons();
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (DOMException e) {
        }
        tableChanged(null);
    }

    /**
     * removed duplicate parametersets, not used anymore?
     *
     * @param ListOfParameterSetsToBeWritten List of parametersets to be checked
     * for duplicates
     * @param infoLabel label to print out some information while checking
     * @return List of parametersets without duplicates
     */
    public ArrayList<parameter[]> removeDuplicates(ArrayList<parameter[]> ListOfParameterSetsToBeWritten, JLabel infoLabel) {
        ArrayList<parameter[]> tmpList = new ArrayList();
        boolean existsInOutPutList;
        for (int i = 0; i < ListOfParameterSetsToBeWritten.size(); i++) {
            infoLabel.setText("Checking " + i + "/" + ListOfParameterSetsToBeWritten.size());
            if (support.isCancelEverything()) {
                infoLabel.setText("Operation canceled");
                this.activateReloadButtons();
                return null;
            }
            parameter[] tmpParameterSet = (parameter[]) ListOfParameterSetsToBeWritten.get(i);
            existsInOutPutList = false;
            if (tmpList.size() > 0) {
                for (parameter[] tmpList1 : tmpList) {
                    parameter[] tmpListParameter = (parameter[]) tmpList1;
                    long tmpParameterSetID = getIDOfParameterSet(tmpParameterSet);
                    long tmpListParameterID = getIDOfParameterSet(tmpListParameter);
                    /*
                     support.log("P1: "+tmpParemeterSetID);
                     support.log("P2: "+tmpListParameterID);
                     * */
                    if (tmpListParameterID == tmpParameterSetID) {
                        existsInOutPutList = true;
                        support.log("These Parameters are equal:");
                        support.log(tmpParameterSetID + " and " + tmpListParameterID);
                        printParameterSetCompare(tmpParameterSet, tmpListParameter);

                    }
                }
            }
            if (!existsInOutPutList) {
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
     *
     * @param parameterset Set of parameters to calculate the id from
     * @return ID of whole parameterset
     */
    public long getIDOfParameterSet(parameter[] parameterset) {
        long id = 0;
        String tmpString = "";
        //Arrays.sort(parameterset);
        for (parameter parameterset1 : parameterset) {
            id = id + parameterset1.getID();
            //support.log("ID of:"+ parameterset[i].getName()+" is " +parameterset[i].getID());
            tmpString = tmpString + String.valueOf(parameterset1.getID());
        }
        //return id;
        return (long) tmpString.hashCode();
    }

    /**
     * Starts and restarts the generator Thread
     */
    public void restartGenerator() {
        support.setCancelEverything(false);
        myGenerator = null;
        ListOfParameterSetIds = new ArrayList<Long>();
        ListOfParameterSetsToBeWritten = new ArrayList<ArrayList<parameter>>();
        myGenerator = new generator(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this, jTableParameterList);
        myGenerator.start();
        support.waitForGeneratorAsynchronous(myGenerator, this);
    }

    /**
     * Recalculates the size of designspace after Table has changed Sends List
     * of possible internal parameter to optimizer-preferences
     *
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
        if (this.jComboBoxSimulationType.getSelectedItem().equals(typeOfSimulator.Cache_Only)) {
            if (!support.isCachedSimulationAvailable()) {
                this.jComboBoxSimulationType.setSelectedItem(typeOfSimulator.Cached_Local);
                support.setChosenSimulatorType(typeOfSimulator.Cached_Local);
            }
        }
        ArrayList<parameter> myParameterList = ((parameterTableModel) this.jTableParameterList.getModel()).getListOfParameter();
        ArrayList<parameter> resultParameterList = new ArrayList<parameter>();
        for (parameter p : myParameterList) {
            if (!p.isExternalParameter() && !p.isIteratable()) {
                resultParameterList.add(p);
            }
        }
        support.getOptimizerPreferences().setPossibleInternalParameters(resultParameterList);
    }

    /**
     * Reads the simulation-parameters from table (not SCPN-Specific parameters)
     * and stores these parameters in local fields
     */
    private void readStaticParametersFromTable() {

        this.getpConfidenceIntervall().setStartValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("ConfidenceIntervall", "StartValue"));
        this.getpConfidenceIntervall().setEndValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("ConfidenceIntervall", "EndValue"));
        this.getpConfidenceIntervall().setStepping(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("ConfidenceIntervall", "Stepping"));

        this.getpEndTime().setStartValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("EndTime", "StartValue"));
        this.getpEndTime().setEndValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("EndTime", "EndValue"));
        this.getpEndTime().setStepping(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("EndTime", "Stepping"));

        this.getpMaxTime().setStartValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("MaxTime", "StartValue"));
        this.getpMaxTime().setEndValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("MaxTime", "EndValue"));
        this.getpMaxTime().setStepping(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("MaxTime", "Stepping"));

        this.getpSeed().setStartValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("Seed", "StartValue"));
        this.getpSeed().setEndValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("Seed", "EndValue"));
        this.getpSeed().setStepping(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("Seed", "Stepping"));

        this.getpMaxError().setStartValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("MaxRelError", "StartValue"));
        this.getpMaxError().setEndValue(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("MaxRelError", "EndValue"));
        this.getpMaxError().setStepping(((parameterTableModel) this.jTableParameterList.getModel()).getDoubleValueByName("MaxRelError", "Stepping"));

    }

    /**
     * Sets the Export-Button for Experiments/Simulations De-active
     */
    protected final void deactivateExportButtons() {
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
    public void activateExportButtons() {
        this.jButtonExport.setEnabled(true);
        //this.jButtonOpenSCPN.setEnabled(true);
        this.jButtonReload.setEnabled(true);
        //this.jButtonStartOptimization.setEnabled(true);
        this.jButtonGenerateListOfExperiments.setEnabled(true);
        this.checkIfTimeNetPathIsCorrect();
    }

    /**
     * Sets the Reload-SCPN-Button and open-SCPN-Button active
     */
    public void activateReloadButtons() {
        this.jButtonOpenSCPN.setEnabled(true);
        this.jButtonReload.setEnabled(true);
    }

    /**
     * Sets the Generate- and OptimizeButtons active
     */
    public void activateGenerateButtons() {
        //this.jButtonStartOptimization.setEnabled(true);
        this.jButtonGenerateListOfExperiments.setEnabled(true);
        //this.jButtonStartBatchSimulation.setEnabled(true);
    }

    /*
     * Checks, if given Path to TimeNet is correct
     * If correct, then install new "RemoteSystem Client.config"
     */
    private void checkIfTimeNetPathIsCorrect() {
        String path = this.getPathToTimeNet();//jTextFieldPathToTimeNet.getText();
        File tmpFile = new File(path + File.separator + "TimeNET.jar");
        support.log("TimeNet should be here: " + tmpFile.getAbsolutePath());
        if (tmpFile.exists()) {
            this.jButtonStartBatchSimulation.setEnabled(true);
            //this.jLabelCheckPathToTimeNet.setVisible(false);
            jButtonPathToTimeNet.setBackground(Color.GREEN);
            jButtonPathToTimeNet.setOpaque(true);
            jButtonPathToTimeNet.setBorderPainted(false);
            jButtonPathToTimeNet.setText("RESET Path To TimeNet");
            //jButtonStartOptimization.setEnabled(true);
            support.setPathToTimeNet(path);
            this.pathToTimeNet = path;
            this.saveProperties();
            //Try to install "RemoteSystem Client.config"
            try {
                InputStream ddlStream = this.getClass().getClassLoader().getResourceAsStream("timenetexperimentgenerator/RemoteSystem Client.config");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(path + File.separator + "RemoteSystem Client.config");
                    byte[] buf = new byte[2048];
                    int r = ddlStream.read(buf);
                    while (r != -1) {
                        fos.write(buf, 0, r);
                        r = ddlStream.read(buf);
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
            } catch (IOException e) {
                support.log("Failed to install RemoteSystem Clent.config");
            }
        } else {

            this.jButtonStartBatchSimulation.setEnabled(false);
            //this.jLabelCheckPathToTimeNet.setVisible(true);
            jButtonPathToTimeNet.setBackground(Color.RED);
            jButtonPathToTimeNet.setOpaque(true);
            jButtonPathToTimeNet.setBorderPainted(true);
            jButtonPathToTimeNet.setText("Enter Path To TimeNet");
            jButtonStartOptimization.setEnabled(false);

        }
    }

    /*
     * Checks, if given Path to R is correct
     */
    private void checkIfRPathIsCorrect() {
        String path = this.getPathToR();
        String rApplicationName;

        String OS = System.getProperty("os.name").toLowerCase();
        if ((OS.contains("win"))) {
            //We are on a windows-system
            rApplicationName = "R.exe";
        } else {
            //We are on a non-windows-system
            rApplicationName = "R";
        }
        File tmpFile = new File(path + File.separator + "bin" + File.separator + rApplicationName);

        support.log("R should be here: " + tmpFile.getAbsolutePath());
        if (tmpFile.exists()) {
            jButtonPathToR.setBackground(Color.GREEN);
            jButtonPathToR.setOpaque(true);
            jButtonPathToR.setBorderPainted(false);
            jButtonPathToR.setText("RESET Path To R");

            support.setPathToR(path);
            this.pathToR = path;
            this.saveProperties();

            jButtonPlotR.setEnabled(true);
        } else {
            jButtonPathToR.setBackground(Color.RED);
            jButtonPathToR.setOpaque(true);
            jButtonPathToR.setBorderPainted(true);
            jButtonPathToR.setText("Enter Path To R");
            jButtonPlotR.setEnabled(false);
        }
    }

    /**
     * Saves program-properties to a local file in home-dir
     */
    private void saveProperties() {
        if (!savePropertiesEnabled) {
            return;
        }
        support.log("Saving Properties.");
        try {
            //support.log("rpath = " + this.getPathToR());
            //support.log("timenetpath = " + this.getPathToTimeNet());

            auto.setProperty("timenetpath", this.getPathToTimeNet());
            auto.setProperty("file", this.jTextFieldSCPNFile.getText());

            auto.setProperty("rpath", this.getPathToR());

            auto.setProperty("ConfidenceIntervallStart", support.getString(this.getpConfidenceIntervall().getStartValue()));
            auto.setProperty("ConfidenceIntervallEnd", support.getString(this.getpConfidenceIntervall().getEndValue()));
            auto.setProperty("ConfidenceIntervallStepping", support.getString(this.getpConfidenceIntervall().getStepping()));

            auto.setProperty("EndTimeStart", support.getString(this.getpEndTime().getStartValue()));
            auto.setProperty("EndTimeEnd", support.getString(this.getpEndTime().getEndValue()));
            auto.setProperty("EndTimeStepping", support.getString(this.getpEndTime().getStepping()));

            auto.setProperty("MaxTimeStart", support.getString(this.getpMaxTime().getStartValue()));
            auto.setProperty("MaxTimeEnd", support.getString(this.getpMaxTime().getEndValue()));
            auto.setProperty("MaxTimeStepping", support.getString(this.getpMaxTime().getStepping()));
            auto.setProperty("SeedStart", support.getString(this.getpSeed().getStartValue()));
            auto.setProperty("SeedEnd", support.getString(this.getpSeed().getEndValue()));
            auto.setProperty("SeedStepping", support.getString(this.getpSeed().getStepping()));

            auto.setProperty("MaxErrorStart", support.getString(this.getpMaxError().getStartValue()));
            auto.setProperty("MaxErrorEnd", support.getString(this.getpMaxError().getEndValue()));
            auto.setProperty("MaxErrorStepping", support.getString(this.getpMaxError().getStepping()));

            auto.setProperty("pathToLastSimulationCache", this.pathToLastSimulationCache);

            auto.setProperty("OptimizationType", support.getChosenOptimizerType().toString());
            auto.setProperty("SimulationType", support.getChosenSimulatorType().toString());
            auto.setProperty("BenchmarkType", support.getChosenBenchmarkFunction().toString());

            auto.setProperty("isRunningAsSlave", Boolean.toString(support.isIsRunningAsSlave()));

            auto.setProperty("RemoteAddress", support.getReMoteAddress());

            if (support.getTmpPath() != null) {
                auto.setProperty("tmppath", support.getTmpPath());
            } else {
                support.log("No tmp-path yet given. Please do so.");
            }

            File parserprops = new File(support.NAME_OF_PREFERENCES_FILE);
            auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
        } catch (IOException e) {
            support.log("Problem Saving the properties.");
        }

    }

    /**
     * Prints 2 parametersets and it`s values to see the difference Just used
     * for debug reasons
     *
     * @param p parameter to be compared with p1
     * @param p1 parameter to be compared with p
     */
    private void printParameterSetCompare(parameter[] p, parameter[] p1) {
        support.log("Printing P-Set:");
        for (int i = 0; i < p.length; i++) {
            support.log(((parameter) p[i]).getName());
            support.log(((parameter) p[i]).getValue() + " vs " + ((parameter) p1[i]).getValue());

        }
    }

    /**
     * Adds a parameterset to the list of parametersets
     *
     * @param p parameterset to be added
     */
    public void addToListOfParameterSetsToBeWritten(ArrayList<parameter> p) {
        ListOfParameterSetsToBeWritten.add(p);
        support.setStatusText("Building Parametersets:" + ListOfParameterSetsToBeWritten.size() * 100 / this.sizeOfDesignSpace + "%");

    }

    /**
     * calculates list of parameters from table this is the base list with
     * start/end/stepping values
     *
     * @return List of Parameters from Table (Base of Parameter Iterations)
     *
     */
    public ArrayList<parameter> getParameterBase() {

        //Return the saved parameterset in support-class for Multiphase-Opti
        if (support.getParameterBase() != null) {
            return support.getParameterBase();
        }

        //int parameterCount=this.jTableParameterList.getModel().getRowCount();
        parameterTableModel tModel = (parameterTableModel) this.jTableParameterList.getModel();
        //String [][] parameterArray=tModel.getParameterArray();
        ArrayList<parameter> parameterArray = new ArrayList<parameter>();

        //ArrayListe aufbauen und Funktion mit dieser Liste aufrufen
        for (int i = 0; i < tModel.getRowCount(); i++) {
            parameter tmpParameter = new parameter();
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
     * Returns list of MeasureTypes to be optimized
     *
     * @return List of MeasureTypes, given by Tabbed-Pane, to which it should be
     * optimized
     */
    public ArrayList<MeasureType> getListOfActiveMeasureMentsToOptimize() {
        ArrayList<MeasureType> myTmpList = new ArrayList<MeasureType>();//((MeasurementForm)this.jTabbedPane1.getComponent(0)).getListOfMeasurements();

        for (int i = 0; i < this.jTabbedPaneOptiTargets.getComponentCount(); i++) {
            MeasurementForm tmpMeasurementForm = (MeasurementForm) this.jTabbedPaneOptiTargets.getComponent(i);
            if (tmpMeasurementForm.isActive()) {
                MeasureType tmpMeasure = tmpMeasurementForm.getChosenMeasurement();
                double targetValue = tmpMeasurementForm.getCustomTargetValue();
                typedef.typeOfTarget targetKind = tmpMeasurementForm.getOptimizationTarget();
                tmpMeasure.setTargetValue(targetValue, targetKind);
                myTmpList.add(tmpMeasure);
            }
        }
        return myTmpList;
    }

    /**
     * Set local variable of path to Timenet
     *
     * @param pathToTimeNet the pathToTimeNet to set
     */
    private void setPathToTimeNet(String pathToTimeNet) {
        this.pathToTimeNet = pathToTimeNet;
    }

    /**
     * Set local variable of path to R
     *
     * @param pathToR the pathToR to set
     */
    private void setPathToR(String pathToR) {
        this.pathToR = pathToR;
    }

    /**
     * returns local value of path to TimeNet
     */
    private String getPathToTimeNet() {
        return this.pathToTimeNet;
    }

    /**
     * returns local value of path to R
     */
    private String getPathToR() {
        return this.pathToR;
    }

    /**
     * Enables or disables the Combobox for chosing benchmark functions
     *
     * @param b true to enable benchmark combobox
     */
    public void setBenchmarkFunctionComboboxEnabled(boolean b) {
        this.jComboBoxBenchmarkFunction.setEnabled(b);
    }

    /**
     * Sets the progressbar for Memory-Usage to a value between 0..100
     *
     * @param s value of progressbar (0..100)
     */
    public void setMemoryProgressbar(int s) {

        if ((s <= 100) && (s >= 0)) {
            this.jProgressBarMemoryUsage.setValue(s);
        }

    }

    /**
     * Checks alle inforamtion about System state and sets the boolean operators
     * for UI Element activation UI Components are not modified by this method,
     * just updates the UIState-ArrayList
     */
    public void updateAllUIStates() {
        //jTextFieldSCPNFile    
    }

    /**
     * updates all UI Components by information from UIState-ArrayList
     */
    public void updateAllUIComponents() {
        try {
            if (this.listOfUIStates.size() == this.listOfUIComponents.size()) {
                for (int i = 0; i < listOfUIStates.size(); i++) {
                    listOfUIComponents.get(i).setEnabled(listOfUIStates.get(i));
                }
            }
        } catch (Exception e) {
            //Exception could be trown, if listOfUIStates is not initialized correctly
        }
    }

    /**
     * Switches to a specific UI-State (Default/Client Mode etc)
     *
     * @param newState new UI-State to be activated
     */
    public void switchUIState(uiState newState) {
        this.pushUIState();//Save active UI-State
        /*
         0-jTextFieldSCPNFile
         1-jButtonReload
         2-jButtonExport
         3-jButtonCancel
         4-jButtonGenerateListOfExperiments
         5-jButtonStartBatchSimulation
         6-jButtonStartOptimization
         7-jButtonLoadCacheFile
         8-jButtonOptiOptions
         9-jButtonPathToTimeNet
         10-jButtonPathToR
         11-jButtonEnterURLToSimServer
         12-this.jButtonPlotR
         13-jTableParameterList
         14-jTabbedPaneOptiTargets
         15-jCheckBoxSlaveSimulator
         16-jComboBoxBenchmarkFunction
         17-jComboBoxSimulationType
         18-jComboBoxOptimizationType
         19-jButtonOpenSCPN
         20-jSpinnerNumberOfOptimizationRuns
         */
        this.listOfUIStates = new ArrayList<Boolean>();
        //Activate all
        for (Component listOfUIComponent : listOfUIComponents) {
            listOfUIStates.add(true);
        }

        switch (newState) {

            case defaultState:
                listOfUIStates.set(2, false);
                listOfUIStates.set(3, false);
                listOfUIStates.set(5, false);
                listOfUIStates.set(6, false);
                //Deactivate Benchmark JCombobox if no benchmark-simulator is chosen        
                listOfUIStates.set(16, jComboBoxSimulationType.getSelectedItem().equals(typeOfSimulator.Benchmark));
                break;
            case clientState:
                for (int i = 0; i < listOfUIStates.size(); i++) {
                    listOfUIStates.set(i, false);
                }
                listOfUIStates.set(15, true);
                break;
            case processRunning:
                //Something is running, only cancel is possible
                for (int i = 0; i < listOfUIStates.size(); i++) {
                    listOfUIStates.set(i, false);
                }
                listOfUIStates.set(3, true);
                break;
            default:
                break;
        }
        updateAllUIComponents();
    }

    /**
     * pushes active UI-State to switch temporary to another state and possibly
     * back
     */
    public void pushUIState() {
        this.listOfUIStatesPushed = new ArrayList();
        //Create deep copy of UI-States
        for (int i = 0; i < listOfUIStates.size(); i++) {
            listOfUIStatesPushed.add(listOfUIComponents.get(i).isEnabled());
        }
    }

    /**
     * restores last saved UI-State
     */
    public void popUIState() {
        if (listOfUIStatesPushed != null) {
            for (int i = 0; i < listOfUIStates.size(); i++) {
                listOfUIStates.set(i, listOfUIStatesPushed.get(i));
            }
            listOfUIStatesPushed = null;
            updateAllUIComponents();
        }
    }

    /**
     * Deactivate every UI component, like buttons, spinners, etc. Except the
     * ones, given in oList
     *
     * @param oList Array of User-Interface components that will not be
     * deactivated
     */
    public void deactivateEveryComponentExcept(Component[] oList) {
        this.jButtonGenerateListOfExperiments.setEnabled(false);

        for (Component listOfUIComponent : this.listOfUIComponents) {
            listOfUIComponent.setEnabled(false);
        }

        for (Component oList1 : oList) {
            try {
                oList1.setEnabled(true);
            } catch (Exception e) {
                //
            }
        }

    }

    /**
     * Callback-Method of SimOptiCallback called when Simulation or optimization
     * is ended succesfully
     *
     * @param message will be shown in staus-label
     * @param feedback will determine what to do next (button activation etc.)
     */
    public void operationSucessfull(String message, typeOfProcessFeedback feedback) {
        int tmpNumberOfOptiRunsToGo = support.getNumberOfOptiRunsToGo();
        if (tmpNumberOfOptiRunsToGo <= 1) {
            this.popUIState();
            support.unsetListOfChangableParametersMultiphase();//Stop Multiphase if it was active
            support.setStatusText(message);
            support.log("Last simulation run has ended. Will show statistics.");
            support.log("Ended was: " + feedback.toString());
            StatisticAggregator.printOptiStatistics();

        } else {
            support.log("Starting next Optimization run, number:" + (tmpNumberOfOptiRunsToGo - 1));
            support.setNumberOfOptiRunsToGo(tmpNumberOfOptiRunsToGo - 1);
            this.startOptimizationAgain();
        }
        switch (feedback) {
            case GenerationSuccessful:
                jButtonStartBatchSimulation.setEnabled(true);
                break;

            default:
                break;
        }
    }

    /**
     * Callback-Method of SimOptiCallback called when Simulation or optimization
     * is canceled
     *
     * @param message will be shown in staus-label
     */
    public void operationCanceled(String message, typeOfProcessFeedback feedback) {
        this.popUIState();
        support.setStatusText(message);
        switch (feedback) {
            case GenerationCanceled:
                jButtonStartBatchSimulation.setEnabled(false);
                support.log("Generation canceled. Deactivate StartBatchButton.");
                break;
            case GenerationNotSuccessful:
                jButtonStartBatchSimulation.setEnabled(false);
                break;

            default:
                break;
        }
    }

    /**
     * Starts an OptimizationRun, useful for Multiple optimization runs
     */
    private void startOptimizationAgain() {
        Optimizer myOptimizer = SimOptiFactory.getOptimizer();
        logFileNameOfOptimizer = support.getTmpPath() + File.separator + myOptimizer.getClass().getSimpleName() + "_" + Calendar.getInstance().getTimeInMillis() + support.getOptimizerPreferences().getPref_LogFileAddon() + ".csv";
        myOptimizer.setLogFileName(logFileNameOfOptimizer);
        myOptimizer.initOptimizer();
        //Wait for end of Optimizer
        support.waitForOptimizerAsynchronous(myOptimizer, this);
    }

    /**
     * @return the pConfidenceIntervall
     */
    public parameter getpConfidenceIntervall() {
        return pConfidenceIntervall;
    }

    /**
     * @param pConfidenceIntervall the pConfidenceIntervall to set
     */
    public void setpConfidenceIntervall(parameter pConfidenceIntervall) {
        this.pConfidenceIntervall = pConfidenceIntervall;
    }

    /**
     * @return the pSeed
     */
    public parameter getpSeed() {
        return pSeed;
    }

    /**
     * @param pSeed the pSeed to set
     */
    public void setpSeed(parameter pSeed) {
        this.pSeed = pSeed;
    }

    /**
     * @return the pEndTime
     */
    public parameter getpEndTime() {
        return pEndTime;
    }

    /**
     * @param pEndTime the pEndTime to set
     */
    public void setpEndTime(parameter pEndTime) {
        this.pEndTime = pEndTime;
    }

    /**
     * @return the pMaxTime
     */
    public parameter getpMaxTime() {
        return pMaxTime;
    }

    /**
     * @param pMaxTime the pMaxTime to set
     */
    public void setpMaxTime(parameter pMaxTime) {
        this.pMaxTime = pMaxTime;
    }

    /**
     * @return the pMaxError
     */
    public parameter getpMaxError() {
        return pMaxError;
    }

    /**
     * @param pMaxError the pMaxError to set
     */
    public void setpMaxError(parameter pMaxError) {
        this.pMaxError = pMaxError;
    }
}
