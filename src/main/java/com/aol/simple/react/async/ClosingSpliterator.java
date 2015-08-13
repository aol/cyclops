package com.aol.simple.react.async;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.subscription.Continueable;


public class ClosingSpliterator<T> implements Spliterator<T> {
        private long estimate;
        final Supplier<T> s;
        private final Continueable subscription;
        private final Queue queue;

        protected ClosingSpliterator(long estimate,Supplier<T> s,
        		Continueable subscription,
        		Queue queue) {
            this.estimate = estimate;
            this.s = s;
            this.subscription = subscription;
            this.queue = queue;
            this.subscription.addQueue(queue);
        }
        public ClosingSpliterator(long estimate,Supplier<T> s,
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
            	subscription.closeQueueIfFinished(queue);
             return true;
            }catch(ClosedQueueException e){
            
            	if(e.isDataPresent())
            		action.accept((T)e.getCurrentData());
            	return false;
            }catch(Exception e){
            	
            	return false;
            }finally {
            	
            }
            
		}

		@Override
		public Spliterator<T> trySplit() {
			
			return new ClosingSpliterator(estimate >>>= 1, s,subscription,queue);
		}

       
    }
  
    
