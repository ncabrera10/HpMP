package msh;

import java.util.ArrayList;

import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import core.Route;
import core.RouteAttribute;
import core.RoutePool;
import core.Solution;
import globalParameters.GlobalParameters;

/**
 * This class solves the set partitioning model to assembly the final solution. 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class CPLEXSetPartitioningSolver implements AssemblyFunction{

	protected int nRequests;
	protected boolean hasTerminals;
	protected IloCplex cplex;
	protected IloNumVar[] x;
	protected int rings;
	
	public double objectiveFunction;
	public int numberOfRingsSolution = 0;
	public ArrayList<Route> solution;
	
	public CPLEXSetPartitioningSolver(int nRequests, boolean hasTerminals, int rings){
		this.nRequests=nRequests;
		this.hasTerminals=hasTerminals;
		this.rings=rings;
	}

	@Override
	public Solution assembleSolution(Solution bound, RoutePool pool) {
		Route[] routes=pool.toArray();
		 try {
			//Build CPLEX environment
			cplex = new IloCplex();
			//Create decision variables
			x=cplex.boolVarArray(routes.length);
			//Create covering/partitioning constraints and objective function
			IloLinearNumExpr[] constraints = new IloLinearNumExpr[nRequests];
			IloLinearNumExpr numberOfRings = cplex.linearNumExpr();
			IloLinearNumExpr of=cplex.linearNumExpr();
			//Add terms to the covering/partitioning constraints and objective function
			int start,end;
			for(int r=0;r<routes.length;r++){
				of.addTerm((double)routes[r].getAttribute(RouteAttribute.COST),x[r]);
				ArrayList<Integer> route=(ArrayList<Integer>) routes[r].getRoute();
				if(hasTerminals){
					start=1;
					end=route.size()-1;
				}else{
					start=0;
					end=route.size();
				}
				for(int i=start;i<end;i++){
					if(constraints[route.get(i)-1]==null)
						constraints[route.get(i)-1]=cplex.linearNumExpr();
					constraints[route.get(i)-1].addTerm(1,x[r]);
				}
				numberOfRings.addTerm(1, x[r]);
			}
			//Add constraints to the model
			for(int i=0;i<constraints.length;i++){
				cplex.addEq(1,constraints[i]);
			}
			
			cplex.addEq(rings, numberOfRings);
			
			//Add objective function
			 cplex.addMinimize(of);
			 
			//Hide the output:
			 cplex.setOut(null);
			 cplex.setParam(IloCplex.Param.TimeLimit,GlobalParameters.MSH_ASSEMBLY_TIME_LIMIT);
			 cplex.setParam(IloCplex.Param.Threads, GlobalParameters.THREADS);
			 
			//Solve model
			 cplex.solve();
			
			//Store the solution:
			 
			 objectiveFunction = cplex.getObjValue();
			 
			 solution = new ArrayList<Route>();
			 
			 for(int r=0;r<routes.length;r++) {
				if(cplex.getValue(x[r]) > 0){
					numberOfRingsSolution++;
					solution.add(routes[r]);
					//System.out.println(r+" - "+routes[r].getAttribute(RouteAttribute.COST)+" - "+routes[r].getRoute().toString());
				}
			}
			
			 
		} catch (IloException e) {
			e.printStackTrace();
		}
		 
		
		return null;
	}
	
	@Override
	public Solution assembleSolution(RoutePool pool) {
		Route[] routes=pool.toArray();
		 try {
			//Build CPLEX environment
			cplex = new IloCplex();
			//Create decision variables
			x=cplex.boolVarArray(routes.length);
			//Create covering/partitioning constraints and objective function
			IloLinearNumExpr[] constraints = new IloLinearNumExpr[nRequests];
			IloLinearNumExpr numberOfRings = cplex.linearNumExpr();
			IloLinearNumExpr of=cplex.linearNumExpr();
			//Add terms to the covering/partitioning constraints and objective function
			int start,end;
			for(int r=0;r<routes.length;r++){
				of.addTerm((double)routes[r].getAttribute(RouteAttribute.COST),x[r]);
				ArrayList<Integer> route=(ArrayList<Integer>) routes[r].getRoute();
				if(hasTerminals){
					start=1;
					end=route.size()-1;
				}else{
					start=0;
					end=route.size();
				}
				for(int i=start;i<end;i++){
					if(constraints[route.get(i)-1]==null)
						constraints[route.get(i)-1]=cplex.linearNumExpr();
					constraints[route.get(i)-1].addTerm(1,x[r]);
				}
				numberOfRings.addTerm(1, x[r]);
			}
			//Add constraints to the model
			for(int i=0;i<constraints.length;i++){
				cplex.addEq(1,constraints[i]);
			}
			
			cplex.addEq(rings, numberOfRings);
			
			//Add objective function
			 cplex.addMinimize(of);
			 
			//Hide the output:
			 cplex.setOut(null);
			 cplex.setParam(IloCplex.Param.TimeLimit,GlobalParameters.MSH_ASSEMBLY_TIME_LIMIT);
			 cplex.setParam(IloCplex.Param.Threads, GlobalParameters.THREADS);
			 
			//Solve model
			 cplex.solve();
			
			//Store the solution:
			 
			 objectiveFunction = cplex.getObjValue();
			 
			 solution = new ArrayList<Route>();
			 
			 for(int r=0;r<routes.length;r++) {
				if(cplex.getValue(x[r]) > 0){
					numberOfRingsSolution++;
					solution.add(routes[r]);
					//System.out.println(r+" - "+routes[r].getAttribute(RouteAttribute.COST)+" - "+routes[r].getRoute().toString());
				}
			}
			
			 
		} catch (IloException e) {
			e.printStackTrace();
		}
		 
		
		return null;
	}
	
	public double[] assembleFractionalSolution(RoutePool pool) {
		Route[] routes=pool.toArray();
		double[] duals_rta = null;
		 try {
			//Build CPLEX environment
			cplex = new IloCplex();
			
			//Create covering/partitioning constraints and objective function
			IloRange[] constraints = new IloRange[nRequests];
			IloRange[] numberOfRings = new IloRange[1];
			IloObjective of = cplex.addMinimize();
			
			// Create the variables:
			
			x = new IloNumVar[pool.size()];
			
			// Create the constraints:
			for(int i=0;i<constraints.length;i++){
				constraints[i] = cplex.addRange(1, Double.MAX_VALUE,"VisitNode_"+(i+1));
			}
			
			numberOfRings[0] = cplex.addRange(rings, rings, "NumberOfRings");
			
			//Add terms to the covering/partitioning constraints and objective function
			int start,end;
			for(int r=0;r<routes.length;r++){
				IloColumn iloColumn = cplex.column(of,(double)routes[r].getAttribute(RouteAttribute.COST));
				
				ArrayList<Integer> route=(ArrayList<Integer>) routes[r].getRoute();
				if(hasTerminals){
					start=1;
					end=route.size()-1;
				}else{
					start=0;
					end=route.size();
				}
				for(int i=start;i<end;i++){
					iloColumn = iloColumn.and(cplex.column(constraints[route.get(i)-1],1));
				}
				iloColumn = iloColumn.and(cplex.column(numberOfRings[0],1));
				
				// Add the column:
				
				IloNumVar var = cplex.numVar(iloColumn,0,1,"x_"+r);
				cplex.add(var);
				x[r] = var;
			}
			
			//
		
			//Hide the output:
			 cplex.setOut(null);
			 //cplex.exportModel("./output/modelT.lp");
			 cplex.setParam(IloCplex.Param.TimeLimit,GlobalParameters.MSH_ASSEMBLY_TIME_LIMIT);
			 cplex.setParam(IloCplex.Param.Threads, GlobalParameters.THREADS);
			 
			//Solve model
			 cplex.solve();
			
			 // Recover the dual variables:
			 double[] duals_setPart = cplex.getDuals(constraints);
			 double[] duals_numRings = cplex.getDuals(numberOfRings);
			 
			 
			//Store the values in the answer array:
			 
			duals_rta = new double[constraints.length+1];
			for(int i=0;i<constraints.length;i++){
		
				duals_rta[i] = duals_setPart[i];
				
			}
			
			duals_rta[duals_rta.length-1] = duals_numRings[0];
			
			 
		} catch (IloException e) {
			e.printStackTrace();
		}
	
		return duals_rta;
	}
	 
}
