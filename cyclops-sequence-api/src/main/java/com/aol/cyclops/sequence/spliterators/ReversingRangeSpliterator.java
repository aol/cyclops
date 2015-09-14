package com.aol.cyclops.sequence.spliterators;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ReversingRangeSpliterator implements Spliterator.OfInt, ReversableSpliterator {
  
    private int index;
    private final int max;
    @Getter @Setter
    private boolean reverse;
   
    public ReversableSpliterator invert(){
		setReverse(!isReverse());
		index = max-1;
		return this;
	}

    @Override
    public boolean tryAdvance(IntConsumer consumer) {
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
    public Spliterator.OfInt trySplit() {
        return this;
    }



	


	


	@Override
	public ReversableSpliterator copy() {
		return new ReversingRangeSpliterator(index, max, reverse);
	}

    
}