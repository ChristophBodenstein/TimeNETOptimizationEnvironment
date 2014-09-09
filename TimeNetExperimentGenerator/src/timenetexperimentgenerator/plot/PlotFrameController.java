package timenetexperimentgenerator.plot;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import timenetexperimentgenerator.helper.Statistic;
import timenetexperimentgenerator.helper.StatisticAggregator;
import timenetexperimentgenerator.support;

/**
 *
 * @author Bastian Mauerer, Simon Niebler
 */
public class PlotFrameController extends javax.swing.JFrame {

    private PlotFrame plotFrame;
    /**
     * Creates new form PlotFrameController
     */
    public PlotFrameController() {
        initComponents();
        
        CachedFilesList.addMouseListener(new MouseAdapter() 
        {
            public void mouseClicked(MouseEvent evt) 
            {
                
                if (evt.getClickCount() == 2 && !CachedFilesList.isSelectionEmpty()) 
                {
                    OpenFileTextField.setText(CachedFilesList.getSelectedValue().toString());
                    loadCSV(CachedFilesList.getSelectedValue().toString());
                } 
            }
        });
        
        this.setTitle("R Plugin");
        plotFrame = new PlotFrame();
        setResizable(false);
    }
    
    public void loadCSV(String fileName)
    {
        File file = new File(fileName);
        try
        {
            FileReader namereader = new FileReader(file);
            BufferedReader in = new BufferedReader(namereader);
            String header = in.readLine();
            
            support.log("csv header is: " + header);
                       
            String[] parts = header.split(";");
            DefaultListModel model = new DefaultListModel();
            model.addElement("None");
            
            for(int i = 0; i<parts.length; i++)
            {
                parts[i] = parts[i].trim();
                model.addElement(parts[i]);
            }
            
            ColumnList.setModel(model);
            
            DefaultComboBoxModel cmodel = new DefaultComboBoxModel();
            
            String first;
            String current;
            
            
            if((first = in.readLine()) != null)
            {
                cmodel.addElement( first.split(";")[0] );
                while( ( current = in.readLine() ) != null && !current.split( ";" )[0].equals( first ) )
                    {
                        cmodel.addElement( current.split(";")[0] );
                    }
            }        
            MeasureComboBox.setModel( cmodel );
        }
        catch(Exception e)
        {
            
        }
        
        XValueLabel.setText("None");
        YValueLabel.setText("None");
        ZValueLabel.setText("None");
    }
    
