package com.aol.cyclops.streams.operators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.streams.StreamUtils;

@Value
public class WindowByTimeAndSizeOperator<T> {
	Stream<T> stream;
	public Stream<Streamable<T>> windowBySizeAndTime(int size, long time, TimeUnit t){
		Iterator<T> it = stream.iterator();
		long toRun = t.toNanos(time);
		return StreamUtils.stream(new Iterator<Streamable<T>>(){
			long start = System.nanoTime();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public Streamable<T> next() {
				
				
				List<T> list = new ArrayList<>();
				while(list.size()==0&& it.hasNext()){
					
					while(System.nanoTime()-start< toRun && it.hasNext() && list.size()<size){
						list.add(it.next());
					}
					start = System.nanoTime();
				}
			
				return Streamable.fromIterable(list);
			}
			
		});
	}
}
