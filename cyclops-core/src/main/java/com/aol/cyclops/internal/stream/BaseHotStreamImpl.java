package com.aol.cyclops.internal.stream;

import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.real_logic.agrona.concurrent.OneToOneConcurrentArrayQueue;

import com.aol.cyclops.internal.stream.spliterators.ClosingSpliterator;
import com.aol.cyclops.control.SequenceM;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.util.stream.StreamUtils;

public abstract class BaseHotStreamImpl<T> extends IteratorHotStream<T> implements HotStream<T>{

	protected final Stream<T> stream;
	
	
	public BaseHotStreamImpl(Stream<T> stream){
		this.stream = stream;	
	}
	
	public HotStream<T> paused(Executor exec){
		pause();
		return init(exec);
	}
	public abstract HotStream<T> init(Executor exec);
	
	public HotStream<T> schedule(String cron,ScheduledExecutorService ex){
		final Iterator<T> it = stream.iterator();
		scheduleInternal(it,cron,ex);
		return this;
		
	}
	
	
	
	public HotStream<T> scheduleFixedDelay(long delay,ScheduledExecutorService ex){
		final Iterator<T> it = stream.iterator();
		scheduleFixedDelayInternal(it,delay,ex);
		return this;
		
	}
	public HotStream<T> scheduleFixedRate(long rate,ScheduledExecutorService ex){
		final Iterator<T> it = stream.iterator();
		scheduleFixedRate(it,rate,ex);
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
		unpause();
		return StreamUtils.sequenceM(StreamSupport.stream(
                new ClosingSpliterator(Long.MAX_VALUE, queue,open), false),Optional.empty());
	}

	@Override
	public <R extends Stream<T>> R connectTo(Queue<T> queue,Function<SequenceM<T>,R> to) {
		return to.apply(connect(queue));
	}
	
}
