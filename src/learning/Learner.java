package learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import core.ArrayDistanceMatrix;
import globalParameters.GlobalParameters;

public class Learner {

	/**
	 * List of all the arcs in the graph
	 */
	
	private ArrayList<Arc> arcs;
	
	/**
	 * Hashtable to directly access any arc:
	 */
	
	private Hashtable<String,Arc> arcs_map;
	
	/**
	 * Reference to the distance matrix
	 */
	
	private ArrayDistanceMatrix distances;
	
	/**
	 * Value of the dual variables associated with each node + the dual of the # rings constraint
	 */
	
	private double[] duals;
	
	/**
	 * This method creates a learner
	 * @param distances Distance matrix of the current instance
	 * @param n Number of nodes in the instance
	 */
	public Learner(ArrayDistanceMatrix distances) {
		
		this.distances = distances;
		this.createArcs(distances.size());
		duals = new double[distances.size()+1];
		for(int i=0;i<duals.length;i++) {
			duals[i] = 0.0;
		}
	}
	
	/**
	 * This method creates the arc list and the arc map
	 * @param n
	 */
	public void createArcs(int n) {
		
		// Initialize the list of arcs
		
		arcs = new ArrayList<Arc>();
		arcs_map = new Hashtable<String,Arc>();
				
		// Populate the list of arcs:
		
		for(int i=0; i<n; i++) {
			
			for(int j=0; j<n; j++) {
				
				// Create the arc:
				
				Arc arc = new Arc(i,j,distances.getDistance(i, j));
				
				// Adds the arc to the list:
				
				arcs.add(arc);
				
				// Adds the arc to the map:
				
				arcs_map.put(i+"-"+j, arc);
				
			}
			
		}
		
	}
	
	/**
	 * This method sorts the arcs based on the number of times they have been used in the petals found in the sampling phase
	 * 
	 */
	public void findCriticalArcs() {
		
		// Sort the list of arcs:
		
		Collections.sort(arcs);
		
		// Reset the arcs:
		
		for(Arc arc : arcs) {
			arc.setCounter(0);
		}
		
	}
	
	/**
	 * This method forbids the top % arcs by setting their distance to postive infinity
	 * 
	 */
	public void forbidArcs() {
		
		// How many arcs we will forbid:
		
		int toForbid = GlobalParameters.MSH_NUMBER_FORBID_ARCS;
		
		// Iterate over the top % decided by the user
		
		for(int i = arcs.size()-1; i >= arcs.size() - toForbid; i--) {
			
			Arc arc = arcs.get(i);
			int tail = arc.getTailID();
			int head = arc.getHeadID();
			distances.setDistance(tail, head, arc.getDistance() * 10);
			distances.setDistance(head, tail, arc.getDistance() * 10);
		
		}
		
	}
	
	/**
	 * This method corrects the distance of the arcs that were forbidden in the previous iteration
	 */
	public void allowArcs() {
		
		// How many arcs we will re-allow:
		
		int toForbid = GlobalParameters.MSH_NUMBER_FORBID_ARCS;
		
		// Iterate over the top % decided by the user
		
		for(int i = arcs.size()-1; i > arcs.size() - toForbid; i--) {
			
			Arc arc = arcs.get(i);
			int tail = arc.getTailID();
			int head = arc.getHeadID();
			distances.setDistance(tail, head, arc.getDistance());
			distances.setDistance(head, tail, arc.getDistance());
			
		}
		
	}

	/**
	 * @return the arcs
	 */
	public ArrayList<Arc> getArcs() {
		return arcs;
	}

	/**
	 * @param arcs the arcs to set
	 */
	public void setArcs(ArrayList<Arc> arcs) {
		this.arcs = arcs;
	}

	/**
	 * @return the arcs_map
	 */
	public Hashtable<String, Arc> getArcs_map() {
		return arcs_map;
	}

	/**
	 * @param arcs_map the arcs_map to set
	 */
	public void setArcs_map(Hashtable<String, Arc> arcs_map) {
		this.arcs_map = arcs_map;
	}

	/**
	 * @return the distances
	 */
	public ArrayDistanceMatrix getDistances() {
		return distances;
	}

	/**
	 * @param distances the distances to set
	 */
	public void setDistances(ArrayDistanceMatrix distances) {
		this.distances = distances;
	}

	/**
	 * @return the duals
	 */
	public double[] getDuals() {
		return duals;
	}

	/**
	 * @param duals the duals to set
	 */
	public void setDuals(double[] duals) {
		this.duals = duals;
	}
	
	public double getLastDual() {
		return duals[duals.length-1];
	}
	
}
