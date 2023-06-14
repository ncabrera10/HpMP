package msh;

import core.RoutePool;
import core.Solution;


public interface AssemblyFunction {
	
	public Solution assembleSolution(Solution bound, RoutePool pool);
	
}
