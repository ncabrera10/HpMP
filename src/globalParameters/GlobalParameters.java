package globalParameters;


public class GlobalParameters {

	// Relative paths:
	
		public static final String INSTANCE_FOLDER = GlobalParametersReader.<String>get("INSTANCE_FOLDER", String.class);
		public static final String RESULT_FOLDER = GlobalParametersReader.<String>get("RESULT_FOLDER", String.class);
		public static final String SOLUTIONS_FOLDER = GlobalParametersReader.<String>get("SOLUTIONS_FOLDER", String.class);

	// Precision:
		
		public static final int PRECISION = GlobalParametersReader.<Integer>get("PRECISION", Integer.class);
		public static final double DECIMAL_PRECISION = Math.pow(10, -PRECISION);

	// Experiment parameters:

		public static int SEED = GlobalParametersReader.<Integer>get("SEED", Integer.class);
		public static int THREADS = GlobalParametersReader.<Integer>get("THREADS", Integer.class);
		public static final boolean PRINT_IN_CONSOLE = GlobalParametersReader.<String>get("PRINT_IN_CONSOLE", String.class).equals("false") ? false:true;
		
		
	// MSH  parameters:
		
		public static final boolean SPLIT_IMPROVE_PETAL_LKH = GlobalParametersReader.<String>get("SPLIT_IMPROVE_PETAL_LKH", String.class).equals("false") ? false:true;
		public static final int MSH_MAX_POOL_SIZE = GlobalParametersReader.<Integer>get("MSH_MAX_POOL_SIZE", Integer.class);
		public static final int MSH_NUM_ITERATIONS = GlobalParametersReader.<Integer>get("MSH_NUM_ITERATIONS", Integer.class);
		public static final int MSH_SAMPLING_TIME_LIMIT = GlobalParametersReader.<Integer>get("MSH_SAMPLING_TIME_LIMIT", Integer.class);
		public static final int MSH_USE_HEURISTICS = GlobalParametersReader.<Integer>get("MSH_USE_HEURISTICS", Integer.class);
		public static final int MSH_RANDOM_FACTOR_HIGH = GlobalParametersReader.<Integer>get("MSH_RANDOM_FACTOR_HIGH", Integer.class);
		public static final int MSH_RANDOM_FACTOR_LOW = GlobalParametersReader.<Integer>get("MSH_RANDOM_FACTOR_LOW", Integer.class);
		public static final int MSH_ASSEMBLY_TIME_LIMIT = GlobalParametersReader.<Integer>get("MSH_ASSEMBLY_TIME_LIMIT", Integer.class);
		public static final int MSH_NUMBER_FORBID_ARCS = GlobalParametersReader.<Integer>get("MSH_NUMBER_FORBID_ARCS", Integer.class);
		public static final int MSH_LEARNING_EVERY_ITERATIONS = GlobalParametersReader.<Integer>get("MSH_LEARNING_EVERY_ITERATIONS", Integer.class);
		public static final boolean MSH_USE_LEARNING_DUALS = GlobalParametersReader.<String>get("MSH_USE_LEARNING_DUALS", String.class).equals("false") ? false:true;
		
		
		
}
