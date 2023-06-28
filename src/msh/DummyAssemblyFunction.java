package msh;

import core.RoutePool;
import core.Solution;

public class DummyAssemblyFunction implements AssemblyFunction{

	@Override
	public Solution assembleSolution(Solution bound, RoutePool pool) {
		return bound;
	}

	@Override
	public Solution assembleSolution(RoutePool pool) {
		return null;
	}

	@Override
	public double[] assembleFractionalSolution(RoutePool pool) {
		return null;
	}
	
	
}
