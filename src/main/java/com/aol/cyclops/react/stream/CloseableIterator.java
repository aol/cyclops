package com.aol.cyclops.react.stream;

import java.util.Iterator;

import lombok.AllArgsConstructor;

import com.aol.cyclops.react.async.Queue;
import com.aol.cyclops.react.async.subscription.Continueable;

@AllArgsConstructor
public class CloseableIterator<T> implements Iterator<T>{

	
	private final Iterator<T> iterator;
	private final Continueable subscription;
	private final Queue queue;
	
	public boolean hasNext(){
		if(!iterator.hasNext())
			close();
		return iterator.hasNext();
	}
	public void close(){
		subscription.closeAll(queue);
	}
	public T next() {
		T next = iterator.next();
		return next;
	}
	
}
