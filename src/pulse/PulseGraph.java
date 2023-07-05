package pulse;

import java.util.ArrayList;

/**
 * This class represents the graph used to solve the pulse algorithm.
 * It contains relevant global information as the primal bound and the final path.
 * The pulse will be executed in the backwards direction.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class PulseGraph {

	// Attributes:
	
	public int numRingsRequired; //Number of rings in a solution: defined by the instance
	
	public double primalBound; //Current cost of the best solution
	
	public int numRingsStar; //Number of rings in the best solution found so far.
	
	public ArrayList<Integer> Path; //Path associated to the best solution
	
	public ArrayList<Node> nodes; //Nodes in the graph. 0: end node. 1 to nodes.size()-2: intermediate nodes. nodes.size()-1: start node.

	
	// Constructor:
	
	public PulseGraph(int numR) {
		this.numRingsRequired = numR;
		this.primalBound = Double.MAX_VALUE;
		this.nodes = new ArrayList<Node>();
		this.Path = new ArrayList<Integer>();
	}
	
	
	
}
