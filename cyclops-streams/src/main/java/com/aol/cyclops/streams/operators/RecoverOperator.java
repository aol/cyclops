package com.aol.cyclops.streams.operators;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.streams.StreamUtils;
@Value
public class RecoverOperator<T> {
	Stream<T> stream;
	public Stream<T> recover(Function<Throwable,T> fn){
		Iterator<T> it = stream.iterator();
		
		return StreamUtils.stream(new Iterator<T>(){
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public T next() {
				try{
					return it.next();
				}catch(Throwable t){
					return fn.apply(t);
				}
			}
			
		});
	}
}
