package msh;

import core.RoutePool;
import core.Solution;

public class DummyAssemblyFunction implements AssemblyFunction{

	@Override
	public Solution assembleSolution(Solution bound, RoutePool pool) {
		return bound;
	}

}
