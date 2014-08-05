package timenetexperimentgenerator.plot;

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
    
    public void openPlotGui()
    {
        controller.readCachedListOfStatistics();
        controller.setVisible(true);
    }
    
    public static void updateCachedListOfStatistics()
    {
        controller.readCachedListOfStatistics();
    }
}
