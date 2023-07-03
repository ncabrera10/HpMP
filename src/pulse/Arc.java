package pulse;

/**
 * This class represents an arc of the pulse graph.
 * @author nicolas.cabrera-malik
 *
 */
public class Arc {

	// Attributes:
	
	private Node tail; //Tail of the arc
	
	private Node head; //Head of the arc
	
	private double cost; //Cost of the arc
	
	private double sort_criteria; //Sort criteria: cost + minCost(tail)
	
	// Constructor:
	
	public Arc(Node t, Node h, double c) {
		
		this.tail = t;
		this.head = h;
		this.cost = c;
		this.sort_criteria = c;
		
		h.addIncomingArc(this); //Adds the incoming arc to the head node.
		
	}
	
	// Methods:
	
	@Override
	public String toString() {
		return tail.id+"-"+head.id;
	}

	// Getters and setters:
	
	/**
	 * @return the tail
	 */
	public Node getTail() {
		return tail;
	}

	/**
	 * @param tail the tail to set
	 */
	public void setTail(Node tail) {
		this.tail = tail;
	}

	/**
	 * @return the head
	 */
	public Node getHead() {
		return head;
	}

	/**
	 * @param head the head to set
	 */
	public void setHead(Node head) {
		this.head = head;
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
	 * @return the sort_criteria
	 */
	public double getSort_criteria() {
		return sort_criteria;
	}

	/**
	 * @param sort_criteria the sort_criteria to set
	 */
	public void setSort_criteria(double sort_criteria) {
		this.sort_criteria = sort_criteria;
	}
	
	
	
	
}