    public void readCachedListOfStatistics()
    {
        DefaultListModel cachedFilesListModel = new DefaultListModel();
        ArrayList<Statistic> cachedListOfStatistics;
        cachedListOfStatistics = StatisticAggregator.getListOfStatistics();
        
        for(int i = 0; i<cachedListOfStatistics.size(); i++)
                cachedFilesListModel.addElement(cachedListOfStatistics.get(i).getName());
            
            CachedFilesList.setModel(cachedFilesListModel);
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
        XLabel = new javax.swing.JLabel();
        YLabel = new javax.swing.JLabel();
        ZLabel = new javax.swing.JLabel();
        MeasureLabel = new javax.swing.JLabel();
        MeasureComboBox = new javax.swing.JComboBox();
        PlotButton = new javax.swing.JButton();
        XValueLabel = new javax.swing.JLabel();
        YValueLabel = new javax.swing.JLabel();
        ZValueLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        CachedFilesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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

        XLabel.setText("X:");

        YLabel.setText("Y:");

        ZLabel.setText("Z:");

        MeasureLabel.setText("Measure:");

        MeasureComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MeasureComboBoxActionPerformed(evt);
            }
        });

        PlotButton.setText("Plot");
        PlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlotButtonActionPerformed(evt);
            }
        });

        XValueLabel.setText("None");

        YValueLabel.setText("None");

        ZValueLabel.setText("None");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LoadFileLabel)
                    .addComponent(CachedFilesLabel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(OpenFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(OpenButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LoadButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(PlotButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(SetXButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(SetYButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(SetZButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(AxisSetupLabel)
                                    .addGap(124, 124, 124)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(XLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(YLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                                    .addComponent(ZLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ZValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(YValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(XValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(MeasureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(MeasureComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(LoadButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(AxisSetupLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(SetYButton)
                                .addComponent(SetZButton))
                            .addComponent(SetXButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(XLabel)
                            .addComponent(XValueLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(YLabel)
                            .addComponent(YValueLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ZLabel)
                            .addComponent(ZValueLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(MeasureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MeasureLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(PlotButton)))
                .addContainerGap())
            .addComponent(jSeparator1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
          if(fileChooser.getSelectedFile().isFile() ){
              filePath=fileChooser.getSelectedFile().toString();
          }else{
              filePath=fileChooser.getCurrentDirectory().toString();
          }
          support.log("chosen .csv file: " + filePath);
          OpenFileTextField.setText(filePath);
          loadCSV(filePath);
    
        }else{
        support.log("No .csv file selected.");
        } 
    }//GEN-LAST:event_OpenButtonActionPerformed

    private void LoadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadButtonActionPerformed
        if(!CachedFilesList.isSelectionEmpty())
        {
            OpenFileTextField.setText(CachedFilesList.getSelectedValue().toString());
            loadCSV(CachedFilesList.getSelectedValue().toString());
        }      
    }//GEN-LAST:event_LoadButtonActionPerformed

    private void OpenFileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenFileTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_OpenFileTextFieldActionPerformed

    private void SetYButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetYButtonActionPerformed
        if(!ColumnList.isSelectionEmpty())
        {
            YValueLabel.setText(ColumnList.getSelectedValue().toString());
        }
        
    }//GEN-LAST:event_SetYButtonActionPerformed

    private void MeasureComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MeasureComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MeasureComboBoxActionPerformed

    private void PlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlotButtonActionPerformed
        try
        {
            PrintWriter writer = new PrintWriter("rscript.r", "UTF-8");
            String userdir = System.getProperty("user.dir");
            userdir = userdir.replace("\\", "/");
            
            writer.println("library(plot3D)");
            writer.println("base<-read.csv(\"" + OpenFileTextField.getText().replace("\\", "/") + "\", sep=\";\", dec=\",\",check.names=FALSE)");
            
            if(MeasureComboBox.getSelectedItem() != null)
            {
                writer.println("sub<-subset(base,  base$MeasureName ==  \"" + MeasureComboBox.getSelectedItem().toString() + "\")");
            }
            else
            {
                writer.println("sub<-base");
            }
            
            writer.println("setwd(\"" + userdir + "\")");
            writer.println("png(filename=\"rplot.png\")");
            //writer.println("svg(filename=\"rplot.svg\")");
            //writer.println("values <- c(1, 3, 6, 4, 9)");
            //writer.println("plot(values, type=\"o\", col=\"blue\")");
            
            if(XValueLabel.getText() != "None" && YValueLabel.getText() != "None" && ZValueLabel.getText() == "None")
            {
                writer.println("plot(as.numeric(sub$\"" + XValueLabel.getText() + "\"),as.numeric(sub$\"" + YValueLabel.getText() + "\"), xlab=\"" + XValueLabel.getText() + "\",ylab=\"" + YValueLabel.getText() + "\" , pch=\"x\")");
            }
            else if(XValueLabel.getText() != "None" && YValueLabel.getText() != "None" && ZValueLabel.getText() != "None")
            {
                writer.println("scatter3D(as.numeric(sub$\"" + XValueLabel.getText() + "\"),as.numeric(sub$\"" + YValueLabel.getText() + "\"),as.numeric(sub$\"" + ZValueLabel.getText() + "\"), xlab=\"" + XValueLabel.getText() + "\",ylab=\"" + YValueLabel.getText() + "\",zlab=\"" + ZValueLabel.getText() + "\", phi=15, theta=120, col=NULL, NAcol=\"white\", colkey=NULL, panel.first=NULL, clim=NULL, clab=NULL, bty=\"b2\", pch=\"x\", add=FALSE)");
            }
            else
            {
                writer.close();
                return;
            }
            
            writer.close();
               
            String command = support.getPathToR() + File.separator + "bin" + File.separator + "Rscript rscript.r 2> errorFile.Rout";
            support.log("executing command: " + command);
            Process child = Runtime.getRuntime().exec(command); 
            try
            {
                child.waitFor();
            }
            catch(InterruptedException e)
            {
            }
        }
        catch(IOException e)
        {
        }
        
        try
        {
            String userdir = System.getProperty("user.dir");
            File errorFile = new File(userdir + File.separator + "errorFile.Rout");
            if(errorFile.exists())
            {
                FileReader errorReader = new FileReader(errorFile);
                BufferedReader bufferedErrorReader = new BufferedReader(errorReader);
                String error = null;
                
                while((error = bufferedErrorReader.readLine()) != null)
                {
                    support.log(error);
                }
                
                bufferedErrorReader.close();
                errorReader.close();
                errorFile.delete();
            }
        }
        catch(Exception e)
        {
        }
        
        
        plotFrame.showImage(System.getProperty("user.dir") + File.separator + "rplot.png");
    }//GEN-LAST:event_PlotButtonActionPerformed

    private void SetXButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetXButtonActionPerformed
        if(!ColumnList.isSelectionEmpty())
        {
            XValueLabel.setText(ColumnList.getSelectedValue().toString());
        }
    }//GEN-LAST:event_SetXButtonActionPerformed

    private void SetZButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetZButtonActionPerformed
        if(!ColumnList.isSelectionEmpty())
        {
            ZValueLabel.setText(ColumnList.getSelectedValue().toString());
        }
    }//GEN-LAST:event_SetZButtonActionPerformed

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
            java.util.logging.Logger.getLogger(PlotFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PlotFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PlotFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PlotFrameController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PlotFrameController().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AxisSetupLabel;
    private javax.swing.JLabel CachedFilesLabel;
    private javax.swing.JList CachedFilesList;
    private javax.swing.JList ColumnList;
    private javax.swing.JButton LoadButton;
    private javax.swing.JLabel LoadFileLabel;
    private javax.swing.JComboBox MeasureComboBox;
    private javax.swing.JLabel MeasureLabel;
    private javax.swing.JButton OpenButton;
    private javax.swing.JTextField OpenFileTextField;
    private javax.swing.JButton PlotButton;
    private javax.swing.JButton SetXButton;
    private javax.swing.JButton SetYButton;
    private javax.swing.JButton SetZButton;
    private javax.swing.JLabel XLabel;
    private javax.swing.JLabel XValueLabel;
    private javax.swing.JLabel YLabel;
    private javax.swing.JLabel YValueLabel;
    private javax.swing.JLabel ZLabel;
    private javax.swing.JLabel ZValueLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
