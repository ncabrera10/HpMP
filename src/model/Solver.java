package model;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import globalParameters.GlobalParameters;
import core.OrderFirstSplitSecondHeuristic;
import core.Route;
import core.RouteAttribute;
import core.RoutePool;
import msh.CPLEXSetPartitioningSolver;
import msh.MSH;
import msh.OrderFirstSplitSecondSampling;
import split.HpMPSplit;
import split.HpMPSplit_ALL;
import split.HpMPSplit_LKH;
import split.HpMPSplit_LKH_CSP;
import core.ArrayDistanceMatrix;
import core.InsertionHeuristic;
import core.NNHeuristic;
import core.Split;
import distanceMatrices.CoindreauDistanceMatrix;
import distanceMatrices.RandomDistanceMatrix;
import distanceMatrices.TSPLibDistanceMatrix;
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
	 * CPU time used in the sampling phase of MSH
	 */
	private double cpu_msh_sampling;

	/**
	 * CPU time used in the assembly phase of MSH
	 */
	private double cpu_msh_assembly;

	
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

		//2. Check if we can improve the instance_u value:
		
				// The maximum number of nodes in a cycle is given by: |C| - 3 * (p-1)
					
					int new_bound = instance_n - 3 * (instance_r - 1);
						
					if(new_bound < instance_u) {
						instance_u = new_bound;
					}
							
		//3. Reads the instance 
			
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
			
			Random random_nn = new Random(GlobalParameters.SEED);
			Random random_ni = new Random(GlobalParameters.SEED+10);
			Random random_fi = new Random(GlobalParameters.SEED+20);
			Random random_bi = new Random(GlobalParameters.SEED+30);
			
			Random random_nn_2 = new Random(GlobalParameters.SEED+40);
			Random random_ni_2 = new Random(GlobalParameters.SEED+50);
			Random random_fi_2 = new Random(GlobalParameters.SEED+60);
			Random random_bi_2 = new Random(GlobalParameters.SEED+70);
			
			Random random_rn = new Random(GlobalParameters.SEED+80);
			Random random_rn_2 = new Random(GlobalParameters.SEED+90);
			
		// 5. Initializes the tsp heuristics:
			
			// RANDOM:
			
				NNHeuristic rn = new NNHeuristic(distances);
				rn.setRandomized(true);
				rn.setRandomGen(random_rn);
				rn.setRandomizationFactor(instance_n);
				rn.setInitNode(0);
				
				NNHeuristic rn_2 = new NNHeuristic(distances);
				rn_2.setRandomized(true);
				rn_2.setRandomGen(random_rn_2);
				rn_2.setRandomizationFactor((int) Math.floor(instance_n/2));
				rn_2.setInitNode(0);
			
			// RNN:
			
				NNHeuristic nn = new NNHeuristic(distances);
				nn.setRandomized(true);
				nn.setRandomGen(random_nn);
				nn.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
				nn.setInitNode(0);
				
				NNHeuristic nn_2 = new NNHeuristic(distances);
				nn_2.setRandomized(true);
				nn_2.setRandomGen(random_nn_2);
				nn_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
				nn_2.setInitNode(0);
			
			// RNI:
				
				InsertionHeuristic ni = new InsertionHeuristic(distances,"NEAREST_INSERTION");
				ni.setRandomized(true);
				ni.setRandomGen(random_ni);
				ni.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
				ni.setInitNode(0);
				
				InsertionHeuristic ni_2 = new InsertionHeuristic(distances,"NEAREST_INSERTION");
				ni_2.setRandomized(true);
				ni_2.setRandomGen(random_ni_2);
				ni_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
				ni_2.setInitNode(0);
				
			// RNI:
				
				InsertionHeuristic fi = new InsertionHeuristic(distances,"FARTHEST_INSERTION");
				fi.setRandomized(true);
				fi.setRandomGen(random_fi);
				fi.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
				fi.setInitNode(0);
				
				InsertionHeuristic fi_2 = new InsertionHeuristic(distances,"FARTHEST_INSERTION");
				fi_2.setRandomized(true);
				fi_2.setRandomGen(random_fi_2);
				fi_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
				fi_2.setInitNode(0);
				
			// BI:
				
				InsertionHeuristic bi = new InsertionHeuristic(distances,"BEST_INSERTION");
				bi.setRandomized(true);
				bi.setRandomGen(random_bi);
				bi.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
				bi.setInitNode(0);
				
				InsertionHeuristic bi_2 = new InsertionHeuristic(distances,"BEST_INSERTION");
				bi_2.setRandomized(true);
				bi_2.setRandomGen(random_bi_2);
				bi_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
				bi_2.setInitNode(0);
				
		// 6. Initializes the split algorithm: 
				
			Split s = null;
				
			if(GlobalParameters.SPLIT_IMPROVE_PETAL_LKH) {
				if(GlobalParameters.SPLIT_SOLVE_CSP) {
					s = new HpMPSplit_LKH_CSP(distances,instance_l,instance_u,instance_r);
				}else {
					s = new HpMPSplit_LKH(distances,instance_l,instance_u,instance_r);
				}
			}else {
				if(GlobalParameters.SPLIT_ADD_ALL) {
					s = new HpMPSplit_ALL(distances,instance_l,instance_u,instance_r);
				}else {
					s = new HpMPSplit(distances,instance_l,instance_u,instance_r);
				}
				
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
			
			OrderFirstSplitSecondHeuristic rn_h = new OrderFirstSplitSecondHeuristic(rn, s);
			OrderFirstSplitSecondHeuristic rn_2h = new OrderFirstSplitSecondHeuristic(rn_2, s);
			
		//8. Set up MSH
			
			int num_iterations = (int)GlobalParameters.MSH_NUM_ITERATIONS / 10;
			OrderFirstSplitSecondSampling f_nn = new OrderFirstSplitSecondSampling(nn_h,num_iterations);
			OrderFirstSplitSecondSampling f_ni = new OrderFirstSplitSecondSampling(ni_h,num_iterations);
			OrderFirstSplitSecondSampling f_fi = new OrderFirstSplitSecondSampling(fi_h,num_iterations);
			OrderFirstSplitSecondSampling f_bi = new OrderFirstSplitSecondSampling(bi_h,num_iterations);
			
			OrderFirstSplitSecondSampling f_nn_2 = new OrderFirstSplitSecondSampling(nn_2h,num_iterations);
			OrderFirstSplitSecondSampling f_ni_2 = new OrderFirstSplitSecondSampling(ni_2h,num_iterations);
			OrderFirstSplitSecondSampling f_fi_2 = new OrderFirstSplitSecondSampling(fi_2h,num_iterations);
			OrderFirstSplitSecondSampling f_bi_2 = new OrderFirstSplitSecondSampling(bi_2h,num_iterations);
			
			OrderFirstSplitSecondSampling f_rn = new OrderFirstSplitSecondSampling(rn_h,num_iterations);
			OrderFirstSplitSecondSampling f_rn_2 = new OrderFirstSplitSecondSampling(rn_2h,num_iterations);
			
			// Initializes all the pool of routes:
			
			ArrayList<RoutePool> pools = new ArrayList<RoutePool>();
			
			RoutePool pool_nn= new RoutePool();
			RoutePool pool_ni= new RoutePool();
			RoutePool pool_fi= new RoutePool();
			RoutePool pool_bi= new RoutePool();
			
			RoutePool pool_nn_2= new RoutePool();
			RoutePool pool_ni_2= new RoutePool();
			RoutePool pool_fi_2= new RoutePool();
			RoutePool pool_bi_2= new RoutePool();
			
			RoutePool pool_rn= new RoutePool();
			RoutePool pool_rn_2= new RoutePool();
			
			
			pools.add(pool_nn);
			pools.add(pool_ni);
			pools.add(pool_fi);
			pools.add(pool_bi);
			
			pools.add(pool_nn_2);
			pools.add(pool_ni_2);
			pools.add(pool_fi_2);
			pools.add(pool_bi_2);
			
			pools.add(pool_rn);
			pools.add(pool_rn_2);
			
		//9. Starts the pool:
			
			pool_nn.start();
			pool_ni.start();
			pool_fi.start();
			pool_bi.start();
			
			pool_nn_2.start();
			pool_ni_2.start();
			pool_fi_2.start();
			pool_bi_2.start();
			
			pool_rn.start();
			pool_rn_2.start();
			
		//9.5 Sets all the pools:
			
			f_nn.setRoutePool(pool_nn);
			f_ni.setRoutePool(pool_ni);
			f_fi.setRoutePool(pool_fi);
			f_bi.setRoutePool(pool_bi);
			
			f_nn_2.setRoutePool(pool_nn_2);
			f_ni_2.setRoutePool(pool_ni_2);
			f_fi_2.setRoutePool(pool_fi_2);
			f_bi_2.setRoutePool(pool_bi_2);
			
			f_rn.setRoutePool(pool_rn);
			f_rn_2.setRoutePool(pool_rn_2);
			
		//10. Stops the clock for the initialization time:
			
			Double FinTime = (double) System.nanoTime();
			cpu_initialization = (FinTime-IniTime)/1000000000;
					
		//11. Creates an assembler:
			
			CPLEXSetPartitioningSolver assembler = new CPLEXSetPartitioningSolver(distances.size()-1,true,instance_r);
			
		//12. Initializes the MSH:
			
			MSH msh = new MSH(f_nn,assembler,pools,GlobalParameters.THREADS);
			msh.addSamplingFunction(f_ni);
			msh.addSamplingFunction(f_fi);
			msh.addSamplingFunction(f_bi);
			
			msh.addSamplingFunction(f_nn_2);
			msh.addSamplingFunction(f_ni_2);
			msh.addSamplingFunction(f_bi_2);
			msh.addSamplingFunction(f_fi_2);
			
			msh.addSamplingFunction(f_rn);
			msh.addSamplingFunction(f_rn_2);

		//13. Runs the assembly phase of MSH:
			
			Double IniTime_msh = (double) System.nanoTime();
			
			msh.run_sampling();
			
			Double FinTime_msh = (double) System.nanoTime();
			
			cpu_msh_sampling = (FinTime_msh-IniTime_msh)/1000000000;
			
		//14. Runs the assembly phase of MSH:
			
			IniTime_msh = (double) System.nanoTime();
			
			msh.run_assembly();
			
			FinTime_msh = (double) System.nanoTime();
			
			cpu_msh_assembly = (FinTime_msh-IniTime_msh)/1000000000;
				
			
		//15. Number of sampling iterations done:
			
			int it = 0;
			it += f_nn.getNuberOfDrawnSamples();
			it += f_ni.getNuberOfDrawnSamples();
			it += f_fi.getNuberOfDrawnSamples();
			it += f_bi.getNuberOfDrawnSamples();
			
			it += f_nn_2.getNuberOfDrawnSamples();
			it += f_ni_2.getNuberOfDrawnSamples();
			it += f_fi_2.getNuberOfDrawnSamples();
			it += f_bi_2.getNuberOfDrawnSamples();
			
			it += f_rn.getNuberOfDrawnSamples();
			it += f_rn_2.getNuberOfDrawnSamples();
			
			int numConflicts = -1;
			numConflicts += pool_nn.getNumTimesAlreadyInside();
			numConflicts += pool_ni.getNumTimesAlreadyInside();
			numConflicts += pool_fi.getNumTimesAlreadyInside();
			numConflicts += pool_bi.getNumTimesAlreadyInside();
			
			numConflicts += pool_nn_2.getNumTimesAlreadyInside();
			numConflicts += pool_ni_2.getNumTimesAlreadyInside();
			numConflicts += pool_fi_2.getNumTimesAlreadyInside();
			numConflicts += pool_bi_2.getNumTimesAlreadyInside();
			
			numConflicts += pool_rn.getNumTimesAlreadyInside();
			numConflicts += pool_rn_2.getNumTimesAlreadyInside();
			
			
			int numNewRoutes = -1;
			numNewRoutes += pool_nn.getNumTimesNewRoute();
			numNewRoutes += pool_ni.getNumTimesNewRoute();
			numNewRoutes += pool_fi.getNumTimesNewRoute();
			numNewRoutes += pool_bi.getNumTimesNewRoute();
			
			numNewRoutes += pool_nn_2.getNumTimesNewRoute();
			numNewRoutes += pool_ni_2.getNumTimesNewRoute();
			numNewRoutes += pool_fi_2.getNumTimesNewRoute();
			numNewRoutes += pool_bi_2.getNumTimesNewRoute();
			
			numNewRoutes += pool_rn.getNumTimesNewRoute();
			numNewRoutes += pool_rn_2.getNumTimesNewRoute();
			
		//16. Prints the solution:
			
			printSummary(msh,assembler,it,numConflicts,numNewRoutes);
			printSolution(msh,assembler);
	
		
	}
	
	
	/**
	 * This method prints a summary in a txt file with the main statistics of the current run.
	 * @param msh MSH object
	 * @param assemble cplex object
	 * @param it Number f iterations performed in the sampling phase
	 * @param numConflicts: Number of times a route was already contained in the pool
	 * @param numNewRoutes: Number of times a route was completely new for the pool
	 */
	public void printSummary(MSH msh, CPLEXSetPartitioningSolver assembler,int it, int numConflicts, int numNewRoutes) {
		
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
					pw.println("TotalTime(s);"+(cpu_msh_sampling+cpu_msh_assembly));
					pw.println("TotalDistance;"+assembler.objectiveFunction);
					pw.println("NumberOfRings;"+assembler.numberOfRingsSolution);
					pw.println("Iterations;"+it);
					pw.println("SizeOfPool;"+msh.getPoolSize());
					pw.println("SamplingTime(s);"+cpu_msh_sampling);
					pw.println("AssemblyTime(s);"+cpu_msh_assembly);
					pw.println("NumberOfConflictingRoutes:"+numConflicts);
					pw.println("NumberOfNewRoutes:"+numNewRoutes);
				
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
						System.out.println("\tSamplingTime(s);"+cpu_msh_sampling);
						System.out.println("\tAssemblyTime(s);"+cpu_msh_assembly);
						System.out.println("\tTotalTime(s);"+(cpu_msh_sampling+cpu_msh_assembly));
						System.out.println("\tTotalDistance: "+assembler.objectiveFunction);
						System.out.println("\tNumberOfRings: "+assembler.numberOfRingsSolution);
						System.out.println("\tIterations: "+it);
						System.out.println("\tSizeOfPool: "+msh.getPoolSize());
						System.out.println("\tNumberOfConflictingRoutes:"+numConflicts);
						System.out.println("\tNumberOfNewRoutes:"+numNewRoutes);
						
					
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
	public void printSolution(MSH msh, CPLEXSetPartitioningSolver assembler) {
		
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
				
					System.out.println("Solution:");
					
					
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
							
							System.out.println("\t"+counter+" - "+ring+" - "+route.getAttribute(RouteAttribute.COST));
							
							
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