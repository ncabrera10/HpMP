package model;

import java.io.IOException;

/**
 * Class to manage the different algorithms
 */

public class Manager {

	public Manager() throws IOException, InterruptedException{

	}
	
	/**
	 * Runs the MSH algorithm
	 */
	
	public Solver runMSH(int instance_class,int instance_dataset,int instance_n,int instance_r,int instance_l,int instance_u,int instance_rep,String instance_set,String instance_tsp_name)throws IOException, InterruptedException {
		
		// Creates a solver instance:
		Solver solver = new Solver();
		
		// Runs the MSH:
		solver.MSH(instance_class,instance_dataset,instance_n,instance_r,instance_l,instance_u,instance_rep,instance_set,instance_tsp_name);
		
		// Returns the solver instance:
		return solver;
	}
	
	
	
}
