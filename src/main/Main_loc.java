package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import globalParameters.GlobalParameters;
import globalParameters.GlobalParametersReader;
import model.Manager;

public class Main_loc {

	public static void main(String[] args) {
		
		// ----------------SELECT THE MAIN PARAMETERS-----------------------
		
			int current_instance = Integer.parseInt(args[0]); //Line of the txt file
			
			String fileName = args[1]; //Txt file name 
				//Options: ExperimentsSmall.txt,  "ExperimentsSmall_Random.txt", "ExperiemntsCoindreau.txt", "ExperimentsMedium.txt", "ExperimentsLarge.txt"
				
			int current_rep = Integer.parseInt(args[2]); //Current replicate
			
			int threads = 1; //Number of threads for cplex and the MSH
			
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
		
		int instance_l = Integer.parseInt(args[4]);
		
		int instance_u = Integer.parseInt(args[5]);
		
		int instance_rep = current_rep;
		
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
