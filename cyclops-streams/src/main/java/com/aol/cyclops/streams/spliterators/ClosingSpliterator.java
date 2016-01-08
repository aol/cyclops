package com.aol.cyclops.streams.spliterators;

import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ClosingSpliterator<T> implements Spliterator<T> {
    private long estimate;
   
   
    private final Queue<T> queue;
    private final AtomicBoolean open;

    public ClosingSpliterator(long estimate,	
    		Queue queue, AtomicBoolean open) {
        this.estimate = estimate;
        this.open = open;
        this.queue = queue;
       
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
		
			if(!open.get() && queue.size()==0){
				
				return false;
			}
        
        	T value;
        	if((value=queue.poll())!=null)
        		action.accept(nullSafe(value));
        	
        	return true;
        
	}

	private T nullSafe(T value) {
		return value;
	}


	@Override
	public Spliterator<T> trySplit() {
		
		return this;
	}

   
}
