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
    this.parent=support.getMainFrame();// parentTMP;
    this.parameterBase=parent.getParameterBase();
    }

    public void run() {
    ArrayList<ArrayList<parameter>> newParameterset;
    //Optimizer init with initial parameterset
    Optimizer myOptimizer;//=SimOptiFactory.getOptimizer(support.getOptimizerPreferences().getPref_typeOfUsedMultiPhaseOptimization() );
    Optimizer lastOptimizer=null;
    /*    Minimales Raster merken
    Maximales Raster = Minimales Raster / 2^PhasenAnzahl
    Pro Phase wird Raster um Faktor 2 verkleinert.
    -->Raster bleibt scheinbar immer gleich groß (aus Sicht des OptiAlgorithmus)
    Schrittweite ist Originalschrittweite*2^Phasenanzahl
        Nur wenn es passt!. Prüfung einbauen, auch beim zurückskalieren
    */


    //Tune size of Design space if Checkbox is not selected
        //if(!keepSizeAndResolutionOfDesignspace){


            for(int i=0;i<this.parameterBase.size();i++){
                parameter p=this.parameterBase.get(i);
                if(p.isIteratableAndIntern()){
                //Originalstepping sichern
                //Stepping vergröbern, wenn möglich
                p.setStepping(p.getStepping()*Math.pow(2, this.numberOfPhases));
                //TODO Check if Stepping is smaller then design space!

                /*double tmpDistance=(p.getEndValue()-p.getStartValue())/2;
                p.setStartValue(p.getStartValue() + tmpDistance);
                p.setEndValue(p.getEndValue()-tmpDistance);
                */
                    //Im folgenden Phasen jeweils des Stepping wieder verkleinern
                    //Dabei Designspace verkleinern (jeweils halbieren), dabei Start UND Endwert anpassen,
                    //damit die Mitte wieder passt, und zwar zum gefundenen Parameterwert!
                }
            }

            support.setParameterBase(parameterBase);

            for(int phaseCounter=0;phaseCounter<this.numberOfPhases;phaseCounter++){
                if(lastOptimizer!=null){
                //get Optimum from last Optimizer as start for next one
                ArrayList<parameter> lastParamaterset=lastOptimizer.getOptimum().getListOfParameters();
                    for(int i=0;i<lastParamaterset.size();i++){
                    parameter p=lastParamaterset.get(i);
                        if(p.isIteratableAndIntern()){
                        //fit stepping
                            p.setStepping(p.getStepping()*2);
                        //fit start & end Value
                        //set start value based on "value", half size but not smaller then original start-Value.
                        //set end Value based on "value", halt size but not bigger then orininal end-Value.

                        }
                    }
                }

                myOptimizer=SimOptiFactory.getOptimizer(support.getOptimizerPreferences().getPref_typeOfUsedMultiPhaseOptimization() );
                //TODO Set the parameterBase first!
                //Push parameterset to support
                //in Mainframe: if support has parameterbase then take this (if Multiphase-simulation)
                myOptimizer.initOptimizer();
            }


        //}

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
