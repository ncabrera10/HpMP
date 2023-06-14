package msh;

import core.JVRAEnv;
import core.OptimizationSense;
import core.OrderFirstSplitSecondHeuristic;
import core.Route;
import core.RoutePool;
import core.Solution;
import core.VRPSolution;

public class OrderFirstSplitSecondSampling implements SamplingFunction{

	private final OrderFirstSplitSecondHeuristic h;
	private int nSamples=1;	//Default value = 1
	private int samplesDrawn=0;
	private RoutePool pool=null;
	private OptimizationSense sense=JVRAEnv.getOptimizationSense();
	private final int numberOfRings;
	
	public OrderFirstSplitSecondSampling(OrderFirstSplitSecondHeuristic h,int nSamples,int nR){
		this.h=h;
		this.nSamples=nSamples;
		this.numberOfRings=nR;
	}

	@Override
	public Solution call() throws Exception {
		if(pool==null)
			throw new IllegalStateException("The route pool has not been set up");
		//Set up variables
		VRPSolution s, best=null;
		int i;
		//Sampling
		for(i=1;i<=this.nSamples;i++){
			s=(VRPSolution)h.run();
			for(Route r:s.getRoutes()){
				pool.add(r);
			}
			this.samplesDrawn++;
			//Update the best bound
			if(best==null) //&& s.getRoutes().size() == numberOfRings //TODO: INCLUDE THIS.
				best=s;
			else if(this.sense==OptimizationSense.MINIMIZATION&&s.getOF()<best.getOF()
					||this.sense==OptimizationSense.MAXIMIZATION&&s.getOF()>best.getOF())
				best=s;
		}
		return best;
	}

	@Override
	public void setNumberOfSamples(int samples) {
		this.nSamples=samples;		
	}

	@Override
	public int getNuberOfDrawnSamples() {
		return this.samplesDrawn;
	}

	@Override
	public void setRoutePool(RoutePool pool) {
		this.pool=pool;
	}

}
