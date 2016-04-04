package com.aol.cyclops.internal.stream.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntConsumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


public class ReversingRangeIntSpliterator implements Spliterator.OfInt, ReversableSpliterator {
  
    private final int min;
    private final int max;
    private int index;
    
    @Getter @Setter
    private boolean reverse;
   
    public ReversingRangeIntSpliterator(int min, int max, boolean reverse) {
        this.min = Math.min(min,max)-1;
        this.max = Math.max(min,max);
        this.reverse = this.max >= this.min ? reverse : !reverse;
        this.index = Math.min(min,max);
    }
    public ReversableSpliterator invert(){
		setReverse(!isReverse());
		index = max-1;
		return this;
	}

    @Override
    public boolean tryAdvance(IntConsumer consumer) {
        Objects.requireNonNull(consumer);
        if(!reverse){
        	if(index<max && index>min){
        		consumer.accept(index++);
        		return true;
        	}
        }
        if(reverse){
        	if(index>min && index<max){
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
    public Spliterator.OfInt trySplit() {
        return this;
    }



	


	


	@Override
	public ReversableSpliterator copy() {
		return new ReversingRangeIntSpliterator(index, max, reverse);
	}

   

    
}