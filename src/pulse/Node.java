package pulse;

import java.util.ArrayList;
import java.util.Random;

import globalParameters.GlobalParameters;


/**
 * Defines the interface to objects that represent a node
 * @author nicolas.cabrera-malik
 *
 */
public abstract class Node {

	//Attributes:
	
	public int id; //Id of the node
	
	public int minArcs; // Lower bound on the number of arcs needed to reach the end node.
	
	public double minCost; //Lower bound on the cost to the end node.
	
	public boolean firstTime; //First time that the node is pulsed?
	
	public ArrayList<Label> labels; //Set of non-dominated labels to enforce dominance
	 
	public ArrayList<Arc> backwardStar; //Incoming arcs to the node.
	
	public PulseGraph graph;
	
	// Abstract methods:
	
	public abstract void pulse(double cost, int numberOfArcs, ArrayList<Integer> path);
	
	// Methods:
	
	/**
	 * This method verifies dominance
	 * @param PTime
	 * @param PDist
	 * @return true if the label is dominated
	 */
	public boolean checkLabels(double cost, int numberOfArcs)
	// Label pruning strategy
	{
		for (int i = 0; i < labels.size(); i++) {
			if (labels.get(i).dominateLabel(cost,numberOfArcs)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method modifies the labels.
	 * @param cost cost of the new label
	 * @param numberOfArcs number of arcs of the new label
	 */
	public void changeLabels(double cost, int numberOfArcs) {
		if(labels.size() == 0) {
			labels.add(new Label(cost,numberOfArcs));
		}else if(labels.size() == 1) {
			labels.add(cost < labels.get(0).getCost() ? 0 : 1, new Label(cost,numberOfArcs));
		}else if(labels.size() == 2) {
			labels.add(numberOfArcs < labels.get(1).getNumberOfArcs() ? 0 : 1, new Label(cost,numberOfArcs));
		}else {
			if(labels.size() < GlobalParameters.SPLIT_PULSE_NUM_LABELS) {
				insertLabel1(cost,numberOfArcs);
			}else {
				if(GlobalParameters.SPLIT_PULSE_NUM_LABELS > 2) {
					Random r = new Random();
					int luck = r.nextInt(GlobalParameters.SPLIT_PULSE_NUM_LABELS-2)+2;
					labels.remove(luck);
					insertLabel1(cost,numberOfArcs);
				}
			}
		}
	}
	
	
	/**
	 * This method inserts a label based on their time
	 * @param cost cost of the label
	 * @param numberOfArcs number of arcs of the label
	 */
	private void insertLabel1(double cost, int numberOfArcs) {
		Label np = new Label(cost, numberOfArcs);
		double cScore = np.getCost();
		
		boolean cond = true;
		int l = 0;
		int r = labels.size();
		int m = (int) ((l + r) / 2);
		double mVal = 0;
		if (labels.size() == 0) {
			labels.add(np);
			return;
		} else if (labels.size() == 1) {
			mVal = labels.get(m).getCost();
			labels.add(cScore < mVal ? 0 : 1, np);
			return;
		} else {
			mVal = labels.get(m).getCost();
		}

		while (cond) {
			if (r - l > 1) {
				if (cScore < mVal) {
					r = m;
					m = (int) ((l + r) / 2);
				} else if (cScore > mVal) {
					l = m;
					m = (int) ((l + r) / 2);
				} else {
					labels.set(m, np);
					return;
				}
				mVal = labels.get(m).getCost();
			} else {
				cond = false;
				if (l == m) {
					labels.add(cScore < mVal ? l : l + 1, np);
				} else if (r == m) {
					labels.add(cScore < mVal ? r : Math.min(r + 1, labels.size()), np);
				} else {
					System.err.println(Node.class +  " insert label, error");
				}
				return;

			}
		}
		
	}
	
	/**
	 * Adds an incoming arc (In order)
	 * @param a the arc
	 */
	public void addIncomingArc(Arc a){
		double cScore = a.getCost();
		
		boolean cond = true;
		int l = 0; //Por izquierda
		int r = backwardStar.size();
		int m = (int)((l+r)/2);
		double mVal = 0;
		if(backwardStar.size() == 0){
			backwardStar.add(a);
			return;
		}
		else if(backwardStar.size() == 1){
			mVal = backwardStar.get(m).getCost();
			if(cScore <= mVal){
				backwardStar.add(a);
			}else{
				backwardStar.add(0, a);
			}
			return;
		}
		else{
			mVal = backwardStar.get(m).getCost();
		}
		while(cond){
			if(r-l>1){
				if(cScore < mVal){
					r = m;
					m = (int)((l+r)/2);
				}
				else if(cScore > mVal){
					l = m;
					m = (int)((l+r)/2);
				}else{
					backwardStar.add(m,a);
					return;
				}
				mVal = backwardStar.get(m).getCost();
			}
			else{
				cond = false;
				if(l == m){
					if(cScore == mVal){
						backwardStar.add(l+1,a);
					}else{
						backwardStar.add(cScore<mVal?l:l+1, a);
					}
				}else if(r == m){
					if(cScore == mVal){
						backwardStar.add(r+1,a);
					}else{
						backwardStar.add(cScore<mVal?r:Math.min(r+1,backwardStar.size()), a);
					}
				}
			}
		}
	}
	
	public String toString() {
		return (""+id);
	}
	
	/**
	 * This method sorts the incoming arcs
	 * @param set
	 */
	protected void Sort(ArrayList<Arc> set) 
	{
		QS(backwardStar, 0, backwardStar.size()-1);
	}
	
	/**
	 * 
	 * @param e
	 * @param b
	 * @param t
	 */
	public void QS(ArrayList<Arc> e, int b, int t)
	{
		 int pivote;
	     if(b < t){
	        pivote=colocar(e,b,t);
	        QS(e,b,pivote-1);
	        QS(e,pivote+1,t);
	     }  
	}
	
	
	/**
	 * 
	 * @param e
	 * @param b
	 * @param t
	 * @return
	 */
	public int colocar(ArrayList<Arc> e, int b, int t)
	{
	    int i;
	    int pivote;
	    double valor_pivote;
	    Arc temp;
	    pivote = b;
	    valor_pivote = e.get(pivote).getSort_criteria()+e.get(pivote).getTail().minCost;
	    for (i=b+1; i<=t; i++){
	        if (e.get(i).getSort_criteria()+e.get(pivote).getTail().minCost < valor_pivote){
	                pivote++;    
	                temp= e.get(i);
	                e.set(i, e.get(pivote));
	                e.set(pivote, temp);
	        }
	    }
	    temp=e.get(b);
	    e.set(b, e.get(pivote));
        e.set(pivote, temp);
	    return pivote;
	    
	}
}
