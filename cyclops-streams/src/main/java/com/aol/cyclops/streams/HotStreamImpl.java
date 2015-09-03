package com.aol.cyclops.streams;

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
					for(int i=0;i<connected;i++){
						connections.get(i).offer(a);
					}
					open.set(false);
				});
		},exec);
		return this;
	}
	public SequenceM<T> connect(){
		return connect(new OneToOneConcurrentArrayQueue<T>(256));
	}
	public <R extends Stream<T>> R connectTo(Function<SequenceM<T>,R> to) {
		return to.apply(connect(new OneToOneConcurrentArrayQueue<T>(256)));
	}
	@Override
	public SequenceM<T> connect(Queue<T> queue) {
		connections.getAndSet(connected++, queue);
		return StreamUtils.sequenceM(StreamSupport.stream(
                new ClosingSpliterator(Long.MAX_VALUE, queue,open), false));
	}

	@Override
	public <R extends Stream<T>> R connectTo(Queue<T> queue,Function<SequenceM<T>,R> to) {
		return to.apply(connect(queue));
	}
	
}
