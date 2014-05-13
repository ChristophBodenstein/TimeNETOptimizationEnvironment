/*
 * Optimization-Algorithm implemented by Andy Seidel during Diploma Thesis 2014
 * 
 * Orinal paper [10KT]: 
 * Kaveh, Talatahari:
 * A novel heuristic optimization method: charged system search (2010)
 */

package timenetexperimentgenerator.optimization;

import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import timenetexperimentgenerator.MainFrame;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.parser;
import timenetexperimentgenerator.support;

/**
 *
 * @author A. Seidel
 */


public class OptimizerChargedSystemSearch implements Runnable, Optimizer{


parser currentSolution;
parser nextSolution;
String tmpPath="";
String filename="";//Original filename
String pathToTimeNet="";
MainFrame parent=null;
JTabbedPane MeasureFormPane;
ArrayList<MeasureType> listOfMeasures=new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
ArrayList<parser> historyOfParsers=new ArrayList<parser>();//History of all simulation runs
parameter[] parameterBase;//Base set of parameters, start/end-value, stepping, etc.
double[] arrayOfIncrements;
boolean optimized=false;//False until Optimization is ended
JLabel infoLabel;
double simulationTimeSum=0;
double cpuTimeSum=0;
    
    public void initOptimizer()
    {
        this.infoLabel=support.getStatusLabel();//  infoLabel;
        this.pathToTimeNet=support.getPathToTimeNet();// pathToTimeNetTMP;
        this.MeasureFormPane=support.getMeasureFormPane();//MeasureFormPaneTMP;
        this.parent=support.getMainFrame();// parentTMP;
        this.parameterBase=parent.getParameterBase();
        this.listOfMeasures=parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
        support.log("# of Measures to be optimized: "+this.listOfMeasures.size());

        //Alle Steppings auf Standard setzen
        arrayOfIncrements=new double[parameterBase.length];
        for(int i=0; i<parameterBase.length; i++)
        {
            arrayOfIncrements[i]=support.getDouble(parameterBase[i].getStepping());
        }

    this.filename=support.getOriginalFilename();// originalFilename;
    //Ask for Tmp-Path

    this.tmpPath=support.getTmpPath();
    //Start this Thread
    new Thread(this).start();
    }

    public void run()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
