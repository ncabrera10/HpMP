package pulse;

import java.util.ArrayList;

/**
 * This class represents an intermediate node in the pulse graph. 
 * @author nicolas.cabrera-malik
 *
 */
public class IntermediateNode extends Node{

	// Constructor
	
	public IntermediateNode(int i, PulseGraph graph) {
		this.id = i;
		this.minCost = Double.MAX_VALUE;
		this.backwardStar = new ArrayList<Arc>();
		this.labels = new ArrayList<Label>();
		this.firstTime = true;
		this.graph = graph;
	}
		
		
	// Methods
		
	@Override
	public void pulse(double cost, int numberOfArcs, ArrayList<Integer> path) {
		
		// Sort the incoming arcs:
		
		if(this.firstTime) {
			this.firstTime = false;
			this.Sort(backwardStar);
		}

		// Update the labels on this node:
		
		this.changeLabels(cost, numberOfArcs);
		
		// Update the path:
		
		path.add(this.id);
		
		
		// Iterate over all the incoming arcs:
		
		for(Arc arc : this.backwardStar) {
			
			// Update the weights:
			
				double newCost = cost + arc.getCost();
				int newNumberOfArcs = numberOfArcs + 1;
				
			// Check feasibility:
				
				if(newNumberOfArcs + arc.getTail().minArcs <= this.graph.numRingsRequired) {
					
					// Check pruning by bounds:
					
					if(newCost + arc.getTail().minCost < this.graph.primalBound) {
						
						// Check pruning by dominance:
						
						if(!arc.getTail().checkLabels(newCost, newNumberOfArcs)) {
							
							// Pulse the tail node:
							
								arc.getTail().pulse(newCost, newNumberOfArcs, path);
							
						}
						
					}
					
				}
			
			
		}
		
		
		// Remove the node from the path:
		
		path.remove(path.size()-1);
		
	}	
}
