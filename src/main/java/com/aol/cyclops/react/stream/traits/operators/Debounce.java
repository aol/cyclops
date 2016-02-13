package com.aol.cyclops.react.stream.traits.operators;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

import com.aol.cyclops.react.async.Queue.ClosedQueueException;
import com.aol.cyclops.react.async.Queue.QueueTimeoutException;
import com.aol.cyclops.react.util.SimpleTimer;

@AllArgsConstructor
public class Debounce<T> implements Function<Supplier<T>, Supplier<T>>{
	
	private final long  timeNanos;
	
	private long elapsedNanos= 1;
	private final boolean reset;
	@Override
	public Supplier<T> apply(Supplier<T> t) {
		return () -> {
			SimpleTimer timer=  new SimpleTimer();
			Optional<T> result = Optional.empty();
			try {
				if(reset)
					elapsedNanos= 1;
				while(elapsedNanos>0){
					try{
						result = Optional.of(t.get());
						elapsedNanos= timeNanos - timer.getElapsedNanoseconds();
					}catch(QueueTimeoutException e) {
						//retry if queue access timed out but not closed
		            }
				
				}
					
					
					
			} catch (ClosedQueueException e) {
				if(result.isPresent())
					throw new ClosedQueueException(result.get());
				else
					throw new ClosedQueueException();
			}
			return result.get();
		};
	}
	
	public Function<Supplier<T>, Supplier<Optional<T>>> liftOptional(){
		return biF ->  () -> {
			try {
				return Optional.of(this.apply(biF).get());
			} catch (ClosedQueueException e) {
				
				return Optional.ofNullable((Optional<T>)e.getCurrentData())
							.flatMap(t->t);
			}
		};
	}
}
