package com.aol.simple.react.stream;

import java.util.Iterator;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import com.aol.simple.react.async.Continueable;

@AllArgsConstructor
public class CloseableIterator<T> implements Iterator<T>{

	@Delegate
	private final Iterator<T> iterator;
	private final Continueable subscription;
	
	public void close(){
		subscription.closeAll();
	}
}
