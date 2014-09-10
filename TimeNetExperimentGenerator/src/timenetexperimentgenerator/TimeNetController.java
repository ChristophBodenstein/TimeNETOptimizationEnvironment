/*
 * TimeNetController Class, sets the Look and Feel and inits the MainFrame
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Christoph Bodenstein
 */
public class TimeNetController {

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
             support.log("ClassNotFoundException at start.");
        } catch (InstantiationException e) {
            support.log("InstantiationException at start.");
        } catch (IllegalAccessException e) {
            support.log("IllegalAccessException at start.");
        } catch (UnsupportedLookAndFeelException e) {
            support.log("UnsupportedLookAndFeelException at start.");
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            MainFrame myFrame=new MainFrame();
            myFrame.setVisible(true);

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            support.printMemoryStats();
            }
            }, 1000*support.DEFAULT_MEMORYPRINT_INTERVALL, 1000*support.DEFAULT_MEMORYPRINT_INTERVALL);

        }
    });
        
        
        
    }

}
