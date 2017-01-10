/*
 * Main Frame for TimeNetExperimentGenerator
 * provides many additional features
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe;

import toe.simulation.SimulationCache;
import toe.simulation.Simulator;
import toe.simulation.SimulatorWebSlave;
import toe.plot.RPlugin;
import toe.optimization.Optimizer;
import toe.helper.SimOptiCallback;
import toe.helper.parameterTableModel;
import toe.helper.StatisticAggregator;
import toe.helper.EnabledJComboBoxRenderer;
import toe.helper.SimulationTypeComboBoxModel;
import toe.datamodel.parameter;
import toe.datamodel.MeasureType;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import toe.optimization.OptimizerPreferences;
import toe.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public final class MainFrame extends javax.swing.JFrame implements TableModelListener, SimOptiCallback {

    Properties auto = new Properties();
    private String fileName = "";
    ArrayList< ArrayList<parameter>> ListOfParameterSetsToBeWritten = new ArrayList<>();//Name, Value
    generator myGenerator;
    private parameter pConfidenceIntervall = new parameter();
    private parameter pSeed = new parameter();
    private parameter pEndTime = new parameter();
    private parameter pMaxTime = new parameter();
    private parameter pMaxError = new parameter();
    ArrayList<Long> ListOfParameterSetIds = new ArrayList<>();
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

    private ArrayList<Component> listOfUIComponents = new ArrayList<>();//List of all Components
    private ArrayList<Boolean> listOfUIStates = new ArrayList<>();
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

        //Create pref dir
        File prefDir = new File(support.NAME_OF_PREF_DIR);
        if (!prefDir.exists()) {
            prefDir.mkdir();
        }

        try {
            FileInputStream in = new FileInputStream(support.NAME_OF_PREFERENCES_FILE);
            auto.load(in);
            in.close();
        } catch (IOException e) {
            // IOException
        }
        //Load Loglevel settings
        this.setLogLevelActivated_ERROR(Boolean.parseBoolean(auto.getProperty("LOGLEVEL_ERROR", "true")));
        this.setLogLevelActivated_INFO(Boolean.parseBoolean(auto.getProperty("LOGLEVEL_INFO", "true")));
        this.setLogLevelActivated_RESULT(Boolean.parseBoolean(auto.getProperty("LOGLEVEL_RESULT", "true")));
        this.setLogLevelActivated_VERBOSE(Boolean.parseBoolean(auto.getProperty("LOGLEVEL_VERBOSE", "true")));

        this.setLogToWindow(Boolean.parseBoolean(auto.getProperty("LOGTOWINDOW", "true")));
        this.setLogToFile(Boolean.parseBoolean(auto.getProperty("LOGTOFILE", "true")));

        //Install default scpn file
        File defaultSCPN = new File(support.NAME_OF_DEFAULT_SCPN);
        if (!defaultSCPN.exists() || !defaultSCPN.isFile()) {
            try {
                InputStream ddlStream = this.getClass().getClassLoader().getResourceAsStream("toe/default_SCPN.xml");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(support.NAME_OF_DEFAULT_SCPN);
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
                support.log("Failed to install default SCPN", typeOfLogLevel.ERROR);
            }
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

        this.jTextFieldSCPNFile.setText(auto.getProperty("file", support.NAME_OF_DEFAULT_SCPN));
        //this.jTextFieldPathToTimeNet.setText(auto.getProperty("timenetpath"));
        this.setPathToTimeNet(auto.getProperty("timenetpath", ""));
        //support.log("Read Path to TimeNet:"+auto.getProperty("timenetpath"));
        this.setPathToR(auto.getProperty("rpath", support.getDefaultPathToR()));
        //Read tmp path from properties, needed for client-mode-start
        support.setTmpPath(auto.getProperty("tmppath"));

        support.setServerSecret(auto.getProperty("serversecret", new BigInteger(70, new SecureRandom()).toString(32)));

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

        this.jCheckBoxDeleteTmpFiles.setSelected(Boolean.valueOf(auto.getProperty("deleteTmpFile", "True")));
        support.setDeleteTmpSimulationFiles(jCheckBoxDeleteTmpFiles.isSelected());
        try {
            support.setChosenBenchmarkFunction(typeOfBenchmarkFunction.valueOf(auto.getProperty("BenchmarkType", support.DEFAULT_TYPE_OF_BENCHMARKFUNCTION.toString())));
        } catch (Exception e) {
            support.log("Error loading Benchmark-Type. Maybe recently used benchmark is not longer available. Using Default.", typeOfLogLevel.ERROR);
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
                    //get List of external Parameters from Support-Class!!!
                    parameter tmpParameter = new parameter();
                    tmpParameter.setName(rowName);
                    if (tmpParameter.isExternalParameter()) {
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

        this.updateComboBoxSimulationType();

        if (support.isIsRunningAsSlave()) {
            this.jCheckBoxSlaveSimulator.setSelected(true);
            new Thread(this.mySlave).start();
        } else {
            this.jCheckBoxSlaveSimulator.setSelected(false);
        }

        support.log("Using simulationtype: " + auto.getProperty("SimulationType"), typeOfLogLevel.INFO);

        this.jComboBoxSimulationType.setSelectedItem(typeOfSimulator.valueOf(auto.getProperty("SimulationType", support.DEFAULT_TYPE_OF_SIMULATOR.toString())));
        support.setChosenSimulatorType((typeOfSimulator) jComboBoxSimulationType.getSelectedItem());
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
        listOfUIComponents.add(this.jButtonEmptyCache);

        //Reload the last File
        try {
            this.readSCPNFile(jTextFieldSCPNFile.getText());
        } catch (Exception e) {
            support.log("Could not read SCPN-file!", typeOfLogLevel.ERROR);
            JOptionPane.showMessageDialog(null, "Please choose a correct SCPN file!");
        }

        this.switchUIState(uiState.defaultState);
        if (support.isIsRunningAsSlave()) {
            this.switchUIState(uiState.clientState);
        }

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                JOptionPane pane = new JOptionPane("Will try to shut down remote simulations...", JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = pane.createDialog(windowEvent.getWindow(), "Closing Remote Simulations.");
                dialog.setModal(false);
                dialog.setVisible(true);
                SimOptiFactory.endAllRemoteSimulations();
                dialog.setVisible(false);
                System.exit(0);
            }
        });

        //try to load from cache
        if (!this.pathToLastSimulationCache.equals("")) {
            if (this.tryToFillCacheFromFile(this.pathToLastSimulationCache)) {
                if (!support.isIsRunningAsSlave()) {
                    JOptionPane.showMessageDialog(null, "Cached simulation data loaded. \n " + this.pathToLastSimulationCache);
                }
                support.getMySimulationCache().reformatParameterTable((parameterTableModel) this.jTableParameterList.getModel());
                this.jTableParameterList.updateUI();
                this.calculateDesignSpace();
                this.checkIfCachedSimulationIsPossible();
            }

        }

    }

    /**
     * Updates the combobox for selection of Simulation type if cache is
     * available --> Selection is possible if server is available --> Selection
     * of Distr. is available
     */
    public void updateComboBoxSimulationType() {
        ArrayList enabledSimulationTypes = new ArrayList<Integer>();
        DefaultListSelectionModel model = new DefaultListSelectionModel();
        if (support.isLocalSimulationAvailable()) {
            model.addSelectionInterval(0, 0);
            enabledSimulationTypes.add(0);
            model.addSelectionInterval(2, 2);
            enabledSimulationTypes.add(2);
        }

        //Benchmark is always available
        model.addSelectionInterval(5, 6);
        enabledSimulationTypes.add(5);
        enabledSimulationTypes.add(6);

        if (support.isCachedSimulationAvailable()) {
            model.addSelectionInterval(1, 1);
            enabledSimulationTypes.add(1);
        }
        if (support.isDistributedSimulationAvailable()) {
            model.addSelectionInterval(3, 3);
            enabledSimulationTypes.add(3);
            model.addSelectionInterval(4, 4);
            enabledSimulationTypes.add(4);
        }

        if (!enabledSimulationTypes.contains(this.jComboBoxSimulationType.getSelectedIndex())) {
            this.jComboBoxSimulationType.setSelectedIndex(5);
        }

        this.jComboBoxSimulationType.setRenderer(new EnabledJComboBoxRenderer(model));
        this.jComboBoxSimulationType.setModel(mySimulationTypeModel);
    }

    /**
     * Check, if cached simulation is possible if cached simulation is possible,
     * then set some switches etc...
     *
     * Needs to be done after loading a cache-file
     *
     * @return true if CachedSimulation is possible, else false
     */
    private boolean checkIfCachedSimulationIsPossible() {

        if (mySimulationCache != null) {
            if (mySimulationCache.checkIfAllParameterMatchTable((parameterTableModel) this.jTableParameterList.getModel())) {
                support.log("Cached Simulation available, all Parameter match.", typeOfLogLevel.INFO);
                support.setMySimulationCache(mySimulationCache);
                support.setCachedSimulationEnabled(true);
                this.jButtonEmptyCache.setEnabled(true);
            } else {
                support.log("Cached Simulation not available, but all Parameter match. Maybe Stepping or Range is wrong.", typeOfLogLevel.INFO);
                support.setCachedSimulationEnabled(false);
            }
        } else {
            support.log("Cached Simulation not available, no simulation cache given.", typeOfLogLevel.INFO);
            support.setCachedSimulationEnabled(false);
            this.jButtonEmptyCache.setEnabled(false);
        }
        this.updateComboBoxSimulationType();
        this.jButtonEmptyCache.setEnabled(support.getMySimulationCache().getCacheSize() >= 1);
        this.saveProperties();
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

        jCheckBoxMenuItem3 = new javax.swing.JCheckBoxMenuItem();
        jButtonOpenSCPN = new javax.swing.JButton();
        jTextFieldSCPNFile = new javax.swing.JTextField();
        jButtonReload = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableParameterList = new javax.swing.JTable()
        {
            public void changeSelection(final int row, final int column, boolean toggle, boolean extend)
            {
                super.changeSelection(row, column, toggle, extend);
                this.editCellAt(row, column);
                this.transferFocus();
            }
        };
        jButtonExport = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jButtonStartBatchSimulation = new javax.swing.JButton();
        jButtonGenerateListOfExperiments = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jTabbedPaneOptiTargets = new javax.swing.JTabbedPane();
        measurementForm1 = new toe.MeasurementForm();
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
        jButton1 = new javax.swing.JButton();
        jCheckBoxDeleteTmpFiles = new javax.swing.JCheckBox();
        jButtonEmptyCache = new javax.swing.JButton();
        jLabelSimulationCountIndicator = new javax.swing.JLabel();
        jLabelTotalSimCount = new javax.swing.JLabel();
        jLabelCachSizeIndicator = new javax.swing.JLabel();
        jLabelCacheSize = new javax.swing.JLabel();
        jLabelDesignspaceSize = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuLog = new javax.swing.JMenu();
        jCheckBoxMenuItemLogToFile = new javax.swing.JCheckBoxMenuItem();
        jMenuItemClearLogFile = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jCheckBoxMenuItemLogToWindow = new javax.swing.JCheckBoxMenuItem();
        jMenuItemClearLogWindow = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jCheckBoxMenuItemResult = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemInfo = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemVerbose = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemError = new javax.swing.JCheckBoxMenuItem();

        jCheckBoxMenuItem3.setSelected(true);
        jCheckBoxMenuItem3.setText("jCheckBoxMenuItem3");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(675, 600));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jButtonOpenSCPN.setText("Open SCPN");
        jButtonOpenSCPN.setToolTipText("Open TimeNET SCPN (xml-file)");
        jButtonOpenSCPN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenSCPNActionPerformed(evt);
            }
        });

        jTextFieldSCPNFile.setToolTipText("Path lo loaded SCPN-file");

        jButtonReload.setText("Reload");
        jButtonReload.setToolTipText("Reload the chosen SCPN-file");
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
        jTableParameterList.setRowMargin(2);
        jTableParameterList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jTableParameterList);

        jButtonExport.setText("Export Experiments");
        jButtonExport.setToolTipText("");
        jButtonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.setToolTipText("Abort every running operation");
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

        measurementForm1.setToolTipText("Choose your Target Measurement (defined in SCPN)");
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
        jButtonLoadCacheFile.setToolTipText("Load simulation results from csv-file");
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
        jCheckBoxSlaveSimulator.setToolTipText("");
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

        jLabelMemoryUsage.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabelMemoryUsage.setText("Memory Usage");

        jLabelSpinning.setText("..");

        jSpinnerNumberOfOptimizationRuns.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        jSpinnerNumberOfOptimizationRuns.setToolTipText("How many optimizations will be run with the same settings");

        jButton1.setText("Secret");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBoxDeleteTmpFiles.setSelected(true);
        jCheckBoxDeleteTmpFiles.setText("Del. tmp-files");
        jCheckBoxDeleteTmpFiles.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxDeleteTmpFilesItemStateChanged(evt);
            }
        });

        jButtonEmptyCache.setText("Empty Cache");
        jButtonEmptyCache.setToolTipText("Clear local cache");
        jButtonEmptyCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEmptyCacheActionPerformed(evt);
            }
        });

        jLabelSimulationCountIndicator.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelSimulationCountIndicator.setText("SimCount");

        jLabelTotalSimCount.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabelTotalSimCount.setText("Total Sim#");

        jLabelCachSizeIndicator.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelCachSizeIndicator.setText("CacheSize");

        jLabelCacheSize.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabelCacheSize.setText("Cache Size");

        jLabelDesignspaceSize.setText("Designspace size:");

        jMenuFile.setText("File");
        jMenuFile.add(jSeparator4);
        jMenuFile.add(jSeparator5);

        jMenuItem5.setText("About");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItem5);
        jMenuFile.add(jSeparator6);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        jMenuItem1.setText("Quit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItem1);

        jMenuBar1.add(jMenuFile);

        jMenuLog.setText("Log");

        jCheckBoxMenuItemLogToFile.setText("Log to file");
        jCheckBoxMenuItemLogToFile.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxMenuItemLogToFileItemStateChanged(evt);
            }
        });
        jMenuLog.add(jCheckBoxMenuItemLogToFile);

        jMenuItemClearLogFile.setText("Clear Log File");
        jMenuItemClearLogFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearLogFileActionPerformed(evt);
            }
        });
        jMenuLog.add(jMenuItemClearLogFile);
        jMenuLog.add(jSeparator7);

        jCheckBoxMenuItemLogToWindow.setSelected(true);
        jCheckBoxMenuItemLogToWindow.setText("Log to window");
        jCheckBoxMenuItemLogToWindow.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxMenuItemLogToWindowItemStateChanged(evt);
            }
        });
        jMenuLog.add(jCheckBoxMenuItemLogToWindow);

        jMenuItemClearLogWindow.setText("Clear Log window");
        jMenuItemClearLogWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearLogWindowActionPerformed(evt);
            }
        });
        jMenuLog.add(jMenuItemClearLogWindow);
        jMenuLog.add(jSeparator8);

        jMenuItem4.setText("Print all Statistics in Log");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenuLog.add(jMenuItem4);

        jMenuItem2.setText("Open Log-Window");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenuLog.add(jMenuItem2);

        jMenu4.setText("LogLevel");

        jCheckBoxMenuItemResult.setSelected(true);
        jCheckBoxMenuItemResult.setText("Results");
        jMenu4.add(jCheckBoxMenuItemResult);

        jCheckBoxMenuItemInfo.setText("Info");
        jMenu4.add(jCheckBoxMenuItemInfo);

        jCheckBoxMenuItemVerbose.setText("Verbose");
        jMenu4.add(jCheckBoxMenuItemVerbose);

        jCheckBoxMenuItemError.setText("Error");
        jMenu4.add(jCheckBoxMenuItemError);

        jMenuLog.add(jMenu4);

        jMenuBar1.add(jMenuLog);

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
                        .add(11, 11, 11)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTextFieldSCPNFile)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(layout.createSequentialGroup()
                                        .add(jButtonPathToR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(18, 18, 18)
                                        .add(jButtonPlotR))
                                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 373, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(jButtonPathToTimeNet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(18, 18, 18)
                                        .add(jCheckBoxDeleteTmpFiles))
                                    .add(jCheckBoxSlaveSimulator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 198, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(jButtonEnterURLToSimServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(18, 18, 18)
                                        .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jLabelDesignspaceSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(0, 0, Short.MAX_VALUE)))
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
                            .add(jButtonStartBatchSimulation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(layout.createSequentialGroup()
                                        .add(jButtonLoadCacheFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 227, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(jButtonEmptyCache, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(jButtonStartOptimization, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(jSpinnerNumberOfOptimizationRuns, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jComboBoxBenchmarkFunction, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(jComboBoxSimulationType, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(jButtonOptiOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(jComboBoxOptimizationType, 0, 121, Short.MAX_VALUE))))
                                .add(5, 5, 5))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 265, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabelMemoryUsage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabelTotalSimCount)
                                            .add(jLabelCacheSize))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabelSimulationCountIndicator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jProgressBarMemoryUsage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabelCachSizeIndicator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jLabelSpinning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(23, 23, 23)))))
                .add(20, 20, 20))
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
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButtonLoadCacheFile)
                            .add(jButtonEmptyCache))
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
                .add(4, 4, 4)
                .add(jLabelDesignspaceSize)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonStartOptimization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSpinnerNumberOfOptimizationRuns, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonPathToTimeNet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckBoxDeleteTmpFiles))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelExportStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jButtonPathToR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jButtonPlotR, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(1, 1, 1)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButtonEnterURLToSimServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(12, 12, 12)
                        .add(jCheckBoxSlaveSimulator))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabelCachSizeIndicator)
                            .add(jLabelCacheSize))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabelSimulationCountIndicator)
                            .add(jLabelTotalSimCount))))
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
                    + fileChooser.getCurrentDirectory(), typeOfLogLevel.INFO);
            support.log("getSelectedFile() : "
                    + fileChooser.getSelectedFile(), typeOfLogLevel.INFO);
            this.jTextFieldSCPNFile.setText(fileChooser.getSelectedFile().toString());
            this.readSCPNFile(fileChooser.getSelectedFile().toString());
            this.saveProperties();
        } else {
            support.log("No Selection ", typeOfLogLevel.INFO);
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
            support.log("Length of ParameterSet-List: " + ListOfParameterSetsToBeWritten.size(), typeOfLogLevel.INFO);
            exporter tmpExporter = new exporter(ListOfParameterSetsToBeWritten);
        } else {
            support.log("Export-Operation canceled.", typeOfLogLevel.INFO);
        }
        support.setCancelEverything(false);
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        support.setCancelEverything(true);
        support.log("Try to cancel everything.", typeOfLogLevel.INFO);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Start of batch simulation
     */
    private void jButtonStartBatchSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartBatchSimulationActionPerformed
        support.setCancelEverything(false);
        //support.resetGlobalSimulationCounter();
        this.pushUIState();
        this.switchUIState(uiState.processRunning);

        //Ask for Tmp-Path
        String tmpPath = support.getPathToDirByDialog("Choose directory for export TMP-Files.\n ", support.getTmpPath());

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
                support.log("No Parametersets to simulate.", typeOfLogLevel.INFO);
                this.popUIState();
                return;
            }
            //If Parameterbase is null -->eject (This is needed for benchmark-simulations)
            if (support.getParameterBase() == null) {
                support.setStatusText("No Paramaterbase set.");
                support.log("No Paramaterbase set. No Simulation possible.", typeOfLogLevel.INFO);
                this.popUIState();
                return;
            }

            Simulator mySimulator = SimOptiFactory.getSimulator();
            mySimulator.initSimulator(ListOfParameterSetsToBeWritten, support.isCreateseparateLogFilesForEverySimulation());
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
        } else {
            this.popUIState();
        }
    }//GEN-LAST:event_jButtonGenerateListOfExperimentsActionPerformed

    private void jButtonStartOptimizationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartOptimizationActionPerformed
        //Show Dialog if many optiruns are planned
        if ((Integer) this.jSpinnerNumberOfOptimizationRuns.getValue() > 1 || support.getOptimizerPreferences().getNumberOfOptiPrefs() > 1) {
            JOptionPane pane = new JOptionPane(
                    "Do multiple Optimizations ?\n Perform " + (Integer) this.jSpinnerNumberOfOptimizationRuns.getValue() + " Optimizations with"
                    + "\n every of " + support.getOptimizerPreferences().getNumberOfOptiPrefs() + " different OptiPreferences?"
                    + " (Total: " + (Integer) this.jSpinnerNumberOfOptimizationRuns.getValue() * support.getOptimizerPreferences().getNumberOfOptiPrefs() + ")");
            Object[] options = new String[]{"Yes, perform multiple optimizations!", "No / Cancel"};
            pane.setOptions(options);
            JDialog dialog = pane.createDialog(new JFrame(), "Perform multiple optimizations?");
            dialog.setVisible(true);
            Object obj = pane.getValue();
            int result = -1;
            for (int k = 0; k < options.length; k++) {
                if (options[k].equals(obj)) {
                    result = k;
                }
            }

            if (result != 0) {
                return;
            }
        }

        support.setCancelEverything(false);
        //support.resetGlobalSimulationCounter();

        //Set base parameterset and orignal base parameterset in support
        support.setOriginalParameterBase(((parameterTableModel) jTableParameterList.getModel()).getListOfParameter());
        support.setParameterBase(((parameterTableModel) jTableParameterList.getModel()).getListOfParameter());

        //Send chosen Optimizertype to support-class
        support.setChosenOptimizerType((typeOfOptimization) this.jComboBoxOptimizationType.getSelectedItem());
        if (this.sizeOfDesignSpace <= support.DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION) {
            support.log("Design space to small, no Optimization posible.", typeOfLogLevel.INFO);
            support.setStatusText("Designspace to small for Opti.");
        } else if (this.getListOfActiveMeasureMentsToOptimize().size() >= 1) {
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
                    support.log("No Paramaterbase set. No Simulation possible.", typeOfLogLevel.INFO);
                    this.popUIState();
                    return;
                }
                //Remove all old Optimizationstatistics
                StatisticAggregator.removeOldOptimizationsFromList();

                //Save original Parameterset, for stepping and designspace borders
                support.setOriginalParameterBase(support.getCopyOfParameterSet(support.getParameterBase()));
                //start Optimization via extra method, set number of multiple optimizations before
                support.setNumberOfOptiRunsToGo((Integer) this.jSpinnerNumberOfOptimizationRuns.getValue());
                support.getOptimizerPreferences().setNumberOfActualOptimizationAnalysis(0);
                support.getOptimizerPreferences().loadPreferences();

                support.resetOptiStatistics();
                startOptimizationAgain();

            } else {
                support.log("No Tmp-Path given, Optimization not possible.", typeOfLogLevel.ERROR);
                this.popUIState();
            }

        } else {
            support.log("No Measurements to optimize for are chosen.", typeOfLogLevel.INFO);
            support.setStatusText("No Measurements chosen. No Opti possible.");
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
            support.log("Choosen outputdir: " + outputDir, typeOfLogLevel.INFO);
            this.setPathToTimeNet(outputDir);
            this.checkIfTimeNetPathIsCorrect();
        } else {
            support.log("No Path to TimeNET chosen.", typeOfLogLevel.INFO);
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
        final String inputFile;

        if (fileChooser.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().isDirectory()) {
                support.log("No input file chosen!", typeOfLogLevel.INFO);
                return;
            } else {
                inputFile = fileChooser.getSelectedFile().toString();
            }
            support.log("Choosen input file with cached simulation results: " + inputFile, typeOfLogLevel.INFO);
        } else {
            support.log("No input file chosen!", typeOfLogLevel.INFO);
            return;
        }
        //this.mySimulationCache=SimOptiFactory.getSimulationCache();
        //Should we empty the cache each time, or only at user-wish? Everytime!
        this.pushUIState();
        this.deactivateEveryComponentExcept(new Component[]{this.jButtonCancel});
        new Thread() {
            @Override
            public void run() {
                tryToFillCacheFromFile(inputFile);
                support.getMySimulationCache().reformatParameterTable((parameterTableModel) jTableParameterList.getModel());
                jTableParameterList.updateUI();
                calculateDesignSpace();
                checkIfCachedSimulationIsPossible();
                popUIState();
            }
        }.start();
    }//GEN-LAST:event_jButtonLoadCacheFileActionPerformed

    /**
     * It will try to fill the local cache from simualtion results out of a csv
     * file It will NOT fit the parameter table
     */
    private boolean tryToFillCacheFromFile(String inputFile) {
        support.emptyCache();
        File testFile = new File(inputFile);
        if (!testFile.isFile()) {
            return false;
        }
        try {
            parameterTableModel tmpModel = (parameterTableModel) this.jTableParameterList.getModel();
            this.mySimulationCache = support.getMySimulationCache();
            if (!mySimulationCache.parseSimulationCacheFile(inputFile, ((MeasurementForm) this.jTabbedPaneOptiTargets.getComponent(0)).getMeasurements(), tmpModel, this)) {
                support.log("Wrong Simulation cache file for this SCPN!", typeOfLogLevel.ERROR);
                support.setStatusText("Error loading cache-file!");
                return false;
            } else {
                support.log("Loading of Cache-file was successful. Will check if its working.", typeOfLogLevel.INFO);
                this.pathToLastSimulationCache = inputFile;
                this.saveProperties();
                //If cached simulation is available and not yet selected as simulator: activate cache as Cache/local simulation
                if (!this.jComboBoxSimulationType.getSelectedItem().toString().contains("Cache") && this.checkIfCachedSimulationIsPossible()) {
                    this.jComboBoxSimulationType.setSelectedItem(typeOfSimulator.Cache_Only);
                    support.setChosenSimulatorType(typeOfSimulator.Cache_Only);
                } else {
                }
                return true;
            }
        } catch (Exception e) {
            support.log("Seems no SCPN-File is loaded but we tried to load a cache file.", typeOfLogLevel.ERROR);
            return false;
        }
    }

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

            support.log("URL of Simulation-Server as given from user is " + s + "!", typeOfLogLevel.INFO);
            support.setRemoteAddress(s);
            this.checkIfURLIsCorrect();

        } else {
            support.log("URL of Simulation-Server was not entered!", typeOfLogLevel.INFO);
        }
    }//GEN-LAST:event_jButtonEnterURLToSimServerActionPerformed

    /**
     * Checks if URL to simulation server is correct. If it` correct, button
     * will be green, else red
     */
    private void checkIfURLIsCorrect() {
        String tmpURL = support.getReMoteAddress();
        boolean checksuccessful = false;
        support.log("Will try to check URL.", typeOfLogLevel.INFO);
        try {
            checksuccessful = support.checkRemoteAddress(tmpURL);
        } catch (IOException ex) {
            support.log("Problem checking the URL to distributed simulation.", typeOfLogLevel.ERROR);
        }

        support.log("Checking URL of distributed simulation server.", typeOfLogLevel.INFO);
        support.setDistributedSimulationAvailable(checksuccessful);
        updateComboBoxSimulationType();
        if (checksuccessful) {
            jButtonEnterURLToSimServer.setBackground(Color.GREEN);
            jButtonEnterURLToSimServer.setOpaque(true);
            jButtonEnterURLToSimServer.setBorderPainted(false);
            jButtonEnterURLToSimServer.setText("RESET URL of Sim.-Server");
            this.saveProperties();
            jButtonEnterURLToSimServer.setEnabled(true);
            jCheckBoxSlaveSimulator.setEnabled(true);
        } else {
            jButtonEnterURLToSimServer.setBackground(Color.RED);
            jButtonEnterURLToSimServer.setOpaque(true);
            jButtonEnterURLToSimServer.setBorderPainted(false);
            jButtonEnterURLToSimServer.setText("Enter URL of Sim.-Server");
            jButtonEnterURLToSimServer.setEnabled(true);
            jCheckBoxSlaveSimulator.setSelected(false);
            jCheckBoxSlaveSimulator.setEnabled(false);
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
            support.log("chosen outputdir: " + outputDir, typeOfLogLevel.INFO);
            this.setPathToR(outputDir);
            this.checkIfRPathIsCorrect();
        } else {
            support.log("No Path to R chosen.", typeOfLogLevel.INFO);
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
        System.gc();
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
                        support.log("Tmp Path ok and timenetpath ok, try to start slave-thread.", typeOfLogLevel.INFO);
                        this.mySlave.setShouldEnd(false);
                        this.saveProperties();
                        new Thread(this.mySlave).start();
                        this.jCheckBoxSlaveSimulator.setSelected(true);
                        this.switchUIState(uiState.clientState);
                    }
                }
            } else {
                //No tmp path selected -->eject
                support.log("No Tmp Path selected for slave mode.", typeOfLogLevel.ERROR);
                this.jCheckBoxSlaveSimulator.setSelected(false);
                support.setIsRunningAsSlave(false);
                this.saveProperties();
            }
        }
    }//GEN-LAST:event_jCheckBoxSlaveSimulatorMouseClicked

    private void jCheckBoxSlaveSimulatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSlaveSimulatorActionPerformed

    }//GEN-LAST:event_jCheckBoxSlaveSimulatorActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String newServerSecret = JOptionPane.showInputDialog(this, "Please enter a secred word/number to secure your simulations on server.", support.getServerSecret());
        if (newServerSecret != null) {
            support.setServerSecret(newServerSecret);
            this.saveProperties();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBoxDeleteTmpFilesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxDeleteTmpFilesItemStateChanged
        support.setDeleteTmpSimulationFiles(jCheckBoxDeleteTmpFiles.isSelected());
        this.saveProperties();
    }//GEN-LAST:event_jCheckBoxDeleteTmpFilesItemStateChanged

    private void jButtonEmptyCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEmptyCacheActionPerformed
        support.emptyCache();
        this.mySimulationCache = support.getMySimulationCache();
        this.pathToLastSimulationCache = "";
        checkIfCachedSimulationIsPossible();
        this.saveProperties();
        support.setStatusText("Cache discarded.");
        if (support.getChosenSimulatorType().equals(typeOfSimulator.Cache_Only)) {
            this.jComboBoxSimulationType.setSelectedItem(typeOfSimulator.Cached_Benchmark);
            support.setChosenSimulatorType(typeOfSimulator.Cached_Benchmark);
        }
    }//GEN-LAST:event_jButtonEmptyCacheActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        saveProperties();
    }//GEN-LAST:event_formWindowClosing

    /**
     * Calculates the design space, number of all permutations of parameters
     * with respect to the stepping sizes
     */
    public void calculateDesignSpace() {
        myGenerator = new generator(ListOfParameterSetsToBeWritten, fileName, jLabelExportStatus, this, jTableParameterList);
        this.sizeOfDesignSpace = myGenerator.getSizeOfDesignspace();
        //support.setStatusText("Designspace-Size:" + NumberFormat.getInstance().format(sizeOfDesignSpace));
        this.jLabelDesignspaceSize.setText("Designspace size: " + NumberFormat.getInstance().format(sizeOfDesignSpace));

        if (sizeOfDesignSpace > support.DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION) {
            this.jButtonStartOptimization.setEnabled(true);
            support.setStatusText("Optimization possible.");
        } else {
            this.jButtonStartOptimization.setEnabled(false);
            support.log("Design space smaller then " + support.DEFAULT_MINIMUM_DESIGNSPACE_FOR_OPTIMIZATION + ". Optimization not possible!", typeOfLogLevel.INFO);
            support.setStatusText("DS to small for Optimization.");
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
                support.log("Could not convert into double, maybe String is used. Will not iterate through parameter " + loopParameter.getName(), typeOfLogLevel.ERROR);
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
                    usedValue = support.round(usedValue, 3);
                    ArrayList<parameter> nextParameterSet = new ArrayList<>();
                    //Get copy of parameterset
                    for (parameter lastParameterSet1 : lastParameterSet) {
                        try {
                            nextParameterSet.add((parameter) lastParameterSet1.clone());
                        } catch (CloneNotSupportedException e) {
                            support.log("Clone is not Supported:" + e.toString(), typeOfLogLevel.ERROR);
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonEmptyCache;
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
    private javax.swing.JCheckBox jCheckBoxDeleteTmpFiles;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem3;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemError;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemInfo;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemLogToFile;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemLogToWindow;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemResult;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemVerbose;
    private javax.swing.JCheckBox jCheckBoxSlaveSimulator;
    private javax.swing.JComboBox jComboBoxBenchmarkFunction;
    private javax.swing.JComboBox jComboBoxOptimizationType;
    private javax.swing.JComboBox jComboBoxSimulationType;
    private javax.swing.JLabel jLabelCachSizeIndicator;
    private javax.swing.JLabel jLabelCacheSize;
    private javax.swing.JLabel jLabelDesignspaceSize;
    private javax.swing.JLabel jLabelExportStatus;
    private javax.swing.JLabel jLabelMemoryUsage;
    private javax.swing.JLabel jLabelSimulationCountIndicator;
    protected javax.swing.JLabel jLabelSpinning;
    private javax.swing.JLabel jLabelTotalSimCount;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItemClearLogFile;
    private javax.swing.JMenuItem jMenuItemClearLogWindow;
    private javax.swing.JMenu jMenuLog;
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
    private toe.MeasurementForm measurementForm1;
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
                support.log(parameterList.item(i).getAttributes().getNamedItem("name").getNodeValue(), typeOfLogLevel.INFO);
            }
            jTableParameterList.setModel(new parameterTableModel(parameterList, this));
            jTableParameterList.getModel().addTableModelListener(this);

            //Read measures
            NodeList MeasurenameList = doc.getElementsByTagName("measure");
            if (MeasurenameList.getLength() >= 1) {
                ArrayList<MeasureType> Measures = new ArrayList();
                support.log("****** Measure-Names ******", typeOfLogLevel.INFO);
                for (int i = 0; i < MeasurenameList.getLength(); i++) {
                    support.log(MeasurenameList.item(i).getAttributes().getNamedItem("name").getNodeValue(), typeOfLogLevel.INFO);
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

            //Set Editor for every Cell
            DefaultCellEditor singleclick = new DefaultCellEditor(new JTextField());
            singleclick.setClickCountToStart(1);
            for (int i = 0; i < jTableParameterList.getColumnCount(); i++) {
                jTableParameterList.setDefaultEditor(jTableParameterList.getColumnClass(i), singleclick);

            }

            this.fileName = filename;//nach Erfolg, globalen filename setzen
            support.setOriginalFilename(filename);
            activateGenerateButtons();
            activateReloadButtons();
        } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
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
                        support.log("These Parameters are equal:", typeOfLogLevel.INFO);
                        support.log(tmpParameterSetID + " and " + tmpListParameterID, typeOfLogLevel.INFO);
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
        ListOfParameterSetIds = new ArrayList<>();
        ListOfParameterSetsToBeWritten = new ArrayList<>();
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
    @Override
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
        ArrayList<parameter> resultParameterList = new ArrayList<>();
        for (parameter p : myParameterList) {
            if (!p.isExternalParameter() && !p.isIteratable()) {
                resultParameterList.add(p);
            }
        }
        support.getOptimizerPreferences().setPossibleInternalParameters(resultParameterList);
        support.getOptimizerPreferences().updateDimension();
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
        support.log("TimeNET should be here: " + tmpFile.getAbsolutePath(), typeOfLogLevel.INFO);
        if (tmpFile.canRead()) {
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
            support.setLocalSimulationAvailable(true);
            //Try to install "RemoteSystem Client.config"
            try {
                InputStream ddlStream = this.getClass().getClassLoader().getResourceAsStream("toe/RemoteSystem Client.config");
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
                support.log("Failed to install RemoteSystem Client.config", typeOfLogLevel.ERROR);
            }
        } else {

            //this.jButtonStartBatchSimulation.setEnabled(false);
            //this.jLabelCheckPathToTimeNet.setVisible(true);
            jButtonPathToTimeNet.setBackground(Color.RED);
            jButtonPathToTimeNet.setOpaque(true);
            jButtonPathToTimeNet.setBorderPainted(false);
            jButtonPathToTimeNet.setText("Enter Path To TimeNet");
            //jButtonStartOptimization.setEnabled(false);
            support.setLocalSimulationAvailable(false);
        }
        this.updateComboBoxSimulationType();
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

        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 2);
        }
        if (path.endsWith(File.separator + "bin")) {
            path = path.substring(0, path.length() - (File.separator + "bin").length());
        }

        File tmpFile = new File(path + File.separator + "bin" + File.separator + rApplicationName);

        support.log("R should be here: " + tmpFile.getAbsolutePath(), typeOfLogLevel.INFO);
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
            jButtonPathToR.setBorderPainted(false);
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
        support.log("Saving Properties.", typeOfLogLevel.INFO);
        try {

            auto.setProperty("timenetpath", this.getPathToTimeNet());
            auto.setProperty("file", this.jTextFieldSCPNFile.getText());

            auto.setProperty("serversecret", support.getServerSecret());

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
            auto.setProperty("deleteTmpFile", Boolean.toString(jCheckBoxDeleteTmpFiles.isSelected()));

            if (support.getTmpPath() != null) {
                auto.setProperty("tmppath", support.getTmpPath());
            } else {
                support.log("No tmp-path yet given. Please do so.", typeOfLogLevel.ERROR);
            }

            auto.setProperty("LOGLEVEL_ERROR", Boolean.toString(getLogLevelActivated_ERROR()));
            auto.setProperty("LOGLEVEL_INFO", Boolean.toString(getLogLevelActivated_INFO()));
            auto.setProperty("LOGLEVEL_RESULT", Boolean.toString(getLogLevelActivated_RESULT()));
            auto.setProperty("LOGLEVEL_VERBOSE", Boolean.toString(getLogLevelActivated_VERBOSE()));

            auto.setProperty("LOGTOWINDOW", Boolean.toString(getLogToWindow()));
            auto.setProperty("LOGTOFILE", Boolean.toString(getLogToFile()));

            File parserprops = new File(support.NAME_OF_PREFERENCES_FILE);
            auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
        } catch (IOException e) {
            support.log("Problem Saving the properties.", typeOfLogLevel.ERROR);
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
        support.log("Printing P-Set:", typeOfLogLevel.INFO);
        for (int i = 0; i < p.length; i++) {
            support.log(((parameter) p[i]).getName(), typeOfLogLevel.INFO);
            support.log(((parameter) p[i]).getValue() + " vs " + ((parameter) p1[i]).getValue(), typeOfLogLevel.INFO);
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
        ArrayList<parameter> parameterArray = new ArrayList<>();

        //Build ArrayList of parameters from table
        for (int i = 0; i < tModel.getRowCount(); i++) {
            parameter tmpParameter = new parameter();
            tmpParameter.setName(tModel.getValueAt(i, 0).toString());
            tmpParameter.setStartValue(tModel.getDoubleValueAt(i, 1));//=StartValue
            tmpParameter.setEndValue(tModel.getDoubleValueAt(i, 2));
            tmpParameter.setValue(tModel.getDoubleValueAt(i, 1));
            tmpParameter.setStepping(tModel.getDoubleValueAt(i, 3));
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
        ArrayList<MeasureType> myTmpList = new ArrayList<>();//((MeasurementForm)this.jTabbedPane1.getComponent(0)).getListOfMeasurements();

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
    protected void setMemoryProgressbar(int s) {

        if ((s <= 100) && (s >= 0)) {
            this.jProgressBarMemoryUsage.setValue(s);
        }

    }

    /**
     * Sets the tooltip of memory-progressbar
     *
     * @param s String to show as tooltip
     */
    protected void setMemoryProgressbarTooltip(String s) {
        this.jProgressBarMemoryUsage.setToolTipText(s);
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
         21-jButtonEmptyCache
         */
        this.listOfUIStates = new ArrayList<>();
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
                listOfUIStates.set(15, support.isDistributedSimulationAvailable());
                listOfUIStates.set(16, jComboBoxSimulationType.getSelectedItem().equals(typeOfSimulator.Benchmark) || jComboBoxSimulationType.getSelectedItem().equals(typeOfSimulator.Cached_Benchmark));
                listOfUIStates.set(21, support.getMySimulationCache().getCacheSize() >= 1);
                break;
            case clientState:
                for (int i = 0; i < listOfUIStates.size(); i++) {
                    listOfUIStates.set(i, false);
                }
                listOfUIStates.set(15, true);
                //Deactivate Benchmark JCombobox if no benchmark-simulator is chosen
                listOfUIStates.set(16, jComboBoxSimulationType.getSelectedItem().equals(typeOfSimulator.Benchmark) || jComboBoxSimulationType.getSelectedItem().equals(typeOfSimulator.Cached_Benchmark));
                listOfUIStates.set(21, support.getMySimulationCache().getCacheSize() >= 1);
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
            listOfUIStates.set(21, support.getMySimulationCache().getCacheSize() >= 1);
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
     * @param message will be shown in status-label
     * @param feedback will determine what to do next (button activation etc.)
     */
    @Override
    public void operationSucessfull(String message, typeOfProcessFeedback feedback) {
        int tmpNumberOfOptiRunsToGo = support.getNumberOfOptiRunsToGo();

        switch (feedback) {
            case GenerationSuccessful:
                this.popUIState();
                support.log("Generation of Designspace successful.", typeOfLogLevel.RESULT);
                jButtonStartBatchSimulation.setEnabled(true);
                break;
            case SimulationSuccessful:
                support.log("Simulation successful.", typeOfLogLevel.RESULT);
                support.setStatusText("Simulation was successful.");
                this.popUIState();
                break;
            case OptimizationSuccessful:
                if (tmpNumberOfOptiRunsToGo <= 1) {
                    support.unsetListOfChangableParametersMultiphase();//Stop Multiphase if it was active
                    support.setStatusText(message);
                    support.log("Last simulation run has ended. Will show statistics.", typeOfLogLevel.RESULT);
                    support.log("Ended was: " + feedback.toString(), typeOfLogLevel.RESULT);
                    support.log("This was Opti-Analysis: " + support.getOptimizerPreferences().getNumberOfActualOptimizationAnalysis().toString(), typeOfLogLevel.RESULT);
                    StatisticAggregator.printOptiStatistics();

                    String addonStringForFileName = support.getOptimizerPreferences().getNumberOfActualOptimizationAnalysis().toString();
                    if (support.getOptimizerPreferences().getNumberOfActualOptimizationAnalysis() <= 0) {
                        addonStringForFileName = "";
                    }
                    support.log("Used Opti-Prefs:", typeOfLogLevel.RESULT);
                    support.dumpTextFileToLog(support.NAME_OF_OPTIMIZER_PREFFERENCES_FILE + addonStringForFileName, typeOfLogLevel.RESULT);

                    //Reset all statistics!
                    //support.resetGlobalSimulationCounter();
                    StatisticAggregator.removeOldOptimizationsFromList();

                    //Check if other optiprefs have to be tested!
                    OptimizerPreferences p = support.getOptimizerPreferences();
                    if (support.getOptimizerPreferences().getNumberOfActualOptimizationAnalysis() >= p.getNumberOfOptiPrefs() - 1) {
                        //All Optimizations done
                        this.popUIState();
                        support.exportOptiStatistics();
                    } else {
                        //Load next Optiprefs and start again
                        p.setNumberOfActualOptimizationAnalysis(p.getNumberOfActualOptimizationAnalysis() + 1);
                        p.loadPreferences();
                        support.setNumberOfOptiRunsToGo((Integer) this.jSpinnerNumberOfOptimizationRuns.getValue());
                        //If cache or cache-support, reload cache
                        reloadFromCacheIfNeeded();
                        startOptimizationAgain();
                    }

                } else {
                    support.log("Starting next Optimization run, number:" + (tmpNumberOfOptiRunsToGo - 1), typeOfLogLevel.INFO);
                    support.setNumberOfOptiRunsToGo(tmpNumberOfOptiRunsToGo - 1);
                    //If cache or cache-support, reload cache
                    reloadFromCacheIfNeeded();
                    this.startOptimizationAgain();
                }
                break;

            default:
                break;
        }
    }

    /**
     * Check if cache is used. If yes, then reload cache In all cases delete
     * local cache to start from scratch
     */
    private void reloadFromCacheIfNeeded() {
        //Check if cache-support is enabled
        support.emptyCache();
        this.mySimulationCache = support.getMySimulationCache();
        //If yes, then reload cache
        typeOfSimulator usedSimulator = support.getChosenSimulatorType();
        if (usedSimulator.toString().contains("Cache")) {
            tryToFillCacheFromFile(this.pathToLastSimulationCache);
            checkIfCachedSimulationIsPossible();
        }
    }

    /**
     * Callback-Method of SimOptiCallback called when Simulation or optimization
     * is canceled
     *
     * @param message will be shown in staus-label
     */
    @Override
    public void operationCanceled(String message, typeOfProcessFeedback feedback) {
        this.popUIState();
        support.setStatusText(message);
        switch (feedback) {
            case GenerationCanceled:
                jButtonStartBatchSimulation.setEnabled(false);
                support.log("Generation canceled. Deactivate StartBatchButton.", typeOfLogLevel.INFO);
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
        support.log("Optimum-Value: " + Double.toString(SimOptiFactory.getSimulator().getCalculatedOptimum(support.getOptimizationMeasure()).getMeasureByName(support.getOptimizationMeasure().getMeasureName()).getMeanValue()), typeOfLogLevel.INFO);
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

    /**
     * Sets the Labeltext of SimCountIndicator
     *
     * @param count Value to be displayed in SimCountLabel
     */
    public void setSimCountLabel(int count) {
        this.jLabelSimulationCountIndicator.setText(NumberFormat.getInstance().format(count));
    }

    /**
     * Sets the LabelText of CacheSizeIndicator
     *
     * @param cacheSize Value to be displayed in CacheSizeLabel
     */
    public void setCacheSizeLabel(int cacheSize) {
        this.jLabelCachSizeIndicator.setText(NumberFormat.getInstance().format(cacheSize));
    }

    /**
     * Returns list of activated LogLevels
     *
     * @return List of activated LogLevels
     */
    public ArrayList getListOfActivatedLogLevels() {
        ArrayList resultList = new ArrayList();
        if (this.jCheckBoxMenuItemError.isSelected()) {
            resultList.add(typeOfLogLevel.ERROR);
        }
        if (this.jCheckBoxMenuItemInfo.isSelected()) {
            resultList.add(typeOfLogLevel.INFO);
        }
        if (this.jCheckBoxMenuItemResult.isSelected()) {
            resultList.add(typeOfLogLevel.RESULT);
        }
        if (this.jCheckBoxMenuItemVerbose.isSelected()) {
            resultList.add(typeOfLogLevel.VERBOSE);
        }
        return resultList;
    }

    /**
     * Get boolean info whether Messages of Loglevel Error will be logged to
     * chosen target
     *
     * @return True if Loglevel Error is active
     */
    public boolean getLogLevelActivated_ERROR() {
        return this.jCheckBoxMenuItemError.isSelected();
    }

    /**
     * Activate/Deactivate messages of Loglevel Error to be logged to chosen
     * target
     *
     * @param active Loglevel Error is active/not active
     */
    public void setLogLevelActivated_ERROR(boolean active) {
        this.jCheckBoxMenuItemError.setSelected(active);
    }

    /**
     * Get boolean info whether Messages of Loglevel Info will be logged to
     * chosen target
     *
     * @return True if Loglevel Info is active
     */
    public boolean getLogLevelActivated_INFO() {
        return this.jCheckBoxMenuItemInfo.isSelected();
    }

    /**
     * Activate/Deactivate messages of Loglevel Info to be logged to chosen
     * target
     *
     * @param active Loglevel Info is active/not active
     */
    public void setLogLevelActivated_INFO(boolean active) {
        this.jCheckBoxMenuItemInfo.setSelected(active);
    }

    /**
     * Get boolean info whether Messages of Loglevel Result will be logged to
     * chosen target
     *
     * @return True if Loglevel Result is active
     */
    public boolean getLogLevelActivated_RESULT() {
        return this.jCheckBoxMenuItemResult.isSelected();
    }

    /**
     * Activate/Deactivate messages of Loglevel Result to be logged to chosen
     * target
     *
     * @param active Loglevel Result is active/not active
     */
    public void setLogLevelActivated_RESULT(boolean active) {
        this.jCheckBoxMenuItemResult.setSelected(active);
    }

    /**
     * Get boolean info whether Messages of Loglevel Verbose will be logged to
     * chosen target
     *
     * @return True if Loglevel Verbose is active
     */
    public boolean getLogLevelActivated_VERBOSE() {
        return this.jCheckBoxMenuItemVerbose.isSelected();
    }

    /**
     * Activate/Deactivate messages of Loglevel Verbose to be logged to chosen
     * target
     *
     * @param active Loglevel Verbose is active/not active
     */
    public void setLogLevelActivated_VERBOSE(boolean active) {
        this.jCheckBoxMenuItemVerbose.setSelected(active);
    }

    /**
     * Get info whether logging to log-window is active
     *
     * @return true if logging to window is active
     */
    public boolean getLogToWindow() {
        return this.jCheckBoxMenuItemLogToWindow.isSelected();

    }

    /**
     * Activate/Deactivate logging to window
     *
     * @param active true to activate, else deactivate
     */
    public void setLogToWindow(boolean active) {
        this.jCheckBoxMenuItemLogToWindow.setSelected(active);
        support.setLogToWindow(active);
    }

    /**
     * Get info whether logging to log-file is active
     *
     * @return true if logging to file is active
     */
    public boolean getLogToFile() {
        return this.jCheckBoxMenuItemLogToFile.isSelected();

    }

    /**
     * Activate/Deactivate logging to file
     *
     * @param active true to activate, else deactivate
     */
    public void setLogToFile(boolean active) {
        this.jCheckBoxMenuItemLogToFile.setSelected(active);
        support.setLogToFile(active);
    }

}
