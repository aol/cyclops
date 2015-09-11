package com.aol.cyclops.sequence.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@AllArgsConstructor
public class ReversingArraySpliterator<T> implements Spliterator<T>, ReversableSpliterator {
	
	
	private final Object[] array;
	@Getter @Setter
	private boolean reverse;
	    
	int index=0;
    @Override
    public long estimateSize() {
        return array.length;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }
    
    public ReversingArraySpliterator<T> invert(){
		setReverse(!isReverse());
		index = array.length-1;
		return this;
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		 Objects.requireNonNull(action);
		 
		 if(!reverse){
			 if(index<array.length && index>-1){
				 action.accept((T)array[index++]);
				 return true;
			 }
		 }
		 else{
			 if(index>-1 & index<array.length){
				 action.accept((T)array[index--]);
				 return true;
			 }
		 }
			return false;
        
	}

	@Override
	public Spliterator<T> trySplit() {
		
		return this;
	}

	@Override
	public ReversableSpliterator copy() {
		return new ReversingArraySpliterator<T>(array, reverse, index);
	}

   
}
