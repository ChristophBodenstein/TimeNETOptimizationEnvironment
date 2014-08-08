/*
 * Optimizer using the simulated annealing algorithm
 * It´s a child of OptimizerHill
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.optimization;

import java.util.ArrayList;
import javax.swing.*;
import timenetexperimentgenerator.MainFrame;
import timenetexperimentgenerator.SimOptiFactory;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.typedef;
import timenetexperimentgenerator.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class OptimizerMultiPhase implements Runnable, Optimizer{
SimulationType bestSolution;
String tmpPath="";
String filename="";//Original filename
String pathToTimeNet="";
MainFrame parent=null;
JTabbedPane MeasureFormPane;
ArrayList<MeasureType> listOfMeasures=new ArrayList<MeasureType>();//Liste aller Measures, abfragen von MeasureFormPane
ArrayList<SimulationType> historyOfParsers=new ArrayList<SimulationType>();//History of all simulation runs
ArrayList<parameter> parameterBase;//Base set of parameters, start/end-value, stepping, etc.
JLabel infoLabel;
int numberOfPhases=2;
boolean keepSizeAndResolutionOfDesignspace=false;

    /**
     * Constructor
     *
     */
    public OptimizerMultiPhase() {
    super();
    }

    public void run() {
    ArrayList<parameter> lastParameterset;
    ArrayList<ArrayList<parameter>> newParameterset;
    //Simulator init with initial parameterset
    Optimizer mySimulator=SimOptiFactory.getOptimizer(support.getOptimizerPreferences().getPref_typeOfUsedMultiPhaseOptimization() );

    //Tune size of Design space if Checkbox is not selected
        if(!keepSizeAndResolutionOfDesignspace){
            for(int i=0;i<this.parameterBase.size();i++){
                if(this.parameterBase.get(i).isIteratableAndIntern()){
                //Originalstepping sichern
                //Stepping vergröbern, wenn möglich
                //Größe des Designspace gleich lassen

                    //Im folgenden Phasen jeweils des Stepping wieder verkleinern
                    //Dabei Designspace verkleinern (jeweils halbieren), dabei Start UND Endwert anpassen,
                    //damit die Mitte wieder passt, und zwar zum gefundenen Parameterwert!
                }
            }
        }

    }

    public void initOptimizer() {
    this.infoLabel=support.getStatusLabel();//  infoLabel;
    this.pathToTimeNet=support.getPathToTimeNet();// pathToTimeNetTMP;
    this.MeasureFormPane=support.getMeasureFormPane();//MeasureFormPaneTMP;
    this.parent=support.getMainFrame();// parentTMP;
    this.parameterBase=parent.getParameterBase();
    this.listOfMeasures=parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
    support.log("# of Measures to be optimized: "+this.listOfMeasures.size());

    this.filename=support.getOriginalFilename();// originalFilename;

    this.tmpPath=support.getTmpPath();

    support.getOptimizerPreferences().setPref_StartValue(typeOfStartValueEnum.middle);
    
    this.numberOfPhases=support.getOptimizerPreferences().getPref_NumberOfPhases();
    this.keepSizeAndResolutionOfDesignspace=support.getOptimizerPreferences().getPref_KeepDesignSpaceAndResolution();

    //Start this Thread
    new Thread(this).start();
    }

    public SimulationType getOptimum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
