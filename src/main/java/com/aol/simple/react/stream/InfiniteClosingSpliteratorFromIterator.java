package com.aol.simple.react.stream;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.subscription.Continueable;

public class InfiniteClosingSpliteratorFromIterator<T>  implements Spliterator<T> {
	    private long estimate;
	    final Iterator<T> it;
	    private final Continueable subscription;
	   

	    protected InfiniteClosingSpliteratorFromIterator(long estimate,Iterator<T> it,
	    		Continueable subscription) {
	        this.estimate = estimate;
	        this.it = it;
	        this.subscription = subscription;
	       
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
	        	
	        	action.accept(it.next());
	        	if(subscription.closed())
	        		return false;
	        	return true;
	        }catch(ClosedQueueException e){
	        	return false;
	        }catch(Exception e){
	        	e.printStackTrace();
	        	return false;
	        }
	        
		}

		@Override
		public Spliterator<T> trySplit() {
			
			return new InfiniteClosingSpliteratorFromIterator(estimate >>>= 1, it,subscription);
		}

	   
	}
