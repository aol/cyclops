package com.aol.cyclops.react.stream.traits.operators;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;


public class SlidingWindow {
	
	public final static <T> Stream<List<T>> sliding(Stream<T> stream,int windowSize,int increment) {
		Iterator<T> it = stream.iterator();
		Mutable<PStack<T>> list = Mutable.of(ConsPStack.empty());
		return stream(new Iterator<List<T>>(){
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public List<T> next() {
				for(int i=0;i<increment && list.get().size()>0;i++)
					 list.mutate(var -> var.minus(0));
				for (int i = 0; list.get().size() < windowSize && it.hasNext(); i++) {
					if(it.hasNext()){
						list.mutate(var -> var.plus(Math.max(0,var.size()),it.next()));
					}
					
				}
				return list.get();
			}
			
		});
	}
	public static <U> Stream<U> stream(Iterator<U> it){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED),
					false);
	}
}
