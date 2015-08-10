package com.aol.simple.react.stream.traits.operators;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.util.SimpleTimer;

@AllArgsConstructor
public class OnePer<U> implements Function<Supplier<U>, Supplier<U>> {
	private final long time;
	private final TimeUnit unit;
	private static final ExceptionSoftener softener = ExceptionSoftener.singleton.factory
			.getInstance();
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
							softener.throwSoftenedException(e);
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
