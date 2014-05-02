/*
 * PocessMonitor used for controlling the started TimeNet-Instance
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christoph Bodenstein
 */
public class ProcMon implements Runnable {

  private final Process _proc;
  private volatile boolean _complete=false;

  public boolean isComplete() { return _complete; }

  public ProcMon(Process p){
  this._proc=p;
  }

  public void run() {
        try {
            _proc.waitFor();
            _complete = true;
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcMon.class.getName()).log(Level.SEVERE, null, ex);
            _complete=true;
        }
  }

  
}
