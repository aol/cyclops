package com.aol.simple.react.threads;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.ReactBuilder;

/**
 * Maintain a pool of x-react builders
 * x-react builders (SimpleReact, EagerReact, LazyReact) can be extracted and returned to the pool externally
 * or Streams creating functions can be supplied to the ReactPool which will select an x-react builder, run the stream and return
 *  the x-react builder to the pool
 * 
 * @author johnmcclean
 *
 * @param <REACTOR> x-react builder type (SimpleReact, EagerReact, LazyReact)
 */
public class ReactPool<REACTOR extends ReactBuilder> {
	
	private final BlockingQueue<REACTOR> queue;
	private final ExceptionSoftener softener = ExceptionSoftener.singleton.factory
			.getInstance();
	private final Supplier<REACTOR> supplier;
	
	private ReactPool(int size){
		queue = new LinkedBlockingQueue<REACTOR>(size);
		supplier = null;
	}
	private ReactPool(){
		queue = new LinkedBlockingQueue<REACTOR>();
		supplier = null;
	}
	private ReactPool(BlockingQueue<REACTOR> queue){
		this.queue = queue;
		supplier = null;
	}
	private ReactPool(Supplier<REACTOR> supplier){
		this.queue = new LinkedBlockingQueue<REACTOR>();
		this.supplier = supplier;
	}
	
	/**
	 * If all REACTORs are in use calling react will block.
	 * 
	 * @param reactors Create a bounded pool of the specified REACTORs
	 * @return ReactPool
	 */
	public static <REACTOR extends ReactBuilder> ReactPool<REACTOR> boundedPool(Collection<REACTOR> reactors){
		 ReactPool<REACTOR> r = new ReactPool<>(reactors.size());
		 reactors.forEach(r::populate);
		 return r;
	}
	/**
	 * If all REACTORs are in use calling react will block.
	 * 
	 * @param reactors Create a unbounded pool of the specified REACTORs, additional REACTORs can be added via populate
	 * @return ReactPool
	 */
	public static <REACTOR extends ReactBuilder> ReactPool<REACTOR> unboundedPool(Collection<REACTOR> reactors){
		 ReactPool<REACTOR> r = new ReactPool<>();
		 reactors.forEach(r::populate);
		 return r;
	}
	/**
	 * If all REACTORs are in use calling react will create a new REACTOR to handle the extra demand.
	 * 
	 * Generate an elastic pool of REACTORs
	 * 
	 * @param supplier To create new REACTORs
	 * @return ReactPool
	 */
	public static <REACTOR extends ReactBuilder> ReactPool<REACTOR> elasticPool(Supplier<REACTOR> supplier){
		 return new ReactPool<>(supplier);
		
	}
	
	/**
	 * @return Synchronous pool requires consumers and producers of the ReactPool to be in sync
	 */
	public static <REACTOR extends BaseSimpleReact> ReactPool<REACTOR> syncrhonousPool(){
		
		ReactPool<REACTOR> r = new ReactPool<>(new SynchronousQueue<>());
		
		return r;
	}
	/**
	 * @param next REACTOR to add to the Pool
	 */
	public void populate(REACTOR next){
	
		try {
			queue.put(next);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			softener.throwSoftenedException(e);
		}
		
	}
	
	
	
	/**
	 * @param fn Function that operates on a REACTOR - typically will build an execute a Stream using that REACTOR. 
	 * 				This method will extract and return the REACTOR to the pool.
	 * @return typically will return the result of Stream execution (result of fn.apply(reactor))
	 */
	public<T> T react(Function<REACTOR,T> fn){
		REACTOR reactor = null;
		
		try{
			reactor = nextReactor();
			return fn.apply(reactor);
		}finally{
		
			if(reactor!=null)
				queue.offer(reactor);
			
		}
	}
	/**
	 * @return Next available REACTOR from Pool
	 */
	public REACTOR nextReactor()  {
		REACTOR reactor = queue.poll();
		try{
	
		if(reactor==null){
			if(isElastic()){
				reactor = supplier.get();
				
			}
			else
				reactor = queue.take();
		}
		}catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			softener.throwSoftenedException(e);
			return null;
		}
		return reactor;
	}
	private boolean isElastic() {
		return supplier!=null;
	}
	
}
