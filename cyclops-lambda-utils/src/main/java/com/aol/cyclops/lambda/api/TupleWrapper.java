package com.aol.cyclops.lambda.api;

import java.util.List;
import java.util.stream.Collectors;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public interface TupleWrapper {

	public Object getInstance();
	
	@SuppressWarnings("unchecked")
	default List<Object> values(){
		
		try {
			return	(List)ReflectionCache.getField(getInstance().getClass()).stream().map(f ->{
				try {
				
					return f.get(getInstance());
				} catch (Exception e) {
					ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
					return null;
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			ExceptionSoftener.singleton.factory.getInstance()
					.throwSoftenedException(e);
			return null;
		}
	}
}
