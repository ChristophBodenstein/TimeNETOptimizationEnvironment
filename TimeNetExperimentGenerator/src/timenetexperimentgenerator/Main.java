/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author sse
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
         try {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TimeNet Experiment Generator");
                
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
        }
        catch(Exception e) {
                e.printStackTrace();
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            MainFrame myFrame=new MainFrame();
            myFrame.setVisible(true);
        }
    });
        
        
        
    }

}
