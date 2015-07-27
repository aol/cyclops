package com.aol.cyclops.streams;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;

import com.aol.cyclops.lambda.api.Streamable;

@AllArgsConstructor
public class ReversedIterator<U> implements Streamable<U>{
	
	private final List<U> list;
	
	public List<U> getValue(){
		return  list;
	}
	
	public Stream<U> stream(){
		return StreamSupport.stream(
		          Spliterators.spliteratorUnknownSize(reversedIterator(), Spliterator.ORDERED),
		          false);
	}
	public Iterator<U> reversedIterator(){
		
		ListIterator<U> iterator = list.listIterator(list.size());
	
		return new Iterator<U>(){

			@Override
			public boolean hasNext() {
				return iterator.hasPrevious();
			}

			@Override
			public U next() {
				return iterator.previous();
			}
			
		};
	}
		
		
	
}
