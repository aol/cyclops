package com.aol.cyclops.streams.operators;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import com.aol.cyclops.streams.StreamUtils;

public class SkipLastOperator<T> {

	Stream<T> stream;
	ArrayDeque<T> buffer;
	int skip;
	public SkipLastOperator(Stream<T> stream,int skip){
		buffer = new ArrayDeque<>(skip);
		this.stream = stream;
		this.skip = skip;
	}
	
	public Stream<T> skipLast(){
		Iterator<T> it = stream.iterator();
		return StreamUtils.stream(new Iterator<T>(){
			boolean finished =false;
			@Override
			public boolean hasNext() {
				while(buffer.size()<skip && it.hasNext()){
					buffer.add(it.next());
				}
				return finished = it.hasNext();
			}

			@Override
			public T next() {
				if(finished)
					throw new NoSuchElementException();
				return buffer.pop();
			}
			
		});
	}
}
