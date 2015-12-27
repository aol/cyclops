package com.aol.cyclops.streams;

import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.real_logic.agrona.concurrent.OneToOneConcurrentArrayQueue;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.scheduling.embedded.org.quartz.CronExpression;
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
	
	public HotStream<T> schedule(String cron,ScheduledExecutorService ex){
		final Iterator<T> it = stream.iterator();
		return scheduleInternal(it,cron,ex);
		
	}
	private HotStream<T> scheduleInternal(Iterator<T> it, String cron,ScheduledExecutorService ex){
		Date now = new Date();
		Date d = ExceptionSoftener.softenSupplier(()->new CronExpression(cron)).get().getNextInvalidTimeAfter(now);
		
		long delay = d.getTime() - now.getTime(); 
		
		ex.schedule(()->{
			if(it.hasNext()){
				try{
					T next = it.next();
				
					int local = connected;
					
					for(int i=0;i<local;i++){
					
						connections.get(i).offer(next);
					}
					
				}
				finally{
					if(it.hasNext()){
						scheduleInternal(it,cron,ex);
					}
				}
			 }
		}, delay, TimeUnit.MILLISECONDS);
		return this;
	}
	public HotStream<T> schedule(long delay,ScheduledExecutorService ex){
		final Iterator<T> it = stream.iterator();
		 ex.scheduleWithFixedDelay(()->{
			if(it.hasNext()){
				
					T next = it.next();
				
					int local = connected;
					
					for(int i=0;i<local;i++){
					
						connections.get(i).offer(next);
					}
					
				
			}
		}, delay,delay,TimeUnit.MILLISECONDS);
		 return this;
		
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
