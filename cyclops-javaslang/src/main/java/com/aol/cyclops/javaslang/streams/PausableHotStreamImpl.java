package com.aol.cyclops.javaslang.streams;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javaslang.collection.Stream;


public class PausableHotStreamImpl<T> extends BaseHotStreamImpl<T> implements PausableJavaslangHotStream<T>  {
	
	public PausableHotStreamImpl(Stream<T> stream) {
		super(stream);
	}

	public PausableJavaslangHotStream<T> init(Executor exec){
		CompletableFuture.runAsync( ()-> {
			
			stream.forEach(a->{
					pause.get().join();
					int local = connected;
					
					for(int i=0;i<local;i++){
					
						connections.get(i).offer(a);
					}
					
					
				});
				
				open.set(false); 
				
					
		},exec);
		return this;
	}
	public PausableJavaslangHotStream<T> paused(Executor exec){
		 super.paused(exec);
		 return this;
	}
	public void unpause(){
		super.unpause();
	}
	public void pause(){
		super.pause();
	}
}
