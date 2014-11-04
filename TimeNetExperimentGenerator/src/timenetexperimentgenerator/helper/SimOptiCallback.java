/*
 * Callback methods to be called after simulation is finished
 * Needed for asychronous simulaiton/Optimization
 *
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */
package timenetexperimentgenerator.helper;

import timenetexperimentgenerator.typedef;

/**
 *
 * @author Christoph Bodenstein
 */
public interface SimOptiCallback {

    public void operationSucessfull(String message, typedef.typeOfProcessFeedback feedback);
    public void operationCanceled(String message, typedef.typeOfProcessFeedback feedback);
}
