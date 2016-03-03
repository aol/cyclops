package com.aol.cyclops.internal.stream.operators.futurestream;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.data.async.Queue.QueueTimeoutException;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.util.SimpleTimer;

@AllArgsConstructor
public class BatchByTime<U> implements Function<BiFunction<Long,TimeUnit,U>, Supplier<Collection<U>>>{

	
	private final long time;
	private final TimeUnit unit;
	private final Continueable subscription;
	private final Queue<U> queue;
	private final Supplier<Collection<U>> factory;
	
	@Override
	public Supplier<Collection<U>> apply(BiFunction<Long, TimeUnit, U> s) {
		return () -> {
			
			
			Collection<U> list= new ArrayList<>();
			
			int passes=0;
			list = buildNextBatch(s,passes);
					
			
			
			return list;
		};
	
	}
	
	private Collection<U> buildNextBatch(BiFunction<Long, TimeUnit, U> s, int passes) {
		Collection<U> list = factory.get();
		SimpleTimer timer = new SimpleTimer();
		do {
			long timeout = Math.min(1000l,unit.toNanos(time)-timer.getElapsedNanoseconds());
				try{
					
						
					
						U result = s.apply(timeout, TimeUnit.NANOSECONDS);
						
						if(result!=null){
							
							
							
							list.add(result);
						
						}
					
				}catch(QueueTimeoutException e) {
					
					
		        }catch (ClosedQueueException e) {
		        	
		        	
					throw new ClosedQueueException(list);
				}
				
			} while (timer.getElapsedNanoseconds()<unit.toNanos(time));
		
		return list;
	}
	

}
