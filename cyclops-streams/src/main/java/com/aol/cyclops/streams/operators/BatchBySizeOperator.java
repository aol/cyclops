package com.aol.cyclops.streams.operators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.streams.StreamUtils;
@Value
public class BatchBySizeOperator<T, C extends Collection<T>> {

	Stream<T> stream;
	Supplier<C> factory;
	public BatchBySizeOperator(Stream<T> stream){
		this.stream = stream;
		factory = ()-> (C)new ArrayList<>();
	}
	public BatchBySizeOperator(Stream<T> stream2, Supplier<C> factory2) {
		this.stream=stream2;
		this.factory=factory2;
	}
	public Stream<C> batchBySize(int groupSize){
		if(groupSize<1)
			throw new IllegalArgumentException("Batch size must be 1 or more");
		Iterator<T> it = stream.iterator();
		return StreamUtils.stream(new Iterator<C>(){
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public C next() {
				C list = factory.get();
				for (int i = 0; i < groupSize; i++) {
					if(it.hasNext())
						list.add(it.next());
					
				}
				return list;
			}
			
		});
	}
	
}
