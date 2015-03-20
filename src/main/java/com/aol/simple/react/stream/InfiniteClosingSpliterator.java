package com.aol.simple.react.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.aol.simple.react.async.ClosingSpliterator;
import com.aol.simple.react.async.Continueable;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;

public class InfiniteClosingSpliterator<T> implements Spliterator<T> {
    private long estimate;
    final Supplier<T> s;
    private final Continueable subscription;
    private final Queue queue;

    protected InfiniteClosingSpliterator(long estimate,Supplier<T> s,
    		Continueable subscription,
    		Queue queue) {
        this.estimate = estimate;
        this.s = s;
        this.subscription = subscription;
        this.queue = queue;
        this.subscription.addQueue(queue);
    }
    public InfiniteClosingSpliterator(long estimate,Supplier<T> s,
    		Continueable subscription) {
        this.estimate = estimate;
        this.s = s;
        this.subscription = subscription;
        this.queue = null;
    }

    @Override
    public long estimateSize() {
        return estimate;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }
    


	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		 Objects.requireNonNull(action);
		
			
        try{ 
        	
        	action.accept(s.get());
        	if(subscription.closed())
        		return false;
        	return true;
        }catch(ClosedQueueException e){
        	return false;
        }catch(Exception e){
        	
        	return false;
        }
        
	}

	@Override
	public Spliterator<T> trySplit() {
		
		return new InfiniteClosingSpliterator(estimate >>>= 1, s,subscription,queue);
	}

   
}


