/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sse
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
