package com.aol.cyclops.streams;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.real_logic.agrona.concurrent.OneToOneConcurrentArrayQueue;

import com.aol.cyclops.sequence.HotStream;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.spliterators.ClosingSpliterator;

public class HotStreamImpl<T> implements HotStream<T>{

	private final Stream<T> stream;
	private final AtomicReferenceArray<Queue<T>> connections = new AtomicReferenceArray<>(10);
	private final AtomicBoolean open =new AtomicBoolean(true);
	private volatile int connected=0;
	
	public HotStreamImpl(Stream<T> stream){
		this.stream = stream;	
	}
	
	public HotStream<T> init(Executor exec){
		CompletableFuture.runAsync( ()-> {
			
			stream.forEach(a->{
					int local = connected;
					
					for(int i=0;i<local;i++){
					
						connections.get(i).offer(a);
					}
					
					
				});
				
				open.set(false); 
				System.out.println("finished!"); 
					
		},exec);
		return this;
	}
	@Override
	public SequenceM<T> connect(){
		return connect(new OneToOneConcurrentArrayQueue<T>(256));
	}
	
	@Override
	public SequenceM<T> connect(Queue<T> queue) {
		connections.getAndSet(connected, queue);
		connected++;
		return StreamUtils.sequenceM(StreamSupport.stream(
                new ClosingSpliterator(Long.MAX_VALUE, queue,open), false),Optional.empty());
	}

	@Override
	public <R extends Stream<T>> R connectTo(Queue<T> queue,Function<SequenceM<T>,R> to) {
		return to.apply(connect(queue));
	}
	
}
