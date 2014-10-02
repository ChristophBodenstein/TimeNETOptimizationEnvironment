/*
 * Callback methods to be called after simulation is finished
 * Needed for asychronous simulaiton/Optimization
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
public interface SimOptiCallback {

    public void operationSucessfull(String message);
    public void operationCanceld(String message);
}
