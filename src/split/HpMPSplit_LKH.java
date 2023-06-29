package split;

import java.util.ArrayList;

import core.DistanceMatrix;
import core.JVRAEnv;
import core.Route;
import core.RouteAttribute;
import core.Solution;
import core.Split;
import core.TSPSolution;
import core.VRPSolution;
import lkh.LKH;

/**
 * Implements a basic split procedure. 
 * 
 */
public class HpMPSplit_LKH implements Split{

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
	public HpMPSplit_LKH(DistanceMatrix distances, int m, int M, int r) {
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
					if(V[i-1] + cost < V[j]){
						V[j] = V[i-1] + cost;
						P[j] = i-1;
					}
				}	
				
				j++;
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
		//EXO 2: try to write this algorithm
		//Initialize the solution
		VRPSolution s=new VRPSolution();
		double of=0;
		int head=P.length-1; //The head of the arc representing the last route
		int  nodesToRoute=P.length-1;
		while (nodesToRoute>0){
			int tail=P[head]+1; //The tail of the arc representing the route being currently built\
			
			//Try to improve the current route with the LKH:

				// Create an arraylist with the nodes in the route:
			
					ArrayList<Integer> tsp_array = new ArrayList<Integer>();
					for(int i=tail; i<=head;i++){
						tsp_array.add(tsp.get(i));
					}
					
					
				// Create an LKH object:
					
					LKH lkh = new LKH(distances,tsp_array);
					
				// Initialze the total distance:
					
					double iniCost = lkh.getDistance();
				
				// Run the algorithm:
					
					lkh.runAlgorithm();
					
				// Get the new total distance:
					
					double newCost = lkh.getDistance();
					
					
				// If the distance is better, then we can use this tour instead:
					
				if(newCost < iniCost) {
					//Initialize a new route
					Route r=JVRAEnv.getRouteFactory().buildRoute();
					r.add(tsp.get(0));
					double load=0;
					for(int i=0; i<lkh.tour.length;i++){ //Build the route
						int node = lkh.tour[i];
						r.add(node);
						load += 1;
						nodesToRoute--;
					}
					r.add(tsp.get(0));
					double cost=newCost;
					r.setAttribute(RouteAttribute.COST,cost);
					of+=cost;
					r.setAttribute(RouteAttribute.LOAD, load);
					s.addRoute(r);
					head=P[head]; //The head of the arc representing the next route to build
				}
				else { //Otherwise we just keep the previous one
					//Initialize a new route
					Route r=JVRAEnv.getRouteFactory().buildRoute();
					r.add(tsp.get(0));
					double load=0;
					for(int i=tail; i<=head;i++){ //Build the route
						int node = tsp.get(i);
						r.add(node);
						load += 1;
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
	
			
		}
		if(s.getRoutes().size() == this.ri) {
			s.setOF(of);
		}else {
			s.setOF(of*100);
		}	
		return s;
	}
}
