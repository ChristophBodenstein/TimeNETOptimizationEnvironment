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
import timenetexperimentgenerator.typedef;
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
private double D;
private double c;
double actualTempParameter=1;
double actualTempCost=1;


    /**
     * Constructor
     *
     */
    public OptimizerSimAnnealing() {
    super();
    D=this.getListOfChangableParameters(support.getMainFrame().getParameterBase()).size();//Number of changeable parameters
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
    
    
    //Inc Simulationcounter
    generated++;

    //Calculate the new Temperatures
    switch(support.getOptimizerPreferences().getPref_Cooling()){
        default:
        case Boltzmann:
            actualTempParameter = (1/(Math.log((double)generated)))*support.getOptimizerPreferences().getPref_MaxTempParameter();
            actualTempCost = (1/(Math.log((double)generated)))*support.getOptimizerPreferences().getPref_MaxTempCost();
            break;
            
        case FastAnnealing:
            actualTempParameter = (1/(double)generated)*support.getOptimizerPreferences().getPref_MaxTempParameter();
            actualTempCost = (1/(double)generated)*support.getOptimizerPreferences().getPref_MaxTempCost();
            break;
            
        case VeryFastAnnealing:
            actualTempParameter=Math.exp(-c*Math.pow((double)generated,1/D) )*support.getOptimizerPreferences().getPref_MaxTempParameter();
            actualTempCost=Math.exp(-c*Math.pow((double)generated,1/D) )*support.getOptimizerPreferences().getPref_MaxTempCost();
            //Eject if Temperature is lower then Epsilon
            if(actualTempCost< support.getOptimizerPreferences().getPref_Epsilon()){return true;}
            if(actualTempParameter< support.getOptimizerPreferences().getPref_Epsilon()){return true;}
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
     * You should overload this method in your child-classes
     * @param actualParameterset  actual parameterset, if null, then first parameterset is calculated
     * @return next parameterset to be simulated
     */
    @Override
    protected ArrayList<parameter> getNextParameterset(ArrayList<parameter> actualParameterset){
    ArrayList<parameter> newParameterset=support.getCopyOfParameterSet(parameterBase);
    ArrayList<parameter> listOfChangableParameters=this.getListOfChangableParameters(newParameterset);
    //Count the number of changable parameters
    this.numberOfChangableParameters=listOfChangableParameters.size();

    support.log("TempParameter:"+actualTempParameter + " and TempCost:"+actualTempCost);
        for(int i=0;i<listOfChangableParameters.size();i++){

            parameter p=listOfChangableParameters.get(i);
            double sign=1;
            double distanceMax=p.getEndValue()-p.getStartValue();
            double r=0;

            double nextValue=p.getEndValue()+1;
            double simpleValue=p.getEndValue()+1;


            //r=-0.99;

            while(nextValue<p.getStartValue() || nextValue>p.getEndValue()){
            //while(r<1){
            r=Math.random();
            r=1-(r*2);
            r=r+0.01;
            
            sign=Math.signum(r);

            //Calculation of Standard nextValue
            //nextValue = p.getValue() + sign * actualTempParameter *(Math.pow(1+(1/actualTempParameter),Math.abs(2*r-1) )) * distanceMax;
            nextValue = p.getValue() + sign * actualTempParameter *(Math.pow(1+(1/actualTempParameter),Math.abs(2*r)-1 )) * distanceMax;
            //support.log("Min:"+p.getStartValue()+" Max:"+p.getEndValue()+" NextValue:"+nextValue);
            /*
            support.log("xCurrent:"+p.getValue());
            support.log("sign:"+sign);
            support.log("r:"+r);
            support.log("Tpar:"+actualTempParameter);
            support.log("Pow_Down:"+(1+(1/actualTempParameter)));
            support.log("Pow_Up:"+(Math.abs(2*r)-1));
            support.log("d:"+distanceMax);
            support.log("NextValue:"+nextValue);
            */
            /*try{
            Thread.sleep(80);
            }catch(Exception e){}
            */
            
            //Calculation of simple nextValue
            double range=(p.getEndValue()-p.getStartValue());
                            simpleValue=Math.round(Math.random()*range*actualTempParameter);
                                if(Math.random()>=0.5){
                                simpleValue=Math.min(p.getValue()+simpleValue,p.getEndValue());
                                }else{
                                simpleValue=Math.max(p.getValue()-simpleValue,p.getStartValue());
                                }

            double stepCount=(p.getEndValue()-p.getStartValue())/p.getStepping();


            
                switch(support.getOptimizerPreferences().getPref_CalculationOfNextParameterset()){
                    default:
                        //Do nothing
                        break;
                    case Stepwise:
                        nextValue=Math.round(nextValue/stepCount) * p.getStepping();
                        break;
                    case Standard:
                        //Do nothing
                        break;
                    case Simple:
                        nextValue=simpleValue;
                        break;
                    case SimpleStepwise:
                        nextValue=Math.round(simpleValue/stepCount) * p.getStepping();
                        break;

                }
            }
 
            p.setValue(nextValue);
            support.log("Setting Parameter "+ p.getName() + " to Value "+p.getValue()+".");

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
