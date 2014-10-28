/*
 * Optimizer using the simulated annealing algorithm
 * It´s a child of OptimizerHill
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.optimization;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.*;
import timenetexperimentgenerator.MainFrame;
import timenetexperimentgenerator.SimOptiFactory;
import timenetexperimentgenerator.datamodel.MeasureType;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.helper.StatisticAggregator;
import timenetexperimentgenerator.support;
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
boolean optimized=false;
String logFileName;

    /**
     * Constructor
     *
     */
    public OptimizerMultiPhase() {
    super();
    this.parent=support.getMainFrame();// parentTMP;
    this.parameterBase=support.getOriginalParameterBase();
    support.setParameterBase(parameterBase);
    logFileName=support.getTmpPath()+File.separator+this.getClass().getSimpleName()+"_"+Calendar.getInstance().getTimeInMillis()+support.getOptimizerPreferences().getPref_LogFileAddon()+".csv";
    support.log("LogfileName:"+logFileName);
    //this.keepSizeAndResolutionOfDesignspace=support.getOptimizerPreferences().getPref_KeepDesignSpaceAndResolution();
    this.numberOfPhases=support.getOptimizerPreferences().getPref_NumberOfPhases();
    support.setListOfChangableParametersMultiphase(support.getOriginalParameterBase());
    }

    public void run() {
    ArrayList<ArrayList<parameter>> newParameterset;
    ArrayList<parameter> originalParameterset=support.getOriginalParameterBase();
    //Optimizer init with initial parameterset
    Optimizer myOptimizer;//=SimOptiFactory.getOptimizer(support.getOptimizerPreferences().getPref_typeOfUsedMultiPhaseOptimization() );
    Optimizer lastOptimizer=null;
    //Push the chosen StartStrategy
    typeOfStartValueEnum myTmpStartValue=support.getOptimizerPreferences().getPref_StartValue();
    
    //Prepare parameterset for first Optimization run
            for(int i=0;i<this.parameterBase.size();i++){
                parameter p=this.parameterBase.get(i);
                keepSizeAndResolutionOfDesignspace=false;
                if(p.isIteratableAndIntern()){
                    //&& !this.keepSizeAndResolutionOfDesignspace
                    //    }){
                    //embiggen the stepping :-)
                    if(!this.keepSizeAndResolutionOfDesignspace){
                    double tmpStepping=p.getStepping()*Math.pow(2, (this.numberOfPhases-1));
                        //TODO This check is wrong!Use ABS and count up DS!
                        if( (Math.abs(p.getEndValue()-p.getStartValue())/tmpStepping) >=((double)support.DEFAULT_MINIMUM_DESIGNSPACE_SIZE_PER_PARAMETER)){
                        p.setStepping(tmpStepping);
                        //p.printInfo();
                        support.log("New Stepping for Parameter "+p.getName()+" is "+ p.getStepping());
                        }else{
                        support.log("Will not change stepping for parameter "+ p.getName() +"Designspace would be to small.");
                        }
                    }else{
                    support.log("Will not change stepping for parameter "+ p.getName() +"KeeSizeAndStepping is activated.");
                    }
                
                }else{
                support.log("Parameter is not Intern & Changable.");
                }
            }


            //Set Parameterbase for first use
            support.setParameterBase(parameterBase);
            
            //Start in the middle
            //support.getOptimizerPreferences().setPref_StartValue(typeOfStartValueEnum.middle);

            for(int phaseCounter=0;phaseCounter<this.numberOfPhases;phaseCounter++){
            support.log("Starting phase "+ (phaseCounter+1) +" of "+this.numberOfPhases);
                if(lastOptimizer!=null){
                //get Optimum from last Optimizer as start for next one
                ArrayList<parameter> lastParamaterset=lastOptimizer.getOptimum().getListOfParameters();
                    for(int i=0;i<lastParamaterset.size();i++){
                    parameter p=lastParamaterset.get(i);
                        if(p.isIteratableAndIntern() && !this.keepSizeAndResolutionOfDesignspace){
                        //fit stepping
                        p.setStepping(p.getStepping()/2);
                        //fit start & end Value
                        //set start value based on "value", half size but not smaller then original start-Value.
                        //set end Value based on "value", halt size but not bigger then original end-Value.
                        double tmpDistance=p.getEndValue()-p.getStartValue();
                        p.setStartValue(Math.max(p.getValue()-tmpDistance/4, p.getStartValue())) ;

                        p.setEndValue(Math.min(p.getValue()+tmpDistance/4, p.getEndValue()));

                        //If design space resolution is to small, ensmall the stepping
                        if((p.getEndValue()-p.getStartValue())/p.getStepping()<((double)support.DEFAULT_MINIMUM_DESIGNSPACE_SIZE_PER_PARAMETER)){
                        support.log("Resulting designspace for parameter "+p.getName()+" to small. Ensmalling the Stepping.");
                        p.setStepping( Math.max((p.getEndValue()-p.getStartValue())/support.DEFAULT_MINIMUM_DESIGNSPACE_SIZE_PER_PARAMETER ,1));
                        }

                        support.log("Parameter "+p.getName()+" set to Start: "+p.getStartValue()+", End: "+p.getEndValue()+", Stepping: "+p.getStepping()+", Value: "+p.getValue());

                        }
                    }
                
                //Push parameterset to support
                support.setParameterBase(lastParamaterset);
                //Set first-Parameter-choosing-strategy to preset
                support.getOptimizerPreferences().setPref_StartValue(typeOfStartValueEnum.preset);
                }

                //Calculate the values for Conf-Intervall, MaxRelError and internal precision parameter
                OptimizerPreferences prefs=support.getOptimizerPreferences();
                ArrayList<parameter> lastParamaterset=support.getParameterBase();
                //Check if all parameters can iterate
                boolean iterateConfidenceInterval=prefs.getPref_ConfidenceIntervallStart()<prefs.getPref_ConfidenceIntervallEnd();
                boolean iterateMaxRelError=prefs.getPref_MaxRelErrorStart()>prefs.getPref_MaxRelErrorEnd();
                boolean iterateInternal=true;//It can be iterated up and down
                double confInterval,maxRelError,internal=0.0;
                if(phaseCounter==0){
                //First Phase, so we use the start-values for all parameters
                confInterval=prefs.getPref_ConfidenceIntervallStart();
                maxRelError=prefs.getPref_MaxRelErrorStart();
                internal=prefs.getPref_InternalParameterStart();    
                }else{
                    if(phaseCounter<this.numberOfPhases-1){
                    //ConfidenceIntervall goes from 85 up to 99 (min to max)
                    double tmpDifference=prefs.getPref_ConfidenceIntervallEnd()-prefs.getPref_ConfidenceIntervallStart();
                    confInterval=prefs.getPref_ConfidenceIntervallStart()+(tmpDifference/(double)this.numberOfPhases)*(double)phaseCounter;
                    confInterval=Math.min(Math.round(confInterval),prefs.getPref_ConfidenceIntervallEnd());
                    confInterval=Math.max(Math.round(confInterval),prefs.getPref_ConfidenceIntervallStart());
                    
                    //MaxRelError goes from 15 to 1 (max to min)
                    tmpDifference=prefs.getPref_MaxRelErrorEnd()-prefs.getPref_MaxRelErrorStart();//will be negative
                    maxRelError=prefs.getPref_MaxRelErrorStart()+(tmpDifference/(double)this.numberOfPhases)*(double)phaseCounter;
                    maxRelError=Math.min(Math.round(maxRelError),prefs.getPref_MaxRelErrorStart());
                    maxRelError=Math.max(Math.round(maxRelError),prefs.getPref_MaxRelErrorEnd());
                    
                    //Internal parameter can go up or down
                    tmpDifference=prefs.getPref_InternalParameterEnd()-prefs.getPref_InternalParameterStart();//will be negative
                    internal=prefs.getPref_InternalParameterStart()+(tmpDifference/(double)this.numberOfPhases)*(double)phaseCounter;
                    internal=Math.min(Math.round(internal), Math.max(prefs.getPref_InternalParameterStart(), prefs.getPref_InternalParameterEnd())   );
                    internal=Math.max(Math.round(internal), Math.min(prefs.getPref_InternalParameterStart(), prefs.getPref_InternalParameterEnd())   );
                    
                    }else{
                    //phaseCounter=this.numberOfPhases!
                    //Last phase, so we use the end-values for all parameters
                    confInterval=prefs.getPref_ConfidenceIntervallEnd();
                    maxRelError=prefs.getPref_MaxRelErrorEnd();
                    internal=prefs.getPref_InternalParameterEnd();
                    }
                
                }
                support.log("In phase "+(phaseCounter+1) +" will set Confinterval:"+confInterval+", maxRelError:"+maxRelError+", internalParamter:"+internal);
                if(iterateConfidenceInterval){
                    support.getParameterByName(lastParamaterset, "ConfidenceIntervall").setValue(confInterval);
                }
                if(iterateMaxRelError){
                    support.getParameterByName(lastParamaterset, "MaxRelError").setValue(maxRelError);
                }
                //TODO: Get Internal Parametername by Combobox
                /*if(iterateInternal){
                    support.getParameterByName(parameterBase, filename)
                }
                */
                
                
                myOptimizer=SimOptiFactory.getOptimizer(support.getOptimizerPreferences().getPref_typeOfUsedMultiPhaseOptimization() );
                myOptimizer.initOptimizer();
                myOptimizer.setLogFileName(this.logFileName);

                //Wait for Optimizer to end
                while(myOptimizer.getOptimum()==null){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        support.log("Problem waiting for Optimizer. (Multiphase)");
                    }
                }
                lastOptimizer=myOptimizer;
            }

            //Pop Startvalue-strategy
            support.getOptimizerPreferences().setPref_StartValue(myTmpStartValue);

            this.bestSolution=lastOptimizer.getOptimum();
            this.optimized=true;
            support.log(this.getClass().getSimpleName()+" has ended, printing optimal value:");
            support.addLinesToLogFile(bestSolution, logFileName);
            support.setStatusText("Optimization ended. See Log.");
            support.printOptimizedMeasures(bestSolution, this.listOfMeasures);
            StatisticAggregator.printStatistic(this.logFileName);
    }

    public void initOptimizer() {
    this.pathToTimeNet=support.getPathToTimeNet();// pathToTimeNetTMP;
    this.MeasureFormPane=support.getMeasureFormPane();//MeasureFormPaneTMP;
    this.parent=support.getMainFrame();// parentTMP;
    this.parameterBase=parent.getParameterBase();
    this.listOfMeasures=parent.getListOfActiveMeasureMentsToOptimize(); //((MeasurementForm)MeasureFormPane.getComponent(0)).getListOfMeasurements();
    support.log("# of Measures to be optimized: "+this.listOfMeasures.size());

    this.filename=support.getOriginalFilename();// originalFilename;

    this.tmpPath=support.getTmpPath();

    //support.getOptimizerPreferences().setPref_StartValue(typeOfStartValueEnum.middle);
    
    this.numberOfPhases=support.getOptimizerPreferences().getPref_NumberOfPhases();
    this.keepSizeAndResolutionOfDesignspace=support.getOptimizerPreferences().getPref_KeepDesignSpaceAndResolution();

    //Start this Thread
    new Thread(this).start();
    }

    
    /**
     * Returns the found optmium (SimulationType)
     * If optimium is not found or optimizer is still running it returns null
     * @return null if optimization not yet ended, else Optimum
     */
    public SimulationType getOptimum(){
        if(this.optimized){
        return this.bestSolution;
        }else{
        return null;
        }
    }
    /**
     * Set the logfilename
     * this is useful for multi-optimization or if you like specific names for your logfiles
     * @param name Name (path) of logfile
     */
    public void setLogFileName(String name){
    this.logFileName=name;
    }
    /**
     * Returns the used logfileName
     * @return name of logfile
     */
    public String getLogFileName() {
    return this.logFileName;
    }
}
