/*
 * Preferences for all optimization-algorithms, combines logic for load/save and GUI
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.optimization;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import toe.datamodel.parameter;
import toe.support;
import toe.typedef;
import toe.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public final class OptimizerPreferences extends javax.swing.JFrame {

    private final Properties auto = new Properties();

    private String pref_LogFileAddon = "";
// parameters for Hill Climbing
    private int pref_WrongSimulationsUntilBreak;
    private int pref_WrongSimulationsPerDirection;
    private typeOfStartValueEnum pref_StartValue;
    private typeOfNeighborhoodEnum pref_NeighborhoodType;
    private int pref_SizeOfNeighborhood;

// parameters for Simulated Annealing
    private boolean preventUpdateEpsilonBasedOnNumberOfSimulations = false;
    private int dimension = (int) 1;

    private int pref_NumberOfPhases;
    private typeOfOptimization pref_typeOfUsedMultiPhaseOptimization;
    private int pref_ConfidenceIntervallStart;
    private int pref_ConfidenceIntervallEnd;
    private int pref_MaxRelErrorStart;
    private int pref_MaxRelErrorEnd;
    private int pref_InternalParameterStart;
    private int pref_InternalParameterEnd;
    private boolean pref_KeepDesignSpaceAndResolution;
    private final double epsilon_min = 0.001;
    private final double epsilon_max = 1;

//parameters for genetic Optimization
    private int pref_GeneticPopulationSize;
    private double pref_GeneticMutationChance;
    private boolean pref_GeneticMutateTopSolution;
    private int pref_GeneticMaximumOptirunsWithoutSolution;
    private typeOfGeneticCrossover pref_GeneticTypeOfCrossover;
    private int pref_GeneticNumberOfCrossings;

//parameters for CSS Optimization
    private int pref_CSS_PopulationSize;
    private double pref_CSS_maxAttraction;

//parameters for ABC Optiization
    private int pref_ABC_NumEmployedBees;
    private int pref_ABC_NumOnlookerBees;
    private int pref_ABC_NumScoutBees;
    private int pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement;

//parameters for MVMO Optimization
    private int pref_MVMO_StartingPop;
    private int pref_MVMO_MaxPop;
    private double pref_MVMO_ScalingFactor;
    private double pref_MVMO_AsymmetryFactor;
    private double pref_MVMO_sd;
    private typeOfMVMOParentSelection pref_MVMO_parentSelection;
    private typeOfMVMOMutationSelection pref_MVMO_mutationSelection;

    private ArrayList<parameter> internalParameterList = null;

    final String noParameterString = "No parameter";

    private Integer numberOfActualOptimizationAnalysis = 0;//We start counting the pref-files from 0. But file #0 is without appended number, it`s the original.

    /**
     * Creates new form OptimizerHillPreferences
     */
    public OptimizerPreferences() {

        initComponents();
        this.setPref_WrongSimulationsUntilBreak(support.DEFAULT_WRONG_SOLUTIONS_IN_A_ROW);
        this.setPref_WrongSimulationsPerDirection(support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION);
        this.setPref_SizeOfNeighborhood(support.DEFAULT_SIZE_OF_NEIGHBORHOOD);
        this.setPref_Cooling(support.DEFAULT_TYPE_OF_ANNEALING, 0);
        this.setPref_Cooling(support.DEFAULT_TYPE_OF_ANNEALING, 1);
        this.setPref_CalculationOfNextParameterset(support.DEFAULT_CALC_NEXT_PARAMETER, 0);
        this.setPref_CalculationOfNextParameterset(support.DEFAULT_CALC_NEXT_PARAMETER, 1);

        this.jSpinnerSizeOfNeighborhoodInPercent.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        ((DefaultEditor) this.jSpinnerSizeOfNeighborhoodInPercent.getEditor()).getTextField().setEditable(false);

        ((DefaultEditor) this.jSpinnerWrongSolutionsPerDirectionUntilBreak.getEditor()).getTextField().setEditable(false);
        ((DefaultEditor) this.jSpinnerWrongSolutionsUntilBreak.getEditor()).getTextField().setEditable(false);


        /*DefaultListSelectionModel model = new DefaultListSelectionModel();
         model.addSelectionInterval(0, 5);


         this.jComboBoxOptimizationType.setRenderer(new EnabledJComboBoxRenderer(model));
         */
        ArrayList tmpVector = new ArrayList(Arrays.asList(typedef.typeOfOptimization.values()));
        tmpVector.remove((typeOfOptimization) typeOfOptimization.MultiPhase);
        tmpVector.remove((typeOfOptimization) typeOfOptimization.TwoPhase);
        this.jComboBoxOptimizationType.setModel(new DefaultComboBoxModel(tmpVector.toArray()));

        this.setNumberOfActualOptimizationAnalysis(0);
        this.loadPreferences();
        updateNumberOfOptiPrefs();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        measurementForm1 = new toe.MeasurementForm();
        jLabelStartvalueForParameters = new javax.swing.JLabel();
        jComboBoxTypeOfStartValue = new javax.swing.JComboBox();
        jTextFieldLogFileAddon = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jCheckBoxAddPrefsToLogfilename = new javax.swing.JCheckBox();
        jButtonSavePrefs = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelHillClimbing = new javax.swing.JPanel();
        jSpinnerWrongSolutionsPerDirectionUntilBreak = new javax.swing.JSpinner();
        jSpinnerWrongSolutionsUntilBreak = new javax.swing.JSpinner();
        jLabelWrongSolutionsUntilBreak = new javax.swing.JLabel();
        jLabelWrongSolutionsPerDirectionUntilBreak = new javax.swing.JLabel();
        jComboBoxTypeOfNeighborhood = new javax.swing.JComboBox();
        jLabelTypeOfNeighborhood = new javax.swing.JLabel();
        jSpinnerSizeOfNeighborhoodInPercent = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jPanelSimAnnealing = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxCalculationOfNextParameterset = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jSpinnerMaxTemperatureParameters = new javax.swing.JSpinner();
        jSpinnerMaxTemperatureCost = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jSpinnerTRatioScale = new javax.swing.JSpinner();
        jSpinnerTAnnealScale = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jSpinnerEpsilon = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jComboBoxCoolingMethod = new javax.swing.JComboBox();
        jSpinnerEstSASimulationCount = new javax.swing.JSpinner();
        jLabelEstSASimulationCount = new javax.swing.JLabel();
        jLabelDimensionDescription = new javax.swing.JLabel();
        jLabelDimensionNumber = new javax.swing.JLabel();
        jButtonCopySA1ToSA0 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jComboBoxCoolingMethod1 = new javax.swing.JComboBox();
        jSpinnerTRatioScale1 = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jSpinnerTAnnealScale1 = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        jComboBoxCalculationOfNextParameterset1 = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jSpinnerMaxTemperatureParameters1 = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        jSpinnerMaxTemperatureCost1 = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        jSpinnerEpsilon1 = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        jSpinnerEstSASimulationCount1 = new javax.swing.JSpinner();
        jLabelEstSASimulationCount1 = new javax.swing.JLabel();
        jButtonCopySA1ToSA1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jComboBoxNumberOfPhases = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSpinnerConfidenceIntervallStart = new javax.swing.JSpinner();
        jSpinnerConfidenceIntervallEnd = new javax.swing.JSpinner();
        jSpinnerMaxRelErrorEnd = new javax.swing.JSpinner();
        jSpinnerMaxRelErrorStart = new javax.swing.JSpinner();
        jCheckBoxKeepDesignspaceAndResolution = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jSpinnerInternalParameterEnd = new javax.swing.JSpinner();
        jSpinnerInternalParameterStart = new javax.swing.JSpinner();
        jComboBoxInternalParameterMultiphase = new javax.swing.JComboBox();
        jComboBoxOptimizationType = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabelGeneticPopulationSize = new javax.swing.JLabel();
        jLabelGeneticMutationChance = new javax.swing.JLabel();
        jSpinnerGeneticPopulationSize = new javax.swing.JSpinner();
        jSpinnerGeneticMutationChance = new javax.swing.JSpinner();
        jCheckBoxGeneticMutateTopSolution = new javax.swing.JCheckBox();
        jSpinnerGeneticMaxOptiRunsWithoutImprovement = new javax.swing.JSpinner();
        jLabel27 = new javax.swing.JLabel();
        jComboBoxGeneticTypeOfGeneticCrossing = new javax.swing.JComboBox();
        jLabel30 = new javax.swing.JLabel();
        jSpinnerGeneticMaxNumberOfCrossings = new javax.swing.JSpinner();
        jLabel31 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jSpinnerCSSPopulationSize = new javax.swing.JSpinner();
        jLabelCSSPopulationSize = new javax.swing.JLabel();
        jSpinnerCSSMaxAttraction = new javax.swing.JSpinner();
        jLabelCSSMaxAttraction = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jSpinnerABCNumEmployedBees = new javax.swing.JSpinner();
        jLabelABCNumEmployedBees = new javax.swing.JLabel();
        jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovement = new javax.swing.JSpinner();
        jSpinnerABCNumScoutBees = new javax.swing.JSpinner();
        jSpinnerABCNumOnlookerBees = new javax.swing.JSpinner();
        jLabelABCNumOnlookerBees = new javax.swing.JLabel();
        jLabelABCNumScoutBees = new javax.swing.JLabel();
        jLabelABCMaxNumberOfFoodUpdateCyclesWithoutImprovement = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jSpinnerMVMOStartingPop = new javax.swing.JSpinner();
        jLabelMVMOStartingPopulation = new javax.swing.JLabel();
        jLabelMVMOMaximumPopulation = new javax.swing.JLabel();
        jSpinnerMVMOMaxPop = new javax.swing.JSpinner();
        jLabelMVMOScalingFactor = new javax.swing.JLabel();
        jSpinnerMVMOScalingFactor = new javax.swing.JSpinner();
        jSpinnerMVMOAsymmetryFactor = new javax.swing.JSpinner();
        jLabelMVMOAsymmetryFactor = new javax.swing.JLabel();
        jSpinnerMVMOsd = new javax.swing.JSpinner();
        jLabelMVMOsd = new javax.swing.JLabel();
        jComboBoxTypeOfMVMOMutationSelection = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        jComboBoxTypeOfParentSelection = new javax.swing.JComboBox();
        jLabel29 = new javax.swing.JLabel();
        jButtonNextPrefs = new javax.swing.JButton();
        jButtonDelAllPrefs = new javax.swing.JButton();
        jTextFieldNumberOfOptiPrefs = new javax.swing.JTextField();
        jButtonPrevPrefs = new javax.swing.JButton();

        setResizable(false);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });

        jLabelStartvalueForParameters.setText("Startvalue for parameters");

        jComboBoxTypeOfStartValue.setModel(new DefaultComboBoxModel(typeOfStartValueEnum.values()));
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

        jTextFieldLogFileAddon.setToolTipText("Addon-Text for Logfilename");
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

        jButtonSavePrefs.setText("Save");
        jButtonSavePrefs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSavePrefsActionPerformed(evt);
            }
        });

        jSpinnerWrongSolutionsPerDirectionUntilBreak.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        jSpinnerWrongSolutionsPerDirectionUntilBreak.setToolTipText("This should always be lower than Wrong Solutions until break");
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

        jSpinnerWrongSolutionsUntilBreak.setModel(new javax.swing.SpinnerNumberModel(2, 1, null, 1));
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

        jLabelWrongSolutionsUntilBreak.setText("Wrong Solutions until break");

        jLabelWrongSolutionsPerDirectionUntilBreak.setText("Wrong Solutions per direction/parameter until break");

        jComboBoxTypeOfNeighborhood.setModel(new DefaultComboBoxModel(typeOfNeighborhoodEnum.values()));
        jComboBoxTypeOfNeighborhood.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxTypeOfNeighborhoodItemStateChanged(evt);
            }
        });
        jComboBoxTypeOfNeighborhood.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                jComboBoxTypeOfNeighborhoodVetoableChange(evt);
            }
        });

        jLabelTypeOfNeighborhood.setText("Type of Neighborhood");

        jLabel1.setText("Size of Neighborhood (in % of Designspace)");

        javax.swing.GroupLayout jPanelHillClimbingLayout = new javax.swing.GroupLayout(jPanelHillClimbing);
        jPanelHillClimbing.setLayout(jPanelHillClimbingLayout);
        jPanelHillClimbingLayout.setHorizontalGroup(
            jPanelHillClimbingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelHillClimbingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelHillClimbingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelHillClimbingLayout.createSequentialGroup()
                        .addComponent(jLabelTypeOfNeighborhood, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxTypeOfNeighborhood, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1)
                    .addGroup(jPanelHillClimbingLayout.createSequentialGroup()
                        .addGroup(jPanelHillClimbingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelWrongSolutionsUntilBreak)
                            .addComponent(jLabelWrongSolutionsPerDirectionUntilBreak))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelHillClimbingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinnerWrongSolutionsPerDirectionUntilBreak)
                            .addComponent(jSpinnerWrongSolutionsUntilBreak, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerSizeOfNeighborhoodInPercent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelHillClimbingLayout.setVerticalGroup(
            jPanelHillClimbingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelHillClimbingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelHillClimbingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTypeOfNeighborhood)
                    .addComponent(jComboBoxTypeOfNeighborhood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelHillClimbingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jSpinnerSizeOfNeighborhoodInPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanelHillClimbingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelHillClimbingLayout.createSequentialGroup()
                        .addComponent(jSpinnerWrongSolutionsUntilBreak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinnerWrongSolutionsPerDirectionUntilBreak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelHillClimbingLayout.createSequentialGroup()
                        .addComponent(jLabelWrongSolutionsUntilBreak, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelWrongSolutionsPerDirectionUntilBreak, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("HillClimbing", jPanelHillClimbing);

        jPanelSimAnnealing.setLayout(null);

        jLabel2.setText("Calculation of next Parameterset");
        jPanelSimAnnealing.add(jLabel2);
        jLabel2.setBounds(20, 60, 220, 16);

        jComboBoxCalculationOfNextParameterset.setModel(new DefaultComboBoxModel(typeOfAnnealingParameterCalculation.values()));
        jPanelSimAnnealing.add(jComboBoxCalculationOfNextParameterset);
        jComboBoxCalculationOfNextParameterset.setBounds(240, 60, 190, 27);

        jLabel3.setText("Max. Temp. for Parameters(T-0-par)");
        jPanelSimAnnealing.add(jLabel3);
        jLabel3.setBounds(20, 110, 230, 16);

        jSpinnerMaxTemperatureParameters.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, 1.0d, 0.01d));
        jSpinnerMaxTemperatureParameters.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerMaxTemperatureParameters, "#.##"));
        jSpinnerMaxTemperatureParameters.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMaxTemperatureParametersStateChanged(evt);
            }
        });
        jPanelSimAnnealing.add(jSpinnerMaxTemperatureParameters);
        jSpinnerMaxTemperatureParameters.setBounds(260, 110, 90, 26);

        jSpinnerMaxTemperatureCost.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, 1.0d, 0.01d));
        jSpinnerMaxTemperatureCost.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerMaxTemperatureCost, "#.##"));
        jSpinnerMaxTemperatureCost.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMaxTemperatureCostStateChanged(evt);
            }
        });
        jPanelSimAnnealing.add(jSpinnerMaxTemperatureCost);
        jSpinnerMaxTemperatureCost.setBounds(260, 140, 90, 26);

        jLabel6.setText("Max. Temp. for Cost(T-0-cost)");
        jPanelSimAnnealing.add(jLabel6);
        jLabel6.setBounds(20, 140, 200, 16);

        jLabel7.setText("TRatioScale");
        jPanelSimAnnealing.add(jLabel7);
        jLabel7.setBounds(480, 30, 80, 16);

        jLabel8.setText("TAnnealScale");
        jPanelSimAnnealing.add(jLabel8);
        jLabel8.setBounds(480, 60, 100, 16);

        jSpinnerTRatioScale.setModel(new SpinnerNumberModel(0.00001, 0.0, 100.0, 0.00001));
        jSpinnerTRatioScale.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerTRatioScale, "#.#####"));
        jSpinnerTRatioScale.setValue(0.00001);
        jSpinnerTRatioScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerTRatioScaleStateChanged(evt);
            }
        });
        jPanelSimAnnealing.add(jSpinnerTRatioScale);
        jSpinnerTRatioScale.setBounds(580, 20, 100, 26);

        jSpinnerTAnnealScale.setModel(new javax.swing.SpinnerNumberModel(100.0d, 0.0d, 1000000.0d, 10.0d));
        jSpinnerTAnnealScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerTAnnealScaleStateChanged(evt);
            }
        });
        jPanelSimAnnealing.add(jSpinnerTAnnealScale);
        jSpinnerTAnnealScale.setBounds(580, 60, 100, 26);

        jLabel5.setText("Epsilon (Abort-Temperature)");
        jPanelSimAnnealing.add(jLabel5);
        jLabel5.setBounds(20, 180, 210, 16);

        jSpinnerEpsilon.setModel(new javax.swing.SpinnerNumberModel(0.001d, 0.001d, 1.0d, 0.001d));
        jSpinnerEpsilon.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerEpsilon, "#.####"));
        jSpinnerEpsilon.setValue(0.01);
        jSpinnerEpsilon.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerEpsilonStateChanged(evt);
            }
        });
        jPanelSimAnnealing.add(jSpinnerEpsilon);
        jSpinnerEpsilon.setBounds(260, 180, 90, 26);

        jLabel9.setText("Cooling Method");
        jPanelSimAnnealing.add(jLabel9);
        jLabel9.setBounds(20, 20, 100, 16);

        jComboBoxCoolingMethod.setModel(new DefaultComboBoxModel(typeOfAnnealing.values()));
        jComboBoxCoolingMethod.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxCoolingMethodItemStateChanged(evt);
            }
        });
        jPanelSimAnnealing.add(jComboBoxCoolingMethod);
        jComboBoxCoolingMethod.setBounds(200, 20, 230, 27);

        jSpinnerEstSASimulationCount.setModel(new javax.swing.SpinnerNumberModel(100L, 0L, null, 10L));
        jSpinnerEstSASimulationCount.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerEstSASimulationCount, "#"));
        jSpinnerEstSASimulationCount.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerEstSASimulationCountStateChanged(evt);
            }
        });
        jPanelSimAnnealing.add(jSpinnerEstSASimulationCount);
        jSpinnerEstSASimulationCount.setBounds(210, 210, 140, 26);

        jLabelEstSASimulationCount.setText("Estimated # of Simulations");
        jPanelSimAnnealing.add(jLabelEstSASimulationCount);
        jLabelEstSASimulationCount.setBounds(20, 220, 210, 16);

        jLabelDimensionDescription.setText("Calculated problem dimension:");
        jPanelSimAnnealing.add(jLabelDimensionDescription);
        jLabelDimensionDescription.setBounds(20, 260, 240, 16);

        jLabelDimensionNumber.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabelDimensionNumber.setForeground(new java.awt.Color(204, 0, 51));
        jLabelDimensionNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelDimensionNumber.setText("1");
        jPanelSimAnnealing.add(jLabelDimensionNumber);
        jLabelDimensionNumber.setBounds(223, 260, 110, 16);

        jButtonCopySA1ToSA0.setText("Copy all prefs from second phase");
        jButtonCopySA1ToSA0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopySA1ToSA0ActionPerformed(evt);
            }
        });
        jPanelSimAnnealing.add(jButtonCopySA1ToSA0);
        jButtonCopySA1ToSA0.setBounds(20, 310, 250, 29);

        jTabbedPane1.addTab("Simulated Annealing", jPanelSimAnnealing);

        jPanel1.setLayout(null);

        jLabel10.setText("Cooling Method");
        jPanel1.add(jLabel10);
        jLabel10.setBounds(20, 60, 100, 16);

        jComboBoxCoolingMethod1.setModel(new DefaultComboBoxModel(typeOfAnnealing.values()));
        jComboBoxCoolingMethod1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxCoolingMethod1ItemStateChanged(evt);
            }
        });
        jPanel1.add(jComboBoxCoolingMethod1);
        jComboBoxCoolingMethod1.setBounds(230, 60, 200, 27);

        jSpinnerTRatioScale1.setModel(new SpinnerNumberModel(0.00001, 0.0, 100.0, 0.00001));
        jSpinnerTRatioScale1.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerTRatioScale1, "#.#####"));
        jSpinnerTRatioScale1.setValue(0.00001);
        jSpinnerTRatioScale1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerTRatioScale1StateChanged(evt);
            }
        });
        jPanel1.add(jSpinnerTRatioScale1);
        jSpinnerTRatioScale1.setBounds(580, 60, 100, 26);

        jLabel11.setText("TRatioScale");
        jPanel1.add(jLabel11);
        jLabel11.setBounds(480, 70, 80, 16);

        jSpinnerTAnnealScale1.setModel(new javax.swing.SpinnerNumberModel(100.0d, 0.0d, 10000.0d, 10.0d));
        jSpinnerTAnnealScale1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerTAnnealScale1StateChanged(evt);
            }
        });
        jPanel1.add(jSpinnerTAnnealScale1);
        jSpinnerTAnnealScale1.setBounds(580, 100, 100, 26);

        jLabel12.setText("TAnnealScale");
        jPanel1.add(jLabel12);
        jLabel12.setBounds(480, 100, 100, 16);

        jComboBoxCalculationOfNextParameterset1.setModel(new DefaultComboBoxModel(typeOfAnnealingParameterCalculation.values()));
        jPanel1.add(jComboBoxCalculationOfNextParameterset1);
        jComboBoxCalculationOfNextParameterset1.setBounds(230, 100, 200, 27);

        jLabel13.setText("Calculation of next Parameterset");
        jPanel1.add(jLabel13);
        jLabel13.setBounds(20, 100, 220, 16);

        jLabel14.setText("Max. Temp. for Parameters(T-0-par)");
        jPanel1.add(jLabel14);
        jLabel14.setBounds(20, 150, 230, 16);

        jSpinnerMaxTemperatureParameters1.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, 1.0d, 0.01d));
        jSpinnerMaxTemperatureParameters1.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerMaxTemperatureParameters1, "#.##"));
        jSpinnerMaxTemperatureParameters1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMaxTemperatureParameters1StateChanged(evt);
            }
        });
        jPanel1.add(jSpinnerMaxTemperatureParameters1);
        jSpinnerMaxTemperatureParameters1.setBounds(260, 150, 90, 26);

        jLabel15.setText("Max. Temp. for Cost(T-0-cost)");
        jPanel1.add(jLabel15);
        jLabel15.setBounds(20, 180, 200, 16);

        jSpinnerMaxTemperatureCost1.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, 1.0d, 0.01d));
        jSpinnerMaxTemperatureCost1.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerMaxTemperatureCost1, "#.##"));
        jSpinnerMaxTemperatureCost1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMaxTemperatureCost1StateChanged(evt);
            }
        });
        jPanel1.add(jSpinnerMaxTemperatureCost1);
        jSpinnerMaxTemperatureCost1.setBounds(260, 180, 90, 26);

        jLabel16.setText("Epsilon (Abort-Temperature)");
        jPanel1.add(jLabel16);
        jLabel16.setBounds(20, 220, 210, 16);

        jSpinnerEpsilon1.setModel(new javax.swing.SpinnerNumberModel(0.011d, 0.0d, 1.0d, 0.001d));
        jSpinnerEpsilon1.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerEpsilon1, "#.###"));
        jSpinnerEpsilon1.setValue(0.01);
        jSpinnerEpsilon1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerEpsilon1StateChanged(evt);
            }
        });
        jPanel1.add(jSpinnerEpsilon1);
        jSpinnerEpsilon1.setBounds(260, 220, 90, 26);

        jLabel17.setFont(new java.awt.Font("Lucida Grande", 1, 16)); // NOI18N
        jLabel17.setText("Parameters for first Phase are taken from Standard-Panel (Simulated Annealing)!!!");
        jPanel1.add(jLabel17);
        jLabel17.setBounds(20, 28, 787, 20);

        jSpinnerEstSASimulationCount1.setModel(new javax.swing.SpinnerNumberModel(100L, 0L, null, 10L));
        jSpinnerEstSASimulationCount1.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinnerEstSASimulationCount1, "#"));
        jSpinnerEstSASimulationCount1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerEstSASimulationCount1StateChanged(evt);
            }
        });
        jPanel1.add(jSpinnerEstSASimulationCount1);
        jSpinnerEstSASimulationCount1.setBounds(210, 250, 140, 26);

        jLabelEstSASimulationCount1.setText("Estimated # of Simulations");
        jPanel1.add(jLabelEstSASimulationCount1);
        jLabelEstSASimulationCount1.setBounds(20, 260, 210, 16);

        jButtonCopySA1ToSA1.setText("Copy all prefs from first phase");
        jButtonCopySA1ToSA1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopySA1ToSA1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonCopySA1ToSA1);
        jButtonCopySA1ToSA1.setBounds(20, 310, 250, 29);

        jTabbedPane1.addTab("Two-Phase", jPanel1);

        jPanel2.setLayout(null);

        jLabel18.setText("Used Optimization Algorithm");
        jPanel2.add(jLabel18);
        jLabel18.setBounds(30, 70, 220, 16);

        jComboBoxNumberOfPhases.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        jPanel2.add(jComboBoxNumberOfPhases);
        jComboBoxNumberOfPhases.setBounds(260, 30, 90, 27);

        jLabel19.setText("Number of Optimization-Phases");
        jPanel2.add(jLabel19);
        jLabel19.setBounds(28, 37, 220, 16);

        jLabel20.setText("Confidence-Intervall Start");
        jPanel2.add(jLabel20);
        jLabel20.setBounds(30, 110, 210, 16);

        jLabel22.setText("Confidence-Intervall End");
        jPanel2.add(jLabel22);
        jLabel22.setBounds(30, 140, 170, 16);

        jLabel23.setText("Maximum Rel. Error Start");
        jPanel2.add(jLabel23);
        jLabel23.setBounds(30, 190, 170, 16);

        jLabel24.setText("Maximum Rel. Error End");
        jPanel2.add(jLabel24);
        jLabel24.setBounds(30, 220, 170, 16);
        jPanel2.add(jSeparator1);
        jSeparator1.setBounds(30, 100, 400, 10);
        jPanel2.add(jSeparator2);
        jSeparator2.setBounds(30, 240, 400, 10);
        jPanel2.add(jSeparator3);
        jSeparator3.setBounds(30, 170, 400, 10);

        jSpinnerConfidenceIntervallStart.setModel(new javax.swing.SpinnerNumberModel(85, 85, 99, 1));
        jPanel2.add(jSpinnerConfidenceIntervallStart);
        jSpinnerConfidenceIntervallStart.setBounds(260, 110, 70, 26);

        jSpinnerConfidenceIntervallEnd.setModel(new javax.swing.SpinnerNumberModel(99, 85, 99, 1));
        jPanel2.add(jSpinnerConfidenceIntervallEnd);
        jSpinnerConfidenceIntervallEnd.setBounds(260, 140, 70, 26);

        jSpinnerMaxRelErrorEnd.setModel(new javax.swing.SpinnerNumberModel(1, 1, 15, 1));
        jPanel2.add(jSpinnerMaxRelErrorEnd);
        jSpinnerMaxRelErrorEnd.setBounds(260, 210, 70, 26);

        jSpinnerMaxRelErrorStart.setModel(new javax.swing.SpinnerNumberModel(5, 1, 15, 1));
        jPanel2.add(jSpinnerMaxRelErrorStart);
        jSpinnerMaxRelErrorStart.setBounds(260, 180, 70, 26);

        jCheckBoxKeepDesignspaceAndResolution.setText("Keep Designspace and Resolution");
        jPanel2.add(jCheckBoxKeepDesignspaceAndResolution);
        jCheckBoxKeepDesignspaceAndResolution.setBounds(510, 30, 260, 23);

        jPanel7.setBackground(new java.awt.Color(204, 204, 204));

        jLabel25.setText("Internal Parameter");

        jLabel26.setText("Internal Parameter Start");

        jLabel21.setText("Internal Parameter End");

        jSpinnerInternalParameterEnd.setModel(new javax.swing.SpinnerNumberModel());

        jSpinnerInternalParameterStart.setModel(new javax.swing.SpinnerNumberModel());

        jComboBoxInternalParameterMultiphase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No parameter" }));
        jComboBoxInternalParameterMultiphase.setEnabled(false);
        jComboBoxInternalParameterMultiphase.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxInternalParameterMultiphaseItemStateChanged(evt);
            }
        });
        jComboBoxInternalParameterMultiphase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxInternalParameterMultiphaseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(jSpinnerInternalParameterStart, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(jSpinnerInternalParameterEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(16, 16, 16))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jComboBoxInternalParameterMultiphase, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jComboBoxInternalParameterMultiphase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel26))
                    .addComponent(jSpinnerInternalParameterStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel21))
                    .addComponent(jSpinnerInternalParameterEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel7);
        jPanel7.setBounds(480, 120, 300, 110);

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
        jPanel2.add(jComboBoxOptimizationType);
        jComboBoxOptimizationType.setBounds(250, 70, 160, 27);

        jTabbedPane1.addTab("Multi-Phase", jPanel2);

        jLabelGeneticPopulationSize.setText("Population Size");

        jLabelGeneticMutationChance.setText("Mutation Chance %");

        jSpinnerGeneticPopulationSize.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        jSpinnerGeneticPopulationSize.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerGeneticPopulationSizePropertyChange(evt);
            }
        });

        jSpinnerGeneticMutationChance.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 100.0d, 1.0d));
        jSpinnerGeneticMutationChance.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerGeneticMutationChancePropertyChange(evt);
            }
        });

        jCheckBoxGeneticMutateTopSolution.setText("Mutate Top Solution");
        jCheckBoxGeneticMutateTopSolution.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxGeneticMutateTopSolutionActionPerformed(evt);
            }
        });

        jSpinnerGeneticMaxOptiRunsWithoutImprovement.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));

        jLabel27.setText("Maximum Optiruns without improvement");

        jComboBoxGeneticTypeOfGeneticCrossing.setModel(new DefaultComboBoxModel(typeOfGeneticCrossover.values()));
        jComboBoxGeneticTypeOfGeneticCrossing.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxGeneticTypeOfGeneticCrossingItemStateChanged(evt);
            }
        });
        jComboBoxGeneticTypeOfGeneticCrossing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxGeneticTypeOfGeneticCrossingActionPerformed(evt);
            }
        });

        jLabel30.setText("Kind of crossing");

        jSpinnerGeneticMaxNumberOfCrossings.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));

        jLabel31.setText("Number of crossings per generation");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jCheckBoxGeneticMutateTopSolution)
                        .addGap(86, 86, 86))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabelGeneticPopulationSize)
                            .addGap(45, 45, 45)
                            .addComponent(jSpinnerGeneticPopulationSize))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabelGeneticMutationChance)
                            .addGap(22, 22, 22)
                            .addComponent(jSpinnerGeneticMutationChance))))
                .addGap(46, 46, 46)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31)
                    .addComponent(jLabel27)
                    .addComponent(jLabel30))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSpinnerGeneticMaxOptiRunsWithoutImprovement, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .addComponent(jComboBoxGeneticTypeOfGeneticCrossing, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSpinnerGeneticMaxNumberOfCrossings, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                .addGap(492, 492, 492))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelGeneticPopulationSize)
                    .addComponent(jSpinnerGeneticPopulationSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerGeneticMaxOptiRunsWithoutImprovement, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelGeneticMutationChance)
                    .addComponent(jSpinnerGeneticMutationChance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxGeneticTypeOfGeneticCrossing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBoxGeneticMutateTopSolution)
                        .addComponent(jSpinnerGeneticMaxNumberOfCrossings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(260, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Genetic", jPanel3);

        jSpinnerCSSPopulationSize.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        jSpinnerCSSPopulationSize.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerCSSPopulationSizePropertyChange(evt);
            }
        });

        jLabelCSSPopulationSize.setText("Population Size");

        jSpinnerCSSMaxAttraction.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 100.0d, 1.0d));
        jSpinnerCSSMaxAttraction.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerCSSMaxAttractionPropertyChange(evt);
            }
        });

        jLabelCSSMaxAttraction.setText("Max Attraction");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelCSSMaxAttraction)
                    .addComponent(jLabelCSSPopulationSize, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSpinnerCSSPopulationSize)
                    .addComponent(jSpinnerCSSMaxAttraction, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addContainerGap(510, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelCSSPopulationSize)
                    .addComponent(jSpinnerCSSPopulationSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelCSSMaxAttraction)
                    .addComponent(jSpinnerCSSMaxAttraction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Charged System Search", jPanel4);

        jSpinnerABCNumEmployedBees.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        jSpinnerABCNumEmployedBees.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerABCNumEmployedBeesPropertyChange(evt);
            }
        });

        jLabelABCNumEmployedBees.setText("Employed Bees");

        jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovement.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovement.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovementPropertyChange(evt);
            }
        });

        jSpinnerABCNumScoutBees.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        jSpinnerABCNumScoutBees.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerABCNumScoutBeesPropertyChange(evt);
            }
        });

        jSpinnerABCNumOnlookerBees.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        jSpinnerABCNumOnlookerBees.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerABCNumOnlookerBeesPropertyChange(evt);
            }
        });

        jLabelABCNumOnlookerBees.setText("Onlooker Bees");

        jLabelABCNumScoutBees.setText("Scout Bees");

        jLabelABCMaxNumberOfFoodUpdateCyclesWithoutImprovement.setText("Max Missed Update Cycles");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabelABCNumEmployedBees)
                        .addGap(81, 81, 81)
                        .addComponent(jSpinnerABCNumEmployedBees))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabelABCMaxNumberOfFoodUpdateCyclesWithoutImprovement, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabelABCNumScoutBees, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabelABCNumOnlookerBees, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jSpinnerABCNumScoutBees, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                                    .addComponent(jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovement)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(jSpinnerABCNumOnlookerBees, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelABCNumEmployedBees)
                    .addComponent(jSpinnerABCNumEmployedBees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelABCNumOnlookerBees)
                    .addComponent(jSpinnerABCNumOnlookerBees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelABCNumScoutBees)
                    .addComponent(jSpinnerABCNumScoutBees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelABCMaxNumberOfFoodUpdateCyclesWithoutImprovement)
                    .addComponent(jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovement, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(215, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Artifical Bee Colony", jPanel5);

        jSpinnerMVMOStartingPop.setModel(new javax.swing.SpinnerNumberModel(2, 1, null, 1));
        jSpinnerMVMOStartingPop.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerMVMOStartingPopPropertyChange(evt);
            }
        });

        jLabelMVMOStartingPopulation.setText("Starting Population:");

        jLabelMVMOMaximumPopulation.setText("Maximum Population:");

        jSpinnerMVMOMaxPop.setModel(new javax.swing.SpinnerNumberModel(70, 1, null, 1));
        jSpinnerMVMOMaxPop.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerMVMOMaxPopPropertyChange(evt);
            }
        });

        jLabelMVMOScalingFactor.setText("Scaling-Factor:");

        jSpinnerMVMOScalingFactor.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, null, 1.0d));
        jSpinnerMVMOScalingFactor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerMVMOScalingFactorPropertyChange(evt);
            }
        });

        jSpinnerMVMOAsymmetryFactor.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, null, 1.0d));
        jSpinnerMVMOAsymmetryFactor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerMVMOAsymmetryFactorPropertyChange(evt);
            }
        });

        jLabelMVMOAsymmetryFactor.setText("Asymmetry-Factor:");

        jSpinnerMVMOsd.setModel(new javax.swing.SpinnerNumberModel(75.0d, 0.0d, null, 1.0d));
        jSpinnerMVMOsd.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSpinnerMVMOsdPropertyChange(evt);
            }
        });

        jLabelMVMOsd.setText("sd (standard si for zero-variance):");

        jComboBoxTypeOfMVMOMutationSelection.setModel(new DefaultComboBoxModel(typeOfMVMOMutationSelection.values()));
        jComboBoxTypeOfMVMOMutationSelection.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxTypeOfMVMOMutationSelectionItemStateChanged(evt);
            }
        });
        jComboBoxTypeOfMVMOMutationSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTypeOfMVMOMutationSelectionActionPerformed(evt);
            }
        });

        jLabel28.setText("Type of MVMO mutation selection");

        jComboBoxTypeOfParentSelection.setModel(new DefaultComboBoxModel(typeOfMVMOParentSelection.values()));
        jComboBoxTypeOfParentSelection.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxTypeOfParentSelectionItemStateChanged(evt);
            }
        });
        jComboBoxTypeOfParentSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTypeOfParentSelectionActionPerformed(evt);
            }
        });

        jLabel29.setText("Type of Parent selection");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jComboBoxTypeOfParentSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabelMVMOAsymmetryFactor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelMVMOScalingFactor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelMVMOMaximumPopulation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabelMVMOStartingPopulation))
                            .addGap(26, 26, 26)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jSpinnerMVMOAsymmetryFactor, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                                .addComponent(jSpinnerMVMOScalingFactor)
                                .addComponent(jSpinnerMVMOMaxPop)
                                .addComponent(jSpinnerMVMOStartingPop)))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(jLabelMVMOsd)
                            .addGap(18, 18, 18)
                            .addComponent(jSpinnerMVMOsd, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(jLabel28)
                            .addGap(35, 35, 35)
                            .addComponent(jComboBoxTypeOfMVMOMutationSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 461, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMVMOStartingPopulation)
                    .addComponent(jSpinnerMVMOStartingPop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMVMOMaximumPopulation)
                    .addComponent(jSpinnerMVMOMaxPop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMVMOScalingFactor)
                    .addComponent(jSpinnerMVMOScalingFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMVMOAsymmetryFactor)
                    .addComponent(jSpinnerMVMOAsymmetryFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMVMOsd, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinnerMVMOsd))
                .addGap(29, 29, 29)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxTypeOfMVMOMutationSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxTypeOfParentSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29))
                .addContainerGap(68, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("MVMO", jPanel6);

        jButtonNextPrefs.setText(">> Next");
        jButtonNextPrefs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextPrefsActionPerformed(evt);
            }
        });

        jButtonDelAllPrefs.setText("DelAllPrefs");
        jButtonDelAllPrefs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDelAllPrefsActionPerformed(evt);
            }
        });

        jTextFieldNumberOfOptiPrefs.setEditable(false);
        jTextFieldNumberOfOptiPrefs.setText("0");

        jButtonPrevPrefs.setText("Prev <<");
        jButtonPrevPrefs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrevPrefsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addComponent(jLabelStartvalueForParameters, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBoxTypeOfStartValue, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jCheckBoxAddPrefsToLogfilename))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextFieldLogFileAddon, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonPrevPrefs)
                                .addGap(21, 21, 21)
                                .addComponent(jButtonSavePrefs)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonNextPrefs)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonDelAllPrefs)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextFieldNumberOfOptiPrefs, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 831, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelStartvalueForParameters)
                    .addComponent(jComboBoxTypeOfStartValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxAddPrefsToLogfilename)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldLogFileAddon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonSavePrefs)
                    .addComponent(jButtonNextPrefs)
                    .addComponent(jButtonDelAllPrefs)
                    .addComponent(jTextFieldNumberOfOptiPrefs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonPrevPrefs))
                .addGap(5, 5, 5))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxTypeOfStartValueVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jComboBoxTypeOfStartValueVetoableChange

    }//GEN-LAST:event_jComboBoxTypeOfStartValueVetoableChange

    private void jTextFieldLogFileAddonVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jTextFieldLogFileAddonVetoableChange

    }//GEN-LAST:event_jTextFieldLogFileAddonVetoableChange

    private void jComboBoxTypeOfStartValueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxTypeOfStartValueItemStateChanged

    }//GEN-LAST:event_jComboBoxTypeOfStartValueItemStateChanged

    private void jTextFieldLogFileAddonInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTextFieldLogFileAddonInputMethodTextChanged

    }//GEN-LAST:event_jTextFieldLogFileAddonInputMethodTextChanged

    private void jTextFieldLogFileAddonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldLogFileAddonKeyReleased

    }//GEN-LAST:event_jTextFieldLogFileAddonKeyReleased

    private void jButtonSavePrefsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSavePrefsActionPerformed
        this.savePreferences();
    }//GEN-LAST:event_jButtonSavePrefsActionPerformed

    private void jSpinnerABCNumOnlookerBeesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerABCNumOnlookerBeesPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerABCNumOnlookerBeesPropertyChange

    private void jSpinnerABCNumScoutBeesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerABCNumScoutBeesPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerABCNumScoutBeesPropertyChange

    private void jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovementPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovementPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovementPropertyChange

    private void jSpinnerABCNumEmployedBeesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerABCNumEmployedBeesPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerABCNumEmployedBeesPropertyChange

    private void jSpinnerCSSMaxAttractionPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerCSSMaxAttractionPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerCSSMaxAttractionPropertyChange

    private void jSpinnerCSSPopulationSizePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerCSSPopulationSizePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerCSSPopulationSizePropertyChange

    private void jCheckBoxGeneticMutateTopSolutionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxGeneticMutateTopSolutionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxGeneticMutateTopSolutionActionPerformed

    private void jSpinnerGeneticMutationChancePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerGeneticMutationChancePropertyChange

    }//GEN-LAST:event_jSpinnerGeneticMutationChancePropertyChange

    private void jSpinnerGeneticPopulationSizePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerGeneticPopulationSizePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerGeneticPopulationSizePropertyChange

    private void jComboBoxOptimizationTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxOptimizationTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxOptimizationTypeActionPerformed

    private void jComboBoxOptimizationTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxOptimizationTypeItemStateChanged
        support.setChosenOptimizerType((typedef.typeOfOptimization) this.jComboBoxOptimizationType.getSelectedItem());
    }//GEN-LAST:event_jComboBoxOptimizationTypeItemStateChanged

    private void jComboBoxTypeOfNeighborhoodVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jComboBoxTypeOfNeighborhoodVetoableChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTypeOfNeighborhoodVetoableChange

    private void jComboBoxTypeOfNeighborhoodItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxTypeOfNeighborhoodItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTypeOfNeighborhoodItemStateChanged

    private void jSpinnerWrongSolutionsUntilBreakVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jSpinnerWrongSolutionsUntilBreakVetoableChange

    }//GEN-LAST:event_jSpinnerWrongSolutionsUntilBreakVetoableChange

    private void jSpinnerWrongSolutionsUntilBreakStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerWrongSolutionsUntilBreakStateChanged

    }//GEN-LAST:event_jSpinnerWrongSolutionsUntilBreakStateChanged

    private void jSpinnerWrongSolutionsPerDirectionUntilBreakVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jSpinnerWrongSolutionsPerDirectionUntilBreakVetoableChange

    }//GEN-LAST:event_jSpinnerWrongSolutionsPerDirectionUntilBreakVetoableChange

    private void jSpinnerWrongSolutionsPerDirectionUntilBreakStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerWrongSolutionsPerDirectionUntilBreakStateChanged

    }//GEN-LAST:event_jSpinnerWrongSolutionsPerDirectionUntilBreakStateChanged

    private void jSpinnerMVMOStartingPopPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerMVMOStartingPopPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerMVMOStartingPopPropertyChange

    private void jSpinnerMVMOMaxPopPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerMVMOMaxPopPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerMVMOMaxPopPropertyChange

    private void jSpinnerMVMOScalingFactorPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerMVMOScalingFactorPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerMVMOScalingFactorPropertyChange

    private void jSpinnerMVMOAsymmetryFactorPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerMVMOAsymmetryFactorPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerMVMOAsymmetryFactorPropertyChange

    private void jSpinnerMVMOsdPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSpinnerMVMOsdPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jSpinnerMVMOsdPropertyChange

    private void jComboBoxInternalParameterMultiphaseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxInternalParameterMultiphaseItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxInternalParameterMultiphaseItemStateChanged

    private void jComboBoxInternalParameterMultiphaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxInternalParameterMultiphaseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxInternalParameterMultiphaseActionPerformed

    private void jComboBoxTypeOfMVMOParentSelectionVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jComboBoxTypeOfMVMOParentSelectionVetoableChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTypeOfMVMOParentSelectionVetoableChange

    private void jComboBoxTypeOfMVMOMutationSelectionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxTypeOfMVMOParentSelectionItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTypeOfMVMOParentSelectionItemStateChanged

    private void jComboBoxTypeOfMVMOMutationSelectionVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_jComboBoxTypeOfMVMOMutationSelectionVetoableChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTypeOfMVMOMutationSelectionVetoableChange

    private void jComboBoxTypeOfMVMOParentSelectionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxOptimizationType1ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxOptimizationType1ItemStateChanged

    private void jComboBoxTypeOfMVMOMutationSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTypeOfMVMOMutationSelectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTypeOfMVMOMutationSelectionActionPerformed

    private void jComboBoxTypeOfParentSelectionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxTypeOfParentSelectionItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTypeOfParentSelectionItemStateChanged

    private void jComboBoxTypeOfParentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTypeOfParentSelectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxTypeOfParentSelectionActionPerformed

    private void jComboBoxGeneticTypeOfGeneticCrossingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxGeneticTypeOfGeneticCrossingItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxGeneticTypeOfGeneticCrossingItemStateChanged

    private void jComboBoxGeneticTypeOfGeneticCrossingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxGeneticTypeOfGeneticCrossingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxGeneticTypeOfGeneticCrossingActionPerformed

    private void jButtonNextPrefsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextPrefsActionPerformed
        //Save a new opti-preferences-file in the default path
        //Save the actual optPrefs
        this.savePreferences();
        //Increase number of optiPrefs
        this.setNumberOfActualOptimizationAnalysis((Integer) (this.getNumberOfActualOptimizationAnalysis() + 1));
        support.log("Next number of optiPrefs is " + this.getNumberOfActualOptimizationAnalysis().toString(), typeOfLogLevel.INFO);
        //Load optiPrefs from given File
        this.loadPreferences();
        //Save again optiPrefs to next file
        this.savePreferences();
        updateNumberOfOptiPrefs();
    }//GEN-LAST:event_jButtonNextPrefsActionPerformed

    /**
     * Returns the number of Opti-Pref-files in standard directory
     *
     * @return Number of Opti-Pref-Files
     */
    public int getNumberOfOptiPrefs() {
        String standardFileName = support.NAME_OF_OPTIMIZER_PREFFERENCES_FILE;
        String target_file;
        int numberOfFoundOptiPrefs = 0;
        File standardFile = new File(standardFileName);

        File folderToScan = new File(standardFile.getParent());

        File[] listOfFiles = folderToScan.listFiles();

        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    target_file = listOfFiles[i].getName();
                    if (target_file.startsWith(standardFile.getName())) {
                        numberOfFoundOptiPrefs++;
                    }
                }
            }
        }
        return numberOfFoundOptiPrefs;
    }

    /**
     * Updates the label to show how many Optipreferences are stored in
     * pref-folder
     */
    private void updateNumberOfOptiPrefs() {
        this.jTextFieldNumberOfOptiPrefs.setText(Integer.toString(getNumberOfOptiPrefs()));
    }

    private void jButtonDelAllPrefsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDelAllPrefsActionPerformed
        //Delete all Optimizerprefs and save the standard-version
        String standardFileName = support.NAME_OF_OPTIMIZER_PREFFERENCES_FILE;
        String target_file;
        File standardFile = new File(standardFileName);
        File folderToScan = new File(standardFile.getParent());

        File[] listOfFiles = folderToScan.listFiles();
        ArrayList listOfOptiPrefsToDelete = new ArrayList<>();

        for (File chosenFile : listOfFiles) {
            if (chosenFile.isFile()) {
                target_file = chosenFile.getName();
                if (target_file.startsWith(standardFile.getName())) {
                    listOfOptiPrefsToDelete.add(chosenFile);
                }
            }
        }

        for (Object iterator : listOfOptiPrefsToDelete) {
            File f = (File) iterator;
            f.delete();
        }
        this.setNumberOfActualOptimizationAnalysis((Integer) 0);
        this.savePreferences();
        this.updateNumberOfOptiPrefs();

    }//GEN-LAST:event_jButtonDelAllPrefsActionPerformed

    private void jButtonPrevPrefsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrevPrefsActionPerformed
        //Save a new opti-preferences-file in the default path
        //Save the actual optPrefs
        this.savePreferences();
        //Increase number of optiPrefs
        this.setNumberOfActualOptimizationAnalysis((Integer) Math.max(this.getNumberOfActualOptimizationAnalysis() - 1, 0));
        support.log("Next number of optiPrefs is " + this.getNumberOfActualOptimizationAnalysis().toString(), typeOfLogLevel.INFO);
        //Load optiPrefs from given File
        this.loadPreferences();
        //Save again optiPrefs to next file
        this.savePreferences();
        updateNumberOfOptiPrefs();
    }//GEN-LAST:event_jButtonPrevPrefsActionPerformed

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        support.log("OptiWindow lost focus. Will save optiPrefs.", typeOfLogLevel.INFO);
        this.savePreferences();
    }//GEN-LAST:event_formWindowLostFocus

    private void jSpinnerEpsilonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerEpsilonStateChanged
        updateNumberOfEstimatedSASimulations(0);
    }//GEN-LAST:event_jSpinnerEpsilonStateChanged

    private void jSpinnerMaxTemperatureCostStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMaxTemperatureCostStateChanged
        updateNumberOfEstimatedSASimulations(0);
    }//GEN-LAST:event_jSpinnerMaxTemperatureCostStateChanged

    private void jSpinnerMaxTemperatureParametersStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMaxTemperatureParametersStateChanged
        updateNumberOfEstimatedSASimulations(0);
    }//GEN-LAST:event_jSpinnerMaxTemperatureParametersStateChanged

    private void jComboBoxCoolingMethodItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxCoolingMethodItemStateChanged
        updateNumberOfEstimatedSASimulations(0);
    }//GEN-LAST:event_jComboBoxCoolingMethodItemStateChanged

    private void jSpinnerTRatioScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerTRatioScaleStateChanged
        updateNumberOfEstimatedSASimulations(0);
    }//GEN-LAST:event_jSpinnerTRatioScaleStateChanged

    private void jSpinnerTAnnealScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerTAnnealScaleStateChanged
        updateNumberOfEstimatedSASimulations(0);
    }//GEN-LAST:event_jSpinnerTAnnealScaleStateChanged

    private void jSpinnerEstSASimulationCountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerEstSASimulationCountStateChanged
        updateEpsilonBasedOnNumberOfSimulations(0);
    }//GEN-LAST:event_jSpinnerEstSASimulationCountStateChanged

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        updateDimension();
    }//GEN-LAST:event_formWindowGainedFocus

    private void jSpinnerEstSASimulationCount1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerEstSASimulationCount1StateChanged
        updateEpsilonBasedOnNumberOfSimulations(1);
    }//GEN-LAST:event_jSpinnerEstSASimulationCount1StateChanged

    private void jComboBoxCoolingMethod1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxCoolingMethod1ItemStateChanged
        updateNumberOfEstimatedSASimulations(1);
    }//GEN-LAST:event_jComboBoxCoolingMethod1ItemStateChanged

    private void jSpinnerMaxTemperatureParameters1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMaxTemperatureParameters1StateChanged
        updateNumberOfEstimatedSASimulations(1);
    }//GEN-LAST:event_jSpinnerMaxTemperatureParameters1StateChanged

    private void jSpinnerMaxTemperatureCost1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMaxTemperatureCost1StateChanged
        updateNumberOfEstimatedSASimulations(1);
    }//GEN-LAST:event_jSpinnerMaxTemperatureCost1StateChanged

    private void jSpinnerEpsilon1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerEpsilon1StateChanged
        updateNumberOfEstimatedSASimulations(1);
    }//GEN-LAST:event_jSpinnerEpsilon1StateChanged

    private void jSpinnerTRatioScale1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerTRatioScale1StateChanged
        updateNumberOfEstimatedSASimulations(1);
    }//GEN-LAST:event_jSpinnerTRatioScale1StateChanged

    private void jSpinnerTAnnealScale1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerTAnnealScale1StateChanged
        updateNumberOfEstimatedSASimulations(1);
    }//GEN-LAST:event_jSpinnerTAnnealScale1StateChanged

    private void jButtonCopySA1ToSA0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopySA1ToSA0ActionPerformed
        copySAParameters(1, 0);
    }//GEN-LAST:event_jButtonCopySA1ToSA0ActionPerformed

    private void jButtonCopySA1ToSA1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopySA1ToSA1ActionPerformed
        copySAParameters(0, 1);
    }//GEN-LAST:event_jButtonCopySA1ToSA1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCopySA1ToSA0;
    private javax.swing.JButton jButtonCopySA1ToSA1;
    private javax.swing.JButton jButtonDelAllPrefs;
    private javax.swing.JButton jButtonNextPrefs;
    private javax.swing.JButton jButtonPrevPrefs;
    private javax.swing.JButton jButtonSavePrefs;
    private javax.swing.JCheckBox jCheckBoxAddPrefsToLogfilename;
    private javax.swing.JCheckBox jCheckBoxGeneticMutateTopSolution;
    private javax.swing.JCheckBox jCheckBoxKeepDesignspaceAndResolution;
    private javax.swing.JComboBox jComboBoxCalculationOfNextParameterset;
    private javax.swing.JComboBox jComboBoxCalculationOfNextParameterset1;
    private javax.swing.JComboBox jComboBoxCoolingMethod;
    private javax.swing.JComboBox jComboBoxCoolingMethod1;
    private javax.swing.JComboBox jComboBoxGeneticTypeOfGeneticCrossing;
    private javax.swing.JComboBox jComboBoxInternalParameterMultiphase;
    private javax.swing.JComboBox jComboBoxNumberOfPhases;
    private javax.swing.JComboBox jComboBoxOptimizationType;
    private javax.swing.JComboBox jComboBoxTypeOfMVMOMutationSelection;
    public javax.swing.JComboBox jComboBoxTypeOfNeighborhood;
    private javax.swing.JComboBox jComboBoxTypeOfParentSelection;
    public javax.swing.JComboBox jComboBoxTypeOfStartValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelABCMaxNumberOfFoodUpdateCyclesWithoutImprovement;
    private javax.swing.JLabel jLabelABCNumEmployedBees;
    private javax.swing.JLabel jLabelABCNumOnlookerBees;
    private javax.swing.JLabel jLabelABCNumScoutBees;
    private javax.swing.JLabel jLabelCSSMaxAttraction;
    private javax.swing.JLabel jLabelCSSPopulationSize;
    private javax.swing.JLabel jLabelDimensionDescription;
    private javax.swing.JLabel jLabelDimensionNumber;
    private javax.swing.JLabel jLabelEstSASimulationCount;
    private javax.swing.JLabel jLabelEstSASimulationCount1;
    private javax.swing.JLabel jLabelGeneticMutationChance;
    private javax.swing.JLabel jLabelGeneticPopulationSize;
    private javax.swing.JLabel jLabelMVMOAsymmetryFactor;
    private javax.swing.JLabel jLabelMVMOMaximumPopulation;
    private javax.swing.JLabel jLabelMVMOScalingFactor;
    private javax.swing.JLabel jLabelMVMOStartingPopulation;
    private javax.swing.JLabel jLabelMVMOsd;
    private javax.swing.JLabel jLabelStartvalueForParameters;
    private javax.swing.JLabel jLabelTypeOfNeighborhood;
    private javax.swing.JLabel jLabelWrongSolutionsPerDirectionUntilBreak;
    private javax.swing.JLabel jLabelWrongSolutionsUntilBreak;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanelHillClimbing;
    private javax.swing.JPanel jPanelSimAnnealing;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSpinner jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovement;
    private javax.swing.JSpinner jSpinnerABCNumEmployedBees;
    private javax.swing.JSpinner jSpinnerABCNumOnlookerBees;
    private javax.swing.JSpinner jSpinnerABCNumScoutBees;
    private javax.swing.JSpinner jSpinnerCSSMaxAttraction;
    private javax.swing.JSpinner jSpinnerCSSPopulationSize;
    private javax.swing.JSpinner jSpinnerConfidenceIntervallEnd;
    private javax.swing.JSpinner jSpinnerConfidenceIntervallStart;
    private javax.swing.JSpinner jSpinnerEpsilon;
    private javax.swing.JSpinner jSpinnerEpsilon1;
    private javax.swing.JSpinner jSpinnerEstSASimulationCount;
    private javax.swing.JSpinner jSpinnerEstSASimulationCount1;
    private javax.swing.JSpinner jSpinnerGeneticMaxNumberOfCrossings;
    private javax.swing.JSpinner jSpinnerGeneticMaxOptiRunsWithoutImprovement;
    private javax.swing.JSpinner jSpinnerGeneticMutationChance;
    private javax.swing.JSpinner jSpinnerGeneticPopulationSize;
    private javax.swing.JSpinner jSpinnerInternalParameterEnd;
    private javax.swing.JSpinner jSpinnerInternalParameterStart;
    private javax.swing.JSpinner jSpinnerMVMOAsymmetryFactor;
    private javax.swing.JSpinner jSpinnerMVMOMaxPop;
    private javax.swing.JSpinner jSpinnerMVMOScalingFactor;
    private javax.swing.JSpinner jSpinnerMVMOStartingPop;
    private javax.swing.JSpinner jSpinnerMVMOsd;
    private javax.swing.JSpinner jSpinnerMaxRelErrorEnd;
    private javax.swing.JSpinner jSpinnerMaxRelErrorStart;
    private javax.swing.JSpinner jSpinnerMaxTemperatureCost;
    private javax.swing.JSpinner jSpinnerMaxTemperatureCost1;
    private javax.swing.JSpinner jSpinnerMaxTemperatureParameters;
    private javax.swing.JSpinner jSpinnerMaxTemperatureParameters1;
    private javax.swing.JSpinner jSpinnerSizeOfNeighborhoodInPercent;
    private javax.swing.JSpinner jSpinnerTAnnealScale;
    private javax.swing.JSpinner jSpinnerTAnnealScale1;
    private javax.swing.JSpinner jSpinnerTRatioScale;
    private javax.swing.JSpinner jSpinnerTRatioScale1;
    private javax.swing.JSpinner jSpinnerWrongSolutionsPerDirectionUntilBreak;
    private javax.swing.JSpinner jSpinnerWrongSolutionsUntilBreak;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldLogFileAddon;
    private javax.swing.JTextField jTextFieldNumberOfOptiPrefs;
    private toe.MeasurementForm measurementForm1;
    // End of variables declaration//GEN-END:variables

    /**
     * Load Preferences from defined file
     */
    public void loadPreferences() {
        String addonStringForFileName = this.getNumberOfActualOptimizationAnalysis().toString();
        if (this.getNumberOfActualOptimizationAnalysis() <= 0) {
            addonStringForFileName = "";
        }
        String filename = support.NAME_OF_OPTIMIZER_PREFFERENCES_FILE + addonStringForFileName;
        //If file does not exist, create it by savingprefs with actual numberOfActualOptimizationprefs
        File testFileHandle = new File(filename);
        if (!testFileHandle.isFile()) {
            this.savePreferences();
        }
        try {
            FileInputStream in = new FileInputStream(filename);
            auto.load(in);
            in.close();

            this.setPref_WrongSimulationsUntilBreak(support.loadIntFromProperties("pref_WrongSimulationsUntilBreak", support.DEFAULT_WRONG_SOLUTIONS_IN_A_ROW, auto));
            //support.log(Integer.toString(support.loadIntFromProperties("pref_WrongSimulationsUntilBreak", getPref_HC_WrongSimulationsUntilBreak(), auto)));
            support.log("Loaded pref_WrongSimulationsUntilBreak is " + getPref_HC_WrongSimulationsUntilBreak(), typeOfLogLevel.INFO);

            this.setPref_WrongSimulationsPerDirection(support.loadIntFromProperties("pref_WrongSimulationsPerDirection", support.DEFAULT_WRONG_SOLUTION_PER_DIRECTION, auto));
            support.log("Loaded pref_WrongSimulationsPerDirection is " + getPref_HC_WrongSimulationsPerDirection(), typeOfLogLevel.INFO);

            this.setPref_SizeOfNeighborhood(support.loadIntFromProperties("pref_SizeOfNeighborhood", support.DEFAULT_SIZE_OF_NEIGHBORHOOD, auto));
            support.log("Loaded Size of Neighborhood is " + getPref_HC_SizeOfNeighborhood(), typeOfLogLevel.INFO);

            this.setPref_StartValue(typeOfStartValueEnum.valueOf(auto.getProperty("pref_StartValue", support.DEFAULT_TYPE_OF_STARTVALUE.toString())));
            support.log("Loaded StartValue is " + getPref_StartValue(), typeOfLogLevel.INFO);

            this.setPref_NeighborhoodType(typeOfNeighborhoodEnum.valueOf(auto.getProperty("pref_NeighborhoodType", support.DEFAULT_TYPE_OF_NEIGHBORHOOD.toString())));
            support.log("Loaded Neighborhoodtype is " + getPref_HC_NeighborhoodType(), typeOfLogLevel.INFO);

            //Loading parameters for Simulated Annealing (first phase)
            this.setPref_Cooling(typeOfAnnealing.valueOf(auto.getProperty("pref_Cooling", support.DEFAULT_TYPE_OF_ANNEALING.toString())), 0);
            support.log("Loaded Annealing method is " + getPref_SA_Cooling(0), typeOfLogLevel.INFO);

            this.setPref_TRatioScale(support.loadDoubleFromProperties("pref_TRatioScale", support.DEFAULT_T_RATIO_SCALE, auto), 0);
            support.log("Loaded TRatioScale is " + getPref_TRatioScale(0), typeOfLogLevel.INFO);

            this.setPref_TAnnealScale(support.loadDoubleFromProperties("pref_TAnnealScale", support.DEFAULT_T_ANNEAL_SCALE, auto), 0);
            support.log("Loaded TAnnealScale is " + getPref_TAnnealScale(0), typeOfLogLevel.INFO);

            this.setPref_MaxTempParameter(support.loadDoubleFromProperties("pref_MaxTempParameter", support.DEFAULT_MAXTEMP_PARAMETER, auto), 0);
            support.log("Loaded MaxTempParameter is " + getPref_MaxTempParameter(0), typeOfLogLevel.INFO);

            this.setPref_MaxTempCost(support.loadDoubleFromProperties("pref_MaxTempCost", support.DEFAULT_MAXTEMP_COST, auto), 0);
            support.log("Loaded MaxTempCost is " + getPref_MaxTempCost(0), typeOfLogLevel.INFO);

            this.setPref_Epsilon(support.loadDoubleFromProperties("pref_Epsilon", support.DEFAULT_EPSILON, auto), 0);
            support.log("Loaded Epsilon is " + getPref_Epsilon(0), typeOfLogLevel.INFO);

            this.setPref_CalculationOfNextParameterset(typeOfAnnealingParameterCalculation.valueOf(auto.getProperty("pref_CalculationOfNextParameterset", support.DEFAULT_CALC_NEXT_PARAMETER.toString())), 0);
            support.log("Loaded Calculation of next Parameterset is " + this.getPref_SA_CalculationOfNextParameterset(0).toString(), typeOfLogLevel.INFO);

            //Loading parameters for Simulated Annealing (second phase)
            this.setPref_Cooling(typeOfAnnealing.valueOf(auto.getProperty("pref_Cooling1", support.DEFAULT_TYPE_OF_ANNEALING.toString())), 1);
            support.log("*2 Loaded Annealing method is " + getPref_SA_Cooling(1), typeOfLogLevel.INFO);

            this.setPref_TRatioScale(support.loadDoubleFromProperties("pref_TRatioScale1", support.DEFAULT_T_RATIO_SCALE, auto), 1);
            support.log("*2 Loaded TRatioScale is " + getPref_TRatioScale(1), typeOfLogLevel.INFO);

            this.setPref_TAnnealScale(support.loadDoubleFromProperties("pref_TAnnealScale1", support.DEFAULT_T_ANNEAL_SCALE, auto), 1);
            support.log("*2 Loaded TAnnealScale is " + getPref_TAnnealScale(1), typeOfLogLevel.INFO);

            this.setPref_MaxTempParameter(support.loadDoubleFromProperties("pref_MaxTempParameter1", support.DEFAULT_MAXTEMP_PARAMETER, auto), 1);
            support.log("*2 Loaded MaxTempParameter is " + getPref_MaxTempParameter(1), typeOfLogLevel.INFO);

            this.setPref_MaxTempCost(support.loadDoubleFromProperties("pref_MaxTempCost1", support.DEFAULT_MAXTEMP_COST, auto), 1);
            support.log("*2 Loaded MaxTempCost is " + getPref_MaxTempCost(1), typeOfLogLevel.INFO);

            this.setPref_Epsilon(support.loadDoubleFromProperties("pref_Epsilon1", support.DEFAULT_EPSILON, auto), 1);
            support.log("*2 Loaded Epsilon is " + getPref_Epsilon(1), typeOfLogLevel.INFO);

            this.setPref_CalculationOfNextParameterset(typeOfAnnealingParameterCalculation.valueOf(auto.getProperty("pref_CalculationOfNextParameterset1", support.DEFAULT_CALC_NEXT_PARAMETER.toString())), 1);
            support.log("*2 Loaded Calculation of next Parameterset is " + this.getPref_SA_CalculationOfNextParameterset(1).toString(), typeOfLogLevel.INFO);

            this.setPref_LogFileAddon(auto.getProperty("pref_LogFileAddon", ""));
            support.log("Loaded Optimizer_Logfile-Addon is " + this.jTextFieldLogFileAddon.getText(), typeOfLogLevel.INFO);

            this.setPref_NumberOfPhases(support.loadIntFromProperties("pref_NumberOfPhases", support.DEFAULT_NumberOfPhases, auto));
            support.log("Loaded pref_NumberOfPhases is " + this.getPref_MP_NumberOfPhases(), typeOfLogLevel.INFO);
            this.setPref_ConfidenceIntervallStart(support.loadIntFromProperties("pref_ConfidenceIntervallStart", support.DEFAULT_ConfidenceIntervallStart, auto));
            support.log("Loaded pref_ConfidenceIntervallStart is " + this.getPref_MP_ConfidenceIntervallStart(), typeOfLogLevel.INFO);
            this.setPref_ConfidenceIntervallEnd(support.loadIntFromProperties("pref_ConfidenceIntervallEnd", support.DEFAULT_ConfidenceIntervallEnd, auto));
            support.log("Loaded pref_ConfidenceIntervallEnd is " + this.getPref_MP_ConfidenceIntervallEnd(), typeOfLogLevel.INFO);
            this.setPref_MaxRelErrorStart(support.loadIntFromProperties("pref_MaxRelErrorStart", support.DEFAULT_MaxRelErrorStart, auto));
            support.log("Loaded pref_MaxRelErrorStart is " + this.getPref_MP_MaxRelErrorStart(), typeOfLogLevel.INFO);
            this.setPref_MaxRelErrorEnd(support.loadIntFromProperties("pref_MaxRelErrorEnd", support.DEFAULT_MaxRelErrorEnd, auto));
            support.log("Loaded pref_MaxRelErrorEnd is " + this.getPref_MP_MaxRelErrorEnd(), typeOfLogLevel.INFO);
            this.setPref_InternalParameterStart(support.loadIntFromProperties("pref_InternalParameterStart", support.DEFAULT_InternalParameterStart, auto));
            support.log("Loaded pref_InternalParameterStart is " + this.getPref_MP_InternalParameterStart(), typeOfLogLevel.INFO);
            this.setPref_InternalParameterEnd(support.loadIntFromProperties("pref_InternalParameterEnd", support.DEFAULT_InternalParameterEnd, auto));
            support.log("Loaded pref_InternalParameterEnd is " + this.getPref_MP_InternalParameterEnd(), typeOfLogLevel.INFO);

            this.setPref_typeOfUsedMultiPhaseOptimization(typeOfOptimization.valueOf(auto.getProperty("pref_typeOfUsedMultiPhaseOptimization", support.DEFAULT_typeOfUsedMultiPhaseOptimization.toString())));
            support.log("Loaded pref_typeOfUsedMultiPhaseOptimization is " + this.getPref_MP_typeOfUsedMultiPhaseOptimization().toString(), typeOfLogLevel.INFO);
            this.setPref_KeepDesignSpaceAndResolution(Boolean.valueOf(auto.getProperty("pref_KeepDesignSpaceAndResolution", Boolean.toString(support.DEFAULT_KeepDesignSpaceAndResolution))));
            support.log("Loaded pref_KeepDesignSpaceAndResolution is " + this.getPref_MP_KeepDesignSpaceAndResolution(), typeOfLogLevel.INFO);

            //load settings for Genetic Optimization
            this.setPref_GeneticPopulationSize(support.loadIntFromProperties("pref_GeneticPopulationSize", support.DEFAULT_GENETIC_POPULATION_SIZE, auto));
            support.log("Loaded pref_GeneticPopulationSize is " + this.getPref_Genetic_PopulationSize(), typeOfLogLevel.INFO);
            this.setPref_GeneticMutationChance(support.loadDoubleFromProperties("pref_GeneticMutationChance", support.DEFAULT_GENETIC_MUTATION_CHANCE, auto));
            support.log("Loaded pref_GeneticMutationChance is " + this.getPref_Genetic_MutationChance(), typeOfLogLevel.INFO);
            this.setPref_GeneticMutateTopSolution(Boolean.valueOf(auto.getProperty("pref_GeneticMutateTopSolution", Boolean.toString(support.DEFAULT_GENETIC_MUTATE_TOP_SOLUTION))));
            support.log("Loaded pref_GeneticMutateTopSolution is " + this.getPref_Genetic_MutateTopSolution(), typeOfLogLevel.INFO);
            this.setPref_GeneticNumberOfCrossings(support.loadIntFromProperties("pref_GeneticNumberOfCrossings", support.DEFAULT_GENETIC_NUMBEROFCROSSINGS, auto));

            this.setPref_GeneticMaximumOptirunsWithoutSolution(support.loadIntFromProperties("pref_GeneticMaxOptiRunsWithoutSolution", support.DEFAULT_GENETIC_MAXWRONGOPTIRUNS, auto));
            support.log("Loaded pref_GeneticMaxOptiRunsWithoutSolution is " + this.getPref_Genetic_MaximumOptirunsWithoutSolution(), typeOfLogLevel.INFO);
            this.setPref_GeneticTypeOfCrossover(typeOfGeneticCrossover.valueOf(auto.getProperty("pref_GeneticTypeOfCrossover", support.DEFAULT_GENETIC_CROSSOVER.toString())));
            support.log("Loaded pref_GeneticTypeOfCrossover is " + this.getPref_Genetic_TypeOfCrossover().toString(), typeOfLogLevel.INFO);

            //load settings for CSS Optimization
            this.setPref_CSS_PopulationSize(support.loadIntFromProperties("pref_CSS_PopulationSize", support.DEFAULT_CSS_POPULATION_SIZE, auto));
            support.log("Loaded pref_CSS_PopulationSize is " + this.getPref_CSS_PopulationSize(), typeOfLogLevel.INFO);
            this.setPref_CSS_MaxAttraction(support.loadDoubleFromProperties("pref_CSS_MaxAttraction", support.DEFAULT_CSS_MAX_ATTRACTION, auto));
            support.log("Loaded pref_CSS_MaxAttraction is " + this.getPref_CSS_MaxAttraction(), typeOfLogLevel.INFO);

            //load settings for ABC Optimization
            this.setPref_ABC_NumEmployedBees(support.loadIntFromProperties("pref_ABC_NumEmployedBees", support.DEFAULT_ABC_NumEmployedBees, auto));
            support.log("Loaded pref_ABC_NumEmployedBees is " + this.getPref_ABC_NumEmployedBees(), typeOfLogLevel.INFO);
            this.setPref_ABC_NumOnlookerBees(support.loadIntFromProperties("pref_ABC_NumOnlookerBees", support.DEFAULT_ABC_NumOnlookerBees, auto));
            support.log("Loaded pref_ABC_NumOnlookerBees is " + this.getPref_ABC_NumOnlookerBees(), typeOfLogLevel.INFO);
            this.setPref_ABC_NumScoutBees(support.loadIntFromProperties("pref_ABC_NumScoutBees", support.DEFAULT_ABC_NumScoutBees, auto));
            support.log("Loaded pref_ABC_NumScoutBees is " + this.getPref_ABC_NumScoutBees(), typeOfLogLevel.INFO);
            this.setPref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement(support.loadIntFromProperties(
                    "pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement", support.DEFAULT_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement, auto));
            support.log("Loaded pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement is " + this.getPref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement(), typeOfLogLevel.INFO);

            //load settings for MVMO Optimization
            this.setPref_MVMO_StartingPop(support.loadIntFromProperties("pref_MVMO_StartingPop", support.DEFAULT_MVMO_STARTING_POPULATION, auto));
            support.log("Loaded MVMO_StartingPop is " + this.getPref_MVMO_StartingPop(), typeOfLogLevel.INFO);
            this.setPref_MVMO_MaxPop(support.loadIntFromProperties("pref_MVMO_MaxPop", support.DEFAULT_MVMO_MAX_POPULATION, auto));
            support.log("Loaded MVMO_MaxPop is " + this.getPref_MVMO_MaxPop(), typeOfLogLevel.INFO);

            this.setPref_MVMO_AsymmetryFactor(support.loadDoubleFromProperties("MVMO_AsymmetryFactor", support.DEFAULT_MVMO_ASYMMETRY_FACTOR, auto));
            support.log("Loaded MVMO_AsymmetryFactor is " + this.getPref_MVMO_AsymmetryFactor(), typeOfLogLevel.INFO);
            this.setPref_MVMO_ScalingFactor(support.loadDoubleFromProperties("MVMO_ScalingFactor", support.DEFAULT_MVMO_SCALING_FACTOR, auto));
            support.log("Loaded MVMO_ScalingFactor is " + this.getPref_MVMO_ScalingFactor(), typeOfLogLevel.INFO);
            this.setPref_MVMO_mutationSelection(typeOfMVMOMutationSelection.valueOf(auto.getProperty("MVMO_mutationSelection", support.DEFAULT_MVMO_MUTATION_STRATEGY.toString())));
            support.log("Loaded MVMO_mutationSelection is " + this.getPref_MVMO_mutationSelection(), typeOfLogLevel.INFO);
            this.setPref_MVMO_ParentSelection(typeOfMVMOParentSelection.valueOf(auto.getProperty("MVMO_parentSelection", support.DEFAULT_MVMO_PARENT_SELECTION.toString())));
            support.log("Loaded MVMO_parentSelection is " + this.getPref_MVMO_parentSelection(), typeOfLogLevel.INFO);
            this.setPref_MVMO_sd(support.loadDoubleFromProperties("MVMO_sd", support.DEFAULT_MVMO_SD, auto));
            support.log("Loaded MVMO_sd is " + this.getPref_MVMO_sd(), typeOfLogLevel.INFO);

        } catch (IOException e) {
            // Exception bearbeiten
            support.log("Error while loading Optimizer-Properties.", typeOfLogLevel.ERROR);
        }

    }

    /**
     * Save Preferences to defined file
     */
    public void savePreferences() {
        support.log("Saving Properties #" + this.getNumberOfActualOptimizationAnalysis().toString() + " of Optimization", typeOfLogLevel.INFO);
        String addonStringForFileName = this.getNumberOfActualOptimizationAnalysis().toString();
        if (this.getNumberOfActualOptimizationAnalysis() <= 0) {
            addonStringForFileName = "";
        }
        String filename = support.NAME_OF_OPTIMIZER_PREFFERENCES_FILE + addonStringForFileName;

        try {

            //Setting Parameters of HillClimbing
            auto.setProperty("pref_WrongSimulationsUntilBreak", Integer.toString(getPref_HC_WrongSimulationsUntilBreak()));
            auto.setProperty("pref_WrongSimulationsPerDirection", Integer.toString(getPref_HC_WrongSimulationsPerDirection()));
            auto.setProperty("pref_SizeOfNeighborhood", Integer.toString(getPref_HC_SizeOfNeighborhood()));

            auto.setProperty("pref_StartValue", getPref_StartValue().toString());
            auto.setProperty("pref_NeighborhoodType", getPref_HC_NeighborhoodType().toString());

            //Setting Parameters of Simulated Annealing
            auto.setProperty("pref_Cooling", getPref_SA_Cooling(0).toString());
            auto.setProperty("pref_TRatioScale", support.getString(this.getPref_TRatioScale(0)));
            auto.setProperty("pref_TAnnealScale", support.getString(this.getPref_TAnnealScale(0)));
            auto.setProperty("pref_MaxTempParameter", support.getString(this.getPref_MaxTempParameter(0)));
            auto.setProperty("pref_MaxTempCost", support.getString(this.getPref_MaxTempCost(0)));
            auto.setProperty("pref_Epsilon", Double.toString(getPref_Epsilon(0)));
            auto.setProperty("pref_CalculationOfNextParameterset", this.getPref_SA_CalculationOfNextParameterset(0).toString());

            //Setting Parameters of Simulated Annealing - Second phase
            auto.setProperty("pref_Cooling1", getPref_SA_Cooling(1).toString());
            auto.setProperty("pref_TRatioScale1", support.getString(this.getPref_TRatioScale(1)));
            auto.setProperty("pref_TAnnealScale1", support.getString(this.getPref_TAnnealScale(1)));
            auto.setProperty("pref_MaxTempParameter1", support.getString(this.getPref_MaxTempParameter(1)));
            auto.setProperty("pref_MaxTempCost1", support.getString(this.getPref_MaxTempCost(1)));
            auto.setProperty("pref_Epsilon1", Double.toString(getPref_Epsilon(1)));
            auto.setProperty("pref_CalculationOfNextParameterset1", this.getPref_SA_CalculationOfNextParameterset(1).toString());

            //Setting Parameters of MultiPhase Optimization
            auto.setProperty("pref_NumberOfPhases", Integer.toString(this.getPref_MP_NumberOfPhases()));
            auto.setProperty("pref_ConfidenceIntervallStart", Integer.toString(this.getPref_MP_ConfidenceIntervallStart()));
            auto.setProperty("pref_ConfidenceIntervallEnd", Integer.toString(this.getPref_MP_ConfidenceIntervallEnd()));
            auto.setProperty("pref_MaxRelErrorStart", Integer.toString(this.getPref_MP_MaxRelErrorStart()));
            auto.setProperty("pref_MaxRelErrorEnd", Integer.toString(this.getPref_MP_MaxRelErrorEnd()));
            auto.setProperty("pref_InternalParameterStart", Integer.toString(this.getPref_MP_InternalParameterStart()));
            auto.setProperty("pref_InternalParameterEnd", Integer.toString(this.getPref_MP_InternalParameterEnd()));
            auto.setProperty("pref_typeOfUsedMultiPhaseOptimization", this.getPref_MP_typeOfUsedMultiPhaseOptimization().toString());
            auto.setProperty("pref_KeepDesignSpaceAndResolution", Boolean.toString(this.getPref_MP_KeepDesignSpaceAndResolution()));

            auto.setProperty("pref_LogFileAddon", this.jTextFieldLogFileAddon.getText());

            //setting parameters for genetic optimization
            auto.setProperty("pref_GeneticPopulationSize", Integer.toString(this.getPref_Genetic_PopulationSize()));
            auto.setProperty("pref_GeneticMutationChance", Double.toString(this.getPref_Genetic_MutationChance()));
            auto.setProperty("pref_GeneticMutateTopSolution", Boolean.toString(this.getPref_Genetic_MutateTopSolution()));
            auto.setProperty("pref_GeneticMaxOptiRunsWithoutSolution", Integer.toString(this.getPref_Genetic_MaximumOptirunsWithoutSolution()));
            auto.setProperty("pref_GeneticTypeOfCrossover", this.getPref_Genetic_TypeOfCrossover().toString());
            auto.setProperty("pref_GeneticNumberOfCrossings", Integer.toString(this.getPref_Genetic_NumberOfCrossings()));

            //setting parameters for CSS optimization
            auto.setProperty("pref_CSS_PopulationSize", Integer.toString(this.getPref_CSS_PopulationSize()));
            auto.setProperty("pref_CSS_MaxAttraction", Double.toString(this.getPref_CSS_MaxAttraction()));

            //setting parameters for ABC optimization
            auto.setProperty("pref_ABC_NumEmployedBees", Integer.toString(this.getPref_ABC_NumEmployedBees()));
            auto.setProperty("pref_ABC_NumOnlookerBees", Integer.toString(this.getPref_ABC_NumOnlookerBees()));
            auto.setProperty("pref_ABC_NumScoutBees", Integer.toString(this.getPref_ABC_NumScoutBees()));
            auto.setProperty("pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement", Integer.toString(this.getPref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement()));

            //save settings for MVMO Optimization
            auto.setProperty("pref_MVMO_StartingPop", Integer.toString(this.getPref_MVMO_StartingPop()));
            auto.setProperty("pref_MVMO_MaxPop", Integer.toString(this.getPref_MVMO_MaxPop()));
            auto.setProperty("MVMO_AsymmetryFactor", Double.toString(this.getPref_MVMO_AsymmetryFactor()));
            auto.setProperty("MVMO_ScalingFactor", Double.toString(this.getPref_MVMO_ScalingFactor()));
            auto.setProperty("MVMO_mutationSelection", this.getPref_MVMO_mutationSelection().toString());
            auto.setProperty("MVMO_parentSelection", this.getPref_MVMO_parentSelection().toString());
            auto.setProperty("MVMO_sd", Double.toString(this.getPref_MVMO_sd()));

            File parserprops = new File(filename);
            auto.store(new FileOutputStream(parserprops), "ExperimentGenerator-Properties");
        } catch (IOException e) {
            support.log("Problem Saving the properties.", typeOfLogLevel.ERROR);
        }

    }

    /**
     * @return the pref_WrongSimulationsUntilBreak
     */
    public int getPref_HC_WrongSimulationsUntilBreak() {
        pref_WrongSimulationsUntilBreak = (Integer) this.jSpinnerWrongSolutionsUntilBreak.getValue();
        return pref_WrongSimulationsUntilBreak;
    }

    /**
     * @param pref_WrongSimulationsUntilBreak the
     * pref_WrongSimulationsUntilBreak to set
     */
    public void setPref_WrongSimulationsUntilBreak(int pref_WrongSimulationsUntilBreak) {
        this.pref_WrongSimulationsUntilBreak = pref_WrongSimulationsUntilBreak;
        this.jSpinnerWrongSolutionsUntilBreak.setValue(pref_WrongSimulationsUntilBreak);
    }

    /**
     * @return the pref_WrongSimulationsPerDirection
     */
    public int getPref_HC_WrongSimulationsPerDirection() {
        pref_WrongSimulationsPerDirection = (Integer) this.jSpinnerWrongSolutionsPerDirectionUntilBreak.getValue();
        return pref_WrongSimulationsPerDirection;
    }

    /**
     * @param pref_WrongSimulationsPerDirection the
     * pref_WrongSimulationsPerDirection to set
     */
    public void setPref_WrongSimulationsPerDirection(int pref_WrongSimulationsPerDirection) {
        this.pref_WrongSimulationsPerDirection = pref_WrongSimulationsPerDirection;
        this.jSpinnerWrongSolutionsPerDirectionUntilBreak.setValue(pref_WrongSimulationsPerDirection);
    }

    /**
     * @return the pref_LogFileAddon
     */
    public String getPref_LogFileAddon() {
        this.pref_LogFileAddon = this.jTextFieldLogFileAddon.getText();
        String addonString = pref_LogFileAddon;

        if (this.jCheckBoxAddPrefsToLogfilename.isSelected()) {

            switch (support.getChosenOptimizerType()) {
                case HillClimbing:
                    addonString += "_WSIMPERDIR_" + this.getPref_HC_WrongSimulationsPerDirection() + "_WSIM_" + this.getPref_HC_WrongSimulationsUntilBreak() + "_StartAt_" + this.jComboBoxTypeOfStartValue.getSelectedItem();
                    addonString += "_TypOfNeighborhood_" + this.jComboBoxTypeOfNeighborhood.getSelectedItem();
                    addonString += "_SizeOfNeighborhood_" + this.getPref_HC_SizeOfNeighborhood();
                    break;
                case SimAnnealing:
                    addonString += "_StartAt_" + this.jComboBoxTypeOfStartValue.getSelectedItem();
                    addonString += "_TAnnealScale_" + this.getPref_TAnnealScale(0);
                    addonString += "_TRatioScale_" + this.getPref_TRatioScale(0);
                    addonString += "_Epsilon_" + this.getPref_Epsilon(0);
                    addonString += "_Cooling_" + this.getPref_SA_Cooling(0);
                    addonString += "_MaxTempPara_" + this.getPref_MaxTempParameter(0);
                    addonString += "_MaxTempCost_" + this.getPref_MaxTempCost(0);
                    break;
                case MultiPhase:
                    addonString += "_ChosenAlg_" + this.getPref_MP_typeOfUsedMultiPhaseOptimization();
                    addonString += "_NumberOfPhases_" + this.getPref_MP_NumberOfPhases();
                    break;

                case TwoPhase:
                    addonString += "_StartAt_" + this.jComboBoxTypeOfStartValue.getSelectedItem();
                    for (int i = 0; i <= 1; i++) {
                        addonString += "_Phase_" + i;
                        addonString += "_TAS_" + this.getPref_TAnnealScale(i);
                        addonString += "_TRS_" + this.getPref_TRatioScale(i);
                        addonString += "_E_" + this.getPref_Epsilon(i);
                        addonString += "_C_" + this.getPref_SA_Cooling(i);
                        addonString += "_MTP_" + this.getPref_MaxTempParameter(i);
                        addonString += "_MTC_" + this.getPref_MaxTempCost(i);
                    }
                    break;

                case Genetic:
                    addonString += "_Popsize_" + this.getPref_Genetic_PopulationSize();
                    addonString += "_MutationChance_" + this.getPref_Genetic_MutationChance();
                    addonString += "_MutateTop_" + this.getPref_Genetic_MutateTopSolution();
                    addonString += "_MaxWrongOptiruns_" + this.getPref_Genetic_MaximumOptirunsWithoutSolution();
                    addonString += "_Crossover_" + this.getPref_Genetic_TypeOfCrossover();
                    break;
                case ChargedSystemSearch:
                    //TODO Add Infos to this Algorithm here!
                    break;
                case ABC:
                    //TODO Add Infos to this Algorithm here!
                    break;
                case MVMO:
            }

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
    public typeOfStartValueEnum getPref_StartValue() {
        pref_StartValue = (typeOfStartValueEnum) this.jComboBoxTypeOfStartValue.getSelectedItem();
        return pref_StartValue;
    }

    /**
     * @param pref_StartValue the pref_StartValue to set
     */
    public void setPref_StartValue(typeOfStartValueEnum pref_StartValue) {
        this.jComboBoxTypeOfStartValue.setSelectedItem(pref_StartValue);
        this.pref_StartValue = pref_StartValue;
    }

    /**
     * Returns the type of neighborhood for Hill Climbing
     *
     * @return the pref_NeighborhoodType
     */
    public typeOfNeighborhoodEnum getPref_HC_NeighborhoodType() {
        pref_NeighborhoodType = (typeOfNeighborhoodEnum) this.jComboBoxTypeOfNeighborhood.getSelectedItem();
        return pref_NeighborhoodType;
    }

    /**
     * Sets the type of neighborhood for Hill Climbing
     *
     * @param pref_NeighborhoodType the pref_NeighborhoodType to set
     */
    public void setPref_NeighborhoodType(typeOfNeighborhoodEnum pref_NeighborhoodType) {
        this.jComboBoxTypeOfNeighborhood.setSelectedItem(pref_NeighborhoodType);
        this.pref_NeighborhoodType = pref_NeighborhoodType;
    }

    /**
     * Returns the size of neighborhood for Hill Climbing
     *
     * @return the pref_SizeOfNeighborhood
     */
    public int getPref_HC_SizeOfNeighborhood() {
        this.pref_SizeOfNeighborhood = (Integer) this.jSpinnerSizeOfNeighborhoodInPercent.getValue();
        return pref_SizeOfNeighborhood;
    }

    /**
     * Sets the size of neighborhood for Hill Climbing
     *
     * @param pref_SizeOfNeighborhood the pref_SizeOfNeighborhood to set
     */
    public void setPref_SizeOfNeighborhood(int pref_SizeOfNeighborhood) {
        this.jSpinnerSizeOfNeighborhoodInPercent.setValue(pref_SizeOfNeighborhood);
        this.pref_SizeOfNeighborhood = pref_SizeOfNeighborhood;
    }

    /**
     * Returns the chosen cooling type for Simulated Annealing
     *
     * @param phase Number of Opti-Phase, can be 0..1
     * @return the pref_Cooling, the Type of Annealing Phase
     */
    public typeOfAnnealing getPref_SA_Cooling(int phase) {
        switch (phase) {
            case 0:
                return (typeOfAnnealing) this.jComboBoxCoolingMethod.getSelectedItem();
            case 1:
                return (typeOfAnnealing) this.jComboBoxCoolingMethod1.getSelectedItem();
            default:
                return (typeOfAnnealing) this.jComboBoxCoolingMethod.getSelectedItem();
        }
    }

    /**
     * Sets the chosen cooling type for Simulated Annealing
     *
     * @param pref_Cooling the pref_Cooling to set
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public void setPref_Cooling(typeOfAnnealing pref_Cooling, int phase) {
        switch (phase) {
            case 0:
                this.jComboBoxCoolingMethod.setSelectedItem(pref_Cooling);
                break;
            case 1:
                this.jComboBoxCoolingMethod1.setSelectedItem(pref_Cooling);
                break;
        }
    }

    /**
     * Gets the TRatioScale for Simulated Annealing
     *
     * @param phase Number of Opti-Phase, can be 0..1
     * @return the pref_TRatioScale Phase 2 in 2-Phase Simulated
     *
     */
    public double getPref_TRatioScale(int phase) {
        switch (phase) {
            case 0:
                return (Double) this.jSpinnerTRatioScale.getValue();
            case 1:
                return (Double) this.jSpinnerTRatioScale1.getValue();
            default:
                return (Double) this.jSpinnerTRatioScale.getValue();
        }
    }

    /**
     * Sets the TScaleRatio for standard SA
     *
     * @param pref_TRatioScale the TRatioScale to set
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public void setPref_TRatioScale(double pref_TRatioScale, int phase) {
        switch (phase) {
            case 0:
                this.jSpinnerTRatioScale.setValue(pref_TRatioScale);
                break;
            case 1:
                this.jSpinnerTRatioScale1.setValue(pref_TRatioScale);
                break;
            default:
                this.jSpinnerTRatioScale.setValue(pref_TRatioScale);
                break;
        }
    }

    /**
     * Gets TAnnealScale for standard SA
     *
     * @return the pref_TAnnealScale
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public double getPref_TAnnealScale(int phase) {
        switch (phase) {
            case 0:
                return (Double) (this.jSpinnerTAnnealScale.getValue());
            case 1:
                return (Double) (this.jSpinnerTAnnealScale1.getValue());
            default:
                return (Double) (this.jSpinnerTAnnealScale.getValue());
        }
    }

    /**
     * Sets TAnnealScale for standard SA
     *
     * @param pref_TAnnealScale the pref_TAnnealScale to set Simulated
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public void setPref_TAnnealScale(double pref_TAnnealScale, int phase) {
        switch (phase) {
            case 0:
                this.jSpinnerTAnnealScale.setValue(pref_TAnnealScale);
                break;
            case 1:
                this.jSpinnerTAnnealScale1.setValue(pref_TAnnealScale);
                break;
            default:
                this.jSpinnerTAnnealScale.setValue(pref_TAnnealScale);
                break;
        }
    }

    /**
     * Gets Max Temperature for standard Simulated Annealing
     *
     * @return the pref_MaxTempParameter
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public double getPref_MaxTempParameter(int phase) {
        switch (phase) {
            case 0:
                return (Double) this.jSpinnerMaxTemperatureParameters.getValue();
            case 1:
                return (Double) this.jSpinnerMaxTemperatureParameters1.getValue();
            default:
                return (Double) this.jSpinnerMaxTemperatureParameters.getValue();
        }
    }

    /**
     * @param pref_MaxTempParameter the pref_MaxTempParameter to set
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public void setPref_MaxTempParameter(double pref_MaxTempParameter, int phase) {
        switch (phase) {
            case 0:
                this.jSpinnerMaxTemperatureParameters.setValue(pref_MaxTempParameter);
                break;
            case 1:
                this.jSpinnerMaxTemperatureParameters1.setValue(pref_MaxTempParameter);
                break;
            default:
                this.jSpinnerMaxTemperatureParameters.setValue(pref_MaxTempParameter);
                break;
        }
    }

    /**
     * @return the pref_MaxTempCost
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public double getPref_MaxTempCost(int phase) {
        switch (phase) {
            case 0:
                return (Double) jSpinnerMaxTemperatureCost.getValue();
            case 1:
                return (Double) jSpinnerMaxTemperatureCost1.getValue();
            default:
                return (Double) jSpinnerMaxTemperatureCost.getValue();
        }
    }

    /**
     * @param pref_MaxTempCost the pref_MaxTempCost to set
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public void setPref_MaxTempCost(double pref_MaxTempCost, int phase) {
        switch (phase) {
            case 0:
                this.jSpinnerMaxTemperatureCost.setValue(pref_MaxTempCost);
                break;
            case 1:
                this.jSpinnerMaxTemperatureCost1.setValue(pref_MaxTempCost);
                break;
            default:
                this.jSpinnerMaxTemperatureCost.setValue(pref_MaxTempCost);
                break;
        }
    }

    /**
     * Get the epsilon value (break condition) for Simulated Annealing
     *
     * @return the pref_Epsilon
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public double getPref_Epsilon(int phase) {
        switch (phase) {
            case 0:
                return (Double) jSpinnerEpsilon.getValue();
            case 1:
                return (Double) jSpinnerEpsilon1.getValue();
            default:
                return (Double) jSpinnerEpsilon.getValue();
        }
    }

    /**
     * Set the epsilon value (break condition) for Simulated Annealing
     *
     * @param pref_Epsilon the pref_Epsilon to set
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public void setPref_Epsilon(double pref_Epsilon, int phase) {
        javax.swing.JSpinner tmpSpinner;
        switch (phase) {
            case 1:
                tmpSpinner = jSpinnerEpsilon1;
                break;
            default:
            case 0:
                tmpSpinner = jSpinnerEpsilon;
                break;
        }

        javax.swing.JSpinner.DefaultEditor editor = (javax.swing.JSpinner.DefaultEditor) tmpSpinner.getEditor();
        if (pref_Epsilon < epsilon_min) {
            tmpSpinner.setToolTipText("Calculated Epsilon is to small!");
            tmpSpinner.setBackground(Color.RED);
            editor.getTextField().setBackground(Color.red);
            pref_Epsilon = epsilon_min;
        } else if (pref_Epsilon > epsilon_max) {
            tmpSpinner.setToolTipText("Calculated Epsilon is to big!");
            editor.getTextField().setBackground(Color.red);
            pref_Epsilon = epsilon_max;
        } else {
            tmpSpinner.setToolTipText("");
            editor.getTextField().setBackground(Color.white);
        }
        tmpSpinner.setValue(support.round(pref_Epsilon, 3));
    }

    /**
     * Returns how the next parameterset is calculated when Simulated Annealing
     * is used
     *
     * @return the pref_CalculationOfNextParameterset
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public typeOfAnnealingParameterCalculation getPref_SA_CalculationOfNextParameterset(int phase) {
        switch (phase) {
            case 0:
                return (typeOfAnnealingParameterCalculation) this.jComboBoxCalculationOfNextParameterset.getSelectedItem();
            case 1:
                return (typeOfAnnealingParameterCalculation) this.jComboBoxCalculationOfNextParameterset1.getSelectedItem();
            default:
                return (typeOfAnnealingParameterCalculation) this.jComboBoxCalculationOfNextParameterset.getSelectedItem();
        }
    }

    /**
     * Set how the next parameterset is calculated when Simulated Annealing is
     * used
     *
     * @param pref_CalculationOfNextParameterset the
     * pref_CalculationOfNextParameterset to set
     * @param phase Number of Opti-Phase, can be 0..1
     */
    public void setPref_CalculationOfNextParameterset(typeOfAnnealingParameterCalculation pref_CalculationOfNextParameterset, int phase) {
        switch (phase) {
            case 0:
                this.jComboBoxCalculationOfNextParameterset.setSelectedItem(pref_CalculationOfNextParameterset);
                break;
            case 1:
                this.jComboBoxCalculationOfNextParameterset1.setSelectedItem(pref_CalculationOfNextParameterset);
                break;
            default:
                this.jComboBoxCalculationOfNextParameterset.setSelectedItem(pref_CalculationOfNextParameterset);
                break;
        }
    }

    /**
     * @return the pref_NumberOfPhases
     */
    public int getPref_MP_NumberOfPhases() {
        this.pref_NumberOfPhases = Integer.valueOf((String) this.jComboBoxNumberOfPhases.getSelectedItem());
        return pref_NumberOfPhases;
    }

    /**
     * @param pref_NumberOfPhases the pref_NumberOfPhases to set
     */
    public void setPref_NumberOfPhases(int pref_NumberOfPhases) {
        this.jComboBoxNumberOfPhases.setSelectedItem(Integer.toString(pref_NumberOfPhases));
        this.pref_NumberOfPhases = pref_NumberOfPhases;
    }

    /**
     * @return the pref_typeOfUsedMultiPhaseOptimization
     */
    public typeOfOptimization getPref_MP_typeOfUsedMultiPhaseOptimization() {
        this.pref_typeOfUsedMultiPhaseOptimization = (typeOfOptimization) this.jComboBoxOptimizationType.getSelectedItem();
        return pref_typeOfUsedMultiPhaseOptimization;
    }

    /**
     * @param pref_typeOfUsedMultiPhaseOptimization the
     * pref_typeOfUsedMultiPhaseOptimization to set
     */
    public void setPref_typeOfUsedMultiPhaseOptimization(typeOfOptimization pref_typeOfUsedMultiPhaseOptimization) {
        this.jComboBoxOptimizationType.setSelectedItem(pref_typeOfUsedMultiPhaseOptimization);
        this.pref_typeOfUsedMultiPhaseOptimization = pref_typeOfUsedMultiPhaseOptimization;
    }

    /**
     * @return the pref_ConfidenceIntervallStart
     */
    public int getPref_MP_ConfidenceIntervallStart() {
        this.pref_ConfidenceIntervallStart = (Integer) jSpinnerConfidenceIntervallStart.getValue();
        return pref_ConfidenceIntervallStart;
    }

    /**
     * @param pref_ConfidenceIntervallStart the pref_ConfidenceIntervallStart to
     * set
     */
    public void setPref_ConfidenceIntervallStart(int pref_ConfidenceIntervallStart) {
        this.jSpinnerConfidenceIntervallStart.setValue(pref_ConfidenceIntervallStart);
        this.pref_ConfidenceIntervallStart = pref_ConfidenceIntervallStart;
    }

    /**
     * @return the pref_ConfidenceIntervallEnd
     */
    public int getPref_MP_ConfidenceIntervallEnd() {
        this.pref_ConfidenceIntervallEnd = (Integer) jSpinnerConfidenceIntervallEnd.getValue();
        return pref_ConfidenceIntervallEnd;
    }

    /**
     * @param pref_ConfidenceIntervallEnd the pref_ConfidenceIntervallEnd to set
     */
    public void setPref_ConfidenceIntervallEnd(int pref_ConfidenceIntervallEnd) {
        this.jSpinnerConfidenceIntervallEnd.setValue(pref_ConfidenceIntervallEnd);
        this.pref_ConfidenceIntervallEnd = pref_ConfidenceIntervallEnd;
    }

    /**
     * @return the pref_MaxRelErrorStart
     */
    public int getPref_MP_MaxRelErrorStart() {
        this.pref_MaxRelErrorStart = (Integer) jSpinnerMaxRelErrorStart.getValue();
        return pref_MaxRelErrorStart;
    }

    /**
     * @param pref_MaxRelErrorStart the pref_MaxRelErrorStart to set
     */
    public void setPref_MaxRelErrorStart(int pref_MaxRelErrorStart) {
        this.jSpinnerMaxRelErrorStart.setValue(pref_MaxRelErrorStart);
        this.pref_MaxRelErrorStart = pref_MaxRelErrorStart;
    }

    /**
     * @return the pref_MaxRelErrorEnd
     */
    public int getPref_MP_MaxRelErrorEnd() {
        this.pref_MaxRelErrorEnd = (Integer) jSpinnerMaxRelErrorEnd.getValue();
        return pref_MaxRelErrorEnd;
    }

    /**
     * @param pref_MaxRelErrorEnd the pref_MaxRelErrorEnd to set
     */
    public void setPref_MaxRelErrorEnd(int pref_MaxRelErrorEnd) {
        jSpinnerMaxRelErrorEnd.setValue(pref_MaxRelErrorEnd);
        this.pref_MaxRelErrorEnd = pref_MaxRelErrorEnd;
    }

    /**
     * @return the pref_InternalParameterStart
     */
    public int getPref_MP_InternalParameterStart() {
        this.pref_InternalParameterStart = (Integer) jSpinnerInternalParameterStart.getValue();
        return pref_InternalParameterStart;
    }

    /**
     * @param pref_InternalParameterStart the pref_InternalParameterStart to set
     */
    public void setPref_InternalParameterStart(int pref_InternalParameterStart) {
        jSpinnerInternalParameterStart.setValue(pref_InternalParameterStart);
        this.pref_InternalParameterStart = pref_InternalParameterStart;
    }

    /**
     * @return the pref_InternalParameterEnd
     */
    public int getPref_MP_InternalParameterEnd() {
        this.pref_InternalParameterEnd = (Integer) jSpinnerInternalParameterEnd.getValue();
        return pref_InternalParameterEnd;
    }

    /**
     * @param pref_InternalParameterEnd the pref_InternalParameterEnd to set
     */
    public void setPref_InternalParameterEnd(int pref_InternalParameterEnd) {
        jSpinnerInternalParameterEnd.setValue(pref_InternalParameterEnd);
        this.pref_InternalParameterEnd = pref_InternalParameterEnd;
    }

    /**
     * @return the pref_KeepDesignSpaceAndResolution
     */
    public boolean getPref_MP_KeepDesignSpaceAndResolution() {
        this.pref_KeepDesignSpaceAndResolution = jCheckBoxKeepDesignspaceAndResolution.isSelected();
        return pref_KeepDesignSpaceAndResolution;
    }

    /**
     * @param pref_KeepDesignSpaceAndResolution the
     * pref_KeepDesignSpaceAndResolution to set
     */
    public void setPref_KeepDesignSpaceAndResolution(boolean pref_KeepDesignSpaceAndResolution) {
        jCheckBoxKeepDesignspaceAndResolution.setSelected(pref_KeepDesignSpaceAndResolution);
        this.pref_KeepDesignSpaceAndResolution = pref_KeepDesignSpaceAndResolution;
    }

    /**
     *
     * @return
     */
    public int getPref_CSS_PopulationSize() {
        this.pref_CSS_PopulationSize = (Integer) jSpinnerCSSPopulationSize.getValue();
        return this.pref_CSS_PopulationSize;
    }

    /**
     *
     * @param Pref_CSS_PopulationSize
     */
    public void setPref_CSS_PopulationSize(int Pref_CSS_PopulationSize) {
        jSpinnerCSSPopulationSize.setValue(Pref_CSS_PopulationSize);
        this.pref_CSS_PopulationSize = Pref_CSS_PopulationSize;
    }

    /**
     *
     * @return
     */
    public double getPref_CSS_MaxAttraction() {
        this.pref_CSS_maxAttraction = new Double(jSpinnerCSSMaxAttraction.getValue().toString());
        return this.pref_CSS_maxAttraction;
    }

    /**
     *
     * @param Pref_CSS_MaxAttraction
     */
    public void setPref_CSS_MaxAttraction(double Pref_CSS_MaxAttraction) {
        jSpinnerCSSMaxAttraction.setValue(Pref_CSS_MaxAttraction);
        this.pref_CSS_maxAttraction = Pref_CSS_MaxAttraction;
    }

    /**
     *
     * @return
     */
    public int getPref_Genetic_PopulationSize() {
        this.pref_GeneticPopulationSize = (Integer) jSpinnerGeneticPopulationSize.getValue();
        return this.pref_GeneticPopulationSize;
    }

    /**
     *
     * @param Pref_geneticPopulationSize
     */
    public void setPref_GeneticPopulationSize(int Pref_geneticPopulationSize) {
        jSpinnerGeneticPopulationSize.setValue(Pref_geneticPopulationSize);
        this.pref_GeneticPopulationSize = Pref_geneticPopulationSize;
    }

    /**
     *
     * @return
     */
    public double getPref_Genetic_MutationChance() {
        this.pref_GeneticMutationChance = (Double) jSpinnerGeneticMutationChance.getValue();
        return this.pref_GeneticMutationChance;
    }

    /**
     *
     * @param Pref_geneticMutationChance
     */
    public void setPref_GeneticMutationChance(double Pref_geneticMutationChance) {
        jSpinnerGeneticMutationChance.setValue(Pref_geneticMutationChance);
        this.pref_GeneticMutationChance = Pref_geneticMutationChance;
    }

    /**
     *
     * @return
     */
    public boolean getPref_Genetic_MutateTopSolution() {
        this.pref_GeneticMutateTopSolution = (boolean) jCheckBoxGeneticMutateTopSolution.isSelected();
        return this.pref_GeneticMutateTopSolution;
    }

    /**
     *
     * @param pref_geneticMutateTopSolution
     */
    public void setPref_GeneticMutateTopSolution(boolean pref_geneticMutateTopSolution) {
        jCheckBoxGeneticMutateTopSolution.setSelected(pref_geneticMutateTopSolution);
        this.pref_GeneticMutateTopSolution = pref_geneticMutateTopSolution;
    }

    /**
     *
     * @return
     */
    public int getPref_ABC_NumEmployedBees() {
        this.pref_ABC_NumEmployedBees = (Integer) jSpinnerABCNumEmployedBees.getValue();
        return this.pref_ABC_NumEmployedBees;
    }

    /**
     *
     * @param Pref_ABC_NumEmployedBees
     */
    public void setPref_ABC_NumEmployedBees(int Pref_ABC_NumEmployedBees) {
        jSpinnerABCNumEmployedBees.setValue(Pref_ABC_NumEmployedBees);
        this.pref_ABC_NumEmployedBees = Pref_ABC_NumEmployedBees;
    }

    /**
     *
     * @return
     */
    public int getPref_ABC_NumOnlookerBees() {
        this.pref_ABC_NumOnlookerBees = (Integer) jSpinnerABCNumOnlookerBees.getValue();
        return this.pref_ABC_NumOnlookerBees;
    }

    /**
     *
     * @param Pref_ABC_NumOnlookerBees
     */
    public void setPref_ABC_NumOnlookerBees(int Pref_ABC_NumOnlookerBees) {
        jSpinnerABCNumOnlookerBees.setValue(Pref_ABC_NumOnlookerBees);
        this.pref_ABC_NumOnlookerBees = Pref_ABC_NumOnlookerBees;
    }

    /**
     *
     * @return
     */
    public int getPref_ABC_NumScoutBees() {
        this.pref_ABC_NumScoutBees = (Integer) jSpinnerABCNumScoutBees.getValue();
        return this.pref_ABC_NumScoutBees;
    }

    /**
     *
     * @param Pref_ABC_NumScoutBees
     */
    public void setPref_ABC_NumScoutBees(int Pref_ABC_NumScoutBees) {
        jSpinnerABCNumScoutBees.setValue(Pref_ABC_NumScoutBees);
        this.pref_ABC_NumScoutBees = Pref_ABC_NumScoutBees;
    }

    /**
     *
     * @return
     */
    public int getPref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement() {
        this.pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement = (Integer) jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovement.getValue();
        return this.pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement;
    }

    /**
     *
     * @param Pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement
     */
    public void setPref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement(int Pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement) {
        jSpinnerABCMaxNumberOfFoodUpdateCyclesWithoutImprovement.setValue(Pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement);
        this.pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement = Pref_ABC_MaxNumberOfFoodUpdateCyclesWithoutImprovement;
    }

    public int getPref_MVMO_StartingPop() {
        this.pref_MVMO_StartingPop = (Integer) jSpinnerMVMOStartingPop.getValue();
        return pref_MVMO_StartingPop;
    }

    public void setPref_MVMO_StartingPop(int Pref_MVMO_StartingPop) {
        jSpinnerMVMOStartingPop.setValue(Pref_MVMO_StartingPop);
        this.pref_MVMO_StartingPop = Pref_MVMO_StartingPop;
    }

    public int getPref_MVMO_MaxPop() {
        this.pref_MVMO_MaxPop = (Integer) jSpinnerMVMOMaxPop.getValue();
        return pref_MVMO_MaxPop;
    }

    private void setPref_MVMO_MaxPop(int Pref_MVMO_MaxPop) {
        jSpinnerMVMOMaxPop.setValue(Pref_MVMO_MaxPop);
        this.pref_MVMO_MaxPop = Pref_MVMO_MaxPop;
    }

    private double getPref_MVMO_ScalingFactor() {
        this.pref_MVMO_ScalingFactor = (Double) jSpinnerMVMOScalingFactor.getValue();
        return pref_MVMO_ScalingFactor;
    }

    private void setPref_MVMO_ScalingFactor(double Pref_MVMO_ScalingFactor) {
        jSpinnerMVMOScalingFactor.setValue(Pref_MVMO_ScalingFactor);
        this.pref_MVMO_ScalingFactor = Pref_MVMO_ScalingFactor;
    }

    private double getPref_MVMO_AsymmetryFactor() {
        this.pref_MVMO_AsymmetryFactor = (Double) jSpinnerMVMOAsymmetryFactor.getValue();
        return pref_MVMO_AsymmetryFactor;
    }

    private void setPref_MVMO_AsymmetryFactor(double Pref_MVMO_AsymmetryFactor) {
        jSpinnerMVMOAsymmetryFactor.setValue(Pref_MVMO_AsymmetryFactor);
        this.pref_MVMO_AsymmetryFactor = Pref_MVMO_AsymmetryFactor;
    }

    private double getPref_MVMO_sd() {
        this.pref_MVMO_sd = (Double) jSpinnerMVMOsd.getValue();
        return this.pref_MVMO_sd;
    }

    private void setPref_MVMO_sd(double Pref_MVMO_sd) {
        jSpinnerMVMOsd.setValue(Pref_MVMO_sd);
        this.pref_MVMO_sd = Pref_MVMO_sd;
    }

    private typeOfMVMOParentSelection getPref_MVMO_parentSelection() {
        this.pref_MVMO_parentSelection = (typeOfMVMOParentSelection) jComboBoxTypeOfParentSelection.getSelectedItem();
        return this.pref_MVMO_parentSelection;
    }

    private void setPref_MVMO_ParentSelection(typeOfMVMOParentSelection PrefMVMOParentSelection) {
        jComboBoxTypeOfMVMOMutationSelection.setSelectedItem(PrefMVMOParentSelection);
        this.pref_MVMO_parentSelection = PrefMVMOParentSelection;
    }

    private typeOfMVMOMutationSelection getPref_MVMO_mutationSelection() {
        this.pref_MVMO_mutationSelection = (typeOfMVMOMutationSelection) jComboBoxTypeOfMVMOMutationSelection.getSelectedItem();
        return this.pref_MVMO_mutationSelection;
    }

    private void setPref_MVMO_mutationSelection(typeOfMVMOMutationSelection Pref_MVMO_mutationSelection) {
        jComboBoxTypeOfMVMOMutationSelection.setSelectedItem(Pref_MVMO_mutationSelection);
        this.pref_MVMO_mutationSelection = Pref_MVMO_mutationSelection;
    }

    /**
     * Sets the list of possible parameter as precision parameter in multiphase
     * This method will modifiy the jCombobox of internal parameters
     *
     * @param pList ArrayList of parameters to show in JCombobox of internal
     * parameters
     */
    public void setPossibleInternalParameters(ArrayList<parameter> pList) {

        this.internalParameterList = pList;
        this.jComboBoxInternalParameterMultiphase.setEnabled(false);
        this.jComboBoxInternalParameterMultiphase.setModel(new DefaultComboBoxModel(new String[]{noParameterString}));
        if (pList != null) {
            if (pList.size() > 0) {
                this.jComboBoxInternalParameterMultiphase.setEnabled(true);
                ArrayList<String> tmpNameList = new ArrayList<String>();
                tmpNameList.add(noParameterString);
                for (parameter p : pList) {
                    tmpNameList.add(p.getName());
                }
                this.jComboBoxInternalParameterMultiphase.setModel(new DefaultComboBoxModel(tmpNameList.toArray()));
            }
        }
    }

    /**
     * Returns the chosen parameter for precision control in
     * multiphase-optimization incl. min, max-Value. Stepsize is defined by
     * number of phases
     *
     * @return chosen parameter for precision control
     */
    public parameter getPref_MP_InternalParameterToIterateInMultiphase() {
        parameter resultParameter = null;
        if (internalParameterList != null) {
            String nameOfChosenParameter = jComboBoxInternalParameterMultiphase.getSelectedItem().toString();
            if (nameOfChosenParameter.equals(noParameterString)) {
                return null;
            }

            for (parameter internalParameterList1 : internalParameterList) {
                if (internalParameterList1.getName().equals(nameOfChosenParameter)) {
                    resultParameter = internalParameterList1;
                }
            }

        }

        return resultParameter;
    }

    /**
     * @return the pref_GeneticMaximumOptirunsWithoutSolution
     */
    public int getPref_Genetic_MaximumOptirunsWithoutSolution() {
        this.pref_GeneticMaximumOptirunsWithoutSolution = (Integer) jSpinnerGeneticMaxOptiRunsWithoutImprovement.getValue();
        return pref_GeneticMaximumOptirunsWithoutSolution;
    }

    /**
     * @param pref_GeneticMaximumOptirunsWithoutSolution the
     * pref_GeneticMaximumOptirunsWithoutSolution to set
     */
    public void setPref_GeneticMaximumOptirunsWithoutSolution(int pref_GeneticMaximumOptirunsWithoutSolution) {
        this.jSpinnerGeneticMaxOptiRunsWithoutImprovement.setValue(pref_GeneticMaximumOptirunsWithoutSolution);
        this.pref_GeneticMaximumOptirunsWithoutSolution = pref_GeneticMaximumOptirunsWithoutSolution;
    }

    /**
     * @return the pref_GeneticTypeOfCrossover
     */
    public typeOfGeneticCrossover getPref_Genetic_TypeOfCrossover() {
        this.pref_GeneticTypeOfCrossover = (typeOfGeneticCrossover) this.jComboBoxGeneticTypeOfGeneticCrossing.getSelectedItem();
        return pref_GeneticTypeOfCrossover;
    }

    /**
     * @param pref_GeneticTypeOfCrossover the pref_GeneticTypeOfCrossover to set
     */
    public void setPref_GeneticTypeOfCrossover(typeOfGeneticCrossover pref_GeneticTypeOfCrossover) {
        this.jComboBoxGeneticTypeOfGeneticCrossing.setSelectedItem(pref_GeneticTypeOfCrossover);
        this.pref_GeneticTypeOfCrossover = pref_GeneticTypeOfCrossover;
    }

    /**
     * @return the pref_GeneticNumberOfCrossings
     */
    public int getPref_Genetic_NumberOfCrossings() {
        pref_GeneticNumberOfCrossings = (Integer) jSpinnerGeneticMaxNumberOfCrossings.getValue();
        return pref_GeneticNumberOfCrossings;
    }

    /**
     * @param pref_GeneticNumberOfCrossings the pref_GeneticNumberOfCrossings to
     * set
     */
    public void setPref_GeneticNumberOfCrossings(int pref_GeneticNumberOfCrossings) {
        jSpinnerGeneticMaxNumberOfCrossings.setValue(pref_GeneticNumberOfCrossings);
        this.pref_GeneticNumberOfCrossings = pref_GeneticNumberOfCrossings;
    }

    /**
     * @return the numberOfActualOptimizationAnalysis
     */
    public Integer getNumberOfActualOptimizationAnalysis() {
        return numberOfActualOptimizationAnalysis;
    }

    /**
     * @param numberOfActualOptimizationAnalysis the
     * numberOfActualOptimizationAnalysis to set
     */
    public void setNumberOfActualOptimizationAnalysis(Integer numberOfActualOptimizationAnalysis) {
        this.numberOfActualOptimizationAnalysis = numberOfActualOptimizationAnalysis;
        this.jButtonSavePrefs.setText("Save [" + (this.getNumberOfActualOptimizationAnalysis() + 1) + "]");
    }

    /**
     * Calculates the estimated number of Simulations based on chosen cooling
     * funntion and its parametervalues
     *
     * @param phase Number of Opti-Phase, can be 0..1
     */
    private void updateNumberOfEstimatedSASimulations(int phase) {
        long numberOfSimulations = 10;

        typeOfAnnealing tmpType = getPref_SA_Cooling(phase);
        double T0 = getPref_MaxTempParameter(phase);
        double epsilon = getPref_Epsilon(phase);

        switch (tmpType) {
            case Boltzmann:
                numberOfSimulations = Math.round(Math.exp(T0 / epsilon));
                break;
            case FastAnnealing:
                numberOfSimulations = Math.round(T0 / epsilon);
                break;
            case VeryFastAnnealing:
                double c = -Math.log(getPref_TRatioScale(phase)) * Math.exp(-(Math.log(getPref_TAnnealScale(phase) / (double) dimension)));
                numberOfSimulations = Math.round(Math.pow(-Math.log(epsilon / T0) / c, (double) dimension));
                break;
            default:
                break;
        }
        preventUpdateEpsilonBasedOnNumberOfSimulations = true;
        //default will be case 0
        switch (phase) {
            case 1:
                this.jSpinnerEstSASimulationCount1.setValue(numberOfSimulations);
                break;
            default:
            case 0:
                this.jSpinnerEstSASimulationCount.setValue(numberOfSimulations);
                break;
        }

        preventUpdateEpsilonBasedOnNumberOfSimulations = false;
    }

    /**
     * Calculates a new epsilon value fo the given Number of Simulations If new
     * Epsilon smaller than 0.001 it will be marked red
     *
     * @param phase Number of Opti-Phase, can be 0..1
     */
    private void updateEpsilonBasedOnNumberOfSimulations(int phase) {
        long numberOfSimulations = 10;
        switch (phase) {
            case 1:
                numberOfSimulations = (long) jSpinnerEstSASimulationCount1.getValue();
                break;
            default:
            case 0:
                numberOfSimulations = (long) jSpinnerEstSASimulationCount.getValue();
                break;
        }
        typeOfAnnealing tmpType = getPref_SA_Cooling(phase);
        double T0 = getPref_MaxTempParameter(phase);
        double epsilon = getPref_Epsilon(phase);

        switch (tmpType) {
            case Boltzmann:
                epsilon = (1 / (Math.log(numberOfSimulations))) * T0;
                break;
            case FastAnnealing:
                epsilon = (1 / (double) numberOfSimulations) * T0;
                break;
            case VeryFastAnnealing:
                double c = -Math.log(getPref_TRatioScale(phase)) * Math.exp(-(Math.log(getPref_TAnnealScale(phase) / (double) dimension)));
                epsilon = Math.exp(-c * Math.pow((double) numberOfSimulations, 1 / (double) dimension)) * T0;
                break;
            default:
                break;
        }
        if (!preventUpdateEpsilonBasedOnNumberOfSimulations) {
            setPref_Epsilon(epsilon, phase);
        }
    }

    /**
     * Gets number of changeable parameters updates the local Dimension-variable
     * & sets text in info-label
     */
    public void updateDimension() {
        try {
            dimension = support.getNumberOfChangeableParameters();
            if (dimension < 1) {
                jLabelDimensionNumber.setText("No Dimension!");
                dimension = 1;
            } else {
                jLabelDimensionNumber.setText(Integer.toString((int) dimension));
            }
        } catch (Exception e) {
            jLabelDimensionNumber.setText("No Dimension!");
        }
        updateNumberOfEstimatedSASimulations(0);
        updateNumberOfEstimatedSASimulations(1);
    }

    /**
     * Copy all parameters from one SA phase the another
     *
     * @param TO Target phase the parameters shall be copied to
     * @param FROM The pashe were the paramaters are copied from
     */
    private void copySAParameters(int FROM, int TO) {
        setPref_Cooling(getPref_SA_Cooling(FROM), TO);
        setPref_CalculationOfNextParameterset(getPref_SA_CalculationOfNextParameterset(FROM), TO);
        setPref_MaxTempParameter(getPref_MaxTempParameter(FROM), TO);
        setPref_MaxTempCost(getPref_MaxTempCost(FROM), TO);
        setPref_TAnnealScale(getPref_TAnnealScale(FROM), TO);
        setPref_TRatioScale(getPref_TRatioScale(FROM), TO);
        setPref_Epsilon(getPref_Epsilon(FROM), TO);
    }

    /**
     * Returns the estimated number of simulation runs per phase of simulated
     * annealing
     *
     * @param phase Number of simulated annealing phase
     * @return estimated number of simulations in this phase
     */
    public long getPref_SA_NumberOfEstimatedSASimulations(int phase) {
        switch (phase) {
            case 1:
                return (long) jSpinnerEstSASimulationCount1.getValue();
            default:
            case 0:
                return (long) jSpinnerEstSASimulationCount.getValue();
        }
    }
}
