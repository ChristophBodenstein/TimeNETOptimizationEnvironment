/*
 * Optimizer using the simulated annealing algorithm
 * It´s a child of OptimizerHill
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.optimization;

import java.util.ArrayList;
import timenetexperimentgenerator.datamodel.SimulationType;
import timenetexperimentgenerator.datamodel.parameter;
import timenetexperimentgenerator.support;
import timenetexperimentgenerator.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class OptimizerSimAnnealing extends OptimizerHill implements Runnable, Optimizer{
private int SimI=1,SimT=0;
private double maxTemp=20;
private int stepCountTemp=100;

private double TempCost=1.0, TempPara =1.0;
private int accepted =0, generated=0;
private double D=this.getListOfChangableParameters().size();//Number of changeable parameters
private double c;


    /**
     * Constructor
     *
     */
    public OptimizerSimAnnealing() {
    super();
    c=-Math.log(support.getOptimizerPreferences().getPref_TRatioScale());
    c=c*Math.exp(-Math.log(support.getOptimizerPreferences().getPref_TAnnealScale())/this.D);
    }




    /**
     * Check, if next Solution is better. Calculate if Optimization is done or not
     * Reset wrongSolutionCounter if solution is better
     * Reset wrongSolutionPerDirectionCounter if solution is better
     * Return true if wrongSolutionCounter is down
     *
     */
    @Override
    protected boolean isOptimized(double actualDistance, double nextDistance){
    //If next Solution is better then take it as actual best solution
    double actualTempParameter=1;
    double actualTempCost=1;
    
    //Inc Simulationcounter
    generated++;

    //Calculate the new Temperatures
    switch(support.getOptimizerPreferences().getPref_Cooling()){
        default:
        case Boltzmann:
            //break; TODO Implement and remove comment
        case FastAnnealing:
            //break; TODO Implement and remove comment
        case VeryFastAnnealing:
            actualTempParameter=Math.exp(-c*Math.pow((double)generated,1/D) )*support.getOptimizerPreferences().getPref_MaxTempParameter();
            actualTempCost=Math.exp(-c*Math.pow((double)generated,1/D) )*support.getOptimizerPreferences().getPref_MaxTempCost();
            //Eject if Temperature is lower then Epsilon
            if(actualTempCost< support.getOptimizerPreferences().getPref_Epsilon())return true;
            if(actualTempParameter< support.getOptimizerPreferences().getPref_Epsilon())return true;
            break;
    }       

    //If new cost is lower then repvious then break and accept new solution
        if(getActualDistance(nextSolution) < getActualDistance(bestSolution)){
        bestSolution=nextSolution;
        
        double delta=getActualDistance(nextSolution)-getActualDistance(currentSolution);

            if((delta<0) || (Math.random() < (Math.exp(-delta/actualTempCost) ) ) ){
            currentSolution=nextSolution;
            accepted++;
            }        
        }
    return false;//Go back and loop again
    }



    /**
     * Returns the next parameterset
     * Next parameterset is chosen randomly within the neighborhood
     * You should overload this method in your child-classes
     * @param actualParameterset  actual parameterset, if null, then first parameterset is calculated
     * @return next parameterset to be simulated
     */
    @Override
    protected ArrayList<parameter> getNextParameterset(ArrayList<parameter> actualParameterset){
    ArrayList<parameter> newParameterset=support.getCopyOfParameterSet(parameterBase);
    ArrayList<parameter> listOfChangableParameters=this.getListOfChangableParameters();
    //Count the number of changable parameters
    this.numberOfChangableParameters=listOfChangableParameters.size();

        newParameterset=support.getCopyOfParameterSet(actualParameterset);
//DUMMY
        //TODO Implement the standard and random stuff
        switch(support.getOptimizerPreferences().getPref_CalculationOfNextParameterset()){
            default:
            case Random:
                break;
            case Standard:
                break;

        }


        return newParameterset;
        

    }



    /**
     * Get probability for chosing the actual parameterset as next solution
     * @return probaility that actual Solution Fy is chosen as the next Fx
     */
    private double getProbabylity(double Fy, double Fx){
    return Math.exp( -(Fy-Fx)/getNextTemperature(SimT) );
    }

    

    /**
     * Calculates the next temperature from max to min
     * The higher SimT is, the lower the temperature is
     */
    private double getNextTemperature(int t){
        if(t>=stepCountTemp-1){
            return 0.001;
        }else{
        
        return (maxTemp - (maxTemp/stepCountTemp)*t);
        }
    }

}
