package com.aol.cyclops.streams.operators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.data.collections.extensions.standard.ListXImpl;
import com.aol.cyclops.streams.StreamUtils;
@Value
public class BatchWhileOperator<T, C extends Collection<? super T>> {
	 private static final Object UNSET = new Object();
	Stream<T> stream;
	Supplier<C> factory;
	
	public BatchWhileOperator(Stream<T> stream){
		this.stream = stream;
		factory = ()-> (C)new ListXImpl();
	}
	public BatchWhileOperator(Stream<T> stream, Supplier<C> factory) {
		super();
		this.stream = stream;
		this.factory = factory;
	}
	
	public Stream<C> batchWhile(Predicate<? super T> predicate){
		Iterator<T> it = stream.iterator();
		return StreamUtils.stream(new Iterator<C>(){
			T value = (T)UNSET;
			@Override
			public boolean hasNext() {
				return value!=UNSET || it.hasNext();
			}
			@Override
			public C next() {
				
				C list = factory.get();
				if(value!=UNSET)
					list.add(value);
				T value;
				
label:					while(it.hasNext()) {
							value=it.next();
							list.add(value);
							
							if(!predicate.test(value)){
								value=(T)UNSET;
								break label;
							}
							value=(T)UNSET;
						
					}
				return list;
			}
			
		}).filter(l->l.size()>0);
	}


	
}
