/*
 * PocessMonitor used for controlling the started TimeNet-Instance
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.helper;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import timenetexperimentgenerator.support;

/**
 *
 * @author Christoph Bodenstein
 */
public class ProcMon implements Runnable {
boolean stopThread=false;
private JLabel localStatusLabel=support.getStatusLabel();

  private final Process _proc;
  private volatile boolean _complete=false;

  public boolean isComplete() { return _complete; }

  public ProcMon(Process p){
  this._proc=p;
  }

  public void run() {

    while(!stopThread){
            try {
                Thread.sleep(500);
                support.spinInLabel(localStatusLabel);
            } catch (InterruptedException ex) {
            }
            
        if(support.isCancelEverything()){
        support.log("Will cancel a TimeNet-Instance. You should take care of existing client threads!");
        _proc.destroy();
        return;
        }

    }
  
  }

  public void stopThread(){
  this.stopThread=true;
  }

    /**
     * @return the localStatusLabel
     */
    public JLabel getLocalStatusLabel() {
        return localStatusLabel;
    }

    /**
     * @param localStatusLabel the localStatusLabel to set
     */
    public void setLocalStatusLabel(JLabel localStatusLabel) {
        this.localStatusLabel = localStatusLabel;
    }

  
}
