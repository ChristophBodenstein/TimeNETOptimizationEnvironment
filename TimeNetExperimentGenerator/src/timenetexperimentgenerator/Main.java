/*
 * Main Class, sets the Look and Feel and inits the MainFrame
 */

package timenetexperimentgenerator;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Christoph Bodenstein
 */
public class Main {

    /**
     * @param args the command line arguments, no arguments are used
     */
    public static void main(String[] args) {
         try {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TimeNet Experiment Generator");        
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
        }
        catch(ClassNotFoundException e) {
             System.out.println("ClassNotFoundException at start.");
        } catch (InstantiationException e) {
            System.out.println("InstantiationException at start.");
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccessException at start.");
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println("UnsupportedLookAndFeelException at start.");
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            MainFrame myFrame=new MainFrame();
            myFrame.setVisible(true);
        }
    });
        
        
        
    }

}
