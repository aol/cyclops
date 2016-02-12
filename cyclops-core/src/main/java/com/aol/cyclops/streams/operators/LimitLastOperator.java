package com.aol.cyclops.streams.operators;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.stream.Stream;

import com.aol.cyclops.util.StreamUtils;

public class LimitLastOperator<T> {

	Stream<T> stream;
	ArrayDeque<T> buffer;
	int limit;
	public LimitLastOperator(Stream<T> stream,int limit){
		buffer = new ArrayDeque<>(limit);
		this.stream = stream;
		this.limit = limit;
	}
	
	public Stream<T> limitLast(){
		Iterator<T> it = stream.iterator();
		return StreamUtils.stream(new Iterator<T>(){

			@Override
			public boolean hasNext() {
				while(it.hasNext()){
					buffer.add(it.next());
					if(buffer.size()>limit)
						buffer.pop();
				}
				return buffer.size()>0;
			}

			@Override
			public T next() {
				return buffer.pop();
			}
			
		});
	}
}
