package com.aol.cyclops.javaslang.streams;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javaslang.collection.Stream;



public class NonPausableHotStream<T>  extends BaseHotStreamImpl<T> {
	public NonPausableHotStream(Stream<T> stream) {
		super(stream);
	}

	public JavaslangHotStream<T> init(Executor exec){
		CompletableFuture.runAsync( ()-> {
			pause.get().join();
			stream.forEach(a->{
					
					int local = connected;
					
					for(int i=0;i<local;i++){
					
						connections.get(i).offer(a);
					}
					
					
				});
				
				open.set(false); 
				
					
		},exec);
		return this;
	}
}
