/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AboutPanel.java
 *
 * Created on 25.08.2014, 13:03:58
 */

package timenetexperimentgenerator;

/**
 *
 * @author sse
 */
public class AboutPanel extends javax.swing.JPanel {

    /** Creates new form AboutPanel */
    public AboutPanel() {
        initComponents();
        String creditsText="Creator: Christoph Bodenstein"+System.getProperty("line.separator")+System.getProperty("line.separator");
        creditsText+="Implementation of Charged System Search, Genetic Optimizer, OptimizerABC: Andy Seidel"+System.getProperty("line.separator");
        creditsText+="RPlot-Plugin: Bastian Maurer & Simon Niebler"+System.getProperty("line.separator");
        creditsText+="Distributed Simulation: Hassan Yousef + Veeranna Sulikeri";

        this.jTextPaneCredits.setText(creditsText);
        this.jTextPaneCredits.setEditable(false);

        this.jLabelVersion.setText("Version: "+support.VERSION);
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

        jLabel1.setFont(new java.awt.Font("Malayalam MN", 1, 14));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("TimeNet Experiment Generator");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Credits");

        jTextPaneCredits.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jTextPaneCredits.setEditable(false);
        jTextPaneCredits.setFont(new java.awt.Font("Lucida Sans", 0, 12)); // NOI18N
        jTextPaneCredits.setText("Christoph Bodenstein");
        jTextPaneCredits.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jTextPaneCredits.setFocusable(false);
        jScrollPane1.setViewportView(jTextPaneCredits);

        jLabelVersion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelVersion.setText("Version: 0.9.173");

        jLabel3.setText("A Tool for exploring SCPN Designspaces and doing Optimization runs within.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelVersion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPaneCredits;
    // End of variables declaration//GEN-END:variables

}