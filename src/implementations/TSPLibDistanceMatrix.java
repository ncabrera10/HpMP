package implementations;

import core.ArrayDistanceMatrix;
import tspReader.TSPLibInstance;

/**
 * This class implements an instance of a distance matrix for TSP lib instances.
 * 
 * @author nicolas.cabrera-malik
 */
public class TSPLibDistanceMatrix extends ArrayDistanceMatrix{

	/**
	 * Constructs the distance matrix
	 */
	
	public TSPLibDistanceMatrix(TSPLibInstance tspLibInstance) {
		
		super();
		
		// Number of nodes:
		
		int dimension = tspLibInstance.getDimension()+1; //0: will be the depot!
		
		// Initializes the distance matrix:
		
		double[][] distances = new double[dimension][dimension];
		
		// Fills the matrix:
		
			//Between customers:
		
			for(int i = 1; i < dimension; i++) {
				
				for(int j = 1; j < dimension; j++) {
					
					distances[i][j] = tspLibInstance.getDistanceTable().getDistanceBetween(i-1, j-1);
					
				}
				
			}
		
			// A dummy depot:
			
			for(int i = 1; i < dimension; i++) {
					
				distances[i][0] = 0.0;
					
			}
			
			for(int i = 1; i < dimension; i++) {
				
				distances[0][i] = 0.0;
					
			}
		
		// Sets the distance matrix:
		
		this.setDistances(distances);
	}
}
