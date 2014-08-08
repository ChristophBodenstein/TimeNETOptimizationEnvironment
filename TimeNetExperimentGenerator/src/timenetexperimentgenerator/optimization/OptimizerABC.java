/*
 * Optimization-Algorithm implemented bei Andy Seidel during Diploma Thesis 2014
 *
 * Original paper [07KB]:
 * Karaboga, Basturk:
 * A powerful and efficient algorithm for numerical
 * function optimization: artificial bee colony (ABC)
 * algorithm (2007)
 */

package timenetexperimentgenerator.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import timenetexperimentgenerator.MainFrame;
import timenetexperimentgenerator.SimOptiFactory;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.simulation.Simulator;
import timenetexperimentgenerator.support;

/**
 *
 * @author A. Seidel
 */
public class OptimizerABC implements Runnable, Optimizer
{
    private String tmpPath = "";
    private String filename = "";//Original filename
    private String pathToTimeNet = "";
    private String logFileName = "";
    private MainFrame parent = null;
    private JTabbedPane MeasureFormPane;
    private ArrayList<MeasureType> listOfMeasures = new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
    private ArrayList<parameter> parameterBase;//Base set of parameters, start/end-value, stepping, etc.
    private JLabel infoLabel;
    private double simulationTimeSum = 0;
    private double cpuTimeSum = 0;
    
    private ArrayList<SimulationType> foodSources = new ArrayList<SimulationType>();
    
    private int numEmployedBees = 10;
    private int numOnlookerBees = 10;
    private int numScoutBees = 1;

    public void initOptimizer() 
    {
        this.infoLabel=support.getStatusLabel();//  infoLabel;
        this.pathToTimeNet=support.getPathToTimeNet();// pathToTimeNetTMP;
        this.MeasureFormPane=support.getMeasureFormPane();//MeasureFormPaneTMP;
        this.parent=support.getMainFrame();// parentTMP;
        this.parameterBase=parent.getParameterBase();
        this.listOfMeasures=parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
        support.log("# of Measures to be optimized: "+this.listOfMeasures.size());
        
        this.filename=support.getOriginalFilename();// originalFilename;
        
        //Ask for Tmp-Path
        this.tmpPath=support.getTmpPath();
        
        //Start this Thread
        new Thread(this).start();
        
    }
    
    private ArrayList<SimulationType> createRandomFoodSources(int numFoodSources, boolean ignoreStepping)
    {
        ArrayList<SimulationType> newFoodSources = new ArrayList<SimulationType>();
        
        for (int i=0; i<numFoodSources; ++i)
        {
            SimulationType newSim = new SimulationType();
            
            MeasureType newMeasure = new MeasureType();
            ArrayList<parameter> pArray = support.getCopyOfParameterSet(parameterBase);
            for (int j=0; j<pArray.size(); ++j)
            {
                
            }
        }
        
        return newFoodSources;
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SimulationType getOptimum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
