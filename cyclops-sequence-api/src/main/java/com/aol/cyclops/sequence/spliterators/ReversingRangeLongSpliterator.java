package com.aol.cyclops.sequence.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.LongConsumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ReversingRangeLongSpliterator implements Spliterator.OfLong, ReversableSpliterator {
  
    private long index;
    private final long max;
    @Getter @Setter
    private boolean reverse;
   
    public ReversableSpliterator invert(){
		setReverse(!isReverse());
		index = max-1;
		return this;
	}

    @Override
    public boolean tryAdvance(LongConsumer consumer) {
        Objects.requireNonNull(consumer);
        if(!reverse){
        	if(index<max && index>-1){
        		consumer.accept(index++);
        		return true;
        	}
        }
        if(reverse){
        	if(index>-1 && index<max){
        		consumer.accept(index--);
        		return true;
        	}
        }
        return false;
    }

    

    @Override
    public long estimateSize() {
      return max;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }
   
    @Override
    public Spliterator.OfLong trySplit() {
        return this;
    }



	


	


	@Override
	public ReversableSpliterator copy() {
		return new ReversingRangeLongSpliterator(index, max, reverse);
	}

    
}