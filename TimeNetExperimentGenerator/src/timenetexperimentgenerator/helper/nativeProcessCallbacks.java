/*
 * Native Process Callbacks
 * To be implemented by every class who wants to start native Processes and get some callback information
 *
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package timenetexperimentgenerator.helper;

/**
 *
 * @author Christoph Bodenstein
 */
public interface nativeProcessCallbacks {

    /**
     * Called by client to inform that a given Process has ended
     */
    public void processEnded();

    /**
     * Called by client to inform that a given Process has exited with error
     * @param message Errormessage to be displayed.
     */
    public void errorOccured(String message);
}
