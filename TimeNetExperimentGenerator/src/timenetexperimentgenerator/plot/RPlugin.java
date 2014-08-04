package timenetexperimentgenerator.plot;

/**
 *
 * @author Simon Niebler, Bastian Mauerer
 */
public class RPlugin 
{      
    private PlotFrameController controller;
            
    public RPlugin()
    {
        controller = new PlotFrameController();
    }
    
    public void openPlotGui()
    {
        controller.setVisible(true);
    }
}
