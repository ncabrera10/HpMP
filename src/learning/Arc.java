package learning;

public class Arc implements Comparable<Arc>{

	/**
	 * Holds a reference to the tail node of the arc
	 */
	private int tailID;
	
	/**
	 * Holds a reference to the head node of the arc
	 */
	private int headID;
	
	/**
	 * Holds a counter: how many times have this arc been used in the petals found by the sampling phase?
	 */
	private int counter;
	
	/**
	 * Distance associated with this arc
	 */
	
	private double distance;
	
	/**
	 * 
	 * @param tailNode a reference to the tail node of the arc
	 * @param headNode a reference to the head node of the arc
	 * @param dist distance of the arc
	 */
	public Arc(int tailID,int headID, double dist) {
		this.setTailID(tailID);
		this.setHeadID(headID);
		this.counter = 0;
		this.distance = dist;
	}

	/**
	 * This method increases the count by one
	 */
	public synchronized void increaseCounter() {
		this.counter++;
	}
	
	/**
	 * Compares to other arc
	 * @param otherArc
	 * @return -1 if the other arc has been used more, 1 if the arc has been used more, 0 if tied
	 */
	@Override
	public int compareTo(Arc otherArc) {
		return (this.counter < otherArc.counter ? -1 : (this.counter > otherArc.counter ? 1 : 0));
	}

	public int getTailID() {
		return tailID;
	}

	public void setTailID(int tailID) {
		this.tailID = tailID;
	}

	public int getHeadID() {
		return headID;
	}

	public void setHeadID(int headID) {
		this.headID = headID;
	}
	
	
	
	/**
	 * @return the counter
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}

	@Override
	public String toString() {
		return tailID+","+headID;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
}
