/*
 * Callback methods to be called after simulation is finished
 * Needed for asychronous simulaiton/Optimization
 *
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package toe.helper;

import toe.typedef;

/**
 *
 * @author Christoph Bodenstein
 */
public interface SimOptiCallback {

    /**
     * Is called after simulation has finished successful
     *
     * @param message to be displayed or printed
     * @param feedback Type of feedback (successful, canceled, etc.)
     */
    public void operationSucessfull(String message, typedef.typeOfProcessFeedback feedback);

    /**
     * Is called after simulation has been canceled
     *
     * @param message to be displayed or printed
     * @param feedback Type of feedback, mostly "canceled"
     */
    public void operationCanceled(String message, typedef.typeOfProcessFeedback feedback);
}
