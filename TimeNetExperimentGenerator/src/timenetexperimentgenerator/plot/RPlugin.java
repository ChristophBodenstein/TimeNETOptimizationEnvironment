
package timenetexperimentgenerator.plot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import timenetexperimentgenerator.PlotFrame;
import timenetexperimentgenerator.support;

/**
 *
 * @author Simon Niebler, Bastian Mauerer
 */
public class RPlugin 
{       
    private PlotFrame plotFrame;
            
    public RPlugin()
    {
        plotFrame = new PlotFrame();
    }
    
    public void plot()
    {
        try
        {
            PrintWriter writer = new PrintWriter("rscript.r", "UTF-8");
            String userdir = System.getProperty("user.dir");
            userdir = userdir.replace("\\", "/");
            
            writer.println("setwd(\"" + userdir + "\")");
            writer.println("png(filename=\"rplot.png\")");
            writer.println("values <- c(1, 3, 6, 4, 9)");
            writer.println("plot(values, type=\"o\", col=\"blue\")");
            writer.close();

            String command = support.getPathToR() + File.separator+"bin" + File.separator + "Rscript rscript.r";
            support.log("executing command: " + command);
            Runtime.getRuntime().exec(command); 
        }
        catch(IOException e)
        {
        }
        
        plotFrame.showImage(System.getProperty("user.dir") + File.separator + "rplot.png");
    }
}
