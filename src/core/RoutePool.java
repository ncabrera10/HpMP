package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import globalParameters.GlobalParameters;

/**
 * Implements a route pool. Technically, a route pool is simply a set of routes with no particular correlation.
 * Route pools are usually used as long term memory in VRP heuristics.
 * Some examples can be found in <a href:"http://dx.doi.org/10.1016/j.trc.2015.09.009">Montoya et al. 2016</a>, 
 * <a href:"http://link.springer.com/article/10.1007/s10732-015-9281-6">Mendoza, Rousseau and Villegas 2016</a>,
 * <a href:"http://www.springerlink.com/content/g3j763126q0tr814/">Mendoza and Vilelgas 2013</a>.</br>
 * Routes are stored in the pool in two phases. First, client classes submit the routes through method {@link #add(Route)}. 
 * The submitted route joins a queue. In the second phase, the route pool dequeues the route and runs a series of
 * {@link PoolFilteringRule}s before effectively storing the route in the pool.</br>
 * 
 * To improve computational performance, the route pool runs on an independent thread.</br>
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Oct 6, 2016
 *
 */
public final class RoutePool implements Runnable{
	/**
	 * Flag set to true if the route pool is actively storing routes and false otherwise
	 */
	private boolean isStoring;


	private LinkedBlockingDeque<Route> queue=new LinkedBlockingDeque<Route>();
	/**
	 * Timeout between two calls to dequeue routes from <code>queue</code>
	 */
	private long timeout=GlobalParameters.TIME_OUT_POOL_MS;
	/**
	 * Time utins for <code>timeout</code>
	 */
	private TimeUnit tUnit=TimeUnit.MILLISECONDS;
	/**
	 * Route pool
	 */
	private HashMap<Integer,Route> pool= new HashMap<>();
	//TODO think if a concurrent data structure is needed to store the pool
	/**
	 * A reference to the thread managing the pool
	 */
	private Thread t;
	/**
	 * The set of filtering rules
	 */
	private ArrayList<PoolFilteringRule> rules=new ArrayList<>();
	/**
	 * A reference to an object responsible for computing the storage and lookup route keys
	 */
	private RouteHashCode hashCode;

	/**
	 * A counter of the number of times the sampling phase generates a route that was already contained in the pool.
	 */
	private int numTimesAlreadyInside;
	
	/**
	 * A counter of the number of times the sampling phase generates a new route.
	 */
	private int numTimesNewRoute;
	
	/**
	 * Constructs a new {@lnik RoutePool} without any route filtering rule and a default {@link RouteHashCode}. 
	 * 
	 * @see {@link PoolFilteringRule} 
	 * @see {@link JVRAEnv}
	 */
	public RoutePool(){
		this.queue=new LinkedBlockingDeque<>();
		this.pool=new HashMap<>();
		this.hashCode=JVRAEnv.getRouteHashCodeFactory().build();
	}

	//TODO write other constructors

	/**
	 * Starts the route pool. After a call to this method, the thread running the pool starts removing routes from the
	 * waiting queue, runing the filtering rules on them, and storing them in the pool.
	 */
	public final synchronized void start(){
		if(this.isStoring)
			throw new IllegalStateException("The route pool is already started");
		this.t=new Thread(this,"Route Pool");
		t.start();
		this.isStoring=true;
		if(GlobalParameters.PRINT_IN_CONSOLE) {
			Logger.getLogger("EXECUTION").log(Level.INFO,Thread.currentThread().getName()+" started the route pool");
		}
	}

	/**
	 * Stops the thread running the pool. After a call to this method, the thread running the pool will finish 
	 * emptying the waiting queue and then stop. The calling thread will be suspended until the waiting queue is 
	 * emptyied. Any route added to the pool through method {@link #add(Route)} 
	 * after a call to this method may or may not be dequeued and stored in the pool. Indeed, if at some point after
	 * the call the queue is emptied, the thread running the pool would imediately stop its
	 * execution. If new routes are added to the pool after the thread stops, they will remain in queue until
	 * a new call to {@link #start()}.
	 */
	public final synchronized void stop(){
		
		if(!this.isStoring)
			throw new IllegalStateException("The route pool has not been started");
		this.isStoring=false;
		if(this.queue.size()>0)
			try {
				this.t.join(); //the calling thread waits until the pool thread finishes dequeuing routes.
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		if(GlobalParameters.PRINT_IN_CONSOLE) {
			Logger.getLogger("EXECUTION").log(Level.INFO,Thread.currentThread().getName()+" stoped the route pool");
			
		}
		
	}

	@Override
	public final void run(){
		Route r;
		while(isStoring||queue.size()!=0){
			try {
				r = this.queue.poll(timeout,tUnit);
				if(r!=null){
					for(PoolFilteringRule rule:rules){
						r=rule.filter(r, pool);
					}
					if(contains(r)) {
						numTimesAlreadyInside++;
					}else {
						this.pool.put(this.hashCode.compute(r),r);
						numTimesNewRoute++;
					}
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * @return the number of routes currently stored in the pool
	 */
	public final synchronized int size(){
		return this.pool.size();
	}
	/**
	 * Clears the route pool (i.e., removes all routes from the pool). Right after a call to this method {@link #size()} should
	 * return 0.
	 */
	public void clear(){
		this.pool.clear();
		if(GlobalParameters.PRINT_IN_CONSOLE) {
			Logger.getLogger("EXECUTION").log(Level.INFO,Thread.currentThread().getName()+" cleared the route pool");
		}
	}
	/**
	 * 
	 * @return an iterator to the routes stored in the pool
	 */
	public Iterator<Route> iterator(){
		return this.pool.values().iterator();
	}
	/**
	 * 
	 * @return the number of routes waiting to be stored
	 */
	public int inQueue(){
		return this.queue.size();
	}
	/**
	 * Adds a route to the route pool
	 * @param r the route to add
	 */
	public void add(Route r){
		this.queue.add(r); //LinkedBlockedQueue should provie thread-safe implementation for this
	}
	
	/**
	 * Checks if the pool contains a given route
	 * @param r the route
	 * @return true if the pool contains the route and false otherwise
	 */
	public synchronized boolean contains(Route r){
		//return this.pool.containsValue(this.hashCode.compute(r));
		return this.pool.containsKey(this.hashCode.compute(r));
	}
	
	//TODO make thread safe
	//TODO document method
	public synchronized Route[] toArray(){
		
		Route[] array=new Route[this.pool.size()];
		Iterator<Route> it=this.pool.values().iterator();
		int r=0;
		while(it.hasNext()){
			array[r]=it.next();
			r++;
		}			
		return array;		
	}


	/**
	 * @return the numTimesAlreadyInside
	 */
	public int getNumTimesAlreadyInside() {
		return numTimesAlreadyInside;
	}

	/**
	 * @param numTimesAlreadyInside the numTimesAlreadyInside to set
	 */
	public void setNumTimesAlreadyInside(int numTimesAlreadyInside) {
		this.numTimesAlreadyInside = numTimesAlreadyInside;
	}

	/**
	 * @return the numTimesNewRoute
	 */
	public int getNumTimesNewRoute() {
		return numTimesNewRoute;
	}

	/**
	 * @param numTimesNewRoute the numTimesNewRoute to set
	 */
	public void setNumTimesNewRoute(int numTimesNewRoute) {
		this.numTimesNewRoute = numTimesNewRoute;
	}
	
	
}
