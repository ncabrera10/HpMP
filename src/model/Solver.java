package model;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import globalParameters.GlobalParameters;
import implementations.HpMPSplit;
import implementations.HpMPSplit_LKH;
import implementations.RandomDistanceMatrix;
import implementations.CoindreauDistanceMatrix;
import implementations.TSPLibDistanceMatrix;
import core.OrderFirstSplitSecondHeuristic;
import core.Route;
import core.RouteAttribute;
import core.RoutePool;
import msh.CPLEXSetPartitioningSolver;
import msh.MSH;
import msh.OrderFirstSplitSecondSampling;
import core.ArrayDistanceMatrix;
import core.InsertionHeuristic;
import core.NNHeuristic;
import core.Split;
import tspReader.TSPLibInstance;

/**
 * This class contains the main logic of the MSH. 
 * It contains the information of the instance and the method MSH.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class Solver {

	/**
	 * Class of the instance
	 */
	private int instance_class;
	
	/**
	 * Dataset of the instance
	 */
	private int instance_dataset;
	
	/**
	 * Number of nodes in the instance
	 */
	private int instance_n;
	
	/**
	 * Number of desired rings or petals in the final solution
	 */
	private int instance_r;
	
	/**
	 * Minimum number of nodes in each petal
	 */
	private int instance_l;
	
	/**
	 * Maximum number of nodes in each petal
	 */
	private int instance_u;
	
	/**
	 * Current replicate
	 */
	private int instance_rep;
	
	/**
	 * Jorlib instances: TSP name
	 */
	private String instance_tsp_name;
	
	/**
	 * Instance identifier
	 */
	private String instance_identifier;
	
	/**
	 * Instance set
	 */
	private String instance_set;
	
	// Statistics 
	
	/**
	 * CPU time used to initialize the instance
	 */
	private double cpu_initialization;
	
	/**
	 * CPU time used to initialize the MSH
	 */
	private double cpu_msh;

	
	//------------------------------------------MAIN LOGIC-----------------------------------
	
	/**
	 * This method runs the MSH
	 * @throws IOException
	 */
	public void MSH(int instance_class,int instance_dataset,int instance_n,int instance_r,int instance_l,int instance_u,int instance_rep,String instance_set,String instance_tsp_name)throws IOException {
		
		//0. Starts the counter for the initialization step:
		
			Double IniTime = (double) System.nanoTime();
			
		//1. Updates the information selected by the user
		
			this.updateInfo(instance_class,instance_dataset,instance_n,instance_r,instance_l,instance_u,instance_rep,instance_set,instance_tsp_name);

		// 1.5 Check if we can improve the instance_u value:
		
				// The maximum number of nodes in a cycle is given by: |C| - 3 * (p-1)
					
					int new_bound = instance_n - 3 * (instance_r - 1);
						
					if(new_bound < instance_u) {
						instance_u = new_bound;
					}
							
		//2. Reads the instance 
			
			ArrayDistanceMatrix distances = null;
			if(instance_set.equals("TSPLib")) {
				TSPLibInstance tspLibInstance = new TSPLibInstance(new File("./instances/tspLib/tsp/"+instance_tsp_name));	
				distances = new TSPLibDistanceMatrix(tspLibInstance);
			}else if(instance_set.equals("Coindreau")){
				distances = new CoindreauDistanceMatrix("./instances/Coindreau/"+instance_tsp_name);
			}else if(instance_set.equals("Random")){
				distances = new RandomDistanceMatrix("./instances/Random/"+instance_tsp_name);
			}else {
				System.out.println("Instance unknown");
				System.exit(0);
			}
				
		// 4. Sets the seed for the generation of random numbers:
			
			Random random = new Random(GlobalParameters.SEED);
			
		// 5. Initializes the tsp heuristics:
			
			// RNN:
			
				NNHeuristic nn = new NNHeuristic(distances);
				nn.setRandomized(true);
				nn.setRandomGen(random);
				nn.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
				nn.setInitNode(0);
				
				NNHeuristic nn_2 = new NNHeuristic(distances);
				nn_2.setRandomized(true);
				nn_2.setRandomGen(random);
				nn_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
				nn_2.setInitNode(0);
			
			// RNI:
				
				InsertionHeuristic ni = new InsertionHeuristic(distances,"NEAREST_INSERTION");
				ni.setRandomized(true);
				ni.setRandomGen(random);
				ni.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
				ni.setInitNode(0);
				
				InsertionHeuristic ni_2 = new InsertionHeuristic(distances,"NEAREST_INSERTION");
				ni_2.setRandomized(true);
				ni_2.setRandomGen(random);
				ni_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
				ni_2.setInitNode(0);
				
			// RNI:
				
				InsertionHeuristic fi = new InsertionHeuristic(distances,"FARTHEST_INSERTION");
				fi.setRandomized(true);
				fi.setRandomGen(random);
				fi.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
				fi.setInitNode(0);
				
				InsertionHeuristic fi_2 = new InsertionHeuristic(distances,"FARTHEST_INSERTION");
				fi_2.setRandomized(true);
				fi_2.setRandomGen(random);
				fi_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
				fi_2.setInitNode(0);
				
			// BI:
				
				InsertionHeuristic bi = new InsertionHeuristic(distances,"BEST_INSERTION");
				bi.setRandomized(true);
				bi.setRandomGen(random);
				bi.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
				bi.setInitNode(0);
				
				InsertionHeuristic bi_2 = new InsertionHeuristic(distances,"BEST_INSERTION");
				bi_2.setRandomized(true);
				bi_2.setRandomGen(random);
				bi_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
				bi_2.setInitNode(0);
				
		// 6. Initializes the split algorithm: 
				
			Split s = null;
				
			if(GlobalParameters.SPLIT_IMPROVE_PETAL_LKH) {
				s = new HpMPSplit_LKH(distances,instance_l,instance_u,instance_r);
			}else {
				s = new HpMPSplit(distances,instance_l,instance_u,instance_r);
			}
			
		//7. Set up heuristic
			
			OrderFirstSplitSecondHeuristic nn_h = new OrderFirstSplitSecondHeuristic(nn, s);
			OrderFirstSplitSecondHeuristic ni_h = new OrderFirstSplitSecondHeuristic(ni, s);
			OrderFirstSplitSecondHeuristic fi_h = new OrderFirstSplitSecondHeuristic(fi, s);
			OrderFirstSplitSecondHeuristic bi_h = new OrderFirstSplitSecondHeuristic(bi, s);
			
			OrderFirstSplitSecondHeuristic nn_2h = new OrderFirstSplitSecondHeuristic(nn_2, s);
			OrderFirstSplitSecondHeuristic ni_2h = new OrderFirstSplitSecondHeuristic(ni_2, s);
			OrderFirstSplitSecondHeuristic fi_2h = new OrderFirstSplitSecondHeuristic(fi_2, s);
			OrderFirstSplitSecondHeuristic bi_2h = new OrderFirstSplitSecondHeuristic(bi_2, s);
			
		//8. Set up MSH
			
			int num_iterations = (int)GlobalParameters.MSH_NUM_ITERATIONS / 8;
			OrderFirstSplitSecondSampling f_nn = new OrderFirstSplitSecondSampling(nn_h,num_iterations,instance_r);
			OrderFirstSplitSecondSampling f_ni = new OrderFirstSplitSecondSampling(ni_h,num_iterations,instance_r);
			OrderFirstSplitSecondSampling f_fi = new OrderFirstSplitSecondSampling(fi_h,num_iterations,instance_r);
			OrderFirstSplitSecondSampling f_bi = new OrderFirstSplitSecondSampling(bi_h,num_iterations,instance_r);
			
			OrderFirstSplitSecondSampling f_nn_2 = new OrderFirstSplitSecondSampling(nn_2h,num_iterations,instance_r);
			OrderFirstSplitSecondSampling f_ni_2 = new OrderFirstSplitSecondSampling(ni_2h,num_iterations,instance_r);
			OrderFirstSplitSecondSampling f_fi_2 = new OrderFirstSplitSecondSampling(fi_2h,num_iterations,instance_r);
			OrderFirstSplitSecondSampling f_bi_2 = new OrderFirstSplitSecondSampling(bi_2h,num_iterations,instance_r);
			
			// Initializes the pool of routes:
			
			RoutePool pool = new RoutePool();
			
		//9. Starts the pool:
			
			pool.start();
			f_nn.setRoutePool(pool);
			f_ni.setRoutePool(pool);
			f_fi.setRoutePool(pool);
			f_bi.setRoutePool(pool);
			
			f_nn_2.setRoutePool(pool);
			f_ni_2.setRoutePool(pool);
			f_fi_2.setRoutePool(pool);
			f_bi_2.setRoutePool(pool);
			
		//10. Stops the clock for the initialization time:
			
			Double FinTime = (double) System.nanoTime();
			cpu_initialization = (FinTime-IniTime)/1000000000;
					
		//12. Creates an assembler:
			
			CPLEXSetPartitioningSolver assembler = new CPLEXSetPartitioningSolver(distances.size()-1,true,instance_r);
			
		//13. Initializes the MSH:
			
			MSH msh = new MSH(f_nn,assembler,pool,GlobalParameters.THREADS);
			msh.addSamplingFunction(f_ni);
			msh.addSamplingFunction(f_fi);
			msh.addSamplingFunction(f_bi);
			
			msh.addSamplingFunction(f_nn_2);
			msh.addSamplingFunction(f_ni_2);
			msh.addSamplingFunction(f_bi_2);
			msh.addSamplingFunction(f_fi_2);
			
		//11. Starts the counter for the msh:
			
			Double IniTime_msh = (double) System.nanoTime();
		
		//14. Runs the MSH:
			
			msh.run();

		//15. Stops the clock for the MSH:
			
			Double FinTime_msh = (double) System.nanoTime();
			cpu_msh = (FinTime_msh-IniTime_msh)/1000000000;
			
		//16. Prints the solution:
			
			printSummary(msh,assembler,pool);
			printSolution(msh,assembler,pool);
	
		
	}
	
	
	/**
	 * This method prints a summary in a txt file with the main statistics of the current run.
	 * @param msh
	 * @param assembler
	 * @param pool
	 */
	public void printSummary(MSH msh, CPLEXSetPartitioningSolver assembler, RoutePool pool) {
		
		// 1. Defines the path for the txt file:
		
			String path = "";
			if(instance_set.equals("TSPLib")) {
				path =  globalParameters.GlobalParameters.RESULT_FOLDER+"TSPLib/"+"/Summary"+instance_identifier+"-"+instance_tsp_name+"-"+GlobalParameters.SEED+".txt";
			}
			else {
				path = globalParameters.GlobalParameters.RESULT_FOLDER+instance_set+"/"+"Summary"+instance_tsp_name+"-"+GlobalParameters.SEED+".txt";
			}
			
		// 2. Prints the txt file:
			
			try {
				
				// Creates the print writer:
				
					PrintWriter pw = new PrintWriter(new File(path));
					
				// Prints relevant information:
					
					pw.println("InstanceClass;"+instance_class);
					pw.println("InstanceDataSet;"+instance_dataset);
					pw.println("InstanceIdentifier;"+instance_identifier);
					pw.println("Instance_l;"+instance_l);
					pw.println("Instance_n;"+instance_n);
					pw.println("Instance_r;"+instance_r);
					pw.println("Instance_set;"+instance_set);
					pw.println("Instance_tsp_name;"+instance_tsp_name);
					pw.println("Instance_rep;"+instance_rep);
					pw.println("InitializationTime(s);"+cpu_initialization);
					pw.println("MSHTime(s);"+cpu_msh);
					pw.println("TotalDistance;"+assembler.objectiveFunction);
					pw.println("NumberOfRings;"+assembler.numberOfRingsSolution);
					pw.println("Iterations;"+GlobalParameters.MSH_NUM_ITERATIONS);
					pw.println("SizeOfPool;"+pool.size());
				
					if(GlobalParameters.PRINT_IN_CONSOLE) {
						System.out.println("Summary:");
						System.out.println("\tInstanceClass: "+instance_class);
						System.out.println("\tInstanceDataSet: "+instance_dataset);
						System.out.println("\tInstanceIdentifier: "+instance_identifier);
						System.out.println("\tInstance_l: "+instance_l);
						System.out.println("\tInstance_n: "+instance_n);
						System.out.println("\tInstance_r: "+instance_r);
						System.out.println("\tInstance_set: "+instance_set);
						System.out.println("\tInstance_tsp_name: "+instance_tsp_name);
						System.out.println("\tInstance_rep: "+instance_rep);
						System.out.println("\tInitializationTime(s): "+cpu_initialization);
						System.out.println("\tMSHTime(s): "+cpu_msh);
						System.out.println("\tTotalDistance: "+assembler.objectiveFunction);
						System.out.println("\tNumberOfRings: "+assembler.numberOfRingsSolution);
						System.out.println("\tIterations: "+GlobalParameters.MSH_NUM_ITERATIONS);
						System.out.println("\tSizeOfPool: "+pool.size());
					}
					
				// Closes the print writer:
				
					pw.close();
					
			}catch(Exception e) {
				System.out.println("Mistake printing the summary");
			}
		
	}
	
	/**
	 * This method prints the routes in a txt file.
	 * @param msh
	 * @param assembler
	 * @param pool
	 */
	public void printSolution(MSH msh, CPLEXSetPartitioningSolver assembler, RoutePool pool) {
		
		// 1. Defines the path for the txt file:
		
			String path = "";
			String path_arcs = "";
			if(instance_set.equals("TSPLib")) {
				path =  globalParameters.GlobalParameters.RESULT_FOLDER+"TSPLib/"+"/Solution"+instance_identifier+"-"+instance_tsp_name+"-"+GlobalParameters.SEED+".txt";
				path_arcs =  globalParameters.GlobalParameters.RESULT_FOLDER+"TSPLib/"+"/SolutionArcs"+instance_identifier+"-"+instance_tsp_name+"-"+GlobalParameters.SEED+".txt";
				
			}
			else {
				path = globalParameters.GlobalParameters.RESULT_FOLDER+instance_set+"/"+"Solution"+instance_tsp_name+"-"+GlobalParameters.SEED+".txt";
				path_arcs = globalParameters.GlobalParameters.RESULT_FOLDER+instance_set+"/"+"SolutionArcs"+instance_tsp_name+"-"+GlobalParameters.SEED+".txt";
				
			}
			
		// 2. Prints the txt file:
			
			try {
				
				// Creates the print writer:
				
					PrintWriter pw = new PrintWriter(new File(path));
					PrintWriter pw_arcs = new PrintWriter(new File(path_arcs));
				
					if(GlobalParameters.PRINT_IN_CONSOLE) {
						System.out.println("Solution:");
					}
					
				// Prints each of the selected routes:
					
					int counter = 0;
					for(Route route : assembler.solution) {
						
						// Build the "Ring":
						
							String ring = "";
							
							for(int pos=1;pos < route.getRoute().size()-1;pos++) {
								
								ring += route.getRoute().get(pos)+"->";
								if(pos<route.getRoute().size()-2) {
									pw_arcs.println(route.getRoute().get(pos)+";"+route.getRoute().get(pos+1)+";"+1+";"+counter);
								}else {
									pw_arcs.println(route.getRoute().get(pos)+";"+route.getRoute().get(1)+";"+1+";"+counter);
								}
								
							}
							
							ring += route.getRoute().get(1);
							
						// Print the ring:
						
							pw.println(counter+" - "+ring+" - "+route.getAttribute(RouteAttribute.COST));
							
							if(GlobalParameters.PRINT_IN_CONSOLE) {
								System.out.println("\t"+counter+" - "+ring+" - "+route.getAttribute(RouteAttribute.COST));
							}
							
						// Update the counter:
							
							counter++;
					}
					
				// Closes the print writer:
				
					pw.close();
					pw_arcs.close();
					
			}catch(Exception e) {
				System.out.println("Mistake printing the solution");
				e.printStackTrace();
			}
		
	}
	
	/**
	 * Updates the instance information
	 * @param instance_class
	 * @param instance_dataset
	 * @param instance_n
	 * @param instance_r
	 * @param instance_l
	 * @param instance_u
	 * @param instance_rep
	 * @param instance_set
	 * @param instance_tsp_name
	 */
	public void updateInfo(int instance_class,int instance_dataset,int instance_n,int instance_r,int instance_l,int instance_u,int instance_rep,String instance_set,String instance_tsp_name) {
		String identf = instance_class+"-"+instance_dataset+"-"+instance_n+"-"+instance_r+"-"+instance_l+"-"+instance_u+"-"+instance_rep;
		this.instance_identifier = identf;
		this.instance_class = instance_class;
		this.instance_dataset = instance_dataset;
		this.instance_n = instance_n;
		this.instance_r = instance_r;
		this.instance_l = instance_l;
		this.instance_u = instance_u;
		this.instance_rep = instance_rep;
		this.instance_set = instance_set;
		this.instance_tsp_name = instance_tsp_name;
	}

	//--------------------------------------------Getters and setters---------------------------
	
	
	/**
	 * @return the instance_class
	 */
	public int getInstance_class() {
		return instance_class;
	}


	/**
	 * @param instance_class the instance_class to set
	 */
	public void setInstance_class(int instance_class) {
		this.instance_class = instance_class;
	}


	/**
	 * @return the instance_dataset
	 */
	public int getInstance_dataset() {
		return instance_dataset;
	}


	/**
	 * @param instance_dataset the instance_dataset to set
	 */
	public void setInstance_dataset(int instance_dataset) {
		this.instance_dataset = instance_dataset;
	}


	/**
	 * @return the instance_n
	 */
	public int getInstance_n() {
		return instance_n;
	}


	/**
	 * @param instance_n the instance_n to set
	 */
	public void setInstance_n(int instance_n) {
		this.instance_n = instance_n;
	}


	/**
	 * @return the instance_r
	 */
	public int getInstance_r() {
		return instance_r;
	}


	/**
	 * @param instance_r the instance_r to set
	 */
	public void setInstance_r(int instance_r) {
		this.instance_r = instance_r;
	}


	/**
	 * @return the instance_l
	 */
	public int getInstance_l() {
		return instance_l;
	}


	/**
	 * @param instance_l the instance_l to set
	 */
	public void setInstance_l(int instance_l) {
		this.instance_l = instance_l;
	}


	/**
	 * @return the instance_u
	 */
	public int getInstance_u() {
		return instance_u;
	}


	/**
	 * @param instance_u the instance_u to set
	 */
	public void setInstance_u(int instance_u) {
		this.instance_u = instance_u;
	}


	/**
	 * @return the instance_rep
	 */
	public int getInstance_rep() {
		return instance_rep;
	}


	/**
	 * @param instance_rep the instance_rep to set
	 */
	public void setInstance_rep(int instance_rep) {
		this.instance_rep = instance_rep;
	}


	/**
	 * @return the instance_tsp_name
	 */
	public String getInstance_tsp_name() {
		return instance_tsp_name;
	}


	/**
	 * @param instance_tsp_name the instance_tsp_name to set
	 */
	public void setInstance_tsp_name(String instance_tsp_name) {
		this.instance_tsp_name = instance_tsp_name;
	}


	/**
	 * @return the instance_identifier
	 */
	public String getInstance_identifier() {
		return instance_identifier;
	}


	/**
	 * @param instance_identifier the instance_identifier to set
	 */
	public void setInstance_identifier(String instance_identifier) {
		this.instance_identifier = instance_identifier;
	}


	/**
	 * @return the instance_set
	 */
	public String getInstance_set() {
		return instance_set;
	}


	/**
	 * @param instance_set the instance_set to set
	 */
	public void setInstance_set(String instance_set) {
		this.instance_set = instance_set;
	}
	
	
	
	
}