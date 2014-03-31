/*
 * Interface for all Optimizers
 */

package timenetexperimentgenerator;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/**
 *
 * @author Christoph Bodenstein
 */
public interface Optimizer {
public void initOptimizer(String originalFilename, MainFrame parentTMP, JTabbedPane MeasureFormPaneTMP, String pathToTimeNetTMP, JLabel infoLabel);
}
