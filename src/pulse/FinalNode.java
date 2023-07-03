package pulse;

import java.util.ArrayList;

/**
 * This class represents the final node in the pulse graph (the first node in the split graph)
 * @author nicolas.cabrera-malik
 *
 */
public class FinalNode extends Node{

	// Constructor
	
	public FinalNode(int i, PulseGraph graph) {
		this.id = i;
		this.minCost = Double.MAX_VALUE;
		this.backwardStar = new ArrayList<Arc>();
		this.labels = new ArrayList<Label>();
		this.firstTime = true;
		this.graph = graph;
	}
	
	// Methods:
	
	@Override
	public void pulse(double cost, int numberOfArcs, ArrayList<Integer> path) {
	
		// Check feasibility:
		
		if(numberOfArcs == graph.numRingsRequired) {
			
			// Check the primal bound:
			
			if(cost < graph.primalBound) {
				
				// Updates the primal bound:
				
					graph.primalBound = cost;
				
				// Updates the path:
					
					// Clears the path:
					
						graph.Path.clear();
					
					// Adds all the nodes to the path:
						
						for(int i=0;i<path.size();i++) {
							graph.Path.add(path.get(i));
						}
					
					// Adds the final node to the path:
						
						graph.Path.add(0);
					
					
			}
		}
		
	}
}
