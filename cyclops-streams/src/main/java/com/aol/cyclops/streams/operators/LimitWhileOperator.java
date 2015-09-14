package com.aol.cyclops.streams.operators;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.streams.StreamUtils;
@Value
public class LimitWhileOperator<U> {
	Stream<U> stream;
	public  Stream<U>  limitWhile(Predicate<? super U> predicate){
		Iterator<U> it = stream.iterator();
		return StreamUtils.stream(new Iterator<U>(){
			U next;
			boolean nextSet = false;
			boolean stillGoing =true;
			@Override
			public boolean hasNext() {
				if(!stillGoing)
					return false;
				if(nextSet)
					return stillGoing;
				
				if (it.hasNext()) {
					next = it.next();
					nextSet = true;
					if (!predicate.test(next)) {
						stillGoing = false;
					}
					
				} else {
					stillGoing = false;
				}
				return stillGoing;
				
					
			}

			@Override
			public U next() {
				
				if(nextSet){
					nextSet = false;
					return next;
				}
				
				
				U local = it.next();
				if(stillGoing){
					stillGoing = !predicate.test(local);
				}
				return local;
			}
			
		});
	}
}
