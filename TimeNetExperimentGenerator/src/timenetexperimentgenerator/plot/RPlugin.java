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
        controller.setVisible(true);
        controller.readCachedListOfStatistics();
    }
    
    public static void updateCachedListOfStatistics()
    {
        controller.readCachedListOfStatistics();
    }
}
