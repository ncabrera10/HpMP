/*
* Log of changes:
* 2012.
*/
package msh;

import java.util.ArrayList;

import core.Route;
import core.RouteAttribute;
import core.RoutePool;
import core.Solution;
import localsolver.LSExpression;
import localsolver.LSModel;
import localsolver.LSObjectiveDirection;
import localsolver.LSOperator;
import localsolver.LocalSolver;


/**
 * @author Jorge E. Mendoza (jorge.mendoza@uco.fr)
 * @since 2012.03.16
 */
public class LocalSolverSetPartitioningSolver implements AssemblyFunction{
	
	/**
	 * Holds the total number of customers
	 */
	private int nbCustomers=0;
	private boolean terminals=true;
	
	public LocalSolverSetPartitioningSolver(int nbCustomers){
		this.nbCustomers=nbCustomers;
	}
	
	
	/* (non-Javadoc)
	 * @see setCoveringSolvers.ISetCoveringSolver#solve(java.util.Collection)
	 */
	@Override
	public Solution assembleSolution(Solution bound, RoutePool pool) {
		
		//Instantiate Local Solver
		LocalSolver localsolver = new LocalSolver();
        LSModel model = localsolver.getModel();
	    
	    Route[] routes=pool.toArray();
	    
	    //Build decision variables and objective function
	    LSExpression[] x = new LSExpression[pool.size()];
	    LSExpression of = model.createExpression(LSOperator.Sum);
	    for(int r=0;r<routes.length;r++){//add columns
	    	x[r] = model.createExpression(LSOperator.Bool);
	    	double columnCost=(Double)routes[r].getAttribute(RouteAttribute.COST);
	    	of.addOperand(model.createExpression(LSOperator.Prod, columnCost, x[r]));
	    }	    
	    model.addObjective(of, LSObjectiveDirection.Minimize);
	    
	    //Build partitioning constraints
	    LSExpression[] partitioningConstraints=new LSExpression[this.nbCustomers];
	    int start,end;
	    for(int r=0;r<routes.length;r++){//for every route
	    	ArrayList<Integer> sequence=(ArrayList<Integer>)routes[r].getRoute();
	    	//Check if the depots are included at the begining and end of the routes or not
	    	if(terminals){
	    		start=1;
	    		end=sequence.size()-1;
	    	}else{
	    		start=0;
	    		end=sequence.size();
	    	}
	    	for(int i=start;i<end;i++){//travel the sequence of customers
	    		LSExpression partitioningConstraint=partitioningConstraints[sequence.get(i)-1]; //TODO are the depots in or not?
	    		if(partitioningConstraint==null){
	    			partitioningConstraint=model.createExpression(LSOperator.Sum); //initialize the constraint
	    			partitioningConstraints[sequence.get(i)-1]=partitioningConstraint;
	    		}
	    		partitioningConstraint.addOperand(x[r]);
	    	}
	    }
	    
	    //Add partitioning constraints to the model
	    for(int i=0; i<partitioningConstraints.length; i++){
	    	model.addConstraint(model.createExpression(LSOperator.Eq,partitioningConstraints[i],1));
	    }
	    
	    //Solve model
	    model.close();
	    //LSPhase phase = localsolver.createPhase();
	    //phase.setTimeLimit(30);
	    localsolver.solve();
	    
		return null;
	}

}
