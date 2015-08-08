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
import com.aol.simple.react.util.SimpleTimer;

@AllArgsConstructor
public class BatchByTimeAndSize<U> implements Function<BiFunction<Long,TimeUnit,U>, Supplier<List<U>>> {

	private final Queue<U> queue;
	private final int size;
	private final long time;
	private final TimeUnit unit;
	
	@Override
	public Supplier<List<U>> apply(BiFunction<Long, TimeUnit, U> s) {
		
		return ()->{
            SimpleTimer timer = new SimpleTimer();
            long nanos = unit.toNanos(time);
            List<U> list = new ArrayList<>();
            try {
                do {
                    if(list.size()==size){
                        return list;
                    }
                     U result = s.apply(nanos-timer.getElapsedNanoseconds(), TimeUnit.NANOSECONDS);
                    if(result!=null)
						list.add(result);
                    
					
					
                   } while (timer.getElapsedNanoseconds()<nanos);
            } catch (ClosedQueueException e) {
            	throw new ClosedQueueException(list);
            }
            return list;
        };
	}
	
	public Function<BiFunction<Long,TimeUnit,U>, Supplier<Optional<List<U>>>> liftOptional(){
		return biF ->  () -> {
			try {
				return Optional.of(this.apply(biF).get());
			} catch (ClosedQueueException e) {
				
				return Optional.ofNullable((List<U>)e.getCurrentData());
			}
		};
	}

}
