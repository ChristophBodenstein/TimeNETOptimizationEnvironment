/*
 * Native Process
 * It needs a processbuilder-object
 * native process will be started and this thread will wait until the and and spin a char in a given label.
 * Label is not necessary
 * Thread will be as long alive as the started process
 *
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.helper;

import javax.swing.JLabel;
import toe.support;
import toe.typedef.typeOfLogLevel;

/**
 *
 * @author Christoph Bodenstein
 */
public class nativeProcess extends Thread {

    private ProcessBuilder myProcessBuilder = null;
    JLabel statusLabel = null;
    private boolean running = true;
    private nativeProcessCallbacks pfc = null;
    private boolean killProcess = false;

    /**
     * Contructor
     *
     * @param p ProcessBuilder Object, this process will be started
     * @param processCallbackHandler Callback handler (must implement
     * Callback-methods)
     */
    public nativeProcess(ProcessBuilder p, nativeProcessCallbacks processCallbackHandler) {
        this.myProcessBuilder = p;
        this.pfc = processCallbackHandler;
        new Thread(this).start();
    }

    /**
     * Main Method for thread. Will start the native process and wait for its
     * end
     *
     */
    @Override
    public void run() {
        realNativeProcess myNativeProcess = new realNativeProcess(this);
        myNativeProcess.start();

        while (isRunning()) {
            try {
                Thread.sleep(100);
                support.spinInLabel();
                if (support.isCancelEverything() || killProcess) {
                    //Kill native Thread and set Running to false
                    support.log("Try to kill the started process.", typeOfLogLevel.INFO);
                    myNativeProcess.killProcess();
                    this.setRunning(false);
                    this.pfc.errorOccured("Plot canceled.");
                    killProcess = false;
                    return;
                }

            } catch (InterruptedException ex) {
                support.log("Error while waiting for real native Process: " + myProcessBuilder.toString(), typeOfLogLevel.ERROR);
            }
        }

        if (this.pfc != null) {
            pfc.processEnded();
        } else {
            support.log("No PlotFrameController given, will not show image.", typeOfLogLevel.ERROR);
        }
        synchronized (this) {
            notify();
        }
    }

    /**
     * @return true if the started process is still running, else false
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * @return the myProcessBuilder
     */
    public ProcessBuilder getMyProcessBuilder() {
        return myProcessBuilder;
    }

    /**
     * @param myProcessBuilder the myProcessBuilder to set
     */
    public void setMyProcessBuilder(ProcessBuilder myProcessBuilder) {
        this.myProcessBuilder = myProcessBuilder;
    }

    /**
     * If set to true, native process is killed in next thread-loop
     *
     * @param killProcess the killProcess to set
     */
    public void setKillProcess(boolean killProcess) {
        this.killProcess = killProcess;
    }
}

class realNativeProcess extends Thread {

    nativeProcess myNativeProcess = null;
    Process p = null;

    public realNativeProcess(nativeProcess p) {
        myNativeProcess = p;
    }

    @Override
    public void run() {
        if (myNativeProcess != null) {
            try {
                p = myNativeProcess.getMyProcessBuilder().start();
                p.waitFor();
                myNativeProcess.setRunning(false);
            } catch (Exception ex) {
                support.log("Error while waiting for nativeProcess:" + myNativeProcess.toString(), typeOfLogLevel.ERROR);
            }

        } else {
            support.log("No Process given, could not start anything.", typeOfLogLevel.ERROR);
        }
    }

    /**
     * Kills the running process. Remember, the started simulation-executable
     * might still be running!
     */
    public void killProcess() {
        if (p != null) {
            p.destroy();
            support.log("Process will be killed. Check for running simulation binaries and kill them manually!!!", typeOfLogLevel.INFO);
        }
    }
}
