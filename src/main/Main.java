package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import globalParameters.GlobalParameters;
import globalParameters.GlobalParametersReader;
import model.Manager;

/**
 * This class runs the MSH procedure. 
 * The user can select the instance and the implementation modifying the main method.
 * 
 * Instances:
 * 	Set: Herran et al. (2019)
 *  Size: Small, Medium, Large
 * 
 * If you want to change more parameters, you can modify the "parametersCG.xml" file.
 * For example:
 * 	-Printing useful information on console
 * 	-Modifying the number of iterations for each TSP heuristic..
 * 	Among others..
 * 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class Main {

	public static void main(String[] args) {
		
		// ----------------SELECT THE MAIN PARAMETERS-----------------------
		
			// Select the txt file, with the instance specifications: The txt files are located in the experiments folder.
			
				//Options: ExperimentsSmall.txt,  "ExperimentsSmall_Random.txt", "ExperiemntsCoindreau.txt", "ExperimentsMedium.txt", "ExperimentsLarge.txt"
		
				String fileName = "ExperimentsALL.txt"; 
				
			// Select the instance you want to run, (i.e., the line of the txt file):
			
				int current_instance = 49; 
			
			// Select a seed for the random number generator:
				
				int current_rep = 5; //Current replicate
			
			//Number of threads for cplex and the MSH
			
				int threads = 8; 
			
		// ------------------------------------------------------------------	
			
		// Main logic:
			
			
			// Create a buffered reader:
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader("./experiments/"+fileName));
				int count = 0;
				String line = reader.readLine();
				count++;
				while(line != null && count < current_instance) {
					line = reader.readLine();
					count++;
				}
				
				args = line.split(";");
				reader.close();
				
			} catch (IOException e1) {
				System.out.println("The file does not exists");
				System.exit(0);
			}
		
		
		int instance_class = Integer.parseInt(args[0]); //Class
		
		int instance_dataset = Integer.parseInt(args[1]); //Dataset
		
		int instance_n = Integer.parseInt(args[2]); //Number of nodes
		
		int instance_r = Integer.parseInt(args[3]); //Number of desired rings
		
		int instance_l = Integer.parseInt(args[4]); //Minimum number of nodes in each ring
		
		int instance_u = Integer.parseInt(args[5]); //Maximum number of nodes in each ring
		
		int instance_rep = current_rep; //Seed
		
		String instance_set = args[6]; // TSPlib or Random or Coindreau
		
		String instance_tsp_name = args[7]; //If instance_set = TSPLib..
		
		int seed = current_rep; //Current seed for the random numbers
		
		// Runs the code:
		
		try {
			
			// Loads the global parameters: some paths, the precision..
			
				GlobalParametersReader.initialize("./config/parametersGlobal.xml");
				setUpConfiguration(seed,threads);
			
			// Creates a Manager:
				
				Manager manager = new Manager();
				
			// Runs the MSH:
				
				manager.runMSH(instance_class,instance_dataset,instance_n,instance_r,instance_l,instance_u,instance_rep,instance_set,instance_tsp_name);
				
			// Closes the code:
				
				System.exit(0);
			
		}catch(Exception e) {
			
			System.out.println("A problem running the code");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This method sets the main parameters of the run: Seed and threads
	 * @param seed
	 * @param threads
	 */
	public static void setUpConfiguration(int seed, int threads) {
		
		GlobalParameters.SEED = seed;
		GlobalParameters.THREADS = threads;
		
	}
	
}
