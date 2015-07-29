/**
 * This frame is used to setup the R Plots, create the R script and run it.
 *
 */
package toe.plot;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import toe.helper.Statistic;
import toe.helper.StatisticAggregator;
import toe.helper.nativeProcess;
import toe.helper.nativeProcessCallbacks;
import toe.support;
import java.awt.ComponentOrientation;
import javax.swing.JColorChooser;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import toe.typedef;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Bastian Mauerer, Simon Niebler, Christoph Bodenstein
 */
public class PlotFrameController extends javax.swing.JFrame implements nativeProcessCallbacks {

    private final String imageFilePath = System.getProperty("user.dir") + File.separator + "rplot.png";
    private final String rScriptFilePath = System.getProperty("user.dir") + File.separator + "rscript.r";
    Color plotColor = Color.black;
    private final String noneString = "None";
    private final String errorFilename = "errorFile.Rout";
    private typedef.typeOfPossiblePlot possiblePlot = typedef.typeOfPossiblePlot.NoPlot;

    private final PlotFrame plotFrame;

    /**
     * Creates new form PlotFrameController
     */
    public PlotFrameController() {
        initComponents();

        CachedFilesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {

                if (evt.getClickCount() == 2 && !CachedFilesList.isSelectionEmpty()) {
                    OpenFileTextField.setText(CachedFilesList.getSelectedValue().toString());
                    loadCSV(CachedFilesList.getSelectedValue().toString());
                }
            }
        });

        this.setTitle("R Plugin");
        plotFrame = new PlotFrame();
        setResizable(false);

        jButtonOpenColorChooser.setBackground(Color.black);
        jCheckBox1.setEnabled(false);

        //jlist alignment
        CachedFilesList.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        final int maximum = jScrollPane1.getHorizontalScrollBar().getMaximum();
        jScrollPane1.getHorizontalScrollBar().setValue(maximum);
        checkIfColorChosingIsPossible();
    }

    /**
     * Loads the header and MeasureNames from a .csv file.
     *
     * @param fileName Name of csv file to be loaded for plotting
     */
    public void loadCSV(String fileName) {
        jCheckBox1.setSelected(false);
        jCheckBox1.setEnabled(false);

        File file = new File(fileName);
        try {
            FileReader namereader = new FileReader(file);
            BufferedReader in = new BufferedReader(namereader);
            String header = in.readLine();

            support.log("csv header is: " + header, typeOfLogLevel.INFO);

            String[] parts = header.split(";");
            DefaultListModel model = new DefaultListModel();
            model.addElement(getNoneString());

            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
                model.addElement(parts[i]);
            }

            ColumnList.setModel(model);

            DefaultComboBoxModel cmodel = new DefaultComboBoxModel();

            String first;
            String current;

            if ((first = in.readLine()) != null) {
                first = first.split(";")[0];
                cmodel.addElement(first.split(";")[0]);
                while ((current = in.readLine()) != null && !current.split(";")[0].equals(first)) {
                    cmodel.addElement(current.split(";")[0]);
                }
            }
            MeasureComboBox.setModel(cmodel);
        } catch (Exception e) {

        }

        XValueLabel.setText(getNoneString());
        YValueLabel.setText(getNoneString());
        ZValueLabel.setText(getNoneString());
    }

    /**
     * Get all cached simulation files and add them to the CachedFilesList.
     *
     */
    public void readCachedListOfStatistics() {
        DefaultListModel cachedFilesListModel = new DefaultListModel();
        ArrayList<Statistic> cachedListOfStatistics;
        cachedListOfStatistics = StatisticAggregator.getListOfStatistics();

        for (Statistic cachedListOfStatistic : cachedListOfStatistics) {
            cachedFilesListModel.addElement(cachedListOfStatistic.getName());
        }

        CachedFilesList.setModel(cachedFilesListModel);

        final int maximum = jScrollPane1.getHorizontalScrollBar().getMaximum();
        jScrollPane1.getHorizontalScrollBar().setValue(maximum);
        this.JButtonPlot.setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        CachedFilesList = new javax.swing.JList();
        OpenButton = new javax.swing.JButton();
        OpenFileTextField = new javax.swing.JTextField();
        LoadButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        ColumnList = new javax.swing.JList();
        AxisSetupLabel = new javax.swing.JLabel();
        SetXButton = new javax.swing.JButton();
        SetYButton = new javax.swing.JButton();
        SetZButton = new javax.swing.JButton();
        LoadFileLabel = new javax.swing.JLabel();
        CachedFilesLabel = new javax.swing.JLabel();
        MeasureLabel = new javax.swing.JLabel();
        MeasureComboBox = new javax.swing.JComboBox();
        JButtonPlot = new javax.swing.JButton();
        XValueLabel = new javax.swing.JLabel();
        YValueLabel = new javax.swing.JLabel();
        ZValueLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxPlotChar = new javax.swing.JComboBox();
        jButtonOpenColorChooser = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jComboBoxTypeOf3DPlot = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        CachedFilesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        CachedFilesList.setAutoscrolls(false);
        jScrollPane1.setViewportView(CachedFilesList);

        OpenButton.setText("Open...");
        OpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenButtonActionPerformed(evt);
            }
        });

        OpenFileTextField.setEditable(false);
        OpenFileTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        OpenFileTextField.setMaximumSize(new java.awt.Dimension(6, 20));
        OpenFileTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenFileTextFieldActionPerformed(evt);
            }
        });

        LoadButton.setText("Load");
        LoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadButtonActionPerformed(evt);
            }
        });

        ColumnList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(ColumnList);

        AxisSetupLabel.setText("Axis Setup");

        SetXButton.setText("SetX");
        SetXButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetXButtonActionPerformed(evt);
            }
        });

        SetYButton.setText("SetY");
        SetYButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetYButtonActionPerformed(evt);
            }
        });

        SetZButton.setText("SetZ");
        SetZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetZButtonActionPerformed(evt);
            }
        });

        LoadFileLabel.setText("Load File");

        CachedFilesLabel.setText("Cached Files");

        MeasureLabel.setText("Measure:");

        MeasureComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MeasureComboBoxActionPerformed(evt);
            }
        });

        JButtonPlot.setText("Plot");
        JButtonPlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JButtonPlotActionPerformed(evt);
            }
        });

        XValueLabel.setText(getNoneString());

        YValueLabel.setText(getNoneString());

        ZValueLabel.setText(getNoneString());

        jLabel1.setText("Plot-Char:");

        jComboBoxPlotChar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ".", "*", "+", "x", "X", "o", "O" }));
        jComboBoxPlotChar.setSelectedIndex(3);
        jComboBoxPlotChar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxPlotCharActionPerformed(evt);
            }
        });

        jButtonOpenColorChooser.setText("Plot-Color");
        jButtonOpenColorChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenColorChooserActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Add to last plot");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jComboBoxTypeOf3DPlot.setModel(new DefaultComboBoxModel(typedef.typeOf3DPlot.values()));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(OpenFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(OpenButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LoadFileLabel)
                            .addComponent(CachedFilesLabel)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(LoadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(SetZButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(ZValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(SetYButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(YValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(SetXButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(XValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(MeasureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(1, 1, 1)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jComboBoxPlotChar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButtonOpenColorChooser)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jComboBoxTypeOf3DPlot, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(MeasureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jCheckBox1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(JButtonPlot, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(2, 2, 2))))))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(AxisSetupLabel))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LoadFileLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(OpenFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(OpenButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CachedFilesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(AxisSetupLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(XValueLabel)
                            .addComponent(SetXButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(YValueLabel)
                            .addComponent(SetYButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ZValueLabel)
                            .addComponent(SetZButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(MeasureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MeasureLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jComboBoxPlotChar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonOpenColorChooser)
                            .addComponent(jComboBoxTypeOf3DPlot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(LoadButton)
                        .addComponent(JButtonPlot, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(jCheckBox1))
                .addContainerGap())
            .addComponent(jSeparator1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * File Open Dialog for .csv files.
     *
     */
    private void OpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenButtonActionPerformed

        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV File", "csv");

        JFileChooser fileChooser = new JFileChooser();

        fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
        fileChooser.setFileFilter(filter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setDialogTitle(" Choose .csv file ");
        String filePath;

        if (fileChooser.showDialog(this, "Choose this") == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().isFile()) {
                filePath = fileChooser.getSelectedFile().toString();
            } else {
                filePath = fileChooser.getCurrentDirectory().toString();
            }
            support.log("chosen .csv file: " + filePath, typeOfLogLevel.INFO);
            OpenFileTextField.setText(filePath);
            loadCSV(filePath);

            ((DefaultListModel) CachedFilesList.getModel()).addElement(filePath);
        } else {
            support.log("No .csv file selected.", typeOfLogLevel.INFO);
        }
    }//GEN-LAST:event_OpenButtonActionPerformed

    /**
     * Load cached .csv file.
     *
     */
    private void LoadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadButtonActionPerformed
        if (!CachedFilesList.isSelectionEmpty()) {
            OpenFileTextField.setText(CachedFilesList.getSelectedValue().toString());
            loadCSV(CachedFilesList.getSelectedValue().toString());
        }
    }//GEN-LAST:event_LoadButtonActionPerformed

    private void OpenFileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenFileTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_OpenFileTextFieldActionPerformed

    /**
     * Set y axis column name.
     *
     */
    private void SetYButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetYButtonActionPerformed
        if (!ColumnList.isSelectionEmpty()) {
            YValueLabel.setText(ColumnList.getSelectedValue().toString());
        }
        checkIfColorChosingIsPossible();
    }//GEN-LAST:event_SetYButtonActionPerformed

    private void MeasureComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MeasureComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MeasureComboBoxActionPerformed

    /**
     * Create the R script and run it.
     *
     * @param evt ActionEvent, sent by Button
     */
    private void JButtonPlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JButtonPlotActionPerformed
        char plotChar = support.DEFAULT_PLOT_CHAR;
        PrintWriter writer;

        try {
            plotChar = this.jComboBoxPlotChar.getSelectedItem().toString().charAt(0);
        } catch (Exception e) {
        }

        //Always delete image file first
        support.del(new File(imageFilePath));

        //Delete Error-File
        support.del(new File(errorFilename));
        try {
            if (!jCheckBox1.isSelected()) //create new script
            {
                support.del(new File(rScriptFilePath));
                writer = new PrintWriter("rscript.r", "UTF-8");
                writer.println("options(warn=-1)");
                writer.println("library(plot3D)");
            } else {
                //append to existing script (hold is checked)
                writer = new PrintWriter(new FileOutputStream(new File("rscript.r"), true));
            }
            String userdir = System.getProperty("user.dir");
            userdir = userdir.replace("\\", "/");

            writer.println("base<-read.csv(\"" + OpenFileTextField.getText().replace("\\", "/") + "\", sep=\";\", dec=\",\",check.names=FALSE)");
            if (MeasureComboBox.getSelectedItem() != null) {
                writer.println("sub<-subset(base,  base$MeasureName ==  \"" + MeasureComboBox.getSelectedItem().toString() + "\")");
            } else {
                writer.println("sub<-base");
            }

            writer.println("setwd(\"" + userdir + "\")");

            switch (this.possiblePlot) {
                case Plot2D:
                    writer.println("png(filename=\"rplot.png\")");
                    writer.println("plot(as.numeric(as.character( sub(\"" + "," + "\" , \"" + "."
                            + "\" , sub$\"" + XValueLabel.getText() + "\"))),as.numeric(as.character( sub(\"" + "," + "\" , \"" + "."
                            + "\" , sub$\"" + YValueLabel.getText() + "\"))), xlab=\"" + XValueLabel.getText()
                            + "\",ylab=\"" + YValueLabel.getText() + "\" , pch=\"" + plotChar + "\", col=\""
                            + String.format("#%02X%02X%02X", plotColor.getRed(), plotColor.getGreen(), plotColor.getBlue()) + "\")");
                    jCheckBox1.setEnabled(true);
                    break;
                case Plot3D:

                    switch (typedef.typeOf3DPlot.valueOf(jComboBoxTypeOf3DPlot.getSelectedItem().toString())) {
                        case ScatterPlot:
                            writer.println("png(filename=\"rplot.png\")");
                            writer.println("scatter3D(as.numeric(as.character( sub(\"" + "," + "\" , \"" + "." + "\" , sub$\"" + XValueLabel.getText()
                                    + "\"))),as.numeric(as.character( sub(\"" + "," + "\" , \"" + "." + "\" , sub$\"" + YValueLabel.getText()
                                    + "\"))),as.numeric(as.character( sub(\"" + "," + "\" , \"" + "." + "\" , sub$\"" + ZValueLabel.getText()
                                    + "\"))), xlab=\"" + XValueLabel.getText() + "\",ylab=\"" + YValueLabel.getText() + "\",zlab=\""
                                    + ZValueLabel.getText() + "\", phi=15, theta=120, col=NULL, NAcol=\"white\", colkey=NULL, panel.first=NULL, clim=NULL, clab=NULL, bty=\"b2\", pch=\"" + plotChar + "\", add=FALSE)");
                            break;
                        case Perspective:
                            writer.println("library(rgl)");
                            writer.println("x<-as.numeric(sub(\"" + "," + "\" , \"" + "." + "\", sub$\"" + XValueLabel.getText() + "\" )) ");
                            writer.println("y<-as.numeric(sub(\"" + "," + "\" , \"" + "." + "\", sub$\"" + YValueLabel.getText() + "\" )) ");
                            writer.println("z<-as.numeric(sub(\"" + "," + "\" , \"" + "." + "\", sub$\"" + ZValueLabel.getText() + "\" )) ");
                            writer.println("x1<-unique(x)");
                            writer.println("y1<-unique(y)");
                            writer.println("x1<-sort(x1)");
                            writer.println("y1<-sort(y1)");
                            writer.println("#Create 2D-Array for z-values");
                            writer.println("my.array<-array(dim=c(length(x1),length(y1)))");

                            writer.println("for( ix in 1:length(x1)){");
                            writer.println("for ( iy in 1:length(y1)){");
                            writer.println("subtemp<-subset(sub, sub$\"" + XValueLabel.getText() + "\"==x1[ix])");
                            writer.println("subtemp<-subset(subtemp, subtemp$\"" + YValueLabel.getText() + "\"==y1[iy])");
                            writer.println("if(dim(subtemp)[1]==1){");
                            writer.println("my.array[ix,iy]<-subtemp$\"" + ZValueLabel.getText() + "\" ");
                            writer.println("} } }");

                            writer.println("col <- tryCatch({");
                            writer.println("zlim <- range(my.array)");
                            writer.println("zlen <- zlim[2] - zlim[1] + 1");
                            writer.println("colorlut <- terrain.colors(zlen) # height color lookup table");
                            writer.println("col <- colorlut[ my.array-zlim[1]+1 ] # assign colors to heights for each point");
                            writer.println("}, error=function(cond) {");
                            writer.println("col=c(\"lightblue\")");
                            writer.println("},warning=function(cond) {");
                            writer.println("col=c(\"lightblue\")");
                            writer.println("},finally={})");

                            writer.println("persp3d(x1, y1, my.array, col = col,xlab=\"" + XValueLabel.getText() + "\",ylab=\"" + YValueLabel.getText() + "\",zlab=\""
                                    + ZValueLabel.getText() + "\")");
                            writer.println("grid3d(c(\"x\", \"y+\", \"z\"))");
                            //Open Browser with webgl
                            writer.println("browseURL(paste(\"file://\", writeWebGL(dir=file.path(" + getRCompatiblePathFromStandardPath(support.getTmpPath() + File.separator + "webgl") + "), \n"
                                    + "          width=1024), sep=\"\"))"
                                    + "");
                            /*String tmpPathURI=new File(support.getTmpPath()).getAbsoluteFile().toURI().toString();
                             writer.println("browseURL(paste(\"file://\", writeWebGL(dir=file.path(\"" + tmpPathURI +  "\",\"webgl\"), \n"
                             + "          width=1024), sep=\"\"))"
                             + "");*/
                            break;

                        case Heatmap:
                            writer.println("library(rgl)");
                            writer.println("x<-as.numeric(sub(\"" + "," + "\" , \"" + "." + "\", sub$\"" + XValueLabel.getText() + "\" )) ");
                            writer.println("y<-as.numeric(sub(\"" + "," + "\" , \"" + "." + "\", sub$\"" + YValueLabel.getText() + "\" )) ");
                            writer.println("z<-as.numeric(sub(\"" + "," + "\" , \"" + "." + "\", sub$\"" + ZValueLabel.getText() + "\" )) ");
                            writer.println("x1<-unique(x)");
                            writer.println("y1<-unique(y)");
                            writer.println("x1<-sort(x1)");
                            writer.println("y1<-sort(y1)");
                            writer.println("#Create 2D-Array for z-values");
                            writer.println("my.array<-array(dim=c(length(x1),length(y1)))");

                            writer.println("for( ix in 1:length(x1)){");
                            writer.println("for ( iy in 1:length(y1)){");
                            writer.println("subtemp<-subset(sub, sub$\"" + XValueLabel.getText() + "\"==x1[ix])");
                            writer.println("subtemp<-subset(subtemp, subtemp$\"" + YValueLabel.getText() + "\"==y1[iy])");
                            writer.println("if(dim(subtemp)[1]==1){");
                            writer.println("my.array[ix,iy]<-subtemp$\"" + ZValueLabel.getText() + "\" ");
                            writer.println("} } }");

                            writer.println("col <- tryCatch({");
                            writer.println("zlim <- range(my.array)");
                            writer.println("zlen <- zlim[2] - zlim[1] + 1");
                            writer.println("colorlut <- terrain.colors(zlen) # height color lookup table");
                            writer.println("col <- colorlut[ my.array-zlim[1]+1 ] # assign colors to heights for each point");
                            writer.println("}, error=function(cond) {");
                            writer.println("col=c(\"lightblue\")");
                            writer.println("},warning=function(cond) {");
                            writer.println("col=c(\"lightblue\")");
                            writer.println("},finally={})");
                            writer.println("png(filename=\"rplot.png\")");
                            writer.println("image2D(z = my.array,x=x1,y=y1,xlab=\"" + XValueLabel.getText() + "\",ylab=\"" + YValueLabel.getText() + "\",clab=\""
                                    + ZValueLabel.getText() + "\",rasterImage = TRUE,colkey = list(length = 0.5, shift = 0.0, width = 1.0, cex.axis=0.5))");

                            //image2D(z = my.array,x=x1,y=y1,xlab = "X-Name", ylab = "Y-Name",rasterImage = TRUE, colkey = list(length = 0.5, shift = 0.0, width = 1.0, cex.axis=0.5))
                            break;
                        default:
                            support.log("No 3DPlot-Type chosen. Plot not possible.", typeOfLogLevel.ERROR);
                            break;

                    }

                    jCheckBox1.setEnabled(true);
                    break;

                default:
                    support.log("No Plot possible", typeOfLogLevel.ERROR);
                    break;
            }
            writer.close();

            String command = support.getPathToR() + File.separator + "bin" + File.separator + "Rscript rscript.r > " + errorFilename;
            support.log("executing command: " + command, typeOfLogLevel.INFO);

            java.lang.ProcessBuilder processBuilder = new java.lang.ProcessBuilder(support.getPathToR() + File.separator + "bin" + File.separator + "Rscript", "rscript.r", "2>", "errorFile.Rout");

            nativeProcess myNativeProcess = new nativeProcess(processBuilder, this);

            this.JButtonPlot.setEnabled(false);
            jCheckBox1.setEnabled(true);

        } catch (Exception e) {
        }


    }//GEN-LAST:event_JButtonPlotActionPerformed

    /**
     * Converts a path to a String which is compatible with R-paths in scripts
     */
    public String getRCompatiblePathFromStandardPath(String standardPath) {
        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String tmpString[] = standardPath.split(pattern);
        String resultString = "";
        for (int i = 0; i < tmpString.length; i++) {

            resultString = resultString + "\"" + tmpString[i] + "\" ";
            if (i < tmpString.length - 1) {
                resultString = resultString + ",";
            }
        }
        return resultString;
    }

    /**
     * Will show the image file, specified in the global vars of this class or
     * Error-Message in log This method is called by the native Thread as a
     * callback after creating the image file
     */
    @Override
    public void processEnded() {
        boolean error = false;
        support.log("Try to show image at:" + imageFilePath, typeOfLogLevel.INFO);
        try {
            File errorFile = new File(errorFilename);
            if (errorFile.exists()) {

                if (errorFile.length() >= 2) {
                    error = true;
                    FileReader errorReader = new FileReader(errorFile);
                    BufferedReader bufferedErrorReader = new BufferedReader(errorReader);
                    String eLine;
                    while ((eLine = bufferedErrorReader.readLine()) != null) {
                        support.log(eLine, typeOfLogLevel.ERROR);
                    }

                    bufferedErrorReader.close();
                    errorReader.close();
                    JOptionPane.showMessageDialog(null, "There were Errors running R-Script for plot. See Log for details.");
                    errorFile.delete();
                }
            }
        } catch (Exception e) {
            support.log("Error while reading the error file.", typeOfLogLevel.ERROR);
        }
        if (this.possiblePlot == typedef.typeOfPossiblePlot.Plot3D && typedef.typeOf3DPlot.valueOf(jComboBoxTypeOf3DPlot.getSelectedItem().toString()) == typedef.typeOf3DPlot.Perspective) {
            //3d-Plot was rendered to webgl
        } else {
            if (!error) {
                plotFrame.showImage(imageFilePath);
            }
        }
        support.setStatusText("");
        this.JButtonPlot.setEnabled(true);
    }

    /**
     * Set x axis column name.
     *
     */
    private void SetXButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetXButtonActionPerformed
        if (!ColumnList.isSelectionEmpty()) {
            XValueLabel.setText(ColumnList.getSelectedValue().toString());
        }
        checkIfColorChosingIsPossible();
    }//GEN-LAST:event_SetXButtonActionPerformed

    /**
     * Set z axis column name.
     *
     */
    private void SetZButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetZButtonActionPerformed
        if (!ColumnList.isSelectionEmpty()) {
            ZValueLabel.setText(ColumnList.getSelectedValue().toString());
        }
        checkIfColorChosingIsPossible();
    }//GEN-LAST:event_SetZButtonActionPerformed

    /**
     * Color chosing is only possible for x-y-Graphs. Else deactivate the
     * color-chose-button
     */
    private void checkIfColorChosingIsPossible() {

        if (!XValueLabel.getText().equals(noneString) && !YValueLabel.getText().equals(noneString) && ZValueLabel.getText().equals(noneString)) {
            this.jButtonOpenColorChooser.setEnabled(true);
            this.JButtonPlot.setEnabled(true);
            possiblePlot = typedef.typeOfPossiblePlot.Plot2D;
            this.jComboBoxTypeOf3DPlot.setEnabled(false);
        } else {
            this.jButtonOpenColorChooser.setEnabled(false);
            if (!XValueLabel.getText().equals(noneString) && !YValueLabel.getText().equals(noneString) && !ZValueLabel.getText().equals(noneString)) {
                possiblePlot = typedef.typeOfPossiblePlot.Plot3D;
                this.JButtonPlot.setEnabled(true);
                this.jComboBoxTypeOf3DPlot.setEnabled(true);
            } else {
                this.JButtonPlot.setEnabled(false);
                this.jComboBoxTypeOf3DPlot.setEnabled(false);
                possiblePlot = typedef.typeOfPossiblePlot.NoPlot;
            }
        }
    }

    /*Choose the plot color*/
    private void jButtonOpenColorChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenColorChooserActionPerformed
        JColorChooser jc = new JColorChooser();
        try {
            AbstractColorChooserPanel[] panels = jc.getChooserPanels();
            for (AbstractColorChooserPanel accp : panels) {
                String name = accp.getDisplayName();
                System.out.println("Name:" + name);
                if (name.equals("Swatches")) {
                    JOptionPane.showMessageDialog(null, accp);

                }
            }

        } catch (Exception ex) {
            Logger.getLogger(PlotFrameController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        jc.setVisible(true);
        Color chosen = jc.getColor();
        if (chosen != null) {
            plotColor = chosen;
            jButtonOpenColorChooser.setBackground(chosen);

            support.log("Chosen color: " + String.format("#%02X%02X%02X", chosen.getRed(), chosen.getGreen(), chosen.getBlue()), typeOfLogLevel.INFO);
        }

    }//GEN-LAST:event_jButtonOpenColorChooserActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jComboBoxPlotCharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxPlotCharActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxPlotCharActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AxisSetupLabel;
    private javax.swing.JLabel CachedFilesLabel;
    private javax.swing.JList CachedFilesList;
    private javax.swing.JList ColumnList;
    private javax.swing.JButton JButtonPlot;
    private javax.swing.JButton LoadButton;
    private javax.swing.JLabel LoadFileLabel;
    private javax.swing.JComboBox MeasureComboBox;
    private javax.swing.JLabel MeasureLabel;
    private javax.swing.JButton OpenButton;
    private javax.swing.JTextField OpenFileTextField;
    private javax.swing.JButton SetXButton;
    private javax.swing.JButton SetYButton;
    private javax.swing.JButton SetZButton;
    private javax.swing.JLabel XValueLabel;
    private javax.swing.JLabel YValueLabel;
    private javax.swing.JLabel ZValueLabel;
    private javax.swing.JButton jButtonOpenColorChooser;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBoxPlotChar;
    private javax.swing.JComboBox jComboBoxTypeOf3DPlot;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables

    /**
     * nativeProcessCallback interface method
     *
     * @param message Message to be sent to Mainframe and probably shown
     */
    public void errorOccured(String message) {
        this.JButtonPlot.setEnabled(true);
        support.log("Error occured during Plot.", typeOfLogLevel.ERROR);
        support.setStatusText("Plot Error!");
        this.JButtonPlot.setEnabled(true);
    }

    /**
     * @return the noneString
     */
    public String getNoneString() {
        return noneString;
    }
}
