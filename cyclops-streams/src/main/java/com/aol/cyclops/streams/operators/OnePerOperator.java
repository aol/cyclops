package com.aol.cyclops.streams.operators;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.streams.StreamUtils;
@Value
public class OnePerOperator<T> {
	Stream<T> stream;
	public Stream<T> onePer( long time, TimeUnit t) {
		Iterator<T> it = stream.iterator();
		long next = t.toNanos(time);
		return StreamUtils.stream(new Iterator<T>(){
			volatile long last = -1;
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public T next() {
				T nextValue = it.next();
				LockSupport.parkNanos(next-System.nanoTime()-last);
				
				last= System.nanoTime();
				return nextValue;
			}
			
		});
	}
}
