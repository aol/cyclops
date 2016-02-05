package com.aol.cyclops.streams.operators;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.streams.StreamUtils;
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
