package com.aol.cyclops.internal.react.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.react.async.subscription.Continueable;

public class InfiniteClosingSpliteratorFromSupplier<T>  implements Spliterator<T> {
	    private long estimate;
	    final Supplier<T> it;
	    private final Continueable subscription;
	   

	    public InfiniteClosingSpliteratorFromSupplier(long estimate,Supplier<T> it,
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
	        	
	        	action.accept(it.get());
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
			
			return new InfiniteClosingSpliteratorFromSupplier(estimate >>>= 1, it,subscription);
		}

	   
	}
