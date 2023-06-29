package split;

import core.DistanceMatrix;
import core.JVRAEnv;
import core.Route;
import core.RouteAttribute;
import core.Solution;
import core.Split;
import core.TSPSolution;
import core.VRPSolution;

/**
 * Implements a basic split procedure. 
 * 
 */
public class HpMPSplit_ALL implements Split{

	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	
	/**
	 * The maximum number of nodes in a ring 
	 */
	private final int m;
	
	/**
	 * The minimum number of nodes in a ring 
	 */
	private final int M;
	
	/**
	 * The number of desired rings
	 */
	private final int ri;
	
	
	/**
	 * This method creates a new instance of the HpMP split algorithm
	 * @param distances
	 * @param m
	 * @param M
	 */
	public HpMPSplit_ALL(DistanceMatrix distances, int m, int M, int r) {
		this.distances = distances;
		this.m = m;
		this.M = M;
		this.ri = r;
	}
	
	@Override
	public Solution split(TSPSolution r) {
		
		//Initialize labels
		int[] P=new int[r.size()-1];			//The predecesor labels
		double[] V=new double[r.size()-1];		//The shortest path labels
		for(int i=1;i<r.size()-1;i++){
			V[i]=Double.MAX_VALUE;
		}
		
		//Initialize pool of routes:
		
		VRPSolution s=new VRPSolution();
		double of = 0.0;
		
		//Build the auxiliary graph and find the shortest path at the same time
		for(int i=1; i<r.size(); i++){
			
			//Initilize auxiliary variables
			double load=0;
			double cost=0;	
			int j=i;
			
			//Explore all routes starting at node i
			while(load <= M && j < r.size()-1){
				//Compute metrics for the route: load and cost
				load+=1;
				if(i==j)
					cost = 0;
				else
					cost = cost + distances.getDistance(r.get(j-1),r.get(j)) + distances.getDistance(r.get(j),r.get(i)) - distances.getDistance(r.get(j-1),r.get(i));
				//Check the route's feasibility
				if(load >= m && load <= M){
					
					// Adds the route to the pool:
					Route route = JVRAEnv.getRouteFactory().buildRoute();
					route.add(r.get(0));
					for(int k=i; k<=j; k++) {
						route.add(r.get(k));
					}
					route.add(r.get(0));
					route.setAttribute(RouteAttribute.COST,cost);
					route.setAttribute(RouteAttribute.LOAD, load);
					s.addRoute(route);
					of+=cost;
					if(V[i-1] + cost < V[j]){
						V[j] = V[i-1] + cost;
						P[j] = i-1;
					}
				}	
				j++;
			}
		}
		s.setOF(of);
		return s;
	}


}
