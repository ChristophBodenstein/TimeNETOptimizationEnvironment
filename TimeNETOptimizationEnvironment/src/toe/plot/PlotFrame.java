/*
 * Implements the frame which displays the R plots.
 */
package toe.plot;

import java.awt.*;
import javax.swing.*;
import javax.swing.JLabel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 * @author Bastian Mauerer, Simon Niebler, Christoph Bodenstein
 */
public class PlotFrame extends javax.swing.JFrame {

    JLabel label;
    ImageIcon icon;

    /**
     * Creates new form PlotFrame
     */
    public PlotFrame() {
        initComponents();
        setResizable(false);
        this.setTitle("R Plot");
        label = new JLabel();
        this.getContentPane().add(label);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Save as...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 576, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 507, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowStateChanged

    /**
     * Save plot image as jpg
     *
     * @param evt ActionEvent sent from button
     */
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG File", "jpg");

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.removeChoosableFileFilter(jFileChooser.getAcceptAllFileFilter());
        jFileChooser.setSelectedFile(new File("image.jpg"));
        if (jFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            Image img = icon.getImage();

            BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_USHORT_555_RGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            try {
                ImageIO.write(bi, "jpg", new File(jFileChooser.getSelectedFile().getAbsolutePath()));
                support.log(jFileChooser.getSelectedFile().getAbsolutePath() + " saved!", typeOfLogLevel.INFO);
            } catch (Exception e) {
                support.log("Exception occured while saving image!", typeOfLogLevel.ERROR);
            }
        } else {
            support.log("Image not saved!", typeOfLogLevel.ERROR);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * Sets a given image to the background of the frame.
     *
     * @param path Path of image to be shown
     *
     */
    public void showImage(String path) {
        this.setVisible(true);
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(path));

            icon = new ImageIcon(bufferedImage);

            label.setSize(icon.getIconWidth(), icon.getIconHeight());
            label.setIcon(icon);

            label.revalidate();
            label.repaint();

            this.getContentPane().setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            this.pack();
        } catch (Exception e) {
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    // End of variables declaration//GEN-END:variables
}
