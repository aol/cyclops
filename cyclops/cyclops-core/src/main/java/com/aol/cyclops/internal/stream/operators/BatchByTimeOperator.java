package com.aol.cyclops.internal.stream.operators;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops.data.collections.extensions.standard.ListXImpl;
import com.aol.cyclops.util.stream.StreamUtils;

import lombok.Value;
@Value
public class BatchByTimeOperator<T, C extends Collection<T>> {
	Stream<T> stream;
	Supplier<C> factory;
	public BatchByTimeOperator(Stream<T> stream){
		this.stream = stream;
		factory = ()-> (C)new ListXImpl<>();
	}
	public BatchByTimeOperator(Stream<T> stream2, Supplier<C> factory2) {
		this.stream=stream2;
		this.factory=factory2;
	}
	public Stream<C> batchByTime(long time, TimeUnit t){
		Iterator<T> it = stream.iterator();
		long toRun = t.toNanos(time);
		return StreamUtils.stream(new Iterator<C>(){
			long start = System.nanoTime();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}
			@Override
			public C next() {
				
				C list = factory.get();
				while(System.nanoTime()-start< toRun && it.hasNext()){
					list.add(it.next());
				}
				if(list.size()==0 && it.hasNext()) //time unit may be too small
					list.add(it.next());
				start = System.nanoTime();
				return list;
			}
			
		}).filter(l->l.size()>0);
	}
}
