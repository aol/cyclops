package com.aol.simple.react.stream.traits.operators;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.Queue.QueueTimeoutException;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.util.SimpleTimer;
/*
 * considerations
 * 
 *  1. read time out (loop again)
 *  2. Queue closed - return result
 *  3. entry should always be non-empty while queue is open (at list one result)
 */

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
						
						if(result!=null)
							list.add(result);
					
				}catch(QueueTimeoutException e) {
					//retry if queue access timed out but not closed
					
					
		        }catch (ClosedQueueException e) {
					
					throw new ClosedQueueException(list);
				}
				
			} while (timer.getElapsedNanoseconds()<unit.toNanos(time));
		return list;
	}
	public Function<BiFunction<Long,TimeUnit,U>, Supplier<Optional<Collection<U>>>> liftOptional(){
		return biF ->  () -> {
			try {
				return Optional.of(this.apply(biF).get());
			} catch (ClosedQueueException e) {
				
				return Optional.ofNullable((List<U>)e.getCurrentData()).
								flatMap(list-> list.isEmpty() ? Optional.empty() : Optional.of(list));
			}
		};
	}

}
