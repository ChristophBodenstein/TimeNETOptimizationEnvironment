/*
 * Web based simulator of SCPNs
 * download SCPNs as xml files, start local simulation, upload the results


* To be modified by: Group studies 2014


 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.simulation;

import timenetexperimentgenerator.support;

/**
 *
 * @author sse
 */
public class SimulatorWebSlave implements Runnable{
private boolean shouldEnd=false;

    public void run() {
        while(true){
        //Enter your code here...
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                support.log("Error while sleeping Thread of Slave Web simulator.");
            }
        support.log("Dummy Thread started to download and simulate SCPNs.");
        
            if(this.shouldEnd){
            support.log("Slave Thread will end now.");
            this.shouldEnd=false;
            return;
            }
        
        }
    }

    /**
     * @return the shouldEnd
     */
    public boolean isShouldEnd() {
        return shouldEnd;
    }

    /**
     * @param shouldEnd the shouldEnd to set
     */
    public void setShouldEnd(boolean shouldEnd) {
        this.shouldEnd = shouldEnd;
    }
    
}
