/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator;

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
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Test");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(ClassNotFoundException e) {
                System.out.println("ClassNotFoundException: " + e.getMessage());
        }
        catch(InstantiationException e) {
                System.out.println("InstantiationException: " + e.getMessage());
        }
        catch(IllegalAccessException e) {
                System.out.println("IllegalAccessException: " + e.getMessage());
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            MainFrame myFrame=new MainFrame();
            myFrame.setVisible(true);
        }
    });
        
        
        
    }

}
