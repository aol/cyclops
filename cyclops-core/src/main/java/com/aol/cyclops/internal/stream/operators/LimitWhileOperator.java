package com.aol.cyclops.internal.stream.operators;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.util.StreamUtils;

import lombok.Value;
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
