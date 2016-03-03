package com.aol.cyclops.internal.stream.operators.futurestream;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;


public class SlidingWindow {
	
	public final static <T> Stream<ListX<T>> sliding(Stream<T> stream,int windowSize,int increment) {
		Iterator<T> it = stream.iterator();
		Mutable<PStackX<T>> list = Mutable.of(PStackX.empty());
		return stream(new Iterator<ListX<T>>(){
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public ListX<T> next() {
				for(int i=0;i<increment && list.get().size()>0;i++)
					 list.mutate(var -> var.minus(0));
				for (int i = 0; list.get().size() < windowSize && it.hasNext(); i++) {
					if(it.hasNext()){
						list.mutate(var -> var.plus(Math.max(0,var.size()),it.next()));
					}
					
				}
				return ListX.fromIterable(list.get());
			}
			
		});
	}
	public static <U> Stream<U> stream(Iterator<U> it){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED),
					false);
	}
}
