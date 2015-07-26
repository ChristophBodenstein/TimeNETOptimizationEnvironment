/*
 * TimeNetController Class, sets the Look and Feel and inits the MainFrame
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package toe;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            try {
                //Try to create pref-dir
                File prefdir=new File(support.NAME_OF_PREF_DIR);
                if(!prefdir.exists()){
                    prefdir.mkdir();
                }
                
                
                MainFrame myFrame=new MainFrame();
                myFrame.setVisible(true);
                
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        //support.printMemoryStats();
                        support.updateMemoryProgressbar();
                        support.checkSpinningShowTime();
                    }
                }, 1000*support.DEFAULT_MEMORYPRINT_INTERVALL, 1000*support.DEFAULT_MEMORYPRINT_INTERVALL);
            } catch (IOException ex) {
                Logger.getLogger(TimeNetController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    });
        
        
        
    }

}
