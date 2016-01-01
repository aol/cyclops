package com.aol.cyclops.javaslang.streams;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import javaslang.collection.Stream;
import uk.co.real_logic.agrona.concurrent.OneToOneConcurrentArrayQueue;

import com.aol.cyclops.javaslang.FromJDK;
import com.aol.cyclops.sequence.HotStream;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.spliterators.ClosingSpliterator;

public class HotStreamImpl<T> implements JavaslangHotStream<T>{

	private final Stream<T> stream;
	private final AtomicReferenceArray<Queue<T>> connections = new AtomicReferenceArray<>(10);
	private final AtomicBoolean open =new AtomicBoolean(true);
	private volatile int connected=0;
	
	public HotStreamImpl(Stream<T> stream){
		this.stream = stream;	
	}
	
	public JavaslangHotStream<T> init(Executor exec){
		CompletableFuture.runAsync( ()-> {
			
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
	

	


	@Override
	public Stream<T> connect() {
		return connect(new OneToOneConcurrentArrayQueue<T>(256));
	}

	@Override
	public Stream<T> connect(Queue<T> queue) {
		connections.getAndSet(connected, queue);
		connected++;
		return FromJDK.stream(SequenceM.fromStream(StreamSupport.stream(
                new ClosingSpliterator(Long.MAX_VALUE, queue,open), false)));
	}

	@Override
	public <R extends Stream<T>> R connectTo(Queue<T> queue, Function<Stream<T>, R> to) {
		return to.apply(connect(queue));
	}
	
}
