
package timenetexperimentgenerator.plot;

import java.io.File;
import java.io.UnsupportedEncodingException;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import timenetexperimentgenerator.PlotFrame;

/**
 *
 * @author Simon Niebler, Bastian Mauerer
 */
public class RPlugin 
{    
    //R interface
    private RConnection r;
    
    private PlotFrame plotFrame;
            
    public RPlugin()
    {
        plotFrame = new PlotFrame();
        
        try
        {
            r = new RConnection();
        }
        catch(RserveException e){}
    }
    
    public void addPlot(File csvFile)
    {
        String path;
        String filename;
    }
    
    public void plot()
    {
        //todo r command for plotting
    }
    
    public String getRVersion()
    {        
        try
        {
            try
            {
                String path = r.eval("getwd()").asString();
                path = path + File.separatorChar + "rplot.png";
                r.eval("png(filename=\"rplot.png\")");
                r.eval("values <- c(1, 3, 6, 4, 9)");
                r.eval("plot(values, type=\"o\", col=\"blue\")");
                r.eval("title(main=\"TEST\", col.main=\"red\", font.main=4)");
                r.eval("dev.off()");
                plotFrame.showImage(path);
         
                return r.eval("R.version.string").asString();
            }
            catch(REXPMismatchException e)
            {
                return "REXP Mismatch Exception";
            }
        }
        catch(RserveException e)
        {
            return ("Rserve Exception");
        }
    }
}
