package com.aol.cyclops.lambda.api;

import java.util.List;
import java.util.stream.Collectors;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.invokedynamic.ReflectionCache;

public interface TupleWrapper {

	public Object getInstance();
	
	@SuppressWarnings("unchecked")
	default List<Object> values(){
		
		try {
			return	(List)ReflectionCache.getFields(getInstance().getClass()).stream().map(f ->{
				try {
				
					return f.get(getInstance());
				} catch (Exception e) {
					ExceptionSoftener.throwSoftenedException(e);
					return null;
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			ExceptionSoftener
					.throwSoftenedException(e);
			return null;
		}
	}
}