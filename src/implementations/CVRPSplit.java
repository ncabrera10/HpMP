package implementations;

import core.Demands;
import core.DistanceMatrix;
import core.JVRAEnv;
import core.Route;
import core.RouteAttribute;
import core.Solution;
import core.Split;
import core.TSPSolution;
import core.VRPSolution;

/**
 * Implements a basic split procedure. The implementation assumes that the {@link TSPSolution} passed to method {@link #split(TSPSolution)} contains 
 * the depot at the begining and end of the route.
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 21, 2016
 *
 */
public class CVRPSplit implements Split {
	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	/**
	 * The customer demands
	 */
	private final Demands demands;
	/**
	 * The vehicle's capacity
	 */
	private final double Q;

	/**
	 * 
	 * @param distances
	 * @param demands
	 * @param Q
	 */
	public CVRPSplit(DistanceMatrix distances, Demands demands, double Q){
		this.distances=distances;
		this.demands=demands;
		this.Q=Q;
	}

	@Override
	public Solution split(TSPSolution r){

		//Initialize labels
		int[] P=new int[r.size()-1];			//The predecesor labels
		double[] V=new double[r.size()-1];		//The shortest path labels
		for(int i=1;i<r.size()-1;i++){
			V[i]=Double.MAX_VALUE;
		}
		
		//Build the auxiliary graph and find the shortest path at the same time
		for(int i=1; i<r.size(); i++){
			
			//Initilize auxiliary variables
			double load=0;
			double cost=0;	
			int j=i;
			
			//Explore all routes starting at node i
			while(load<=Q&&j<r.size()-1){
				//Compute metrics for the route: load and cost
				load+=demands.getDemand(r.get(j));
				if(i==j)
					cost=distances.getDistance(0, r.get(j))+distances.getDistance(r.get(j),0);
				else
					cost=cost-distances.getDistance(r.get(j-1),0)+distances.getDistance(r.get(j-1),r.get(j))+distances.getDistance(r.get(j),0);
				//Check the route's feasibility
				if(load<=Q){
					if(V[i-1]+cost<V[j]){
						V[j]=V[i-1]+cost;
						P[j]=i-1;
					}
					j++;
				}					
			}
		}

		return extractRoutes(P,V, r);
	}
	/**
	 * Extracts the routes from the labels, builds a solution, and evaluates the solution
	 * @param P the predecessors
	 * @param V the shortest path labels
	 * @param tsp the TSP tour
	 * @return a solution with the routes in the optimal partition of the TSP tour
	 */
	private VRPSolution extractRoutes(int[] P, double[] V, TSPSolution tsp){

		//Initialize the solution
		VRPSolution s=new VRPSolution();
		double of=0;
		int head=P.length-1; //The head of the arc representing the last route
		int  nodesToRoute=P.length-1;
		while (nodesToRoute>0){
			int tail=P[head]+1; //The tail of the arc representing the route being currently built
			//Initialize a new route
			Route r=JVRAEnv.getRouteFactory().buildRoute();
			r.add(tsp.get(0));
			double load=0;
			for(int i=tail; i<=head;i++){ //Build the route
				int node=tsp.get(i);
				r.add(node);
				load+=demands.getDemand(node);
				nodesToRoute--;
			}
			r.add(tsp.get(0));
			double cost=V[head]-V[P[head]];
			r.setAttribute(RouteAttribute.COST,cost);
			of+=cost;
			r.setAttribute(RouteAttribute.LOAD, load);
			s.addRoute(r);
			head=P[head]; //The head of the arc representing the next route to build
		}
		s.setOF(of);
		return s;
	}

}
