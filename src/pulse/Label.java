package pulse;

/**
 * This class represents a label. Inside each label we store the cost and the number of arcs that have been used.
 * @author nick0
 *
 */
public class Label {

	// Attributes:
	
	private double cost;
	
	private int numberOfArcs;
	
	// Constructor
	
	/**
	 * This method creates a new Label
	 * @param c
	 * @param numA
	 */
	public Label(double c, int numA) {
		this.cost = c;
		this.numberOfArcs = numA;
	}
	
	// Methods:
	
	/**
	 * This method checks if the label dominates the new label that enters as an input.
	 * The new label is dominated if it has a larger cost and a larger number of arcs.
	 * @param Lp_cost
	 * @param Lp_numA
	 * @return
	 */
	public boolean dominateLabel(double Lp_cost, int Lp_numA) {
		
		// If the cost is larger, this label does not dominate the new label:
		
		if(cost > Lp_cost) {
			return false;
		}
		
		// If the number of arcs is larger, this label does not dominate the new label:
		
		if(numberOfArcs > Lp_numA) {
			return false;
		}
		
		return true;
		
	}
	
	@Override
	public String toString() {
		return cost+"-"+numberOfArcs;
	}

	/**
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * @return the numberOfArcs
	 */
	public int getNumberOfArcs() {
		return numberOfArcs;
	}

	/**
	 * @param numberOfArcs the numberOfArcs to set
	 */
	public void setNumberOfArcs(int numberOfArcs) {
		this.numberOfArcs = numberOfArcs;
	}
	
	
}
