/**
 * Interface: MainFrame <--> R Plot Plugin
*
 */
package toe.plot;

import java.awt.Point;

/**
 *
 * @author Simon Niebler, Bastian Mauerer
 */
public class RPlugin {

    private static PlotFrameController controller;

    public RPlugin() {
        controller = new PlotFrameController();
    }

    /**
     * Shows the frame for setting up the plot.
     *
     */
    public void openPlotGui() {
        controller.setVisible(true);
        controller.readCachedListOfStatistics();
    }

    /**
     * Get cached simulation files.
     *
     */
    public static void updateCachedListOfStatistics() {
        controller.readCachedListOfStatistics();
    }

    /**
     * Return position of Plot-Fram as Point
     *
     * @return Position of RFrame
     */
    public Point getFramePostition() {
        return controller.getLocation();
    }

    /**
     * Sets the location of RPlot-Frame
     *
     * @param p the location to be set for RPlot-Frame
     */
    public void setFramePosition(Point p) {
        controller.setLocation(p);
    }

}
