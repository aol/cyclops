package com.aol.simple.react.stream;

import java.util.Iterator;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import com.aol.simple.react.async.Continueable;

@AllArgsConstructor
public class CloseableIterator<T> implements Iterator<T>{

	
	private final Iterator<T> iterator;
	private final Continueable subscription;
	
	public boolean hasNext(){
		if(!iterator.hasNext())
			close();
		return iterator.hasNext();
	}
	public void close(){
		subscription.closeAll();
	}
	public T next() {
		return iterator.next();
	}
	
}
