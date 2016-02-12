package com.aol.cyclops.internal.stream.operators;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.util.StreamUtils;

import lombok.Value;
@Value
public class SkipWhileOperator<U> {

	Stream<U> stream;
	public Stream<U> skipWhile(Predicate<? super U> predicate){
		Iterator<U> it = stream.iterator();
		return StreamUtils.stream(new Iterator<U>(){
			U next;
			boolean nextSet = false;
			boolean init =false;
			@Override
			public boolean hasNext() {
				if(init)
					return it.hasNext();
				try{
					while(it.hasNext()){
						
						next = it.next();
						nextSet = true;
						
						if(!predicate.test(next))
							return true;
					
					}
					return false;
				}finally{
					init =true;
				}
			}

			@Override
			public U next() {
				if(!init){
					hasNext();
				}
				if(nextSet){
					nextSet = false;
					return next;
				}
				return it.next();
			}
			
		});
	}
}
