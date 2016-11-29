/*
 * TOE Class, sets the Look and Feel and inits the MainFrame
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
import toe.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class TOE {

    /**
     * @param args the command line arguments, no arguments are used
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        try {

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TimeNet Experiment Generator");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (ClassNotFoundException e) {
            support.log("ClassNotFoundException at start.", typeOfLogLevel.ERROR);
        } catch (InstantiationException e) {
            support.log("InstantiationException at start.", typeOfLogLevel.ERROR);
        } catch (IllegalAccessException e) {
            support.log("IllegalAccessException at start.", typeOfLogLevel.ERROR);
        } catch (UnsupportedLookAndFeelException e) {
            support.log("UnsupportedLookAndFeelException at start.", typeOfLogLevel.ERROR);
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    MainFrame myFrame = new MainFrame();
                    support.setMainFrame(myFrame);
                    File prefdir = new File(support.NAME_OF_PREF_DIR);
                    if (!prefdir.exists()) {
                        prefdir.mkdir();
                    }
                    myFrame.setVisible(true);
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            //support.printMemoryStats();
                            support.updateMemoryProgressbar();
                            support.checkSpinningShowTime();
                            support.updateCountLabels();
                        }
                    }, 1000 * support.DEFAULT_MEMORYPRINT_INTERVAL, 1000 * support.DEFAULT_MEMORYPRINT_INTERVAL);
                } catch (IOException ex) {
                    Logger.getLogger(TOE.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

    }

}
