package com.aol.cyclops.react.stream.traits.operators;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.react.async.Queue.ClosedQueueException;
import com.aol.cyclops.react.util.SimpleTimer;

@AllArgsConstructor
public class OnePer<U> implements Function<Supplier<U>, Supplier<U>> {
	private final long time;
	private final TimeUnit unit;
	
	@Override
	public Supplier<U> apply(Supplier<U> s) {
		
			
			return () -> {
				SimpleTimer timer=  new SimpleTimer();
				Optional<U> result = Optional.empty();
				try {
					
						
						try {
							long elapsedNanos= unit.toNanos(time)- timer.getElapsedNanoseconds();
							long millis = elapsedNanos/1000000;
							int nanos = (int)(elapsedNanos - millis*1000000);
							Thread.sleep(Math.max(0,millis),Math.max(0,nanos));
						} catch (InterruptedException e) {
							throw ExceptionSoftener.throwSoftenedException(e);
						}
						result = Optional.of(s.get());
						
				} catch (ClosedQueueException e) {
					if(result.isPresent())
						throw new ClosedQueueException(result);
					else
						throw new ClosedQueueException();
				}
				return result.get();
			};
		
	}
	public Function<Supplier<U>, Supplier<Optional<U>>> liftOptional(){
		return biF ->  () -> {
			try {
				return Optional.of(this.apply(biF).get());
			} catch (ClosedQueueException e) {
				
				return Optional.ofNullable((U)e.getCurrentData());
			}
		};
	}


}
