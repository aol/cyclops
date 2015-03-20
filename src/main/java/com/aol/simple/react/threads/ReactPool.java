package com.aol.simple.react.threads;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.stream.BaseSimpleReact;

public class ReactPool<REACTOR extends BaseSimpleReact> {
	
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
	
	public static <REACTOR extends BaseSimpleReact> ReactPool<REACTOR> boundedPool(Collection<REACTOR> reactors){
		 ReactPool<REACTOR> r = new ReactPool<>(reactors.size());
		 reactors.forEach(r::populate);
		 return r;
	}
	public static <REACTOR extends BaseSimpleReact> ReactPool<REACTOR> unboundedPool(Collection<REACTOR> reactors){
		 ReactPool<REACTOR> r = new ReactPool<>();
		 reactors.forEach(r::populate);
		 return r;
	}
	public static <REACTOR extends BaseSimpleReact> ReactPool<REACTOR> elasticPool(Supplier<REACTOR> supplier){
		 return new ReactPool<>(supplier);
		
	}
	
	public static <REACTOR extends BaseSimpleReact> ReactPool<REACTOR> syncrhonousPool(){
		
		ReactPool<REACTOR> r = new ReactPool<>(new SynchronousQueue<>());
		
		return r;
	}
	public void populate(REACTOR next){
	
		try {
			queue.put(next);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			softener.throwSoftenedException(e);
		}
		
	}
	
	
	
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
