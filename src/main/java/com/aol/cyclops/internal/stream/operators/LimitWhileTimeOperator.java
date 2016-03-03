package com.aol.cyclops.internal.stream.operators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Value;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.util.stream.StreamUtils;
@AllArgsConstructor
public class LimitWhileTimeOperator<U> {
    
    private final  Stream<U> stream;
    
	public  Stream<U>  limitWhile(long time, TimeUnit unit){
		Iterator<U> it = stream.iterator();
		long start = System.nanoTime();
		long allowed = unit.toNanos(time);
		return StreamUtils.stream(new Iterator<U>(){
			U next;
			boolean stillGoing =true;
			@Override
			public boolean hasNext() {
				stillGoing = System.nanoTime()-start < allowed;
				if(!stillGoing)
					return false;
				return it.hasNext();
					
			}

			@Override
			public U next() {
				if(!stillGoing)
					throw new NoSuchElementException();
				
				U val = it.next();
				stillGoing = System.nanoTime()-start < allowed;
				return val;
				
			}
			
		});
	}
}
