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

    public void processEnded();
    public void errorOccured(String message);
}
