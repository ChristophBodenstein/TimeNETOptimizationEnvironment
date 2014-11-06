/*
 * Provides some Information about this application
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 * Created on 25.08.2014, 13:03:58
 */

package timenetexperimentgenerator;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author Christoph Bodenstein
 */
public class AboutPanel extends javax.swing.JPanel implements HyperlinkListener{

    /** Creates new form AboutPanel */
    public AboutPanel() {
        initComponents();
        String creditsText="Creator: Christoph Bodenstein"+System.getProperty("line.separator")+System.getProperty("line.separator");
        creditsText+="Implementation of Charged System Search, Genetic Optimizer, ABC, MVO: Andy Seidel"+System.getProperty("line.separator");
        creditsText+="RPlot-Plugin: Bastian Maurer & Simon Niebler"+System.getProperty("line.separator");
        creditsText+="Distributed Simulation: Hassan Yousef + Veeranna Sulikeri";

        this.jTextPaneCredits.setText(creditsText);
        this.jTextPaneCredits.setEditable(false);

        this.jLabelVersion.setText("Version: "+support.VERSION);
        
        try {
            URL file=getClass().getResource("SystemRequirements.html");
            support.log("Path to file is:"+file.toString());
            //Desktop.getDesktop().browse(file.toURI());
            
            InputStream in = file.openStream();
            InputStreamReader isr = new InputStreamReader(in);
            StringBuffer buff = new StringBuffer();

            int len;
            while ((len = isr.read()) != -1) {
                buff.append((char) len);
            }
            in.close();
            isr.close(); 
            
            this.jEditorPaneRequirments.setContentType("text/html");
            this.jEditorPaneRequirments.setText(buff.toString());
            
            
        } catch (Exception ex) {
            support.log("Failed to open SystemRequirements.html in default browser. Maybe you are using an old java-version.");
        }
        //Add this listener to handle hyperlink clicks
        this.jEditorPaneRequirments.addHyperlinkListener(this);
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPaneCredits = new javax.swing.JTextPane();
        jLabelVersion = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPaneRequirements = new javax.swing.JScrollPane();
        jEditorPaneRequirments = new javax.swing.JEditorPane();

        jLabel1.setFont(new java.awt.Font("Malayalam MN", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TimeNET Optimization Environment");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Credits");

        jTextPaneCredits.setEditable(false);
        jTextPaneCredits.setBackground(new java.awt.Color(204, 204, 204));
        jTextPaneCredits.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jTextPaneCredits.setFont(new java.awt.Font("Lucida Sans", 1, 12)); // NOI18N
        jTextPaneCredits.setText("Christoph Bodenstein");
        jTextPaneCredits.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jTextPaneCredits.setDropTarget(null);
        jTextPaneCredits.setFocusable(false);
        jScrollPane1.setViewportView(jTextPaneCredits);

        jLabelVersion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelVersion.setText("Version: x.x.xxx");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("A Tool for exploring SCPN Designspaces and doing Optimization runs within.");

        jEditorPaneRequirments.setEditable(false);
        jScrollPaneRequirements.setViewportView(jEditorPaneRequirments);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneRequirements, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelVersion)
                .addGap(24, 24, 24)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneRequirements, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPaneRequirments;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneRequirements;
    private javax.swing.JTextPane jTextPaneCredits;
    // End of variables declaration//GEN-END:variables

    /**
     * Handle hyperlink events
     * If user clicks on hyperlink, this link will be browsed by local web browser
     * @param e Hyperlinkevent (click, Activated, etc.)
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
    
        if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
            final URL url = e.getURL();
            try {
                Desktop.getDesktop().browse(url.toURI());
            } catch (IOException ex) {
                support.log("IOException while opening Link to "+ url.toString());
            } catch (URISyntaxException ex) {
                support.log("URISyntaxException while opening Link to "+ url.toString());
            }
        }
    }

}
