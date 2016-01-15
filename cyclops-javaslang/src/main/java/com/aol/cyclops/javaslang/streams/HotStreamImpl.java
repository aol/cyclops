package com.aol.cyclops.javaslang.streams;

import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import javaslang.collection.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import uk.co.real_logic.agrona.concurrent.OneToOneConcurrentArrayQueue;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.javaslang.FromJDK;
import com.aol.cyclops.javaslang.sequence.ReactiveStream;
import com.aol.cyclops.scheduling.util.cron.CronExpression;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.spliterators.ClosingSpliterator;

public class HotStreamImpl<T> implements JavaslangHotStream<T>{
	@Getter(AccessLevel.PACKAGE)
	private final Stream<T> stream;
	private final AtomicReferenceArray<Queue<T>> connections = new AtomicReferenceArray<>(10);
	private final AtomicBoolean open =new AtomicBoolean(true);
	private volatile int connected=0;
	private final AtomicReference<CompletableFuture<Void>> pause = new AtomicReference<>(CompletableFuture.<Void>completedFuture(null));

	public HotStreamImpl(Stream<T> stream){
		this.stream = stream;	
	}
	
	public JavaslangHotStream<T> paused(Executor exec){
		pause();
		return init(exec);
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
	public boolean isPaused(){
		return pause.get().isDone();
	}
	private void unpause(){
		CompletableFuture<Void> current = pause.get();
		if(!current.isDone())
			current.complete(null);
	}
	private void pause(){
		
		pause.set(new CompletableFuture<Void>());
	}
	public JavaslangHotStream<T> schedule(String cron,ScheduledExecutorService ex){
		final Iterator<T> it = stream.iterator();
		return scheduleInternal(it,cron,ex);
		
	}
	private JavaslangHotStream<T> scheduleInternal(Iterator<T> it, String cron,ScheduledExecutorService ex){
		Date now = new Date();
		Date d = ExceptionSoftener.softenSupplier(()->new CronExpression(cron)).get().getNextInvalidTimeAfter(now);
		
		long delay = d.getTime() - now.getTime(); 
		
		ex.schedule(()->{
			synchronized(it){
				if(it.hasNext()){
					try{
						T next = it.next();
					
						int local = connected;
						
						for(int i=0;i<local;i++){
						
							connections.get(i).offer(next);
						}
						
					}
					finally{
						
							scheduleInternal(it,cron,ex);
						
					}
				 }else{
					 open.set(false);
				 }
			}
		}, delay, TimeUnit.MILLISECONDS);
		return this;
	}
	
	
	public JavaslangHotStream<T> scheduleFixedDelay(long delay,ScheduledExecutorService ex){
		final Iterator<T> it = stream.iterator();
		 ex.scheduleWithFixedDelay(()->{
			 synchronized(it){
				if(it.hasNext()){
					
						T next = it.next();
					
						int local = connected;
						
						for(int i=0;i<local;i++){
						
							connections.get(i).offer(next);
						}
						
					
				}else{
					 open.set(false);
				 }
			 }
		}, delay,delay,TimeUnit.MILLISECONDS);
		 return this;
		
	}
	public JavaslangHotStream<T> scheduleFixedRate(long rate,ScheduledExecutorService ex){
		final Iterator<T> it = stream.iterator();
		
		 ex.scheduleAtFixedRate(()->{
			 synchronized(it){
				if(it.hasNext()){
					
						T next = it.next();
					
						int local = connected;
						
						for(int i=0;i<local;i++){
						
							connections.get(i).offer(next);
						}
						
					
				}else{
					 open.set(false);
				 }
			 }
		}, 0,rate,TimeUnit.MILLISECONDS);
		 return this;
		
	}
	


	@Override
	public ReactiveStream<T> connect() {
		unpause();
		return connect(new OneToOneConcurrentArrayQueue<T>(256));
	}

	@Override
	public ReactiveStream<T> connect(Queue<T> queue) {
		unpause();
		connections.getAndSet(connected, queue);
		connected++;
		return ReactiveStream.fromJDK(SequenceM.fromStream(StreamSupport.stream(
                new ClosingSpliterator(Long.MAX_VALUE, queue,open), false)));
	}

	@Override
	public <R extends ReactiveStream<T>> R connectTo(Queue<T> queue, Function<ReactiveStream<T>, R> to) {
		return to.apply(connect(queue));
	}
	
}
