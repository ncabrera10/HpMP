package msh;

import java.util.ArrayList;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
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
	
}
