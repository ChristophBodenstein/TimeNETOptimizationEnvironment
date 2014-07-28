/*
 * Optimizer using the simulated annealing algorithm
 * It´s a child of OptimizerHill
 *
 * Christoph Bodenstein
 * TU-Ilmenau, FG SSE
 */

package timenetexperimentgenerator.optimization;

import timenetexperimentgenerator.typedef.*;

/**
 *
 * @author Christoph Bodenstein
 */
public class OptimizerSimAnnealing extends OptimizerHill implements Runnable, Optimizer{
private int SimI=1,SimT=0;
private double maxTemp=20;
private int stepCountTemp=100;
private double Fx;//Current Distance
private double Fy;//New Distance?




    /**
     * Constructor
     *
     */
    public OptimizerSimAnnealing() {
    super();
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
