package implementations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import core.ArrayDistanceMatrix;
import util.EuclideanCalculator;

/**
 * This class implements an instance of a distance matrix.
 * 
 * @author nicolas.cabrera-malik
 */
public class RandomDistanceMatrix extends ArrayDistanceMatrix{

	/**
	 * Constructs the distance matrix
	 * @throws IOException 
	 */
	
	public RandomDistanceMatrix(String path) throws IOException {
		
		super();
		
		// Read the coordinates of the nodes:
		
			//0. Creates a buffered reader:
			
			BufferedReader buff = new BufferedReader(new FileReader(path+".txt"));
		
			//1. Skips the first line:
			
			String line = buff.readLine();
			
			//2. Read the coordinates of each node:
			
				//2. Initializes all list:
				
					ArrayList<Double> xCoors = new ArrayList<Double>();
					ArrayList<Double> yCoors = new ArrayList<Double>();
					ArrayList<Integer> nodes = new ArrayList<Integer>();
				
				// 3. Reades the 2 second line .. and iterates until there are no more nodes
				
					line = buff.readLine();
					line = buff.readLine();
					line = buff.readLine();
					line = buff.readLine();
					line = buff.readLine();
					line = buff.readLine();
					line = buff.readLine();
					line = buff.readLine();
					
					while(line != null) {
						String[] attrs = line.split(";");
						nodes.add(Integer.parseInt(attrs[0]));
						xCoors.add(Double.parseDouble(attrs[1]));
						yCoors.add(Double.parseDouble(attrs[2]));
						line = buff.readLine();
						
					}
			
					buff.close();
					
			
		// Number of nodes:
		
		int dimension = nodes.size()+1; //0: will be the dummy depot!
		
		// Initializes the distance matrix:
		
		double[][] distances = new double[dimension][dimension];
		
		// Fills the matrix:
		
			//Between customers:
		
			EuclideanCalculator euc = new EuclideanCalculator();
			for(int i = 1; i < dimension; i++) {
				
				for(int j = 1; j < dimension; j++) {
					
					distances[i][j] = euc.calc(xCoors.get(i-1),yCoors.get(i-1),xCoors.get(j-1),yCoors.get(j-1));
					
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
