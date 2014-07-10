/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package timenetexperimentgenerator.plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Simon
 */
public class rplugin 
{
    String _workdir; //workdirectory for r
    String _rpath; //path to r
    PrintWriter _writer;
    File _script;
    
    
    public rplugin(String rpath) throws UnsupportedEncodingException
    {
        _rpath = rpath;
        _script = new File(_workdir);
        
        try
        {
            _writer = new PrintWriter(_script,"UTF-8");
        }
        catch(FileNotFoundException FileNotFoundError)
        {
            System.out.println(FileNotFoundError);
        }
    }
    
    public void setWorkdir(String workdir)
    {
        _workdir = workdir;
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
    
}
