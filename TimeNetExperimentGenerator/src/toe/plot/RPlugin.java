/**
* Interface: MainFrame <--> R Plot Plugin
**/

package toe.plot;

/**
 *
 * @author Simon Niebler, Bastian Mauerer
 */
public class RPlugin 
{      
    private static  PlotFrameController controller;
            
    public RPlugin()
    {
        controller = new PlotFrameController();
    }
    
    /**
     * Shows the frame for setting up the plot.
     **/
    public void openPlotGui()
    {
        controller.setVisible(true);
        controller.readCachedListOfStatistics();
    }
    
    /**
     * Get cached simulation files.
     **/
    public static void updateCachedListOfStatistics()
    {
        controller.readCachedListOfStatistics();
    }
}
