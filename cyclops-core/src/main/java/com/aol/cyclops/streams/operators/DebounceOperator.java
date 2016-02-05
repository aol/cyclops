package com.aol.cyclops.streams.operators;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.streams.StreamUtils;
@Value
public class DebounceOperator<T> {
	Stream<T> stream;
	public Stream<T> debounce(long time, TimeUnit t){
		Iterator<T> it = stream.iterator();
		long timeNanos = t.toNanos(time);
		return StreamUtils.stream(new Iterator<T>(){
			volatile long last = 0;
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public T next() {
				long elapsedNanos = 1;
				T nextValue=null;
				while(elapsedNanos>0 && it.hasNext()){
						
						nextValue = it.next();
						if(last==0){
							last= System.nanoTime();
							return nextValue;
						}
						elapsedNanos= timeNanos - (System.nanoTime()-last);
				}
				
				
				
				last= System.nanoTime();
				if(it.hasNext())
					return nextValue;
				else if(elapsedNanos <=0)
					return nextValue;
				else
					return (T)DEBOUNCED;
			}
			
		}).filter(i->i!=DEBOUNCED);
	}
	
	private final static Object DEBOUNCED = new Object();
}
